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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
import java.util.ArrayList;
import java.util.List;
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
    private WebAPIHelper webAPIHelper;
    private LineChart chart;
    private LineDataSet chartDataSet;
    private LineData lineData;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ParameterDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webAPIHelper = new WebAPIHelper();

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
        timer.schedule(task, 0, 1000*20); //Every 20 seconds
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

        // Chart setup
        chart = (LineChart) rootView.findViewById(R.id.chart);
        chart.setDescription(""); // No description
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        chartDataSet = new LineDataSet(null, "Main data label");

        lineData = new LineData(chartDataSet);
        chart.setData(lineData);

        if (mItem != null) {
            detailTextView.setText(R.string.current_value);
        }

        return rootView;
    }

    private class UpdateValueTask extends AsyncTask<Object, Object, String>{
        private int parameterID;

        @Override
        protected String doInBackground(Object[] params) {
            parameterID = (int) params[0];
            return webAPIHelper.getParameterMeasurement(parameterID);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Measurement measurement = webAPIHelper.buildMeasurement(result, parameterID);
                if(measurement != null){
                    detailTextView.setText(String.format("%s %.2f", getResources().getString(R.string.current_value), measurement.value));
                    AddEntryToChart(measurement);
                }
            } else {
                if(getActivity() != null)
                    Toast.makeText(getActivity(), "No connection to server", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void AddEntryToChart(Measurement measurement){
        LineData data = chart.getData();

        if (data != null) {
            data.addDataSet(chartDataSet);

            data.addEntry(new Entry(chartDataSet.getEntryCount(), (float)measurement.value,0),0);
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(120);
            chart.moveViewToX(data.getEntryCount());
        }
    }
}