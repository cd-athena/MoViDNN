from Models import FSRCNN, ESPCN, SRABRNet, EVSRNet
from div2k import DIV2K

DATA_PATH = "./DIV2K/"
SAVED_MODEL_PATH = "./Checkpoints/EVSRNet_x4"
SCALE = 4


def train(model, scale_factor=SCALE, num_epochs=10, batch_size=32):
    train_gen = DIV2K(DATA_PATH, scale_factor=scale_factor, batch_size=batch_size)

    model.compile(optimizer='adam', loss='mse')
    model.fit(train_gen, epochs=num_epochs, workers=8)
    model.save(SAVED_MODEL_PATH, overwrite=True, include_optimizer=False, save_format='tf')


model = EVSRNet.build(scale_factor=SCALE, num_channels=3, input_shape=(None, None, 3))
print(model.summary())
train(model)
