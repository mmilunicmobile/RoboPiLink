package frc.lib.robopilink;


import uk.pigpioj.PigpioConstants;
import uk.pigpioj.PigpioInterface;

import java.util.function.Consumer;

public class RPLOutputDigital implements PigpiojDevice{
    private RoboPiLink pythonInterface;
    private int port;
    private boolean commandedValue = false;
    private boolean lastSentValue = false;

    public RPLOutputDigital(RoboPiLink pythonInterface, int port) {
        this.port = port;
        this.pythonInterface = pythonInterface;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }

        pythonInterface.sendCommand((i) -> {
            i.setMode(port, PigpioConstants.MODE_PI_OUTPUT);
            i.write(port, false);
        });

        pythonInterface.registerDevice(this);

        pythonInterface.block();
    }

    public Consumer<PigpioInterface> getDisabledInit() {
        return getSendValueString(false);
    }

    public Consumer<PigpioInterface> getEnabledPeriodic() {
        if (lastSentValue == commandedValue) return (i) -> {};
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

    private Consumer<PigpioInterface> getSendValueString(boolean value) {
        lastSentValue = value;
        return (i) -> {
            i.write(port, value);
        };
    }
}
