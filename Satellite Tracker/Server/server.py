from flask import Flask, request, send_from_directory
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
import math
from os import path as Path
import shapely
from shapely import Polygon, Point, LineString
from collections.abc import Iterable

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
lastAction = Time.time()
lastHome = Time.time()
manager = None
idleStowed = False
socketRef = None
readPositionLock = Lock()
homingLock = Lock()
grid = None
zones = []

statePattern = r'<(?P<state>\w+).*WPos:(?P<x>-?\d+\.\d{3}),(?P<y>-?\d+\.\d{3})'
settingsPattern = r'\$(?P<index>\d+)=(?P<value>[\d\.]+)'
rotctlCommandPattern = r'(?P<command>\w+)\s*(?P<params>[^\\n]+)?'
movePattern = r'G[01] (?:(?:(?:X(?P<x>[\-\d\.]+))|(?:Y(?P<y>[\-\d\.]+))|(?:[A-Z][\-\d\.]+))\s*)+'
typePattern = r'[rw]'

baudRates = [4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 921600]

class Subscriber:
    def __init__(self, id, type, callback):
        self.id = id
        self.type = type
        self.callback = callback
        self.connected = False

class SerialManager:
    def __init__(self):
        self.serialInterface = None
        self.subscribers = {}
        self.running = False
        self.subscriberLock = Lock()
        self.writeLock = Lock()

    def close(self):
        self.running = False
        if self.serialInterface is not None:
            self.serialInterface.close()

    def connect(self, port, baud, scan = False):
        self.serialInterface = serial.Serial(port, baud, timeout=0.1)
        self.running = True
        self.connected = True
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
                self.connected = False
                print(traceback.format_exc(), flush=True)

        
    def subscribe(self, type, callback):
        if re.search(typePattern, type) is None:
            raise Exception("Invalid type")

        id = str(uuid.uuid4())
        with self.subscriberLock:
            self.subscribers[id] = Subscriber(id, type, callback)

        return id

    def unsubscribe(self, id):
        if id in self.subscribers:
            with self.subscriberLock:
                del self.subscribers[id]

    def write(self, data):
        if self.serialInterface is not None:
            with self.writeLock:
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

@app.route('/', defaults={'path': ''})
@app.route('/<path:path>')
def serve(path):
    if path != "" and Path.exists(app.static_folder + '/' + path):
        return send_from_directory(app.static_folder, path)
    else:
        return send_from_directory(app.static_folder, 'index.html')

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
        loadSettings()
        sendRead("SETTINGS SAVED")
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
    
@app.route('/api/get/position', methods=["GET"])
def getPosition():
    global position, lastAction
    try:
        readPositionUntilIdle()
        return {'x': round(position['x'] % 360, 3), 'y': round(position['y'], 3)}, 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    

@app.route('/api/reset/serial', methods=["GET"])
def resetSerial():
    global lastAction
    try:
        manager.write('crtl-x')
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/send/command', methods=["POST"])
def sendCommand():
    global manager, lastAction
    try:
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
        with homingLock:
            setRotorPosition(0, 10, False, keepOutOverride=True)
            readPositionUntilIdle()
            manager.write('$H')
            Time.sleep(0.5)
            start = Time.perf_counter()
            with SerialBuffer(manager) as buffer:
                while (Time.perf_counter() - start) < 30:
                    manager.write('?')
                    line = buffer.readline(1)
                    buffer.clear()
                    if len(line) > 0:
                        Time.sleep(0.5)
                        manager.write("G0 X0 Y0")
                        break        
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
@app.route('/api/toggle/motors', methods=["GET"])
def toggleMotors():
    global manager, position, grblSettings, lastAction
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
    global lastAction
    try:
        reconnect()
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/stow', methods=["GET"])
def stow():
    global lastAction
    try:
        stowRotor()
        return "", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500


def search(grid, init, goal):
    DIRECTIONS = [(0,1), (1,0), (0,-1), (-1,0), (1,1), (-1,1), (1,-1), (-1,-1)]
    heuristic = lambda x,y: math.dist((x,y), goal) * (3 if grid[x][y] else 1)

    R = int(len(grid) / 2)
    H = len(grid[0])
    closed = [[0 for col in range(len(grid[0]))] for row in range(len(grid) + 1)]     
    action = [[0 for col in range(len(grid[0]))] for row in range(len(grid) + 1)]
    closed[init[0]][init[1]] = 1

    cell = [[heuristic(init[0], init[1]), 0, init[0], init[1]]]

    while True:
        if len(cell) == 0:
            global settings, position
            print("position: " + str(position), flush=True)
            print("init: " + str(init), flush=True)
            print("goal: " + str(goal), flush=True)
            print("settings: " + str(settings), flush=True)
            raise ValueError("Algorithm is unable to find solution")
        else:  
            cell.sort(key=lambda x: x[0], reverse=True)
            next_cell = cell.pop()
            x = next_cell[2]
            y = next_cell[3]
            g = next_cell[1]

            if x == goal[0] and y == goal[1]:
                break
            else:
                for i in range(8):
                    x2 = x + DIRECTIONS[i][0]
                    y2 = y + DIRECTIONS[i][1]
                    if (-R <= x2 <= R and 0 <= y2 < H and not closed[x2 + R][y2]):
                        g2 = g + 10
                        f2 = heuristic(x2, y2)
                        #f2 = g2 + heuristic(x2, y2)
                        cell.append([f2, g2, x2, y2])
                        closed[x2 + R][y2] = 1
                        action[x2 + R][y2] = i
    invpath = []
    x = goal[0]
    y = goal[1]
    invpath.append([x, y])
    while x != init[0] or y != init[1]:
        x2 = x - DIRECTIONS[action[x + R][y]][0]
        y2 = y - DIRECTIONS[action[x + R][y]][1]
        x = x2
        y = y2
        invpath.append([x, y])

    path = []
    for i in range(len(invpath)):
        path.append(invpath[len(invpath) - 1 - i])

    return path

