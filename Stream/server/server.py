from flask import Flask, request, send_from_directory
from flask_cors import CORS
from os import path as Path
import sys
import json
from datetime import datetime, timezone
import time as Time
from threading import Thread
import traceback
from hcsr04sensor import sensor as hcsr04
import board
from adafruit_dht import DHT11
import RPi.GPIO as GPIO
import mysql.connector
import ADS1x15


class FlaskWrapper(Flask):
    def __init__(self):
        super().__init__(__name__, static_folder='../client/build')

    def cleanup(self):
        global DHT11Sensor
        print("Cleanup", flush=True)
        if DHT11Sensor:
            print("Release DHT11 Sensor", flush=True)
            DHT11Sensor.exit()

        print("Release GPIO", flush=True)
        GPIO.cleanup((TRIG_PIN, ECHO_PIN))

app = FlaskWrapper()
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)

settings = {}
sensorData = { "temperature": 15 }
DHT11Sensor = None
DEBUG = len(sys.argv) >= 2 and sys.argv[1] == 'debug'
TRIG_PIN = 14
ECHO_PIN = 15
config = {
  'user': 'admin',
  'password': 'password',
  'host': '127.0.0.1',
  'database': 'measurements'
}
 
@app.route('/', defaults={'path': ''})
@app.route('/<path:path>')
def serve(path):
    if path != "" and Path.exists(app.static_folder + '/' + path):
        return send_from_directory(app.static_folder, path)
    else:
        return send_from_directory(app.static_folder, 'index.html')

@app.route('/api/measurements', methods=["POST"])
def getMeasurments():
    try:
        body = request.get_json(force = True, silent = True)
        with mysql.connector.connect(**config) as conn:
            with conn.cursor() as cursor:
                if body["type"] == "full":
                    sql = "SELECT * FROM data WHERE datetime >= %s AND datetime <= %s ORDER BY datetime"
                elif body["type"] == "hour":
                    sql = "SELECT AVG(value), AVG(temperature), FROM_UNIXTIME(ROUND(UNIX_TIMESTAMP(datetime) / 3600) * 3600) as datetime FROM data WHERE datetime >= %s AND datetime <= %s GROUP BY FROM_UNIXTIME(ROUND(UNIX_TIMESTAMP(datetime) / 3600) * 3600) ORDER BY datetime"
                elif body["type"] == "day":
                    sql = "SELECT AVG(value), AVG(temperature), DATE(datetime) FROM data WHERE datetime >= %s AND datetime <= %s GROUP BY DATE(datetime) ORDER BY datetime"
                cursor.execute(sql, (body["start"], body["end"]))
                data = [[round(x[0], 2), round(x[1], 2), x[2].isoformat()] for x in cursor.fetchall()]
                cursor.execute("SELECT average from metadata")
                row = cursor.fetchone()
                return json.dumps({ "data": data, "metadata": { "average": row[0] }}), 200, {'Content-Type':'application/json'} 
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
@app.route('/api/battery', methods=["POST"])
def getBattery():
    try:
        body = request.get_json(force = True, silent = True)
        with mysql.connector.connect(**config) as conn:
            with conn.cursor() as cursor:
                if body["type"] == "range":
                    cursor.execute("SELECT * FROM battery WHERE datetime >= %s AND datetime <= %s ORDER BY datetime", (body["start"], body["end"]))
                elif body["type"] == "current":
                    cursor.execute("SELECT * from battery ORDER BY datetime DESC LIMIT 1")
                
                data = [[round(x[1], 2), x[2].isoformat()] for x in cursor.fetchall()]
                return data, 200, {'Content-Type':'application/json'} 
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500
    
@app.route('/api/settings', methods=["POST"])
def saveSettings():
    global settings
    body = request.get_json(force = True, silent = True)
    if body is None:
        return 'No settings data', 400
    
    try:
        for key in settings:
            settings[key] = body[key]

        with open(f"./Data/settings", 'w') as f:
            f.write(json.dumps(settings))
        return "Success", 200
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

@app.route('/api/settings', methods=["GET"])
def retrieveSettings():
    try:
        with open(f"./Data/settings", 'r') as f:
            data = json.loads(f.read())
            return data, 200, {'Content-Type':'application/json'} 
    except Exception as ex:
        print(traceback.format_exc(), flush=True)
        return "Failed", 500

def temperatureWorker():
    while True:
        global DHT11Sensor, sensorData        
        try:
            DHT11Sensor = DHT11(board.D4)

            while True:
                retries = 0            
                while True:
                    try:
                        DHT11Sensor.measure()
                        if not DHT11Sensor._temperature or not DHT11Sensor._humidity:
                            raise RuntimeError("Empty sensor values")
                        sensorData["temperature"] = DHT11Sensor._temperature
                        sensorData["humidity"] = DHT11Sensor._humidity
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
                Time.sleep(int(settings["temperaturePeriod"]))
        finally:
            try:
                if DHT11Sensor:
                    DHT11Sensor.exit()
            except:
                pass

def ultrasonicWorker():   
    global sensorData    

    try:
        sensor = hcsr04.Measurement(TRIG_PIN, ECHO_PIN)

        while True:
            try:
                sensor.temperature = sensorData["temperature"]
                value = sensor.raw_distance()
                with mysql.connector.connect(**config) as conn:
                    with conn.cursor() as cursor:
                        cursor.execute("INSERT INTO data VALUES (%s, %s, %s)", (value, sensorData["temperature"], datetime.now(timezone.utc)))
                        cursor.execute("SELECT (SELECT total from metadata) as total, (SELECT COUNT(value) from data) as count")
                        row = cursor.fetchone()
                        total = row[0] + value
                        count = row[1] + 1
                        cursor.execute("UPDATE metadata SET total = %s, average = %s", (total, total / count))
                    conn.commit()
            except Exception as error:
                print("ultrasonicWorker, Error: " + str(error), flush=True)
            Time.sleep(int(settings["ultrasonicPeriod"]))
    finally:
        GPIO.cleanup((TRIG_PIN, ECHO_PIN))    

def batteryWorker():   
    try:
        ADS = ADS1x15.ADS1115(1)
        ADS.setMode(1)
        ADS.setGain(0)

        while True:
            try:
                raw = ADS.readADC(0)
                VCF = float(settings["voltageConversionFactor"])
                voltage = ADS.toVoltage(raw) if VCF == 0 else raw * VCF

                with mysql.connector.connect(**config) as conn:
                    with conn.cursor() as cursor:
                        cursor.execute("INSERT INTO battery VALUES (%s, %s, %s)", (raw, voltage, datetime.now(timezone.utc)))
                    conn.commit()
            except Exception as error:
                print("batteryWorker, Error: " + str(error), flush=True)
            Time.sleep(int(settings["batteryPeriod"]))
    except Exception as ex:
         print("batteryWorker, Start up Error: " + str(error), flush=True)

def appStart():
    global settings
    print("Loading Settings", flush=True)
    with open(f"./Data/settings", 'rb') as f:
        settings = json.loads(f.read())
    if not settings:
        exit("Failed to load settings")
    print("Starting Temperature Manager", flush=True)
    Thread(target=temperatureWorker).start()
    print("Starting Ultrasonic Manager", flush=True)
    Thread(target=ultrasonicWorker).start()
    print("Starting Battery Manager", flush=True)
    Thread(target=batteryWorker).start()
    print("Loading Complete", flush=True)
    if DEBUG:
        try:
            app.run(host='0.0.0.0', port=3001)    
        finally:
            app.cleanup()
    else:
        return app
    

if __name__ == '__main__':
    appStart()
    

