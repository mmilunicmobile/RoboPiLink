package frc.lib.robopilink;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.wpi.first.wpilibj.DriverStation;

public class RoboPiLink {
    public final String OUTPUT_KEY = "DATATRANSFER";

    private Process m_pythonProcess;

    private BufferedReader m_pythonProcessInput;

    private BufferedWriter m_pythonProcessOutput;

    private Map<String, Double> m_variableMap = new ConcurrentHashMap<String, Double>();

    private CopyOnWriteArrayList<PythonDevice> m_devices = new CopyOnWriteArrayList<PythonDevice>();

    private CopyOnWriteArrayList<Integer> m_devicePorts = new CopyOnWriteArrayList<Integer>();

    private Queue<String> m_commandQueue = new ConcurrentLinkedQueue<String>();

    private String m_host;
    private boolean m_isSimulation;

    public RoboPiLink(String host, boolean simulate) {
        this.m_host = host;
        this.m_isSimulation =simulate;

        ProcessBuilder processBuilder = new ProcessBuilder("python3", "-i");

        processBuilder.redirectErrorStream(true);

        try {
        m_pythonProcess = processBuilder.start();
        System.out.println("python process started");
        } catch (IOException e) {
        throw new RuntimeException("python process failed to start", e);
        }

        m_pythonProcessInput = new BufferedReader(new InputStreamReader(m_pythonProcess.getInputStream()));

        m_pythonProcessOutput = new BufferedWriter(new OutputStreamWriter(m_pythonProcess.getOutputStream()));

        new Thread(pythonPrinter()).start();
        new Thread(pythonSender()).start();

        m_devicePorts.add(2);

        startPigpiodComs();
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


    public void startPigpiodComs() {
        sendCommand(
        """
        import gpiozero
        from gpiozero.pins.pigpio import PiGPIOFactory
        from gpiozero.pins.mock import MockFactory, MockPWMPin
        #import os
        #os.system('say I am booting up! &')

        disable_calls = []

        def disable():
            #print("\\nDisabling")
            for call in disable_calls:
                call()
        
        enable_calls = []

        def enable():
            #print("\\nEnabling")
            os.system('say I am Enabling! &')
            for call in enable_calls:
                call()
        
        logging_calls = []

        def logger():
            for call in logging_calls:
                call()
        
        """);
        //sendCommand("factory = PiGPIOFactory(host='" + host + "')\n");
        if (m_isSimulation) {
            sendCommand("factory = MockFactory(pin_class=MockPWMPin)\n");
        } else {
            sendCommand("factory = PiGPIOFactory(host='" + m_host + "')\n");
        }
        //
        sendCommand(
        """
        ping_pin = gpiozero.LED(2, pin_factory=factory)
        ping_pin.on()
        ping_pin.off()

        """);
        block();

        new Thread(pinger()).start();
    }

    private Runnable pinger() {
        return () -> {
            while (true) {
                try {
                    sendCommand("ping_pin.toggle()\n");
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

    public void block() {
        sendCommand("print(\"\\n" + OUTPUT_KEY + ":blocking:1\")\n");
        while (getValue("blocking") == 0) {}
        sendCommand("print(\"\\n" + OUTPUT_KEY + ":blocking:0\")\n");
        while (getValue("blocking") == 1) {}
    }

    public boolean isPortTaken(int port) {
        return m_devicePorts.contains(port);
    }

    private Runnable pythonPrinter() {
        return () -> {
        String line;
            while (true) {
            try {
            line = m_pythonProcessInput.readLine();
            //System.out.println(line);
            try {
                if (line.startsWith(OUTPUT_KEY)) {
                    String[] parts = line.split(":");
                    if (parts[2].equals("None")) {
                        parts[2] = "0.0";
                    }
                    m_variableMap.put(parts[1], Double.parseDouble(parts[2]));
                } else if (line.startsWith("PRINT")){
                    String[] parts = line.split(":");
                    System.out.println(parts[1]);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            } catch (IOException e) {
            throw new RuntimeException("python process failed to read", e);
            }
        }
    };
  }

  private Runnable pythonSender() {
    return () -> {
        while (true) {
          try {
            String command = m_commandQueue.poll();
            if (command != null) {
                sendCommandLocal(command);
            }
                  } catch (RuntimeException e) {
        throw new RuntimeException("python process failed to write", e);
      }
        }
    };
  }

  private void sendCommandLocal(String command) {
    try {
        m_pythonProcessOutput.write(command);
        m_pythonProcessOutput.flush();
        } catch (IOException e) {
        throw new RuntimeException("python process failed to write", e);
        }
    }

  /**
   * sends a command to the python process
   * 
   * explicitly sends the string sent in. a '\n' at the end of the command is usually needed.
   * @param command
   */
  public void sendCommand(String command) {
    m_commandQueue.add(command + "\n");
  }
  /**
   * gets a value from the python process sent out using the data transfer key and method
   * @param value
   * @return
   */
  public double getValue(String value) {
      Double result = m_variableMap.get(value);
      if (result == null) {
        return 0.0;
      }
      return result;
  }

  private void enabledInit() {
    for (PythonDevice device : m_devices) {
        Optional<String> command = device.getEnabledInit();
        if (command.isPresent()) {
            sendCommand(command.get());
            //System.out.println(command.get());
            block();
        }
    }
  }

  private void enabledPeriodic() {
    for (PythonDevice device : m_devices) {
        Optional<String> command = device.getEnabledPeriodic();
        if (command.isPresent()) {
            sendCommand(command.get());
            //System.out.println(command.get());
            block();
        }
    }
  }

  private void disabledInit() {
    for (PythonDevice device : m_devices) {
        Optional<String> command = device.getDisabledInit();
        if (command.isPresent()) {
            sendCommand(command.get());
            //System.out.println(command.get());
            block();
        }
    }
  }

  private void disabledPeriodic() {
    for (PythonDevice device : m_devices) {
        Optional<String> command = device.getDisabledPeriodic();
        if (command.isPresent()) {
            sendCommand(command.get());
            //System.out.println(command.get());
            block();
        }
    }
  }

  public void registerDevice(PythonDevice device) {
    m_devices.add(device);
  }
}
