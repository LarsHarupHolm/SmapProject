package com.smap16e.group02.isamonitor;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
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
import com.smap16e.group02.isamonitor.model.Parameter;
import com.smap16e.group02.isamonitor.model.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KSJensen on 30/09/2016.
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 */

public class BackgroundService extends Service {

    //region Properties
    private static final String TAG = "BackgroundService";
    public static final String BROADCAST_NEW_PARAMETERINFO = "userParameterList";
    public static String APIurl = "http://139.59.152.53/api/measurement/";

    public List<Parameter> generalParameterList;
    public List<Parameter> subscribedParameterList;
    public List<Parameter> notSubscribedParameterList;
    private List<Integer> userParameterIDList;

    private String userID = "userIDSAMPLE";
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
    //endregion

    //region Constructor and overrides
    public BackgroundService() {
    }

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SERVICE CREATED");

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
                subscribedParameterList = new ArrayList<>();
                notSubscribedParameterList = new ArrayList<>();

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
                boolean addedToSubscribedList = false;
                for(int parameterID : userParameterIDList){
                    if(parameter.id == parameterID){
                        subscribedParameterList.add(parameter);
                        addedToSubscribedList = true;
                    }
                }
                if(!addedToSubscribedList){
                    notSubscribedParameterList.add(parameter);
                }
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            broadCastNewInformation(BROADCAST_NEW_PARAMETERINFO);
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