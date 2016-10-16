package com.smap16e.group02.isamonitor.model;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.DecimalFormat;
import java.util.Objects;

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
    public String format;

    @Override
    public String toString() {
        return name;
    }

    public String readingToString() {
        if (reading == null) return "";
        switch (format) {
            case "in/out":
                return (reading.intValue() == 1 ? "In" : "Out");
            case "open/close":
                return (reading.intValue() == 1 ? "Open" : "Close");
            default:
                DecimalFormat decimalFormat = new DecimalFormat(format);
                return decimalFormat.format(reading) + (unit != "" ? " " + unit : "");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parameter)) return false;

        Parameter parameter = (Parameter) o;

        if (id != parameter.id) return false;
        if (name != null ? !name.equals(parameter.name) : parameter.name != null) return false;
        if (surname != null ? !surname.equals(parameter.surname) : parameter.surname != null)
            return false;
        return unit != null ? unit.equals(parameter.unit) : parameter.unit == null;
    }
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }
}
