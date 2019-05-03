package com.papadopoulou.christina.unipismartalert;

import java.util.Date;

public class Characteristics {
    private double lat;
    private double longt;
    private int countAlarmAbort;
    private int countSos;
    private boolean fallAborted;

    public Characteristics() {
    }

    public Characteristics(double lat, double longt, boolean fallAborted) {
        this.lat = lat;
        this.longt = longt;
        this.fallAborted = fallAborted;
    }

    public Characteristics( int countAlarmAbort, int countSos) {
        this.countAlarmAbort = countAlarmAbort;
        this.countSos = countSos;
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

    public int getCountAlarmAbort() {
        return countAlarmAbort;
    }

    public void setCountAlarmAbort(int countAlarmAbort) {
        this.countAlarmAbort = countAlarmAbort;
    }

    public int getCountSos() {
        return countSos;
    }

    public void setCountSos(int countSos) {
        this.countSos = countSos;
    }

    public boolean isFallAborted() {
        return fallAborted;
    }

    public void setFallAborted(boolean fallAborted) {
        this.fallAborted = fallAborted;
    }
}
