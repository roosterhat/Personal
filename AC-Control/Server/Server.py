from flask import Flask, request, send_from_directory
from flask_cors import CORS
import re
from os import listdir, path as Path, system
import json
import uuid
import io
import cv2
import numpy as np
from PIL import Image, ImageColor
from datetime import datetime, timedelta
import time
from threading import Thread
import hashlib
import math


ILLEGAL_CHARS = r'\/\.\@\#\$\%\^\&\*\(\)\{\}\[\]\"\'\`\,\<\>\\'
fileExtPattern = re.compile(r'\.(?P<ext>js|ico|css|png|jpg|html)$')
fileNamePattern = re.compile(rf'[^{ILLEGAL_CHARS}]+')
contentTypeMap = { 'js': 'text/javascript', 'ico': 'image/x-icon', 'css': 'text/css', 'png': 'image/png', 'jpg': 'image/jpg', 'html': 'text/html' }

app = Flask(__name__, static_folder='../Client/build')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'
camera = None
settings = None
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
        time.sleep((datetime.now() - lastLoginAttempt).seconds)

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
        f = open(f"./Data/settings", 'w')
        f.write(json.dumps(body))
        settings = body
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
        f = open(f"./Data/settings", 'rb')
        data = f.read()
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
    dC = pow((2 + rBar / 256) * (c1[0] - c2[0]) + 4 * pow(c1[1] - c2[1], 2) + (2 + (255 - rBar) / 256) * (c1[2] - c2[2]), 1/2)
    return 300 if math.isnan(dC) else dC

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
            if element["name"] not in ")(notandor":
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

def getState(config):
    currentState = {}
    result, status = getCameraFrame()
    if status != 200:
        return None
    frame = result

    if "states" in config["frame"]:
        currentState["states"] = config["frame"]["states"]
        for state in config["frame"]["states"]:
            state["active"] = sampleFrameEllipse(frame, state, config["frame"])
    
    # if "digits" in config["frame"]:
    #     currentState["digits"] = config["frame"]["digits"]
    #     for digit in currentState["digits"]:
    #         digit["value"] = None
    
    currentState["power"] = { "active": getPowerState(config, currentState) }
    return currentState

@app.route('/api/state/<id>')
def getState_API(id):
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
        state = getState(config)
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
    
    state = getState(config)
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
        else:
            return 'Bad debug type', 400 
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500

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
            scale = config["frame"]["position"]["scale"]
            points = np.array([[int(p["x"] / scale), int(p["y"] / scale)] for p in config["frame"]["crop"]["shape"]["vertices"]])
            (_,_), (width, height), a = cv2.minAreaRect(points)
            if a > 45:
                width, height = height, width
            dstPts = [[0, 0], [width, 0], [width, height], [0, height]]
            transform = cv2.getPerspectiveTransform(np.float32(points), np.float32(dstPts))
            out = cv2.warpPerspective(frame, transform, (int(width), int(height)))
            out = np.rot90(out, -config["frame"]["rotate"] / 90)
            out = cv2.cvtColor(out, cv2.COLOR_BGR2RGB)
            buffer = io.BytesIO()
            image = Image.fromarray(out)
            scale = max(max(255 / image.width, 1), max(150 / image.height, 1))
            image = image.resize((int(image.width * scale), int(image.height * scale)))
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

def stateChanged(newState, oldState):
    try:
        return any(newState[i]["active"] != oldState[i]["active"] for i in range(len(newState)))
    except:
        return True
    
def stateActive(states, id):
    return next((x["active"] for x in states if x["id"] == id), None)
    
def attemptSetPower(config, action, target):
    for i in range(settings["triggerAttempts"]):
        triggerIR(config["ir_config"], action)
        state = getState(config)
        if state and state["power"]["active"] == target:
            return True
        time.sleep(settings["frameRefreshDelay"] / 100)
    return False

def walkStateGroup(config, group, state, action):
    oldState = getState(config)
    if oldState and stateActive(oldState["states"], state["id"]):
        return True
    for _ in range(len(group["states"])):
        for _ in range(settings["triggerAttempts"]):
            time.sleep(settings["frameRefreshDelay"] / 100)
            triggerIR(config["ir_config"], action)
            newState = getState(config)
            if stateActive(newState["states"], state["id"]):
                return True
            if stateChanged(newState, oldState):
                break
            oldState = newState            
    return False
            

def setState(config, targetState):
    state = getState(config)
    if not state:
        return "Failed to get state"

    buttonMap = {}
    for b in config["buttons"]:
        buttonMap[b["id"]] = b["action"]
    stateMap = {}
    for g in config["actions"]["stateGroups"]:
        for s in g["states"]:
            stateMap[s["id"]] = g

    powerState = getPowerState(config, state)
    if targetState["power"]["active"]:
        if not powerState:
            if not attemptSetPower(config, buttonMap[config["actions"]["power"]["button"]], True):
                return f"Failed to set {config['actions']['power']['name']} [On]"
        for state in targetState["states"]:
            if state["id"] not in stateMap:
                return "State not associated with a group"
            group = stateMap[state["id"]]
            if walkStateGroup(config, group, state, buttonMap[group["button"]]):
                return f"Failed to set {state['name']} [On]"
        #set temperature
    else:
        if powerState:
            if not attemptSetPower(config, buttonMap[config["actions"]["power"]["button"]], False):
                return f"Failed to set {config['actions']['power']['name']} [Off]"


@app.route('/api/setstate/<config>', methods=["POST"])
def setState_API(config):
    #if not verifyToken():
    #    return "Unauthorized", 401
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

def manageSessions():
    while True:
        expiredSessions = []
        for session in sessions:
            if session["lastActivity"] + timedelta(minutes=60) < datetime.now():
                expiredSessions.append(session)
        for session in expiredSessions:
            sessions.remove(session)
        time.sleep(60)


if __name__ == '__main__':
    try:
        Thread(target=manageSessions)
        f = open(f"./Data/settings", 'rb')
        settings = json.loads(f.read())
        f.close()
        setupCamera()
        app.run(host='0.0.0.0', port=3001, ssl_context=('cert.pem', 'key.pem'))     
    finally:
        if camera:
            camera.release()   
