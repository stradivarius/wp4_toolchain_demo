from flask import Flask, jsonify, request, json
import requests
from arrowhead import ArrowheadConnectorServiceRegistry, find_Service_SR, find_Service_Orchestrator, notify_Choreographer
from config import SENSOR_IP_ADDRESS, SENSOR_PORT, SENSOR_GETDATA, DATABASE_IP_ADDRESS, DATABASE_PORT, DATABASE_FILL, DATABASE_GETDATA, DATABASE_DELETEDATA, DATABASE_SETCONFIG, DATABASE_SYSTEM_NAME
import sys
import atexit
import time as t
import mysql.connector
import json
import datetime
import threading

def saveData(serviceURL):
    response = requests.get(serviceURL)
        
    try:
        connection = mysql.connector.connect(
            host="localhost",
            user="root",
            password="database",
            database="tesi"
        )

        cursor = connection.cursor()
        
        sql = "INSERT INTO sensordata (time, plugged, percent) VALUES (%s, %s, %s)"
        val = []
        
        for datapoint in json.loads(response.text):
            plugged = bool(datapoint['plugged'])
            percent = str(datapoint['percent'])
            time = str(datapoint['time'])
            
            val.append((time, plugged, percent))
        
        cursor.executemany(sql, val)
        connection.commit()
        print("Record inserted successfully")
    
    except mysql.connector.Error as error:
        print("Failed to insert into MySQL table {}".format(error))
    
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()
            print("MySQL connection is closed")


def getData():
    try:
        connection = mysql.connector.connect(
            host="localhost",
            user="root",
            password="database",
            database="tesi"
        )

        cursor = connection.cursor()
        
        sql = "SELECT * FROM sensordata"
        
        cursor.execute(sql)
        result = cursor.fetchall()
    
    except mysql.connector.Error as error:
        print("Failed to get data from MySQL table {}".format(error))
    
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()
            print("MySQL connection is closed")
            return result

def deleteData():
    try:
        connection = mysql.connector.connect(
            host="localhost",
            user="root",
            password="database",
            database="tesi"
        )

        cursor = connection.cursor()
        
        sql = "DELETE FROM sensordata"
        
        cursor.execute(sql)
        connection.commit()
        result = str(cursor.rowcount)
    
    except mysql.connector.Error as error:
        print("Failed to delete data from MySQL table {}".format(error))
    
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()
            print("MySQL connection is closed")
            print(result)
            return result

def converter(o):
    if isinstance(o, datetime.datetime):
        return o.__str__()

def connection_level(intervalC):
    while (True):
        if stop_threads:
            break
    
        if AL == 0:
            serviceURL = "http://"+SENSOR_IP_ADDRESS+":"+str(SENSOR_PORT)+"/"+SENSOR_GETDATA
        else:   
            if AL == 1:
                serviceURL = find_Service_SR(SENSOR_GETDATA)     
            elif AL == 2:
                serviceURL = find_Service_Orchestrator(DATABASE_IP_ADDRESS, DATABASE_PORT, DATABASE_SYSTEM_NAME)
        
        saveData(serviceURL)
        t.sleep(intervalC.interval)



Impl_Levels = [0, 1, 2, 3]
AL = int(sys.argv[1])
if AL not in Impl_Levels:
    AL = 0

class intervalClass:
    def __init__(self, interval = 10):
        self.interval = interval
    
intervalC = intervalClass();

app = Flask(__name__)

if AL>0:
    ah_connector = ArrowheadConnectorServiceRegistry(DATABASE_IP_ADDRESS, DATABASE_PORT, DATABASE_SYSTEM_NAME)
    ah_connector.register_service(endpoint=DATABASE_GETDATA, name=DATABASE_GETDATA)
    ah_connector.register_service(endpoint=DATABASE_DELETEDATA, name=DATABASE_DELETEDATA)
    ah_connector.register_service(endpoint=DATABASE_SETCONFIG, name=DATABASE_SETCONFIG)
    ah_connector.register_service(endpoint=DATABASE_FILL, name=DATABASE_FILL)

@app.route('/'+DATABASE_SYSTEM_NAME+'/'+DATABASE_GETDATA, methods=['GET'])
@app.route('/'+DATABASE_GETDATA, methods=['GET'])
def database_getData():
    return json.dumps(getData(), default = converter)

@app.route('/'+DATABASE_SYSTEM_NAME+'/'+DATABASE_DELETEDATA, methods=['POST'])
@app.route('/'+DATABASE_DELETEDATA, methods=['POST'])
def database_deletedata():
    return deleteData()

@app.route('/'+DATABASE_SYSTEM_NAME+'/'+DATABASE_SETCONFIG, methods=['POST'])
@app.route('/'+DATABASE_SETCONFIG, methods=['POST'])
def database_setConfig():
    content_type = request.headers.get('Content-Type')
    json = request.get_json()
    print(json)
    if ((content_type == 'application/json') and ('interval' in json)):
        if (type(json['interval']) is int):
            if (json['interval']>0):
                intervalC.interval = json['interval']
                return ('Config Update')
            else:
                return ('Value of "interval" incorrect')
        else:
            return ('Interval type error')
    else:
        return ('Error with json codex')

@app.route('/'+DATABASE_SYSTEM_NAME+'/'+DATABASE_FILL, methods=['POST'])
@app.route('/'+DATABASE_FILL, methods=['POST'])
def database_fill():
    if AL == 3:
        content_type = request.headers.get('Content-Type')
        if (content_type == 'application/json'):
            json = request.get_json()
            print(json)
            sessionId = json['sessionId']
            runningStepId = json['runningStepId']
            
            t.sleep(intervalC.interval)
            ah_connector = ArrowheadConnectorServiceRegistry(DATABASE_IP_ADDRESS, DATABASE_PORT, DATABASE_SYSTEM_NAME)
            a = ah_connector.register_system()
            serviceURL = find_Service_Orchestrator(DATABASE_IP_ADDRESS, DATABASE_PORT, DATABASE_SYSTEM_NAME)   
            
            saveData(serviceURL)
            
            notify_Choreographer(sessionId, runningStepId)
            return ('Service Available')
        else:
            print('Content-Type not supported!')
            return ('Service Unavailable')
    else:
        return ('Service Unavailable')

def shutdown():
    stop_threads=True
    if AL>0:
        ah_connector.unregister_service(endpoint=DATABASE_GETDATA, name=DATABASE_GETDATA)
        ah_connector.unregister_service(endpoint=DATABASE_DELETEDATA, name=DATABASE_DELETEDATA)
        ah_connector.unregister_service(endpoint=DATABASE_SETCONFIG, name=DATABASE_SETCONFIG)
        ah_connector.unregister_service(endpoint=DATABASE_FILL, name=DATABASE_FILL)
    print("Closed Successfully")

stop_threads=False
if AL != 3:
    x = threading.Thread(target=connection_level, args=(intervalC, ), daemon=True)
    x.start()

atexit.register(shutdown)
app.run(host=DATABASE_IP_ADDRESS, port=DATABASE_PORT)