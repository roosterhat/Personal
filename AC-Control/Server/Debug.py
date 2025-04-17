import json
import cv2
import numpy as np
from PIL import ImageColor, ImageDraw, Image
import Utility

class Debug:
    def __init__(self, camera, OCRModels, StateModels, State, settings, sensor):
        self.camera = camera
        self.OCRModels = OCRModels
        self.StateModels = StateModels
        self.State = State
        self.Settings = settings
        self.Sensor = sensor
        self.colors = ["#B0C5A4", "#D37676", "#EBC49F", "#EADFB4", "#9BB0C1", "#51829B", "#F6995C", "#FFE6E6", "#E1AFD1", "#AD88C6", "#7469B6", "#638889", "#FF90BC"]

    def debugSampleFrameEllipse(self, frame, state, config):
        shape = state["shape"]
        scale = config["position"]["scale"]
        subsampleScale = state["properties"]["subsampleScale"]
        subsampleShape = np.divide(np.shape(frame), subsampleScale).astype(int)
        subsample = np.array(Image.fromarray(frame, mode='RGB').resize(subsampleShape[:2][::-1]))
        x1 = int(max(shape["x"] - shape["r1"], 0) / (scale * subsampleScale))
        x2 = int(max(shape["x"] + shape["r1"], 0) / (scale * subsampleScale)) 
        y1 = int(max(shape["y"] - shape["r2"], 0) / (scale * subsampleScale))
        y2 = int(max(shape["y"] + shape["r2"], 0) / (scale * subsampleScale))
        patch = np.ndarray((y2-y1, x2-x1, 3), np.uint8)
        mask = np.ndarray((y2-y1, x2-x1), np.uint8)
        cx = int(shape["x"] / (scale * subsampleScale))
        cy = int(shape["y"] / (scale * subsampleScale))
        r1 = int(shape["r1"] / (scale * subsampleScale))
        r2 = int(shape["r2"] / (scale * subsampleScale))
        activeColor = ImageColor.getrgb(state["properties"]["activeColor"])[:3]
        threshold = state["properties"]["colorDistanceThreshold"]
        count = 0 
        total = 0

        for y in range(y1, y2):
            for x in range(x1, x2):
                if pow((x - cx) / r1, 2) + pow((y - cy) / r2, 2) - 1 < 0:
                    active = Utility.colorDistance_old(activeColor, subsample[y][x]) <= threshold
                    count += 1 if active else 0
                    total += 1
                    mask[y - y1][x - x1] = 255 if active else 0
                    patch[y - y1][x - x1] = subsample[y][x]
                else:
                    mask[y - y1][x - x1] = 50
                    patch[y - y1][x - x1] = [50,50,50]

        activation = count / total * 100
        patch = cv2.cvtColor(patch, cv2.COLOR_BGR2RGB)
        patch = np.rot90(patch, -config["rotate"] / 90)
        mask = np.rot90(mask, -config["rotate"] / 90)
        return mask, patch, activation 
        
    def debugState(self, config, groupId, request):
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
            stateGroup = body
        else:
            stateGroup = next((x for x in config["actions"]["stateGroups"] if x["id"] == groupId), None)
            
        if stateGroup:
            #mask, patch, act = self.debugSampleFrameEllipse(frame, state, config["frame"])
            #return {"mask": mask.tolist(), "patch": patch.tolist(), "activation": act}, 200, {'Content-Type':'application/json'} 
            states = list(x for x in config["frame"]["states"] if any(x["id"] == s["id"] for s in stateGroup["states"]))
            combined = Utility.buildStateFrame(frame, states, config["frame"])
            results = self.StateModels[stateGroup["model"]](combined["frame"], device="cpu", verbose=False, agnostic_nms=True)

            outputImage = combined["frame"]
            draw = ImageDraw.Draw(outputImage)           

            box = results[0].boxes.data[0].numpy() if len(results[0].boxes.data) > 0 else None
            for state in states:
                shape = combined["shapes"][state["id"]]
                active = Utility.boundingBoxInsideShape(shape, box)
                state["active"] = active
                draw.ellipse([(shape["cx"] - shape["r1"], shape["cy"] - shape["r2"]), (shape["cx"] + shape["r1"], shape["cy"] + shape["r2"])], outline=(0, 255, 0) if active else (255, 0, 0))
            
            for data in ([x.numpy() for x in sorted(results[0].boxes.data, key=lambda x: x[0])]):
                draw.rectangle([tuple(data[0:2]),tuple(data[2:4])], outline=np.random.choice(self.colors))

            return { "output": np.asarray(outputImage).tolist(), "states": states }, 200, {'Content-Type':'application/json'} 
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
            elif element["type"] == "state":
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
        for data in ([x.numpy() for x in sorted(results[0].boxes.data, key=lambda x: x[0])]):
            draw.rectangle([tuple(data[0:2]),tuple(data[2:4])], outline=np.random.choice(self.colors))
            value += str(int(data[5]))

        return {"image": np.asarray(image).tolist(), "value": value}, 200, {"Content-Type": "application/json"}
    
    def debugSchedule(self, config, request):
        if request.method != 'POST':
            return 'No schedule data', 400
        body = request.get_json(force = True, silent = True)
        if body is None:
            return 'No schedule data', 400
        
        state = self.State.getState(config, ["states"] if any(x["type"] == "state" for x in body["conditionEquation"]) else ["basic"])
        if not state:
            return "Failed to get state", 500        

        equation = ""
        for element in body["conditionEquation"]:
            if element["type"] == "operator":
                if element["name"] not in ["<",">","=","(",")","not","and","or"]:
                    return "Invalid operator", 400
                equation += f"{element['name']}{" " if element["name"] in ["not","and","or"] else ""}"
            elif element["type"] == "state":
                value = next((s["active"] for s in state["states"] if s["id"] == element["id"]), None)
                if value is None:
                    return f"No state value found for {element['name']}", 400
                equation += f"{value} "
            elif element["type"] == "sensor":
                value = state[element["name"].lower()] if element["name"].lower() in state else None
                if value is None:
                    return f"No sensor value found for {element['name']}", 400
                if element["name"].lower() == "temperature" and self.Settings["temperatureUnit"] == "F":
                    value = value * (9 / 5) + 32
                equation += f"{value} "
            elif element["type"] == "system":
                if element["name"] == "On":
                    value = state["power"]
                elif element["name"] == "On":
                    value = not state["power"]
                else:
                    return f"Invalid system value {element['name']}", 400
                equation += f"{value} "
            elif element["type"] == "value":
                try:
                    value = int(element["value"])
                    equation += f"{value} "
                except Exception as ex:
                    return f"Invalid value {element['value']}", 400
        
        testResult = {}
        try:
            print(equation, flush=True)
            result = eval(equation)
            if isinstance(result, bool):
                testResult = {"success": True}
            else:
                testResult = {"success": False, "error": "Invalid return type: " + str(type(result))}
        except Exception as ex:
            testResult = {"success": False, "error": str(ex)}
        return json.dumps(testResult), 200, {"Content-Type": "application/json"}     

