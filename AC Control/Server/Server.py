from flask import Flask, request
from flask_cors import CORS
import re
from os import listdir, system
from os.path import isfile, join
import json
import uuid

ILLEGAL_CHARS = r'\/\.\@\#\$\%\^\&\*\(\)\{\}\[\]\"\'\`\,\<\>\\'
fileExtPattern = re.compile(r'\.(?P<ext>js|ico|css|png|jpg|html)$')
fileNamePattern = re.compile(rf'[^{ILLEGAL_CHARS}]+')
contentTypeMap = { 'js': 'text/javascript', 'ico': 'image/x-icon', 'css': 'text/css', 'png': 'image/png', 'jpg': 'image/jpg', 'html': 'text/html' }

app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

@app.route('/list')
def list():
    try:
        names = []
        for file in ([f for f in listdir('./Data/Configs') if isfile(join('./Data/Configs', f))]):
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

@app.route('/save', methods=["POST"])
def save():
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
    
@app.route('/retrieve/<id>')
def retrieve(id):
    try:
        if id == 'default':
            f = open("./Data/default", 'r')
            id = f.read()
            f.close()
            if not isfile(f"./Data/Configs/{id}"):
                return "None", 404
        else:
            f = open(f"./Data/default", 'w')
            f.write(id)
            f.close()
        f = open(f"./Data/Configs/{id}", 'rb')
        data = f.read()
        return data, 200, {'ContentType':'application/json'} 
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/upload', methods=["POST"])
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

@app.route('/background/<filename>')
def background(filename):
    if not isfile('./Data/Backgrounds/'+filename):
        return 'File does not exists', 400
    try:
        f = open('./Data/Backgrounds/'+filename, 'rb')
        data = f.read()
        return data, 200, {'ContentType':'image'} 
    except Exception as ex:
        print(ex, flush=True)
        return "Failed", 500
    finally:
        f.close()

@app.route('/trigger/<config>/<id>')
def trigger(config, id):
    if not isfile('./Data/Configs/'+config):
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
    #system(f"irsend SEND_ONCE {config} {action}")

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3001)
