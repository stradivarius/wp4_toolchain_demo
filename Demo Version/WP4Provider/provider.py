import random
import threading
from datetime import datetime
from flask import Flask, jsonify, request
from arrowhead import ArrowheadConnector
import queue
import logging
app = Flask(__name__)

logging.basicConfig(level=logging.DEBUG)

_CLEAR_QUEUE = False

class MeasurementProvider:

    def __init__(self, sampling_time = 2, buffer_length = 100):
        self.last_measurement_time = datetime.now()
        self.config = {'sampling_time': sampling_time, 'buffer_length': buffer_length}
        self.buffer = queue.Queue()

    def get_measurements(self):
        ret = list(self.buffer.queue)
        if _CLEAR_QUEUE:
            self.buffer.queue.clear()
        return ret

    def generate_measurement(self):
        self.last_measurement_time = datetime.now()
        return {"value": 23+random.uniform(-5,5), "time": datetime.now().isoformat()}

    def change_configuration(self, config):
        while(len(self.buffer.queue) > config["buffer_length"]):
            self.buffer.get()
        self.config = config

    def measurement_thread(self):
        while(True):
            if((datetime.now() - self.last_measurement_time).total_seconds() >self.config['sampling_time']):
                logging.debug("measuring " + str(datetime.now()))
                while(len(self.buffer.queue) >= self.config["buffer_length"]):
                    self.buffer.get()
                self.buffer.put(self.generate_measurement())

    def start_measurements(self):
        x = threading.Thread(target=self.measurement_thread, daemon=True)
        x.start()


ah_connector = ArrowheadConnector()
ah_connector.register_service(endpoint="temperature", name="wp4demo-temperature")
ah_connector.register_service(endpoint="configuration", name="wp4demo-configuration")
provider = MeasurementProvider()
provider.start_measurements()

@app.route('/wp4demo-measurement-provider/temperature', methods=['GET'])
@app.route('/temperature', methods=['GET'])
def temperature_measurement():
    measurements = jsonify(provider.get_measurements())
    return measurements

@app.route('/wp4demo-measurement-provider/configuration', methods=['POST'])
@app.route('/configuration', methods=['POST'])
def update_configuration():
    payload = request.json
    provider.change_configuration(payload)
    resp = jsonify(success=True)
    return resp

app.run(host='0.0.0.0')




