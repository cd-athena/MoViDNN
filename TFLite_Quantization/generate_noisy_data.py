import os
from skimage.util import random_noise, img_as_ubyte
from skimage import io
from skimage.color import rgb2gray


def crop_image(image, size):
    return image[0:size, 0:size]


def to_gray(image):
    return rgb2gray(image)


def read_img(image, train=False, gray=False):
    img = io.imread(image)
    img = crop_image(img, 256) if train else img
    img = to_gray(img) if gray else img

    return img


def add_gaussian_noise(image, noise_level):
    return random_noise(image, mode="gaussian", seed=None, clip=True, var=(noise_level / 255)**2)


def add_poisson_noise(image):
    return random_noise(image, mode="poisson", seed=None, clip=True)


def prepare_folder(output_folder, set_name, folder_name):
    new_path = os.path.join(output_folder, set_name, folder_name)
    os.makedirs(new_path, exist_ok=True)

    return new_path


def generate_gaussian_folder(set_name, data_folder, output_folder, noise_levels, train, gray):
    for nl in noise_levels:
        save_path = prepare_folder(output_folder, set_name, "Gaussian" + str(nl))
        for img_file in os.listdir(data_folder):
            img = read_img(os.path.join(data_folder, img_file), train, gray)
            noisy_img = img_as_ubyte(add_gaussian_noise(img, nl))
            img_name = set_name + "_gaussian" + str(nl) + "_" + img_file[:-4] + ".png"
            io.imsave(os.path.join(save_path, img_name), noisy_img)


def generate_ground_truth(set_name, data_folder, output_folder, train, gray):
    save_path = prepare_folder(output_folder, set_name, "GroundTruths")
    for img_file in os.listdir(data_folder):
        img = read_img(os.path.join(data_folder, img_file), train, gray)
        io.imsave(os.path.join(save_path, set_name + "_gt_" + img_file[:-4] + ".png"), img)


def extract_csv_info(filename):
    dataset, noise, index = filename.split("_")
    return dataset, noise, index[:-4]


def generate_noisy_set(data_folder, output_folder, train=False, gray=False):
    set_name = data_folder.split("/")[1] if not gray else "G" + data_folder.split("/")[1]
    generate_gaussian_folder(set_name, data_folder, output_folder, [25, 50], train, gray)
    generate_ground_truth(set_name, data_folder, output_folder, train, gray)


# Generate training set
generate_noisy_set(data_folder="./DIV2K/DIV2K_train_LR_bicubic/X1/", output_folder="./", train=True)