from tensorflow.keras.layers import Conv2D, Input, ReLU, Add
from tensorflow.keras.models import Model
import tensorflow as tf


def build(scale_factor=2, num_channels=3, input_shape=(270, 540, 3)):
    inp = Input(shape=input_shape)
    # First Layer, Important, save it
    x = Conv2D(filters=16, kernel_size=(3, 3), padding='same', kernel_initializer="Orthogonal")(inp)
    x = ReLU()(x)
    res_init = x
    # Processing and residual connections
    dx = Conv2D(filters=16, kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(x)
    dx = ReLU()(dx)
    # Add first layer to second
    dx_2 = Add()([dx, res_init])
    dx_2 = Conv2D(filters=16, kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(dx_2)
    dx_2 = ReLU()(dx_2)
    # Add first two layers to third
    dx_3 = Add()([dx_2, dx, res_init])
    dx_3 = Conv2D(filters=16, kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(dx_3)
    dx_3 = ReLU(max_value=1)(dx_3)
    # Upscale
    dx_4 = Add()([dx_3, dx_2, dx, res_init])
    up_x = Conv2D(filters=num_channels * (scale_factor ** 2), kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(dx_4)
    up_x = ReLU(max_value=1)(up_x)
    # Output
    outputs = tf.nn.depth_to_space(up_x, scale_factor)

    return Model(inp, outputs)