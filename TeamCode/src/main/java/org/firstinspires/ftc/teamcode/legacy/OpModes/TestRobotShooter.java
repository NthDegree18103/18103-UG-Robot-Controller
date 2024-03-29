package org.firstinspires.ftc.teamcode.legacy.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.team18103.src.Robot;
import org.firstinspires.ftc.teamcode.team18103.states.DriveMode;

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
@Disabled
@TeleOp
public class TestRobotShooter extends OpMode {
    Robot robot = new Robot();

    @Override
    public void init() {
        robot.init(hardwareMap, telemetry);
    }

    @Override
    public void start() {
        robot.start();
    }

    @Override
    public void loop() {
        robot.loop(telemetry);

        robot.getDriveSubsystem().POVMecanumDrive(gamepad1.left_stick_y, gamepad1.left_stick_x,
                gamepad1.right_stick_x, DriveMode.Sport); // Max Speed

        robot.getDriveSubsystem().zeroCoords(gamepad1.a);

        robot.getIOSubsystem().runIntake(gamepad1.right_trigger - gamepad1.left_trigger);

        robot.getIOSubsystem().runTransfer(-(gamepad1.right_trigger-gamepad1.left_trigger));

        if(gamepad1.y) {
            robot.getDriveSubsystem().rotateToShootingAngle();

            robot.getIOSubsystem().outtakeFromPoint3(Math.hypot(
                    126 - robot.getDriveSubsystem().getDataFusionY(),
                    Math.abs(robot.getDriveSubsystem().getDataFusionX() - 9)));
        } else {
            robot.getIOSubsystem().runOuttake(false);
        }

        //if (gamepad1.a) {
            //robot.getWobbleSubsystem().moveLatch(1.0); //open
        //}

        if (gamepad1.b) {
            robot.getWobbleSubsystem().moveJoint(false);
        } else if (gamepad1.x) {
            robot.getWobbleSubsystem().moveJoint(true);
        }

        if (gamepad1.dpad_up) {
            //robot.getWobbleSubsystem().moveLatch(1);
        } else if (gamepad1.dpad_down) {
            robot.getWobbleSubsystem().moveLatch(0.40);
        }

    }

}
