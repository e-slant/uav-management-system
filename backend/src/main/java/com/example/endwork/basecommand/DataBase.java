package com.example.endwork.basecommand;

import com.example.endwork.struct.DataType;
import com.example.endwork.Page;
import com.example.endwork.struct.IEntityStruct;
import com.example.endwork.struct.PositArray;
import org.yaml.snakeyaml.util.Tuple;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DataBase {
    private Connection con=null;
    private final Object key="key";

    //增删改查，连接，关闭
    public DataBase() throws SQLException {
        String db_url="jdbc:mysql://"+ Page.AllPage.host+":3306?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        con= DriverManager.getConnection(db_url,Page.AllPage.userName,Page.AllPage.password);
        initDataBase();
    }

    private void initDataBase() throws SQLException {
        PreparedStatement pst = null;
        try{
            String creatBasesql="CREATE DATABASE IF NOT EXISTS "+Page.AllPage.database;
            pst=con.prepareStatement(creatBasesql);
            System.out.println("正在创建数据库...");
            pst.execute();
            pst=con.prepareStatement("use "+Page.AllPage.database);
            pst.execute();
            System.out.println("正在创建数据表...");
            String datasql="CREATE TABLE IF NOT EXISTS " +Page.AllPage.tableName[0]+
                    "    (id INT AUTO_INCREMENT PRIMARY KEY," +
                    "    `number` VARCHAR(5)," +
                    "    longitude DOUBLE," +
                    "    latitude DOUBLE," +
                    "    flip SMALLINT," +
                    "    pitch SMALLINT," +
                    "    `state` VARCHAR(2)," +
                    "    height INT," +
                    "    createTime VARCHAR(8)," +
                    "    delay BIGINT" +
                    ")";
            pst=con.prepareStatement(datasql);
            pst.execute();
            String possql="CREATE TABLE IF NOT EXISTS "+Page.AllPage.tableName[1]+" (" +
                    "    `id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "    `number` VARCHAR(5)," +
                    "    `x1` DOUBLE," +
                    "    `y1` DOUBLE," +
                    "    `x2` DOUBLE," +
                    "    `y2` DOUBLE," +
                    "    `x3` DOUBLE," +
                    "    `y3` DOUBLE," +
                    "    `x4` DOUBLE," +
                    "    `y4` DOUBLE," +
                    "    `x5` DOUBLE," +
                    "    `y5` DOUBLE," +
                    "    `x6` DOUBLE," +
                    "    `y6` DOUBLE," +
                    "    `x7` DOUBLE," +
                    "    `y7` DOUBLE," +
                    "    `x8` DOUBLE," +
                    "    `y8` DOUBLE," +
                    "    `x9` DOUBLE," +
                    "    `y9` DOUBLE," +
                    "    `x10` DOUBLE," +
                    "    `y10` DOUBLE," +
                    "`createtime` timestamp" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            pst=con.prepareStatement(possql);
            pst.execute();
            pst.close();
        }catch (SQLException e){
            System.out.println("数据库初始化失败");
            if (pst != null) {
                pst.close();
            }
            throw new SQLException(e.getMessage());
        }
    }

    public Tuple<Integer, IEntityStruct> selectItem(int id, String table) throws SQLException {
        String sql="select * from "+table+" where id=?";
        try(PreparedStatement pst=con.prepareStatement(sql)){
            pst.setInt(1,id);
            try(ResultSet rs=pst.executeQuery()){
                if(rs.next()){
                    if(table.equals(Page.AllPage.tableName[0])){
                        DataType dt =new DataType(rs);
                        return new Tuple<>(rs.getInt(1),dt);
                    }else if(table.equals(Page.AllPage.tableName[1])){
                        PositArray pa=new PositArray(rs);

                        return new Tuple<>(rs.getInt(1),pa);
                    }else {
                        throw  new SQLException("数据表的名称不存在");
                    }
                }else {
                    return null;
                }
            }
        }
    }

    public Map<Integer,IEntityStruct>  selectAllData(String table) throws SQLException {
        Map<Integer,IEntityStruct> map=new HashMap<>();
        String sql="select * from "+table;
        try(PreparedStatement pst=con.prepareStatement(sql)){
            try(ResultSet rs=pst.executeQuery()){
                if(table.equals(Page.AllPage.tableName[0])){
                    while(rs.next()){
                        map.put(rs.getInt(1),new DataType(rs));
                    }
                }else if(table.equals(Page.AllPage.tableName[1])){
                    while(rs.next()){
                        map.put(rs.getInt(1),new PositArray(rs));
                    }
                }else {
                    throw new SQLException("数据表的名字不存在");
                }
                return map;
            }
        }
    }

    public void insertData(int id, IEntityStruct st) throws SQLException {
        if(st instanceof DataType dt) {
            String sql="insert into "+Page.AllPage.tableName[0]+"(id,number,longitude,latitude,flip,pitch,state,height,createTime,delay) values(?,?,?,?,?,?,?,?,?,?)";

            try(PreparedStatement pst=con.prepareStatement(sql)){
                pst.setInt(1,id);
                dt.getPst(pst,2);
                pst.execute();
            }
        }else if(st instanceof PositArray pa){
            String sql="insert into "+Page.AllPage.tableName[1]+"(id,number,x1,y1,x2,y2,x3,y3,x4,y4,x5,y5,x6,y6,x7,y7,x8,y8,x9,y9,x10,y10,createtime) values(?,?";
            String sb = sql +
                    ",?".repeat(21) +
                    ")";
            try(PreparedStatement pst=con.prepareStatement(sb)){
                pst.setInt(1,id);
                pa.getPst(pst,2);
                pst.execute();
            }
        }else {
            throw new SQLException("插入错误");
        }
    }//插入id

    public void updateData(int id,IEntityStruct st) throws SQLException {
        if(st instanceof DataType dt) {
            String sql="update " + Page.AllPage.tableName[0] + " set number=?,longitude=?,latitude=?,flip=?,pitch=?,state=?,height=?,createTime=?,delay=? where id=?";
            try(PreparedStatement pst=con.prepareStatement(sql)){
                dt.getPst(pst,1);
                pst.setInt(10,id);
                pst.execute();
            }
        }else if(st instanceof PositArray pa){
            String sql="update " + Page.AllPage.tableName[1] + " set number=?,x1=?,y1=?,x2=?,y2=?,x3=?,y3=?,x4=?,y4=?,x5=?,y5=?,x6=?,y6=?,x7=?,y7=?,x8=?,y8=?,x9=?,y9=?,x10=?,y10=?,createtime=? where id=?";
            try(PreparedStatement pst=con.prepareStatement(sql)){
                pa.getPst(pst,1);
                pst.setInt(23,id);
                pst.execute();
            }
        }else{
            throw new SQLException("索引不存在");
        }
    }

    public void deleteData(int id,String table) throws SQLException {
        String sql="delete from "+table+ " where id=?";
        try(PreparedStatement pst=con.prepareStatement(sql)){
            pst.setInt(1,id);
            pst.execute();
        }

    }

    public int getId(String table) throws SQLException {
        synchronized (key){
            String sql="SELECT max(id) from "+table;
            try(PreparedStatement pst=con.prepareStatement(sql)){
                try(ResultSet rs=pst.executeQuery()){
                    if(rs.next()){
                        return rs.getInt(1);
                    }else {
                        throw new SQLException("未获取到最新id");
                    }
                }
            }
        }
    }

    public void close(){
        try{
            if(con!=null){
                con.close();
            }
        }catch(SQLException e){
            System.out.println(e.toString());
        }
    }
}
