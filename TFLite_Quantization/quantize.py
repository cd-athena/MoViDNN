from div2k import DIV2K
import tensorflow as tf
import numpy as np
import pathlib


SAVED_MODEL = "./Checkpoints/SRNet_x4"
MODEL_NAME = "srnet_x4.tflite"
QUANT_MODEL_PATH = "./QuantModels/" + MODEL_NAME
DATA_PATH = "./DIV2K/"
SCALE = 4
INPUT_SHAPE = [1, 270, 480, 3]
#SCALE = 3
#INPUT_SHAPE = [1, 360, 640, 3]
# SCALE = 2
# INPUT_SHAPE = [1, 540, 960, 3]


def simple_quantize(scale_factor, model_name):
    def representative_dataset_gen():
        div2k = DIV2K(DATA_PATH, scale_factor=scale_factor, patch_size=0)
        for i in range(5):
            x, _ = div2k[i]
            x = np.expand_dims(x, 0)
            yield [x]

    model = tf.keras.models.load_model(SAVED_MODEL)
    # model = tf.saved_model.load(saved_model_path)
    # Setup fixed input shape
    concrete_func = model.signatures[tf.saved_model.DEFAULT_SERVING_SIGNATURE_DEF_KEY]
    concrete_func.inputs[0].set_shape(INPUT_SHAPE)
    # Get tf.lite converter instance
    converter = tf.lite.TFLiteConverter.from_concrete_functions([concrete_func])
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    converter.representative_dataset = representative_dataset_gen

    tflite_quant_model = converter.convert()

    tflite_models_dir = pathlib.Path("./QuantModels/")
    tflite_models_dir.mkdir(exist_ok=True, parents=True)
    tflite_model_file = tflite_models_dir / model_name
    tflite_model_file.write_bytes(tflite_quant_model)
    print("Quantized Model is Saved!")


simple_quantize(scale_factor=SCALE, model_name=MODEL_NAME)
