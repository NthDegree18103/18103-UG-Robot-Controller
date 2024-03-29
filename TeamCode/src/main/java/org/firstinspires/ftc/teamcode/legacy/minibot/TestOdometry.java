package org.firstinspires.ftc.teamcode.legacy.minibot;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.lib.drivers.Motor;
import org.firstinspires.ftc.teamcode.team18103.src.Constants;
import org.firstinspires.ftc.teamcode.team18103.src.Robot;
import org.firstinspires.ftc.teamcode.team18103.states.DriveMode;

import java.util.Arrays;

/*
 * Author: Akhil G
 *
 * Controls
 *
 * Chassis:
 *
 * Forward/Backward (GamePad Left Stick y)
 * Left/Right (Strafe) (GamePad Left Stick x)
 * (Turn) Rotational Force (GamePad Right Stick x)
 * Lower Gear Adjustment (GamePad Left Trigger)
 * Higher Gear Adjustment (GamePad Right Trigger)
 * Field-Centric Drive Setter (GamePad Left Bumper) * Don't Use
 * Point-of-View Drive Setter (GamePad Right Bumper) * Don't Use
 * Zero Yaw Setter (GamePad X Button)
 *
 * Intake-Outtake:
 *
 * Run at full speed (GamePad Y Button)
 *
 */

@TeleOp
@Disabled
public class TestOdometry extends OpMode {
    public DcMotorEx frontLeft, frontRight, backLeft, backRight;
    DcMotorEx[] driveMotors;

    private DcMotorEx left, right, horizontal;
    private final double ticksPerInch = Motor.REV_Encoder.getTicksPerInch(35);
    private double dt = Constants.Dt;

    private double x = 0, y = 0, theta = 0;
    private double r_0 = 0, l_0 = 0, s_0 = 0;

    @Override
    public void init() {
        frontLeft = hardwareMap.get(DcMotorEx.class, Constants.frontLeft);
        frontRight = hardwareMap.get(DcMotorEx.class, Constants.frontRight);
        backLeft = hardwareMap.get(DcMotorEx.class, Constants.backLeft);
        backRight = hardwareMap.get(DcMotorEx.class, Constants.backRight);

        frontLeft.setDirection(DcMotorEx.Direction.REVERSE);
        backLeft.setDirection(DcMotorEx.Direction.REVERSE);

        driveMotors = new DcMotorEx[]{frontLeft, frontRight, backLeft, backRight};

        for (DcMotorEx motor : driveMotors) {
            //motor.setPositionPIDFCoefficients(Constants.DRIVE_P);
            motor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
            motor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
            motor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        }

        left = hardwareMap.get(DcMotorEx.class, Constants.left);
        right = hardwareMap.get(DcMotorEx.class, Constants.right);
        horizontal = hardwareMap.get(DcMotorEx.class, Constants.horizontal);

        left.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        right.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        horizontal.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

        right.setDirection(DcMotorEx.Direction.REVERSE);
    }

    public void POVMecanumDrive(double y, double x, double turn, DriveMode mode) {
        turn *= 0.75; //Custom reduction bc it was requested.
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

    @Override
    public void start() {

    }

    @Override
    public void loop() {
        telemetry.addLine()
                .addData("Robot Initialized: ", true);

        telemetry.addLine()
                .addData("Odometry Left: ", getLeft().getCurrentPosition())
                .addData("Odometry Right: ", getRight().getCurrentPosition())
                .addData("Odometry Horizontal: ", getHorizontal().getCurrentPosition())
                .addData("Front Left", frontLeft.getCurrentPosition())
                .addData("Front Right", frontRight.getCurrentPosition())
                .addData("Back Left", backLeft.getCurrentPosition())
                .addData("Back Right", backRight.getCurrentPosition());

        /*robot.getDriveSubsystem().ultimateDriveController(gamepad1.left_stick_y, gamepad1.left_stick_x,
                gamepad1.right_stick_x, gamepad1.left_trigger, gamepad1.right_trigger,
                gamepad1.left_bumper, gamepad1.right_bumper, gamepad1.x);*/

        POVMecanumDrive(gamepad1.left_stick_y, gamepad1.left_stick_x,
                gamepad1.right_stick_x, DriveMode.Balanced); // Half Speed

    }

    public DcMotorEx getRight() {
        return right;
    }

    public DcMotorEx getLeft() {
        return left;
    }

    public DcMotorEx getHorizontal() {
        return horizontal;
    }

}
