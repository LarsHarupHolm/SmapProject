package com.smap16e.group02.isamonitor.model;

/**
 * Created by KSJensen on 30/09/2016.
 */

public class Parameter {
    private int id;
    private String name;
    private String surname;
    private String unit;
    private boolean isActive;

    public Parameter(){}

    public Parameter(int id, String name, String surname, String unit, boolean isActive) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.unit = unit;
        this.isActive = isActive;
    }

    //region Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    //endregion


}
