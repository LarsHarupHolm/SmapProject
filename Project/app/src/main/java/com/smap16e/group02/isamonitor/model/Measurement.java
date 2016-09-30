package com.smap16e.group02.isamonitor.model;

/**
 * Created by KSJensen on 30/09/2016.
 */

public class Measurement {
    private int id;
    private int parameterId;
    private int value;
    private long measureTime;

    public Measurement(){}

    //region Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParameterId() {
        return parameterId;
    }

    public void setParameterId(int parameterId) {
        this.parameterId = parameterId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(long measureTime) {
        this.measureTime = measureTime;
    }
    //endregion
}
