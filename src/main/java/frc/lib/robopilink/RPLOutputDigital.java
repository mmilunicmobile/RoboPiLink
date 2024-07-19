package frc.lib.robopilink;

import com.diozero.api.DigitalOutputDevice;

public class RPLOutputDigital implements PigpiojDevice {
    private RoboPiLink pythonInterface;
    private int port;
    private boolean commandedValue = false;
    private boolean lastSentValue = false;
    private DigitalOutputDevice i;

    public RPLOutputDigital(RoboPiLink pythonInterface, int port) {
        this.port = port;
        this.pythonInterface = pythonInterface;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }

        i = new DigitalOutputDevice.Builder(port)
            .setDeviceFactory(pythonInterface.getDeviceFactory())
            .setInitialValue(false)
            .build();

        pythonInterface.registerDevice(this);
    }

    public Runnable getDisabledInit() {
        return getSendValueString(false);
    }

    public Runnable getEnabledPeriodic() {
        if (lastSentValue == commandedValue) return () -> {};
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

    private Runnable getSendValueString(boolean value) {
        lastSentValue = value;
        return () -> {
            i.setValue(value);
        };
    }
}
