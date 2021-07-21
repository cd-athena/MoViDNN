from tensorflow.keras.layers import Conv2D, Conv2DTranspose, Input, ReLU, Add
from tensorflow.keras.models import Model
import tensorflow as tf


def residual_block(x):
    fx = Conv2D(filters=8, kernel_size=(3, 3), activation="relu", padding='same')(x)
    fx = Conv2D(filters=8, kernel_size=(3, 3), padding='same')(fx)
    out = Add()([x, fx])
    out = ReLU()(out)

    return out


def build(scale_factor=3, input_shape=(270, 480, 3), num_channels=3):
    """
    Implements EVSRNet in Keras
    @InProceedings{Liu_2021_CVPR,
        author    = {Liu, Shaoli and Zheng, Chengjian and Lu, Kaidi and Gao, Si and Wang, Ning and Wang, Bofei and Zhang, Diankai and Zhang, Xiaofeng and Xu, Tianyu},
        title     = {EVSRNet: Efficient Video Super-Resolution With Neural Architecture Search},
        booktitle = {Proceedings of the IEEE/CVF Conference on Computer Vision and Pattern Recognition (CVPR) Workshops},
        month     = {June},
        year      = {2021},
        pages     = {2480-2485}
    }
    """
    inp = Input(shape=input_shape)
    x = Conv2D(filters=8, kernel_size=(3, 3), activation="relu", padding='same')(inp)
    # Residual blocks
    x = residual_block(x)
    x = residual_block(x)
    x = residual_block(x)
    x = residual_block(x)
    x = residual_block(x)
    x = Conv2D(filters=num_channels * (scale_factor ** 2), kernel_size=(3, 3), padding="same")(x)
    # Clipped ReLU to fix the output
    x = ReLU(max_value=1)(x)
    # Upscaling
    out = tf.nn.depth_to_space(x, scale_factor)

    return Model(inputs=inp, outputs=out)


