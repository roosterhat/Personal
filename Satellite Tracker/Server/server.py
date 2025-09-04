from flask import Flask, request
from flask_cors import CORS
from flask_socketio import SocketIO, send
import sys
import json
import traceback
import serial
import serial.tools.list_ports
from threading import Thread
import time as Time
import re
import uuid

class FlaskWrapper(Flask):
    def __init__(self):
        super().__init__(__name__, static_folder='../Client/build')

    def cleanup(self):
        print("Cleanup", flush=True)

app = FlaskWrapper()
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'
app.config['SECRET_KEY'] = 'secret!'
socketio = SocketIO(app)
socketio.init_app(app, cors_allowed_origins="*")
DEBUG = len(sys.argv) >= 2 and sys.argv[1] == 'debug'

settings = {}
position = { 'x': 0, 'y': 0 }
grblSettings = [0] * 23
manager = None

statePattern = r'<(?P<state>\w+).*WPos:(?P<x>-?\d+\.\d{3}),(?P<y>-?\d+\.\d{3})'
settingsPattern = r'\$(?P<index>\d+)=(?P<value>[\d\.]+)'
typePattern = r'[rw]'

baudRates = [4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600]

class Subscriber:
    def __init__(self, id, type, callback):
        self.id = id
        self.type = type
        self.callback = callback

class SerialManager:
    def __init__(self):
        self.serialInterface = None
        self.subscribers = {}
        self.running = False

    def close(self):
        self.running = False
        if self.serialInterface is not None:
            self.serialInterface.close()

    def connect(self, port, baud):
        self.serialInterface = serial.Serial(port, baud, timeout=0.1)
        self.running = True
        Thread(target=self._read).start()

    def _read(self):
        try:
            while self.running:
                if self.serialInterface is None:
                    break

                data = self.serialInterface.readline().decode()

                if len(data) > 0:
                    for s in self.subscribers:
                        if self.subscribers[s].type == 'r':
                            self.subscribers[s].callback(data)
        except Exception as ex:
            print(traceback.format_exc(), flush=True)

        
    def subscribe(self, type, callback):
        if re.search(typePattern, type) is None:
            raise Exception("Invalid type")

        id = str(uuid.uuid4())
        self.subscribers[id] = Subscriber(id, type, callback)

        return id

    def unsubscribe(self, id):
        if id in self.subscribers:
            del self.subscribers[id]

    def write(self, data):
        if self.serialInterface is not None:
            self.serialInterface.write((data + '\n').encode())
            for s in self.subscribers:
                if self.subscribers[s].type == 'w':
                    self.subscribers[s].callback(data)

    def __del__(self):
        self.close()
        
class SerialBuffer:
    def __init__(self, serialManager):
        self.buffer = []
        self.serialManager = serialManager
        self.id = serialManager.subscribe('r', self._readEvent)

    def _readEvent(self, data):
        self.buffer.append(data)

    def readline(self):
        while len(self.buffer) == 0:
            Time.sleep(0.1)
        
        return self.buffer.pop(0)
    
    def clear(self):
        self.buffer = []

    def dispose(self):
        if self.id is not None:
            self.serialManager.unsubscribe(self.id)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self.dispose()

    def __del__(self):
        self.dispose()

@app.route('/api/get/settings', methods=["GET"])
def getSettings():
    try:
        f = open(f"./Data/settings", 'r')
        data = json.loads(f.read())
        return data, 200, {'Content-Type':'application/json'} 
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    finally:
        f.close()


@app.route('/api/get/ports', methods=["GET"])
def getPorts():
    try:
        ports = serial.tools.list_ports.comports()
        return [x.device for x in ports], 200, {'Content-Type':'application/json'}
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
@app.route('/api/set/port', methods=["POST"])
def setPort():
    try:
        body = request.get_json(force = True, silent = True)
        settings["port"] = body["port"]
        saveSettings()
        reconnect()
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500


@app.route('/api/scan/baud', methods=["GET"])
def scanBaud():
    global manager
    found = ""
    with SerialBuffer(manager) as buffer:
        for baud in baudRates:        
            try:
                settings["baud"] = baud
                manager.write('$')
                line = buffer.readline()
                if re.search(statePattern, line) != None:
                    found = baud
                    setBaudRate(baud)
                    break 
            except:
                pass
        
    return found, 404 if found == "" else 200

    
