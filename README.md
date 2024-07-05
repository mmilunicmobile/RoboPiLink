# RoboPiLink

This is a base WPILib project that can interface with **any** Raspberry Pi over a network connection!

### Why would I use this?
If you have excess Raspberry Pis you can use them as a platform to teach WPILib on! This creates a similar avenue to that of the Romi and XRP, but accessible to any Raspberry Pi.

An example would be creating a few minibots for a STEM Day using some spare Raspberry Pi 1s and some old Robot Radios. They can then each have code written with WPILib run on them and can also be controlled from the computer with the code running.

> [!NOTE]
> This project is in early development. Please read through the warnings and acknowledge the license before doing anything.

### How to use!

Make sure you have Rasperry Pi OS running on the target Raspberry Pi. Make sure the target Raspberry Pi is connected to the same network as the computer that will be holding the code.

Make sure that the computer you are running on has an installation of Python3 and also has gpiozero and pigpio installed as Python3 modules. To do the second part with pip, you can run `python3 -m pip install -u gpiozero pigpio`.

SSH into the Raspberry Pi and run `sudo pigpiod`. If using the [watchdog](#watchdog-script-highly-recomended), also start that script now.

> [!CAUTION]
> This above step will allow **all** remote connections via gpiozero to the device until the next time it shuts down and is not super secure. If you are worried about someone getting on your network and messing with the pins, you can use the `-n` option, which is documented on the [gpiozero docs](https://gpiozero.readthedocs.io/en/latest/remote_gpio.html#command-line-pigpiod).

Clone the code in this repository and open it in WPILib VSCode.

`Robot.java` should have a `robotInit()` method which looks like this:
```java
  @Override
  public void robotInit() {

    // Set wether to try connecting with an actual RaspberryPi or simulated RaspberryPi
    boolean isSimulation = false;

    // Instantiate robot python interface
    m_roboPiLink = new RoboPiLink("raspberrypi", isSimulation);

    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();

    // After initializing our python objects in our RobotContainer, start the main loop for the
    // communications with the python interface
    m_roboPiLink.startMainLoop();
  }
```

Make sure that `"raspberrypi"` in the `RoboPiLink` creation is the hostname/local IP address of your Raspberry Pi.

Then simply "simulate the code" by pressing on the W in the top right corner, typing "sim" and choosing the simulate option.

> [!CAUTION]
> If the robot program abruptly ends or disconnects from the Raspberry Pi while pins are delivering power, **they will continue to deliver the last signal they were given**! This means a robot with more than a bit of power may become uncontrollable if you accidenally close the program without disabling, or the WiFi goes out! The following Python3 watchdog script on the Raspberry Pi will shut off all pins if it detects a lost connection while it is running.

### Watchdog Script (Highly Recomended)
```python3
#!/usr/bin/env python3
import pigpio
import sys
import signal

pin = 2 #int(sys.argv[1])
timeout = 200# int(sys.argv[2])

pi = pigpio.pi()

def callback(gpio, level, tick):
    if level == 2:
        print("watxhdog triggered")
        shutoffAllPins()

def shutoffAllPins():
    for i in range(0, 56):
        try:
            pi.set_mode(i, pigpio.INPUT)
            pi.set_pull_up_down(i, pigpio.PUD_OFF)
        except:
            pass

pi.callback(2, pigpio.EITHER_EDGE, callback)
pi.set_watchdog(pin, timeout)

signal.pause()
```

This program can also be found in [this file](robopilinkd). 

### Daemon (Optional)

Additionally, there is a systemd compatible [`robopilinkd.service`](robopilinkd.service) which will automatically setup pigpiod and the watchdog. If setup correctly the service will allow you to immediately run your WPILib code on a Pi following boot with no steps required. Further help will be included in a future revision/
