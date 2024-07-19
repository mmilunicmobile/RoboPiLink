# RoboPiLink

[Skip to the How To Use!](#how-to-use)

This is a base WPILib project that can interface with **any** Raspberry Pi over a network connection!

![Logo](https://github.com/user-attachments/assets/099c9651-5aac-41bd-bd3d-5a77cc09ec8d)

### Why would I use this?
If you have excess Raspberry Pis you can use them as a platform to teach WPILib on! This creates a similar avenue to that of the Romi and XRP, but accessible to any Raspberry Pi.

An example would be creating a few minibots for a STEM Day using some spare Raspberry Pi 1s and some old Robot Radios. They can then each have code written with WPILib run on them and can also be controlled from the computer with the code running.

> [!NOTE]
> This project is in early development. Please read through the warnings and acknowledge the license before doing anything.

RoboPiLink has three main components:
* The WPILib Project (where any code you write would go)
* PiGPIO (runs on the Pi and allows remote pin access)
* The Watchdog (shuts off all pins if it detects a disconnection)

The WPILib Project is everything in the repository except `robopilinkd` and `robopilinkd.service`. The Watchdog is contained in the file `robopilinkd` and as a systemd service in `robopilinkd.service`. PiGPIO is installable as a Python package through Pip.

All new classes for RoboPiLink are in the [`lib/robopilink`](src/main/java/frc/lib/robopilink/) directory.

# How to Use!

## Script Install

Clone this repository into the directory you want your robot project to be in. 

You can do this using git like this:
```
git clone https://github.com/mmilunicmobile/RoboPiLink.git
```

Then run 
```
python3 rplsetup.py pi raspberrypi
```
but replace `pi` with the username for your pi and `raspberrypi` with the hostname of your RaspberryPi. This will likely ask for the password to your RaspberryPi as it needs to ssh into it. The RaspberryPi **must** be running Raspbian.

## Manual Install

Alternatively, you can follow [these directions](docs/MANUAL-INSTALL.md) to manually install.

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
//Robot Container Constructor or Subsystem Init
// Create a servo motor output on port 5
RPLOutputServo driveMotor = new RPIOutputServo(Robot.m_roboPiLink, 5);

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

Run the code by pressing on the W in the top right corner, typing "sim" and choosing the simulate option. **This will attempt to connect to a real Raspberry Pi and run the code!** To actually simulate the code, make sure `isSimulation` is set to `true`.
