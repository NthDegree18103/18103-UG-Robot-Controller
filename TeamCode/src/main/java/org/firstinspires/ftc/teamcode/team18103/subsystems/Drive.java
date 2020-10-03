package org.firstinspires.ftc.teamcode.team18103.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.lib.control.PIDSVA;
import org.firstinspires.ftc.teamcode.lib.drivers.Motor;
import org.firstinspires.ftc.teamcode.lib.motion.ProfileState;
import org.firstinspires.ftc.teamcode.lib.motion.TrapezoidalMotionProfileGenerator;
import org.firstinspires.ftc.teamcode.lib.util.MathFx;
import org.firstinspires.ftc.teamcode.lib.util.MeanOptimizedDataFusionModel;
import org.firstinspires.ftc.teamcode.team18103.src.Constants;
import org.firstinspires.ftc.teamcode.team18103.states.DriveMode;
import org.firstinspires.ftc.teamcode.team18103.subsystems.IMU.IMU;
import org.firstinspires.ftc.teamcode.team18103.subsystems.Odometry.TriWheelOdometryGPS;
import org.firstinspires.ftc.teamcode.team18103.subsystems.Vision.VuforiaVision;

import java.util.Arrays;

/*
 * Author: Akhil G
 */

public class Drive extends Subsystem {
    private DcMotorEx frontLeft, frontRight, backLeft, backRight;
    private DcMotorEx[] driveMotors;
    private DriveMode driveMode = DriveMode.Balanced;
    private int driveType = 0; // 0 - Field-Centric, 1 - POV
    private IMU imu;
    private TriWheelOdometryGPS odometry;
    private VuforiaVision vision;
    private MeanOptimizedDataFusionModel model;
    private ProfileState driveState;
    private double x, y, theta;

    public Drive(IMU imu, TriWheelOdometryGPS odometry, VuforiaVision vision) {
        this.imu = imu;
        this.odometry = odometry;
        this.vision = vision;
        model = new MeanOptimizedDataFusionModel();
        update();
    }

    @Override
    public void init(HardwareMap ahMap) {
        frontLeft = ahMap.get(DcMotorEx.class, Constants.frontLeft);
        frontRight = ahMap.get(DcMotorEx.class, Constants.frontRight);
        backLeft = ahMap.get(DcMotorEx.class, Constants.backLeft);
        backRight = ahMap.get(DcMotorEx.class, Constants.backRight);

        frontRight.setDirection(DcMotorEx.Direction.REVERSE);
        backRight.setDirection(DcMotorEx.Direction.REVERSE);

        driveMotors = new DcMotorEx[]{frontLeft, frontRight, backLeft, backRight};

        for (DcMotorEx motor : driveMotors) {
            //motor.setPositionPIDFCoefficients(Constants.DRIVE_P);
            motor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        }

    }

    // Autonomous Algorithms

    // Setting Drivetrain

    /**
     * Sets Drive to go forward/backwards
     * @param power Speed of movement
     */
    private void setDriveMotors(double power) {
        for (DcMotorEx i : driveMotors) {
            i.setPower(power);
        }
    }

    /**
     * Sets Drive to go left/right
     * @param power Speed of movement
     */
    private void setStrafeMotors(double power) {
        frontLeft.setPower(power);
        backLeft.setPower(-power);
        frontRight.setPower(-power);
        backRight.setPower(power);
    }

    /**
     * Sets Drive to rotate
     * @param power Speed of Movement
     */
    private void setRotateMotors(double power) {
        frontLeft.setPower(power);
        backLeft.setPower(power);
        frontRight.setPower(-power);
        backRight.setPower(-power);
    }

    // Distanced Driving (Motion Profiling)

