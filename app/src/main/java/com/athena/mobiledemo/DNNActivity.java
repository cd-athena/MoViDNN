package com.athena.mobiledemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
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
    Spinner networkSpinner;
    Button initButton;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnn);
        networkSpinner = (Spinner) findViewById(R.id.nnListSpinner);
        initButton = (Button) findViewById(R.id.dnnInitButton);
        startButton = (Button) findViewById(R.id.dnnStartButton);
        fillSpinner();
        setOnClicks();
    }

    public void fillSpinner() {
        String[] networkList = new String[]{"FSRCNN", "ESPCN", "SRABRNet"};
        ArrayAdapter<String> nnSpinnerArray = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, networkList);
        nnSpinnerArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networkSpinner.setAdapter(nnSpinnerArray);
    }

    private void setOnClicks() {
        initButton.setOnClickListener(this::initializeDNN);
        startButton.setOnClickListener(this::runSR);
    }

    private MappedByteBuffer loadModelFile(String modelName) throws IOException {
        AssetFileDescriptor modelFileDescriptor = this.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(modelFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = modelFileDescriptor.getStartOffset();
        long declaredLength = modelFileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void initializeDNN(View view) {
        // Read options here and intiialize the model
        try {
            srModel = new Interpreter(loadModelFile("srabrnet_x4.tflite"));
            Log.e("EKREM", "Model Initalized: " + srModel.getInputTensor(0).name());
        } catch (IOException e) {
            Log.e("EKREM:" ,"Error while initializing model: " + e);
        }
    }

    private TensorImage prepareInput(Bitmap img) {
        TensorImage lrImg = TensorImage.fromBitmap(img);

        return lrImg;
    }

    private void saveImage(Bitmap bmp, String filename) throws IOException {
        // Assume block needs to be inside a Try/Catch block.
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        File file = new File(path, filename + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        fOut = new FileOutputStream(file);

        bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
        fOut.flush(); // Not really required
        fOut.close(); // do not forget to close the stream

        MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
    }

    private Bitmap tensorToBitmap(TensorImage tensorOutput) {
        // Get the output and convert it to Bitmap
        ByteBuffer SROut = tensorOutput.getBuffer();
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

    private void runSR(View view) {

    }
}