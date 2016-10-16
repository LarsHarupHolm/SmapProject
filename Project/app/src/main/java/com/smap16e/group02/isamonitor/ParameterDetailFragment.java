package com.smap16e.group02.isamonitor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.smap16e.group02.isamonitor.model.Measurement;
import com.smap16e.group02.isamonitor.model.Parameter;
import com.smap16e.group02.isamonitor.model.ParameterList;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A fragment representing a single Parameter detail screen.
 * This fragment is either contained in a {@link ParameterListActivity}
 * in two-pane mode (on tablets) or a {@link ParameterDetailActivity}
 * on handsets.
 */
public class ParameterDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    public Parameter mItem;
    private TextView detailTextView;
    private ImageView isValidIndicator;
    private Handler handler;
    private Timer timer;
    private WebAPIHelper webAPIHelper;
    private LineChart chart;
    private LineDataSet chartDataSet;

    public ParameterDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webAPIHelper = new WebAPIHelper();

        handler = new Handler();
        timer = new Timer();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = ParameterList.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mItem.name);
        }

        TimerTask task = new TimerTask() {
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
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.parameter_detail, container, false);

        detailTextView = (TextView) rootView.findViewById(R.id.parameter_detail);
        isValidIndicator = (ImageView) rootView.findViewById(R.id.statusIcon);

        // Chart setup
        chart = (LineChart) rootView.findViewById(R.id.chart);
        chart.setDescription(""); // No description
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);

        chartDataSet = new LineDataSet(null, "Main data label");

        LineData lineData = new LineData(chartDataSet);
        chart.setData(lineData);

        if (mItem != null) {
            detailTextView.setText(R.string.current_value);
            isValidIndicator.setColorFilter(ContextCompat.getColor(this.getContext(), R.color.redA700));
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
                mItem.reading = measurement.value;
                detailTextView.setText(String.format("%s %s", getResources().getString(R.string.current_value), mItem.readingToString()));
                isValidIndicator.setColorFilter(ContextCompat.getColor(getContext(), mItem.isValid ? R.color.greenA700 : R.color.redA700));
                AddEntryToChart(measurement);
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