import subprocess

MODEL_NAME = "srabrnet_x3"

cmd = ["tflite_codegen", "--model=./AndroidWrappers/Metadata/{}/{}.tflite".format(MODEL_NAME, MODEL_NAME),
       "--package_name=com.example.tflitesr.{}".format(MODEL_NAME),
       "--model_class_name={}".format(MODEL_NAME.upper()), "--destination=./AndroidWrappers/{}".format(MODEL_NAME)]
process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
out, err = process.communicate()
print(err)
