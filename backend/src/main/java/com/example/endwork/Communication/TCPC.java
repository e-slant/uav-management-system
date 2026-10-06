package com.example.endwork.Communication;

import com.example.endwork.Page;
import com.example.endwork.struct.DataType;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalTime;

public class TCPC implements IComm{
    Socket socket;
    DataOutputStream dos;
    DataInputStream dis;
    Convert convert=new Convert();
    public void start(){
        String host= Page.AllPage.host;
        String port= Page.AllPage.port;
        try {
            System.out.println("建立tpc连接");
            socket=new Socket();
            socket.connect(new InetSocketAddress(host,Integer.parseInt(port)),10000);
            InputStream in=socket.getInputStream();
            OutputStream out=socket.getOutputStream();
            dos=new DataOutputStream(out);
            dis=new DataInputStream(in);
            System.out.println("服务已连接");
        }catch (SocketTimeoutException e) {
            try {
                System.out.println("tcp连接超时");
//                e.printStackTrace();
                if(socket!=null){
                    socket.close();
                }
            }catch (Exception e1){
                System.out.println("tpc连接关闭失败");
//                e1.printStackTrace();
            }
        }
        catch (Exception e){
            System.out.println("tpc错误,产生其他异常");
//            e.printStackTrace();
        }
    }
    @Override
    public void send(DataType dt) {
        try{
            dt.setCreateTime(dt.getTime(LocalTime.now()));
            dos.write(convert.convert((DataType) dt.renew()));
//            System.out.println("发送成功");
        }catch (Exception e){
            System.out.println("发送失败");
//            e.printStackTrace();
            System.out.println(e);
        }
    }
    @Override
    public DataType receive() {
        byte[] temp=new byte[79];
        try {
            dis.readFully(temp);

//            System.out.println("数据接收成功");
        }catch (IOException e){
            System.out.println("数据接收失败");
//            e.printStackTrace();
            System.out.println(e);
            return null;
        }
        System.out.println(convert.convert(temp));
        return convert.convert(temp);
    }
    @Override
    public void close() {
        if(socket!=null){
            try {
                dis.close();
                dos.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("TCP客户端连接关闭失败");
                System.out.println(e);
            }
        }
    }
}
