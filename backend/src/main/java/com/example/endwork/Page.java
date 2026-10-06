package com.example.endwork;

import com.example.endwork.basecommand.CommandInvoker;
import com.example.endwork.basecommand.DataBase;

import java.util.Scanner;

public class Page {
    private Page(){};
    static public class AllPage{
        public static String database="DroneSys";
        public static String[] tableName={"DroneData","PositData"};
        public static String userName="$$$";
        public static String password="$$$";
        public static String host="localhost";
        public static String port="7778";
        public static boolean isTcp=false;
        public static DataBase DB;
        public static boolean issave=false;
    }
    static public void initPage(){
        printl("这里是无人机系统基站端，请进行您的基站参数配置");
        Scanner sc = new Scanner(System.in);
        print("数据库名称:");
        AllPage.database =sc.nextLine();
        print("表名称（逗号分割多个表名,第一个表是无人机信息表，第二个表是无人机路径表）:");
        AllPage.tableName = sc.nextLine().trim().split(",");
        print("数据库用户名:");
        AllPage.userName = sc.nextLine();
        print("数据库密码:");
        AllPage.password = sc.nextLine();
        print("本机ip/host:");
        AllPage.host = sc.nextLine();
        print("基站运行端口号:");
        AllPage.port = sc.nextLine();
        int temp;
        while (true) {
            try{
                print("协议选择(1.TCP(单无人机通信),2.UDP(多无人机通信))");
                temp =sc.nextInt();
                break;
            }catch (Exception e){
                printl(e.toString());
                sc.nextLine();
            }
        }
        if(temp==1){
            AllPage.isTcp=true;
        }else {
            AllPage.isTcp=false;
        }
        printl("基站服务配置完毕，服务器准备正常运行");
    }
    static public void switchAgree(){
        AllPage.isTcp=!AllPage.isTcp;
    }
    static private void printl(String s){
        System.out.println(s);
    }
    static private void print(String s){
        System.out.print(s);
    }
}
