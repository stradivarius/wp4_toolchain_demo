#!/usr/bin/env python
# coding: utf-8

# In[17]:


import requests
import json
from datetime import datetime, timedelta
from dateutil.parser import parse
from firebase import firebase
import time as t
import sys


# In[5]:


# Unregister the Arrowhead Service
def unregister_service():
    payload = {
    "address": "ahtwp4demo-default-rtdb.firebaseio.com",
    "port": 80,
    "system_name": "wp4demo-persister",
    "service_definition": "wp4demo-persister-db"
    }

    response = requests.delete('http://137.204.57.93:8443/serviceregistry/unregister', params=payload)
    print(response, response.request.url, response.text)


# In[10]:


# Register the Arrowhead Service # IT ALSO REGISTERS THE SYSTEM!!!
def register_service():
    
    sys_id = -1
    ser_id = -1
    
    payload = {
    "interfaces": [
        "HTTP-INSECURE-JSON"
    ],
    "providerSystem": {
        "address": "ahtwp4demo-default-rtdb.firebaseio.com",
        "port": 80,
        "systemName": "wp4demo-persister"
    },
    "serviceDefinition": "wp4demo-persister-db",
    "serviceUri": "Temperature",
    "secure": "NOT_SECURE"
    }

    response = requests.post("http://137.204.57.93:8443/serviceregistry/register", json=payload)

    print(response)
    if response.status_code == 201:
        sys_id = json.loads(response.text)["provider"]["id"]
        ser_id = json.loads(response.text)["id"]
        print("Service created with ID: " + str(ser_id))
        print("System created with ID: " + str(sys_id))
    return sys_id, ser_id


# In[11]:


# Arrowhead raw service parse
def discover_endpoint_AL1():
    page = 1
    page_size = 50
    found = False
    serviceDef = 'wp4demo-temperature'

    # Service query
    query = {'direction':'ASC', 'sort_field':'id'}
    response = requests.get("http://137.204.57.93:8443/serviceregistry/mgmt/servicedef/" + serviceDef, params=query)
    service_desc = json.loads(response.text)
    if "errorMessage" in service_desc:
        print("No service found")
    else:
        print("Service instances found:", len(service_desc['data']))
        for service in service_desc['data']:
            endpoint = service['provider']['address']
            port = service['provider']['port']
            uri = service['serviceUri']
            URL = "http://" + str(endpoint) + ":" + str(port) + "/" + str(uri)
            print ("\t-",URL) #URL = "http://51.77.192.150:5000/temperature"
            return URL # We are using only the first


# In[14]:


def discover_endpoint_AL2(sys_id):
    response = requests.get("http://137.204.57.93:8441/orchestrator/orchestration/" + str(sys_id))
    service_desc = json.loads(response.text)
    if "errorMessage" in service_desc:
        print("No service found")
    else:
        print("Service instances found:", len(service_desc['response']))
    for service in service_desc['response']:
        endpoint = service['provider']['address']
        port = service['provider']['port']
        uri = service['serviceUri']
        URL = "http://" + str(endpoint) + ":" + str(port) + "/" + str(uri)
        print ("\t-",URL) #URL = "http://51.77.192.150:5000/temperature"
        return URL # We are using only the first


# In[18]:


# Init hooks and Parameters for connections

# ADOPTION LEVEL
Impl_Levels = [1,2]
AL = int(sys.argv[1])
if AL not in Impl_Levels:
    AL = 1

# Parameters for the URL request (#TODO APPLY)
timestamp = datetime.utcnow().replace(microsecond=0).isoformat()
query = {'timestamp':timestamp}

# Initalize pointer to database °°° My firebase is at https://ahtwp4demo-default-rtdb.firebaseio.com/
db = firebase.FirebaseApplication('YOUR_OWN_FIREBASE', None)
fireResult = db.put('/', "config", 2)

# Register the Arrowhead Service
unregister_service()
sys_id, ser_id = register_service()
if AL == 1:
    serviceURL = discover_endpoint_AL1()
elif AL == 2:
    serviceURL = discover_endpoint_AL2(sys_id)


# In[21]:


# Firebase real time DB push TODO put into a loop

try:
    while (True):
        response = requests.get(serviceURL)
        for datapoint in json.loads(response.text):
            time = parse(datapoint['time'])
            value = float(datapoint['value'])

            # Push onto Firebase realtime database
            fireResult = db.post('/Temperature', {"timestamp" : time, "value" : value})
            print (time, value, fireResult)

        # Firebase Condition check
        results = db.get('Temperature', None, {'orderBy': '\"timestamp\"'})#, {'limitToLast': '2'}) # Only one parameter working
        lastTimestamp = parse(results[list(results)[-1]]['timestamp'])
        lastValue = int(results[list(results)[-1]]['value'])
        startTimestamp = parse(results[list(results)[0]]['timestamp'])
        for datapoint in list(results)[::-1]: # Backwards
            if not int(results[datapoint]['value']) == lastValue:
                startTimestamp = parse(results[datapoint]['timestamp'])
                break
        newInterval = max(((lastTimestamp - startTimestamp) / 2).seconds, 1)
        fireResult = db.put('/', "config", int(newInterval))
        # We check backwards the measurements until we find a different one from the last. The new time interval is set to the half of their difference in seconds (minimum 1)
        print(startTimestamp, lastTimestamp, newInterval)


        # Firebase Delete data older than 1 Hour
        print("deleting old ones...")
        for datapoint in list(results): #Forwards
            if parse(results[datapoint]['timestamp']) < (datetime.now() - timedelta(hours = 1)):
                db.delete('Temperature', datapoint)
            else:
                break # Because they are ordered
        print("old data points deleted!")
        t.sleep(10)
except KeyboardInterrupt:
    print("Interrupted")
    unregister_service()
    pass
except Exception as e:
    print(e)
    unregister_service()
    pass


# In[5]:




