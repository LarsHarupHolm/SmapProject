package com.smap16e.group02.isamonitor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.smap16e.group02.isamonitor.adaptors.AddParameterAdapter;
import com.smap16e.group02.isamonitor.model.AddParameterModel;
import com.smap16e.group02.isamonitor.model.Parameter;

import java.util.ArrayList;

public class AddParameter extends AppCompatActivity {
    private String TAG = "AddParameterActivity";
    private final String bundle_ObjectArray = "add parametermodel array";
    private long selectedParameter = -1;
    private AddParameterAdapter mAdapter;
    private Boolean isLoadedFromSavedInstance = false;

    private ListView mListView;

    //region Service binding
    BackgroundService mService;
    boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) service;
            mService = binder.getService();

            if(mService.generalParameterList != null && mService.subscribedParameterList != null){
                populateList();
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    void bindService() {
        Intent intent = new Intent(this, BackgroundService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    void unbindService() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parameter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BackgroundService.BROADCAST_NEW_PARAMETERINFO);
        LocalBroadcastManager.getInstance(this).registerReceiver(onServiceResult, filter);

        mListView = (ListView)findViewById(R.id.add_parameter_list);

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                setResult(ParameterListActivity.RESULT_CANCELED, resultIntent);
                finish();
            }
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addParameterToUserSubscription();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(ParameterListActivity.RESULT_ID, selectedParameter);
                setResult(ParameterListActivity.RESULT_OK, resultIntent);
                finish();
            }
        });

        if(savedInstanceState != null){
            if(mListView != null) {
                isLoadedFromSavedInstance = true;
                ArrayList<AddParameterModel> parameterModelArrayList = savedInstanceState.getParcelableArrayList(bundle_ObjectArray);
                mAdapter = new AddParameterAdapter(AddParameter.this, android.R.layout.list_content, parameterModelArrayList);
                mListView.setAdapter(mAdapter);
                return;
            }
        }

        if(mService != null && mService.generalParameterList != null && mService.subscribedParameterList != null)
            populateList();
    }

    private BroadcastReceiver onServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isLoadedFromSavedInstance)
                return;

            populateList();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(bundle_ObjectArray, mAdapter.getParameterModels());

        super.onSaveInstanceState(savedInstanceState);
    }

    private void populateList() {
        //Get id's of subscribed to
        ArrayList<AddParameterModel> addParameterModelArrayList = new ArrayList<>();
        for (Parameter parameter : mService.generalParameterList)
        {
            boolean isSubscribed = mService.subscribedParameterList.contains(parameter);
            addParameterModelArrayList.add(new AddParameterModel(
                    parameter.id,
                    parameter.name,
                    parameter.surname,
                    isSubscribed));
        }
        if(mListView != null) {
            mAdapter = new AddParameterAdapter(AddParameter.this, android.R.layout.list_content, addParameterModelArrayList);
            mListView.setAdapter(mAdapter);
        }
    }

    private void addParameterToUserSubscription(){
        if(mAdapter.ParameterModels == null) return;
        ArrayList<Integer> parameterIds = new ArrayList<>();
        for(AddParameterModel model : mAdapter.ParameterModels) {
            if(model.isChecked) {
                parameterIds.add(model.id);
            }
        }
        mService.addParameterListSubscription(parameterIds);
    }
}
