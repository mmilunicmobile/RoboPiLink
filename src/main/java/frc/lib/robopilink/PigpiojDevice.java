package frc.lib.robopilink;

import java.util.function.Consumer;
import uk.pigpioj.PigpioInterface;

public interface PigpiojDevice {
    public int getPort();
    public default Consumer<PigpioInterface> getDisabledInit() {return (i) -> {};}
    public default Consumer<PigpioInterface> getDisabledPeriodic() {return (i) -> {};}
    public default Consumer<PigpioInterface> getEnabledInit() {return (i) -> {};}
    public default Consumer<PigpioInterface> getEnabledPeriodic() {return (i) -> {};}
}
