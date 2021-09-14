import argparse
import pathlib
import os
import numpy as np
import tensorflow as tf
from dataset import SRDataset

INPUT_SHAPES = {1: [1, 1080, 1920, 3], 2: [1, 540, 960, 3], 3: [1, 360, 640, 3], 4: [1, 270, 480, 3]}


def quantize(scale, checkpoint, quantized_model_name, lr_root, hr_root):
    def representative_dataset_gen():
        sr_set = SRDataset(lr_root, hr_root)
        batch_x, _ = sr_set[0]
        for x in batch_x:
            x = np.expand_dims(x, 0)
            yield [x]

    # Create the directory to store quantized models
    os.makedirs("./QuantizedModels/", exist_ok=True)
    model = tf.keras.models.load_model(checkpoint)
    # Setup fixed input shape
    concrete_func = model.signatures[tf.saved_model.DEFAULT_SERVING_SIGNATURE_DEF_KEY]
    concrete_func.inputs[0].set_shape(INPUT_SHAPES[scale])
    # Get tf.lite converter instance
    converter = tf.lite.TFLiteConverter.from_concrete_functions([concrete_func])
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    # converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    converter.target_spec.supported_types = [tf.float16]
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]
    converter.representative_dataset = representative_dataset_gen

    quantized_model_name = quantized_model_name + "_x{}.tflite".format(scale)
    tflite_quant_model = converter.convert()

    tflite_models_dir = pathlib.Path("./QuantizedModels/")
    tflite_models_dir.mkdir(exist_ok=True, parents=True)
    tflite_model_file = tflite_models_dir / quantized_model_name
    tflite_model_file.write_bytes(tflite_quant_model)
    print("Quantized Model is Saved!")


parser = argparse.ArgumentParser(description="This script is to quantize models for MoViDNN")
parser.add_argument("-s", "--scale", type=int, required=True, help="Scale of the trained model, use it as 1 for denoising/deblocking models")
parser.add_argument("-c", "--checkpoint", required=True, type=str, help="Path to the saved Keras model checkpoint as the folder")
parser.add_argument("-q", "--quantized_path", type=str, required=True, help="Name of the quantized model")
parser.add_argument("-lr", "--lr_root", type=str, help="Root directory of the distorted images")
parser.add_argument("-hr", "--hr_root", type=str, help="Root directory of the ground truth images")
args = parser.parse_args()

scale = args.scale
checkpoint = args.checkpoint
quantized_model_name = args.quantized_path
lr_root = args.lr_root if args.lr_root is not None else "./DIV2K/X{}/".format(scale)
hr_root = args.hr_root if args.hr_root is not None else "./DIV2K/X1/"

quantize(scale=scale, checkpoint=checkpoint, quantized_model_name=quantized_model_name,
         lr_root=lr_root, hr_root=hr_root)
