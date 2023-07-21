from flask import Flask, request, send_from_directory
from flask_cors import CORS
import re
from os import listdir, path as Path, system
import json
import uuid
import io
import cv2
import numpy as np
from PIL import Image, ImageColor, ImageDraw, ImageOps
from datetime import datetime, timedelta, time
import time as Time
from threading import Thread
import hashlib
import math
from ultralytics import YOLO


ILLEGAL_CHARS = r'\/\.\@\#\$\%\^\&\*\(\)\{\}\[\]\"\'\`\,\<\>\\'
fileExtPattern = re.compile(r'\.(?P<ext>js|ico|css|png|jpg|html)$')
fileNamePattern = re.compile(rf'[^{ILLEGAL_CHARS}]+')
dateTimePattern = re.compile(r'\d{4}-\d{2}-\d{2}\s*\d{2}:\d{2}:\d{2}.\d+')
contentTypeMap = { 'js': 'text/javascript', 'ico': 'image/x-icon', 'css': 'text/css', 'png': 'image/png', 'jpg': 'image/jpg', 'html': 'text/html' }

app = Flask(__name__, static_folder='../Client/build')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'
camera = None
settings = None
OCRModel = None
sessions = []
lastLoginAttempt = datetime.now()

@app.route('/', defaults={'path': ''})
@app.route('/<path:path>')
def serve(path):
    if path != "" and Path.exists(app.static_folder + '/' + path):
        return send_from_directory(app.static_folder, path)
    else:
        return send_from_directory(app.static_folder, 'index.html')

@app.route('/api/login', methods=["POST"])
def login():
    global lastLoginAttempt, settings
    if lastLoginAttempt + timedelta(seconds=5) > datetime.now():
        Time.sleep((datetime.now() - lastLoginAttempt).seconds)

    lastLoginAttempt = datetime.now()
    body = request.get_json(force = True, silent = True)
    if body is None or "password" not in body:
        return 'Bad login', 400
    
    try:        
        if hashlib.sha512((body["password"] + settings["salt"]).encode('utf-8')).hexdigest() == settings["password"]:
            token = str(uuid.uuid4())
            sessions.append({"token": token, "lastActivity": datetime.now()})
            return token, 200
        else:
            return 'Bad login', 400
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500

@app.route('/api/list')
def listConfigs():
    if not verifyToken():
        return "Unauthorized", 401
    try:
        names = []
        for file in ([f for f in listdir('./Data/Configs') if Path.isfile(Path.join('./Data/Configs', f))]):
            try:
                f = open(f"./Data/Configs/{file}", 'rb')
                data = json.loads(f.read())
                names.append({'name': data['name'], 'id': data['id']})
            except Exception as ex:
                print(ex, flush=True)
                return "Failed", 500
            finally:
                f.close()
        return names, 200
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500

@app.route('/api/save', methods=["POST"])
def saveConfig():
    if not verifyToken():
        return "Unauthorized", 401
    body = request.get_json(force = True, silent = True)
    if body is None:
        return 'No config data', 400
    if "id" not in body or not fileNamePattern.search(body["id"]):
        return 'Invalid config data', 400        
    
    try:        
        f = open(f"./Data/Configs/{body['id']}", 'w')
        f.write(json.dumps(body))
        f.close()
        f = open(f"./Data/default", 'w')
        f.write(body["id"])
        return "Success", 200
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()
    
