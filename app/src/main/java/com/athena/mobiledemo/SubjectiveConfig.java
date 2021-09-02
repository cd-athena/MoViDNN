package com.athena.mobiledemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SubjectiveConfig extends AppCompatActivity {
    // Status check
//    boolean[] setup = {false, false, false};
    Button nextButton;

    public static String[]    availableModels;
    private static boolean[]  checkedModels;

    public static String[]    availableVideos;
    public static String[]    availableVideosPaths;
    private static boolean[]  checkedVideos;
    public static ArrayList<String> testedVideosPaths;

    final String inputDatabaseDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/Input";
    final String DnnAppliedDatabaseDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults";

//    private boolean checkStartStatus() {
//        for (boolean b : setup) if (!b) return false;
//        return true;
//    }

    private void approveColor(Button button) {
        button.setBackgroundColor(getColor(R.color.athena_blue));
        if (!ifNothingChecked(checkedVideos))
            nextButton.setBackgroundColor(getColor(R.color.athena_blue));
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjective_config);

        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this::startInstruction);

        fillNetworks();
        fillVideos();

        // For network selection
        Button networkSelectionButton = findViewById(R.id.networkSelectionButton);
        networkSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> networkList = Arrays.asList(availableModels);

                AlertDialog.Builder builder = new AlertDialog.Builder(SubjectiveConfig.this);
                builder.setMultiChoiceItems(availableModels, checkedModels, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedModels[which] = isChecked;
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
                        if(!ifNothingChecked(checkedModels))
                            approveColor(networkSelectionButton);
                        else
                            networkSelectionButton.setBackgroundColor(getColor(R.color.NotDoneButton));

                        fillVideos();
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
        videoSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> videoList = Arrays.asList(availableVideos);

                AlertDialog.Builder builder = new AlertDialog.Builder(SubjectiveConfig.this);
                builder.setMultiChoiceItems(availableVideos, checkedVideos, new DialogInterface.OnMultiChoiceClickListener() {
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
                        if (ifNothingChecked(checkedVideos)){
                            alertBox("No video is selected, please select at least one");
                            videoSelectionButton.setBackgroundColor(getColor(R.color.NotDoneButton));
                            nextButton.setBackgroundColor(getColor(R.color.NotDoneButton));
                        }
                        else {
                            approveColor(videoSelectionButton);
                            testedVideosPaths = new ArrayList<>();
                            ArrayList<String> referenceVideosPaths = new ArrayList<>();

                            if (ifNothingChecked(checkedModels)) {
                                for (int i = 0; i < checkedVideos.length; i++) {
                                    if (checkedVideos[i]) {
                                        testedVideosPaths.add(getReferenceTestedVideoPath(availableVideos[i]));
                                        Log.e("Minh", "tested REF Path: " + getReferenceTestedVideoPath(availableVideos[i]));
                                    }
                                }
                            }
                            else {
                                for (int i = 0; i < checkedVideos.length; i++) {
                                    if (checkedVideos[i]) {
                                        testedVideosPaths.add(getDnnVideoPath(availableVideos[i]));
                                        Log.e("Minh", "testedPath: " + i + ": " + getDnnVideoPath(availableVideos[i]));


                                        if (testedVideosPaths.size() > 0) {
                                            for (int j = 0; j < testedVideosPaths.size(); j++) {
                                                Log.e("Minh", "testedPath: " + testedVideosPaths.get(j));

                                                if (testedVideosPaths.get(j).equals(getReferenceTestedVideoPath(availableVideos[i]))) {
                                                    Log.e("Minh", "======= Already added reference");
                                                    break;
                                                } else {
                                                    if (j == testedVideosPaths.size() - 1) {
                                                        testedVideosPaths.add(getReferenceTestedVideoPath(availableVideos[i]));
                                                        Log.e("Minh", "======= Add NEW reference");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
    private String getDnnVideoPath (String videoName) {
        return DnnAppliedDatabaseDirectoryPath + '/' + videoName + ".mp4";
    }

    private String getRefVideopath (String videoName){
        return inputDatabaseDirectoryPath + '/' + videoName.split("_")[0] + ".mp4";
    }

    public void fillNetworks() {
        try {
            availableModels = getAssets().list("models/");
            for (int i =0; i < availableModels.length; i++) {
                availableModels[i] = availableModels[i].replace(".tflite", "");
            }
        } catch (IOException e) {
            Log.e("Error:", "Error while reading list of models");
        }

        checkedModels = new boolean[availableModels.length];
    }

    private void fillVideos() {

        if (ifNothingChecked(checkedModels)) {  // subjective test without SR
            File inputDirectory = new File(inputDatabaseDirectoryPath);
            File[] videos = inputDirectory.listFiles();
            availableVideos = new String[videos.length];
            availableVideosPaths = new String[videos.length];

            for (int i =0; i < videos.length; i++) {
                availableVideos[i] = videos[i].getName().replace(".mp4", "");
                availableVideosPaths[i] = videos[i].getAbsolutePath();
                Log.e("Minh", "Available video paths: "+ videos[i].getAbsolutePath());
            }
        }
        else {  // subjective test with SR and reference videos
            File inputDirectory = new File(DnnAppliedDatabaseDirectoryPath);
            File[] videos = inputDirectory.listFiles();
            ArrayList<String> suitableVideos = new ArrayList<>();
//            ArrayList<String> referenceVideos = new ArrayList<>();

            for (int i = 0; i < videos.length; i++) {
                for (int j = 0; j < availableModels.length; j++){
                    if (checkedModels[j]) {
                        String pattern = ".*_" + availableModels[j] + ".mp4";
                        final Pattern p = Pattern.compile(pattern);
                        if (p.matcher(videos[i].getName()).matches()) {
                            suitableVideos.add(videos[i].getName().replace(".mp4", ""));
                            Log.e("Minh", "********** Found Available Video for selection: " + videos[i].getName());

//                            if (referenceVideos.size() == 0) {
//                                referenceVideos.add(videos[i].getName().split("_")[0]);
//                            }
//                            else {
//                                for (String refVideo : referenceVideos) {
//                                    if (!refVideo.equals(videos[i].getName().split("_")[0])) {
//                                        referenceVideos.add(videos[i].getName().split("_")[0]);
//                                        Log.e("Minh", "********** Found Video: " + videos[i].getName() + " from Ref Video: " + referenceVideos.get(referenceVideos.size() - 1));
//                                    }
//                                    else {
//                                        Log.e("Minh", "********** Found Video: " + videos[i].getName() + " from OLD Ref Video: " + referenceVideos.get(referenceVideos.size() - 1));
//                                    }
//                                }
//                            }
                        }
                    }
                }
            }

            if (suitableVideos.size() == 0) {
                Log.e("Minh", "No Available Videos");
            }
            else {
                availableVideos = new String[suitableVideos.size()];
//                availableVideosPaths = new String[suitableVideos.size() + referenceVideos.size()];
                for (int i = 0; i < suitableVideos.size(); i++) {
                    availableVideos[i] = suitableVideos.get(i);
//                    availableVideosPaths[i] = DnnAppliedDatabaseDirectoryPath + '/' + availableVideos[i] + ".mp4";
//                    Log.e("Minh", "===> video path # " + i + ": " + availableVideosPaths[i]);
                }

//                for (int i = 0; i < referenceVideos.size(); i++) {
//                    availableVideosPaths[i + availableVideos.length] = inputDatabaseDirectoryPath + '/' + referenceVideos.get(i);
//                    Log.e("Minh", "===> Ref video path # " + i + ": " + availableVideosPaths[i]);
//                }
            }
        }

        checkedVideos = new boolean[availableVideos.length];
    }

    private boolean ifNothingChecked(boolean[] array) {
        for (boolean b : array) if (b) return false;
        return true;
    }
    private String getReferenceTestedVideoPath(String video ) {
        String refVideoName = video.split("_")[0];
        return inputDatabaseDirectoryPath + "/" + refVideoName + ".mp4";
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
        if (ifNothingChecked(checkedVideos))
            alertBox("Some parameters are not selected. Please check again");
        else {
            Intent instructionIntent = new Intent(this, SubjectiveInstruction.class);
            startActivity(instructionIntent);
        }
    }

    public static boolean[] getCheckedModels() {
        return checkedModels;
    }
    public static boolean[] getCheckedVideos() {
        return checkedVideos;
    }
}
