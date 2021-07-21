from PIL import Image
import os

LR_DIR = "./DIV2K/DIV2K_train_LR_bicubic/"
HR_DIR = "./DIV2K/DIV2K_train_HR/"


def resize_div2k(scale):
    images = [file for file in os.listdir(HR_DIR) if file.endswith(('png'))]
    lr_dir = LR_DIR + "X{}/".format(scale)
    os.makedirs(lr_dir, exist_ok=True)
    for image in images:
        img = Image.open(HR_DIR + image)
        res_im = img.resize((1920 // scale, 1080 // scale), Image.BICUBIC)
        res_im.save(lr_dir + image[:-4] + "x{}.png".format(scale))

resize_div2k(4)