    /**
     * Sets Drive to rotate to a certain angle (-180, 180) w/ motion profile & PIDSVA
     * @param heading rotation angle
     */
    public void PointRotation(double heading) {
        double curHead = getDataFusionTheta();

        double error = heading - curHead;

        double distance = Constants.ENCODER_DIFFERENCE * Math.PI * error / 360;

        TrapezoidalMotionProfileGenerator motionProfile = new TrapezoidalMotionProfileGenerator(distance, Motor.GoBILDA_312);
        for (DcMotorEx i : driveMotors) {
            i.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
            i.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        }

        ElapsedTime timer = new ElapsedTime();
        PIDSVA controller = new PIDSVA(10d/12d, 1d/12d, 10d/12d, 0d, 1d/motionProfile.getMaxV(), 0.01d/12d);
        while (timer.seconds() < motionProfile.getTotalTime()) {
            double timeStamp = timer.seconds();
            double position = motionProfile.getPosition(timeStamp);
            double velocity = motionProfile.getVelocity(timeStamp);
            double acceleration = motionProfile.getAcceleration(timeStamp);
            double pos_error = position - (frontLeft.getCurrentPosition()/ Motor.GoBILDA_312.getTicksPerInch());
            double output = controller.getOutput(pos_error, velocity, acceleration);

            setRotateMotors(output);

            setDriveState(motionProfile.getProfileState(timeStamp));

        }
        setDriveMotors(0);
    }

    /**
     * Sets Drive to go forward/backwards w/ motion profile & PIDSVA
     * @param distance target setpoint
     */
    public void motionProfileDrive(double distance) {
        TrapezoidalMotionProfileGenerator motionProfile = new TrapezoidalMotionProfileGenerator(distance, Motor.GoBILDA_312);
        for (DcMotorEx i : driveMotors) {
            i.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
            i.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        }

        ElapsedTime timer = new ElapsedTime();
        PIDSVA controller = new PIDSVA(0, 0, 0, 0d, 1d/motionProfile.getMaxV(), 0);
        while (timer.seconds() < motionProfile.getTotalTime()) {
            double timeStamp = timer.seconds();
            double position = motionProfile.getPosition(timeStamp);
            double velocity = motionProfile.getVelocity(timeStamp);
            double acceleration = motionProfile.getAcceleration(timeStamp);
            double error = position - (frontLeft.getCurrentPosition()/ Motor.GoBILDA_312.getTicksPerInch());
            double output = controller.getOutput(error, velocity, acceleration);

            setDriveMotors(output);

            setDriveState(motionProfile.getProfileState(timeStamp));

        }
        setDriveMotors(0);
    }

    /**
     * Sets Drive to go left/right w/ motion profile & PIDSVA
     * @param distance target setpoint
     */
    public void motionProfileStrafe(double distance) {
        TrapezoidalMotionProfileGenerator motionProfile = new TrapezoidalMotionProfileGenerator(distance, Motor.GoBILDA_312);
        for (DcMotorEx i : driveMotors) {
            i.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
            i.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        }

        ElapsedTime timer = new ElapsedTime();
        PIDSVA controller = new PIDSVA(0, 0, 0, 0d, 1d/motionProfile.getMaxV(), 0);
        while (timer.seconds() < motionProfile.getTotalTime()) {
            double timeStamp = timer.seconds();
            double position = motionProfile.getPosition(timeStamp);
            double velocity = motionProfile.getVelocity(timeStamp);
            double acceleration = motionProfile.getAcceleration(timeStamp);
            double error = position - (frontLeft.getCurrentPosition()/ Motor.GoBILDA_312.getTicksPerInch());
            double output = controller.getOutput(error, velocity, acceleration);

            setStrafeMotors(output);

            setDriveState(motionProfile.getProfileState(timeStamp));

        }
        setDriveMotors(0);
    }

    // TeleOp Methods

    /**
     * Tank Drive Control
     * @param left left side power
     * @param right right side power
     */
    public void tankDrive(double left, double right) {
        frontLeft.setPower(left);
        backLeft.setPower(left);
        frontRight.setPower(right);
        backRight.setPower(right);
    }

    /**
     * Mecanum Drive Control
     * @param y Forward/Backward Force (GamePad Left Stick y)
     * @param x Left/Right (Strafe) Force (GamePad Left Stick x)
     * @param turn Rotational Force (GamePad Right Stick x)
     * @param mode Drivetrain Speed Setting (Sport, Normal, Economy)
     */
    public void POVMecanumDrive(double y, double x, double turn, DriveMode mode) {

        double v1 = -(y - (turn * Constants.strafeScaling) - (x/Constants.turnScaling));
        double v2 = -(y - (turn * Constants.strafeScaling) + (x/Constants.turnScaling));
        double v3 = -(y + (turn * Constants.strafeScaling) - (x/Constants.turnScaling));
        double v4 = -(y + (turn * Constants.strafeScaling) + (x/Constants.turnScaling));

        Double[] v = new Double[]{Math.abs(v1), Math.abs(v2), Math.abs(v3), Math.abs(v4)};

        Arrays.sort(v);

        if (v[3] > 1) {
            v1 /= v[3];
            v2 /= v[3];
            v3 /= v[3];
            v4 /= v[3];
        }

        frontLeft.setPower(v1 * mode.getScaling());
        backLeft.setPower(v2 * mode.getScaling());
        backRight.setPower(v3 * mode.getScaling());
        frontRight.setPower(v4 * mode.getScaling());
    }

