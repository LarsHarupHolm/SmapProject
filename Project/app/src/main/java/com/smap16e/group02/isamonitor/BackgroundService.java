package com.smap16e.group02.isamonitor;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.smap16e.group02.isamonitor.db.MonitorDbHelper;
import com.smap16e.group02.isamonitor.model.Parameter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by KSJensen on 30/09/2016.
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 */

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";
    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        if(MonitorDbHelper.isParameterDatabaseEmpty()){
//            LoadDatabaseWithParameters();
//        }
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    private void LoadDatabaseWithParameters(){
        String json = null;
        try {
            InputStream is = getAssets().open("parameters.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            for(int i = 1; i < 21; i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i-1);
                Parameter parameter = new Parameter(){};
                parameter.setId(jsonObject.getInt("id"));
                parameter.setName(jsonObject.getString("name"));
                parameter.setSurname(jsonObject.getString("surname"));
                parameter.setUnit(jsonObject.getString("unit"));
                parameter.setActive(jsonObject.getBoolean("isActive"));

                //TODO: Mangler pt denne metode.
                //MonitorDbHelper.insertParameter(parameter);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}