package com.athena.movidnn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DNNConfig extends AppCompatActivity {
    int checkedNetwork = -1;
    int checkedAccelerator = -1;
    boolean[] setup = {false, false, false};
    Button dnnSelector;
    Button acceleratorSelector;
    Button videoSelector;
    Button startButton;
    SwitchCompat defaultVideoSwitch;
    SwitchCompat defaultNetworkSwitch;
    private static String[] availableNetworks;
    String[] accelerators;
    private static String[] availableVideos;
    boolean[] checkedVideos;
    String[] selectedVideos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnnconfig);
        defaultVideoSwitch = findViewById(R.id.defaultVideosSwitch);
        defaultNetworkSwitch = findViewById(R.id.defaultNetworksSwitch);
        dnnSelector = findViewById(R.id.network_select_button);
        acceleratorSelector = findViewById(R.id.accelerator_picker);
        videoSelector = findViewById(R.id.video_picker);
        startButton = findViewById(R.id.start_button);
        setOnClicks();
    }

    public void fillNetworks() {
        ArrayList<String> networkList = new ArrayList<>();
        String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MoViDNN/Networks";
        File inputDirectory = new File(directoryPath);
        File[] additionalNetworks = inputDirectory.listFiles();
        for (File additionalNetwork : additionalNetworks) {
            networkList.add(additionalNetwork.getName().replace(".tflite", ""));
        }
        availableNetworks = (String[]) networkList.toArray(new String[0]);
        if (!defaultNetworkSwitch.isChecked()) {
            filterDefaultNetworks();
        }
    }

    private void filterDefaultNetworks() {
        ArrayList<String> filteredNetworks = new ArrayList<>();
        Set<String> defaultNetworks = new HashSet<>(Arrays.asList("dncnn_x1", "espcn_x2",
                "espcn_x3", "espcn_x4", "evsrnet_x2", "evsrnet_x3", "evsrnet_x4"));
        for(String network: availableNetworks) {
            if(!defaultNetworks.contains(network))
                filteredNetworks.add(network);
        }
        availableNetworks = (String[]) filteredNetworks.toArray(new String[0]);
    }

    private void filterDefaultVideos() {
        ArrayList<String> filteredVideos = new ArrayList<>();
        Set<String> defaultVideos = new HashSet<>(Arrays.asList("BotanicalGarden", "ScreenRecording",
                "SoccerGame", "TearsOfSteel", "Traffic"));
        for(String video: availableVideos) {
            if(!defaultVideos.contains(video))
                filteredVideos.add(video);
        }
        availableVideos = (String[]) filteredVideos.toArray(new String[0]);
    }

    private void fillVideos() {
        ArrayList<String> videoList = new ArrayList<>();
        String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MoViDNN/InputVideos";
        File inputDirectory = new File(directoryPath);
        File[] additionalVideos = inputDirectory.listFiles();
        for (File additionalVideo : additionalVideos) {
            videoList.add(additionalVideo.getName().replace(".mp4", ""));
        }
        availableVideos = (String[]) videoList.toArray(new String[0]);
        if (!defaultVideoSwitch.isChecked()) {
            filterDefaultVideos();
        }
        checkedVideos = new boolean[availableVideos.length];
    }

    private void setOnClicks() {
        dnnSelector.setOnClickListener(this::pickDNN);
        acceleratorSelector.setOnClickListener(this::pickAccelerator);
        videoSelector.setOnClickListener(this::pickVideo);
        startButton.setOnClickListener(this::completeSetup);
    }

    private void alertBox(String message) {
        final androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(this).create();
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

    private void approveColor(Button button) {
        button.setBackgroundColor(getColor(R.color.athena_blue));
        if (checkStartStatus())
            startButton.setBackgroundColor(getColor(R.color.athena_blue));
    }

    private boolean checkStartStatus() {
        for (boolean b : setup) if (!b) return false;
            return true;
    }

    private void pickDNN(View view) {
        fillNetworks();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Pick the DNN");
        alertDialog.setSingleChoiceItems(availableNetworks, checkedNetwork, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedNetwork = which;
            }
        });
        // Set the positive/yes button click listener
        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(checkedNetwork == -1)
                    alertBox("No network is selected. Please select one!");
                else {
                    setup[0] = true;
                    approveColor(dnnSelector);
                }
            }
        });
        // Set the neutral/cancel button click listener
        alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click neutral button
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void pickAccelerator(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Pick the Accelerator");
        accelerators = new String[]{"CPU", "GPU", "NNAPI"};
        alertDialog.setSingleChoiceItems(accelerators, checkedAccelerator, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedAccelerator = which;
            }
        });
        // Set the positive/yes button click listener
        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(checkedAccelerator == -1)
                    alertBox("No accelerator is selected. Please select one");
                else {
                    setup[1] = true;
                    approveColor(acceleratorSelector);
                }
            }
        });
        // Set the neutral/cancel button click listener
        alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click neutral button
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private boolean isNothingChecked(boolean[] array) {
        for (boolean b : array) if (b) return false;
        return true;
    }

    private void pickVideo(View view) {
        fillVideos();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Pick the Videos");
        final List<String> videoList = Arrays.asList(availableVideos);

        alertDialog.setMultiChoiceItems(availableVideos, checkedVideos, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedVideos[which] = isChecked;
            }
        });

        // Set the positive/yes button click listener
        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(isNothingChecked(checkedVideos))
                    alertBox("No videos is selected. Please select one");
                else {
                    setup[2] = true;
                    setNamesOfSelectedVideos();
                    approveColor(videoSelector);
                }
            }
        });
        // Set the neutral/cancel button click listener
        alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click neutral button
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void setNamesOfSelectedVideos() {
        ArrayList<String> pickedVideoNames = new ArrayList<String>();
        for(int i = 0; i < checkedVideos.length; i++) {
            if(checkedVideos[i])
                pickedVideoNames.add(availableVideos[i]);
        }
        selectedVideos = (String[]) pickedVideoNames.toArray(new String[0]);
    }

    private void completeSetup(View view) {
        if(checkStartStatus()) {
            Intent dnnIntent = new Intent(getBaseContext(), DNNActivity.class);
            Bundle setupData = new Bundle();
            setupData.putString("SELECTED_NETWORK", availableNetworks[checkedNetwork]);
            setupData.putString("SELECTED_ACCELERATOR", accelerators[checkedAccelerator]);
            setupData.putStringArray("SELECTED_VIDEOS", selectedVideos);
            dnnIntent.putExtras(setupData);
            startActivity(dnnIntent);
        } else {
            alertBox("Please make sure you have completed the setup!");
        }
    }
}