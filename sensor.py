from flask import Flask, jsonify, request, json
import requests
from arrowhead import ArrowheadConnectorServiceRegistry, find_Service_SR, find_Service_Orchestrator, notify_Choreographer
from config import SENSOR_IP_ADDRESS, SENSOR_PORT, SENSOR_STARTDATA, SENSOR_STOPDATA, SENSOR_GETDATA, SENSOR_SETCONFIG, SENSOR_SYSTEM_NAME
import sys
import atexit
import psutil
import atexit
import queue
import threading

import logging
from datetime import datetime

class MeasurementSENSOR:

    def __init__(self, sampling_time = 2, buffer_length = 100):
        self.stop_threads=False
        self.last_measurement_time = datetime.now()
        self.config = {'sampling_time': sampling_time, 'buffer_length': buffer_length}
        self.buffer = queue.Queue()

    def get_measurements(self):
        ret = list(self.buffer.queue)
        self.buffer.queue.clear()
        return ret

    def generate_measurement(self):
        self.last_measurement_time = datetime.now()
        battery = psutil.sensors_battery()
        plugged = battery.power_plugged
        percent = str(battery.percent)
        v = {"plugged": plugged, "percent": percent, "time": datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
        return v

    def change_configuration(self, config):
        while(len(self.buffer.queue) > config["buffer_length"]):
            self.buffer.get()
        self.config = config

    def measurement_thread(self):
        while(True):
            if self.stop_threads:
                break
            if((datetime.now() - self.last_measurement_time).total_seconds() >self.config['sampling_time']):
                logging.debug("measuring " + str(datetime.now()))
                while(len(self.buffer.queue) >= self.config["buffer_length"]):
                    self.buffer.get()
                self.buffer.put(self.generate_measurement())

    def start_measurements(self):
        self.x = threading.Thread(target=self.measurement_thread, daemon=True)
        self.stop_threads=False
        self.x.start()
    
    def stop_measurements(self):
        self.stop_threads=True

Impl_Levels = [0, 1, 2, 3]
AL = int(sys.argv[1])
if AL not in Impl_Levels:
    AL = 0

app = Flask(__name__)

SENSOR = MeasurementSENSOR()
if AL != 3:
    SENSOR.start_measurements()

if AL>0:
    ah_connector = ArrowheadConnectorServiceRegistry(SENSOR_IP_ADDRESS, SENSOR_PORT, SENSOR_SYSTEM_NAME)
    ah_connector.register_service(endpoint=SENSOR_GETDATA, name=SENSOR_GETDATA)
    ah_connector.register_service(endpoint=SENSOR_SETCONFIG, name=SENSOR_SETCONFIG)
    ah_connector.register_service(endpoint=SENSOR_STARTDATA, name=SENSOR_STARTDATA)
    ah_connector.register_service(endpoint=SENSOR_STOPDATA, name=SENSOR_STOPDATA)

@app.route('/'+SENSOR_SYSTEM_NAME+'/'+SENSOR_GETDATA, methods=['GET'])
@app.route('/'+SENSOR_GETDATA, methods=['GET'])
def temperature_measurement():
    measurements = jsonify(SENSOR.get_measurements())
    return measurements

@app.route('/'+SENSOR_SYSTEM_NAME+'/'+SENSOR_SETCONFIG, methods=['POST'])
@app.route('/'+SENSOR_SETCONFIG, methods=['POST'])
def update_configuration():
    content_type = request.headers.get('Content-Type')
    json = request.get_json()
    if ((content_type == 'application/json') and ('sampling_time' in json) and ('buffer_length' in json)):
        if ((type(json['sampling_time']) is int) and (type(json['buffer_length']) is int)):
            if ((json['sampling_time']>0) and (json['buffer_length']>0)):
                SENSOR.change_configuration(json)
                return ('Config Update')
            else:
                return ('Value of "sampling_time" or "buffer_length" incorrect')
        else:
            return ('sampling_time or buffer_length value type error')
    else:
        return ('Error with json codex')

@app.route('/'+SENSOR_SYSTEM_NAME+'/'+SENSOR_STARTDATA, methods=['POST'])
@app.route('/'+SENSOR_STARTDATA, methods=['POST'])
def temperature_startMeasurement():
    if AL == 3:
        content_type = request.headers.get('Content-Type')
        if (content_type == 'application/json'):
            json = request.get_json()
            print(json)
            sessionId = json['sessionId']
            runningStepId = json['runningStepId']
            
            SENSOR.start_measurements()
            
            notify_Choreographer(sessionId, runningStepId)
            return ('Service Available')
        else:
            print('Content-Type not supported!')
            return ('Content-Type not supported!')
    else:
        return ('Service Unavailable')
    
@app.route('/'+SENSOR_SYSTEM_NAME+'/'+SENSOR_STOPDATA, methods=['POST'])
@app.route('/'+SENSOR_STOPDATA, methods=['POST'])
def temperature_stopMeasurement():
    if AL == 3:
        content_type = request.headers.get('Content-Type')
        if (content_type == 'application/json'):
            json = request.get_json()
            print(json)
            sessionId = json['sessionId']
            runningStepId = json['runningStepId']
            
            t = SENSOR.stop_measurements()
            
            notify_Choreographer(sessionId, runningStepId)
            return ('Service Available')
        else:
            print('Content-Type not supported!')
            return ('Service Unavailable')
    else:
        return ('Service Unavailable')

def shutdown():
    SENSOR.stop_measurements()
    if AL>0:
        ah_connector.unregister_service(endpoint=SENSOR_GETDATA, name=SENSOR_GETDATA)
        ah_connector.unregister_service(endpoint=SENSOR_SETCONFIG, name=SENSOR_SETCONFIG)
        ah_connector.unregister_service(endpoint=SENSOR_STARTDATA, name=SENSOR_STARTDATA)
        ah_connector.unregister_service(endpoint=SENSOR_STOPDATA, name=SENSOR_STOPDATA)
    print("Closed Successfully")
    return
    
atexit.register(shutdown)

app.run(host=SENSOR_IP_ADDRESS, port=SENSOR_PORT)
