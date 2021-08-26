package com.athena.mobiledemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;


class SortedFrame implements Comparable<SortedFrame> {
    // This is used to store read frames
    public final double timestamp;
    public final Bitmap frame;

    public SortedFrame(PictureWithMetadata p) {
        frame = AndroidUtil.toBitmap(p.getPicture());
        timestamp = p.getTimestamp();
    }

    @Override
    public int compareTo(SortedFrame o2) {
        return Double.compare(timestamp, o2.timestamp);
    }

}

public class DNNActivity extends AppCompatActivity {
    // Data from the setup
    Bundle data;
    // Display Objects
    Button doneButton;
    ImageView display;
    TextView executionTimeView;
    // Variables
    String selectedModel;
    String selectedAccelerator;
    String[] selectedVideos;
    Interpreter srModel;
    Interpreter.Options options;
    GpuDelegate gpuDelegate;
    NnApiDelegate nnApiDelegate;
    CompatibilityList compatList;
    Bitmap lrImg;
    final File resultsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults");
    // Input variables
    int width = 480;
    int height = 270;
    // Video processing variables
    ArrayList<SortedFrame> videoFrames = new ArrayList<>();
    ArrayList<Bitmap> srFrames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = this.getIntent().getExtras();
        selectedModel = data.getString("SELECTED_NETWORK");
        selectedAccelerator = data.getString("SELECTED_ACCELERATOR");
        selectedVideos = data.getStringArray("SELECTED_VIDEOS");
        setContentView(R.layout.activity_dnn);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        doneButton = findViewById(R.id.doneButton);
        display = findViewById(R.id.srImage);
        executionTimeView = findViewById(R.id.executionTimeView);
        compatList = new CompatibilityList();

