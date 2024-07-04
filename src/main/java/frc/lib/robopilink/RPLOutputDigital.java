package frc.lib.robopilink;

import java.util.Optional;

public class RPLOutputDigital implements PythonDevice{
    String variableName;
    RoboPiLink pythonInterface;
    int port;
    boolean commandedValue = false;
    boolean lastSentValue = false;

    public RPLOutputDigital(RoboPiLink pythonInterface, int port) {
        this.port = port;
        this.pythonInterface = pythonInterface;
        this.variableName = "OUT" + port;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }
        
        pythonInterface.sendCommand(
            variableName + " = gpiozero.OutputDevice(" + port + ", pin_factory=factory)\n" +
            variableName + ".value = 0\n"
        );

        pythonInterface.block();

        pythonInterface.registerDevice(this);
    }

    public Optional<String> getDisabledInit() {
        return getSendValueString(false);
    }

    public Optional<String> getEnabledPeriodic() {
        if (lastSentValue == commandedValue) return Optional.empty();
        return getSendValueString(commandedValue);
    }

    public void setOn() {
        setValue(true);
    }

    public void setOff() {
        setValue(false);
    }

    public void setValue(boolean value) {
        commandedValue = value;
    }

    public int getPort() {
        return port;
    }

    public boolean getValue() {
        return lastSentValue;
    }

    private Optional<String> getSendValueString(boolean value) {
        lastSentValue = value;
        return Optional.of(variableName + ".value = " + (value ? "1" : "0"));
    }
}
