package com.athena.movidnn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button dnnButton;
    Button subjectiveButton;
    final File inputDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MoViDNN/InputVideos");
    final File networksDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MoViDNN/Networks");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        dnnButton = findViewById(R.id.dnnButton);
        subjectiveButton = findViewById(R.id.subjectiveButton);
        if (!inputDir.exists()) {
            if (!inputDir.mkdir()) {
                Log.e ("Error", "Could not create the directories");
            }
        }
        if (!networksDir.exists()) {
            if (!networksDir.mkdir()) {
                Log.e ("Error", "Could not create the directories");
            }
        }
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