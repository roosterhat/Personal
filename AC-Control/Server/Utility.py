from os import system
import cv2
import numpy as np
from PIL import Image, ImageOps
import math

def colorDistance(c1, c2):
    rBar = 0.5 * (c1[0] + c2[0])
    v = (2 + rBar / 256) * (c1[0] - c2[0]) + 4 * pow(c1[1] - c2[1], 2) + (2 + (255 - rBar) / 256) * (c1[2] - c2[2])
    if math.isnan(v) or v < 0:
        return False
    return pow(v, 1/2)

def prepareOCRImage(image, options):
    if "grayscale" in options and options["grayscale"]:
        image = image.convert("L")
    if "threshold" in options and options["threshold"]:
        image = image.point( lambda p: p if p > options["threshold"] else np.random.randint(int(options["threshold"] * 0.2), int(options["threshold"] * 0.8)))
    if "invert" in options and options["invert"]:
        image = ImageOps.invert(image)
    if "scale" in options and options["scale"]:
        image = image.resize(((int(image.width * options["scale"])), int(image.height * options["scale"])))
    
    padding = 0.4
    padded = Image.new("RGB", (int(image.width + image.width * padding), int(image.height + image.height * padding)), 0)
    padded.paste(image, (int(image.width * (padding / 2)), int(image.height * (padding / 2))))
    return padded

def reshapeImage(config, frame, vertices):
    scale = config["frame"]["position"]["scale"]
    points = np.array([[int(p["x"] / scale), int(p["y"] / scale)] for p in vertices])
    (_,_), (width, height), a = cv2.minAreaRect(points)
    if a > 45:
        width, height = height, width
    dstPts = [[0, 0], [width, 0], [width, height], [0, height]]
    transform = cv2.getPerspectiveTransform(np.float32(points), np.float32(dstPts))
    out = cv2.warpPerspective(frame, transform, (int(width), int(height)))
    out = np.rot90(out, -config["frame"]["rotate"] / 90)
    out = cv2.cvtColor(out, cv2.COLOR_BGR2RGB)
    image = Image.fromarray(out)
    return image

def triggerIR(config, action):
    print(f"Trigger: [{config}] [{action}]")
    system(f"irsend SEND_ONCE {config} {action}")