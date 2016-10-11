package com.smap16e.group02.isamonitor.model;

/**
 * Created by Lars on 11-10-2016.
 */

public class AddParameterModel {
    public AddParameterModel(int id, String name, String surname, boolean isChecked) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.isChecked = isChecked;
    }
    public int id;
    public String name;
    public String surname;
    public boolean isChecked;
}
