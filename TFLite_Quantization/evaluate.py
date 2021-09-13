import math
import tensorflow as tf
import numpy as np
import cv2
import time
from skimage.metrics import structural_similarity as SSIM
from skimage.metrics import peak_signal_noise_ratio as PSNR
from div2k import DIV2K


def evaluate(model_file, scale):
    model = tf.keras.models.load_model(model_file)
    test_set = DIV2K("./DIV2K/", noise_level=50, batch_size=1, patch_size=0, type="train")
    # test_set = DIV2K("./DIV2K/", scale_factor=scale, patch_size=0, type="val")
    PSNRS = []
    SSIMS = []
    BIC_PSNRS = []
    BIC_SSIMS = []
    for i in range(19):
        lr, hr = test_set[i]
        lr = np.expand_dims(lr, 0)
        sr = model.predict(lr).squeeze()
        hr = np.round(hr * 255).astype(np.uint8).squeeze()
        sr_img = np.round(sr * 255.).astype(np.uint8).squeeze()
        bic_img = cv2.resize(np.round(lr * 255).astype(np.uint8).squeeze(), (1920, 1080), interpolation=cv2.INTER_LINEAR)
        psnr = PSNR(hr, sr_img, data_range=255)
        ssim = SSIM(hr, sr_img, data_range=255, multichannel=True)
        bic_psnr = PSNR(hr, bic_img, data_range=255)
        bic_ssim = SSIM(hr, bic_img, data_range=255, multichannel=True)
        PSNRS.append(psnr)
        SSIMS.append(ssim)
        BIC_PSNRS.append(bic_psnr)
        BIC_SSIMS.append(bic_ssim)

    print("Avg PSNR for SR: {}".format(sum(PSNRS) / len(PSNRS)))
    print("Avg SSIM for SR: {}".format(sum(SSIMS) / len(SSIMS)))
    print("Avg PSNR for BICUBIC: {}".format(sum(BIC_PSNRS) / len(PSNRS)))
    print("Avg SSIM for BICUBIC: {}".format(sum(BIC_SSIMS) / len(PSNRS)))

    return PSNRS, SSIMS, BIC_PSNRS, BIC_SSIMS

# PSNRS, SSIMS, BIC_PSNRS, BIC_SSIMS = evaluate("./Checkpoints/SRNetMod_x2", 2)
# PSNRS, SSIMS, BIC_PSNRS, BIC_SSIMS = evaluate("./Checkpoints/SRNetMod_x3", 3)
# PSNRS, SSIMS, BIC_PSNRS, BIC_SSIMS = evaluate("./Checkpoints/SRABRNetNonVal_x2", 2)


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


def quantized_sr(model_file, input_image, output_name):
    lr = cv2.imread(input_image, -1).astype(np.float32)
    lr = lr / 255.

    interpreter = tf.lite.Interpreter(model_path=model_file)
    input_details = interpreter.get_input_details()
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
    cv2.imwrite(output_name, sr_img)

    return sr_img


# normal_sr(model_file="./Checkpoints/DnCNN_25", input_image="./NoisyTest/DIV2K_gaussian25_0136x1.png",
#           output_name="./dncn_25_0136.png")
quantized_sr(model_file="./QuantModels/srabrnet_x2.tflite", input_image="./DIV2K/DIV2K_train_LR_bicubic/X2/0791x2.png", output_name="./srabrnet_x2.png")
quantized_sr(model_file="./QuantModels/srabrnet_x3.tflite", input_image="./DIV2K/DIV2K_train_LR_bicubic/X3/0791x3.png", output_name="./srabrnet_x3.png")
quantized_sr(model_file="./QuantModels/srabrnet_x4.tflite", input_image="./DIV2K/DIV2K_train_LR_bicubic/X4/0791x4.png", output_name="./srabrnet_x4.png")
# normal_sr(model_file="./Checkpoints/SRNetMod_x2", input_image="./DIV2K/DIV2K_train_LR_bicubic/X2/0791x2.png", output_name="./srnet_x2.png")
# normal_sr(model_file="./Checkpoints/SRNet_x3", input_image="./DIV2K/DIV2K_train_LR_bicubic/X3/0791x3.png", output_name="./srnet_x3.png")
# normal_sr(model_file="./Checkpoints/SRNet_x4", input_image="./DIV2K/DIV2K_train_LR_bicubic/X4/0791x4.png", output_name="./srnet_x4.png")
"""
calculate_PSNR("./srnet_x2.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")
calculate_PSNR("./srnet_x3.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")
calculate_PSNR("./srnet_x4.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")

calculate_PSNR("./0791_BIL_x2.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")
calculate_PSNR("./0791_BIL_x3.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")
calculate_PSNR("./0791_BIL_x4.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")

calculate_PSNR("./0791_BI_x2.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")
calculate_PSNR("./0791_BI_x3.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")
calculate_PSNR("./0791_BI_x4.png", "./DIV2K/DIV2K_train_LR_bicubic/X1/0791x1.png")
"""