import cv2
import numpy as np
from PIL import Image

class Camera:
    def __init__(self, settings):
        self.settings = settings
        self.setup()

    def getFrame(self):
        if(self.settings["cameraIndex"] == -1):
            data = np.asarray(Image.open("Data/Test/PXL_20230626_022707896.jpg"))
            data = cv2.cvtColor(data, cv2.COLOR_RGB2BGR)
            return data, 200
        else:
            if not self.camera:
                print("Camera not initialized, attempting to connect")
                self.setup()
                if not self.camera:
                    return "Failed to connect camera", 500
                
            if not self.camera.isOpened():
                return "Failed open camera", 500
                
            if "cameraExposure" in self.settings:
                self.camera.set(cv2.CAP_PROP_EXPOSURE, self.settings["cameraExposure"])

            self.camera.read()
            success, frame = self.camera.read()
            if not success:
                return "Can't receive frame", 500
            
            return frame, 200

    def setup(self):
        self.camera = cv2.VideoCapture(self.settings["cameraIndex"])
        if not self.camera:
            print("No camera detected")
        if self.camera.isOpened():
            self.camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)
            if "cameraExposure" in self.settings:
                self.camera.set(cv2.CAP_PROP_EXPOSURE, self.settings["cameraExposure"])
        else:
            print("Failed open camera")

    def release(self):
        if self.camera:
            self.camera.release()