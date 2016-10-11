package com.smap16e.group02.isamonitor.model;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by KSJensen on 30/09/2016.
 */

@IgnoreExtraProperties
public class Parameter {
    public int id;
    public String name;
    public String surname;
    public String unit;
    public Double reading;

    @Override
    public String toString() {
        return name;
    }

}
