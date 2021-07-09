package com.athena.mobiledemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button dnnButton;
    Button abrButton;
    Button subjectiveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dnnButton = (Button) findViewById(R.id.dnnButton);
        abrButton = (Button) findViewById(R.id.abrButton);
        subjectiveButton = (Button) findViewById(R.id.subjectiveButton);
        assignOnClickFunctions();
    }


    public void assignOnClickFunctions() {
        dnnButton.setOnClickListener(this::startDNN);
        abrButton.setOnClickListener(this::startABR);
        subjectiveButton.setOnClickListener(this::startSubjective);
    }

    public void startDNN(View view) {
        Intent dnnIntent = new Intent(this, DNNActivity.class);
        startActivity(dnnIntent);
    }

    public void startABR(View view) {
        Intent abrIntent = new Intent(this, ABRActivity.class);
        startActivity(abrIntent);
    }

    public void startSubjective(View view) {
        Intent dnnIntent = new Intent(this, SubjectiveInstruction.class);
        startActivity(dnnIntent);
    }
}