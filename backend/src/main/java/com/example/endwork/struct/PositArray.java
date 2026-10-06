package com.example.endwork.struct;

import com.example.endwork.Page;
import com.example.endwork.basecommand.DataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;


public class PositArray implements IEntityStruct{
    private int id;
    private String number;
    private Timestamp timestamp=Timestamp.valueOf("2020-01-01 00:00:00");
    private Position lastP;
    public Queue<Position> pos=new LinkedList<>();
    public synchronized void addPos(Position pos){
        if(this.pos.size()==10){
            this.pos.remove();
            this.pos.add(pos);
        }else {
            this.pos.add(pos);
        }
        lastP=new Position(pos.getX(),pos.getY());
    }
    public Position getLastP(){
        return lastP;
    }
    @Override
    public String getTableName() {
        return Page.AllPage.tableName[1];
    }
    public void getPst(PreparedStatement pst,int position)throws SQLException{
        position-=1;
        pst.setString(++position,number);
        for (Position po : pos) {
            pst.setDouble(++position, po.getX());
            pst.setDouble(++position, po.getY());
        }
        timestamp=new Timestamp(System.currentTimeMillis());
        pst.setTimestamp(++position,timestamp);
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public PositArray(){}
    public PositArray(Queue<Position> position){
        this.pos=new LinkedList<>(position);
    }
    public PositArray(ResultSet rs) throws SQLException {
        number=rs.getString("number");
        timestamp=rs.getTimestamp("createtime");
        for(int i=1;i<=10;i++){
            pos.add(new Position(rs.getDouble("x"+i),rs.getDouble("y"+i)));
        }
    }
    @Override
    public String toString() {
        return number+","+pos.stream().map(Position::toString).collect(Collectors.joining(","))+","+timestamp.toString();
    }

    @Override
    public IEntityStruct renew() {
        return new PositArray(pos);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        if(number==null||number.isEmpty()){
            this.number="00000";
        }else {
            if(number.length()<=5){
                StringBuilder builder = new StringBuilder();
                if(number.length()<5){
                    for(int i=number.length();i<5;i++){
                        builder.append('0');
                    }
                }
                builder.append(number);
                this.number = builder.toString();
            }else {
                System.out.println("编号赋值失败");
                this.number =null;
            }
        }
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        if(timestamp==null){
            timestamp=new Timestamp(System.currentTimeMillis());
        }
    }
}
