package com.smap16e.group02.isamonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class AddParameter extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private long selectedParameter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parameter);

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                setResult(MainActivity.RESULT_CANCELED, resultIntent);
                finish();
            }
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(MainActivity.RESULT_ID, selectedParameter);
                setResult(MainActivity.RESULT_OK, resultIntent);
                finish();
            }
        });

        ArrayList<String> testArray = new ArrayList<>();
        testArray.add("Parameter 1");
        testArray.add("Parameter 2");
        testArray.add("Parameter 3");
        testArray.add("Parameter 4");
        testArray.add("Parameter 5");

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
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
}
