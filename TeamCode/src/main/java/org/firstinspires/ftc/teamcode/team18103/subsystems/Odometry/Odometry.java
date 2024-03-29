package org.firstinspires.ftc.teamcode.team18103.subsystems.Odometry;

import org.firstinspires.ftc.teamcode.team18103.subsystems.Subsystem;

/*
 * Author: Akhil G
 */

public abstract class Odometry extends Subsystem {

    public abstract void run();

    public abstract double getX();

    public abstract double getY();

    public abstract double getTheta();

}
