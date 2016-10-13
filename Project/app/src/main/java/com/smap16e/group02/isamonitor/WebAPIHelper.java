package com.smap16e.group02.isamonitor;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.smap16e.group02.isamonitor.model.Measurement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by KSJensen on 11/10/2016.
 */

public class WebAPIHelper {

    public String getParameterMeasurement(int parameterID){
        return makeHTTPRequest(BackgroundService.APIurl+parameterID, 50);
    }

    public List<Measurement> getParameterMeasurements(){
        String requestResult = makeHTTPRequest(BackgroundService.APIurl, 1000);
        if(requestResult != null)
            return buildMeasurements(requestResult);
        else
            return null;
    }


    private String makeHTTPRequest(String urlString, int bufferLength){
        String result = null;
        InputStream inputStream;

        try {
            URL url = new URL(urlString);
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
                    char[] buffer = new char[bufferLength];
                    reader.read(buffer);
                    result = new String(buffer);
                    inputStream.close();
                    urlConnection.disconnect();
                    break;
                case 502:
                    Log.e(TAG, "No connection to server");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public Measurement buildMeasurement(String jsonString, int parameterID){
        Measurement result = new Measurement();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            result.id = parameterID;
            result.value = jsonObject.getDouble("v");
            result.measureTime =  jsonObject.getLong("m");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public List<Measurement> buildMeasurements(String jsonString){
        List<Measurement> result = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Measurement measurement = new Measurement();
                measurement.id = i+1;
                measurement.value = jsonObject.getDouble("v");
                measurement.measureTime =  jsonObject.getLong("m");
                result.add(measurement);
            }
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}