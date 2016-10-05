package com.smap16e.group02.isamonitor;

import android.app.Service;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KSJensen on 30/09/2016.
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 */

public class BackgroundService extends Service {

    //region Properties
    public static final String BROADCAST_NEW_GENERALPARAMETERLIST = "generalParameterList";
    public static final String BROADCAST_NEW_USERPARAMETERLIST = "userParameterList";

    private static List<Parameter> generalParameterList;
    private static List<Parameter> subscribedParameterList;
    private static List<Parameter> notSubscribedParameterList;

    private List<Integer> userParameterIDList;

    private static final String TAG = "BackgroundService";
    public static String APIurl = "http://37.139.13.108/api/measurement/";
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
            return BackgroundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SERVICE CREATED");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        } else {
            // No user is signed in
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        fetchAPIUrl();
        fetchGeneralParameterList();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return mBinder; }
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

    private void broadCastNewInformation(String info){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(info);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
    //endregion

}