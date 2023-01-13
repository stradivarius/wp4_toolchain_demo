import requests
from config import SERVICE_REGISTRY_ADDRESS, SERVICE_IP_ADDRESS, SERVICE_PORT


class ArrowheadConnector:

    def __init__(self, ipaddress=SERVICE_IP_ADDRESS, port=SERVICE_PORT, system_name="wp4demo-measurement-provider"):
        self.service_registry_address = SERVICE_REGISTRY_ADDRESS
        self.exposed_ipaddress = ipaddress
        self.exposed_port = port
        self.exposed_system_name = system_name

    def register_service(self, endpoint="temperature", name="wp4demo-temperature"):
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
        r = requests.post(self.service_registry_address +
                          '/serviceregistry/register', json=payload)
        try: 
            print(r.json())
        except:
            print("Error when decoding response from Arrowhead. Make sure the address you provided hosts Eclipse Arrowhead.")

    # Unregister the Arrowhead Service
    def unregister_service(self, service_name="wp4demo-temperature"):
        payload = {
            "address": self.exposed_ipaddress,
            "port": self.exposed_port,
            "system_name": self.exposed_system_name,
            "service_definition": service_name
        }
        r = requests.delete(self.service_registry_address +
                            '/serviceregistry/unregister', params=payload)
        print(r, r.request.url, r.text)
