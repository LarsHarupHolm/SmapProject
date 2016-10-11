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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.smap16e.group02.isamonitor.model.Parameter;

import java.util.ArrayList;

public class AddParameter extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private long selectedParameter = -1;
    private Spinner spinner;
    private ArrayList<Parameter> parameterArrayList;

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

            if(mService.notSubscribedParameterList != null){
                populateSpinner();
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

        bindService();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BackgroundService.BROADCAST_NEW_PARAMETERINFO);
        LocalBroadcastManager.getInstance(this).registerReceiver(onServiceResult, filter);

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
        //todo: Change to checkbox check yo
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
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        if(mService != null && mService.notSubscribedParameterList != null)
            populateSpinner();
    }

    private BroadcastReceiver onServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            populateSpinner();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
    }

    private void populateSpinner(){
        ArrayList<String> testArray = new ArrayList<>();
        parameterArrayList = new ArrayList<>();
        for(Parameter parameter: mService.notSubscribedParameterList){
            testArray.add(parameter.name + parameter.surname);
            parameterArrayList.add(parameter);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddParameter.this, android.R.layout.simple_dropdown_item_1line, testArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void addParameterToUserSubscription(){
        mService.addParameterSubscription(parameterArrayList.get((int) selectedParameter).id);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedParameter = id;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Wat
    }
}
