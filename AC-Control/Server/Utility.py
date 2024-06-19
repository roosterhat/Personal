from os import system
import cv2
import numpy as np
from PIL import Image, ImageOps, ImageCms
import math
import json
from datetime import datetime
import traceback
import re

def colorDistance_old(c1, c2):
    rBar = 0.5 * (c1[0] + c2[0])
    v = (2 + rBar / 256) * (c1[0] - c2[0]) + 4 * pow(c1[1] - c2[1], 2) + (2 + (255 - rBar) / 256) * (c1[2] - c2[2])
    if math.isnan(v) or v < 0:
        return False
    return pow(v, 1/2)

srgb_p = ImageCms.createProfile("sRGB")
lab_p  = ImageCms.createProfile("LAB")
rgb2lab = ImageCms.buildTransformFromOpenProfiles(srgb_p, lab_p, "RGB", "LAB")
def colorDistance(color1, color2):    
    img1 = Image.new('RGB', (1,1), tuple(color1))
    img2 = Image.new('RGB', (1,1), tuple(color2))
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
    res1 = 0 if i < 0 else math.sqrt(i)

    rBar = 0.5 * (color1[0] + color2[0])
    v = (2 + rBar / 256) * (color1[0] - color2[0]) + 4 * pow(color1[1] - color2[1], 2) + (2 + (255 - rBar) / 256) * (color1[2] - color2[2])
    res2 = 0 if math.isnan(v) or v < 0 else pow(v, 1/2)    

    return (res1 + res2) / 2


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
    config = re.sub(r'[^\d\w-]+', '', config)
    action = re.sub(r'[^\d\w-]+', '', action)
    print(f"Trigger: [{config}] [{action}]")
    system(f"irsend SEND_ONCE {config} {action}")

def boundingBoxInsideShape(shape, box):
    if box is None:
        return False
    boxCenter = {"x": box[0] + (box[2] - box[0]) / 2, "y": box[1] + (box[3] - box[1]) / 2 }
    if shape["type"] == "ellipse":
        return bool(pow((boxCenter["x"] - shape["cx"]) / shape["r1"], 2) + pow((boxCenter["y"] - shape["cy"]) / shape["r2"], 2) - 1 < 0)
    return False

def buildStateFrame(frame, states, config):
    scale = config["position"]["scale"]
    w = 0
    h = 0
    padding = 5
    buffer = 2
    offset = padding    
    patches = {}
    for state in states:
        shape = state["shape"]             

        x1 = int(max(shape["x"] - shape["r1"], 0) / scale)
        x2 = int(max(shape["x"] + shape["r1"], 0) / scale)
        y1 = int(max(shape["y"] - shape["r2"], 0) / scale)
        y2 = int(max(shape["y"] + shape["r2"], 0) / scale)
        cx = int(shape["x"] / scale)
        cy = int(shape["y"] / scale)
        r1 = int(shape["r1"] / scale)
        r2 = int(shape["r2"] / scale)

        patch = np.ndarray((y2 - y1, x2 - x1, 3), np.uint8)

        for y in range(y1, y2):
            for x in range(x1, x2):
                patch[y - y1][x - x1] = frame[y][x] if pow((x - cx) / r1, 2) + pow((y - cy) / r2, 2) - 1 < 0 else 0                    

        patch = cv2.cvtColor(patch, cv2.COLOR_BGR2RGB)
        patch = np.rot90(patch, -config["rotate"] / 90)
        patches[state["id"]] = patch
        w = max(len(patch[0]), w)
        h += len(patch) + buffer

    w += padding * 2
    h += padding * 2
    shapes = {}
    combined = Image.new("RGB", (int(w), int(h)), 0)

    for id in patches:
        patch = patches[id]
        combined.paste(Image.fromarray(patch), (int((w - len(patch[0])) / 2), int(offset)))
        shapes[id] = { 
            "type": "ellipse",
            "cx": int(w / 2),             
            "cy": int(len(patch) / 2 + offset), 
            "r1": int(len(patch[0]) / 2),
            "r2": int(len(patch) / 2)         
        }
        offset += len(patch) + buffer
    return { "frame": combined, "shapes": shapes }

def AppendEvent(type, value):
    with open("./Data/events", 'rb') as f:
        try:
            data = json.loads(f.read())
        except Exception:
            print(traceback.format_exc())
            data = []
    data.append({
        "time": datetime.now(),
        "type": type,
        "value": value
    })
    with open("./Data/events", 'w') as f:
        f.write(json.dumps(data, default=str))
