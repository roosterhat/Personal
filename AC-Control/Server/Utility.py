from os import system
import cv2
import numpy as np
from PIL import Image, ImageOps, ImageCms
import math

def colorDistance_old(c1, c2):
    rBar = 0.5 * (c1[0] + c2[0])
    v = (2 + rBar / 256) * (c1[0] - c2[0]) + 4 * pow(c1[1] - c2[1], 2) + (2 + (255 - rBar) / 256) * (c1[2] - c2[2])
    if math.isnan(v) or v < 0:
        return False
    return pow(v, 1/2)

srgb_p = ImageCms.createProfile("sRGB")
lab_p  = ImageCms.createProfile("LAB")
rgb2lab = ImageCms.buildTransformFromOpenProfiles(srgb_p, lab_p, "RGB", "LAB")
def colorDistance(c1, c2):    
    img1 = Image.new('RGB', (1,1), tuple(c1))
    img2 = Image.new('RGB', (1,1), tuple(c2))
    labA = ImageCms.applyTransform(img1, rgb2lab).getpixel((0,0))
    labB = ImageCms.applyTransform(img2, rgb2lab).getpixel((0,0))
    deltaL = labA[0] - labB[0]
    deltaA = labA[1] - labB[1]
    deltaB = labA[2] - labB[2]
    c1 = math.sqrt(labA[1] * labA[1] + labA[2] * labA[2])
    c2 = math.sqrt(labB[1] * labB[1] + labB[2] * labB[2])
    deltaC = c1 - c2
    deltaH = deltaA * deltaA + deltaB * deltaB - deltaC * deltaC
    deltaH = 0 if deltaH < 0 else math.sqrt(deltaH)
    sc = 1.0 + 0.045 * c1
    sh = 1.0 + 0.015 * c1
    deltaLKlsl = deltaL / (1.0)
    deltaCkcsc = deltaC / (sc)
    deltaHkhsh = deltaH / (sh)
    i = deltaLKlsl * deltaLKlsl + deltaCkcsc * deltaCkcsc + deltaHkhsh * deltaHkhsh
    return 0 if i < 0 else math.sqrt(i)


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