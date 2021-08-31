from Models import FSRCNN, ESPCN, SRABRNet, EVSRNet, SRNet, DnCNN
from div2k import DIV2K
import tensorflow as tf

DATA_PATH = "./DIV2K/"
SAVED_MODEL_PATH = "./Checkpoints/DnCNN_25"
SCALE = 4
NOISE_LEVEL = 25


def train_sr(model, scale_factor=SCALE, num_epochs=2, batch_size=4):
    train_gen = DIV2K(DATA_PATH, scale_factor=scale_factor, batch_size=batch_size, type="train")
    # valid_gen = DIV2K(DATA_PATH, scale_factor=scale_factor, batch_size=batch_size, type="val")
    adam = tf.keras.optimizers.Adam(learning_rate=2e-4)
    model.compile(optimizer=adam, loss='mse')
    # reduce_lr = tf.keras.callbacks.ReduceLROnPlateau(monitor='val_loss', factor=0.1, patience=10, min_lr=2e-6)
    model.fit(train_gen, epochs=num_epochs, workers=8, verbose=2)
    model.save(SAVED_MODEL_PATH, overwrite=True, include_optimizer=False, save_format='tf')


def train_denoise(model, noise_level=NOISE_LEVEL, num_epochs=2, batch_size=4):
    train_gen = DIV2K(DATA_PATH, noise_level=noise_level, batch_size=batch_size, patch_size=0, type="train")
    adam = tf.keras.optimizers.Adam(learning_rate=2e-4)
    model.compile(optimizer=adam, loss='mse')
    model.fit(train_gen, epochs=num_epochs, workers=8, verbose=1)
    model.save(SAVED_MODEL_PATH, overwrite=True, include_optimizer=False, save_format='tf')


# model = SRNet.build(scale_factor=SCALE, num_channels=3, input_shape=(None, None, 3))
model = DnCNN.build(input_shape=(None, None, 3))
print(model.summary())
train_denoise(model)