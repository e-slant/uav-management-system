package com.example.endwork.service;

import com.example.endwork.Communication.UDP;
import com.example.endwork.Page;
import com.example.endwork.basecommand.DataBase;
import com.example.endwork.struct.DroneGroup;
import com.example.endwork.struct.TempData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class InitService {
    public final DroneGroup droneGroup;
    public final TempData tempData;
    public final TCPClient tcpClient;
    public final TCPService tcpService;
    public final UDPService udpService;
    public boolean isinit=false;
    public InitService(TempData tempData,TCPService tcpService,TCPClient tcpClient,DroneGroup droneGroup,UDPService udpService) {
        this.tempData = tempData;
        this.tcpService = tcpService;
        this.tcpClient = tcpClient;
        this.droneGroup = droneGroup;
        this.udpService = udpService;
    }

    public void doInit(){
        System.out.println("正在初始化...");
        //无人机池清空
        tempData.clearAll();
        tcpClient.stop();
        tcpService.stop();
        udpService.stop();
        droneGroup.clearDrones();
        droneGroup.stop();
        try {
            Page.AllPage.DB=new DataBase();
        } catch (SQLException e) {
            System.out.println("数据库创建失败，请检查数据库配置");
            throw new RuntimeException(e);
        }
        System.out.println("初始化完成");
    }
}
