package com.main.bean;

import com.google.gson.annotations.Expose;

public class Pm2d5 {
    @Expose
    private String aqi;
    @Expose
    private String pm2_5_24h;
    @Expose
    private String quality;
    @Expose
    private String suggest;




    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getPm2_5_24h() {
        return pm2_5_24h;
    }

    public void setPm2_5_24h(String pm2_5_24h) {
        this.pm2_5_24h = pm2_5_24h;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }


    public String getSuggest() {
        return suggest;
    }

    public void setSuggest(String suggest) {
        this.suggest = suggest;
    }

    @Override
    public String toString() {
        return "Pm2d5 [aqi=" + aqi + "pm2_5_24h=" + pm2_5_24h + "quality=" + quality+"suggest=" + suggest + "]";
    }

}
