package com.papadopoulou.christina.unipismartalert;

import java.util.Date;

public class Characteristics {
    private double lat;
    private double longt;
    private Date date;

    public Characteristics() {
    }

    public Characteristics(double lat, double longt, Date date) {
        this.lat = lat;
        this.longt = longt;
        this.date = date;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongt() {
        return longt;
    }

    public void setLongt(double longt) {
        this.longt = longt;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
