from flask import Flask, request, send_from_directory
from flask_cors import CORS
import re
from os import listdir, path as Path, system
import json
import uuid
import io
import cv2
import numpy as np
from PIL import Image

#https://www.digikey.com/en/maker/blogs/2021/how-to-send-and-receive-ir-signals-with-a-raspberry-pi#:~:text=The%20Raspberry%20Pi%20can%20receive,of%20the%20Arduino's%20PWM%20pins.

ILLEGAL_CHARS = r'\/\.\@\#\$\%\^\&\*\(\)\{\}\[\]\"\'\`\,\<\>\\'
fileExtPattern = re.compile(r'\.(?P<ext>js|ico|css|png|jpg|html)$')
fileNamePattern = re.compile(rf'[^{ILLEGAL_CHARS}]+')
contentTypeMap = { 'js': 'text/javascript', 'ico': 'image/x-icon', 'css': 'text/css', 'png': 'image/png', 'jpg': 'image/jpg', 'html': 'text/html' }

app = Flask(__name__, static_folder='../Client/build')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'
camera = None
settings = None

@app.route('/', defaults={'path': ''})
@app.route('/<path:path>')
def serve(path):
    if path != "" and Path.exists(app.static_folder + '/' + path):
        return send_from_directory(app.static_folder, path)
    else:
        return send_from_directory(app.static_folder, 'index.html')

@app.route('/api/list')
def listConfigs():
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
    try:
        if id == 'default':
            f = open("./Data/default", 'r')
            id = f.read()
            f.close()
            if not Path.isfile(f"./Data/Configs/{id}"):
                return "None", 404
        else:
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

@app.route('/api/frame')
@app.route('/api/frame/<id>')
def frame(id = None):
    try:        
        #image = open("C:\\Users\\eriko\\Pictures\\PXL_20230626_022707896.jpg", 'rb')
        #data = image.read()
        #image.close()
        #return data, 200, {'Content-Type':'image/png'} 
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

        success, frame = camera.read()
        if not success:
            return "Can't receive frame", 500
                
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

def triggerIR(config, action):
    print(f"Trigger: [{config}] [{action}]")
    system(f"irsend SEND_ONCE {config} {action}")

def setupCamera():
    global camera
    camera = cv2.VideoCapture(0)
    if not camera:
        print("No camera detected")
    if camera.isOpened():
        camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)
        if "cameraExposure" in settings:
            camera.set(cv2.CAP_PROP_EXPOSURE, settings["cameraExposure"])

if __name__ == '__main__':
    try:
        f = open(f"./Data/settings", 'rb')
        settings = json.loads(f.read())
        f.close()
        setupCamera()
        app.run(host='0.0.0.0', port=3001)     
    finally:
        if camera:
            camera.release()   

