package com.example.myapplication5.common;

public class ColoredRange {

    private int color;
    private double begin;
    private double end;

    public ColoredRange(int color, double begin, double end) {
        this.color = color;
        this.begin = begin;
        this.end = end;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public double getBegin() {
        return begin;
    }

    public void setBegin(double begin) {
        this.begin = begin;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }
}
