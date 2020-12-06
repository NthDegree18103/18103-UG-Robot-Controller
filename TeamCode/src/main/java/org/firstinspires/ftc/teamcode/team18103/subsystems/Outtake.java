package org.firstinspires.ftc.teamcode.team18103.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.lib.drivers.Motor;
import org.firstinspires.ftc.teamcode.team18103.src.Constants;

public class Outtake extends Subsystem {
    DcMotorEx firstOuttake;
    DcMotorEx secondOuttake;
    ElapsedTime elapsedTime;

    double firstOuttakeRPM;
    double secondOuttakeRPM;

    double firstOuttakelastPos;
    double secondOuttakeLastPos;


    public Outtake() {

    }

    @Override
    public void init(HardwareMap ahMap) {
        firstOuttake = ahMap.get(DcMotorEx.class, Constants.firstOuttake);
        secondOuttake = ahMap.get(DcMotorEx.class, Constants.secondOuttake); //Needed a simple name and was lazy :/
        firstOuttake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        firstOuttake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        secondOuttake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        secondOuttake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        elapsedTime = new ElapsedTime();
        firstOuttakeRPM = 0;
        secondOuttakeRPM = 0;
        firstOuttakelastPos = 0;
        secondOuttakeLastPos = 0;
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    public void runOuttake(double power) {
        ElapsedTime timeout = new ElapsedTime();
        firstOuttake.setPower(power);
        secondOuttake.setPower(power);
        //do {} while(firstOuttakeRPM < 4000 || timeout.seconds() > 3);
    }

    public void runOuttake(boolean on) {
        if (on) {
            runOuttake(0.9);
        } else {
            stopIntake();
        }
        updateDiagnostics();
    }

    public void PIDOuttake() {

    }

    private void updateDiagnostics() {
        firstOuttakeRPM = ((firstOuttake.getCurrentPosition() - firstOuttakelastPos)/ Motor.GoBILDA_6000.getENCODER_TICKS_PER_REVOLUTION())/(elapsedTime.seconds()/60);
        secondOuttakeRPM = ((secondOuttake.getCurrentPosition() - secondOuttakeLastPos)/Motor.GoBILDA_6000.getENCODER_TICKS_PER_REVOLUTION())/(elapsedTime.seconds()/60);
        elapsedTime.reset();
        firstOuttakelastPos = firstOuttake.getCurrentPosition();
        secondOuttakeLastPos = secondOuttake.getCurrentPosition();
    }

    public void stopIntake() {
        runOuttake(0);
    }

    public double getFirstOuttakeRPM() {
        return firstOuttakeRPM;
    }

    public double getSecondOuttakeRPM() {
        return secondOuttakeRPM;
    }

    public double getFirstOuttakelastPos() {
        return firstOuttakelastPos;
    }

    public double getSecondOuttakeLastPos() {
        return secondOuttakeLastPos;
    }
}
