from flask import Flask, request
from flask_cors import CORS
from flask_socketio import SocketIO, send
import sys
import json
import traceback
import serial
import serial.tools.list_ports
from threading import Thread, Lock
import time as Time
import re
import uuid
import socket
from datetime import datetime

class FlaskWrapper(Flask):
    def __init__(self):
        super().__init__(__name__, static_folder='../Client/build')

    def cleanup(self):
        global socketRef
        print("Cleanup", flush=True)
        if socketRef:
            socketRef.close()
            print("Socket closed", flush=True)

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
lastAction = Time.time()
idleStowed = False
socketRef = None

statePattern = r'<(?P<state>\w+).*WPos:(?P<x>-?\d+\.\d{3}),(?P<y>-?\d+\.\d{3})'
settingsPattern = r'\$(?P<index>\d+)=(?P<value>[\d\.]+)'
movePattern = r'G0(\sX[\d\.]+)?(\sY[\d\.]+)?'
rotctlCommandPattern = r'(?P<command>\w+)\s*(?P<params>[^\\n]+)?'
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
        self.subscriberLock = Lock()

    def close(self):
        self.running = False
        if self.serialInterface is not None:
            self.serialInterface.close()

    def connect(self, port, baud, scan = False):
        self.serialInterface = serial.Serial(port, baud, timeout=0.1)
        self.running = True
        self.scan = scan
        Thread(target=self._read).start()

    def _read(self):
        try:
            while self.running and self.serialInterface is not None and self.serialInterface.is_open:
                data = self.serialInterface.readline().decode()
                if len(data) > 0:
                    callbacks = []
                    with self.subscriberLock:
                        for s in self.subscribers:
                            if self.subscribers[s].type == 'r':
                                callbacks.append(self.subscribers[s].callback)
                    for callback in callbacks:
                        Thread(target=callback, args=[data]).start()
                    
        except Exception as ex:
            if self.running and not self.scan:
                print(traceback.format_exc(), flush=True)

        
    def subscribe(self, type, callback):
        if re.search(typePattern, type) is None:
            raise Exception("Invalid type")

        id = str(uuid.uuid4())
        self.subscribers[id] = Subscriber(id, type, callback)

        return id

    def unsubscribe(self, id):
        if id in self.subscribers:
            with self.subscriberLock:
                del self.subscribers[id]

    def write(self, data):
        if self.serialInterface is not None:
            self.serialInterface.write((data + '\n').encode())
            callbacks = []
            with self.subscriberLock:
                for s in self.subscribers:
                    if self.subscribers[s].type == 'w':
                        callbacks.append(self.subscribers[s].callback)
            for callback in callbacks:
                Thread(target=callback, args=[data]).start()

    def __del__(self):
        self.close()
        
class SerialBuffer:
    def __init__(self, serialManager):
        self.buffer = []
        self.serialManager = serialManager
        self.id = serialManager.subscribe('r', self._readEvent)

    def _readEvent(self, data):
        self.buffer.append(data)

    def readline(self, timeout = 1):
        start = Time.perf_counter()
        while len(self.buffer) == 0:
            Time.sleep(0.1)
            if Time.perf_counter() - start > timeout:
                return ""
        
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

@app.route('/api/set/settings', methods=["POST"])
def setSettings():
    try:
        body = request.get_json(force = True, silent = True)
        for x in settings:
            if x in body:
                settings[x] = body[x]

        saveSettings()
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500


@app.route('/api/get/ports', methods=["GET"])
def getPorts():
    try:
        ports = serial.tools.list_ports.comports()
        return [x.device for x in ports], 200, {'Content-Type':'application/json'}
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
@app.route('/api/set/baudport', methods=["POST"])
def setBaudPort():
    try:
        body = request.get_json(force = True, silent = True)
        settings["port"] = body["port"]
        settings["baud"] = body["baud"]
        saveSettings()
        reconnect()
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500


