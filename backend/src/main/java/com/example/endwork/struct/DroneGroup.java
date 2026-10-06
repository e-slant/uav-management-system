package com.example.endwork.struct;

import com.example.endwork.Communication.Convert;
import com.example.endwork.Communication.UDP;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DroneGroup {
    private final ConcurrentHashMap<String,DataType> drones=new ConcurrentHashMap<>();
    private final TempData tempData;
    private UDP udp;
    private  ScheduledExecutorService scheduler;
    Thread thread;
    private boolean start=false;

    public DroneGroup(TempData tempData) {
        this.tempData = tempData;
    }
    public void start(){
        if(!start){
            start=true;
            udp=new UDP();
            udp.start();
            thread=new Thread(this::receive,"udp-receive-thread");
            thread.setDaemon(true);
            thread.start();
            scheduler= Executors.newScheduledThreadPool(2);
            scheduler.scheduleAtFixedRate(this::tick, 0, 1000, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(this::send, 0, 2000, TimeUnit.MILLISECONDS);
        }
    }
    public void stop(){
        if(start){
            start=false;
            if (udp!=null){
                udp.close();
            }
            if(thread!=null){
                thread.interrupt();
            }
            if(scheduler!=null){
                scheduler.shutdown();
            }
            System.out.println("udp服务已关闭");
        }
    }

    private void receive(){
        while (!thread.isInterrupted()){
            try{
                DataType dt =udp.receive();
                if(dt!=null&&(dt.getNumber()!=null&&!dt.getNumber().isEmpty())&&dt.getTarget()!=null){
                    if(drones.containsKey(dt.getNumber())){
                        drones.get(dt.getNumber()).setTarget(dt.getTarget());
                    }
                }
                Thread.sleep(500);
            }catch (Exception e){
//                e.printStackTrace();
                break;
            }
        }
    }
    private void tick(){
        for(DataType dt:drones.values()){
            dt.ModifyPosition();
            dt.computeHeading();
        }
    }
    private void send(){
        List<DataType> dts = new ArrayList<>(drones.values());
        if(!dts.isEmpty()){
            udp.sendAll(dts);
        }
    }
    public void addDrone(DataType dt){
        String number = dt.getNumber();
        if (number != null && !number.trim().isEmpty() && dt.getSpeed() > 0) {
            drones.putIfAbsent(number, dt);
        }
    }
    public void removeDrone(String number){
        if(number!=null&&!number.isEmpty()){
            drones.remove(number);
            tempData.allRemove(number);
        }
    }
    public void clearDrones(){
        drones.clear();
    }
}
