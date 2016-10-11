package com.smap16e.group02.isamonitor.adaptors;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.smap16e.group02.isamonitor.R;
import com.smap16e.group02.isamonitor.model.AddParameterModel;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Lars on 11-10-2016.
 */

//ListView adapter with checkbox: http://techlovejump.com/android-listview-with-checkbox/

public class AddParameterAdapter extends ArrayAdapter <AddParameterModel> {

    private Context mContext;
    private LayoutInflater mInflater;
    public ArrayList<AddParameterModel> ParameterModels;

    public AddParameterAdapter(Context context, int resource, List<AddParameterModel> objects) {
        super(context, resource, objects);
        mContext = context;
        ParameterModels = (ArrayList<AddParameterModel>) objects;
        mInflater = LayoutInflater.from(mContext);
    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.add_parameter_row, parent, false);

        //fill in info to Row
        TextView textViewname = (TextView)convertView.findViewById(R.id.row_parameter_name);
        TextView textViewsurname = (TextView)convertView.findViewById(R.id.row_parameter_surname);
        CheckBox checkBoxIsChecked = (CheckBox)convertView.findViewById(R.id.row_checkbox);

        textViewname.setText(ParameterModels.get(position).name);
        textViewsurname.setText(ParameterModels.get(position).surname);
        checkBoxIsChecked.setChecked(ParameterModels.get(position).isChecked);

        checkBoxIsChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Set models[postition].isChecked accordingly
                ParameterModels.get(position).isChecked = isChecked; //Here
            }
        });
        return convertView;

        //return super.getView(position, convertView, parent);
    }
}
