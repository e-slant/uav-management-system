package com.example.endwork.Communication;

import com.example.endwork.Page;
import com.example.endwork.struct.DataType;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalTime;

public class TCPS implements IComm{
    ServerSocket serverSocket;
    Socket socket;
    Convert convert=new Convert();
    DataOutputStream dos;
    DataInputStream dis;

    public void start(){
        int port=Integer.parseInt(Page.AllPage.port);
        try {
            System.out.println("建立tpc连接");
            serverSocket =new ServerSocket(port);
            serverSocket.setSoTimeout(10000);
            socket=serverSocket.accept();
            InputStream in=socket.getInputStream();
            OutputStream out=socket.getOutputStream();
            dos=new DataOutputStream(out);
            dis=new DataInputStream(in);
            System.out.println("服务已创建");
        }catch (SocketTimeoutException e) {
            System.out.println("等待客户端连接超时: " + e);
            // 超时后关闭ServerSocket，避免资源泄漏
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ex) {
                System.out.println("关闭ServerSocket失败: " + ex);
            }
        }
        catch (IOException e){
            System.out.println(e);
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
            System.out.println(e);
            return null;
        }
        DataType result=convert.convert(temp);
        result.ComputingDelay(LocalTime.now());
        return result;
    }

    @Override
    public void close() {
        if(serverSocket!=null){
            try{
                dos.close();
                dis.close();
                socket.close();
                serverSocket.close();
            }catch (IOException e){
                System.out.println("TCP服务端关闭失败");
                System.out.println(e);
            }
        }
    }
}
