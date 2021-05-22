package frc.robot;

/*
  Project: 2648InfiniteRechargeRebuild
  Description: A rework of code that was lost so we can make the robot move again
  Authors: Brad B and Marcelino C
  Date Created: 5/13/2021
*/

//Our imports, imports allow access to other "types"
//A type is often a representation of a real world thing (although it doesn't necessarily have to be)
//Types can be as simple as an integer, or a single character
//Types can be as complicated as representing a compressor, a motor controller, etc.
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

//Our robot class
//This is where all the code that makes the robot work goes
//By defining this "class" (another word for it could be a template), we can create a "type"
//used by the RoboRIO to make the robot function
public class Robot extends TimedRobot {

  //Our Initialization Variables
  //An Initialization Variable is something that is "part of" our type.
  //These variables help us to access or manipulate different parts of our robot
  //Currently, all the variables you see below are "empty" (or null)
  //We'll fill them in and make them usful down below in robotInit()

  //Our SpeedController Initialization Variables
  //SpeedControllers are any device on the robot that makes a motor move
  //Some motors require separate devices as SpeedControllers (Victors, Talons, SPARKS)
  //Some motors have built in SpeedControllers (Falcons)
  //SpeedController is sort of a "generic type". It's good to create your variables
  //with generic types sometimes, usually it's done to make code more flexible.
  //We have SpeedControllers for each of the 4 drivetrain motors, as well as the intake motor
  private SpeedController frontLeft;
  private SpeedController frontRight;
  private SpeedController rearLeft;
  private SpeedController rearRight;
  private SpeedController intake;
  private SpeedController ballFeeder;
  private SpeedController shooter1;
  private SpeedController shooter2;
  private SpeedController hoodMotor;

  //Our SpeedControllerGroup Initialization Variables
  //SpeedControllerGroups are designed to take multiple SpeedController variables,
  //and collect them into one "thing" that you can control.
  //In this case, we've created left and right side representations of our drivetrain
  private SpeedControllerGroup left;
  private SpeedControllerGroup right;
  private SpeedControllerGroup shooter;

  //Our DifferentialDrive Initialization Variable
  //A DifferentialDrive is what our robot uses to move. It's a combination
  //of motors, chain, spockets, and wheels.
  //There's are several types of "drives" available to us, but for the most part
  //you'll be getting familiar with the DifferentialDrive
  private DifferentialDrive drivetrain;

  //Our XboxController Initialization Variable
  //Our robot communicates with the laptop used to control the robot
  //to "expose" USB XboxControllers and Joysticks for us to use.
  //This XboxController variable will give us access to the buttons, joysticks,
  //dpad, and even the rumble motors.
  private XboxController controller;

  //Our Compressor Initialization Variable
  //Our robot uses compressed air to shift between high and low gear.
  //In a very basic sense, it's similar to how a car or truck shifts gears.
  //A compressor simply takes air from the space surrounding it, and jams
  //it into a tank, compressing it and storing it for later that we can use
  //to do "work"
  private Compressor compressor;

  //Our DoubleSolenoid Initialization Variable
  //A DoubleSolenoid is a way of "directing" compressed air.
  //By sending air in one direction, we can do one thing, by sending air
  //in the opposite direction, we can do another thing.
  //This DoubleSolenoid allows us to shift between high and low gear
  private DoubleSolenoid shifter;

  //Our DigitalInput Initialization Variables
  //A DigitalInput is a way of sensing something that can either be on
  //or off (true or false). In this use case, we are using these DigitalInputs
  //to detect whether or not our hood is all the way up, or all the way down.
  //Knowing this will help us prevent the motor that makes the hood move
  //from breaking the plastic that it's made out of.
  //These limit switches are true if they are NOT pressed, and false if they are
  //I know this may seem a little backwards logically, but we'll make it work
  private DigitalInput hoodDownLimitSwitch;
  private DigitalInput hoodUpLimitSwitch;

