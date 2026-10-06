package com.example.endwork.Communication;

import com.example.endwork.struct.DataType;
import com.example.endwork.struct.Position;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Convert {
    public byte[] convert(DataType dt){
        ByteBuffer buffer = ByteBuffer.allocate(79);
        buffer.put(dt.getNumber().getBytes());
        buffer.putDouble(dt.getLongitude());
        buffer.putDouble(dt.getLatitude());
        buffer.putShort(dt.getFlip());
        buffer.putShort(dt.getPitch());
        buffer.put(dt.getState().toString().getBytes(StandardCharsets.UTF_8));
        buffer.putInt(dt.getHeight());
        buffer.put(dt.getCreateTime().getBytes(StandardCharsets.UTF_8));
        buffer.putLong(dt.getDelay());
        buffer.putDouble(dt.getTarget().getX());
        buffer.putDouble(dt.getTarget().getY());
        buffer.putDouble(dt.getSpeed());
        buffer.putDouble(dt.getHeading());
        return buffer.array();
    }
    public DataType convert(byte[] bt){
        ByteBuffer buffer = ByteBuffer.wrap(bt);
        byte[] number = new byte[5];
        buffer.get(number);
        double dLongitude=buffer.getDouble();
        double dLatitude=buffer.getDouble();
        short sFlip=buffer.getShort();
        short sPitch=buffer.getShort();
        byte[] state = new byte[2];
        buffer.get(state);
        int iHeight=buffer.getInt();
        byte[] time=new byte[8];
        buffer.get(time);
        long delay=buffer.getLong();
        double x=buffer.getDouble();
        double y=buffer.getDouble();
        double speed=buffer.getDouble();
        double heading=buffer.getDouble();
        DataType dt=new DataType(new String(number),dLongitude,dLatitude
                ,sFlip,sPitch,new String(state)
                ,iHeight,new String(time),delay,heading);
        dt.setTarget(new Position(x,y));
        dt.setSpeed(speed);
        return dt;
    }
}
