package com.smap16e.group02.isamonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * References:
 *  https://developer.android.com/reference/android/app/Service.html
 *  https://www.sitepoint.com/create-your-own-content-provider-in-android/
 */
public class MainActivity extends AppCompatActivity {

    static final String RESULT_ID = "result id";
    static final int ADD_PARAMETER = 0;

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
        bindService(new Intent(MainActivity.this,
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
        setContentView(R.layout.activity_main);
        doBindService();

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddParameter.class);
                startActivityForResult(i, ADD_PARAMETER);
            }
        });
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
                Toast.makeText(MainActivity.this, "User selected parameter " + data.getLongExtra(RESULT_ID, -2), Toast.LENGTH_SHORT).show();
        }
    }
}