  //Our robotInit() Method
  //A method in the simplest terms is something the robot can "do".
  //In this case, the robotInit method is where you should 
  //"fill in" any initialization variables that you created above
  //This method is "activated" (or called) once when the robot first
  //finishes booting up
  public void robotInit() {
    //The first thing we do is setup our drivetrain and shooter motors.
    //We use NEO motors on our drivetrain and shooter, so we need to make use
    //of the SPARK MAX motor controllers. We "fill in" our variables
    //using "new CANSparkMax(canID, motortype)"
    //The numbers represent CAN bus IDs, these are specially assigned
    //to each controller, so that communication between the RoboRIO
    //and each individual controller can happen.
    //Motor types are a little bit off topic, for NEO motors, the motor type
    //should ALWAYS be kBrushless, changing this could destroy the motor and the controller
    frontLeft = new CANSparkMax(25, MotorType.kBrushless);
    frontRight = new CANSparkMax(28, MotorType.kBrushless);
    rearLeft = new CANSparkMax(22, MotorType.kBrushless);
    rearRight = new CANSparkMax(27, MotorType.kBrushless);
    shooter1 = new CANSparkMax(21, MotorType.kBrushless);
    shooter2 = new CANSparkMax(23, MotorType.kBrushless);

    //We use a CIM motor on our intake and ball feeder, and we use
    //a window motor for our shooter hood, so we need to make use
    //of a VictorSPX motor controller. We "fill in" this variable
    //using "new WPI_VictorSPX(canID)"
    intake = new WPI_VictorSPX(11);
    ballFeeder = new WPI_VictorSPX(14);
    hoodMotor = new WPI_VictorSPX(13);

    //Next, we fill in our SpeedControllerGroups using
    //new SpeedControllerGroup(listOfSpeedControllers)
    //You can put any number of SpeedControllers into a SpeedControllerGroup,
    //for our purposes though, each group only gets two variables each, two
    //on the left side of the drivetain, two on the right, and then
    //two for the shooter (the shooter has two motors that make it work).
    left = new SpeedControllerGroup(frontLeft, rearLeft);
    right = new SpeedControllerGroup(frontRight, rearRight);
    shooter = new SpeedControllerGroup(shooter1, shooter2);

    //Next, we setup our DifferentialDrive using
    //new DifferentialDrive(leftSideOfDrivetrain, rightSideOfDrivetrain)
    //You can see here why we ended up creating the SpeedControllerGroups,
    //we wouldn't be able to provide all 4 motors with what DifferentialDrive
    //accepts for types. So we use the SpeedControllerGroups to "combine"
    //motors together into left and right sides.
    drivetrain = new DifferentialDrive(left, right);

    //Next, we set up our XboxController using
    //new XboxController(usbID)
    //The usbID you need to use comes from the Driver's Station Software used
    //to control the robot from a computer. Generally speaking, your 
    //first controller you create should use USB ID 0. 
    controller = new XboxController(0);

    //Next, we set up our Compressor
    //Compressors, by default, don't need anything special to set them up.
    //We do something special though as an added step on the second line.
    //compressor.setClosedLoopControl(true) makes it so that the compressor
    //uses a special sensor to automatically detect when it needs to turn on
    //to compress more air.
    compressor = new Compressor();
    compressor.setClosedLoopControl(true);

    //Next, we set up our DoubleSolenoid using
    //new DoubleSolenoid(valveID1, valveID2)
    //We need to specify two valve numbers because sending an "on"
    //value to one ID sends air one direction, sending "on" to the other ID
    //sends air in the other direction (these directions are usually called
    //forward and reverse)
    //These IDs come from whatever the DoubleSolenoid is plugged into on
    //the Pneumatics Control Module (PCM). 
    shifter = new DoubleSolenoid(0, 1);

    //Next, we set up our DigitalInputs using
    //new DigitalInput(DIOID)
    //We need to specify a value when creating a DigitalInput
    //that represents what Digital Input/Output (DIO) channel
    //we've plugged into on the RoboRIO. In this case, the limit switch that
    //checks to see if the hood is all the way down is plugged into DIO 0,
    //and the limit switch that checks to see if the hood is all the way up
    //is plugged into DIO 1
    hoodDownLimitSwitch = new DigitalInput(0);
    hoodUpLimitSwitch = new DigitalInput(1);
  }

  //Ignore me for now
  public void robotPeriodic() {}

  //Ignore me for now
  public void autonomousInit() {}

  //Ignore me for now
  public void autonomousPeriodic() {}

  //Ignore me for now
  public void teleopInit() {}

