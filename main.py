import ntcore
import time
import sys
import os

if __name__ == "__main__":
    servers = ["127.0.0.1"]
    servers += sys.argv[1:]
    try:
        servers.append(os.environ["SSH_CLIENT"].split(" ")[0])
    except:
        pass

    inst = ntcore.NetworkTableInstance.getDefault()
    table = inst.getTable("Robot")
    xSub = table.getDoubleTopic("Battery Voltage").subscribe(0)
    inst.startClient4("RaspberWyPILib")
    inst.setServer(servers)
    #inst.setServerTeam(2539) # where TEAM=190, 294, etc, or use inst.setServer("hostname") or similar
    #inst.startDSClient() # recommended if running on DS computer; this gets the robot IP from the DS

    while True:
        time.sleep(0.02)

        x = xSub.get()
        print(f"X: {x}")