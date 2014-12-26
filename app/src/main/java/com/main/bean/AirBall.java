package com.main.bean;

/**
 * Created by wangxm-wr on 2014/12/24 in Vanke.
 */

import com.google.gson.annotations.Expose;


public class AirBall {
    @Expose
    private int pressure;
    @Expose
    private int humidity;
    @Expose
    private int pm25;
    @Expose
    private int temp;
    @Expose
    private int time;

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPm25() {
        return pm25;
    }

    public void setPm25(int pm25) {
        this.pm25 = pm25;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "The info of AirBall";
    }

}
