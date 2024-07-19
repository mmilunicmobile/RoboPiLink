package frc.lib.robopilink;

import java.util.Queue;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.diozero.api.DigitalOutputDevice;
import com.diozero.internal.provider.mock.MockDeviceFactory;
import com.diozero.internal.provider.pigpioj.PigpioJDeviceFactory;

import edu.wpi.first.wpilibj.DriverStation;

public class RoboPiLink {

    private CopyOnWriteArrayList<PigpiojDevice> m_devices = new CopyOnWriteArrayList<PigpiojDevice>();

    private CopyOnWriteArrayList<Integer> m_devicePorts = new CopyOnWriteArrayList<Integer>();

    private Queue<Runnable> m_commandQueue = new ConcurrentLinkedQueue<>();

    private DigitalOutputDevice m_ping_pin;


    private String m_host;
    private boolean m_isSimulation;

    public RoboPiLink(String host, boolean simulate) {
        m_host = host;
        m_isSimulation = simulate;

        if (m_isSimulation) {
            new MockDeviceFactory();
        } else {
            PigpioJDeviceFactory.newSocketInstance(host);
        }

        new Thread(commandRunner()).start();

        m_ping_pin = new DigitalOutputDevice(2);

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
                    sendCommand(() -> m_ping_pin.toggle());
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
            Runnable command = m_commandQueue.poll();
            if (command != null) {
                sendCommandLocal(command);
            }
        }
    };
  }

  private void sendCommandLocal(Runnable command) {
        command.run();
    }

  /**
   * sends a command to the python process
   * 
   * explicitly sends the string sent in. a '\n' at the end of the command is usually needed.
   * @param command
   */
  public void sendCommand(Runnable command) {
    command.run();
    //m_commandQueue.add(command);
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
