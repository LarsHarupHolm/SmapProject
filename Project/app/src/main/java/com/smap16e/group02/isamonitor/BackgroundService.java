package com.smap16e.group02.isamonitor;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smap16e.group02.isamonitor.model.Measurement;
import com.smap16e.group02.isamonitor.model.Parameter;
import com.smap16e.group02.isamonitor.model.User;

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

/**
 * Created by KSJensen on 30/09/2016.
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 */

public class BackgroundService extends Service {

    //region Properties
    public static final String BROADCAST_NEW_READING_RESULT = "new reading result";
    public static final String BROADCAST_NEW_GENERALPARAMETERLIST = "generalParameterList";
    public static final String BROADCAST_NEW_USERPARAMETERLIST = "userParameterList";
    public static final String BROADCAST_BINDING = "service binded";

    public static final String MEASUREMENT_ID = "measurement id";
    public static final String MEASUREMENT_VALUE = "measurement value";
    public static final String MEASUREMENT_TIME = "measurement time";

    private static List<Parameter> generalParameterList;
    private static List<Parameter> subscribedParameterList;
    private static List<Parameter> notSubscribedParameterList;

    private List<Integer> userParameterIDList;

    private static final String TAG = "BackgroundService";
    private String APIurl = "http://37.139.13.108/api/measurement/";
    private String userID = "userIDSAMPLE";
    private DatabaseReference mDatabase;

    public boolean hasParameterList = false;

    private final IBinder mBinder = new LocalBinder();
    //endregion

    //region Constructor and overrides
    public BackgroundService() {
    }

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            broadCastNewInformation(BROADCAST_BINDING);
            return BackgroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        fetchAPIUrl();
        fetchGeneralParameterList();

        return mBinder;
    }
    //endregion

    //region Public methods
    public List<Parameter> getNotSubscribedParameterList(){ return notSubscribedParameterList; }

    public List<Parameter> getSubscribedParameterList(){
        return subscribedParameterList;
    }

    public void addParameterSubscription(int parameterID){
        if(!userParameterIDList.contains(parameterID))
            userParameterIDList.add(parameterID);

        mDatabase.child("users").child(userID).child("subscribedParameters").setValue(userParameterIDList);

        fetchUserParameterList();
    }
    //endregion

    //region Private methods
    private void fetchGeneralParameterList(){
        mDatabase.child("parameters").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.e(TAG, "Reading parameters from database...");

                        generalParameterList = new ArrayList<>();
                        for(DataSnapshot parameterSnapShot: dataSnapshot.getChildren()){
                            Parameter parameter = parameterSnapShot.getValue(Parameter.class);
                            generalParameterList.add(parameter);
                        }
                        broadCastNewInformation(BROADCAST_NEW_GENERALPARAMETERLIST);
                        hasParameterList = true;
                        fetchUserParameterList();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getParameters:onCancelled", databaseError.toException());
                    }
                }
        );
    }

    private void fetchUserParameterList(){
        mDatabase.child("users").child(userID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.e(TAG, "Reading user parameters from database...");

                        User user = dataSnapshot.getValue(User.class);
                        userParameterIDList = new ArrayList<>();
                        subscribedParameterList = new ArrayList<>();
                        notSubscribedParameterList = new ArrayList<>();
                        userParameterIDList = user.subscribedParameters;
                        for(Parameter parameter : generalParameterList){
                            boolean addedToSubscribedList = false;
                            for(int parameterID : userParameterIDList){
                                if(parameter.getId() == parameterID){
                                    subscribedParameterList.add(parameter);
                                    addedToSubscribedList = true;
                                }
                            }
                            if(!addedToSubscribedList){
                                notSubscribedParameterList.add(parameter);
                            }
                        }
                        broadCastNewInformation(BROADCAST_NEW_USERPARAMETERLIST);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUserList:onCancelled", databaseError.toException());
                    }
                }
        );
    }

    private void fetchAPIUrl(){
        mDatabase.child("settings").child("webAPIUrl").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        APIurl = (String) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getAPIUrl:onCancelled", databaseError.toException());
                    }
                }
        );
    }

    public void GetCurrentReading(final int parameterID) {

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

    private void broadCastNewInformation(String info){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(info);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
    //endregion

}