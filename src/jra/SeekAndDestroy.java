package jra;

import java.awt.Color;
import java.util.Vector;
import robocode.*;
import robocode.util.Utils;

public class SeekAndDestroy extends AdvancedRobot {

	Vector<ScannedRobotEvent> scannedRobots = new Vector<>();
	ScannedRobotEvent nearestRobot;
	int hitTurnDirection = 1;
	
	public void run() {
		setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
		setColors(Color.BLACK, Color.LIGHT_GRAY, Color.DARK_GRAY);
		setBulletColor(Color.MAGENTA);
		setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		turnRadarRightRadians(Double.POSITIVE_INFINITY); //keep turning radar right
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		if (scannedRobots.size() < (getTotalOtherRobots())) {
			log("Adding, Scanned %s - scannedSize: %s - haveScanned %s", e.getName(), scannedRobots.size(), haveScannedRobot(e));
			// Scan all robots first, keeping track of the closest one
			scannedRobots.add(e);
			if (e.isSentryRobot()) {
				return;
			}
			
			if (nearestRobot == null) {
				nearestRobot = e;
			} else if (e.getDistance() < nearestRobot.getDistance()) {
				nearestRobot = e;
			}
		} else {
			// All robots scanned, move to the closest one
			
			if (nearestRobot.getName().equals(e.getName())) {
				nearestRobot = e;
												
				setTurnGunRightRadians(getGunTurnRadians(nearestRobot));
				customFire(nearestRobot);
				
				setTurnRightRadians(getTurnRadians(nearestRobot));
				setAhead(nearestRobot.getDistance() - 50);
			}
			
			if (shouldResetNearestRobot(e)) {
				clearNearestTarget();
			}
		}	
	}
	
	private int getTotalOtherRobots() {
		return getOthers() + getNumSentries();
	}

	private double getTurnRadians(ScannedRobotEvent targetRobot) {
		double absBearing = targetRobot.getBearingRadians() + getHeadingRadians();
		double lateralVelocity = targetRobot.getVelocity() * Math.sin(targetRobot.getHeadingRadians() - absBearing);
		double turnAmountRadians = Utils.normalRelativeAngle(absBearing - getHeadingRadians() + lateralVelocity / getVelocity());
		
		return turnAmountRadians;
	}

	private double getGunTurnRadians(ScannedRobotEvent targetRobot) {
		double absBearing = targetRobot.getBearingRadians() + getHeadingRadians();
		double lateralVelocity = targetRobot.getVelocity() * Math.sin(targetRobot.getHeadingRadians() - absBearing);
		double gunTurnAmountRadians = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + lateralVelocity / 22);
		
		return gunTurnAmountRadians;
	}

	private boolean shouldResetNearestRobot(ScannedRobotEvent e) {
			
		return scannedRobots.size() > getTotalOtherRobots();
	}
	
			
	public void onHitRobot(HitRobotEvent e) {
		if (e.getName() != nearestRobot.getName()) {
			clearNearestTarget();
		}
	}
	
	public void onHitWall(HitWallEvent e) {
		turnRight(-e.getBearing());
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		if (getDistanceRemaining() == 0) {
			setTurnRight(90 + e.getBearing() * hitTurnDirection);
			setAhead(100);
			hitTurnDirection *= -1;
		}
	}
	
	public void onWin(WinEvent e) {
		for (int i = 0; i < 5; i++) {			
			turnGunRight(60);
			turnLeft(30);
			turnGunLeft(60);
			turnRight(30);
		}
	}
	
	private boolean haveScannedRobot(ScannedRobotEvent e) {
		boolean haveScanned = false;
		for (int i = 0; i < scannedRobots.size(); i++) {
			if (e.getName() == scannedRobots.get(i).getName()) {
				haveScanned = true;
				break;
			}
		}
		
		return haveScanned;
	}	
	
	private void clearNearestTarget() {
		scannedRobots = new Vector<>();
		nearestRobot = null;
	}
	
	private void customFire(ScannedRobotEvent e) {
		if (e == null || getGunTurnRemaining() > 0) { return; }
		
		double height = getHeight();
		double distance = e.getDistance();
		double power = 0.5;
		
		double closeRange = height * 4;
		double mediumRange = height * 6;
		double longRange = height * 8;
		
		if (distance <= longRange && distance > mediumRange) {
			power = 1;
		} else if (distance <= mediumRange && distance > closeRange) {
			power = 2;
		} else if (distance <= closeRange) {
			power = 3;
		}
		
		setFire(power);
	}
	
	private void log(String message, Object... params){
		System.out.println(String.format(message,  params));
	}
}
