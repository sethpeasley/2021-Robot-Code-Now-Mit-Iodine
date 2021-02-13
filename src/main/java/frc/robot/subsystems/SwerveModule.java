// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;


import com.ctre.phoenix.sensors.CANCoder;
import com.ctre.phoenix.sensors.CANCoderConfiguration;
import com.ctre.phoenix.sensors.CANCoderFaults;
import com.ctre.phoenix.sensors.CANCoderStickyFaults;
import com.ctre.phoenix.sensors.MagnetFieldStrength;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;
import frc.robot.Constants.SwerveDriveModuleConstants;
import frc.robot.Constants.SwerveDriveModuleConstants.DriveModulePIDValues;
import frc.robot.Constants.SwerveDriveModuleConstants.SteeringControllerPIDValues;



/** 
 * Swerve Module is based on the WPI Swerve module code.
 * 
 */
public class SwerveModule
{
    // REV robotics Spark Max motor controllers
    private CANSparkMax m_driveMotor;
    private CANSparkMax m_steeringMotor;

    // REV Robotics Spark Max encoder signal; the encoder built-in to the motor
    private CANEncoder m_driveEncoder;

    // CTRE SRX Magnetic Encoder signal; encoder is external and mounted on the swerve module
    private CANEncoder m_steeringEncoder;

    // PID Loops using the Spark API
    private CANPIDController m_driverPIDController;
    private CANPIDController m_steeringPIDController;

    // These two PID controllers are from the WPILib and we're temporarily commenting them to use the REV Spark PIDs
    // private final PIDController m_driverPIDController = new PIDController(SwerveDriveModuleConstants.DriveModule.k_Proportional, 
    //                                                                       SwerveDriveModuleConstants.DriveModule.k_Intergral,
    //                                                                       SwerveDriveModuleConstants.DriveModule.k_Differential);

    // private final ProfiledPIDController m_turningPIDController = 
    //     new ProfiledPIDController(
    //         SwerveDriveModuleConstants.TurningController.k_Prop,
    //         SwerveDriveModuleConstants.TurningController.k_Inter,
    //         SwerveDriveModuleConstants.TurningController.k_Diff,
    //         new TrapezoidProfile.Constraints(
    //             SwerveDriveModuleConstants.k_MaxModuleAngularSpeedRadiansPerSecond,
    //             SwerveDriveModuleConstants.k_MaxModuleAngularAccelerationRadiansPerSecondSquared) );
    
    /**
     * Constructs a SwerveModule.
     *
     * @param driveMotorChannel ID for the drive motor.
     * @param steeringMotorChannel ID for the turning motor.
     */
    public SwerveModule(
        int driveMotorChannel,
        MotorType driveMotorType,
        int steeringMotorChannel,
        MotorType steeringMotorType,
        boolean driveEncoderReversed,
        boolean turningEncoderReversed)
        {
            ConfigureDriveMotor(driveMotorChannel, driveMotorType);
            ConfigureSteeringMotor(steeringMotorChannel, steeringMotorType);


            // m_steeringEncoder = m_steeringMotor.getAlternateEncoder(SwerveDriveModuleConstants.k_AlternateEncoderType, 
            //                                                         SwerveDriveModuleConstants.k_AltEnc_CountPerRev);

            // // Steering encoder is a Cross-The-Road Electronics CANCoder
            // this.m_steeringEncoder = new CANCoder(steeringEncoderChannel); 
            
            // CANCoderConfiguration m_canCoderConfiguration = new CANCoderConfiguration();
            // m_canCoderConfiguration.unitString = "radians";
            // m_canCoderConfiguration.sensorCoefficient = SwerveDriveModuleConstants.k_SteeringEncoderCoefficient;

            // m_steeringEncoder.configAllSettings(m_canCoderConfiguration);

            
            // Limit the PID Controller's input range between -pi and pi and set the input
            // to be continuous.
           // m_turningPIDController.enableContinuousInput(-Math.PI, Math.PI);
        }

    /**
     * Returns the current state of the module.
     *
     * @return The current state of the module.
     */
    public SwerveModuleState getState()
    {
        return new SwerveModuleState(m_driveEncoder.getVelocity(), // the getVelocity() has been scaled to go from RPM to m/s
                                     new Rotation2d(m_steeringEncoder.getPosition()));
    }

    /**
     * Sets the desired state for the module.
     *
     * @param state Desired state with speed and angle.
     */
    public void setDesiredState(SwerveModuleState state)
    {
        // // Calculate the drive output from the drive PID controller.
        // final var driveOutput = m_driverPIDController.calculate(m_driveEncoder.getVelocity(), state.speedMetersPerSecond);

        // // Calculate the turning motor output from the turning PID controller.
        // final var steerOutput = m_turningPIDController.calculate(m_steeringEncoder.getPosition(), state.angle.getRadians());

        // m_driveMotor.set(driveOutput);
        // m_steeringMotor.set(steerOutput);

        m_driverPIDController.setReference(state.speedMetersPerSecond, ControlType.kSmartVelocity);
        m_steeringPIDController.setReference(state.angle.getRadians(), ControlType.kSmartMotion);
    }


    /** Zeros all the SwerveModule encoders. */
    public void resetEncoders()
    {
        m_driveEncoder.setPosition(0);
        m_steeringEncoder.setPosition(0);
        
    }


