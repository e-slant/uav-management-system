package com.example.endwork.Communication;

import com.example.endwork.Page;
import com.example.endwork.struct.DataType;

import java.io.IOError;
import java.net.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UDP implements IComm{
    MulticastSocket socket;
    InetSocketAddress group;
    Convert convert=new Convert();
    public UDP(){}
    public void start(){
        try {
            System.out.println("建立udp连接");
            socket=new MulticastSocket(Integer.parseInt(Page.AllPage.port));
            group=new InetSocketAddress("224.0.0.100",Integer.parseInt(Page.AllPage.port));
            socket.joinGroup(group,null);
            System.out.println("UDP组播服务已启动，端口: " + Page.AllPage.port);
            System.out.println("组播地址: " + group.getAddress().getHostAddress());
        }
        catch (SocketTimeoutException e) {
            try {
                System.out.println("udp连接超时");
                if(socket!=null){
                    socket.close();
                }
            }catch (IOError e1){
                System.out.println("udp连接失败");
//                e1.printStackTrace();
            }
        }
        catch (Exception e){
            System.out.println("UDP服务因其他异常启动失败");
//            e.printStackTrace();
        }
    }
    @Override
    public void send(DataType dt) {
        dt.setCreateTime(dt.getTime(LocalTime.now()));
        byte[] data=convert.convert((DataType) dt.renew());
        DatagramPacket packet=new DatagramPacket(data,data.length,group.getAddress(),group.getPort());
        try {
            socket.send(packet);
            System.out.println("数据已发送");
        }catch (Exception e){
            System.out.println("UPD服务数据发送失败");
//            e.printStackTrace();
        }
    }
    public void sendAll( List<DataType> dts){
        //createtime
        int size=79*dts.size();
        byte[] data=new byte[size];
        int offset=0;

        for (DataType dt : dts) {
            dt.setCreateTime(dt.getTime(LocalTime.now()));
            byte[] temp = convert.convert(dt);
            System.arraycopy(temp, 0, data, offset, temp.length);
            offset += temp.length;
        }
        DatagramPacket packet=new DatagramPacket(data,data.length,group.getAddress(),group.getPort());
        try {
            socket.send(packet);
            System.out.println("批量消息已发送");
        }catch (Exception e){
            System.out.println("UDP服务批量消息发送失败");
//            e.printStackTrace();
        }
    }
    public List<DataType> receiveAll(){
        //delay
        Map<String,DataType> map=new HashMap<>();
        byte[] data=new byte[79*12];
        try {
            socket.setSoTimeout(50);
        } catch (SocketException e) {}
        while (true){
            DatagramPacket packet=new DatagramPacket(data,data.length);
            try {
                socket.receive(packet);
                int count=packet.getLength()/79;
                byte[] temp=new byte[79];
                for(int i=0;i<count;i++){
                    System.arraycopy(packet.getData(),i*79,temp,0,79);
                    DataType dt=convert.convert(temp);
                    map.put(dt.getNumber(),dt);
                }
                System.out.println("批量消息已接收");
            }catch (SocketTimeoutException e){
                break;
            }
            catch (Exception e){
                System.out.println("UDP服务批量消息接收失败,接收服务已关闭");
//                e.printStackTrace();
                break;
            }
        }
        try {
            socket.setSoTimeout(5000);
        }catch (Exception e){}
        return new ArrayList<>(map.values());
    }

    @Override
    public DataType receive() {
        byte[] data=new byte[79];
        DatagramPacket packet=new DatagramPacket(data,data.length);
        try {
            socket.receive(packet);
            System.out.println("数据已接收");
        }catch (Exception e){
            System.out.println("UDP服务数据接收失败");
//            e.printStackTrace();
        }
        DataType result=convert.convert(packet.getData());
        result.ComputingDelay(LocalTime.now());
        return result;
    }

    @Override
    public void close() {
        if(socket!=null){
            try {
                socket.leaveGroup(group,null);
                socket.close();
            }catch (Exception e){
                System.out.println("UDP服务关闭失败");
//                e.printStackTrace();
            }
        }
    }
}
