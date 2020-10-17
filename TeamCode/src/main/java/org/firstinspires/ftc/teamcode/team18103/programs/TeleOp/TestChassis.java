package org.firstinspires.ftc.teamcode.team18103.programs.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.team18103.src.Robot;
import org.firstinspires.ftc.teamcode.team18103.states.DriveMode;

/*
 * Author: Akhil G
 */

@TeleOp
public class TestChassis extends OpMode {
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
        telemetry.addData("Left_Stick_y", gamepad1.left_stick_y);
        telemetry.addData("Left_Stick_x", gamepad1.left_stick_x);
        telemetry.addData("Right_Stick_y", gamepad1.right_stick_y);
        telemetry.addData("Right_Stick_x", gamepad1.left_stick_y);

        //robot.getDriveSubsystem().POVMecanumDrive(gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x, DriveMode.Balanced);

        robot.getDriveSubsystem().ultimateDriveController(gamepad1.left_stick_y, gamepad1.left_stick_x,
                gamepad1.right_stick_x, gamepad1.left_trigger, gamepad1.right_trigger,
                gamepad1.left_bumper, gamepad1.right_bumper, gamepad1.x);

        //System.out.print("[" + gamepad1.left_stick_x + ", " + gamepad1.left_stick_y + ", " + gamepad1.right_stick_x + "], " );

    }

}
