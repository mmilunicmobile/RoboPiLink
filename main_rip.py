import networktables
import time
import sys
import os

if __name__ == "__main__":
    servers = "127.0.0.1"
    try:
        servers = os.environ["SSH_CLIENT"].split(" ")[0]
    except(Exception) as e:
        print(f"No SSH Client Detected: {e}")
    if (len(sys.argv) > 1):
        servers = sys.argv[1]

    inst = networktables.NetworkTablesInstance.getDefault()
    table = inst.getTable("RaspbeWyPILib")
    inst.startClient(servers)
    #inst.setServerTeam(2539) # where TEAM=190, 294, etc, or use inst.setServer("hostname") or similar
    #inst.startDSClient() # recommended if running on DS computer; this gets the robot IP from the DS

    while True:
        time.sleep(0.02)

        if inst.isConnected():
            x = table.getNumber("Battery Voltage", 0)
        else:
            x = 0
        print(f"X: {x}")