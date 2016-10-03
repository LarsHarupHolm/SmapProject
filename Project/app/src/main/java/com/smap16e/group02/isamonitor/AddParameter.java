package com.smap16e.group02.isamonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.smap16e.group02.isamonitor.model.Measurement;
import com.smap16e.group02.isamonitor.model.Parameter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddParameter extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private long selectedParameter = -1;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parameter);

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
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(ParameterListActivity.RESULT_ID, selectedParameter);
                setResult(ParameterListActivity.RESULT_OK, resultIntent);
                finish();
            }
        });

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayList<String> testArray = new ArrayList<>();
        List<Parameter> parameterList = LoadParametersFromJson();

        for (Parameter parameter : parameterList) {
            testArray.add(parameter.getName() + parameter.getSurname());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, testArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedParameter = id+1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Wat
    }

    private List<Parameter> LoadParametersFromJson(){
        List<Parameter> parameterList = new ArrayList<>();
        String json;
        try {
            InputStream is = getAssets().open("parameters.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            for(int i = 1; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i-1);
                Parameter parameter = new Parameter(){};
                parameter.setId(jsonObject.getInt("id"));
                parameter.setName(jsonObject.getString("name"));
                parameter.setSurname(jsonObject.getString("surname"));
                parameter.setUnit(jsonObject.getString("unit"));
                parameter.setActive(jsonObject.getBoolean("isActive"));
                parameterList.add(parameter);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return parameterList;
    }
}
