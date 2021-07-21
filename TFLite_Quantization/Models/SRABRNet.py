from tensorflow.keras.layers import Conv2D, Input, ReLU, Add
from tensorflow.keras.models import Model
import tensorflow as tf

# TODO Change the residual structure here


def build(scale_factor=4, num_channels=3, input_shape=(270, 540, 3)):
    inp = Input(shape=input_shape)
    x = Conv2D(filters=8, kernel_size=(3, 3), padding='same', kernel_initializer="Orthogonal")(inp)
    x = ReLU()(x)
    res1 = x
    # Downscale
    x = Conv2D(filters=8, kernel_size=(3, 3), padding="same", strides=2, kernel_initializer="Orthogonal")(x)
    x = Conv2D(filters=8, kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    x = Conv2D(filters=32, kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    # Upscale
    x = tf.nn.depth_to_space(x, 2)
    res2 = x
    # First res connection
    x = Add()([x, res1])
    # Upscaled process
    x = Conv2D(filters=8, kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    x = Conv2D(filters=8, kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    # Second res connection
    x = Add()([x, res2])
    x = Conv2D(filters=num_channels * (scale_factor ** 2), kernel_size=(3, 3), padding="same", kernel_initializer="Orthogonal")(x)
    outputs = tf.nn.depth_to_space(x, scale_factor)

    return Model(inp, outputs)

build()