package com.smap16e.group02.isamonitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smap16e.group02.isamonitor.db.MonitorDbHelper;
import com.smap16e.group02.isamonitor.model.Measurement;
import com.smap16e.group02.isamonitor.model.Parameter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by KSJensen on 30/09/2016.
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 */

public class BackgroundService extends Service {

    public static final String BROADCAST_NEW_READING_RESULT = "new reading result";
    public static final String MEASUREMENT_ID = "measurement id";
    public static final String MEASUREMENT_VALUE = "measurement value";
    public static final String MEASUREMENT_TIME = "measurement time";

    private static final String TAG = "BackgroundService";
    private final IBinder mBinder = new LocalBinder();
    private String APIurl = "http://37.139.13.108/api/measurement/";

    public class LocalBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //region Service functionality
    private void GetCurrentReading(final int parameterID) {

        final AsyncTask<Object, Object, String> task = new AsyncTask<Object, Object, String>() {
            @Override
            protected String doInBackground(Object[] params) {
                String result = null;
                InputStream inputStream;
                int length = 100;

                try {
                    URL url = new URL(APIurl + parameterID);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(10000 /* milliseconds */);
                    urlConnection.setConnectTimeout(15000 /* milliseconds */);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    int status = urlConnection.getResponseCode();

                    switch (status) {
                        case 200:
                            inputStream = urlConnection.getInputStream();
                            Reader reader = new InputStreamReader(inputStream, "UTF-8");
                            char[] buffer = new char[length];
                            reader.read(buffer);
                            result = new String(buffer);
                            inputStream.close();
                            urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if (result != null) {
                    Measurement measurement = buildMeasurement(result, parameterID);

                    if (measurement != null) {
                        broadCastNewInformation(measurement);
                    }
                }
            }
        };

        task.execute();
    }

    private Measurement buildMeasurement(String jsonString, int parameterID){
        Measurement result = new Measurement();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            result.setId(parameterID);
            result.setValue(jsonObject.getDouble("v"));
            result.setMeasureTime(jsonObject.getLong("m"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private void broadCastNewInformation(Measurement measurement){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BROADCAST_NEW_READING_RESULT);
        broadcastIntent.putExtra(MEASUREMENT_ID, measurement.getId());
        broadcastIntent.putExtra(MEASUREMENT_VALUE, measurement.getValue());
        broadcastIntent.putExtra(MEASUREMENT_TIME, measurement.getMeasureTime());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
    //endregion
}