    /**
     * Field-Centric Mecanum Drive Control
     * @param y Forward/Backward Force (GamePad Left Stick y)
     * @param x Left/Right (Strafe) Force (GamePad Left Stick x)
     * @param turn Rotational Force (GamePad Right Stick x)
     * @param mode Drivetrain Speed Setting (Sport, Normal, Economy)
     */
    public void fieldCentricMecanumDrive(double y, double x, double turn, DriveMode mode) {
        x = x * Math.cos(getDataFusionTheta()) - y * Math.sin(getDataFusionTheta());
        y = x * Math.sin(getDataFusionTheta()) + y * Math.cos(getDataFusionTheta());

        POVMecanumDrive(y, x, turn, mode);
    }

    /**
     * Ultimate Drive Controller
     * @param y Forward/Backward Force (GamePad Left Stick y)
     * @param x Left/Right (Strafe) Force (GamePad Left Stick x)
     * @param turn Rotational Force (GamePad Right Stick x)
     * @param left_trigger Lower Gear Adjustment
     * @param right_trigger Higher Gear Adjustment
     * @param left_bumper Field-Centric Drive Setter
     * @param right_bumper Point-of-View Drive Setter
     * @param button Zero Yaw Setter
     */

    public void ultimateDriveController(double y, double x, double turn, float left_trigger,
                                        float right_trigger, boolean left_bumper,
                                        boolean right_bumper, boolean button) {

        double mode = driveMode.getId();
        double modeChange = right_trigger - left_trigger;
        if (Math.abs(modeChange) > 0.5) {
            mode = MathFx.scale(-1, 1, Math.round(mode + modeChange));
        }

        setDriveMode(getDMbyID(mode));

        if (left_bumper) {
            setDriveType(0);
        }

        if (right_bumper) {
            setDriveType(1);
        }

        if (button) {
            zeroYaw();
        }

        switch (driveType) {
            case 0:
                fieldCentricMecanumDrive(y, x, turn, driveMode);
                break;
            case 1:
                POVMecanumDrive(y, x, turn, driveMode);
                break;
        }


    }

    @Override
    public void update() {
        getDataFusionX();
        getDataFusionY();
        getDataFusionTheta();
    }

    public void zeroYaw() {
        model.setBias(-getDataFusionTheta());
    }

    public DriveMode getDriveMode() {
        return driveMode;
    }

    public void setDriveMode(DriveMode driveMode) {
        this.driveMode = driveMode;
    }

    public DriveMode getDMbyID(double id) {
        if (id == -1) {
            return DriveMode.Economy;
        } else if (id == 0) {
            return DriveMode.Balanced;
        } else {
            return DriveMode.Sport;
        }
    }

    public double getDriveType() {
        return driveType;
    }

    public void setDriveType(int driveType) {
        this.driveType = driveType;
    }

    public TriWheelOdometryGPS getOdometry() {
        return odometry;
    }

    public VuforiaVision getVision() {
        return vision;
    }

    public double getDataFusionTheta() {
        setTheta(model.fuse(new double[]{imu.getHeading(), vision.getTheta(),
                odometry.getTheta()}, new double[]{1, 2, 2}));
        return model.fuse(new double[]{imu.getHeading(), vision.getTheta(),
                odometry.getTheta()}, new double[]{1, 2, 2});
    }

    public double getDataFusionX() {
        setX(model.fuse(new double[]{vision.getX(),
                odometry.getX()}));
        return model.fuse(new double[]{vision.getX(),
                odometry.getX()});
    }

    public double getDataFusionY() {
        setY(model.fuse(new double[]{vision.getY(),
                odometry.getY()}));
        return model.fuse(new double[]{vision.getY(),
                odometry.getY()});
    }

    public void setDriveState(ProfileState driveState) {
        this.driveState = driveState;
    }

    public ProfileState getDriveState() {
        return driveState;
    }

    public MeanOptimizedDataFusionModel getModel() {
        return model;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }
}
