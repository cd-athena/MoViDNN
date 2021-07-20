package com.athena.mobiledemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class DNNActivity extends AppCompatActivity {

    Interpreter srModel;
    String modelName;
    Spinner networkSpinner;
    Button initButton;
    Button startButton;
    RadioGroup acceleratorPicker;
    RadioButton selectedAccelerator;
    Interpreter.Options options;
    GpuDelegate gpuDelegate;
    NnApiDelegate nnApiDelegate;
    CompatibilityList compatList;
    Bitmap lrImg;
    String[] availableModels;
    Boolean isDNNReady = false; // To check if the DNN is loaded before running SR
    ImageView display;
    TextView executionTimeView;

    final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileDemo/DNNResults");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnn);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        networkSpinner = findViewById(R.id.nnListSpinner);
        initButton = findViewById(R.id.dnnInitButton);
        startButton = findViewById(R.id.dnnStartButton);
        acceleratorPicker = findViewById(R.id.acceleratorPicker);
        display = findViewById(R.id.srImage);
        executionTimeView = findViewById(R.id.executionTimeView);
        compatList = new CompatibilityList();

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Log.e ("ALERT", "Could not create the directories");
            }
        }

        fillSpinner();
        setOnClicks();
    }

    public void fillSpinner() {
        try {
            availableModels = getAssets().list("models/");
            for (int i =0; i < availableModels.length; i++) {
                availableModels[i] = availableModels[i].replace(".tflite", "");
            }
        } catch (IOException e) {
            Log.e("EKREM:", "Error while reading list of models");
        }
        ArrayAdapter<String> nnSpinnerArray = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availableModels);
        nnSpinnerArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networkSpinner.setAdapter(nnSpinnerArray);
    }

    private void setOnClicks() {
        initButton.setOnClickListener(this::initializeDNN);
        startButton.setOnClickListener(this::runSR);
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
            srModel = new Interpreter(loadModelFile(modelName + ".tflite"), options);
            Log.e("EKREM", "Model Initalized with GPU support: " + srModel.getInputTensor(0).name());
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
            srModel = new Interpreter(loadModelFile(modelName + ".tflite"), options);
            Log.e("EKREM", "Model Initalized with NNAPI support: " + srModel.getInputTensor(0).name());
        } catch (IOException e) {
            Log.e("EKREM:" ,"Error while initializing model with NNAPI: " + e);
        }
    }

    private void initializeDNNwithCPU() {
        closeAllDelagates();
        try {
            srModel = new Interpreter(loadModelFile(modelName + ".tflite"));
            Log.e("EKREM", "Model Initalized: " + srModel.getInputTensor(0).name());
        } catch (IOException e) {
            Log.e("EKREM:" ,"Error while initializing model: " + e);
        }
    }

    private String getSelectedAccelerator() {
        int selectedRadioButtonId = acceleratorPicker.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "Please select an accelerator", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            selectedAccelerator = findViewById(selectedRadioButtonId);
            return selectedAccelerator.getText().toString();
        }
    }

    private String getSelectedModelName() {
        return networkSpinner.getSelectedItem().toString();
    }

    private void initializeDNN(View view) {
        // Check the options here and run the corresponding function
        String accelerator = getSelectedAccelerator();
        modelName = getSelectedModelName();
        if (accelerator != null) {
            Log.e("Ekrem:", "Accelerator Selected: " + accelerator);
            switch (accelerator) {
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
            isDNNReady = true;
            // Setup the display in the activity
            try {
                lrImg = BitmapFactory.decodeStream(getAssets().open("frames/0002x4.png"));
                display.setImageBitmap(lrImg);
            } catch (IOException e) {
                Log.e("EKREM:", "Error while loading input image: " + e);
            }
        }
    }

    private void saveImage(Bitmap bmp, String filename) throws IOException {
        // Assume block needs to be inside a Try/Catch block.
        OutputStream fOut = null;
        File file = new File(dir, filename + ".png"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
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

    private TensorImage prepareInput() {
        TensorImage lrImage = TensorImage.fromBitmap(lrImg);
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(270, 480, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
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
    private void runSR(View view) {
        if (isDNNReady) {
            TensorImage lrImage = prepareInput();
            TensorImage srImage = prepareOutput();
            long startTime = System.currentTimeMillis();
            srModel.run(lrImage.getBuffer(), srImage.getBuffer());
            long difference = System.currentTimeMillis() - startTime;
            executionTimeView.setText("SR Execution Time: " + difference + "ms - " + 1000/difference + "fps");
            saveSrImage(srImage);
        }
        else {
            Toast.makeText(this, "Please load a model first!", Toast.LENGTH_SHORT).show();
        }
    }
}