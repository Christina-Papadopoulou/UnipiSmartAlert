package com.papadopoulou.christina.unipismartalert;

import java.util.Date;

public class Characteristics {
    private double lat;
    private double longt;
    private boolean alarmAbort;

    public Characteristics() {
    }

    public Characteristics(double lat, double longt, boolean alarmAbort) {
        this.lat = lat;
        this.longt = longt;
        this.alarmAbort = alarmAbort;
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

    public boolean isAlarmAbort() {
        return alarmAbort;
    }

    public void setAlarmAbort(boolean alarmAbort) {
        this.alarmAbort = alarmAbort;
    }
}