@app.route('/api/scan/baud', methods=["GET"])
def scanBaud():
    global manager
    found = None
    with SerialBuffer(manager) as buffer:
        for baud in baudRates:        
            try:
                sendRead(f"SCAN: {baud}")
                buffer.clear()
                manager.close()
                manager.connect(settings["port"], baud, scan=True)
                Time.sleep(1)
                start = Time.perf_counter()
                while Time.perf_counter() - start < 2:
                    manager.write('?')
                    line = buffer.readline(0.5)
                    if re.search(statePattern, line) is not None:
                        sendRead("SUCCESS")
                        found = baud
                        settings["baud"] = baud
                        saveSettings()
                        reconnect()
                        break 
                if found is not None:
                    break
            except:
                pass
    if found is None:
        sendRead(f"NO MATCH")
    return str(found), 404 if found == "" else 200

    
@app.route('/api/get/baud', methods=["GET"])
def getBaudRates():
    try:
       return baudRates, 200, {'Content-Type':'application/json'}
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500   
    
@app.route('/api/set/position', methods=["POST"])
def setPosition():    
    try:
        body = request.get_json(force = True, silent = True)
        setRotorPosition(body["coordinates"]["x"], body["coordinates"]["y"], body["absolute"])
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
def setRotorPosition(xpos, ypos, absolute):
    global position, lastAction

    lastAction = Time.time()
    readPosition()    

    if absolute:
        diff = xpos - (position["x"] % 360)
        sign = 1 if diff == 0 else abs(diff) / diff
        nsign = -1 * sign
        x = round((diff if abs(diff) <= 180 else (360 - abs(diff)) * nsign) + position["x"], 3)
        y = max(min(ypos, 180), 0)
    else:
        x = xpos + position["x"]             
        y = max(min(ypos + position["y"], 180), 0)

    manager.write(f"$7=255")
    manager.write(f"G0 X{x} Y{y}")
    
@app.route('/api/get/position', methods=["GET"])
def getPosition():
    global position, lastAction
    try:
        lastAction = Time.time()
        readPosition()
        return {'x': round(position['x'] % 360, 3), 'y': round(position['y'], 3)}, 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    

@app.route('/api/reset/serial', methods=["GET"])
def resetSerial():
    global lastAction
    try:
        lastAction = Time.time()
        manager.write('crtl-x')
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/send/command', methods=["POST"])
def sendCommand():
    global manager, lastAction
    try:
        lastAction = Time.time()
        body = request.get_json(force = True, silent = True)        
        manager.write(body["command"])
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/home', methods=["GET"])
def home():
    global lastAction
    try:
        lastAction = Time.time()
        setRotorPosition(0, 10, False)
        readPosition()
        manager.write('$H')
        start = Time.perf_counter()
        with SerialBuffer(manager) as buffer:
            while (Time.perf_counter() - start) < 15:
                manager.write('?')
                line = buffer.readline(1)
                buffer.clear()
                if len(line) > 0:
                    break
        manager.write('G0 X0 Y0')
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
@app.route('/api/toggle/motors', methods=["GET"])
def toggleMotors():
    global manager, position, grblSettings, lastAction
    try:
        lastAction = Time.time()
        getState()
        manager.write(f"$7={25 if grblSettings[7] == 255 else 255}")
        manager.write(f"G0 X{position['x']} Y{position['y']}")
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/reset', methods=["GET"])
def resetController():
    global lastAction
    try:
        lastAction = Time.time()
        reconnect()
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/stow', methods=["GET"])
def stow():
    global lastAction
    try:
        lastAction = Time.time()
        stowRotor()
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

def stowRotor(): 
    global idleStowed
    if not idleStowed:
        setRotorPosition(0, 10, False)
        setRotorPosition(settings["stowPosition"], 10, True)
        readPosition() 
        manager.write(f"$7=25")
        manager.write(f"G0 Y0")
        idleStowed = True

def saveSettings():
    with open(f"./Data/settings", 'w') as f:
        f.write(json.dumps(settings))

def sendRead(line): 
    socketio.emit("message", json.dumps({"type": "read", "data": line.rstrip()}))

