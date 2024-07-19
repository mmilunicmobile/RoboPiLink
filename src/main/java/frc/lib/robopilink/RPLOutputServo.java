package frc.lib.robopilink;

import java.util.OptionalDouble;

import com.diozero.api.ServoDevice;

public class RPLOutputServo implements PigpiojDevice {
    private RoboPiLink pythonInterface;
    private int port;
    private OptionalDouble commandedValue = OptionalDouble.of(0.0);
    private OptionalDouble lastSentValue = OptionalDouble.of(0.0);
    private ServoDevice i;

    public RPLOutputServo(RoboPiLink pythonInterface, int port) {
        this.port = port;
        this.pythonInterface = pythonInterface;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }

        i = new ServoDevice.Builder(port).setInitialPulseWidthUs(0).setFrequency(50).build();

        pythonInterface.registerDevice(this);
    }

    public Runnable getDisabledInit() {
        return getSendValueString(OptionalDouble.empty());
    }

    public Runnable getEnabledPeriodic() {
        if (lastSentValue.equals(commandedValue)) return () -> {};
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

    private Runnable getSendValueString(OptionalDouble value) {
        lastSentValue = value;
        if (value.isPresent()) {
            // convert to microseconds
            double properOutput = value.getAsDouble() * 500 + 1500;
            return () -> i.setPulseWidthUs((int) properOutput);
        } else {
            return () -> i.setPulseWidthUs(0);
        }
    }
}