@app.route('/api/retrieve/<id>')
def retrieveConfig(id):
    if not verifyToken():
        return "Unauthorized", 401
    try:
        if id == 'default':
            f = open("./Data/default", 'r')
            id = f.read()
            f.close()
            if not Path.isfile(f"./Data/Configs/{id}"):
                return "None", 404
        else:
            if not Path.isfile('./Data/Configs/'+id):
                return 'Config does not exists', 400
            f = open(f"./Data/default", 'w')
            f.write(id)
            f.close()
        f = open(f"./Data/Configs/{id}", 'rb')
        data = f.read()
        return data, 200, {'Content-Type':'application/json'} 
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/settings', methods=["POST"])
def saveSettings():
    if not verifyToken():
        return "Unauthorized", 401
    global settings
    body = request.get_json(force = True, silent = True)
    if body is None:
        return 'No settings data', 400
    
    try:        
        f = open(f"./Data/settings", 'r')
        data = json.loads(f.read())
        f.close()
        for key, value in body.items():
            if key not in ["password", "salt"]:
                data[key] = value
        f = open(f"./Data/settings", 'w')
        f.write(json.dumps(data))
        settings = data
        return "Success", 200
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/settings', methods=["GET"])
def retrieveSettings():
    if not verifyToken():
        return "Unauthorized", 401
    try:
        f = open(f"./Data/settings", 'r')
        data = json.loads(f.read())
        for item in ["password", "salt"]:
            del data[item]
        return data, 200, {'Content-Type':'application/json'} 
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/upload', methods=["POST"])
def upload():
    if not verifyToken():
        return "Unauthorized", 401
    file = request.get_data()
    if file is None or not any(file):
        return 'No file data', 400
    try:
        filename = str(uuid.uuid4())
        f = open('./Data/Backgrounds/'+filename, 'wb')
        f.write(file)
        return filename, 200
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/background/<filename>')
def background(filename):
    if not verifyToken():
        return "Unauthorized", 401
    if not Path.isfile('./Data/Backgrounds/'+filename):
        return 'File does not exists', 400
    try:
        f = open('./Data/Backgrounds/'+filename, 'rb')
        data = f.read()
        return data, 200, {'Content-Type':'image'} 
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

def colorDistance(c1, c2):
    rBar = 0.5 * (c1[0] + c2[0])
    v = (2 + rBar / 256) * (c1[0] - c2[0]) + 4 * pow(c1[1] - c2[1], 2) + (2 + (255 - rBar) / 256) * (c1[2] - c2[2])
    if math.isnan(v) or v < 0:
        return False
    return pow(v, 1/2)

def sampleFrameEllipse(frame, state, config):
    shape = state["shape"]
    scale = config["position"]["scale"]
    x1 = int(max(shape["x"] - shape["r1"], 0) / scale)
    x2 = int(max(shape["x"] + shape["r1"], 0) / scale)
    y1 = int(max(shape["y"] - shape["r2"], 0) / scale)
    y2 = int(max(shape["y"] + shape["r2"], 0) / scale)
    cx = int(shape["x"] / scale)
    cy = int(shape["y"] / scale)
    r1 = int(shape["r1"] / scale)
    r2 = int(shape["r2"] / scale)
    activeColor = ImageColor.getrgb(state["properties"]["activeColor"])[:3]
    threshold = state["properties"]["colorDistanceThreshold"] if "colorDistanceThreshold" in state["properties"] else 20
    count = 0
    for y in range(y1, y2):
        for x in range(x1, x2):
            if pow((x - cx) / r1, 2) + pow((y - cy) / r2, 2) - 1 < 0 and colorDistance(activeColor, frame[y][x]) <= threshold:
                count += 1

    activation = count / ((y2-y1) * (x2-x1)) * 100
    threshold = state["properties"]["stateActivationPercentage"] if "stateActivationPercentage" in state["properties"] else 5
    return activation >= threshold

def getPowerState(config, state):
    equation = ""
    for element in config["actions"]["power"]["stateEquation"]:
        if element["type"] == "operator":
            if element["name"] not in ["(",")","not","and","or"]:
                return "Invalid operator", 400
            equation += f"{element['name']} "
        else:
            value = next((s["active"] for s in state["states"] if s["id"] == element["id"]), None)
            if value is None:
                return f"No state value found for {element['name']}", 400
            equation += f"{value} "
    
    try:
        return eval(equation)
    except:
        return False

def prepareOCRImage(view, image):
    if "grayscale" in view["properties"] and view["properties"]["grayscale"]:
        image = image.convert("L")
    if "threshold" in view["properties"] and view["properties"]["threshold"]:
        image = image.point( lambda p: p if p > view["properties"]["threshold"] else np.random.randint(int(view["properties"]["threshold"] * 0.2), int(view["properties"]["threshold"] * 0.8)))
    if "invert" in view["properties"] and view["properties"]["invert"]:
        image = ImageOps.invert(image)
    if "scale" in view["properties"] and view["properties"]["scale"]:
        image = image.resize(((int(image.width * view["properties"]["scale"])), int(image.height * view["properties"]["scale"])))
    
    padding = 0.4
    padded = Image.new("RGB", (int(image.width + image.width * padding), int(image.height + image.height * padding)), 0)
    padded.paste(image, (int(image.width * (padding / 2)), int(image.height * (padding / 2))))
    return padded

