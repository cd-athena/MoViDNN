# MoViDNN: A Mobile Platform for Evaluating Video Quality Enhancement with Deep Neural Networks

MoViDNN is an Android application that can be used to evaluate DNN based video quality enhancements for mobile devices. We provide the structure to evaluate both super-resolution, and denoising/deblocking DNNs in this application. However, the structure can be extended easily to adapt to additional approaches such as video frame interpolation.

Moreover, MoViDNN can also be used as a **Subjective** test environment to evaulate DNN based enhancements. 

We use [*tensorflow-lite*](https://www.tensorflow.org/lite) as the DNN framework and [*FFMPEG*](https://github.com/tanersener/ffmpeg-kit) for the video processing. 

We also provide a Python repository that can be used to convert existing Tensorflow/Keras models to **tensorflow-lite** versions for Android. [Preparation](https://github.com/cd-athena/MoViDNN/tree/main/TFLite_Quantization)

## DNN Evaluation

### Preparing the Folder Structure



```
MoViDNN
│   README.md
│   file001.txt    
│
└───Models
│   │   DNCNN.py
│   │   ESPCN.py
│   │
│   │  <YourModel>.py
```

