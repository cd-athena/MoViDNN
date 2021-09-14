import argparse
import tensorflow as tf
from Models import ESPCN, EVSRNet, DnCNN
from div2k import DIV2K

DATA_PATH = "./DIV2K/"
SAVED_MODEL_PATH = "./Checkpoints/DnCNN_25"
SCALE = 4
NOISE_LEVEL = 25


def train_sr(model, scale_factor=SCALE, num_epochs=2, batch_size=4):
    train_gen = DIV2K(DATA_PATH, scale_factor=scale_factor, batch_size=batch_size, type="train")
    adam = tf.keras.optimizers.Adam(learning_rate=5e-6)
    model.compile(optimizer=adam, loss='mse')
    model.fit(train_gen, epochs=num_epochs, workers=8, verbose=2)
    model.save(SAVED_MODEL_PATH, overwrite=True, include_optimizer=False, save_format='tf')


def train_denoise(model, noise_level=NOISE_LEVEL, num_epochs=2, batch_size=4):
    train_gen = DIV2K(DATA_PATH, noise_level=noise_level, batch_size=batch_size, patch_size=0, type="train")
    adam = tf.keras.optimizers.Adam(learning_rate=5e-6)
    model.compile(optimizer=adam, loss='mse')
    model.fit(train_gen, epochs=num_epochs, workers=8, verbose=1)
    model.save(SAVED_MODEL_PATH, overwrite=True, include_optimizer=False, save_format='tf')


default_models = ["ESPCN", "EVSRNet", "DnCNN"]
parser = argparse.ArgumentParser(description="This script is used for training SR or Denoising networks for MoViDNN")
parser.add_argument("-d", "--default_model", type=str, help="Choose one of the default models to train", choices=default_models)
parser.add_argument("-s", "--scale", type=int, required=True,
                    help="Scale for the SR model, leave it as 1 for denoising/deblocking models")
parser.add_argument("-data", "--data_path", required=True,
                    type=str, help="Path for the input data")
parser.add_argument("-c", "--checkpoint", required=True,
                    type=str, help="Path to save the trained model checkpoint")
args = parser.parse_args()

scale = args.scale
data_path = args.data_path
checkpoint = args.checkpoint


model = ESPCN.build(scale_factor=SCALE, num_channels=3, input_shape=(None, None, 3))
# model = DnCNN.build(input_shape=(None, None, 3))
# print(model.summary())
# train_denoise(model)