def getState(config, sections = None):
    currentState = {}
    result, status = getCameraFrame()
    if status != 200:
        return None
    frame = result

    if "states" in config["frame"] and (sections is None or "states" in sections or "power" in sections):
        currentState["states"] = config["frame"]["states"]
        for state in config["frame"]["states"]:
            state["active"] = sampleFrameEllipse(frame, state, config["frame"])
    
    if "ocr" in config["frame"] and (sections is None or "ocr" in sections):
        currentState["ocr"] = config["actions"]["ocr"]
        for target in currentState["ocr"]:
            view = next((x for x in config["frame"]["ocr"] if x["id"] == target["view"]), None)
            image = ReshapeImage(config, frame, view["shape"]["vertices"])
            image = prepareOCRImage(view, image)
            results = OCRModel(image, device="cpu", verbose=False, agnostic_nms=True)
            value = ""
            for data in ([x.numpy() for x in sorted(results[0].boxes.data, key=lambda x: x[0])]):
                value += str(int(data[5]))
            target["value"] = value
    
    if sections is None or "power" in sections:
        currentState["power"] = { "active": getPowerState(config, currentState) }
    return currentState

def stateChanged(newState, oldState):
    try:
        return any(newState[i]["active"] != oldState[i]["active"] for i in range(len(newState)))
    except:
        return True
    
def stateActive(states, id):
    return next((x["active"] for x in states if x["id"] == id), None)
    
def attemptSetPower(config, action, target, settings):
    for i in range(settings["triggerAttempts"]):
        triggerIR(config["ir_config"], action)
        Time.sleep(settings["setStateDelay"] / 1000)
        state = getState(config, ["power"])
        if state and state["power"]["active"] == target:
            return True        
    return False

def walkStateGroup(config, group, state, action, settings):
    oldState = getState(config, ["states"])
    if oldState and stateActive(oldState["states"], state["id"]):
        return True
    for _ in range(len(group["states"])):
        for _ in range(settings["triggerAttempts"]):
            triggerIR(config["ir_config"], action)
            Time.sleep(settings["setStateDelay"] / 1000)
            newState = getState(config, ["states"])
            if stateActive(newState["states"], state["id"]):
                return True
            if stateChanged(newState, oldState):
                break
            oldState = newState            
    return False

def getTemperature(state):
    return next((int(x["value"]) for x in state["ocr"] if x["name"] == "Temperature"), None)

def temperatureChanged(oldState, newState):
    return getTemperature(oldState) != getTemperature(newState)

def setTemperature(config, target, actions, settings):
    currentTemp = getTemperature(getState(config, ["ocr"]))
    if currentTemp == target:
        return True
    count = 0
    UP = next((x for x in actions if x["name"] == "Up"), None)
    DOWN = next((x for x in actions if x["name"] == "Down"), None)
    while settings["minTemperature"] <= currentTemp <= settings["maxTemperature"]:
        triggerIR(config["ir_config"], UP["action"] if currentTemp < target else DOWN["action"])
        Time.sleep(settings["setStateDelay"] / 1000)
        newTemp = getTemperature(getState(config, ["ocr"]))
        if newTemp == target:
            return True
        if currentTemp != newTemp:
            count = 0
        else:
            count += 1
        if count >= settings["triggerAttempts"]:
            return False
        currentTemp = newTemp
    return False

def getOCRValue(state, id):
    return next((x["value"] for x in state["ocr"] if x["id"] == id), None)

def atTargetValue(state, id, target):
    return getOCRValue(state, id) == target

def OCRValueChanged(oldState, newState, id):
    return getOCRValue(oldState, id) != getOCRValue(newState, id)

