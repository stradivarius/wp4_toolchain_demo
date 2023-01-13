import requests
import json
import re
from config import ARROWHEAD_ADDRESS

############################################
###      CONNECTOR SERVICE REGISTRY      ###
############################################
class ArrowheadConnectorServiceRegistry:

    def __init__(self, ipaddress, port, system_name):
        self.service_registry_address = ARROWHEAD_ADDRESS+":8443"
        self.exposed_ipaddress = ipaddress
        self.exposed_port = port
        self.exposed_system_name = system_name

    def register_service(self, endpoint, name):
        payload = {
            "interfaces": [
                "HTTP-INSECURE-JSON"
            ],
            "providerSystem": {
                "address": self.exposed_ipaddress,
                "port": self.exposed_port,
                "systemName": self.exposed_system_name
            },
            "serviceDefinition": name,
            "serviceUri": endpoint
        }
        r = requests.post(self.service_registry_address + '/serviceregistry/register', json=payload)
        try: 
            #print("REGISTER_SERVICE: ", r.json())
            if r.status_code == 201:
                sys_id = json.loads(r.text)["provider"]["id"]
                ser_id = json.loads(r.text)["id"]
                print("REGISTER_SERVICE: ")
                print("Service created with ID: " + str(ser_id))
                print("System created with ID: " + str(sys_id), "\n")
                return sys_id
            elif re.search('already exists', r.text):
                return "-1" 
        except:
            print("Error when decoding response from Arrowhead. Make sure the address you provided hosts Eclipse Arrowhead.")

    # Unregister the Arrowhead Service
    def unregister_service(self, endpoint, name):
        payload = {
            "address": self.exposed_ipaddress,
            "port": self.exposed_port,
            "service_definition": name,
            "service_uri": endpoint,
            "system_name": self.exposed_system_name
        }
        r = requests.delete(self.service_registry_address + '/serviceregistry/unregister', params=payload)
        print("UNREGISTER_SERVICE: ", r, " - ", r.request.url, r.text, "\n")
        
    def register_system(self):
        payload = {
            "address": self.exposed_ipaddress,
            "port": self.exposed_port,
            "systemName": self.exposed_system_name
        }
        r = requests.post(self.service_registry_address + '/serviceregistry/register-system', json=payload)
        try: 
            #print("REGISTER_SYSTEM: ", r.json())
            if r.status_code == 201:
                sys_id = json.loads(r.text)["id"]
                print("REGISTER_SYSTEM: ")
                print("System created with ID: " + str(sys_id), "\n")
                return sys_id
            elif re.search('already exists', r.text):
                return "-1" 
        except:
            print("Error when decoding response from Arrowhead. Make sure the address you provided hosts Eclipse Arrowhead.")

    # Unregister the Arrowhead Service
    def unregister_system(self):
        payload = {
            "address": self.exposed_ipaddress,
            "port": self.exposed_port,
            "system_name": self.exposed_system_name
        }
        r = requests.delete(self.service_registry_address + '/serviceregistry/unregister-system', params=payload)
        print("UNREGISTER_SERVICE: ", r, " - ", r.request.url, r.text, "\n")


############################################
###             FIND SERVICE             ###
############################################
def find_Service_SR(name):
    
    service_registry_address = ARROWHEAD_ADDRESS+":8443"
    URL = None

    query = { "serviceDefinitionRequirement": name }
    response = requests.post(service_registry_address + '/serviceregistry/query', json=query)
    service_desc = json.loads(response.text)
    
    if "errorMessage" in service_desc:
        print("No service found")
    else:
        print("Service instances found:", len(service_desc['serviceQueryData']))
        for service in service_desc['serviceQueryData']:
            endpoint = service['provider']['address']
            port = service['provider']['port']
            uri = service['serviceUri']
            URL = "http://" + str(endpoint) + ":" + str(port) + "/" + str(uri)
            print ("\t-",URL)
        return URL 

def find_Service_Orchestrator(ipaddress, port, system_name):
    orchestrator_address = ARROWHEAD_ADDRESS+":8441"
    URL = None

    query = { "requesterSystem": { "address": ipaddress, "metadata":{}, "port": port, "systemName": system_name } }
    response = requests.post(orchestrator_address + '/orchestrator/orchestration', json=query)    
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
            print ("\t-",URL)
        return URL

def notify_Choreographer(sessionId, runningStepId):
    choreographer_address = ARROWHEAD_ADDRESS+":8457"

    # Service query
    query = { "runningStepId": runningStepId, "sessionId": sessionId}
    response = requests.post(choreographer_address + "/choreographer/notifyStepDone", json=query)
    
    print("Notification Step Done")    