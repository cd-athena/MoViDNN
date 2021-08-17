from div2k import DIV2K
import tensorflow as tf
import numpy as np
import pathlib


DATA_PATH = "./DIV2K/"
INPUT_SHAPES = {1: [1, 1080, 1920, 3], 2: [1, 540, 960, 3], 3: [1, 360, 640, 3], 4: [1, 270, 480, 3]}


def quantize(scale_factor, saved_model_name, quant_model_name):
    def representative_dataset_gen():
        div2k = DIV2K(DATA_PATH, scale_factor=scale_factor, patch_size=0)
        for i in range(5):
            x, _ = div2k[i]
            x = np.expand_dims(x, 0)
            yield [x]

    model = tf.keras.models.load_model("./Checkpoints/" + saved_model_name)
    # Setup fixed input shape
    concrete_func = model.signatures[tf.saved_model.DEFAULT_SERVING_SIGNATURE_DEF_KEY]
    concrete_func.inputs[0].set_shape(INPUT_SHAPES[scale_factor])
    # Get tf.lite converter instance
    converter = tf.lite.TFLiteConverter.from_concrete_functions([concrete_func])
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    # converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    converter.target_spec.supported_types = [tf.float16]
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]
    converter.representative_dataset = representative_dataset_gen

    tflite_quant_model = converter.convert()

    tflite_models_dir = pathlib.Path("./QuantModels/")
    tflite_models_dir.mkdir(exist_ok=True, parents=True)
    tflite_model_file = tflite_models_dir / quant_model_name
    tflite_model_file.write_bytes(tflite_quant_model)
    print("Quantized Model is Saved!")


quantize(scale_factor=1, saved_model_name="BMCNN_25", quant_model_name="bmcnn_x1.tflite")
