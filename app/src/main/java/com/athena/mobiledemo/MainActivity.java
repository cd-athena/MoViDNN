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
        dnnButton = findViewById(R.id.dnnButton);
        abrButton = findViewById(R.id.abrButton);
        subjectiveButton = findViewById(R.id.subjectiveButton);
        assignOnClickFunctions();
    }


    public void assignOnClickFunctions() {
        dnnButton.setOnClickListener(this::startDNN);
        abrButton.setOnClickListener(this::startABR);
        subjectiveButton.setOnClickListener(this::startSubjective);
    }

    public void startDNN(View view) {
        Intent dnnIntent = new Intent(this, DNNConfig.class);
        startActivity(dnnIntent);
    }

    public void startABR(View view) {
        Intent abrIntent = new Intent(this, ABRActivity.class);
        startActivity(abrIntent);
    }

    public void startSubjective(View view) {
        Intent subjectiveIntent = new Intent(this, SubjectiveConfig.class);
        startActivity(subjectiveIntent);
    }
}