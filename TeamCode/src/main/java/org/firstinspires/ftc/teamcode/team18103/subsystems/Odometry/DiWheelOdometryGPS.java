package org.firstinspires.ftc.teamcode.team18103.subsystems.Odometry;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.team18103.src.Constants;

/*
 * Author: Akhil G
 */

public class DiWheelOdometryGPS extends Odometry {

    private DcMotorEx frontLeft, frontRight, backLeft, backRight;
    private double ticksPerInch, dt;

    private double x = 0, y = 0, theta = 0;
    private double r_0 = 0, l_0 = 0;

    public DiWheelOdometryGPS(double ticksPerInch, int dt) {
        this.ticksPerInch = ticksPerInch;
        this.dt = dt;
    }

    public DiWheelOdometryGPS(double ticksPerInch, int dt, double x0, double y0, double theta0) {
        this.ticksPerInch = ticksPerInch;
        this.dt = dt;
        x = x0;
        y = y0;
        theta = theta0;
    }

    @Override
    public void init(HardwareMap ahMap) {
        frontLeft = ahMap.get(DcMotorEx.class, Constants.frontLeft);
        frontRight = ahMap.get(DcMotorEx.class, Constants.frontRight);
        backLeft = ahMap.get(DcMotorEx.class, Constants.backLeft);
        backRight = ahMap.get(DcMotorEx.class, Constants.backRight);

        frontRight.setDirection(DcMotorEx.Direction.REVERSE);
        backRight.setDirection(DcMotorEx.Direction.REVERSE);

    }

    @Override
    public void run() {
        //Get Current Positions
        double lPos = (getLeft() * getTicksPerInch());
        double rPos = (getRight() * getTicksPerInch());

        double dl = lPos - getL_0();
        double dr = rPos - getR_0();

        //Calculate Angle
        double dTheta = (dl - dr) / (Constants.ENCODER_DIFFERENCE);
        theta += dTheta;

        double p = ((dr + dl) / (2 * getTheta()));

        //Calculate and update the position values
        double dx = p * Math.sin(dTheta);
        double dy = dx * Math.tan(dTheta /2);

        x += dx;
        y += dy;

        setZeros(lPos, rPos);
    }

    @Override
    public void update() {
        run();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getTheta() {
        return theta;
    }

    public int getLeft() {
        return (frontLeft.getCurrentPosition() + backLeft.getCurrentPosition())/2;
    }

    public int getRight() {
        return (frontRight.getCurrentPosition() + backRight.getCurrentPosition())/2;
    }

    public double getTicksPerInch() {
        return ticksPerInch;
    }

    public double getL_0() {
        return l_0;
    }

    public double getR_0() {
        return r_0;
    }

    public void setZeros(double l_0, double r_0) {
        this.l_0 = l_0;
        this.r_0 = r_0;
    }

}
