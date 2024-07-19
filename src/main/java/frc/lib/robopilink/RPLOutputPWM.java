package frc.lib.robopilink;

import java.util.Optional;
import java.util.function.Consumer;

import uk.pigpioj.PigpioInterface;

public class RPLOutputPWM implements PigpiojDevice {
    RoboPiLink pythonInterface;
    int port;
    double commandedValue = 0.0;
    double lastSentValue = 0.0;

    public RPLOutputPWM(RoboPiLink pythonInterface, int port) {
        this.port = port;
        this.pythonInterface = pythonInterface;

        if (pythonInterface.isPortTaken(port)) {
            throw new RuntimeException("port " + port + " is already in use on RPi");
        }
        
        pythonInterface.sendCommand(
            () -> {
                
            }
            variableName + " = gpiozero.PWMLED(" + port + ", pin_factory=factory)\n" +
            variableName + ".value = 0\n"
        );

        pythonInterface.registerDevice(this);

        pythonInterface.block();
    }

    public Consumer<PigpioInterface> getDisabledInit() {
        return getSendValueString(0);
    }

    public Consumer<PigpioInterface> getEnabledPeriodic() {
        if (lastSentValue == commandedValue) return (i) ->{};
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

    private Consumer<PigpioInterface> getSendValueString(double value) {
        lastSentValue = value;
        return (i) -> {
            i.setPWMDutyCycle(port, (int) (255 * value));
        };
    }
}
