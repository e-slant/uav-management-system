package com.example.endwork.struct;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempData {
    private final Map<String,DataType> dt=new ConcurrentHashMap<>();
    private final Map<String,PositArray> pos=new ConcurrentHashMap<>();
    public void setDt(String key,DataType dt){
        DataType temp = new DataType();
        temp.setNumber(dt.getNumber());
        temp.setLongitude(dt.getLongitude());
        temp.setLatitude(dt.getLatitude());
        temp.setFlip(dt.getFlip());
        temp.setPitch(dt.getPitch());
        temp.setState(dt.getState());
        temp.setCreateTime(dt.getCreateTime());
        temp.setDelay(dt.getDelay());
        temp.setHeading(dt.getHeading());
        this.dt.put(key, temp);
    }

    public Map<String, DataType> getDtAll(){
        return dt;
    }
    public Map<String, PositArray> getPosAll(){
        return pos;
    }
    public void dataTypeAdd(String key, DataType dt){
        DataType temp=(DataType) dt.renew();
        this.dt.put(key,temp);
    }
    public void allRemove(String key){
        if(!key.isEmpty()){
            boolean ls=dt.containsKey(key);
            this.dt.remove(key);
            this.pos.remove(key);
        }
    }
    public void posPositAdd(String key,Position p){
        PositArray pa=this.pos.computeIfAbsent(key,k->{
            PositArray temp=new PositArray();
            temp.setNumber(key);
            return temp;
        });
        pa.addPos(p);
    }

    public void setPos(String key,PositArray pos){
        PositArray temp = new PositArray();
        temp.setNumber(key);
        temp.pos = new LinkedList<>(pos.pos); // 直接替换引用
        this.pos.put(key, temp);

    }
    public DataType getDt(String key) {
        return dt.get(key);
    }

    public PositArray getPos(String key) {
        return pos.get(key);
    }
    public void  clearAll(){
        this.dt.clear();
        this.pos.clear();
    }
}
