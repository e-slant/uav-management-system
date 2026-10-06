package com.example.endwork;

import com.example.endwork.Communication.IComm;
import com.example.endwork.Communication.UDP;
import com.example.endwork.basecommand.DataBase;
import com.example.endwork.service.InitService;
import com.example.endwork.service.TCPClient;
import com.example.endwork.service.TCPService;
import com.example.endwork.service.UDPService;
import com.example.endwork.struct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class Test implements CommandLineRunner {

    private final InitService initService;
    public Test(InitService initService) {
        this.initService = initService;
    }

    @Override
    public void run(String... args) throws Exception {
        //main
        //page服务初始化
        System.out.println("是否进行配置服务");
        System.out.println("1.是     2.否(5秒内无输入自动选否)");
        String input = getString();
        if(input!=null&&input.equals("1")){
            Page.initPage();
        }
        System.out.println("是否开启数据库持久化和日志存储");
        System.out.println("1.是     2.否(5秒内无输入自动选否)");
        input = getString();
        if(input!=null&&input.equals("1")){
            Page.AllPage.issave =true;
        }
        initService.isinit=true;
        initService.doInit();
        System.out.println("是否开启随机模拟");
        System.out.println("1.是    2.否 (5秒内无输入自动选择否)");
        input = getString();
        if(input != null&&input.equals("1")){
            if (Page.AllPage.isTcp){
                Random random = new Random();
                initService.tcpService.start();
                DataType dataType = new DataType(new Position(random.nextDouble(180),random.nextDouble(90)));
                dataType.setSpeed(random.nextDouble(10));
                initService.tcpClient.start();
                System.out.println("模拟目标发送中");
                initService.tcpService.send(new Position(random.nextDouble(180),random.nextDouble(90)));
                Thread.sleep(15000);
                initService.tcpClient.stop();
                initService.tcpService.stop();
                System.out.println("模拟完毕");
            }else {
                Random random = new Random();
                DroneGroup group=initService.droneGroup;
                DataType d1=new DataType(new Position(random.nextDouble(180),random.nextDouble(90)));
                d1.setSpeed(random.nextDouble(10));
                DataType d2=new DataType(new Position(random.nextDouble(180),random.nextDouble(90)));
                d2.setSpeed(random.nextDouble(10));
                group.addDrone(d1);
                UDPService udp =initService.udpService;
                udp.start();
                group.start();
                group.addDrone(d2);
                System.out.println("目标发送...");
                udp.send(d1.getNumber(),new Position(random.nextDouble(180),random.nextDouble(90)));
                udp.send(d2.getNumber(),new Position(random.nextDouble(180),random.nextDouble(90)));
                Thread.sleep(20000);
                group.stop();
                udp.stop();
            }
        }
        initService.doInit();
        initService.isinit=false;
    }

    private static String getString() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = null;
        long startTime = System.currentTimeMillis();
        int timeout = 5000; // 设置超时时间为 5000 毫秒 (5秒)
        try {
            // 循环检查是否超时
            while (System.currentTimeMillis() - startTime < timeout) {
                // reader.ready() 返回 true 表示用户输入了内容并按了回车
                if (reader.ready()) {
                    input = reader.readLine();
                    break; // 读到输入，跳出循环
                }
                // 休眠 100 毫秒，避免死循环空转占用过多 CPU 资源
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return input;
    }

}
