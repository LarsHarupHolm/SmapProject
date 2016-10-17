package com.smap16e.group02.isamonitor;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smap16e.group02.isamonitor.model.Measurement;
import com.smap16e.group02.isamonitor.model.Parameter;
import com.smap16e.group02.isamonitor.model.ParameterList;
import com.smap16e.group02.isamonitor.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by KSJensen on 30/09/2016.
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 */

public class BackgroundService extends Service {

    //region Properties
    private static final String TAG = "BackgroundService";
    public static final String BROADCAST_NEW_PARAMETERINFO = "userParameterList";
    public static final String BROADCAST_NEW_MEASUREMENT = "newMeasurement";
    public static String APIurl = "";

    public List<Parameter> generalParameterList;
    public List<Parameter> subscribedParameterList;
    private List<Integer> userParameterIDList;

    private String userID = "";
    private DatabaseReference mDatabase;
    private DatabaseReference mGeneralParametersReference;
    private DatabaseReference mUserParametersReference;
    private DatabaseReference mSettingsReference;
    private ValueEventListener mGeneralParametersListener;
    private ValueEventListener mUserParametersListener;
    private ValueEventListener mSettingsListener;
    public boolean hasGeneralParameterList = false;
    private boolean hasUserParameterList = false;
    private final IBinder mBinder = new LocalBinder();
    private WebAPIHelper webAPIHelper = new WebAPIHelper();
    private Timer timer;
    private Handler handler = new Handler();

    //endregion

    //region Constructor and overrides
    public BackgroundService() {}

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SERVICE CREATED");

        subscribedParameterList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        } else {
            Log.e(TAG, "INVALID USER!");
            return;
        }

        mGeneralParametersReference = FirebaseDatabase.getInstance().getReference().child("parameters");
        mUserParametersReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        mSettingsReference = FirebaseDatabase.getInstance().getReference().child("settings");

        subscribeToSettings();
        subscribeToGeneralParameterList();
        subscribeToUserParameterList();

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        new UpdateReadings().execute();
                    }
                });
            }
        };
        timer.schedule(task, 0, 1000*5); //Every 5 seconds
    }

    private class UpdateReadings extends AsyncTask<Object, Object, List<Measurement>> {
        @Override
        protected List<Measurement> doInBackground(Object[] params) {
            if (subscribedParameterList != null)
                return webAPIHelper.getParameterMeasurements();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Measurement> result) {
            super.onPostExecute(result);

            if (result != null) {
                //Matching measurements to their parameters
                for (Parameter parameter : subscribedParameterList) {
                    for (Measurement m : result) {
                        if (parameter.id == m.id) {
                            parameter.measurements.add(m);
                            parameter.isValid = m.isValid;
                        }
                    }
                }
                ParameterList.setParameters(subscribedParameterList);
                broadCastNewInformation(BROADCAST_NEW_MEASUREMENT);
            } else {
                // Error handling
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel(); //Cancelling the routine that fetches new data from the WebAPI

        if (mGeneralParametersListener != null) {
            mGeneralParametersReference.removeEventListener(mGeneralParametersListener);
        }
        if (mUserParametersListener != null) {
            mUserParametersReference.removeEventListener(mUserParametersListener);
        }
        if (mSettingsListener != null) {
            mSettingsReference.removeEventListener(mSettingsListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return mBinder; }
    //endregion

    public void addParameterListSubscription(ArrayList<Integer> parameterIds)
    {
        if(parameterIds != null) {
            userParameterIDList = parameterIds;
            mDatabase.child("users").child(userID).child("subscribedParameters").setValue(userParameterIDList);
        }
    }

    //region Private methods
    private void subscribeToGeneralParameterList(){
        ValueEventListener generalParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "Reading parameters from database...");

                generalParameterList = new ArrayList<>();
                for(DataSnapshot parameterSnapShot: dataSnapshot.getChildren()){
                    Parameter parameter = parameterSnapShot.getValue(Parameter.class);
                    generalParameterList.add(parameter);
                }
                hasGeneralParameterList = true;

                if(hasUserParameterList)
                    new BuildUserParameterList().execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getParameters:onCancelled", databaseError.toException());
            }
        };

        mGeneralParametersReference.addValueEventListener(generalParameterListener);
        mGeneralParametersListener = generalParameterListener;
    }

    private void subscribeToUserParameterList(){
        ValueEventListener userParameterListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "Reading user parameters from database...");

                User user = dataSnapshot.getValue(User.class);
                userParameterIDList = new ArrayList<>();

                if(user.subscribedParameters != null){
                    userParameterIDList = user.subscribedParameters;
                    hasUserParameterList = true;
                }
                else
                    userParameterIDList = new ArrayList<>();

                if(hasGeneralParameterList)
                    new BuildUserParameterList().execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUserList:onCancelled", databaseError.toException());
            }
        };

        mUserParametersReference.addValueEventListener(userParameterListener);
        mUserParametersListener = userParameterListener;
    }

    private class BuildUserParameterList extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            for(Parameter parameter : generalParameterList){
                for(int parameterID : userParameterIDList){
                    if(parameter.id == parameterID){
                        subscribedParameterList.add(parameter);
                    }
                }
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            ParameterList.setParameters(subscribedParameterList);
            broadCastNewInformation(BROADCAST_NEW_PARAMETERINFO);
            new UpdateReadings().execute();
        }
    }

    private void subscribeToSettings(){
        ValueEventListener settingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                APIurl = (String) dataSnapshot.child("webAPIUrl").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getAPIUrl:onCancelled", databaseError.toException());
            }
        };

        mSettingsReference.addValueEventListener(settingsListener);
        mSettingsListener = settingsListener;
    }

    private void broadCastNewInformation(String info){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(info);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
    //endregion

}