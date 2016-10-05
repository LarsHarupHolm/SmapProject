package com.smap16e.group02.isamonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smap16e.group02.isamonitor.model.Measurement;
import com.smap16e.group02.isamonitor.model.Parameter;
import com.smap16e.group02.isamonitor.model.ParameterList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * A fragment representing a single Parameter detail screen.
 * This fragment is either contained in a {@link ParameterListActivity}
 * in two-pane mode (on tablets) or a {@link ParameterDetailActivity}
 * on handsets.
 */
public class ParameterDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public Parameter mItem;
    private TextView detailTextView;
    private Handler handler;
    private Timer timer;
    private TimerTask task;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ParameterDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = ParameterList.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.name);
            }
        }

        handler = new Handler();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        new UpdateValueTask().execute(mItem.id);
                    }
                });
            }
        };
        timer.schedule(task, 0, 1000*60); //Every minute
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.parameter_detail, container, false);


        detailTextView = (TextView) rootView.findViewById(R.id.parameter_detail);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            detailTextView.setText("Current value: ");
        }

        return rootView;
    }

    private class UpdateValueTask extends AsyncTask<Object, Object, String>{
        private int parameterID;

        @Override
        protected String doInBackground(Object[] params) {
            String result = null;
            InputStream inputStream;
            int length = 50;

            try {
                parameterID = (int) params[0];
                URL url = new URL(BackgroundService.APIurl + params[0]);
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
                        break;
                    case 502:
                        Log.e(TAG, "No connection to server");
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
                if(measurement != null){
                    detailTextView.setText(String.format("Current value: %.2f", measurement.value));
                }
            } else {
                Toast.makeText(getActivity(), "No connection to server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Measurement buildMeasurement(String jsonString, int parameterID){
        Measurement result = new Measurement();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            result.id = parameterID;
            result.value = jsonObject.getDouble("v");
            result.measureTime =  jsonObject.getLong("m");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}