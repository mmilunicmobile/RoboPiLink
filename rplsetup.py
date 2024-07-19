from sys import argv, executable
from subprocess import check_call

if len(argv) != 3:
    print("Please specify the user and name of the Raspberry Pi: python3 rplsetup.py [USER_NAME] [RASPBERRYPI_HOSTNAME]\n\
          Or to install required computer libraries: python3 rplsetup.py")
    exit(1)

RASPBERRYPI_NAME = argv[2]
USER_NAME = argv[1]

print("Using RaspberryPi hostname of " + RASPBERRYPI_NAME + " and user name of " + USER_NAME)
print("This script may ask you for the password of the RasbperryPi")

with open("robopilinkd", "r") as f:
    robopilinkd_script = f.read()

with open("robopilinkd.service", "r") as f:
    robopilinkd_service = f.read()


script_contents = "\
echo Enabling remote GPIO...\n\
sudo raspi-config nonint do_rgpio 0\n\
echo Creating robopilinkd script...\n\
sudo bash <<EOF\necho '" + robopilinkd_script + "' > /usr/bin/robopilinkd\nEOF\n\
echo Granting permissions to robopilinkd script...\n\
sudo chmod +x /usr/bin/robopilinkd\n\
echo Creating robopilinkd service...\n\
sudo bash <<EOF\necho '" + robopilinkd_service + "' > /etc/systemd/system/robopilinkd.service\nEOF\n\
echo Enabling service...\n\
sudo systemctl enable robopilinkd.service\n\
echo Starting service...\n\
sudo systemctl start robopilinkd.service\n"

check_call(["ssh", USER_NAME + "@" + RASPBERRYPI_NAME, script_contents])
print("Setup complete!")