package frc.lib.robopilink;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.DriverStation;
import uk.pigpioj.PigpioInterface;
import uk.pigpioj.PigpioJ;

public class RoboPiLink {
    private PigpioInterface m_pigpioInterface;

    private CopyOnWriteArrayList<PigpiojDevice> m_devices = new CopyOnWriteArrayList<PigpiojDevice>();

    private CopyOnWriteArrayList<Integer> m_devicePorts = new CopyOnWriteArrayList<Integer>();

    private Queue<Consumer<PigpioInterface>> m_commandQueue = new ConcurrentLinkedQueue<>();

    private String m_host;
    private boolean m_isSimulation;

    public RoboPiLink(String host, boolean simulate) {
        m_host = host;
        m_isSimulation = simulate;

        m_pigpioInterface = PigpioJ.newSocketImplementation(m_host);

        new Thread(commandRunner()).start();

        m_devicePorts.add(2);

        new Thread(pinger()).start();
    }

    public void startMainLoop() {
        new Thread(mainLoop()).start();
    }

    public String getHost() {
        return m_host;
    }

    public boolean isSimulation() {
        return m_isSimulation;
    }

    private Runnable pinger() {
        return () -> {
            while (true) {
                try {
                    sendCommand((PigpioInterface i) -> i.write(2, true));
                    Thread.sleep(100);
                    sendCommand((PigpioInterface i) -> i.write(2, false));
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private Runnable mainLoop() {
            return () -> {
            boolean previouslyDisabled = true;
            while (true) {
                try {
                    boolean currentlyDisabled = DriverStation.isDisabled() || DriverStation.isEStopped();
                    if (currentlyDisabled && !previouslyDisabled) {
                        // Disabled Init
                        disabledInit();
                        System.out.println("disabled init");
                    } else if (!currentlyDisabled && previouslyDisabled) {
                        // Enabled Init
                        enabledInit();
                        System.out.println("enabled init");
                    } else if (currentlyDisabled && previouslyDisabled) {
                        // Disabled Periodic
                        disabledPeriodic();
                        //System.out.println("disabled periodic");
                    } else if (!currentlyDisabled && !previouslyDisabled) {
                        // Enabled Periodic
                        enabledPeriodic();
                        //System.out.println("enabled periodic");
                    }
                    previouslyDisabled = currentlyDisabled;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public boolean isPortTaken(int port) {
        return m_devicePorts.contains(port);
    }

  private Runnable commandRunner() {
    return () -> {
        while (true) {
            Consumer<PigpioInterface> command = m_commandQueue.poll();
            if (command != null) {
                sendCommandLocal(command);
            }
        }
    };
  }

  private void sendCommandLocal(Consumer<PigpioInterface> command) {
        command.accept(m_pigpioInterface);
    }

  /**
   * sends a command to the python process
   * 
   * explicitly sends the string sent in. a '\n' at the end of the command is usually needed.
   * @param command
   */
  public void sendCommand(Consumer<PigpioInterface> command) {
    m_commandQueue.add(command);
  }

  private void enabledInit() {
    for (PigpiojDevice device : m_devices) {
        sendCommand(device.getEnabledInit());
    }
  }

  private void enabledPeriodic() {
    for (PigpiojDevice device : m_devices) {
        sendCommand(device.getEnabledPeriodic());
    }
  }

  private void disabledInit() {
    for (PigpiojDevice device : m_devices) {
        sendCommand(device.getDisabledInit());
    }
  }

  private void disabledPeriodic() {
    for (PigpiojDevice device : m_devices) {
        sendCommand(device.getDisabledPeriodic());
    }
  }

  public void registerDevice(PigpiojDevice device) {
    m_devices.add(device);
  }

  public void block() {
    while(!m_commandQueue.isEmpty()) {}
  }
}
