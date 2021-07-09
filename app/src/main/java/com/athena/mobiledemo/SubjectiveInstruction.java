package com.athena.mobiledemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SubjectiveInstruction extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjective_instruction);


        final TextView instruction = (TextView) findViewById(R.id.instruction);
        instruction.setText(R.string.instruction);

        Button startbutton = findViewById(R.id.subjectiveStartButton);
        startbutton.setOnClickListener(this::startSubjective);
    }

    public void startSubjective(View view) {
        Intent dnnIntent = new Intent(this, SubjectiveActivity.class);
        startActivity(dnnIntent);
    }
}
