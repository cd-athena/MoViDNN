package com.athena.mobiledemo;

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
    private static int       NUM_OF_TEST_VIDEO;
    private VideoView       videoView;
    private MediaController mediaController;
    private static int      video_index = 1;
    private static int      current_num_videos = 0;

    private static List<String> checkedNetworks = new ArrayList<>();
    private static List<String> checkedScales   = new ArrayList<>();
    private static List<String> checkedVideos   = new ArrayList<>();

    public List<Integer>    rate = new ArrayList<>();
    public List<String>     videoNames = new ArrayList<>();
    public List<String>     videoPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjective_test);

        rate.clear();
        videoNames.clear();
        // Get all test videos
        String video_folder_string = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults";
//        File directory = new File(video_folder_string);


        // get subjective configuration
        checkedNetworks = getCheckedItem(SubjectiveConfig.getCheckedNetworks(), SubjectiveConfig.networks);
        checkedScales   = getCheckedItem(SubjectiveConfig.getCheckedScales(), SubjectiveConfig.scales);
        checkedVideos   = getCheckedItem(SubjectiveConfig.getCheckedVideos(), SubjectiveConfig.videos);

//        File[] files = directory.listFiles();
//
//        for (int i = 0; i < files.length; i++) {
//            video_paths.add(files[i].getAbsolutePath());
//            Log.i("Minh", "video_path " + i + ": " + video_paths.get(video_paths.size()-1));
//        }

//        Log.e("Minh", "Video_filder_String: " + video_folder_string);
        for (int i = 0; i < checkedVideos.size(); i ++) {
            for (int j = 0; j < checkedNetworks.size(); j ++) {
                for (int k = 0; k < checkedScales.size(); k ++) {
                    String current_file_path =  video_folder_string + '/' +
                                                checkedVideos.get(i) + '_' +
                                                checkedNetworks.get(j) + '_' +
                                                checkedScales.get(k) + ".mp4";

                    Log.e("Video", "===> Video: " + current_file_path);

                    videoPaths.add(current_file_path);
                }
            }
        }

        NUM_OF_TEST_VIDEO = videoPaths.size();

        onSubjectiveTestRunning();
    }

    private List<String> getCheckedItem(boolean[] arrayChecked, String[] arrayString) {
        List<String> output = new ArrayList<>();

        for (int i = 0; i < arrayChecked.length; i++) {
            if (arrayChecked[i]) {
                output.add(arrayString[i]);
            }
        }
        return output;
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
                Log.e("Minh", "==> Video name: " + videoName);
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
                Log.e("Minh", "===> Video # " + rate.size() + ". Rate: "
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
        final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/SubjectiveResults");

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
                String string = videoNames.get(i) + ',' +
                                rate.get(i) + '\n';
                log_writer.write(string);
            }

            log_writer.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String[] options = {"Again", "Home"};
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
                startActivity(intent);
            }
        });

        builder.show();

        Log.i("MINH", "////////////// TEST SESSION ENDED /////////////");
    }

}