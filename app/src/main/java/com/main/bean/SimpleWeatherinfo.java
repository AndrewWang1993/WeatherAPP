package com.main.bean;

import com.google.gson.annotations.Expose;

public class SimpleWeatherinfo {
	@Expose
	private String city;
	@Expose
	private String temp;
	@Expose
	private String SD;
	@Expose
	private String time;
    @Expose
    private String alertinfo;
    @Expose
    private String level;
    @Expose
    private String description;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}


	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

    public String getAlertinfo() {
        return alertinfo;
    }

    public void setAlertinfo(String alertinfo) {
        this.alertinfo = alertinfo;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSD() {
		return SD;
	}

	public void setSD(String sD) {
		SD = sD;
	}


	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "SimpleWeatherinfo [city=" + city + ", temp=" + temp + ", SD=" + SD +", time=" + time + "]";
	}

}
