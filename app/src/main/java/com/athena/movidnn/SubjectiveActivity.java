package com.athena.movidnn;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


public class SubjectiveActivity extends AppCompatActivity {
    Bundle data;
    private static int       NUM_OF_TEST_VIDEO;
    private VideoView       videoView;
    private MediaController mediaController;
    private static int      video_index = 1;
    private static int      current_num_videos = 0;

    public List<Integer>    rate = new ArrayList<>();
    public List<String>     videoNames = new ArrayList<>();
    public List<String>     videoPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjective_test);
        data = this.getIntent().getExtras();
        videoPaths = data.getStringArrayList("SELECTED_VIDEOS");
        rate.clear();
        videoNames.clear();
        // Get all test videos

        for (String videoPath : videoPaths) {
            Log.e("Minh", "This video will be tested: " + videoPath);
        }

        NUM_OF_TEST_VIDEO = videoPaths.size();

        onSubjectiveTestRunning();
    }

    private void onSubjectiveTestRunning() {
        videoView = findViewById(R.id.videoView);
        video_index = new Random().nextInt(videoPaths.size());
        Uri uri = Uri.parse(videoPaths.get(video_index));
        videoPaths.remove(video_index);
        videoView.setVideoURI(uri);

        mediaController  = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        Button playButton = findViewById(R.id.playButton);
        playButton.setVisibility(View.VISIBLE);
        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                playButton.setVisibility(View.GONE);
                videoView.start();  // auto play video
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                String[] pathElements = uri.getPath().split("/");
                String videoName = pathElements[pathElements.length-1];
                onTestVideoEnd(videoName);
            }
        });
        // TODO: show rate scale and record selection.
    }

    private void onTestVideoEnd(String videoName) {
        String[] rates_str = {"5: Excellent", "4: Good", "3: Fair", "2: Poor", "1: Bad"};
        int[] rates_int = {5, 4, 3, 2, 1};
        current_num_videos++;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How is the quality of this video?");
        builder.setItems(rates_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rate.add(rates_int[which]);
                videoNames.add(videoName);
                Log.i("Log", "Video # " + rate.size() + ". MoS: "
                        + rate.get(rate.size()-1) + " for video "
                        + videoNames.get(videoNames.size()-1));

                if (current_num_videos == NUM_OF_TEST_VIDEO) {
                    current_num_videos = 0;
                    onSubjectTestEnd();
                    return;
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // wait for 500ms before going to the next screen
                        onSubjectiveTestRunning();
                    }
                }, 500);

            }
        });
        builder.show();
    }

    private void onSubjectTestEnd() {
        Calendar calendar = Calendar.getInstance();
        String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String year = String.format("%04d", calendar.get(Calendar.YEAR));
        String hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.format("%02d", calendar.get(Calendar.MINUTE));

        String fileNameString = year + '_' + month + '_' + day + "__" +
                                hour + '_' + minute;
        final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MoViDNN/SubjectiveResults");

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.e ("Error", "Could not create the directories");
            }
        }

        final File log = new File (dir, fileNameString + ".csv");
        if (!log.exists()) {
            try {
                log.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Writer log_writer = new OutputStreamWriter(new FileOutputStream(log));
            log_writer.write("Model,VideoName,MoS\n");

            for (int i = 0; i < rate.size(); i ++) {
                String[] temp = videoNames.get(i).split("[_.]");
                String string = "";
                String videoName = "";
                String modelName = "";

                if (temp.length > 2) {     // DNN-applied video
                    videoName = temp[0];
                    modelName = temp[1] + "_" + temp[2];
                }
                else {                     // Input video
                    videoName = temp[0];
                    modelName = "None";
                }
                string = modelName + ',' +
                        videoName + ',' +
                        rate.get(i) + '\n';
                log_writer.write(string);
            }

            log_writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String[] options = {"AGAIN", "HOME"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setNegativeButton(options[1], new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SubjectiveActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        builder.setMessage("Thanks for joining our subjective test");
        builder.setPositiveButton(options[0], new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SubjectiveActivity.this, SubjectiveInstruction.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        });

        builder.show();

        Log.i("MINH", "////////////// TEST SESSION ENDED /////////////");
    }

}