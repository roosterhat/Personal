from flask import Flask, request, send_from_directory
from flask_cors import CORS
import re
from os import listdir, path as Path
import sys
import json
import uuid
import io
import cv2
from datetime import datetime, timedelta, time
import time as Time
from threading import Thread
import hashlib
from ultralytics import YOLO
from Debug import Debug
from Camera import Camera
from State import State
import Utility
import traceback

ILLEGAL_CHARS = r'\/\.\@\#\$\%\^\&\*\(\)\{\}\[\]\"\'\`\,\<\>\\'
fileNamePattern = re.compile(rf'[^{ILLEGAL_CHARS}]+')

app = Flask(__name__, static_folder='../Client/build')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'
sessions = []
lastLoginAttempt = datetime.now()
_Camera = None
_Debug = None
_State = None
OCRModels = {}
settings = {}

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
    
@app.route('/api/models')
def listModels():
    if not verifyToken():
        return "Unauthorized", 401
    try:
        names = []
        for file in ([f for f in listdir('./Data/OCR') if Path.isfile(Path.join('./Data/OCR', f))]):
            names.append(file)
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
        for key, value in body.items():
            if key not in ["password", "salt"]:
                settings[key] = value
        f = open(f"./Data/settings", 'w')
        f.write(json.dumps(settings))
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
        state = _State.getState(config, sections)
        if state:
            return state, 200, {'Content-Type':'image'} 
        else:
            return "Failed to get state", 500
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500

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
            return _Debug.debugState(config, targetId, request)
        elif type == "power":
            return _Debug.debugPower(config, request)
        elif type == "setstate":
            return _Debug.debugSetState(config, request)
        elif type == "ocr":
            return _Debug.debugOCR(config, request)
        else:
            return 'Bad _Debug type', 400 
    except Exception as ex:
        print(traceback.format_exc())
        return "Failed", 500

@app.route('/api/frame')
@app.route('/api/frame/<id>')
def frame(id = None):
    if not verifyToken():
        return "Unauthorized", 401
    try:                
        result, status = _Camera.getFrame()
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
            image = Utility.reshapeImage(config, frame, config["frame"]["crop"]["shape"]["vertices"])
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
        Utility.triggerIR(data['ir_config'], buttons[0]['action'])
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
        _State.setState(config, body)
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
        lastRun = roundToMinutes(datetime.fromisoformat(runs[schedule["id"]]["lastRun"]))
    currentRun = roundToMinutes(checkDateTime)
    scheduleTime = time.fromisoformat(schedule["time"])

    currentDOW = currentRun.weekday()
    nextClosestDate = None
    nextClosestDateIndex = None
    for index, day in enumerate(schedule["days"]):
        DOW = Days.index(day)
        dt = roundToMinutes(datetime.combine((currentRun + timedelta(days = DOW - currentDOW)).date(), scheduleTime))
        if DOW < currentDOW:
            dt = dt + timedelta(days=7)
        if dt >= currentRun and (nextClosestDate is None or dt < nextClosestDate):
            nextClosestDate = dt
            nextClosestDateIndex = index
    
    if not nextClosestDate:
        return False

    lastScheduledDOW = Days.index(schedule["days"][(nextClosestDateIndex - 1) % len(schedule["days"])])
    lastScheduledDateTime = roundToMinutes(datetime.combine((currentRun + timedelta(days = lastScheduledDOW - currentDOW)).date(), scheduleTime))
    if lastScheduledDOW >= currentDOW:
        lastScheduledDateTime = lastScheduledDateTime - timedelta(days=7)    

    return (nextClosestDate < checkDateTime + timedelta(seconds=60) or 
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
            runs = json.loads(f.read())
            f.close()

            updated = False
            checkDateTime = datetime.now()
            for schedule in config["schedules"]:
                if schedule["enabled"] and shouldRun(schedule, runs, checkDateTime):
                    updated = True
                    runs[schedule["id"]]["lastAttempt"] = checkDateTime
                    start = datetime.now()
                    result = _State.setState(config, schedule["state"])
                    if result is None:
                        runs[schedule["id"]]["lastRun"] = checkDateTime                        
                    runs[schedule["id"]]["error"] = result
                    runs[schedule["id"]]["duration"] = datetime.now() - start

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

def appStart():
    global _State, _Debug, _Camera, OCRModels, settings
    print("Loading Settings", flush=True)
    f = open(f"./Data/settings", 'rb')
    settings = json.loads(f.read())
    f.close()
    if not settings:
        exit("Failed to load settings")
    print("Starting Session Manager", flush=True)
    Thread(target=manageSessions).start()
    print("Starting Schedule Manager", flush=True)
    Thread(target=manageSchedules).start()
    print("Loading Camera", flush=True)
    _Camera = Camera(settings)
    print("Loading OCR Models...", flush=True)
    for file in ([f for f in listdir('./Data/OCR') if Path.isfile(Path.join('./Data/OCR', f))]):
        print("- "+file)
        OCRModels[file] = YOLO(Path.join('./Data/OCR', file))

    _State = State(_Camera, OCRModels, settings)
    _Debug = Debug(_Camera, OCRModels, _State)
    if len(sys.argv) >= 2 and sys.argv[1] == 'debug':
        app.run(host='0.0.0.0', port=3001, ssl_context=('cert.pem', 'key.pem'))     
    else:
        return app
    

if __name__ == '__main__':
    try:
        appStart()
    finally:
        if _Camera:
            _Camera.release()

