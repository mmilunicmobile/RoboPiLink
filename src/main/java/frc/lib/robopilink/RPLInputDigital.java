package frc.lib.robopilink;

import java.util.function.Consumer;

import uk.pigpioj.PigpioConstants;
import uk.pigpioj.PigpioInterface;

public class RPLInputDigital implements PigpiojDevice {
    private RoboPiLink pythonInterface;
    private int port;
    private boolean pullUp;
    boolean value = false;

    public RPLInputDigital(RoboPiLink pythonInterface, int port, boolean pullUp) {
        this.port = port;
        this.pythonInterface = pythonInterface;
        this.pullUp = pullUp;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }

        pythonInterface.sendCommand((i) -> {
            i.setMode(port, PigpioConstants.MODE_PI_INPUT);
            i.setPullUpDown(port, pullUp ? PigpioConstants.PI_PUD_UP : PigpioConstants.PI_PUD_DOWN );
        });

        pythonInterface.registerDevice(this);

        pythonInterface.block();
    }

    public int getPort() {
        return port;
    }

    public boolean getValue() {
        return value;
    }

    private Consumer<PigpioInterface> getLoggingPeriodic() {
        return (i) -> {
            value = i.read(port) == 1;
        };
    }

    public Consumer<PigpioInterface> getDisabledPeriodic() {
        return getLoggingPeriodic();
    }

    public Consumer<PigpioInterface> getEnabledPeriodic() {
        return getLoggingPeriodic();
    }
}
