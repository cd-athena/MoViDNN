import math
import tensorflow as tf
import numpy as np
import cv2
import time
from div2k import DIV2K


def evaluate(model_file, data_path, image_index=800):
    # div2k = DIV2K(data_path, scale_factor=4, patch_size=0)
    # Get lr, hr image pair
    # lr, hr = div2k[image_index]
    lr = cv2.imread("./0801x4.png", -1).astype(np.float32)
    lr = lr / 255.
    width = lr.shape[0]
    height = lr.shape[1]

    interpreter = tf.lite.Interpreter(model_path=model_file)
    input_details = interpreter.get_input_details()
    input_shape = input_details[0]['shape']
    print("Input Shape {}".format(input_shape))
    print("LR Shape {}".format(lr.shape))
    input_index = interpreter.get_input_details()[0]["index"]
    output_index = interpreter.get_output_details()[0]["index"]
    # interpreter.resize_tensor_input(input_index, (1, width, height, 3))
    interpreter.allocate_tensors()
    lr = np.expand_dims(lr, 0)
    interpreter.set_tensor(input_index, lr)
    t1 = time.time()
    interpreter.invoke()
    print("Execution time: {:.2f} s".format(time.time() - t1))
    sr = interpreter.get_tensor(output_index)
    # hr = hr[:output_shape[1], :output_shape[2]]
    # sr_img = np.clip(np.round(sr * 255.), 0, 255).astype(np.uint8).squeeze()
    sr_img = np.round(sr * 255.).astype(np.uint8).squeeze()
    print("SR Shape {}".format(sr_img.shape))
    cv2.imwrite("./test.png", sr_img)

    return sr_img


evaluate(model_file="./QuantModels/evsrnet_x4.tflite", data_path="./DIV2K/")