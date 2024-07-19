package frc.lib.robopilink;

public interface PigpiojDevice {
    public int getPort();
    public default Runnable getDisabledInit() {return () -> {};}
    public default Runnable getDisabledPeriodic() {return () -> {};}
    public default Runnable getEnabledInit() {return () -> {};}
    public default Runnable getEnabledPeriodic() {return () -> {};}
}
