package com.smap16e.group02.isamonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.smap16e.group02.isamonitor.adaptors.SimpleItemRecyclerViewAdapter;
import com.smap16e.group02.isamonitor.model.ParameterList;

/**
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 *  https://www.sitepoint.com/create-your-own-content-provider-in-android/
 */
public class ParameterListActivity extends AppCompatActivity {

    static final String RESULT_ID = "result id";
    static final int ADD_PARAMETER = 0;
    public static boolean modeTwoPane;

    private FragmentManager fragmentManager;

    //region Service handling
    private BackgroundService mBoundService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((BackgroundService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    void doBindService() {
        bindService(new Intent(ParameterListActivity.this,
                BackgroundService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter_list);
        doBindService();

        fragmentManager = getSupportFragmentManager();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View recyclerView = findViewById(R.id.parameter_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.parameter_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            modeTwoPane = true;
        }

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ParameterListActivity.this, AddParameter.class);
                startActivityForResult(i, ADD_PARAMETER);
            }
        });
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(ParameterList.ITEMS, fragmentManager));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_CANCELED)
            return;

        switch(requestCode){
            case ADD_PARAMETER:
                Toast.makeText(ParameterListActivity.this, "User selected parameter " + data.getLongExtra(RESULT_ID, -2), Toast.LENGTH_SHORT).show();
        }
    }
}
