my_ip="192.168.178.141"

############################################
###               GENERAL                ###
############################################
ARROWHEAD_ADDRESS = "http://"+my_ip
SYSTEM_NAME = "system-Example"

############################################
###                SENSOR                ###
############################################
SENSOR_IP_ADDRESS = my_ip
SENSOR_PORT = 5000
SENSOR_GETDATA = "batterySensor-getData"
SENSOR_SETCONFIG = "batterySensor-setConfig"
SENSOR_STARTDATA = "batterySensor-startData"
SENSOR_STOPDATA = "batterySensor-stopData"
SENSOR_SYSTEM_NAME = "batterySensorSystem"

############################################
###               DATABASE               ###
############################################
DATABASE_IP_ADDRESS = my_ip
DATABASE_PORT = 5001
DATABASE_FILL = "database-fill"
DATABASE_GETDATA = "database-getData"
DATABASE_DELETEDATA = "database-deleteData"
DATABASE_SETCONFIG = "database-setConfig"
DATABASE_SYSTEM_NAME = "databaseSystem"