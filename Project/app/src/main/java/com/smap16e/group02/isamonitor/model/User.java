package com.smap16e.group02.isamonitor.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by KSJensen on 4/10/2016.
 */

@IgnoreExtraProperties
public class User {
    public String email;
    public List<Integer> subscribedParameters;
}
