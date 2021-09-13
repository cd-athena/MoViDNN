from tensorflow.keras.layers import Conv2D, Input, ReLU, BatchNormalization, Subtract
from tensorflow.keras.models import Model

def bmcnn(input_shape=(None, None, 3)):
    inp = Input(shape=input_shape)
    # First Layer
    x = Conv2D(filters=64, kernel_size=(3, 3), dilation_rate=(1, 1), padding='same')(inp)
    x = ReLU()(x)
    x = Conv2D(filters=24, kernel_size=(3, 3), dilation_rate=(2, 2), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = Conv2D(filters=24, kernel_size=(3, 3), dilation_rate=(2, 2), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = Conv2D(filters=24, kernel_size=(3, 3), dilation_rate=(2, 2), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = Conv2D(filters=24, kernel_size=(3, 3), dilation_rate=(2, 2), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU()(x)
    x = Conv2D(filters=24, kernel_size=(3, 3), dilation_rate=(2, 2), padding='same')(x)
    x = BatchNormalization()(x)
    x = ReLU(max_value=1)(x)
    x = Conv2D(3, kernel_size=(3, 3), padding='same', dilation_rate = (1,1))(x)
    output = ReLU(max_value=1)(x)

    return Model(inp, output)