def setOCRValue(config, id, target, action, settings):
    oldState = getState(config, ["ocr"])
    if oldState and atTargetValue(oldState, id, target):
        return True
    for _ in range(settings["maxOCRValueChange"]):
        for _ in range(settings["triggerAttempts"]):
            triggerIR(config["ir_config"], action)
            Time.sleep(settings["setStateDelay"] / 1000)
            newState = getState(config, ["ocr"])
            if atTargetValue(newState, id, target):
                return True
            if OCRValueChanged(newState, oldState, id):
                break
            oldState = newState            
    return False

def setState(config, targetState, setting=None):
    global settings
    if setting is None:
        setting = settings

    currentState = getState(config, ["power"])
    if not currentState:
        return "Failed to get state"

    buttonMap = {}
    for b in config["buttons"]:
        buttonMap[b["id"]] = b["action"]
    stateMap = {}
    for g in config["actions"]["stateGroups"]:
        for s in g["states"]:
            stateMap[s["id"]] = g

    if targetState["power"]["active"]:
        if not currentState["power"]["active"]:
            if not attemptSetPower(config, buttonMap[config["actions"]["power"]["button"]], True, setting):
                return f"Failed to set power [On]"
        for state in targetState["states"]:
            if state["id"] not in stateMap:
                return "State not associated with a group"
            group = stateMap[state["id"]]
            if not walkStateGroup(config, group, state, buttonMap[group["button"]], setting):
                return f"Failed to set {config['frame']['states'][state['id']]['name']} [On]"
        for ocr in targetState["ocr"]:
            if not ocr["target"]:
                continue 
            if ocr["name"] == "Temperature":
                if not setTemperature(config, int(ocr["target"]), ([{"name": x["name"], "action": buttonMap[x["button"]]} for x in ocr["buttons"]]), setting):
                    return f"Failed to set Temperature to [{ocr['target']}]"
            else:
                if not setOCRValue(config, ocr["id"], ocr["target"], buttonMap[ocr["buttons"][0]["button"]], setting):
                    return f"Failed to set {ocr['name']} to [{ocr['target']}]"
    else:
        if currentState["power"]["active"]:
            if not attemptSetPower(config, buttonMap[config["actions"]["power"]["button"]], False, setting):
                return f"Failed to set power [Off]"

@app.route('/api/state/<id>')
@app.route('/api/state/<id>/<section>')
def getState_API(id, section = None):
    if not verifyToken():
        return "Unauthorized", 401
    try:
        if not Path.isfile('./Data/Configs/'+id):
            return 'Config does not exists', 400
        f = open(f"./Data/Configs/{id}", 'rb')
        config = json.loads(f.read())
        f.close()
        if "frame" not in config:
            return "No frame data", 400
        sections = None
        if section and section in ["states", "power", "ocr"]:
            sections = [section]
        state = getState(config, sections)
        if state:
            return state, 200, {'Content-Type':'image'} 
        else:
            return "Failed to get state", 500
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500

def debugSampleFrameEllipse(frame, state, config):
    shape = state["shape"]
    scale = config["position"]["scale"]
    x1 = int(max(shape["x"] - shape["r1"], 0) / scale)
    x2 = int(max(shape["x"] + shape["r1"], 0) / scale)
    y1 = int(max(shape["y"] - shape["r2"], 0) / scale)
    y2 = int(max(shape["y"] + shape["r2"], 0) / scale)
    patch = np.ndarray((y2-y1, x2-x1, 3), np.uint8)
    mask = np.ndarray((y2-y1, x2-x1), np.uint8)
    cx = int(shape["x"] / scale)
    cy = int(shape["y"] / scale)
    r1 = int(shape["r1"] / scale)
    r2 = int(shape["r2"] / scale)
    activeColor = ImageColor.getrgb(state["properties"]["activeColor"])[:3]
    threshold = state["properties"]["colorDistanceThreshold"] if "colorDistanceThreshold" in state["properties"] else 20
    count = 0 
    total = 0
    for y in range(y1, y2):
        for x in range(x1, x2):
            if pow((x - cx) / r1, 2) + pow((y - cy) / r2, 2) - 1 < 0:
                active = colorDistance(activeColor, frame[y][x]) <= threshold
                count += 1 if active else 0
                total += 1
                mask[y - y1][x - x1] = 255 if active else 0
                patch[y - y1][x - x1] = frame[y][x]       
            else:
                mask[y - y1][x - x1] = 50
                patch[y - y1][x - x1] = [50,50,50]

    activation = count / total * 100
    patch = cv2.cvtColor(patch, cv2.COLOR_BGR2RGB)
    return mask, patch, activation 
    