        if (!resultsDir.exists()) {
            if (!resultsDir.mkdir()) {
                Log.e ("ALERT", "Could not create the directories");
            }
        }
        setOnClicks();
        runSR();
    }


    private void setOnClicks() {
        doneButton.setOnClickListener(this::completeTest);
    }

    private void completeTest(View view) {

    }

    private MappedByteBuffer loadModelFile(String modelName) throws IOException {
        AssetFileDescriptor modelFileDescriptor = this.getAssets().openFd("models/" + modelName);
        FileInputStream inputStream = new FileInputStream(modelFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = modelFileDescriptor.getStartOffset();
        long declaredLength = modelFileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void closeAllDelagates() {
        if(null != srModel)
            srModel.close();
        if(null != nnApiDelegate)
            nnApiDelegate.close();
        if(null != gpuDelegate)
            gpuDelegate.close();
    }

    private void initializeDNNwithGPU() {
        closeAllDelagates();
        try {
            options = new Interpreter.Options();
            // GPU delegate
            GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
            gpuDelegate = new GpuDelegate(delegateOptions);
            options.addDelegate(gpuDelegate);
            srModel = new Interpreter(loadModelFile(selectedModel + ".tflite"), options);
        } catch (IOException e) {
            Log.e("EKREM:" ,"Error while initializing model with GPU: " + e);
        }
    }

    private void initializeDNNwithNNAPI() {
        closeAllDelagates();
        try {
            options = new Interpreter.Options();
            // GPU delegate
            nnApiDelegate = new NnApiDelegate();
            options.addDelegate(nnApiDelegate);
            srModel = new Interpreter(loadModelFile(selectedModel + ".tflite"), options);
        } catch (IOException e) {
            Log.e("EKREM:" ,"Error while initializing model with NNAPI: " + e);
        }
    }

    private void initializeDNNwithCPU() {
        closeAllDelagates();
        try {
            srModel = new Interpreter(loadModelFile(selectedModel + ".tflite"));
        } catch (IOException e) {
            Log.e("EKREM:" ,"Error while initializing model: " + e);
        }
    }

    private void setScale(String modelName) {
        // Check the model name and determine the scale
        String scale = modelName.split("_")[1];
        switch (scale) {
            case "x1":
                width = 1920;
                height = 1080;
                break;
            case "x2":
                width = 960;
                height = 540;
                break;
            case "x3":
                width = 640;
                height = 360;
                break;
            case "x4":
                width = 480;
                height = 270;
                break;
        }
    }

    private void initializeDNN() {
        // Check the options here and run the corresponding function
        setScale(selectedModel);
        switch (selectedAccelerator) {
            case "CPU":
                initializeDNNwithCPU();
                break;
            case "GPU":
                initializeDNNwithGPU();
                break;
            case "NNAPI":
                initializeDNNwithNNAPI();
                break;
        }
    }

    private void saveImage(Bitmap bmp, String filename) throws IOException {
        // Assume block needs to be inside a Try/Catch block.
        OutputStream fOut = null;
        File file = new File(resultsDir, filename + ".png"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        fOut = new FileOutputStream(file);

        bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
        fOut.flush(); // Not really required
        fOut.close(); // do not forget to close the stream

        MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
    }

    private Bitmap tensorToBitmap(TensorImage tensorOutput) {
        // Get the output and convert it to Bitmap
        ByteBuffer SROut = tensorOutput.getBuffer();
        SROut.rewind();
        // get the shape of output and set width height (NHWC in tensor)
        int height = tensorOutput.getHeight();
        int width = tensorOutput.getWidth();
        // placeholder bitmap image
        Bitmap bmpImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int [] pixels = new int[width * height];
        // fill the values in the byte array
        for (int i = 0; i < width * height; i++) {
            int a = 0xFF;
            float r = SROut.getFloat() * 255.0f;
            float g = SROut.getFloat() * 255.0f;
            float b = SROut.getFloat() * 255.0f;

            pixels[i] = a << 24 | ((int) r << 16) | ((int) g << 8) | (int) b;
        }
        bmpImage.setPixels(pixels, 0, width, 0, 0, width, height);

        return bmpImage;
    }

    private void saveSrVideo() {
        FileChannelWrapper out = null;
        File output = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults/test.mp4");

        try { out = NIOUtils.writableFileChannel(output.getAbsolutePath());
            AndroidSequenceEncoder encoder = new AndroidSequenceEncoder(out, Rational.R(15, 1));
            for (Bitmap bitmap : srFrames) {
                encoder.encodeImage(bitmap);
            }
            encoder.finish();
        } catch (IOException e) {
            Log.e("EKREM", "Error while encoding video: " + e);
        } finally {
            NIOUtils.closeQuietly(out);
        }
    }

    private void readFrames(String videoName) {
        try {
            Toast.makeText(this, "Reading and processing all frames, this will take some time !", Toast.LENGTH_LONG).show();
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults/" + videoName + ".mp4");
            // File file = new File(String.valueOf(getAssets().openFd("frames/video.mp4")));
            FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
            // For some reason it jitters in the first 3 seconds and dont get any metadata, skip them for now
            // grab.seekToSecondPrecise(1);
            /**
            for(int i = 0; i < 60; i++) {
                PictureWithMetadata pic = grab.getNativeFrameWithMetadata();
                videoFrames.add(new SortedFrame(pic));
            }
             **/
            // Reading all frames
            PictureWithMetadata picture;
            while (null != (picture = grab.getNativeFrameWithMetadata())) {
                videoFrames.add(new SortedFrame(picture));
            }
            // Sort frames based on the times
            Collections.sort(videoFrames);
            // Setup the display in the activity
            lrImg = videoFrames.get(5).frame;
            display.setImageBitmap(lrImg);
        } catch (IOException | JCodecException e) {
            Log.e("EKREM", "Error while reading frames: " + e);
        }
    }

    private TensorImage prepareInput() {
        TensorImage lrImage = TensorImage.fromBitmap(lrImg);
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(height, width, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(new NormalizeOp(0.0f, 255.0f))
                .build();
        lrImage = imageProcessor.process(lrImage);

        return lrImage;
    }

    private TensorImage prepareOutput() {
        TensorImage srImage = new TensorImage(DataType.FLOAT32);
        int[] srShape = new int[]{1080, 1920, 3};
        srImage.load(TensorBuffer.createFixedSize(srShape, DataType.FLOAT32));

        return srImage;
    }

    private void saveSrImage(TensorImage srImage) {
        try {
            Bitmap srImg = tensorToBitmap(srImage);
            // Change the displayed image in the activity
            display.setImageBitmap(srImg);
            saveImage(srImg, "SR_Out");
            Log.e("EKREM:", "Saved SR Image!");
        } catch (IOException e) {
            Log.e("EKREM:", "Error while saving the SR Image" + e);
        }
    }

    @SuppressLint("SetTextI18n")
    private void runSR() {
        initializeDNN();
        for(String video: selectedVideos){
            readFrames(video);
            Toast.makeText(this, "Running SR for all frames, this will take some time!", Toast.LENGTH_LONG).show();
            long difference = 0;
            for(SortedFrame frame : videoFrames) {
                lrImg = frame.frame;
                TensorImage lrImage = prepareInput();
                TensorImage srImage = prepareOutput();
                long startTime = System.currentTimeMillis();
                srModel.run(lrImage.getBuffer(), srImage.getBuffer());
                Log.e("EKREM:", "SR Run time: " + (System.currentTimeMillis() - startTime));
                difference += (System.currentTimeMillis() - startTime);
                Bitmap srImg = tensorToBitmap(srImage);
                srFrames.add(srImg);
            }
            int numOfFrames = srFrames.size();
            Log.e("EKREM", "Num of SR Frames: " + numOfFrames);
            difference /= numOfFrames;
            executionTimeView.setText("Average SR Execution Time: " + difference+ "ms - " + 1000/difference + "fps");
            Toast.makeText(this, "Saving SR video, this will take some time!", Toast.LENGTH_LONG).show();
            saveSrVideo();
            srFrames.clear();
            videoFrames.clear();
        }
    }
}