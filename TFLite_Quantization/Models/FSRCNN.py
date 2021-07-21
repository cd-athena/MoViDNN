from tensorflow.keras.layers import Conv2D, Conv2DTranspose, Input, ReLU
from tensorflow.keras.models import Model


def build(scale_factor=3, input_shape=(270, 480, 3), num_channels=3):
    """Implements FSRCNN in Keras
    http://mmlab.ie.cuhk.edu.hk/projects/FSRCNN.html
    """
    # Input shape = (H, W, Channels)
    inp = Input(shape=input_shape)
    # Feature extraction
    x = Conv2D(filters=56, kernel_size=(5, 5), padding='same', kernel_initializer="Orthogonal")(inp)
    x = ReLU()(x)
    # Shrinking
    x = Conv2D(filters=16, kernel_size=(5, 5), padding='same', kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    # Mapping
    x = Conv2D(filters=12, kernel_size=(3, 3), padding='same', kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    x = Conv2D(filters=12, kernel_size=(3, 3), padding='same', kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    x = Conv2D(filters=12, kernel_size=(3, 3), padding='same', kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    x = Conv2D(filters=12, kernel_size=(3, 3), padding='same', kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    # Expanding
    x = Conv2D(filters=56, kernel_size=(3, 3), padding='same', kernel_initializer="Orthogonal")(x)
    x = ReLU()(x)
    # Deconvolution
    out = Conv2DTranspose(num_channels, kernel_size=(9, 9), strides=(scale_factor, scale_factor), padding='same')(x)

    return Model(inputs=inp, outputs=out)


