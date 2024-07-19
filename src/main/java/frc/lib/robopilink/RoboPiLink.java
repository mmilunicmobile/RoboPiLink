package frc.lib.robopilink;

import java.util.OptionalInt;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.diozero.api.DigitalOutputDevice;
import com.diozero.internal.provider.mock.MockDeviceFactory;
import com.diozero.internal.provider.pigpioj.PigpioJDeviceFactory;
import com.diozero.internal.spi.BaseNativeDeviceFactory;
import com.diozero.sbc.BoardPinInfo;

import edu.wpi.first.wpilibj.DriverStation;

public class RoboPiLink {

    private CopyOnWriteArrayList<PigpiojDevice> m_devices = new CopyOnWriteArrayList<PigpiojDevice>();

    private CopyOnWriteArrayList<Integer> m_devicePorts = new CopyOnWriteArrayList<Integer>();

    private Queue<Runnable> m_commandQueue = new ConcurrentLinkedQueue<>();

    private DigitalOutputDevice m_ping_pin;

    private BaseNativeDeviceFactory m_deviceFactory;

    public RoboPiLink(BaseNativeDeviceFactory deviceFactory, OptionalInt pingPin) {
        m_deviceFactory = deviceFactory;

        new Thread(commandRunner()).start();

        if (pingPin.isPresent()) {
            m_ping_pin = new DigitalOutputDevice.Builder(pingPin.getAsInt()).setDeviceFactory(m_deviceFactory).build();
            m_devicePorts.add(pingPin.getAsInt());
            new Thread(pinger()).start();
        }
    }

    public static RoboPiLink remotePi(String host, boolean simulate) {
        if (simulate) {
            MockDeviceFactory mock = new MockDeviceFactory();
            BoardPinInfo info = mock.getBoardPinInfo();
            new MockBoardConfigurator().configure(info);
            return new RoboPiLink(mock, OptionalInt.empty());
        } else {
            return new RoboPiLink(PigpioJDeviceFactory.newSocketInstance(host), OptionalInt.of(2));
        }
    }

    public void startMainLoop() {
        new Thread(mainLoop()).start();
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

  public BaseNativeDeviceFactory getDeviceFactory() {
    return m_deviceFactory;
  }
}
