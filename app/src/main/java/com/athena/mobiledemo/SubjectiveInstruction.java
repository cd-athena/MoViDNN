package com.athena.mobiledemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class SubjectiveInstruction extends AppCompatActivity {
    private EditText nameEditText;
    private EditText ageEditText;

    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;

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

    private void startSubjective(View view) {
        Intent dnnIntent = new Intent(this, SubjectiveActivity.class);
        startActivity(dnnIntent);
    }
}
