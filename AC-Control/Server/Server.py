from flask import Flask, request, send_from_directory
from flask_cors import CORS
import re
from os import listdir, path as Path, system
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
import random


ILLEGAL_CHARS = r'\/\.\@\#\$\%\^\&\*\(\)\{\}\[\]\"\'\`\,\<\>\\'
fileNamePattern = re.compile(rf'[^{ILLEGAL_CHARS}]+')

class FlaskWrapper(Flask):
    def __init__(self):
        super().__init__(__name__, static_folder='../Client/build')

    def cleanup(self):
        global _Camera, DHT11Sensor
        print("Cleanup", flush=True)
        if _Camera:
            print("Release Camera", flush=True)
            _Camera.release()
        if DHT11Sensor:
            print("Release DHT11 Sensor", flush=True)
            DHT11Sensor.exit()

app = FlaskWrapper()
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'
sessions = []
lastLoginAttempt = datetime.now()
_Camera = None
_Debug = None
_State = None
OCRModels = {}
StateModels = {}
settings = {}
sensor = {}
DHT11Sensor = None
DEBUG = len(sys.argv) >= 2 and sys.argv[1] == 'debug'

if not(DEBUG):
    global board, adafruit_dht
    import board
    import adafruit_dht

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
        print(traceback.format_exc())
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
                print(traceback.format_exc())
                return "Failed", 500
            finally:
                f.close()
        return names, 200
    except Exception as ex:
        print(traceback.format_exc())
        return "Failed", 500
    
@app.route('/api/models')
def listModels():
    if not verifyToken():
        return "Unauthorized", 401
    try:
        types = [{"model": "OCR", "dir": "./Data/OCR"}, {"model": "State", "dir": "./Data/State"}]
        models = { "OCR": [], "State": []}
        for type in types:
            for file in ([f for f in listdir(type["dir"]) if Path.isfile(Path.join(type["dir"], f))]):
                models[type["model"]].append(file)
        return models, 200
    except Exception as ex:
        print(traceback.format_exc())
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
        print(traceback.format_exc())
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
        print(traceback.format_exc())
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
        print(traceback.format_exc())
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
        print(traceback.format_exc())
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
        print(traceback.format_exc())
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
        print(traceback.format_exc())
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/state/<id>')
@app.route('/api/state/<id>/<section>')
def getState(id, section = None):
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
        if section and section in ["states", "power", "ocr", "basic"]:
            sections = [section]
        state = _State.getState(config, sections)
        if state:
            return state, 200, {'Content-Type':'image'} 
        else:
            return "Failed to get state", 500
    except Exception as ex:
        print(traceback.format_exc())
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
        elif type == "schedule":
            return _Debug.debugSchedule(config, request)
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
        print(traceback.format_exc())
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
        Utility.AppendEvent("trigger", buttons[0]["name"])
        return "Success", 200
    except Exception as ex:
        print(traceback.format_exc())
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/setstate/<config>', methods=["POST"])
def setState(config):
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
        error = _State.setState(config, body)
        if error:
            return error, 500
        else:
            return "Success", 200
    except Exception as ex:
        print(traceback.format_exc())
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
        print(traceback.format_exc())
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/test/authorize')
def testAuthorize():
    if not verifyToken():
        return "Unauthorized", 401
    else:
        return "Success", 200

@app.route('/api/events')
def events():
    if not verifyToken():
        return "Unauthorized", 401
    try:
        f = open('./Data/events', 'rb')
        data = f.read()
        return data, 200, {'Content-Type':'application/json'}
    except Exception as ex:
        print(traceback.format_exc())
        return "Failed", 500
    finally:
        f.close()

@app.route('/api/reboot')
def reboot():
    if not verifyToken():
        return "Unauthorized", 401
    else:
        Thread(target=rebootSystem).start()
        return "Success", 200
    
@app.route('/api/restart')
def restart():
    if not verifyToken():
        return "Unauthorized", 401
    else:
        Thread(target=restartService).start()
        return "Success", 200

def restartService():
    Time.sleep(1)
    system("sudo systemctl restart ACControl.service")

def rebootSystem():
    Time.sleep(1)
    system("sudo reboot")

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
    currentRun = roundToMinutes(checkDateTime)   
    currentDOW = currentRun.weekday()

    if schedule["anytime"]:
        return Days[currentDOW] in schedule["days"]
    else:
        lastRun = None        
        scheduleTime = time.fromisoformat(schedule["time"])

        if schedule["id"] in runs and "lastRun" in runs[schedule["id"]]:
            lastRun = roundToMinutes(datetime.fromisoformat(runs[schedule["id"]]["lastRun"]))        

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

def checkCondition(schedule, config, state, errors):
        try:
            if len(schedule["conditionEquation"]) == 0:
                return True            

            equation = ""
            for element in schedule["conditionEquation"]:
                if element["type"] == "operator":
                    if element["name"] not in ["<", ">", "=","(",")","not","and","or"]:
                        raise Exception("Invalid operator")
                    equation += f"{element['name']} "
                elif element["type"] == "state":
                    value = next((s["active"] for s in state["states"] if s["id"] == element["id"]), None)
                    if value is None:
                        raise Exception(f"No state value found for {element['name']}")
                    equation += f"{value} "
                elif element["type"] == "sensor":
                    value = state[element["name"].lower()] if state["name"].lower() in sensor else None
                    if value is None:
                        raise Exception(f"No sensor value found for {element['name']}")
                    if element["name"].lower() == "temperature" and settings["temperatureUnit"] == "F":
                        value = value * (9 / 5) + 32
                    equation += f"{value} "
                elif element["type"] == "system":
                    if element["name"] == "On":
                        value = state["power"]
                    elif element["name"] == "On":
                        value = not state["power"]
                    else:
                        raise Exception(f"Invalid system value {element['name']}")
                    equation += f"{value} "
                elif element["type"] == "value":
                    try:
                        value = int(element["value"])
                        equation += f"{value} "
                    except:
                        raise Exception(f"Invalid value {element['value']}")

            result = eval(equation)
            return bool(result)
        except Exception as ex:
            errors.append(str(ex))
            return False        

