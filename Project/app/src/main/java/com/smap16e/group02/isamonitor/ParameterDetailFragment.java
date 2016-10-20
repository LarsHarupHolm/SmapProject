package com.smap16e.group02.isamonitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.smap16e.group02.isamonitor.model.Measurement;
import com.smap16e.group02.isamonitor.model.Parameter;
import com.smap16e.group02.isamonitor.model.ParameterList;

import java.util.List;

/**
 * A fragment representing a single Parameter detail screen.
 * This fragment is either contained in a {@link ParameterListActivity}
 * in two-pane mode (on tablets) or a {@link ParameterDetailActivity}
 * on handsets.
 */
public class ParameterDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    public Parameter parameterItem;
    private TextView detailTextView;
    private ImageView isValidIndicator;
    private LineChart chart;
    private LineDataSet chartDataSet;
    protected boolean onCreateViewCalled = false;

    public ParameterDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateParameterItem();
        if (parameterItem != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(parameterItem.name);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
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

        initializeFragment();
        onCreateViewCalled = true;
        return rootView;
    }

    public void updateFragment() {
        if (onCreateViewCalled && isAdded()) {
            updateParameterItem();
            if (parameterItem == null) { return; }
            detailTextView.setText(String.format("%s %s", getResources().getString(R.string.current_value), parameterItem.readingToString()));
            isValidIndicator.setColorFilter(ContextCompat.getColor(getContext(), parameterItem.isValid ? R.color.greenA700 : R.color.redA700));
            AddEntryToChart(parameterItem.getLatestMeasurement());
        }
    }

    public void initializeFragment() {
        updateParameterItem();
        if (parameterItem == null) { return; }
        detailTextView.setText(String.format("%s %s", getResources().getString(R.string.current_value), parameterItem.readingToString()));
        isValidIndicator.setColorFilter(ContextCompat.getColor(getContext(), parameterItem.isValid ? R.color.greenA700 : R.color.redA700));
        AddEntriesToChart(parameterItem.measurements);
    }

    private void updateParameterItem() {
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            parameterItem = ParameterList.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    private void AddEntryToChart(Measurement measurement){
        LineData data = chart.getData();

        if (data != null) {
            data.addDataSet(chartDataSet);

            data.addEntry(new Entry(chartDataSet.getEntryCount(), (float)measurement.value,0),0);
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(getContext().getResources().getInteger(R.integer.maximumMeasurementsForParameter));
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private void AddEntriesToChart(List<Measurement> measurements){
        for (Measurement m : measurements) {
            AddEntryToChart(m);
        }
    }
}