package com.smap16e.group02.isamonitor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KSJensen on 1/10/2016.
 */

public class ParameterList {
    /**
     * An array of sample (dummy) items.
     */
    public static final List<Parameter> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Parameter> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyParameter(i));
        }
    }

    private static void addItem(Parameter item) {
        ITEMS.add(item);
        ITEM_MAP.put(Integer.toString(item.getId()), item);
    }

    private static Parameter createDummyParameter(int position) {
        return new Parameter(position, "Parameter " + position, "Surname", "Volt", false);
    }
}
