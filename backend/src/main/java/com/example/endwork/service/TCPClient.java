package com.example.endwork.service;

import com.example.endwork.Communication.TCPC;
import com.example.endwork.state.IState;
import com.example.endwork.state.Normal;
import com.example.endwork.struct.DataType;
import com.example.endwork.struct.Position;
import com.example.endwork.struct.TempData;
import org.springframework.stereotype.Service;


import java.time.LocalTime;
@Service
public class TCPClient {
    private final TempData tempData;
    private Thread sThread;
    private TCPC tcpc;
    private Thread rThread;
    private DataType dt;
    private Thread tThread;
    private boolean start=false;

    public TCPClient(TempData tempData) {

        tcpc=new TCPC();
        this.tempData = tempData;
    }
    public void addDrone(DataType dataType){
        dt=(DataType) dataType.renew();
    }
    public void start(){
        if(!start){
            start=true;
            sThread=new Thread(this::send);
            sThread.setName("tcpSend");
            sThread.setDaemon(true);
            rThread=new Thread(this::receive);
            rThread.setName("tcpReceive");
            rThread.setDaemon(true);
            tThread=new Thread(this::tick);
            tThread.setName("tcpTick");
            tThread.setDaemon(true);

            rThread.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
            sThread.start();
            tThread.start();
        }
    }

    private void send(){
        IState state=new Normal();
        while (!sThread.isInterrupted()){
            try {
                Thread.sleep(2000);

            synchronized (dt){
                dt.setCreateTime(dt.getTime(LocalTime.now()));
                dt.setState(state);
                tcpc.send(dt);
                System.out.println("无人机当前信息已发送:"+dt);
            }

            }catch (InterruptedException e) {
                System.out.println("发送线程被中断，退出");
//                e.printStackTrace();
                System.out.println(e.getMessage());
                break;
            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println("发送网络异常: " + e.getMessage());
                break;
            }
        }
    }
    private void tick(){
        while(!tThread.isInterrupted()){
            try {
                Thread.sleep(500);
                synchronized (dt){
                this.dt.ModifyPosition();
                this.dt.computeHeading();
            }

            }catch (InterruptedException e) {
                System.out.println("移动线程被中断，退出");
//                e.printStackTrace();
                break;
            } catch (Exception e) {
                System.out.println("移动计算发生异常: " + e.getMessage());
//                e.printStackTrace();
                break; // 或者不 break，根据业务容错需求决定是否继续重试
            }
        }
    }
    private void receive(){
        tcpc.start();
        while (!rThread.isInterrupted()){
            DataType rdt= tcpc.receive();
            if(rdt!=null){
                Position newt=rdt.getTarget();
                synchronized (dt){
                    dt.setTarget(newt);
                }
                System.out.println("目标坐标:"+dt.getTarget()+",当前速度:"+dt.getSpeed());
            }
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e) {
                System.out.println("接收线程被中断，退出");
                System.out.println(e.getMessage());
//                e.printStackTrace();
                break;
            } catch (Exception e) {
                System.out.println("接收异常或连接关闭: " + e.getMessage());
//                e.printStackTrace();
                break;
            }
        }
    }
    public void removeDrone(String key){
        tempData.allRemove(key);
    }
    public void stop(){
        if(start){
            start=false;
            if(sThread!=null){
                sThread.interrupt();
            }
            if(rThread!=null){
                rThread.interrupt();
            }
            if(tThread!=null){
                tThread.interrupt();
            }
            if(tcpc!=null){
                tcpc.close();
            }
            System.out.println("tcp客户端已停止");
        }
    }
}
