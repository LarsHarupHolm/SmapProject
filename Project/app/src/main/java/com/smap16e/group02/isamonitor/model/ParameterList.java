package com.smap16e.group02.isamonitor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KSJensen on 1/10/2016.
 */

public class ParameterList {
    public static final List<Parameter> ITEMS = new ArrayList<>();
    public static final Map<String, Parameter> ITEM_MAP = new HashMap<>();

    public static void setParameters(List<Parameter> parameters) {
        ITEMS.clear();
        ITEM_MAP.clear();
        for(Parameter parameter : parameters){
            addItem(parameter);
        }
    }

    private static void addItem(Parameter item) {
        ITEMS.add(item);
        ITEM_MAP.put(Integer.toString(item.id), item);
    }
}