    private void ConfigureDriveMotor(int channel, MotorType motorType)
    {
        m_driveMotor = new CANSparkMax(channel, motorType);
    
        m_driveEncoder = m_driveMotor.getEncoder();   //assumes the use of the internal motor encoder
        //m_driveEncoder.setVelocityConversionFactor(SwerveDriveModuleConstants.k_DriveEncoderDistancePerPulse); // <- This line for use in WPILib PID loops, not Spark
        m_driverPIDController = m_driveMotor.getPIDController();

        // following the example found here: https://github.com/REVrobotics/SPARK-MAX-Examples/blob/master/Java/Smart%20Motion%20Example/src/main/java/frc/robot/Robot.java
        m_driverPIDController.setP(DriveModulePIDValues.k_Proportional);
        m_driverPIDController.setI(DriveModulePIDValues.k_Intergral);
        m_driverPIDController.setD(DriveModulePIDValues.k_Differential);
        m_driverPIDController.setIZone(DriveModulePIDValues.k_Iz);
        m_driverPIDController.setFF(DriveModulePIDValues.k_FF);
        m_driverPIDController.setOutputRange(DriveModulePIDValues.k_MinOutput, DriveModulePIDValues.k_MaxOutput);

        /**
         * Smart Motion coefficients are set on a CANPIDController object
         * 
         * - setSmartMotionMaxVelocity() will limit the velocity in RPM of
         * the pid controller in Smart Motion mode
         * - setSmartMotionMinOutputVelocity() will put a lower bound in
         * RPM of the pid controller in Smart Motion mode
         * - setSmartMotionMaxAccel() will limit the acceleration in RPM^2
         * of the pid controller in Smart Motion mode
         * - setSmartMotionAllowedClosedLoopError() will set the max allowed
         * error for the pid controller in Smart Motion mode
         */

        m_driverPIDController.setSmartMotionMaxVelocity(DriveModulePIDValues.k_maxVel, DriveModulePIDValues.k_smartMotionSlot);
        m_driverPIDController.setSmartMotionMinOutputVelocity(DriveModulePIDValues.k_minVel, DriveModulePIDValues.k_smartMotionSlot);
        m_driverPIDController.setSmartMotionMaxAccel(DriveModulePIDValues.k_maxAcc, DriveModulePIDValues.k_smartMotionSlot);
        m_driverPIDController.setSmartMotionAllowedClosedLoopError(DriveModulePIDValues.k_allowedError, DriveModulePIDValues.k_smartMotionSlot);
    }


    private void ConfigureSteeringMotor(int channel, MotorType motorType)
    {
        m_steeringMotor = new CANSparkMax(channel, motorType);

        // https://github.com/REVrobotics/SPARK-MAX-Examples/blob/master/Java/Alternate%20Encoder/src/main/java/frc/robot/Robot.java
        m_steeringEncoder = m_steeringMotor.getAlternateEncoder(SwerveDriveModuleConstants.k_AlternateEncoderType, SwerveDriveModuleConstants.k_AltEnc_CountPerRev);   // Using alternate external SRX Magnetic encoder
        
        m_steeringPIDController = m_steeringMotor.getPIDController();
        m_steeringPIDController.setFeedbackDevice(m_steeringEncoder);

        // following the example found here: https://github.com/REVrobotics/SPARK-MAX-Examples/blob/master/Java/Smart%20Motion%20Example/src/main/java/frc/robot/Robot.java
        m_steeringPIDController.setP(SteeringControllerPIDValues.k_Prop);
        m_steeringPIDController.setI(SteeringControllerPIDValues.k_Inter);
        m_steeringPIDController.setD(SteeringControllerPIDValues.k_Diff);
        m_steeringPIDController.setIZone(SteeringControllerPIDValues.k_Iz);
        m_steeringPIDController.setFF(SteeringControllerPIDValues.k_FF);
        m_steeringPIDController.setOutputRange(SteeringControllerPIDValues.k_MinOutput, SteeringControllerPIDValues.k_MaxOutput);

        /**
         * Smart Motion coefficients are set on a CANPIDController object
         * 
         * - setSmartMotionMaxVelocity() will limit the velocity in RPM of
         * the pid controller in Smart Motion mode
         * - setSmartMotionMinOutputVelocity() will put a lower bound in
         * RPM of the pid controller in Smart Motion mode
         * - setSmartMotionMaxAccel() will limit the acceleration in RPM^2
         * of the pid controller in Smart Motion mode
         * - setSmartMotionAllowedClosedLoopError() will set the max allowed
         * error for the pid controller in Smart Motion mode
         */

        m_steeringPIDController.setSmartMotionMaxVelocity(SteeringControllerPIDValues.k_maxVel, SteeringControllerPIDValues.k_smartMotionSlot);
        m_steeringPIDController.setSmartMotionMinOutputVelocity(SteeringControllerPIDValues.k_minVel, SteeringControllerPIDValues.k_smartMotionSlot);
        m_steeringPIDController.setSmartMotionMaxAccel(SteeringControllerPIDValues.k_maxAcc, SteeringControllerPIDValues.k_smartMotionSlot);
        m_steeringPIDController.setSmartMotionAllowedClosedLoopError(SteeringControllerPIDValues.k_allowedError, SteeringControllerPIDValues.k_smartMotionSlot);
    }




}
