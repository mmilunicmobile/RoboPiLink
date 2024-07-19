package frc.lib.robopilink;


import com.diozero.api.PwmOutputDevice;


public class RPLOutputPWM implements PigpiojDevice {
    private RoboPiLink pythonInterface;
    private int port;
    private double commandedValue = 0.0;
    private double lastSentValue = 0.0;
    private PwmOutputDevice i;

    public RPLOutputPWM(RoboPiLink pythonInterface, int port) {
        this.port = port;
        this.pythonInterface = pythonInterface;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }

        i = new PwmOutputDevice(port);
        
        i.setValue(0);

        pythonInterface.registerDevice(this);
    }

    public Runnable getDisabledInit() {
        return getSendValueString(0);
    }

    public Runnable getEnabledPeriodic() {
        if (lastSentValue == commandedValue) return () ->{};
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

    private Runnable getSendValueString(double value) {
        lastSentValue = value;
        return () -> {
            i.setValue(0);
        };
    }
}
