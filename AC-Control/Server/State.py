from PIL import ImageColor
import time as Time
import Utility

class State:
    def __init__(self, camera, OCRModel, settings):
        self.camera = camera
        self.settings = settings
        self.OCRModel = OCRModel

    def sampleFrameEllipse(self, frame, state, config):
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
                if pow((x - cx) / r1, 2) + pow((y - cy) / r2, 2) - 1 < 0 and Utility.colorDistance(activeColor, frame[y][x]) <= threshold:
                    count += 1

        activation = count / ((y2-y1) * (x2-x1)) * 100
        threshold = state["properties"]["stateActivationPercentage"] if "stateActivationPercentage" in state["properties"] else 5
        return activation >= threshold

    def getPowerState(self, config, state):
        equation = ""
        for element in config["actions"]["power"]["stateEquation"]:
            if element["type"] == "operator":
                if element["name"] not in ["(",")","not","and","or"]:
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

    def getState(self, config, sections = None):
        currentState = {}
        result, status = self.camera.getFrame()
        if status != 200:
            return None
        frame = result

        if "states" in config["frame"] and (sections is None or "states" in sections or "power" in sections):
            currentState["states"] = config["frame"]["states"]
            for state in config["frame"]["states"]:
                state["active"] = self.sampleFrameEllipse(frame, state, config["frame"])
        
        if "ocr" in config["frame"] and (sections is None or "ocr" in sections):
            currentState["ocr"] = config["actions"]["ocr"]
            for target in currentState["ocr"]:
                view = next((x for x in config["frame"]["ocr"] if x["id"] == target["view"]), None)
                image = Utility.reshapeImage(config, frame, view["shape"]["vertices"])
                image = Utility.prepareOCRImage(view, image)
                results = self.OCRModel(image, device="cpu", verbose=False, agnostic_nms=True)
                value = ""
                for data in ([x.numpy() for x in sorted(results[0].boxes.data, key=lambda x: x[0])]):
                    value += str(int(data[5]))
                target["value"] = value
        
        if sections is None or "power" in sections:
            currentState["power"] = { "active": self.getPowerState(config, currentState) }
        return currentState

    def stateChanged(self, newState, oldState):
        try:
            return any(newState[i]["active"] != oldState[i]["active"] for i in range(len(newState)))
        except:
            return True
        
    def stateActive(self, states, id):
        return next((x["active"] for x in states if x["id"] == id), None)
        
    def attemptSetPower(self, config, action, target, settings):
        for i in range(settings["triggerAttempts"]):
            Utility.triggerIR(config["ir_config"], action)
            Time.sleep(settings["setStateDelay"] / 1000)
            state = self.getState(config, ["power"])
            if state and state["power"]["active"] == target:
                return True        
        return False

    def walkStateGroup(self, config, group, state, action, settings):
        oldState = self.getState(config, ["states"])
        if oldState and self.stateActive(oldState["states"], state["id"]):
            return True
        for _ in range(len(group["states"])):
            for _ in range(settings["triggerAttempts"]):
                Utility.triggerIR(config["ir_config"], action)
                Time.sleep(settings["setStateDelay"] / 1000)
                newState = self.getState(config, ["states"])
                if self.stateActive(newState["states"], state["id"]):
                    return True
                if self.stateChanged(newState, oldState):
                    break
                oldState = newState            
        return False

    def getTemperature(self, state):
        return next((int(x["value"]) for x in state["ocr"] if x["name"] == "Temperature"), None)

    def temperatureChanged(self, oldState, newState):
        return self.getTemperature(oldState) != self.getTemperature(newState)

    def setTemperature(self, config, target, actions, settings):
        currentTemp = self.getTemperature(self.getState(config, ["ocr"]))
        if currentTemp == target:
            return True
        count = 0
        UP = next((x for x in actions if x["name"] == "Up"), None)
        DOWN = next((x for x in actions if x["name"] == "Down"), None)
        while settings["minTemperature"] <= currentTemp <= settings["maxTemperature"]:
            Utility.triggerIR(config["ir_config"], UP["action"] if currentTemp < target else DOWN["action"])
            Time.sleep(settings["setStateDelay"] / 1000)
            newTemp = self.getTemperature(self.getState(config, ["ocr"]))
            if newTemp == target:
                return True
            if currentTemp != newTemp:
                count = 0
            else:
                count += 1
            if count >= settings["triggerAttempts"]:
                return False
            currentTemp = newTemp
        return False

    def getOCRValue(self, state, id):
        return next((x["value"] for x in state["ocr"] if x["id"] == id), None)

    def atTargetValue(self, state, id, target):
        return self.getOCRValue(state, id) == target

    def OCRValueChanged(self, oldState, newState, id):
        return self.getOCRValue(oldState, id) != self.getOCRValue(newState, id)

    def setOCRValue(self, config, id, target, action, settings):
        oldState = self.getState(config, ["ocr"])
        if oldState and self.atTargetValue(oldState, id, target):
            return True
        for _ in range(settings["maxOCRValueChange"]):
            for _ in range(settings["triggerAttempts"]):
                Utility.triggerIR(config["ir_config"], action)
                Time.sleep(settings["setStateDelay"] / 1000)
                newState = self.getState(config, ["ocr"])
                if self.atTargetValue(newState, id, target):
                    return True
                if self.OCRValueChanged(newState, oldState, id):
                    break
                oldState = newState            
        return False

    def setState(self, config, targetState, setting=None):
        if setting is None:
            setting = self.settings

        currentState = self.getState(config, ["power"])
        if not currentState:
            return "Failed to get state"

        buttonMap = {}
        for b in config["buttons"]:
            buttonMap[b["id"]] = b["action"]
        stateMap = {}
        for g in config["actions"]["stateGroups"]:
            for s in g["states"]:
                stateMap[s["id"]] = g

        if targetState["power"]["active"]:
            if not currentState["power"]["active"]:
                if not self.attemptSetPower(config, buttonMap[config["actions"]["power"]["button"]], True, setting):
                    return f"Failed to set power [On]"
            for state in targetState["states"]:
                if state["id"] not in stateMap:
                    return "State not associated with a group"
                group = stateMap[state["id"]]
                if not self.walkStateGroup(config, group, state, buttonMap[group["button"]], setting):
                    return f"Failed to set {config['frame']['states'][state['id']]['name']} [On]"
            for ocr in targetState["ocr"]:
                if not ocr["target"]:
                    continue 
                if ocr["name"] == "Temperature":
                    if not self.setTemperature(config, int(ocr["target"]), ([{"name": x["name"], "action": buttonMap[x["button"]]} for x in ocr["buttons"]]), setting):
                        return f"Failed to set Temperature to [{ocr['target']}]"
                else:
                    if not self.setOCRValue(config, ocr["id"], ocr["target"], buttonMap[ocr["buttons"][0]["button"]], setting):
                        return f"Failed to set {ocr['name']} to [{ocr['target']}]"
        else:
            if currentState["power"]["active"]:
                if not self.attemptSetPower(config, buttonMap[config["actions"]["power"]["button"]], False, setting):
                    return f"Failed to set power [Off]"