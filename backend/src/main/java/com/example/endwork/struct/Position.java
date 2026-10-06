package com.example.endwork.struct;

/**
 * double x,double y
 */
public class Position {
    private double x;
    private double y;
    public Position(double x, double y) {
        setX(x);
        setY(y);
    }

    @Override
    public String toString() {
        return x+","+y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        if(x<=180&&x>=-180){
            this.x = x;
        }
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        if(y<=90&&y>=-90){
            this.y = y;
        }
    }
}
