// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.OptionalDouble;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.robopilink.RPLOutputDigital;
import frc.lib.robopilink.RPLOutputServo;
import frc.robot.Robot;

public class ExampleSubsystem extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */

  public ExampleSubsystem() {
    setDefaultCommand(exampleMethodCommand());

    //Robot Container Constructor
// Create a servo motor output on port 5
    RPLOutputServo driveMotor = new RPLOutputServo(Robot.m_roboPiLink, 5);

// ...

// somewhere in robot periodic or a command or a subsystem
// Set the motor to go forwards at full output
driveMotor.setValue(OptionalDouble.of(1.0));

// Set the drive motor to coast/brake
driveMotor.setValue(OptionalDouble.empty());

// Gets the state of the drive motor
OptionalDouble value = driveMotor.getValue();

  }

  /**
   * Example command factory method.
   *
   * @return a command
   */
  public Command exampleMethodCommand() {
    // Inline construction of command goes here.
    // Subsystem::RunOnce implicitly requires `this` subsystem.
    return run(
        () -> {
        });
  }

  /**
   * An example method querying a boolean state of the subsystem (for example, a digital sensor).
   *
   * @return value of some boolean subsystem state, such as a digital sensor.
   */
  public boolean exampleCondition() {
    // Query some boolean state, such as a digital sensor.
    return false;
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
