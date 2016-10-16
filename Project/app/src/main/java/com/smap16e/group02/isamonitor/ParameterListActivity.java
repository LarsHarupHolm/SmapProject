package com.smap16e.group02.isamonitor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.icu.util.Measure;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;

import com.google.firebase.auth.FirebaseAuth;
import com.smap16e.group02.isamonitor.adaptors.RecyclerViewAdapter;
import com.smap16e.group02.isamonitor.model.Measurement;
import com.smap16e.group02.isamonitor.model.Parameter;
import com.smap16e.group02.isamonitor.login.LoginActivity;
import com.smap16e.group02.isamonitor.model.ParameterList;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 *  https://developer.android.com/training/appbar/setting-up.html
 *  https://developer.android.com/training/appbar/actions.html
 */
public class ParameterListActivity extends AppCompatActivity {

    static final String RESULT_ID = "result id";
    static final int ADD_PARAMETER = 0;
    public static boolean modeTwoPane;

    private FragmentManager fragmentManager;
    private static final String TAG = "ParameterListActivity";
    private View recyclerView;
    private WebAPIHelper webAPIHelper;
    private Handler handler;
    private Timer timer;
    private RecyclerViewAdapter recyclerViewAdapter;

    //region Service binding
    BackgroundService mService;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.e(TAG, "onServiceConnected called...");

            mService = ((BackgroundService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    void bindService() {
        Log.e(TAG, "bindService called...");

        bindService(new Intent(ParameterListActivity.this,
                BackgroundService.class), mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    void unbindService() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    //endregion

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        if(recyclerViewAdapter == null){
            recyclerViewAdapter = new RecyclerViewAdapter(ParameterList.ITEMS, fragmentManager);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    }

    //region Overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter_list);
        webAPIHelper = new WebAPIHelper();
        handler = new Handler();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BackgroundService.BROADCAST_NEW_PARAMETERINFO);
        LocalBroadcastManager.getInstance(this).registerReceiver(onServiceResult, filter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();

        recyclerView = findViewById(R.id.parameter_list);
        assert recyclerView != null;
        if(ParameterList.ITEMS != null){
            setupRecyclerView((RecyclerView) recyclerView);
        }

        // If the parameter_detail_container exists then we are in two pane mode.
        modeTwoPane = findViewById(R.id.parameter_detail_container) != null;

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ParameterListActivity.this, AddParameter.class);
                startActivityForResult(i, ADD_PARAMETER);
            }
        });
    }

    private BroadcastReceiver onServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mService.subscribedParameterList == null)
                return;

            new UpdateReadings().execute();

            ParameterList.setParameters(mService.subscribedParameterList);
            setupRecyclerView((RecyclerView) recyclerView);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
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
        timer.schedule(task, 0, 1000*20); //Every 20 seconds
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
        timer.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_CANCELED)
            return;

        switch(requestCode){
            case ADD_PARAMETER:
                Log.i(TAG, "Added parameter to subscriptions");
        }
    }
    //endregion

    private class UpdateReadings extends AsyncTask<Object, Object, List<Measurement>> {
        @Override
        protected List<Measurement> doInBackground(Object[] params) {
            if (mService != null && mService.subscribedParameterList != null)
                return webAPIHelper.getParameterMeasurements();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Measurement> result) {
            super.onPostExecute(result);

            if (result != null && mService != null && mService.subscribedParameterList != null) {
                List<Parameter> parameterList = mService.subscribedParameterList;
                for (Parameter parameter : parameterList) {
                    for (Measurement m : result) {
                        if (parameter.id == m.id) {
                            parameter.reading = m.value;
                            parameter.isValid = m.isValid;
                        }
                    }
                }
                ParameterList.setParameters(parameterList);
                recyclerViewAdapter.notifyDataSetChanged();
            } else {
                // Error handling
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                //Log out with firebase auth and return to login.
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String email = auth.getCurrentUser().getEmail();
                auth.signOut();

                Intent i = new Intent(ParameterListActivity.this, LoginActivity.class);
                i.putExtra(LoginActivity.EXTRA_EMAIL, email);
                startActivity(i);
                finish();
                return true;
        }
        return false;
    }
}