  //Our teleopPeriodic() Method
  //This method is "activated" (or called) about 50 times a second
  //whenever the robot is enabled, and in the teleoperated mode on the
  //Driver's Station. This method defines all of the stuff the robot should
  //be able to do when you are in control.
  public void teleopPeriodic() {
    //This is an example of "activating" (or calling) a method
    //In this instance we are calling on the drivetrain to move using the joystick
    //on the XboxController. We need to provide two values for this to work
    //The first value comes from -controller.getY(Hand.kLeft), by doing this, 
    //we get the up and down value from the joystick on the left hand side of the XboxController
    //We have a negative sign in front (-) to flip the value we receive to make sure the robot
    //moves forward and back correctly
    //The second value comes from controller.getX(Hand.kLeft), by doing this,
    //we get the left and right value from the joystick on the left ahdn side of the XboxController
    //The drivetrain.arcadeDrive call combines these two value together to make the robot move
    //similar to how you might move in an old video game.
    drivetrain.arcadeDrive(-controller.getY(Hand.kLeft), controller.getX(Hand.kLeft));

    //This is an if statement, these are used to make decisions based on true or false
    //values. In this instance, we're asking questions about our XboxController to see
    //if we should be shifting into high gear (the A Button) or low gear (the B Button)
    //If you were to write this out like a sentence, you could say "If the A Button is 
    //pressed, use the double solenoid to shift into high gear, otherwise if the B
    //button is pressed, use the double solenoid to shift into low gear"
    if(controller.getAButton()) {
      shifter.set(Value.kForward);
    } else if(controller.getBButton()) {
      shifter.set(Value.kReverse);
    }

    //In this if statement, we're asking questions about what direction our intake should
    //move (either bring balls in or move balls out). 
    //controller.getBumper(Hand.kLeft) checks to see if the bumper button on the left
    //hand side of the controller is pressed. If it is, we get the value of the left hand
    //trigger and make it negative using a minus sign (-controller.getTriggerAxis(Hand.kLeft))
    //We pass this value to intake.set to set the speed of the intake motor, based on how much
    //the left trigger has been pressed down. We do something similar if the button is not pressed
    //but instead of negating the value, we just send it as is. If you were to write this out
    //like a sentence, you could say "If the left bumper is pressed, use the negative left trigger value
    //to set the intake speed, otherwise, if the button is not pressed, use the left trigger value to 
    //set the intake speed"
    if(controller.getBumper(Hand.kLeft)) {
      intake.set(controller.getTriggerAxis(Hand.kLeft));
    } else {
      intake.set(-controller.getTriggerAxis(Hand.kLeft));
    }

    //In this if statement, we're asking questions about what direction our ball feeder should
    //move (either moving balls towards the shooter, or away from it).
    //controller.getXButton() checks to see if the x button on the controller is pressed. 
    //If it is, we set the motor for the ball feeder to full speed forward (1) using
    //ballFeeder.set(speed). We do something similar if the Y button is pressed, except
    //instead of saying full forward, we say full reverse (-1). If neither button is pressed
    //we stop the motor by calling .set(0). If you were to write this out like a sentence,
    //you could say "If the x button is pressed, move balls towards the shooter, else if the 
    //y button is pressed, move balls away from the shooter, otherwise, if neither button is pressed,
    //stop the ball feeder."
    if(controller.getXButton()) {
      ballFeeder.set(1);
    } else if(controller.getYButton()) {
      ballFeeder.set(-1);
    } else {
      ballFeeder.set(0);
    }

    //This piece is pretty basic, we've seen pieces of this before.
    //We are setting the speed of the shooter motors, by getting how much
    //the right hand trigger has been pressed down. 
    shooter.set(controller.getTriggerAxis(Hand.kRight));

    //In this if statement, we're asking questions about how we should move the hood
    //of the shooter. controller.getPOV() is used to retrieve what button on the d-pad
    //is being pressed. 0 means that your pushing the up button, 180 means your pushing
    //the down button. In order for this to work, we have to check two things, whether or not
    //the right d-pad button is pressed AND making sure that the limit switch for the direction
    //we want to move in is safe and we're not going to break something (i.e. we check the limit switch)
    //If you were to write this out like a sentence, you could say "If the d-pad up button is pressed 
    //AND the hood up limit switch is not pressed, move the hood up, else if the d-pad down button is
    //pressed AND the hood down limit switch is not pressed, move the hood down, otherwise if neither 
    //of these are true, stop the hood motor."
    if(controller.getPOV() == 0 && hoodUpLimitSwitch.get()) {
      hoodMotor.set(1);
    } else if(controller.getPOV() == 180 && hoodDownLimitSwitch.get()) {
      hoodMotor.set(-1);
    } else {
      hoodMotor.set(0);
    }
  }
}
