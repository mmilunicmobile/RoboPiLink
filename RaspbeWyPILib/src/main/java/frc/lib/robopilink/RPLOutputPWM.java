package frc.lib.robopilink;

import java.util.Optional;

public class RPLOutputPWM implements PythonDevice {
    String variableName;
    RoboPiLink pythonInterface;
    int port;
    double commandedValue = 0.0;
    double lastSentValue = 0.0;

    public RPLOutputPWM(RoboPiLink pythonInterface, int port) {
        this.port = port;
        this.pythonInterface = pythonInterface;
        this.variableName = "PWM" + port;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }
        
        pythonInterface.sendCommand(
            variableName + " = gpiozero.PWMLED(" + port + ", pin_factory=factory)\n" +
            variableName + ".value = 0\n"
        );

        pythonInterface.block();

        pythonInterface.registerDevice(this);
    }

    public Optional<String> getDisabledInit() {
        return getSendValueString(0);
    }

    public Optional<String> getEnabledPeriodic() {
        if (lastSentValue == commandedValue) return Optional.empty();
        return getSendValueString(commandedValue);
    }

    public void setValue(double value) {
        commandedValue = value;
    }

    public void setOn() {
        setValue(1.0);
    }

    public void setOff() {
        setValue(0.0);
    }

    public double getValue() {
        return lastSentValue;
    }

    public int getPort() {
        return port;
    }

    private Optional<String> getSendValueString(double value) {
        lastSentValue = value;
        return Optional.of(variableName + ".value = " + value);
    }
}