def manageSessions():
    while True:
        Time.sleep(60)
        expiredSessions = []
        for session in sessions:
            if session["lastActivity"] + timedelta(minutes=60) < datetime.now():
                expiredSessions.append(session)
        for session in expiredSessions:
            sessions.remove(session)

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
            f.close()

            f = open("./Data/scheduleRuns", 'rb')
            runs = json.loads(f.read())
            f.close()
            
            state = _State.getState(config, ["states"] if any(any(o["type"] == "state" for o in s["conditionEquation"]) for s in config["schedules"]) else ["basic"])
            if not state:
                raise Exception("Failed to get state")
            
            updated = False
            checkDateTime = datetime.now()
            for schedule in config["schedules"]:
                errors = []
                if schedule["enabled"] and shouldRun(schedule, runs, checkDateTime) and checkCondition(schedule, config, state, errors):                    
                    updated = True
                    if schedule["id"] not in runs:
                        runs[schedule["id"]] = {}
                    runs[schedule["id"]]["lastAttempt"] = checkDateTime
                    start = datetime.now()
                    result = _State.setState(config, schedule["state"])
                    if result is None:
                        runs[schedule["id"]]["lastRun"] = checkDateTime                        
                    runs[schedule["id"]]["error"] = result + (", ".join(errors) if len(errors) > 0 else "")
                    runs[schedule["id"]]["duration"] = datetime.now() - start

            activeRuns = {}
            for id in runs:
                if any(x for x in config["schedules"] if x["id"] == id):
                    activeRuns[id] = runs[id]
                else:
                    updated = True
            runs = activeRuns

            if updated:
                with open("./Data/scheduleRuns", 'w') as f:
                    f.write(json.dumps(runs, default=str))
        except Exception as ex:
            print(traceback.format_exc())

def temperatureWorker():
    while True:
        if DEBUG:
            sensor["temperature"] = random.randint(10, 30)
            sensor["humidity"] = random.randint(40,80)
            Time.sleep(5)
        else:
            global DHT11Sensor
            DHT11Sensor = adafruit_dht.DHT11(board.D4)
            try:
                while True:
                    retries = 0            
                    while True:
                        try:
                            DHT11Sensor.measure()
                            if not DHT11Sensor._temperature or not DHT11Sensor._humidity:
                                raise RuntimeError("Empty sensor values")
                            sensor["temperature"] = DHT11Sensor._temperature
                            sensor["humidity"] = DHT11Sensor._humidity
                            break
                        except RuntimeError as error:
                            retries += 1
                            if retries > 5:
                                raise Exception("Maximum retries reached")
                            Time.sleep(1)
                            continue
                        except Exception as error:
                            print("temperatureWorker, Error: " + str(error), flush=True)
                            raise error                    
                    Time.sleep(5)
            finally:
                if DHT11Sensor:
                    DHT11Sensor.exit()

def historyWorker():
    while True:        
        Time.sleep(60)
        with open("./Data/events", 'rb') as f:
            try:
                data = json.loads(f.read())
            except Exception:
                print(traceback.format_exc())
                data = []

        with open("./Data/settings", 'rb') as f:
            settings = json.loads(f.read())
        
        newData = []
        pastCutoff = False        
        for event in data:
            if pastCutoff or datetime.now() - datetime.fromisoformat(event["time"]) < timedelta(days=settings["historyLength"]):
                pastCutoff = True
                newData.append(event)

        if not(DEBUG):
            newData.append({
                "time": datetime.now(),
                "type": "sensor",
                "value": sensor
            })
        else:
            newData.append({
                "time": datetime.now(),
                "type": "sensor",
                "value": { "temperature": random.randint(75,80), "humidity": random.randint(50, 65) }
            })

        with open("./Data/events", 'w') as f:
            f.write(json.dumps(newData, default=str))
        

def appStart():
    global _State, _Debug, _Camera, OCRModels, StateModels, settings
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
    print("Starting Temperature Manager", flush=True)
    Thread(target=temperatureWorker).start()
    print("Starting History Manager", flush=True)
    Thread(target=historyWorker).start()
    print("Loading Camera", flush=True)
    _Camera = Camera(settings)
    print("Loading OCR Models...", flush=True)
    dir = './Data/OCR'
    for file in ([f for f in listdir(dir) if Path.isfile(Path.join(dir, f))]):
        print("- "+file)
        OCRModels[file] = YOLO(Path.join(dir, file))
    
    print("Loading State Models...", flush=True)
    dir = './Data/State'
    for file in ([f for f in listdir(dir) if Path.isfile(Path.join(dir, f))]):
        print("- "+file)
        StateModels[file] = YOLO(Path.join(dir, file))

    _State = State(_Camera, OCRModels, StateModels, settings, sensor)
    _Debug = Debug(_Camera, OCRModels, StateModels, _State, settings, sensor)
    print("Loading Complete", flush=True)
    if DEBUG:
        try:
            app.run(host='0.0.0.0', port=3001, ssl_context=('cert.pem', 'key.pem'))    
        finally:
            app.cleanup()
    else:
        return app
    

if __name__ == '__main__':
    appStart()
    

