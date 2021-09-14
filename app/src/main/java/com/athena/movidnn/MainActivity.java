package com.athena.movidnn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button dnnButton;
    Button subjectiveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        dnnButton = findViewById(R.id.dnnButton);
        subjectiveButton = findViewById(R.id.subjectiveButton);
        assignOnClickFunctions();
    }


    public void assignOnClickFunctions() {
        dnnButton.setOnClickListener(this::startDNN);
        subjectiveButton.setOnClickListener(this::startSubjective);
    }

    public void startDNN(View view) {
        Intent dnnIntent = new Intent(this, DNNConfig.class);
        startActivity(dnnIntent);
    }

    public void startSubjective(View view) {
        Intent subjectiveIntent = new Intent(this, SubjectiveConfig.class);
        startActivity(subjectiveIntent);
    }
}