def debugState(config, stateId):
    if "frame" not in config:
        return "No frame data", 400
    
    result, status = getCameraFrame()
    if status != 200:
        return result, status
    frame = result

    if request.method == 'POST':
        body = request.get_json(force = True, silent = True)
        if body is None:
            return 'No config data', 400
        state = body
    else:
        state = next((x for x in config["frame"]["states"] if x["id"] == stateId), None)
        
    if state:
        mask, patch, act = debugSampleFrameEllipse(frame, state, config["frame"])
        return {"mask": mask.tolist(), "patch": patch.tolist(), "activation": act}, 200, {'Content-Type':'application/json'} 
    else:
        return "No state found", 404

def debugPower(config):
    if request.method != 'POST':
        return 'No config data', 400
    body = request.get_json(force = True, silent = True)
    if body is None:
        return 'No config data', 400
    
    state = getState(config, ["power"])
    if not state:
        return "Failed to get state", 500

    equation = ""
    for element in body["stateEquation"]:
        if element["type"] == "operator":
            if element["name"] not in "()notandor":
                return "Invalid operator", 400
            equation += f"{element['name']} "
        else:
            value = next((s["active"] for s in state["states"] if s["id"] == element["id"]), None)
            if value is None:
                return f"No state value found for {element['name']}", 400
            equation += f"{value} "
    
    testResult = {}
    try:
        result = eval(equation)
        testResult = {"success": True, "active": result}
    except Exception as ex:
        testResult = {"success": False, "error": str(ex)}
    return json.dumps(testResult), 200, {"Content-Type": "application/json"}     


def debugSetState(config):
    if request.method != 'POST':
        return 'No config data', 400
    body = request.get_json(force = True, silent = True)
    if body is None:
        return 'No config data', 400
    
    testResult = {}
    try:
        result = setState(config, body["state"], body["settings"])
        testResult = {"success": result is None, "error": result}
    except Exception as ex:
        print(ex)
        testResult = {"success": False, "error": str(ex)}
    return json.dumps(testResult), 200, {"Content-Type": "application/json"}   


def debugOCR(config):
    if request.method != 'POST':
        return 'No config data', 400
    view = request.get_json(force = True, silent = True)
    if view is None:
        return 'No config data', 400
    
    result, status = getCameraFrame()
    if status != 200:
        return result, status
    frame = result

    image = ReshapeImage(config, frame, view["shape"]["vertices"])
    image = prepareOCRImage(view, image)

    results = OCRModel(image, device="cpu", verbose=False, agnostic_nms=True)
    value = ""
    draw = ImageDraw.Draw(image)
    colors = ["#77DD77", "#836953", "#89cff0", "#99c5c4", "#9adedb", "#aa9499", "#aaf0d1", "#b2fba5"]
    for data in ([x.numpy() for x in sorted(results[0].boxes.data, key=lambda x: x[0])]):
        draw.rectangle([tuple(data[0:2]),tuple(data[2:4])], outline=np.random.choice(colors))
        value += str(int(data[5]))

    return {"image": np.asarray(image).tolist(), "value": value}, 200, {"Content-Type": "application/json"}


@app.route('/api/debug/<type>/<id>', methods=['POST', 'GET'])
@app.route('/api/debug/<type>/<id>/<targetId>', methods=['POST', 'GET'])
def getStateDebug(type, id, targetId = None):
    if not verifyToken():
        return "Unauthorized", 401
    try:        
        if not Path.isfile('./Data/Configs/'+id):
            return 'Config does not exists', 400
        f = open(f"./Data/Configs/{id}", 'rb')
        config = json.loads(f.read())
        f.close()

        if type == "state":
            return debugState(config, targetId)
        elif type == "power":
            return debugPower(config)
        elif type == "setstate":
            return debugSetState(config)
        elif type == "ocr":
            return debugOCR(config)
        else:
            return 'Bad debug type', 400 
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500

