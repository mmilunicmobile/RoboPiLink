# RoboPiLink

This is a base WPILib project that can interface with **any** Raspberry Pi over a network connection!

### Why would I use this?
If you have excess Raspberry Pis you can use them as a platform to teach WPILib on! This creates a similar avenue to that of the Romi and XRP, but accessible to any Raspberry Pi.

An example would be creating a few minibots for a STEM Day using some spare Raspberry Pi 1s and some old Robot Radios. They can then each have code written with WPILib run on them and can also be controlled from the computer with the code running.

> [!NOTE]
> This project is in early development. Please read through the warnings and acknowledge the license before doing anything.

# How to use!

RoboPiLink has three main components:
* The WPILib Project (where any code you write would go)
* PiGPIO (runs on the Pi and allows remote pin access)
* The Watchdog (shuts off all pins if it detects a disconnection)

The WPILib Project is everything in the repository except `robopilinkd` and `robopilinkd.service`. The Watchdog is contained in the file `robopilinkd` and as a systemd service in `robopilinkd.service`. PiGPIO is installable as a Python package through Pip.

All new classes for RoboPiLink are in the [`lib/robopilink`](src/main/java/frc/lib/robopilink/) directory.

## Raspberry Pi Setup

### 1. Install the Watchdog Script

To prevent the RaspberryPi pins from retaining their values after a disconnect, a Watchdog script needs to remain running during the process.

> [!CAUTION]
> If the robot program abruptly ends or disconnects from the Raspberry Pi while pins are delivering power, and the watchdog is not runnning, **they will continue to deliver the last signal they were given**! This means a robot with more than a bit of power may become uncontrollable if you accidenally close the program without disabling, or the WiFi goes out! The following Python3 watchdog script on the Raspberry Pi will shut off all pins if it detects a lost connection while it is running. **While the program will work without this, this is useful for safety.**

```python
#!/usr/bin/env python3
import pigpio
import sys
import signal

pin = 2
timeout = 200

pi = pigpio.pi()

def callback(gpio, level, tick):
    if level == 2:
        print("RoboPiLink watchdog triggered!")
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

Get this program onto the RaspberryPi in an easily executable location. A home directory works very well for this. 

Installing the watchdog script can be done in a few ways, including but not limited to
* using [wget](https://phoenixnap.com/kb/wget-command-with-examples) and running ```wget https://raw.githubusercontent.com/mmilunicmobile/RoboPiLib/main/robopilinkd```
* using [scp](https://www.geeksforgeeks.org/scp-command-in-linux-with-examples/) to copy the file
* copy and pasting the contents of the file from above or the file into a file on the RaspberryPi

## Computer Setup

### 1. Pull Code

Use git to clone the code in this repository and open it in WPILib VSCode.

### 2. Install gpiozero and pigpio
Make sure that the computer you are running on has an installation of Python3 and also has gpiozero and pigpio installed as Python3 modules. 

To do the second part with pip, you can run `python3 -m pip install -u gpiozero pigpio`.

### 3. Change Hostname
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

Make sure that `"raspberrypi"` in the `RoboPiLink` creation is the hostname/local IP address of your Raspberry Pi. (By default this is `raspberrypi` hence the default.)

## Write Code

There are four types of GPIO pins built in you can create, described below. All of their constructors take a GPIO Pin which must not be 2. (That is used for watchdogging.) Most methods are pretty self explanitory.

### RPLInputDigital
This is for detecting input on a GPIO pin of the Raspberry Pi. Its constructor takes an argument as for whether it should use a pull up resistor (`true`) or a pull down resistor (`false`).

### RPLOutputDigital
This is for writing binary output to a GPIO pin of the Raspberry Pi. (High or low only.)

### RPLOutputPWM
This is for writing pseudo-analog output to a GPIO pin of the Raspberry Pi using PWM (hardward PWM if the pin supports it, software PWM otherwise). Values range from 0 (0% duty cycle) to 1 (100% duty cycle).

### RPLOutputServe
This is for writing PWM servo control output to a GPIO pin of the Raspberry Pi using PWM (hardware PWM if the pin supports it, software PWM otherwise). This is very useful for conrolling basically all FRC motor controllers that will accept PWM. They take an `OptionalDouble` as their value as they can also be set to no control.

A quick example is included below.

```java
//Robot Container Constructor
// Create a servo motor output on port 5
RPIOutputServo driveMotor = new RPIOutputServo(Robot.m_roboPiLink, 5);

// ...

// somewhere in robot periodic or a command or a subsystem
// Set the motor to go forwards at full output
driveMotor.setValue(OptionalDouble.of(1.0));

// Set the drive motor to coast/brake
driveMotor.setValue(OptionalDouble.empty());

// Gets the state of the drive motor
OptionalDouble value = driveMotor.getValue();
```

## Run Code

### 1. Start Pigpiod

Make sure you have Rasperry Pi OS running on the target Raspberry Pi. Make sure the target Raspberry Pi is connected to the same network as the computer that will be holding the code.

SSH into the Raspberry Pi and run `sudo pigpiod`.

> [!CAUTION]
> This above step will allow **all** remote connections via gpiozero to the device until the next time it shuts down and is not super secure. If you are worried about someone getting on your network and messing with the pins, you can use the `-n` option, which is documented on the [gpiozero docs](https://gpiozero.readthedocs.io/en/latest/remote_gpio.html#command-line-pigpiod).

### 2. Start the Watchdog

Run the watchdog script via `python3 robopilinkd`. Note that it is not actually a daemon and continues running in the foreground so it can be easily killed with `^C`.

### 3. Start the Code

Then run the code by pressing on the W in the top right corner, typing "sim" and choosing the simulate option. **This will attempt to connect to a real Raspberry Pi and run the code!** To actually simulate the code, make sure `isSimulation` is set to `true`.

## Setup Daemon (Optional)

Additionally, there is a systemd compatible [`robopilinkd.service`](robopilinkd.service) which will automatically setup pigpiod and the watchdog. If setup correctly the service will allow you to immediately run your WPILib code on a Pi following boot with no steps required. Instructions for setup are planned for a future version of the documentation. For now, there's a chance you can figure it out. **You do not need to do or understand this step.**
