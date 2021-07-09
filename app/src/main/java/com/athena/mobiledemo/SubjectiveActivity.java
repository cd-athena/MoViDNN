package com.athena.mobiledemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SubjectiveActivity extends AppCompatActivity {

    private VideoView       videoView;
    private MediaController mediaController;
    public List<Integer>    rate = new ArrayList<>();
    public List<Integer>    video_id = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjective_test);


        rate.clear();
        video_id.clear();
        onSubjectiveTestRunning();

    }

    protected void onSubjectiveTestRunning() {
        videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.test_video;
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
                onTestVideoEnd();
            }
        });
        // TODO: show rate scale and record selection.
    }

    protected void onTestVideoEnd() {
        String[] rates_str = {"1: Very poor", "2: Poor", "3: Average", "4: Good", "5: Excellent"};
        int[] rates_int = {1, 2, 3, 4, 5};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate this video");
        builder.setItems(rates_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rate.add(rates_int[which]);
                video_id.add(1234);
                Log.e("Minh", "===> Video " + rate.size() + ": "
                        + rate.get(rate.size()-1) + " for video "
                        + video_id.get(video_id.size()-1));

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        onSubjectiveTestRunning();
                    }
                }, 500);

            }
        });
        builder.show();
    }

}