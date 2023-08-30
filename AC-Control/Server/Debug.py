import json
import cv2
import numpy as np
from PIL import ImageColor, ImageDraw
import Utility

class Debug:
    def __init__(self, camera, OCRModels, State):
        self.camera = camera
        self.OCRModels = OCRModels
        self.State = State

    def debugSampleFrameEllipse(self, frame, state, config):
        f = np.rot90(frame, -config["frame"]["rotate"] / 90)
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
        threshold = state["properties"]["colorDistanceThreshold"]
        count = 0 
        total = 0
        for y in range(y1, y2):
            for x in range(x1, x2):
                if pow((x - cx) / r1, 2) + pow((y - cy) / r2, 2) - 1 < 0:
                    active = Utility.colorDistance(activeColor, f[y][x]) <= threshold
                    count += 1 if active else 0
                    total += 1
                    mask[y - y1][x - x1] = 255 if active else 0
                    patch[y - y1][x - x1] = f[y][x]       
                else:
                    mask[y - y1][x - x1] = 50
                    patch[y - y1][x - x1] = [50,50,50]

        activation = count / total * 100
        patch = cv2.cvtColor(patch, cv2.COLOR_BGR2RGB)
        return mask, patch, activation 
        
    def debugState(self, config, stateId, request):
        if "frame" not in config:
            return "No frame data", 400
        
        result, status = self.camera.getFrame()
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
            mask, patch, act = self.debugSampleFrameEllipse(frame, state, config["frame"])
            return {"mask": mask.tolist(), "patch": patch.tolist(), "activation": act}, 200, {'Content-Type':'application/json'} 
        else:
            return "No state found", 404

    def debugPower(self, config, request):
        if request.method != 'POST':
            return 'No config data', 400
        body = request.get_json(force = True, silent = True)
        if body is None:
            return 'No config data', 400
        
        state = self.State.getState(config, ["states"])
        if not state:
            return "Failed to get state", 500

        equation = ""
        for element in body["stateEquation"]:
            if element["type"] == "operator":
                if element["name"] not in ["(",")","not","and","or"]:
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


    def debugSetState(self, config, request):
        if request.method != 'POST':
            return 'No config data', 400
        body = request.get_json(force = True, silent = True)
        if body is None:
            return 'No config data', 400
        
        testResult = {}
        try:
            result = self.State.setState(config, body["state"], body["settings"])
            testResult = {"success": result is None, "error": result}
        except Exception as ex:
            print(ex)
            testResult = {"success": False, "error": str(ex)}
        return json.dumps(testResult), 200, {"Content-Type": "application/json"}   


    def debugOCR(self, config, request):
        if request.method != 'POST':
            return 'No config data', 400
        body = request.get_json(force = True, silent = True)
        if body is None:
            return 'No config data', 400
        
        result, status = self.camera.getFrame()
        if status != 200:
            return result, status
        frame = result

        image = Utility.reshapeImage(config, frame, body["view"]["shape"]["vertices"])
        image = Utility.prepareOCRImage(image, body["action"]["view"]["properties"])

        results = self.OCRModels[body["action"]["model"]](image, device="cpu", verbose=False, agnostic_nms=True)
        value = ""
        draw = ImageDraw.Draw(image)
        colors = ["#77DD77", "#836953", "#89cff0", "#99c5c4", "#9adedb", "#aa9499", "#aaf0d1", "#b2fba5"]
        for data in ([x.numpy() for x in sorted(results[0].boxes.data, key=lambda x: x[0])]):
            draw.rectangle([tuple(data[0:2]),tuple(data[2:4])], outline=np.random.choice(colors))
            value += str(int(data[5]))

        return {"image": np.asarray(image).tolist(), "value": value}, 200, {"Content-Type": "application/json"}

