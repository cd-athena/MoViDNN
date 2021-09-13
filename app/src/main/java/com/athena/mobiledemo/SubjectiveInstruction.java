package com.athena.mobiledemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SubjectiveInstruction extends AppCompatActivity {
    Bundle data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        data = this.getIntent().getExtras();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjective_instruction);

        final TextView instruction = (TextView) findViewById(R.id.instruction);
        instruction.setText(R.string.instruction);

        Button startbutton = findViewById(R.id.subjectiveStartButton);
        startbutton.setOnClickListener(this::startSubjective);
    }

    private void startSubjective(View view) {
        Intent subjectiveActivityIntent = new Intent(this, SubjectiveActivity.class);
        subjectiveActivityIntent.putExtras(data);
        startActivity(subjectiveActivityIntent);
    }
}
