import cv2
import numpy as np
import tensorflow as tf
from glob import glob
from sklearn.utils import shuffle


class SRDataset(tf.keras.utils.Sequence):
    def __init__(self, lr_root, hr_root, batch_size=32):
        self.batch_size = batch_size
        lr_images = sorted(glob(lr_root + "*.png"))
        hr_images = sorted(glob(hr_root + "*.png"))
        # Shuffle
        self.lr_images, self.hr_images = shuffle(np.array(lr_images), np.array(hr_images))

    def __getitem__(self, idx):
        return self._get_image_pair(idx)

    def __len__(self):
        length = int(np.ceil(len(self.lr_images) / self.batch_size))
        return length

    def _get_image_pair(self, idx):
        hr_file = self.hr_images[idx]
        hr = self.get_image(hr_file).astype(np.float32)
        hr = hr / 255.
        lr_file = self.lr_images[idx]
        lr = self.get_image(lr_file).astype(np.float32)
        lr = lr / 255.

        lr = np.expand_dims(lr, 0)
        hr = np.expand_dims(hr, 0)

        lr = tf.convert_to_tensor(lr, dtype=tf.float32)
        hr = tf.convert_to_tensor(hr, dtype=tf.float32)

        return lr, hr

    def get_image(self, image_path):
        im = cv2.imread(image_path, -1)
        return im


def representative_dataset_gen(lr_root, hr_root):
    sr_set = SRDataset(lr_root, hr_root)
    batch_x, _ = sr_set[0]
    for x in batch_x:
        x = np.expand_dims(x, 0)
        yield [x]