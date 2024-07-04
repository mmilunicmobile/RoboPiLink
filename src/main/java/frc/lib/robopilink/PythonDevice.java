package frc.lib.robopilink;

import java.util.Optional;

public interface PythonDevice {
    public int getPort();
    public default Optional<String> getDisabledInit() {return Optional.empty();}
    public default Optional<String> getDisabledPeriodic() {return Optional.empty();}
    public default Optional<String> getEnabledInit() {return Optional.empty();}
    public default Optional<String> getEnabledPeriodic() {return Optional.empty();}
}
