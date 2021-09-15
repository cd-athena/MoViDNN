import argparse, os
import tensorflow as tf
from Models import ESPCN, EVSRNet, DnCNN
from dataset import SRDataset

DATA_PATH = "./Dataset/TrainingFrames/"
SAVED_MODEL_PATH = "./Checkpoints/DnCNN_25"
SCALE = 4
NOISE_LEVEL = 25


def build_default_model(model_name, scale):
    if model_name == "ESPCN":
        return ESPCN.build(scale_factor=scale, num_channels=3, input_shape=(None, None, 3))
    elif model_name == "EVSRNet":
        return EVSRNet.build(scale_factor=scale, num_channels=3, input_shape=(None, None, 3))
    elif model_name == "DnCNN":
        return DnCNN.build(input_shape=(None, None, 3))


def train(model, model_name, scale, data_path, num_epochs=2, batch_size=4):
    os.makedirs("./Checkpoints/", exist_ok=True)
    train_gen = SRDataset(lr_root=data_path + "X{}/".format(scale), hr_root=data_path + "X1/", batch_size=batch_size)
    adam = tf.keras.optimizers.Adam(learning_rate=5e-6)
    model.compile(optimizer=adam, loss='mse')
    model.fit(train_gen, epochs=num_epochs, workers=8, verbose=1)
    model.save("./Checkpoints/{}".format(model_name), overwrite=True, include_optimizer=True, save_format='tf')


default_models = ["ESPCN", "EVSRNet", "DnCNN"]
parser = argparse.ArgumentParser(description="This script is used for training SR or Denoising networks for MoViDNN")
parser.add_argument("-d", "--default_model", type=str, help="Choose one of the default models to train", choices=default_models)
parser.add_argument("-s", "--scale", type=int, required=True,
                    help="Scale for the SR model, leave it as 1 for denoising/deblocking models")
parser.add_argument("-data", "--data_path", required=True,
                    type=str, help="Path for the input data")
parser.add_argument("-n", "--name", required=True,
                    type=str, help="Name to save the trained model checkpoint")
parser.add_argument("-e", "--epoch", type=int, help="Number of epochs to be trained")
args = parser.parse_args()

scale = args.scale
data_path = args.data_path
name = args.name
epoch = args.epoch if args.epoch is not None else 50
default_model = build_default_model(args.default_model, scale) if args.default_model is not None else build_default_model("ESPCN", scale)

train(default_model, model_name=name, scale=scale, data_path=data_path, num_epochs=epoch, batch_size=16)



