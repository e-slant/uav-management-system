package com.example.endwork.struct;

import com.example.endwork.Page;
import com.example.endwork.state.Error;
import com.example.endwork.state.IState;
import com.example.endwork.state.Normal;
import com.example.endwork.state.Warning;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DataType implements IEntityStruct {
    private int id;
    private String number;
    private double longitude;//x
    private double latitude;//y
    private short flip;
    private short pitch;
    private IState state;
    private int height;
    private String createTime;//客户端创建
    private long delay;
    private double heading;
    private Position target;//客户端解析
    private double speed;//客户端解析
    public DataType() {
        setId(0);
        setNumber("00000");
        setLongitude(0);
        setLatitude(0);
        setFlip((short) 0);
        setPitch((short) 0);
        setState(new Normal());
        setDelay(0);
        setTarget(new Position(0, 0));
        setCreateTime(null);
        setSpeed(0);
        setHeight(0);
    }
    public DataType(ResultSet rs) throws SQLException {
        this.number=rs.getString(2);
        this.longitude=rs.getDouble(3);
        this.latitude=rs.getDouble(4);
        this.flip=rs.getShort(5);
        this.pitch=rs.getShort(6);
        setState(rs.getString(7));
        this.height=rs.getInt(8);
        this.createTime=rs.getString(9);
        this.delay=rs.getLong(10);
    }
    public DataType(int id,String number, double longitude, double latitude, short flip, short pitch, String state
    , int height, String createTime, long delay,double speed,double heading) {
        setId(id);
        setNumber(number);
        setLongitude(longitude);
        setLatitude(latitude);
        setFlip(flip);
        setPitch(pitch);
        setState(state);
        setHeight(height);
        setCreateTime(createTime);
        setDelay(delay);
        setTarget(new Position(longitude,latitude));
        setSpeed(speed);
        setHeading(heading);
    }
    public DataType(String number, double longitude, double latitude, short flip, short pitch, String state
            , int height, String createTime, long delay,double heading) {
        setNumber(number);
        setLongitude(longitude);
        setLatitude(latitude);
        setFlip(flip);
        setPitch(pitch);
        setState(state);
        setHeight(height);
        setCreateTime(createTime);
        setDelay(delay);
        setTarget(new Position(longitude,latitude));
        setSpeed(0);
        setHeading(heading);
    }
    public DataType(Position p){
        setLongitude(p.getX());
        setLatitude(p.getY());
        numberBuild();
        setFlip((short) 0);
        setPitch((short) 0);
        setState("00");
        setHeight(0);
        setCreateTime(null);
        setDelay(0);
        setTarget(new Position(longitude,latitude));
        setSpeed(0);
    }
    @Override
    public String toString() {
        return id+","+number+","+longitude+","+latitude+","+flip+","+pitch+","+state+","+height+","+createTime+","+delay;
    }
    public void getPst(PreparedStatement pst,int position) throws SQLException {
//        pst.setInt(++i,id);
        position-=1;
        pst.setString(++position,number);
        pst.setDouble(++position,longitude);
        pst.setDouble(++position,latitude);
        pst.setShort(++position,flip);
        pst.setShort(++position,pitch);
        pst.setString(++position,state.toString());
        pst.setInt(++position,height);
        pst.setString(++position,createTime);
        pst.setLong(++position,delay);
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getTableName() {
        return Page.AllPage.tableName[0];
    }
    public void setData(String number, double longitude, double latitude, short flip, short pitch, String state, int height, String createTime, long delay) {
        this.number = number;
        this.longitude = longitude;
        this.latitude = latitude;
        this.flip = flip;
        this.pitch = pitch;
        this.setState(state);
        this.height = height;
        this.createTime=createTime;
        this.delay=delay;
    }
    public void ComputingDelay(LocalTime time){
        setDelay(Math.abs(Duration.between(getTime(getCreateTime()),time).getSeconds()));
    }
    //执行一次无人机走一次
    public synchronized boolean ModifyPosition(){
        Position currentP=new Position(longitude,latitude);
        if(currentP.equals(target))return true;
        double dx=target.getX()-currentP.getX();
        double dy=target.getY()-currentP.getY();
        if(dx>=180){
            dx-=360;
        }else if(dx<=-180){
            dx+=360;
        }
        double distance=Math.sqrt(dx*dx+dy*dy);

        if(distance<=speed){
            setLongitude(target.getX());
            setLatitude(target.getY());
            return true;
        }else {
            double unitx=dx/distance;
            double unity=dy/distance;
            // 3. 计算新经度，并规范化回 [-180, 180]
            double newLng = currentP.getX() + unitx * speed;
            while (newLng > 180) newLng -= 360;
            while (newLng < -180) newLng += 360;

            // 4. 计算新纬度，并限制在 [-90, 90] 范围内（防越界）
            double newLat = currentP.getY() + unity * speed;
            if (newLat > 90) newLat = 90;
            if (newLat < -90) newLat = -90;

            setLongitude(newLng);
            setLatitude(newLat);
            return false;
        }
    }
//    航向计算
    public synchronized void computeHeading(){
//        转弧度
        double lona=getLongitude()*Math.PI/180;
        double lata=getLatitude()*Math.PI/180;
        double lonb=getTarget().getX()*Math.PI/180;
        double latb=getTarget().getY()*Math.PI/180;
//        经度差
        double deltalon =lonb-lona;
        double x=Math.sin(deltalon )*Math.cos(latb);
        double y=Math.cos(lata) * Math.sin(latb) - Math.sin(lata) * Math.cos(latb) * Math.cos(deltalon);
        double theta=Math.atan2(x,y)*180/Math.PI;
        if(theta<0)theta+=360;
        setHeading(theta);

    }
    private void numberBuild(){
        StringBuilder sb=new StringBuilder();
        //0-9 10 a-z 26 A-Z 26 - 1 48 65  97
        //10+26*2+1=63
        Random rand=new Random();
        for(int i=0;i<5;i++){
            char temp;
            int seed=rand.nextInt(63);
            if(seed<10){
                temp=(char)(seed+48);
            }else if(seed<36){
                temp=(char)(seed-10+65);
            } else if (seed<62) {
                temp=(char)(seed-36+97);
            }else {
                temp='-';
            }
            sb.append(temp);
        }
        setNumber(sb.toString().trim());
    }
    public LocalTime getTime(String time){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.parse(time,dtf);
    }
    public String getTime(LocalTime time){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        return time.format(dtf);
    }

    public String getNumber() {
        return number;
    }
    public IEntityStruct renew(){
        return new DataType(this.id,this.number,this.longitude,this.latitude,this.flip,this.pitch,this.state.toString(),this.height,this.createTime,this.delay,this.speed,this.heading);
    }

    public void setNumber(String number) {
        //五个英文或数字字符
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

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if(longitude<=180&&longitude>=-180){
            this.longitude = longitude;
        }else {
            System.out.println("经度赋值失败");
            this.longitude =0;
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if(latitude<=90&&latitude>=-90){
            this.latitude = latitude;
        }else {
            System.out.println("纬度赋值失败");
            this.latitude =0;
        }
    }

    public short getFlip() {
        return flip;
    }

    public void setFlip(short flip) {
        if(flip>=-180&&flip<=180){
            this.flip = flip;
        }else {
            System.out.println("翻转角度赋值失败");
            this.flip =0;
        }
    }

    public short getPitch() {
        return pitch;
    }

    public void setPitch(short pitch) {
        if(pitch>=-90&&pitch<=90){
            this.pitch = pitch;
        }else {
            System.out.println("俯仰角度赋值失败");
            this.pitch =0;
        }
    }



    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        if(createTime==null||createTime.trim().isEmpty()){
            this.createTime = "00:00:00";
        }else {
            try{
                this.createTime = createTime;
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public IState getState() {
        return state;
    }

    public void setState(IState state) {
        this.state = state;
    }
    public void setState(String state) {
        if(state==null||state.isEmpty()){
            state = "00";
        }
        switch (state){
            case "00":this.state=new Normal();
            break;
            case "01":this.state=new Warning();
            break;
            case "02":this.state=new Error();
            break;
            default:this.state=null;
        }
    }

    public Position getTarget() {
        return target;
    }

    public void setTarget(Position target) {
        this.target = target;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        if(heading>=0&&heading<=360){
            this.heading = heading;
        }
    }
}
