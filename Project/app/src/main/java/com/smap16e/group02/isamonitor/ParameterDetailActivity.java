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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity representing a single Parameter detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ParameterListActivity}.
 */
public class ParameterDetailActivity extends AppCompatActivity {


    private ParameterDetailFragment fragment;
    //region Service binding
    private static final String TAG = "ParameterDetailFragment";
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
        bindService(new Intent(ParameterDetailActivity.this,
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);


        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BackgroundService.BROADCAST_NEW_MEASUREMENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNewMeasurementResult, filter);

        filter = new IntentFilter();
        filter.addAction(BackgroundService.BROADCAST_CONNECTION_ERROR);
        LocalBroadcastManager.getInstance(this).registerReceiver(onErrorConnection, filter);
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ParameterDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ParameterDetailFragment.ARG_ITEM_ID));
            fragment = new ParameterDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.parameter_detail_container, fragment)
                    .commit();
            fragment.setRetainInstance(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private BroadcastReceiver onNewMeasurementResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mService.subscribedParameterList == null)
                return;

            if (fragment != null) {
                fragment.UpdateFragment();
            }
        }
    };

    private BroadcastReceiver onErrorConnection = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(ParameterDetailActivity.this, getResources().getText(R.string.ErrorMessageNotConnected), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, ParameterListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
