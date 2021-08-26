package com.athena.mobiledemo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class SubjectiveConfig extends AppCompatActivity {
    // Status check
    boolean[] setup = {false, false, false};
    Button nextButton;

    public final static String[] networks = new String[]{"NetworkA", "NetworkB", "NetworkC"};
    private static boolean[]     checkedNetworks = new boolean[] {false, false, false};

    public final static String[] scales = new String[]{"x2", "x3", "x4"};
    private static boolean[]     checkedScales = new boolean[] {false, false, false};

    public final static String[] videos = new String[]{"BigBuckBunny", "StearsOfSteel", "TBD"};
    private static boolean[]     checkedVideos = new boolean[] {false, false, false};

    private boolean checkStartStatus() {
        for (boolean b : setup) if (!b) return false;
        return true;
    }

    private void approveColor(Button button) {
        button.setBackgroundColor(getColor(R.color.athena_blue));
        if (checkStartStatus())
            nextButton.setBackgroundColor(getColor(R.color.athena_blue));
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjective_config);

        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this::startInstruction);

        // For network selection
        Button networkSelectionButton = findViewById(R.id.networkSelectionButton);
        networkSelectionButton.setOnClickListener(new View.OnClickListener() { // TODO: source: https://android--code.blogspot.com/2015/08/android-alertdialog-multichoice.html
            @Override
            public void onClick(View v) {
                final List<String> networkList = Arrays.asList(networks);

                AlertDialog.Builder builder = new AlertDialog.Builder(SubjectiveConfig.this);
                builder.setMultiChoiceItems(networks, checkedNetworks, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedNetworks[which] = isChecked;
                        String currentItem = networkList.get(which);
                    }
                });

                // Specify the dialog is not cancelable
                builder.setCancelable(false);

                // Set the title
                builder.setTitle("Select tested network");

                // Set the positive/yes button click listener
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(ifNothingChecked(checkedNetworks))
                            alertBox("No network is selected, please select at least one");
                        else {
                            setup[0] = true;
                            approveColor(networkSelectionButton);
                        }
                    }
                });

                // Set the neutral/cancel button click listener
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something when click neutral button
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // For scale selection
        Button scaleSelectionButton = findViewById(R.id.scaleSelectionButton);
        scaleSelectionButton.setOnClickListener(new View.OnClickListener() { // TODO: source: https://android--code.blogspot.com/2015/08/android-alertdialog-multichoice.html
            @Override
            public void onClick(View v) {
                final List<String> scaleList = Arrays.asList(scales);

                AlertDialog.Builder builder = new AlertDialog.Builder(SubjectiveConfig.this);
                builder.setMultiChoiceItems(scales, checkedScales, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedScales[which] = isChecked;
                        String currentItem = scaleList.get(which);
                    }
                });

                // Specify the dialog is not cancelable
                builder.setCancelable(false);

                // Set the title
                builder.setTitle("Select scales for networks");

                // Set the positive/yes button click listener
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(ifNothingChecked(checkedScales))
                            alertBox("No scale is selected, please select at least one");
                        else {
                            setup[1] = true;
                            approveColor(scaleSelectionButton);
                        }
                    }
                });

                // Set the neutral/cancel button click listener
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something when click neutral button
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // For video selection
        Button videoSelectionButton = findViewById(R.id.videoSelectionButton);
        videoSelectionButton.setOnClickListener(new View.OnClickListener() { // TODO: source: https://android--code.blogspot.com/2015/08/android-alertdialog-multichoice.html
            @Override
            public void onClick(View v) {
                final List<String> videoList = Arrays.asList(videos);

                AlertDialog.Builder builder = new AlertDialog.Builder(SubjectiveConfig.this);
                builder.setMultiChoiceItems(videos, checkedVideos, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedVideos[which] = isChecked;
                        String currentItem = videoList.get(which);
                    }
                });

                // Specify the dialog is not cancelable
                builder.setCancelable(false);

                // Set the title
                builder.setTitle("Select tested videos");

                // Set the positive/yes button click listener
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(ifNothingChecked(checkedVideos))
                            alertBox("No video is selected, please select at least one");
                        else {
                            setup[2] = true;
                            approveColor(videoSelectionButton);
                        }
                    }
                });

                // Set the neutral/cancel button click listener
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something when click neutral button
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private boolean ifNothingChecked(boolean[] array) {
        for (boolean b : array) if (b) return false;
        return true;
    }

    private void alertBox(String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(SubjectiveConfig.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void startInstruction(View view) {
        if (!checkStartStatus())
            alertBox("Some parameters are not selected. Please check again");
        else {
            Intent instructionIntent = new Intent(this, SubjectiveInstruction.class);
            startActivity(instructionIntent);
        }
    }

    public static boolean[] getCheckedNetworks() {
        return checkedNetworks;
    }

    public static boolean[] getCheckedScales() {
        return checkedScales;
    }

    public static boolean[] getCheckedVideos() {
        return checkedVideos;
    }
}