def sendPosition(line): 
    match = re.search(statePattern, line)
    if match is not None:
        socketio.emit("message", json.dumps({"type": "position", "data": { "x": round(float(match.group('x')) % 360, 3), "y": round(float(match.group('y')), 3)}}))

def sendWrite(line): 
    socketio.emit("message", json.dumps({"type": "write", "data": line.rstrip()}))

def watchMove(line): 
    global idleStowed
    match = re.search(movePattern, line)
    if match is not None:
        idleStowed = False
        readPosition()

def initSerialManager():
    global manager
    try:
        if manager is not None:
            manager.close()

        manager = SerialManager()
        manager.subscribe("r", sendRead)
        manager.subscribe("r", sendPosition)
        manager.subscribe("w", sendWrite)
        manager.subscribe("w", watchMove)
        #manager.subscribe("r", lambda x: print(x.rstrip()))
        #manager.subscribe("w", lambda x: print("> " + x.rstrip()))
        manager.connect(settings['port'], settings['baud'])
    except Exception as ex:
        print(traceback.format_exc(), flush=True)

def reconnect():
    global manager
    try:
        manager.close()
        Time.sleep(0.1)
        manager.connect(settings['port'], settings['baud'])
        sendRead(f"CONNECT: {settings['port']} @ {settings['baud']}")
    except Exception as ex:
        sendRead(str(ex))

def getPositionWithOffset():
    global position, settings
    return {"x": (position["x"] - settings["offset"]) % 360, "y": position["y"]}

def readPosition(wait = True):
    global manager, position
    with SerialBuffer(manager) as buffer:
        start = Time.perf_counter()
        while True:
            Time.sleep(0.5)
            manager.write('?')
            line = buffer.readline(0.5)
            buffer.clear()
            match = re.search(statePattern, line)
            if match is not None:
                start = Time.perf_counter()
                position['x'] = float(match.group('x'))
                position['y'] = float(match.group('y'))
                if match.group('state') == 'Idle' or not wait:                    
                    break
            if Time.perf_counter() - start > 2:
                sendRead("NO RESPONSE")
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
    
def idleMonitor():
    global lastAction, idleStowed
    while True:
        Time.sleep(1)
        if (Time.time() - lastAction) > settings["idleTimeout"]:
            if not idleStowed:
                sendRead("IDLE STOW")
                stowRotor()
            

def socketSend(client, data):
    #print(data)
    client.send(str(data).encode())

def initSocket():
    global position, grblSettings, socketRef
    socketRef = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        port = 3002
        socketRef.bind(('', port)) 
        socketRef.listen(5) 
        print("Socket Connected: " + str(port), flush=True)
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return
    
    while True: 
        client, address = socketRef.accept()         
        try:
            while True:
                data = client.recv(1024)
                if not data:
                    break

                data = data.decode().rstrip()
                print(f'Socket: {address[0]} [{datetime.now().strftime("%d/%b/%Y %H:%M:%S")}] {data}')
                match = re.search(rotctlCommandPattern, data)
                if match is None:
                    break

                command = match.group('command')
                params = match.group('params')
                if command == 'p': # get position
                    #readPosition(False)
                    pos = getPositionWithOffset()
                    socketSend(client, f'{pos["x"]}\n{pos["y"]}')
                elif command == 'P': # set position
                    pos = params.split(" ")
                    setRotorPosition((float(pos[0]) + settings["offset"]) % 360, float(pos[1]), True)
                    socketSend(client, f"RPRT 0")
        except Exception as ex:
            print(traceback.format_exc(), flush=True)
        finally:
            client.close()
            print("Client closed", flush=True)
        
            
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

    print("Starting Idle Monitor", flush=True)
    Thread(target=idleMonitor).start()

    print("Starting Socket Monitor", flush=True)
    Thread(target=initSocket).start()

    if manager.running:
        readPosition()

    print("Loading Complete", flush=True)
    if DEBUG:
        try:
            #socketio.run(app, host='0.0.0.0', port=3001, ssl_context='adhoc')    
            socketio.run(app, host='0.0.0.0', port=3001)    
        finally:
            app.cleanup()
    else:
        return socketio
    

if __name__ == '__main__':
    appStart()