def ReshapeImage(config, frame, vertices):
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

@app.route('/api/frame')
@app.route('/api/frame/<id>')
def frame(id = None):
    if not verifyToken():
        return "Unauthorized", 401
    try:                
        result, status = getCameraFrame()
        if status != 200:
            print(result)
            return result, status
        frame = result

        config = {}
        if id is not None:
            f = open(f"./Data/Configs/{id}", 'rb')
            config = json.loads(f.read())
            f.close()

        if "frame" in config and "position" in config["frame"] and "crop" in config["frame"]:
            image = ReshapeImage(config, frame, config["frame"]["crop"]["shape"]["vertices"])
            scale = max(max(255 / image.width, 1), max(150 / image.height, 1))
            image = image.resize((int(image.width * scale), int(image.height * scale)))
            buffer = io.BytesIO()
            image.save(buffer, "png")
            buffer.seek(0)
            return buffer, 200, {'Content-Type':'image/png'} 
        else:
            success, buffer = cv2.imencode(".png", frame)
            if not success:
               return "Error processing image", 500
            return bytes(buffer), 200, {'Content-Type':'image/png'}
        
    except Exception as ex:
        print(ex)
        return "Failed", 500

@app.route('/api/trigger/<config>/<id>')
def trigger(config, id):
    if not verifyToken():
        return "Unauthorized", 401
    if not Path.isfile('./Data/Configs/'+config):
        return 'Config does not exists', 400
    try:
        f = open('./Data/Configs/'+config, 'r')
        data = json.loads(f.read())
        buttons = [b for b in data['buttons'] if b['id'] == id]
        if not any(buttons):
            return 'Button does not exist', 400
        triggerIR(data['ir_config'], buttons[0]['action'])
        return "Success", 200
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/setstate/<config>', methods=["POST"])
def setState_API(config):
    if not verifyToken():
        return "Unauthorized", 401
    if not Path.isfile('./Data/Configs/'+config):
        return 'Config does not exists', 400
    try:
        f = open('./Data/Configs/'+config, 'r')
        config = json.loads(f.read())
        body = request.get_json(force = True, silent = True)
        if body is None:
            return 'No state data', 400
        setState(config, body)
        return "Success", 200
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/scheduleruns')
def getScheduleRuns():
    if not verifyToken():
        return "Unauthorized", 401
    try:
        f = open('./Data/scheduleRuns', 'rb')
        data = f.read()
        return data, 200, {'Content-Type':'application/json'}
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/test/authorize')
def testAuthorize():
    if not verifyToken():
        return "Unauthorized", 401
    else:
        return "Success", 200

def triggerIR(config, action):
    print(f"Trigger: [{config}] [{action}]")
    system(f"irsend SEND_ONCE {config} {action}")

def setupCamera():
    global camera, settings
    camera = cv2.VideoCapture(settings["cameraIndex"])
    if not camera:
        print("No camera detected")
    if camera.isOpened():
        camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)
        if "cameraExposure" in settings:
            camera.set(cv2.CAP_PROP_EXPOSURE, settings["cameraExposure"])

def getCameraFrame():
    # data = np.asarray(Image.open("C:\\Users\\eriko\\Pictures\\PXL_20230626_022707896.jpg"))
    # data = cv2.cvtColor(data, cv2.COLOR_RGB2BGR)
    # return data, 200
    global camera
    if not camera:
        print("Camera not initialized, attempting to connect")
        setupCamera()
        if not camera:
            return "Failed to connect camera", 500
        
    if not camera.isOpened():
        return "Failed open camera", 500
        
    if "cameraExposure" in settings:
        camera.set(cv2.CAP_PROP_EXPOSURE, settings["cameraExposure"])

    camera.read()
    success, frame = camera.read()
    if not success:
        return "Can't receive frame", 500
    
    return frame, 200

