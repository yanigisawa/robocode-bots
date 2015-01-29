package jra;

import java.awt.Color;
import java.util.Vector;
import robocode.*;

public class SeekAndDestroy extends AdvancedRobot {

	Vector<ScannedRobotEvent> scannedRobots = new Vector<>();
	ScannedRobotEvent nearestRobot;
	
	public void run() {
		setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
		setColors(Color.BLACK, Color.LIGHT_GRAY, Color.DARK_GRAY);
		setBulletColor(Color.blue);
		//setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		turnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right		
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		
		//setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		if (scannedRobots.size() < getOthers() && !haveScannedRobot(e)) {
			// Scan all robots first, keeping track of the closest one
			scannedRobots.add(e);
			if (nearestRobot == null) {
				nearestRobot = e;
			} else if (e.getDistance() < nearestRobot.getDistance()) {
				nearestRobot = e;
			}
			return;
		} else {
			// All robots scanned, move to the closest one
			
			if (nearestRobot.getName().equals(e.getName())) {				
				nearestRobot = e;			
				turnRight(nearestRobot.getBearing());			
				ahead(nearestRobot.getDistance() - 50);
				customFire(nearestRobot);			
			}
			
			if (shouldResetNearestRobot(e)) {
				scannedRobots = new Vector<>();
				nearestRobot = null;
			}
		}		
	}
	
	private boolean shouldResetNearestRobot(ScannedRobotEvent e) {
		boolean nearestChangedDistance = e.getName().equals(nearestRobot.getName()) 
				&& e.getBearing() != nearestRobot.getBearing();
		boolean someBotDied = scannedRobots.size() != getOthers();
		
		log("Distance changed %s - botDied %s - scanned %s - getOthers %s", nearestChangedDistance, someBotDied, scannedRobots.size(), getOthers());
		return nearestChangedDistance || someBotDied;
	}
	
			
	public void onHitRobot(HitRobotEvent e) {
		ahead(0); // Stop movement
	}
	
	public void onHitWall(HitWallEvent e) {
		turnRight(e.getBearing());
	}
	
	private boolean haveScannedRobot(ScannedRobotEvent e) {
		boolean haveScanned = false;
		for (int i = 0; i < scannedRobots.size(); i++) {
			if (e.getName() == scannedRobots.get(i).getName()) {
				haveScanned = true;
				break;
			}
		}
		
		log("Have Scanned %s - %s", e.getName(), haveScanned);
		return haveScanned;
	}
	
	private void customFire(ScannedRobotEvent e) {
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
		
		log("Firing on %s for power %s", e.getName(), power);
		fire(power);
	}
	
	private void log(String message, Object... params){
		System.out.println(String.format(message,  params));
	}
}