def smooth_path(path, grid):
    if not path:
        return []
    smoothed = [path[0]]
    for i in range(2, len(path)):
        if not has_obstacle(smoothed[-1], path[i], grid):
            continue  # skip middle point
        smoothed.append(path[i-1])
    smoothed.append(path[-1])
    return smoothed

def has_obstacle(p1, p2, grid):
    x1, y1 = p1
    x2, y2 = p2
    dx = abs(x2 - x1)
    dy = abs(y2 - y1)
    sx = 1 if x1 < x2 else -1
    sy = 1 if y1 < y2 else -1
    err = dx - dy
    while (x1, y1) != (x2, y2):
        if grid[x1][y1]:
            return True
        e2 = 2 * err
        if e2 > -dy:
            err -= dy
            x1 += sx
        if e2 < dx:
            err += dx
            y1 += sy
    return False

def getMoves(x, y, keepOutOverride):
    global position, grid, zones

    moves = []    
    if keepOutOverride or (x == position["x"] and y == position["y"] or len(zones) == 0) or not settings["usePathing"]: 
        moves = [{"x": x, "y": y}]
    else:
        pos = [position["x"], position["y"]]
        lines = []
        normX = x % 360
        normPos = pos[0] % 360
        offset = normX - x 
        diff = abs(normPos - normX)
        if(normPos > normX):
            pos[0] = (-(360 - normPos) if diff > 180 else normPos)
        else:
            normX = (-(360 - normX) if diff > 180 else normX)
            pos[0] = normPos

        try:
            path = smooth_path(search(grid, (round(pos[0]), round(pos[1])), (round(normX), round(y))), grid)
            for i in range(len(path) - 1):
                lines.append(LineString((path[i], path[i + 1])))
        except Exception as ex:
            print(ex, flush=True)
            print(traceback.format_exc(), flush=True)
            #lines = [LineString([pos, (x, y)])]


        for line in lines:
            inter = None
            for zone in zones:
                inter = zone.intersection(line)
                if not inter.is_empty:
                    break
                
            if inter is None or inter.is_empty:
                moves.append({"x": line.coords[-1][0] - offset, "y": line.coords[-1][1]})
            else:
                if inter.geom_type == "LineString":
                    moves.append({"x": inter.coords[-1][0] - offset, "y": inter.coords[-1][1]})
                elif inter.geom_type == "MultiPoint":
                    moves.append({"x": inter.geoms[0].x - offset, "y": inter.geoms[0].y})
                elif inter.geom_type == "Point":
                    moves.append({"x": inter.x - offset, "y": inter.y})
                break

    for move in moves:
        diff = move["x"] - (position["x"] % 360)
        sign = 1 if diff == 0 else abs(diff) / diff
        nsign = -1 * sign
        move["x"] = (diff if abs(diff) <= 180 else (360 - abs(diff)) * nsign) + position["x"]

    return moves

def setRotorPosition(xpos, ypos, absolute, feedRate = None, keepOutOverride = False):
    global position, lastAction, settings

    readPositionUntilIdle()
    if absolute:
        x = xpos
        y = max(min(ypos, 180), 0)
    else:
        x = xpos + position["x"]             
        y = max(min(ypos + position["y"], 180), 0)    

    moves = getMoves(x, y, keepOutOverride)

    if settings["displayPath"]:
        socketio.emit("message", json.dumps({"type": "path", "data": [position] + moves}))

    for move in moves:        
        manager.write(f"$7=255")
        if feedRate is None:
            manager.write(f"G0 X{round(move['x'], 3)} Y{round(move['y'], 3)}")
        else:
            manager.write(f"G1 X{round(move['x'], 3)} Y{round(move['y'], 3)} F{feedRate}")
        if settings["waitWhilePathing"]:
            readPositionUntilIdle()

