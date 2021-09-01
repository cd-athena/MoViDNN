package com.athena.mobiledemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class DNNActivity extends AppCompatActivity {
    // Data from the setup
    Bundle data;
    // Display Objects
    Button doneButton;
    TextView executionTimeView;
    TextView fpsView;
    TextView totalFramesView;
    TextView minPSNRView;
    TextView maxPSNRView;
    TextView avgPSNRView;
    TextView yPSNRView;
    TextView allSSIMView;
    TextView ySSIMView;
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
    final File framesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/Frames");
    final File srFramesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/SRFrames");
    // Input variables
    int width = 480;
    int height = 270;
    // Video processing variables
    ArrayList<String> videoFramePaths = new ArrayList<>();
    ArrayList<String> srFramePaths = new ArrayList<>();
    // Progress Bars
    ProgressBar srProgressBar;
    ProgressBar resultsProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = this.getIntent().getExtras();
        selectedModel = data.getString("SELECTED_NETWORK");
        selectedAccelerator = data.getString("SELECTED_ACCELERATOR");
        selectedVideos = data.getStringArray("SELECTED_VIDEOS");
        setContentView(R.layout.activity_dnn);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // View elements
        doneButton = findViewById(R.id.doneButton);
        executionTimeView = findViewById(R.id.executionTime);
        fpsView = findViewById(R.id.fps);
        totalFramesView = findViewById(R.id.totalFrames);
        minPSNRView = findViewById(R.id.minPSNR);
        maxPSNRView = findViewById(R.id.maxPSNR);
        avgPSNRView = findViewById(R.id.avgPSNR);
        minPSNRView = findViewById(R.id.minPSNR);
        yPSNRView = findViewById(R.id.yPSNR);
        allSSIMView = findViewById(R.id.allSSIM);
        ySSIMView = findViewById(R.id.ySSIM);

        srProgressBar = findViewById(R.id.srProgressBar);
        srProgressBar.setScaleX(6f);
        srProgressBar.setScaleY(2f);

        resultsProgressBar = findViewById(R.id.resultsProgressBar);
        resultsProgressBar.setScaleX(6f);
        resultsProgressBar.setScaleY(2f);

        compatList = new CompatibilityList();
        if (!resultsDir.exists()) {
            if (!resultsDir.mkdir()) {
                Log.e ("Error:", "Could not create the directories");
            }
        }
        setOnClicks();
        runSR();
    }


    private void setOnClicks() {
        doneButton.setOnClickListener(this::completeTest);
    }

    private void completeTest(View view) {
        // TODO Fill this function
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
            Log.e("Error:" ,"Error while initializing model with GPU: " + e);
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
            Log.e("Error:" ,"Error while initializing model with NNAPI: " + e);
        }
    }

    private void initializeDNNwithCPU() {
        closeAllDelagates();
        try {
            srModel = new Interpreter(loadModelFile(selectedModel + ".tflite"));
        } catch (IOException e) {
            Log.e("Error:" ,"Error while initializing model: " + e);
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

    private void saveSrVideo(String videoName, String fps) {
        String inputPath = srFramesDir + "/srframe_%04d.jpeg";
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults/" + videoName + "_SR.mp4";
        FFmpegSession ffmpegSession = FFmpegKit.execute("-y -i " + inputPath + " -s 1920x1080 -vf format=yuv420p,fps=" + fps + " -preset ultrafast " + outputPath);

        if (ReturnCode.isSuccess(ffmpegSession.getReturnCode())) {
            Log.e("Log:", "Saved SR video successfully");
        } else {
            // Failure
            Log.d("Error:", String.format("Saving SR video failed with state %s and rc %s.%s", ffmpegSession.getState(),
                    ffmpegSession.getReturnCode(), ffmpegSession.getFailStackTrace()));
        }
    }


    private void fillFrames() {
        File inputDirectory = new File(String.valueOf(framesDir));
        File[] frames = inputDirectory.listFiles();
        videoFramePaths = new ArrayList<>();
        for (File frame : frames) {
            videoFramePaths.add(frame.getName());
        }
    }


    private void readFrames(String videoName, String fps) {
        if (!framesDir.exists()) {
            if (!framesDir.mkdir()) {
                Log.e ("Error:", "Could not create the directories");
            }
        }
        String inpuPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults/" + videoName + ".mp4";
        String outputPath = framesDir + "/frame_%04d.png";
        // Execute the ffmpeg command
        FFmpegSession ffmpegSession = FFmpegKit.execute("-i " + inpuPath + " -vf fps=" + fps + " -preset ultrafast " + outputPath);
        if (ReturnCode.isSuccess(ffmpegSession.getReturnCode())) {
            Log.e("Log:", "Extracted frames successfully");
        } else {
            // Failure
            Log.d("Error:", String.format("Extracting frames failed with state %s and rc %s.%s", ffmpegSession.getState(),
                    ffmpegSession.getReturnCode(), ffmpegSession.getFailStackTrace()));
        }
        fillFrames();
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

    private void saveImage(Bitmap bmp, String filename) {
        // Assume block needs to be inside a Try/Catch block.
        OutputStream fOut = null;
        File file = new File(srFramesDir, filename); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        try {
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private void calculatePSNR(String video) {
        String inputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults/" + video + "_SR.mp4";
        String referencePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults/" + video + "_Ref.mp4";
        FFmpegSession ffmpegSession = FFmpegKit.execute("-i " + inputPath + " -i " + referencePath + " -filter_complex psnr -f null -");
        if (ReturnCode.isSuccess(ffmpegSession.getReturnCode())) {
            // Extract PSNRs
            String out = ffmpegSession.getOutput();
            String psnrLine = out.substring(out.lastIndexOf("PSNR"));
            String avgPSNRLine = psnrLine.substring(psnrLine.lastIndexOf("average"), psnrLine.indexOf("min"));
            String yPSNRLine = psnrLine.substring(psnrLine.lastIndexOf("y:"), psnrLine.indexOf("u:"));
            String minPSNRLine = psnrLine.substring(psnrLine.lastIndexOf("min:"), psnrLine.indexOf("max:"));
            String maxPSNRLine = psnrLine.substring(psnrLine.lastIndexOf("max:"));
            Float PSNR = Float.valueOf(avgPSNRLine.substring(8));
            Float yPSNR = Float.valueOf(yPSNRLine.substring(2));
            Float minPSNR = Float.valueOf(minPSNRLine.substring(4));
            Float maxPSNR = Float.valueOf(maxPSNRLine.substring(4));
            minPSNRView.setText(String.format("%.2f", minPSNR));
            maxPSNRView.setText(String.format("%.2f", maxPSNR));
            avgPSNRView.setText(String.format("%.2f", PSNR));
            yPSNRView.setText(String.format("%.2f", yPSNR));
        } else {
            // FAILURE
            Log.e("Error:", String.format("Calculating PSNR failed with state %s and rc %s.%s", ffmpegSession.getState(),
                    ffmpegSession.getReturnCode(), ffmpegSession.getFailStackTrace()));
        }
    }

    @SuppressLint("DefaultLocale")
    private void calculateSSIM(String video) {
        String inputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults/" + video + "_SR.mp4";
        String referencePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults/" + video + "_Ref.mp4";
        FFmpegSession ffmpegSession = FFmpegKit.execute("-i " + inputPath + " -i " + referencePath + " -filter_complex ssim -f null -");
        if (ReturnCode.isSuccess(ffmpegSession.getReturnCode())) {
            // Extract SSIMs
            String out = ffmpegSession.getOutput();
            String SSIMLine = out.substring(out.lastIndexOf("SSIM"));
            String allSSIMLine = SSIMLine.substring(SSIMLine.lastIndexOf("All:"));
            String ySSIMLine = SSIMLine.substring(SSIMLine.lastIndexOf("Y:"), SSIMLine.indexOf("U:"));
            Float allSSIM = Float.valueOf(allSSIMLine.substring(4, allSSIMLine.indexOf("(")));
            Float ySSIM = Float.valueOf(ySSIMLine.substring(2, ySSIMLine.indexOf("(")));
            allSSIMView.setText(String.format("%.4f", allSSIM));
            ySSIMView.setText(String.format("%.4f", ySSIM));
        } else {
            Log.e("Error:", String.format("Calculating SSIM failed with state %s and rc %s.%s", ffmpegSession.getState(),
                    ffmpegSession.getReturnCode(), ffmpegSession.getFailStackTrace()));
        }
    }

    public void deleteRecursive(File path) {
        if (path.isDirectory()) {
            for (File child : path.listFiles()) {
                deleteRecursive(child);
            }
        }
        path.delete();
    }

    private void runSR() {

        new Thread() {
            public void run() {

                initializeDNN();
                for(String video: selectedVideos){
                    if (!srFramesDir.exists()) {
                        if (!srFramesDir.mkdir()) {
                            Log.e ("Error:", "Could not create the SR frame directories");
                        }
                    }
                    // TODO ADD fps here
                    String fps = "24";
                    readFrames(video, fps);
                    long difference = 0;
                    int frame_index = 0;
                    int max_process = videoFramePaths.size();
                    for(String frame : videoFramePaths) {
                        lrImg = BitmapFactory.decodeFile(framesDir + "/" + frame);
                        TensorImage lrImage = prepareInput();
                        TensorImage srImage = prepareOutput();
                        long startTime = System.currentTimeMillis();
                        srModel.run(lrImage.getBuffer(), srImage.getBuffer());
                        difference += (System.currentTimeMillis() - startTime);
                        Bitmap srImg = tensorToBitmap(srImage);
                        String img_name = String.format("srframe_%04d.jpeg", frame_index);
                        frame_index += 1;
                        saveImage(srImg, img_name);
                        srFramePaths.add(srFramesDir + "/" + img_name);

                        // Progress bar
                        int finalFrame_index = frame_index;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                srProgressBar = findViewById(R.id.srProgressBar);
                                srProgressBar.setMax(max_process);
                                srProgressBar.setProgress(finalFrame_index);
                            }
                        });
                    }
                    int numOfFrames = srFramePaths.size();
                    difference /= numOfFrames;
                    totalFramesView.setText(String.valueOf(numOfFrames));
                    executionTimeView.setText(String.format("%d ms", difference));
                    fpsView.setText(String.valueOf(Math.toIntExact(1000 / difference)));
                    // Progress bar update
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultsProgressBar = findViewById(R.id.resultsProgressBar);
                            resultsProgressBar.setMax(10);
                            resultsProgressBar.setProgress(1);
                        }
                    });
                    // Save SR Video
                    saveSrVideo(video, fps);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultsProgressBar = findViewById(R.id.resultsProgressBar);
                            resultsProgressBar.setProgress(4);
                        }
                    });
                    // Delete the frames folder
                    deleteRecursive(framesDir);
                    deleteRecursive(srFramesDir);
                    // Calculate Metrics
                    calculatePSNR(video);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultsProgressBar = findViewById(R.id.resultsProgressBar);
                            resultsProgressBar.setProgress(7);
                        }
                    });
                    calculateSSIM(video);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultsProgressBar = findViewById(R.id.resultsProgressBar);
                            resultsProgressBar.setProgress(10);
                        }
                    });
                    srFramePaths.clear();
                    videoFramePaths.clear();
                    // Indicate its completed
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doneButton = findViewById(R.id.doneButton);
                            doneButton.setBackgroundColor(getColor(R.color.athena_blue));
                        }
                    });
                }
            }
        }.start();
    }

}