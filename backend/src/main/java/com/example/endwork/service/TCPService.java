package com.example.endwork.service;

import com.example.endwork.Communication.TCPS;
import com.example.endwork.Page;
import com.example.endwork.basecommand.CommandInvoker;
import com.example.endwork.basecommand.DataBase;
import com.example.endwork.basecommand.InsertCommand;
import com.example.endwork.struct.DataType;
import com.example.endwork.struct.PositArray;
import com.example.endwork.struct.Position;
import com.example.endwork.struct.TempData;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;


@Service
public class TCPService {
    private TCPS tcps;
    private final TempData tempData;
    private Thread thread;
    private boolean start=false;
    private final boolean issave=Page.AllPage.issave;

    public TCPService(TempData tempData) {
        this.tempData=tempData;
    }
    public void start(){
        if(!start){
            start=true;
            thread=new Thread(this::receive);
            thread.setDaemon(true);
            tcps=new TCPS();
            thread.start();
        }
    }
    private void receive(){
        tcps.start();
        long lastTime=0;
        DataBase db=Page.AllPage.DB;
        CommandInvoker invoker=new  CommandInvoker();
        PositArray pos=new PositArray();
        double oldx=0,oldy=0;
        while (!thread.isInterrupted()){
            try {
                DataType dt =tcps.receive();
                if(dt!=null){
                    double lon=dt.getLongitude(),lat=dt.getLatitude();
                    tempData.posPositAdd(dt.getNumber(),new Position(lon,lat));
                    tempData.dataTypeAdd(dt.getNumber(),(DataType) dt.renew());
                    if (pos.pos.isEmpty()||Math.abs(oldx-lon)>0.000001||Math.abs(oldy-lat)>0.000001){
                        if(pos.pos.isEmpty()){
                            pos.setTimestamp(new Timestamp(System.currentTimeMillis()));
                            pos.setNumber(dt.getNumber());
                        }
                        oldx=dt.getLongitude();
                        oldy=dt.getLatitude();
                        pos.addPos(new Position(oldx,oldy));
                    }
                    if(pos.pos.size()==10){
                        if(issave){
                            invoker.execute(new InsertCommand(pos,db));
                        }
                        pos.pos.clear();
                        pos.addPos(new Position(oldx,oldy));
                    }
                    long currentTime=System.currentTimeMillis();
                    if((currentTime-lastTime)>=10000){
                        if(issave){
                            invoker.execute(new InsertCommand(dt,db));
                        }
                        lastTime=currentTime;
                    }
                }
                Thread.sleep(500);
            }  catch (InterruptedException e) {
                System.out.println("接收线程被中断，准备退出...");
//                e.printStackTrace();
                break;
            } catch (Exception e) {
                // 捕获网络异常、Socket关闭异常等，防止线程崩溃
                System.out.println("TCP接收发生异常: " + e.getMessage());
//                e.printStackTrace();
                break; // 发生异常时退出循环，避免死循环刷屏
            }
        }
        System.out.println("TCP接收线程已停止。");
    }
    public void send(Position p){
        if(tcps==null)return;
        DataType dt=new DataType(p);
        tcps.send(dt);
        System.out.println("发送目标位置:"+p);
    }
    @PreDestroy
    public void stop(){
        if(start){
            start=false;
            if(thread!=null){
                thread.interrupt();
            }
            if(tcps!=null){
                tcps.close();
            }
            System.out.println("tcp服务关闭");
        }
    }
}
