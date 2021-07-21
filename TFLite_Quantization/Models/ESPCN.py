from tensorflow.keras.layers import Conv2D, Input
from tensorflow.keras.models import Model
import tensorflow as tf


def build(scale_factor=4, num_channels=3, input_shape=(270, 540, 3)):
    conv_args = {
        "activation": "relu",
        "kernel_initializer": "Orthogonal",
        "padding": "same",
    }
    inputs = Input(shape=input_shape)
    x = Conv2D(64, 5, **conv_args)(inputs)
    x = Conv2D(64, 3, **conv_args)(x)
    x = Conv2D(32, 3, **conv_args)(x)
    x = Conv2D(num_channels * (scale_factor ** 2), 3, **conv_args)(x)
    outputs = tf.nn.depth_to_space(x, scale_factor)

    return Model(inputs, outputs)
