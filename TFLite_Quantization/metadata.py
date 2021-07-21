"""Writes metadata to SR models."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import os

from absl import app
import tensorflow as tf

import flatbuffers
from tflite_support import metadata_schema_py_generated as _metadata_fb
from tflite_support import metadata as _metadata
# pylint: enable=g-direct-tensorflow-import

MODEL = "srabrnet_x3"
MODEL_NAME = "{}.tflite".format(MODEL)
QUANT_MODEL_DIR = "./QuantModels/"
METADATA_DIR = "./AndroidWrappers/Metadata/{}".format(MODEL)

class ModelSpecificInfo(object):
    """Holds information that is specificly tied to an SR network"""

    def __init__(self, name, scale, target_width, target_height, image_min,
                 image_max, author):
        self.name = name
        self.scale = scale
        self.target_width = target_width
        self.target_height = target_height
        self.image_min = image_min
        self.image_max = image_max
        self.author = author

_MODEL_INFO = {
    "{}.tflite".format(MODEL):
        ModelSpecificInfo(
            name="{} Model for SR-ABR".format(MODEL),
            scale=4,
            target_width=1920,
            target_height=1080,
            image_min=0,
            image_max=1,
            author="SR_ABR")
}


class MetadataPopulatorForImageClassifier(object):
  """Populates the metadata for the SR network"""

  def __init__(self, model_file, model_info):
    self.model_file = model_file
    self.model_info = model_info
    self.metadata_buf = None

  def populate(self):
    """Creates metadata and then populates it for the SR network"""
    self._create_metadata()
    self._populate_metadata()

  def _create_metadata(self):
    """Creates the metadata for the SR network"""

    # Creates model info.
    model_meta = _metadata_fb.ModelMetadataT()
    model_meta.name = self.model_info.name
    model_meta.description = ("Upscales the given image with x{}".format(self.model_info.scale))
    model_meta.author = self.model_info.author
    # Creates input info.
    input_meta = _metadata_fb.TensorMetadataT()
    input_meta.name = "LR image"
    input_meta.description = (
        "Input image to be Upscaled. The expected image is {0} x {1}, with "
        "three channels (red, blue, and green) per pixel. Each value in the "
        "tensor is a single byte between {2} and {3}.".format(
            self.model_info.target_width / self.model_info.scale, self.model_info.target_height / self.model_info.scale,
            self.model_info.image_min, self.model_info.image_max))
    input_meta.content = _metadata_fb.ContentT()
    input_meta.content.contentProperties = _metadata_fb.ImagePropertiesT()
    input_meta.content.contentProperties.colorSpace = (
        _metadata_fb.ColorSpaceType.RGB)
    input_meta.content.contentPropertiesType = (
        _metadata_fb.ContentProperties.ImageProperties)
    input_normalization = _metadata_fb.ProcessUnitT()
    input_normalization.optionsType = (
        _metadata_fb.ProcessUnitOptions.NormalizationOptions)
    input_normalization.options = _metadata_fb.NormalizationOptionsT()
    input_normalization.options.mean = [0.0]
    input_normalization.options.std = [255.0]
    input_meta.processUnits = [input_normalization]
    input_stats = _metadata_fb.StatsT()
    input_stats.max = [self.model_info.image_max]
    input_stats.min = [self.model_info.image_min]
    input_meta.stats = input_stats

    # Creates output info.
    output_meta = _metadata_fb.TensorMetadataT()
    output_meta.name = "SR image"
    output_meta.description = "Upscaled image with the scale factor x{}".format(self.model_info.scale)
    output_meta.content = _metadata_fb.ContentT()
    output_meta.content.contentProperties = _metadata_fb.ImagePropertiesT()
    output_meta.content.contentProperties.colorSpace = (
        _metadata_fb.ColorSpaceType.RGB)
    output_meta.content.contentPropertiesType = (
        _metadata_fb.ContentProperties.ImageProperties)
    output_normalization = _metadata_fb.ProcessUnitT()
    output_normalization.optionsType = (
        _metadata_fb.ProcessUnitOptions.NormalizationOptions)
    output_normalization.options = _metadata_fb.NormalizationOptionsT()
    output_normalization.options.mean = [0.0]
    output_normalization.options.std = [1.0]
    output_meta.processUnits = [output_normalization]
    output_stats = _metadata_fb.StatsT()
    output_stats.max = [self.model_info.image_max]
    output_stats.min = [self.model_info.image_min]
    output_meta.stats = output_stats

    # Creates subgraph info.
    subgraph = _metadata_fb.SubGraphMetadataT()
    subgraph.inputTensorMetadata = [input_meta]
    subgraph.outputTensorMetadata = [output_meta]
    model_meta.subgraphMetadata = [subgraph]

    b = flatbuffers.Builder(0)
    b.Finish(
        model_meta.Pack(b),
        _metadata.MetadataPopulator.METADATA_FILE_IDENTIFIER)
    self.metadata_buf = b.Output()

  def _populate_metadata(self):
    """Populates metadata and label file to the model file."""
    populator = _metadata.MetadataPopulator.with_model_file(self.model_file)
    populator.load_metadata_buffer(self.metadata_buf)
    populator.populate()


def main(_):
  model_file = QUANT_MODEL_DIR + MODEL_NAME
  model_basename = MODEL_NAME
  if model_basename not in _MODEL_INFO:
    raise ValueError(
        "The model info for, {0}, is not defined yet.".format(model_basename))

  os.makedirs(METADATA_DIR, exist_ok=True)
  export_model_path = os.path.join(METADATA_DIR, model_basename)

  # Copies model_file to export_path.
  tf.io.gfile.copy(model_file, export_model_path, overwrite=False)

  # Generate the metadata objects and put them in the model file
  populator = MetadataPopulatorForImageClassifier(
      export_model_path, _MODEL_INFO.get(model_basename))
  populator.populate()

  # Validate the output model file by reading the metadata and produce
  # a json file with the metadata under the export path
  displayer = _metadata.MetadataDisplayer.with_model_file(export_model_path)
  export_json_file = os.path.join(METADATA_DIR,
                                  os.path.splitext(model_basename)[0] + ".json")
  json_file = displayer.get_metadata_json()
  with open(export_json_file, "w") as f:
    f.write(json_file)

  print("Finished populating metadata and associated file to the model:")
  print(model_file)
  print("The metadata json file has been saved to:")
  print(export_json_file)
  print("The associated file that has been been packed to the model is:")
  print(displayer.get_packed_associated_file_list())


if __name__ == "__main__":
  app.run(main)