@app.route('/api/get/baud', methods=["GET"])
def getBaudRates():
    try:
       return baudRates, 200, {'Content-Type':'application/json'}
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/set/baud', methods=["POST"])
def setBaudRate():
    try:
        body = request.get_json(force = True, silent = True)
        settings["baud"] = body["baud"]
        saveSettings()
        reconnect()
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500       
    
@app.route('/api/set/position', methods=["POST"])
def setPosition():
    global position
    try:
        readPosition()
        body = request.get_json(force = True, silent = True)

        if body["absolute"]:
            diff = body["coordinates"]["x"] - (position["x"] % 360)
            sign = 1 if diff == 0 else abs(diff) / diff
            nsign = -1 * sign
            x = round((diff if abs(diff) <= 180 else (360 - abs(diff)) * nsign) + position["x"], 3)
            y = max(min(body["coordinates"]["y"], 180), 0)
        else:
            x = body["coordinates"]["x"] + position["x"]             
            y = max(min(body["coordinates"]["y"] + position["y"], 180), 0)

        manager.write(f"$7=255")
        manager.write(f"G0 X{x} y{y}")
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
@app.route('/api/get/position', methods=["GET"])
def getPosition():
    global position
    try:
        readPosition()
        return {'x': round(position['x'] % 360, 3), 'y': round(position['y'] % 360, 3)}, 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    

@app.route('/api/reset/serial', methods=["GET"])
def resetSerial():
    try:
        manager.write('crtl-x')
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/send/command', methods=["POST"])
def sendCommand():
    global manager
    try:
        body = request.get_json(force = True, silent = True)        
        manager.write(body["command"])
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/home', methods=["GET"])
def home():
    try:
        manager.write('$H')
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
@app.route('/api/toggle/motors', methods=["GET"])
def toggleMotors():
    global manager, position, grblSettings
    try:
        getState()
        manager.write(f"$7={25 if grblSettings[7] == 255 else 255}")
        manager.write(f"G0 X{position['x']} Y{position['y']}")
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/reset', methods=["GET"])
def resetController():
    try:
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

def saveSettings():
    with open(f"./Data/settings", 'w') as f:
        f.write(json.dumps(settings))

def sendRead(line): 
    socketio.emit("message", json.dumps({"type": "read", "data": line.rstrip()}))

def sendWrite(line): 
    socketio.emit("message", json.dumps({"type": "write", "data": line.rstrip()}))

def initSerialManager():
    global manager
    try:
        if manager is not None:
            manager.close()

        manager = SerialManager()
        manager.subscribe("r", sendRead)
        manager.subscribe("w", sendWrite)
        #manager.subscribe("r", lambda x: print(x.rstrip()))
        #manager.subscribe("w", lambda x: print("> " + x.rstrip()))
        manager.connect(settings['port'], settings['baud'])
    except Exception as ex:
        print(traceback.format_exc(), flush=True)

def reconnect():
    global manager
    manager.close()
    manager.connect(settings['port'], settings['baud'])

def readPosition(wait = True):
    global manager, position
    with SerialBuffer(manager) as buffer:
        while True:
            Time.sleep(0.25)
            manager.write('?')
            line = buffer.readline()
            buffer.clear()
            match = re.search(statePattern, line)
            if match is not None:
                if match.group('state') == 'Idle' or not wait:
                    position['x'] = float(match.group('x'))
                    position['y'] = float(match.group('y'))
                    break

def readState(): 
    global manager, grblSettings
    with SerialBuffer(manager) as buffer: 
        manager.write('$$')
        while True: 
            line = buffer.readline().rstrip()
            
            if line == 'ok' or line == '':
                break

            match = re.search(settingsPattern, line)
            if match is not None:
                grblSettings[int(match.group('index'))] = float(match.group('value'))

def getState(wait = False):
    readPosition(wait)
    readState()
    
    
def appStart():
    global settings, manager
    print("Loading Settings", flush=True)
    f = open(f"./Data/settings", 'rb')
    settings = json.loads(f.read())
    f.close()
    if not settings:
        exit("Failed to load settings")

    print("Initialize Serial Manager", flush=True)
    initSerialManager()    

    print("Loading Complete", flush=True)
    if DEBUG:
        try:
            socketio.run(app, host='0.0.0.0', port=3001)    
        finally:
            app.cleanup()
    else:
        return socketio
    

if __name__ == '__main__':
    appStart()