def verifyToken():
    if "token" in request.headers:
        for session in sessions:
            if session["token"] == request.headers["token"] and session["lastActivity"] + timedelta(minutes=60) > datetime.now():
                session["lastActivity"] = datetime.now()
                return True
    return False

def roundToMinutes(datetime):
    return datetime - timedelta(seconds=datetime.second, microseconds=datetime.microsecond)

def shouldRun(schedule, runs, checkDateTime):
    Days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    lastRun = None
    if schedule["id"] in runs:
        lastRun = roundToMinutes(runs[schedule["id"]])
    currentRun = roundToMinutes(checkDateTime)

    currentDOW = currentRun.weekday()
    nextClosestDate = None
    nextClosestDateIndex = None
    for index, day in enumerate(schedule["days"]):
        DOW = Days.index(day)
        dt = roundToMinutes(datetime.combine((currentRun + timedelta(days = DOW - currentDOW)).date(), time.fromisoformat(schedule["time"])))
        if DOW < currentDOW:
            dt = dt + timedelta(days=7)
        if dt >= currentRun and (nextClosestDate is None or dt < nextClosestDate):
            nextClosestDate = dt
            nextClosestDateIndex = index
    
    #print(currentRun, nextClosestDate, lastScheduledDateTime, lastRun)
    lastScheduledDOW = Days.index(schedule["days"][(nextClosestDateIndex - 1) % len(schedule["days"])])
    lastScheduledDateTime = roundToMinutes(datetime.combine((currentRun + timedelta(days = lastScheduledDOW - currentDOW)).date(), time.fromisoformat(schedule["time"])))
    if lastScheduledDOW > currentDOW:
        lastScheduledDateTime = lastScheduledDateTime - timedelta(days=7)    

    return ((nextClosestDate and nextClosestDate < checkDateTime + timedelta(seconds=60)) or 
            (abs(currentRun - lastScheduledDateTime) < timedelta(minutes=5) and (not lastRun or abs(lastRun - lastScheduledDateTime) > timedelta(minutes=5))))


def manageSessions():
    while True:
        Time.sleep(60)
        expiredSessions = []
        for session in sessions:
            if session["lastActivity"] + timedelta(minutes=60) < datetime.now():
                expiredSessions.append(session)
        for session in expiredSessions:
            sessions.remove(session)

def decodeObject(obj):
    for key,value in obj.items():
        obj[key] = datetime.fromisoformat(value)
    return obj

def manageSchedules():
    while True:
        Time.sleep(60)
        try:
            f = open("./Data/default", 'r')
            id = f.read()
            f.close()
            if not Path.isfile(f"./Data/Configs/{id}"):
                continue
            f = open(f"./Data/Configs/{id}", 'rb')
            config = json.loads(f.read())
            f = open("./Data/scheduleRuns", 'rb')
            runs = json.loads(f.read(), object_hook=decodeObject)
            f.close()

            updated = False
            checkDateTime = datetime.now()
            for schedule in config["schedules"]:
                if schedule["enabled"] and shouldRun(schedule, runs, checkDateTime):
                    setState(config, schedule["state"])
                    runs[schedule["id"]] = checkDateTime
                    updated = True

            activeRuns = {}
            for id in runs:
                if any(x for x in config["schedules"] if x["id"] == id):
                    activeRuns[id] = runs[id]
                else:
                    updated = True
            runs = activeRuns

            if updated:
                f = open("./Data/scheduleRuns", 'w')
                f.write(json.dumps(runs, default=str))
                f.close()
        except Exception as ex:
            print(ex)

if __name__ == '__main__':
    try:
        print("Starting Session Manager", flush=True)
        Thread(target=manageSessions).start()
        print("Starting Schedule Manager", flush=True)
        Thread(target=manageSchedules).start()
        print("Loading Settings", flush=True)
        f = open(f"./Data/settings", 'rb')
        settings = json.loads(f.read())
        f.close()
        print("Setting up camera", flush=True)
        setupCamera()
        print("Loading OCR Model", flush=True)
        OCRModel = YOLO('7seg.pt')
        app.run(host='0.0.0.0', port=3001, ssl_context=('cert.pem', 'key.pem'))     
    finally:
        if camera:
            camera.release()   

