package frc.lib.robopilink;

import com.diozero.api.DigitalInputDevice;
import com.diozero.api.GpioPullUpDown;

public class RPLInputDigital implements PigpiojDevice {
    @SuppressWarnings("unused")
    private RoboPiLink pythonInterface;
    private int port;
    private GpioPullUpDown pullUp;
    private boolean value = false;
    private DigitalInputDevice i;

    public RPLInputDigital(RoboPiLink pythonInterface, int port) {
        this(pythonInterface, port, GpioPullUpDown.PULL_UP);
    }

    public RPLInputDigital(RoboPiLink pythonInterface, int port, GpioPullUpDown pullUpDown) {
        this.port = port;
        this.pythonInterface = pythonInterface;
        this.pullUp = pullUpDown;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }

        i = new DigitalInputDevice.Builder(port).setPullUpDown(pullUp).setDeviceFactory(pythonInterface.getDeviceFactory()).build();

        pythonInterface.registerDevice(this);
    }

    public int getPort() {
        return port;
    }

    public boolean getValue() {
        return value;
    }

    private Runnable getLoggingPeriodic() {
        return () -> {
            value = i.getValue();
        };
    }

    public Runnable getDisabledPeriodic() {
        return getLoggingPeriodic();
    }

    public Runnable getEnabledPeriodic() {
        return getLoggingPeriodic();
    }

    public GpioPullUpDown getPullUpDown() {
        return pullUp;
    }
}
