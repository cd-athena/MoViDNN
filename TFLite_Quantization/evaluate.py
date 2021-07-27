import math
import tensorflow as tf
import numpy as np
import cv2
import time
from skimage.metrics import structural_similarity as SSIM
from skimage.metrics import peak_signal_noise_ratio as PSNR
from div2k import DIV2K


def evaluate(model_file, test_path):
    # TODO Calculate PSNR and SSIM here
    pass


def calculate_PSNR(input_path, gt_path):
    input = cv2.imread(input_path, -1).squeeze()
    gt = cv2.imread(gt_path, -1)
    psnr = PSNR(gt, input, data_range=255)
    ssim = SSIM(gt, input, data_range=255, multichannel=True)
    print(psnr)
    print(ssim)


def normal_sr(model_file, input_image, output_name):
    lr = cv2.imread(input_image, -1).astype(np.float32)
    lr = lr / 255.
    lr = np.expand_dims(lr, 0)
    model = tf.keras.models.load_model(model_file)
    sr = model.predict(lr).squeeze()
    sr_img = np.round(sr * 255.).astype(np.uint8).squeeze()
    cv2.imwrite(output_name, sr_img)


def quantized_sr(model_file, input_image):
    lr = cv2.imread(input_image, -1).astype(np.float32)
    lr = lr / 255.

    interpreter = tf.lite.Interpreter(model_path=model_file)
    input_details = interpreter.get_input_details()
    input_shape = input_details[0]['shape']
    input_index = interpreter.get_input_details()[0]["index"]
    output_index = interpreter.get_output_details()[0]["index"]
    # width = lr.shape[0]
    # height = lr.shape[1]
    # interpreter.resize_tensor_input(input_index, (1, width, height, 3))
    interpreter.allocate_tensors()
    lr = np.expand_dims(lr, 0)
    interpreter.set_tensor(input_index, lr)
    t1 = time.time()
    interpreter.invoke()
    print("Execution time: {:.2f} s".format(time.time() - t1))
    sr = interpreter.get_tensor(output_index)
    sr_img = np.round(sr * 255.).astype(np.uint8).squeeze()
    cv2.imwrite("./quant_new_x4.png", sr_img)

    return sr_img

# quantized_sr(model_file="./QuantModels/srabrnet_x4.tflite", input_image="./DIV2K/DIV2K_train_LR_bicubic/X4/0001x4.png")
# normal_sr(model_file="./Checkpoints/SRNet_x2", input_image="./DIV2K/DIV2K_train_LR_bicubic/X2/0001x2.png",
#           output_name="./srnet_x2.png")
calculate_PSNR("./srnet_x2.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0001x1.png")
calculate_PSNR("./srnet_x3.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0001x1.png")
calculate_PSNR("./srnet_x4.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0001x1.png")