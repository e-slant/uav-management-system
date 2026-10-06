package com.example.endwork.service;

import com.example.endwork.Communication.UDP;
import com.example.endwork.Page;
import com.example.endwork.basecommand.CommandInvoker;
import com.example.endwork.basecommand.DataBase;
import com.example.endwork.basecommand.InsertCommand;
import com.example.endwork.struct.DataType;
import com.example.endwork.struct.PositArray;
import com.example.endwork.struct.Position;
import com.example.endwork.struct.TempData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UDPService {
    private final TempData tempData;
    private final UDP udp;
    private Thread thread;
    private final boolean issave=Page.AllPage.issave;
    private boolean start=false;
    public UDPService(TempData tempData) {
        this.tempData = tempData;
        udp=new UDP();
    }
    public void start(){
        if(!start){
            start=true;
            thread=new Thread((this::receive));
            udp.start();
            thread.start();
        }
    }
    public void send(String number, Position position){
        if(number!=null&&!number.isEmpty()&&position!=null){
            DataType data=new DataType(position);
            data.setNumber(number);
            udp.send(data);
            System.out.println("已发送给"+number+"无人机,位置:"+position);
        }
    }

    private void receive(){
        CommandInvoker invoker=new CommandInvoker();
        long lasttime=0;
        long currenttime;
        Map<String, PositArray> mps=new HashMap<>();
        DataBase db=Page.AllPage.DB;
        while (!thread.isInterrupted()){
            try {
                List<DataType> ds=udp.receiveAll();
                if(ds.isEmpty()){
                    try {
                        Thread.sleep(20);
                    }catch(InterruptedException e){
//                        e.printStackTrace();
                        break;
                    }
                    continue;
                }
                for(DataType d:ds){
                    System.out.println(d);
                }
                currenttime=System.currentTimeMillis();
                if((currenttime-lasttime)>=10000){
                    for(DataType d:ds){
                        if(issave){
                            invoker.execute(new InsertCommand(d,db));

                        }
                    }
                    lasttime=System.currentTimeMillis();
                }
                for(DataType d:ds){
                    if (d.getSpeed()<=0)continue;
                    String number=d.getNumber();
                    if (number == null || number.isEmpty()) {
                        continue;
                    }
                    double lat=d.getLatitude();
                    double lon=d.getLongitude();
                    d.ComputingDelay(LocalTime.now());
                    tempData.dataTypeAdd(d.getNumber(),(DataType) d.renew());
                    tempData.posPositAdd(d.getNumber(),new Position(lon,lat));
                    if(!mps.containsKey(number)){
                        PositArray pa =new PositArray();
                        pa.setNumber(number);
                        pa.setTimestamp(new Timestamp(System.currentTimeMillis()));
                        pa.addPos(new Position(lon,lat));
                        mps.put(number,pa);
                    }else {
                        PositArray pa=mps.get(number);
                        Position lp=pa.getLastP();
                        if(lp!=null&&Math.abs(lp.getX()-lon)>0.000001&&Math.abs(lp.getY()-lat)>0.000001){
                            pa.addPos(new Position(lon,lat));
                        }
                        if(pa.pos.size()==10){
                            if(issave){
                                invoker.execute(new InsertCommand(pa,db));
                            }
                            pa.pos.clear();
                            pa.addPos(pa.getLastP());
                        }
                    }
                }
            }catch (Exception e){
                System.out.println("Udp服务的接收已结束");
//                e.printStackTrace();
                break;
            }
        }
    }
    public void stop(){
        if(start){
            start=false;
            if(thread!=null){
                thread.interrupt();
            }
            udp.close();
        }
    }
}
