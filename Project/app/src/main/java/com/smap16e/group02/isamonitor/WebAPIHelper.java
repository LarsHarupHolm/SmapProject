package com.smap16e.group02.isamonitor;

import android.util.Log;

import com.smap16e.group02.isamonitor.model.Measurement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by KSJensen on 11/10/2016.
 */

public class WebAPIHelper {

    public List<Measurement> getParameterMeasurements() throws IOException {
        String requestResult = makeHTTPRequest(BackgroundService.APIurl);
        if(requestResult != null)
            return buildMeasurements(requestResult);
        else
            return null;
    }

    //Inspiration from http://stackoverflow.com/questions/21170893/httpurlconnection-cutting-inputstream-returned
    private String makeHTTPRequest(String urlString) throws IOException {
        StringBuilder result = new StringBuilder();
        InputStream inputStream;

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(5000 /* milliseconds */);
            urlConnection.setConnectTimeout(5000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            int status = urlConnection.getResponseCode();

            switch (status) {
                case 200:
                    inputStream = urlConnection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String output;
                    while ((output = bufferedReader.readLine()) != null) {
                        result.append(output);
                    }
                    inputStream.close();
                    urlConnection.disconnect();
                    break;
                case 502:
                    Log.e(TAG, "No connection to server");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        return result.toString();
    }

    public Measurement buildMeasurement(String jsonString, int parameterID){
        Measurement result = new Measurement();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            result.id = parameterID;
            result.value = jsonObject.getDouble("value");
            result.measureTime =  jsonObject.getLong("timestamp");
            result.isValid = jsonObject.getBoolean("isValid");
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
                measurement.id = jsonObject.getInt("parameterId");
                measurement.value = jsonObject.getDouble("value");
                measurement.measureTime =  jsonObject.getLong("timestamp");
                measurement.isValid = jsonObject.getBoolean("isValid");
                result.add(measurement);
            }
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
