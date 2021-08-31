# SRABRNET_X2 Usage

```
import com.example.tflitesr.srabrnet_x2.SRABRNET_X2;

// 1. Initialize the Model
SRABRNET_X2 model = null;

try {
    model = SRABRNET_X2.newInstance(context);  // android.content.Context
} catch (IOException e) {
    e.printStackTrace();
}

if (model != null) {

    // 2. Set the inputs
    // Prepare tensor "lrImage" from a Bitmap with ARGB_8888 format.
    Bitmap bitmap = ...;
    TensorImage lrImage = TensorImage.fromBitmap(bitmap);
    // Alternatively, load the input tensor "lrImage" from pixel values.
    // Check out TensorImage documentation to load other image data structures.
    // int[] pixelValues = ...;
    // int[] shape = ...;
    // TensorImage lrImage = new TensorImage();
    // lrImage.load(pixelValues, shape);

    // 3. Run the model
    SRABRNET_X2.Outputs outputs = model.process(lrImage);

    // 4. Retrieve the results
    TensorImage srImage = outputs.getSrImageAsTensorImage();
}
```