def stowRotor(): 
    global idleStowed
    if not idleStowed:
        setRotorPosition(settings["stowPosition"], 0, True)
        readPositionUntilIdle()
        manager.write(f"$7=25")
        manager.write(f"G0 Y0")
        idleStowed = True
        readPositionUntilIdle()
        socketio.emit("message", json.dumps({"type": "target", "data": position}))

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
    global idleStowed, lastAction
    match = re.search(movePattern, line)
    if match is not None:
        idleStowed = False
        lastAction = Time.time()
        socketio.emit("message", json.dumps({"type": "target", "data": { "x": round(float("0" if match.group('x') is None else match.group('x')) % 360, 3), "y": round(float("0" if match.group('y') is None else match.group('y')), 3)}}))
        readPositionUntilIdle()

def reconnect():
    global manager
    try:
        manager.close()
        Time.sleep(0.1)
        manager.connect(settings['port'], settings['baud'])
        sendRead(f"CONNECT: {settings['port']} @ {settings['baud']}")
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        sendRead("ERROR CONNECTING")

def getPositionWithOffset():
    global position, settings
    return {"x": (position["x"] - settings["offset"]) % 360, "y": position["y"]}

def readPositionUntilIdle(wait = True):
    global manager, position
    if readPositionLock.acquire(timeout=0.1): # ensure only one thread is attempting to read position, others can get same outcome by waiting for lock
        try:
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
                        if match.group('state') == 'Idle':
                            Time.sleep(0.1) # allow serial response to propagate                    
                            return
                    if Time.perf_counter() - start > 2:
                        sendRead("NO RESPONSE")
                        return                    
        except Exception as ex:
            print(traceback.format_exc(), flush=True)

        finally:
            readPositionLock.release()
    elif wait:
        with readPositionLock:
            pass
        

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

def getState():
    readPositionUntilIdle()
    readState()
    
def idleMonitor():
    global lastAction, idleStowed, lastHome, manager
    while True:
        Time.sleep(1)
        if not manager.connected:
            lastAction = Time.time()
        elif (Time.time() - lastAction) > settings["idleTimeout"]:
            if not idleStowed:
                if(Time.time() - lastHome) > settings["homePeriod"] * 60:
                    sendRead("HOMING")
                    home()
                    lastHome = Time.time()
                sendRead("IDLE STOW")
                stowRotor()
            

def socketSend(client, data):
    #print(data)
    client.send(str(data).encode())

def initSocket():
    global position, grblSettings, socketRef
    socketRef = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        port = 4533
        socketRef.bind(('', port)) 
        socketRef.listen(5) 
        print("Socket open: " + str(port), flush=True)
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
                    pos = getPositionWithOffset()
                    socketSend(client, f'{pos["x"]}\n{pos["y"]}')
                elif command == 'P': # set position
                    paramPos = [float(x) for x in params.split(" ")]
                    pos = getPositionWithOffset()
                    paramPos[0] = (paramPos[0] + settings["offset"]) % 360
                    dist = math.dist(paramPos, [pos["x"], pos["y"]])
                    setRotorPosition(paramPos[0], paramPos[1], True, round(max(1500 * min(dist / 20, 1), 100), 3))
                    socketSend(client, "RPRT 0")
        except ConnectionResetError:
            pass
        except Exception as ex:
            print(traceback.format_exc(), flush=True)
        finally:
            client.close()
            print("Client closed", flush=True)
            
def initialHoming():
    sendRead("HOMING")
    home()

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

        if settings["homeOnStart"]:
            Thread(target=initialHoming).start()            

    except Exception as ex:
        print(traceback.format_exc(), flush=True)
    
def loadSettings():
    global settings, zones, grid
    f = open(f"./Data/settings", 'rb')
    settings = json.loads(f.read())
    f.close()

    zones = []
    grid = [[0 for y in range(90)] for x in range(-360, 360)]
    for zone in settings["keepOutZones"]:
        poly = Polygon((point['az'] - (360 if any(point['az'] > zone[(i + x) % len(zone)]['az'] + 180 for x in range(-1, 2)) else 0), point['el']) for i, point in enumerate(zone))
        zones.append(poly)
        bufferPoly = poly.buffer(2)
        for x in range(round(bufferPoly.bounds[0]), round(bufferPoly.bounds[2])):
            for y in range(round(bufferPoly.bounds[1]), round(bufferPoly.bounds[3])):
                if bufferPoly.contains(Point(x, y)):
                    if y >= 0:
                        grid[round(x)][round(y)] = 1

    res = shapely.union_all(zones)
    zones = [p.boundary for p in (res if isinstance(res, Iterable) else [res])]
            
def appStart():
    global settings, manager
    print("Loading Settings", flush=True)
    loadSettings()
    if not settings:
        exit("Failed to load settings")

    print("Initialize Serial Manager", flush=True)
    initSerialManager()    

    print("Starting Idle Monitor", flush=True)
    Thread(target=idleMonitor).start()

    print("Starting Socket Monitor", flush=True)
    Thread(target=initSocket).start()

    if manager.running:
        readPositionUntilIdle()

    print("Loading Complete", flush=True)
    if DEBUG:
        try:
            socketio.run(app, host='0.0.0.0', port=3001)    
        finally:
            app.cleanup()
    else:
        return app
    

if __name__ == '__main__':
    appStart()