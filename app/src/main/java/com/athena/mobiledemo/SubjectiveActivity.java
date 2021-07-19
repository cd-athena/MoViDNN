package com.athena.mobiledemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SubjectiveActivity extends AppCompatActivity {

    private VideoView       videoView;
    private MediaController mediaController;
    private static int      video_index = 1;

    public List<Integer>    rate = new ArrayList<>();
    public List<String>     video_id = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjective_test);


        rate.clear();
        video_id.clear();
        onSubjectiveTestRunning(video_index);

    }

    protected void onSubjectiveTestRunning(int video_index) {
        int video_id = getResources().getIdentifier("video_" + video_index,
                "raw", getPackageName());
        videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + video_id;
        Uri uri = Uri.parse(videoPath);
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
                onTestVideoEnd(video_index);
            }
        });
        // TODO: show rate scale and record selection.
    }

    protected void onTestVideoEnd(int video_index) {
        String[] rates_str = {"1: Very poor", "2: Poor", "3: Average", "4: Good", "5: Excellent"};
        int[] rates_int = {1, 2, 3, 4, 5};
        final int currentVideoIndex = video_index;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate this video");
        builder.setItems(rates_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rate.add(rates_int[which]);
                String videoName = "video_" + String.valueOf(currentVideoIndex);
                video_id.add(videoName);
                Log.e("Minh", "===> Video " + rate.size() + ": "
                        + rate.get(rate.size()-1) + " for video "
                        + video_id.get(video_id.size()-1));

                if (currentVideoIndex == 2) {
                    onSubjectTestEnd();
                    return;
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // wait for 500ms before going to the next screen
                        onSubjectiveTestRunning(currentVideoIndex + 1);
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
        final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/");

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.e ("ALERT", "Could not create the directories");
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
            log_writer.write("VideoIdx,Rate\n"); // TODO: add PSNR, SSIM

            for (int i = 0; i < rate.size(); i ++) {
                String string = video_id.get(i) + ',' +
                                rate.get(i) + '\n';
                log_writer.write(string);
            }

            log_writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("MINH", "////////////// TEST SESSION ENDED /////////////");
    }

}