package frc.lib.robopilink;

import java.util.Optional;

public class RPLInputDigital implements PythonDevice {
    String variableName;
    RoboPiLink pythonInterface;
    int port;
    boolean pullUp;

    public RPLInputDigital(RoboPiLink pythonInterface, int port, boolean pullUp) {
        this.port = port;
        this.pythonInterface = pythonInterface;
        this.variableName = "INPUT" + port;
        this.pullUp = pullUp;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }
        
        pythonInterface.sendCommand(
            variableName + " = gpiozero.InputDevice(" + port + ", pin_factory=factory)\n" +
            variableName + ".pull_up = " + (pullUp ? "True" : "False") + "\n"
        );

        pythonInterface.block();

        pythonInterface.registerDevice(this);
    }

    public int getPort() {
        return port;
    }

    public boolean getValue() {
        return pythonInterface.getValue(variableName) == 1.0;
    }

    private Optional<String> getLoggingPeriodic() {
        return Optional.of("print('\\n" + pythonInterface.OUTPUT_KEY + ":" + variableName + ":' + str(" + variableName + ".value))");
    }

    public Optional<String> getDisabledPeriodic() {
        return getLoggingPeriodic();
    }

    public Optional<String> getEnabledPeriodic() {
        return getLoggingPeriodic();
    }
}
