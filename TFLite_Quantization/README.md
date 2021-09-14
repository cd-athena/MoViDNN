# MoViDNN DNN Quantization

We provide the necessary codes to convert your existing Tensorflow/Keras DNN models to *TensorflowLite* versions in this repository. 

# Required Packages

```
cv2
tensorflow
numpy
scikit-learn
```



## Training Own Model

### Preparing the Model

Before training your own model, you need to implement it in **Keras** and put it into the **Models** folder. We provide three models inside, you can check their structure and prepare your model accordingly.

```
MoViDNN_Quantization
│   README.md
│   file001.txt    
│
└───Models
│   │   DNCNN.py
│   │   ESPCN.py
│   │
│   │  <YourModel>.py
```

### Preparing the Data

Once your model is in the correct structure and folder, next step is to prepare the training dataset. We provide the code to load the distorted and ground truth images as a **Keras Dataset**. 

You can store your **Ground Truth** images in the  `./Dataset/GT/ ` folder and your **distorted images** in the `./Dataset/LR/` folder. It is recommended to include the *downscaling* factor in the **distorted images** folder name if the dataset is prepared for Super-resolution. Moreover, the **file names** of the **ground truth** and the **distorted** image pairs should be **same** in their folders. An example **DIV2K**  structure used for **super-resolution** training is given below. 

```
MoViDNN_Quantization
│   README.md
│   file001.txt    
│
└───Models
│   │   DNCNN.py
│   │   ESPCN.py
│   │
│   │  <YourModel>.py
└───DIV2K
│   └───X1
│   │   │	0001x1.png
│   │   │	0002x1.png
│   └───X2
│   │   │	0001x2.png
│   │   │	0002x2.png
│   └───X3
│   │   │	0001x3.png
│   │   │	0002x3.png
```

You can initialize a dataset to train **x2** super-resolution network, using the above dataset structure as following:

```python
train_gen = SRDataset(lr_root="./DIV2K/X2/", hr_root="./DIV2K/X1/", batch_size=32)
```

<CONTINUE HERE>

## Quantization

To quantize a saved checkpoint into a *tensorflow lite* version, you can use the `quantize.py` file. An example call looks like this:

```bash
python3 quantize.py -s 2 -c ./Checkpoints/ESPCN_x2 -q espcn 
```

This will read the **ESPCN_x2** checkpoint file, quantize it with **float 16**, convert it to *tflite* model, and save it in the `./QuantizedModels/espcn_x2.tflite` path.

Note that by default this script will read data from included **DIV2K** dataset to fine tune the quantization. If you want to specify a new data path for your **distorted** and **ground truth** images you can pass them as an optional argument.

```bash
python3 quantize.py -s 2 -c ./Checkpoints/ESPCN_x2 -q espcn -lr <path_to_distorted_images> -hr <path_to_ground_truth_images>
```

Moreover, by default, we use the following resolutions for the scale factors:

```python
INPUT_SHAPES = {1: [1, 1080, 1920, 3], 2: [1, 540, 960, 3], 3: [1, 360, 640, 3], 4: [1, 270, 480, 3]}
```

If **resolutions** in your dataset is different, please modify this dictionary.