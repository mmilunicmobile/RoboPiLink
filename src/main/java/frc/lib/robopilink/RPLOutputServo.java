package frc.lib.robopilink;

import java.util.Optional;
import java.util.OptionalDouble;

public class RPLOutputServo implements PigpiojDevice {
    String variableName;
    RoboPiLink pythonInterface;
    int port;
    OptionalDouble commandedValue = OptionalDouble.of(0.0);
    OptionalDouble lastSentValue = OptionalDouble.of(0.0);

    public RPLOutputServo(RoboPiLink pythonInterface, int port) {
        this.port = port;
        this.pythonInterface = pythonInterface;
        this.variableName = "SERVO" + port;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }
        
        pythonInterface.sendCommand(
            variableName + " = gpiozero.Servo(" + port + ", pin_factory=factory)\n" +
            variableName + ".value = None\n"
        );

        pythonInterface.registerDevice(this);

        pythonInterface.block();
    }

    public Optional<String> getDisabledInit() {
        return getSendValueString(OptionalDouble.empty());
    }

    public Optional<String> getEnabledPeriodic() {
        if (lastSentValue.equals(commandedValue)) return Optional.empty();
        return getSendValueString(commandedValue);
    }

    public void setValue(OptionalDouble value) {
        commandedValue = value;
    }

    public OptionalDouble getValue() {
        return lastSentValue;
    }

    public int getPort() {
        return port;
    }

    private Optional<String> getSendValueString(OptionalDouble value) {
        lastSentValue = value;
        if (value.isPresent()) {
            return Optional.of(variableName + ".value = " + value.getAsDouble());
        } else {
            return Optional.of(variableName + ".value = None");
        }
    }
}
