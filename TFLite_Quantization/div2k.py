import h5py
import numpy as np
import tensorflow as tf
import cv2


class DIV2K(tf.keras.utils.Sequence):
    def __init__(self, data_root, scale_factor=2, batch_size=32, patch_size=48, type='train'):
        self.data_root = data_root
        self.scale_factor = scale_factor
        self.batch_size = batch_size
        self.image_ids = range(1, 802)
        self.patch_size = patch_size
        self.type = 'train'

    def __getitem__(self, idx):
        if self.patch_size > 0:
            start = idx * self.batch_size
            end = (idx + 1) * self.batch_size
            batch_ids = self.image_ids[start:end]
            lr_batch = np.zeros((self.batch_size, self.patch_size // self.scale_factor, self.patch_size // self.scale_factor, 3), dtype='float32')
            hr_batch = np.zeros((self.batch_size, self.patch_size, self.patch_size, 3), dtype='float32')
            for i, id in enumerate(batch_ids):
                lr, hr = self._get_image_pair(id)
                lr = np.expand_dims(lr, 0)
                hr = np.expand_dims(hr, 0)
                lr_batch[i] = lr
                hr_batch[i] = hr
            return lr_batch, hr_batch
        else:
            # Return 1 image pair if not returning an image patch
            return self._get_image_pair(self.image_ids[idx])

    def __len__(self):
        if self.patch_size > 0:
            length = int(np.ceil(len(self.image_ids) / self.batch_size))
        else:
            length = 1
        return length

    def _get_image_pair(self, id):
        image_id = f'{id:04}'
        hr_file = f'{self.data_root}/DIV2K_{self.type}_HR/{image_id}.png'
        hr = self.get_image(hr_file).astype(np.float32)
        if self.patch_size > 0:
            hr = self._crop_center(hr, self.patch_size, self.patch_size)
        hr = hr / 255.
        lr_file = f'{self.data_root}/DIV2K_{self.type}_LR_bicubic/X{self.scale_factor}/{image_id}x{self.scale_factor}.png'
        lr = self.get_image(lr_file).astype(np.float32)
        if self.patch_size > 0:
            lr = self._crop_center(lr, self.patch_size // self.scale_factor, self.patch_size // self.scale_factor)
        lr = lr / 255.
        return lr, hr

    def get_image(self, image_path):
        im = cv2.imread(image_path, -1)
        return im

    def _crop_center(self, im, crop_h, crop_w):
        startx = im.shape[1] // 2 - (crop_w // 2)
        starty = im.shape[0] // 2 - (crop_h // 2)
        return im[starty:starty + crop_h, startx:startx + crop_w]


def representative_dataset_gen():
    div2k = DIV2K('datasets/DIV2K')
    batch_x, _ = div2k[0]
    for x in batch_x:
        x = np.expand_dims(x, 0)
        yield [x]
