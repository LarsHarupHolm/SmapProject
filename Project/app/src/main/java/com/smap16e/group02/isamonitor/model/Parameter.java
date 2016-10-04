package com.smap16e.group02.isamonitor.model;

/**
 * Created by KSJensen on 30/09/2016.
 */

public class Parameter {
    public int id;
    public String name;
    public String surname;
    public String unit;

    public Parameter(){}

    public Parameter(int id, String name, String surname, String unit) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.unit = unit;
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
    //endregion

    @Override
    public String toString() {
        return name;
    }

}
