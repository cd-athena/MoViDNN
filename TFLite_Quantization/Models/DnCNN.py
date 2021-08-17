from tensorflow.keras.layers import Conv2D, Input, ReLU, BatchNormalization, Subtract
from tensorflow.keras.models import Model


def build(input_shape=(270, 540, 3)):
    inp = Input(shape=input_shape)
    # First Layer
    x = Conv2D(filters=64, kernel_size=(3, 3), strides=(1, 1), padding='same')(inp)
    x = ReLU()(x)
    for _ in range(15):
        # Add 15 layers in the middle
        x = Conv2D(filters=64, kernel_size=(3, 3), strides=(1, 1), padding="same")(x)
        x = BatchNormalization(axis=-1, epsilon=1e-3)(x)
        x = ReLU()(x)
    # Output
    x = Conv2D(filters=3, kernel_size=(3, 3), strides=(1, 1), padding="same")(x)
    x = Subtract()([inp - x])
    output = ReLU(max_value=1)(x)

    return Model(inp, output)
