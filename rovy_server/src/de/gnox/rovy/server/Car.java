package de.gnox.rovy.server;

import java.util.Collection;
import java.util.Optional;

import com.pi4j.io.gpio.RaspiPin;

import de.gnox.rovy.api.RovyTelemetryData;
import de.gnox.rovy.ocv.ArucoDictionary;
import de.gnox.rovy.ocv.ArucoMarker;
import de.gnox.rovy.ocv.CameraProcessor;
import de.gnox.rovy.ocv.Matrix;
import de.gnox.rovy.ocv.Point2i;
import de.gnox.rovy.ocv.Vector;

public class Car {

	private static final int STATUSRECHECK_MILLIS = 1;

	private static final int SPEEDUP_START = 50;

	private static final int SPEEDUP_MILLIS = 2000;

	private Wheel rightWheel;

	private Wheel leftWheel;

	private MarkerFollower markerFollower = null;

	public Car() {
		rightWheel = new Wheel(1, RaspiPin.GPIO_03, RaspiPin.GPIO_06);
		leftWheel = new Wheel(23, RaspiPin.GPIO_29, RaspiPin.GPIO_28);
	}

	private class MarkerFollower implements Runnable {

		private CamTower camTower;

		public MarkerFollower(CamTower camTower) {
			this.camTower = camTower;
		}

		boolean stop = false;

		@Override
		public void run() {
			CameraProcessor camera = new CameraProcessor(false, 0);
			camera.initArucoWithPoseEstimation(ArucoDictionary.DICT_4X4_250, 0.1f);
			camera.startCapturing();
			Point2i camCenter = new Point2i(340, 220);
			camTower.getCam().switchLightOn();

			while (!stop) {

				Collection<ArucoMarker> markers = camera.detectArucoMarkers();
				long startTime = System.currentTimeMillis();
				
				Optional<ArucoMarker> marker42 = markers.stream().filter(marker -> marker.getId() == 42).findAny();
				
//				if (marker42.isPresent())
//					System.out.println("Marker 42: " + marker42.get().getTranslationVector());
				
				rightWheel.stop();
				leftWheel.stop();
				
				if (marker42.isPresent()) {

					Point2i markerCenter = marker42.get().getCenter();
					System.out.println("Marker 42 detected: " + markerCenter + " " + marker42.get().getSize());

					int xDiff = markerCenter.getX() - camCenter.getX();
					int xDiffAbs = Math.abs(xDiff);

					int zDiff = 70 - marker42.get().getSize();
					int zDiffAbs = Math.abs(zDiff);
					
					
					
					if (xDiffAbs > 50 && xDiffAbs < 200 && zDiffAbs > 20) {
						// drive and turn 
						Wheel fastWheel = rightWheel;
						Wheel slowWheel = leftWheel;
						if (zDiff > 0) {
							if (xDiff > 0) {
								fastWheel = leftWheel;
								slowWheel = rightWheel;
							} 
						} else {
							if (xDiff < 0) {
								fastWheel = leftWheel;
								slowWheel = rightWheel;
							} 
						}
						fastWheel.start(zDiff > 0, 100);
						slowWheel.start(zDiff > 0, 70);
						RovyUtility.sleep(100);
					} else if (xDiffAbs <= 50 && zDiffAbs > 20) {
						// drive only
						boolean direction = zDiff > 0;
						rightWheel.start(direction, 100);
						leftWheel.start(direction, 100);
						RovyUtility.sleep(100);
					} else if (xDiffAbs > 50) {
						// turn only
						boolean direction = xDiff > 0;
						rightWheel.start(!direction, 70);
						leftWheel.start(direction, 70);
						RovyUtility.sleep(100);
						rightWheel.stop();
						leftWheel.stop();
					}
					
					// update cam tower position
					int yDiff = markerCenter.getY() - camCenter.getY();
					if (Math.abs(yDiff) > 50) {
						boolean direction = yDiff > 0;
						if (direction)
							camTower.camDownNoCapturing();
						else
							camTower.camUpNoCapturing();
					}
					
				} 
				
				long duration = System.currentTimeMillis() - startTime;
				long sleepTime = 100 - duration;
				if (sleepTime < 1)
					sleepTime = 1;
				RovyUtility.sleep(sleepTime);
			}
			camTower.getCam().switchLightOff();
			camera.stopCapturing();
			Car.this.markerFollower = null;
		}

		public void stop() {
			stop = true;
		}

	};
	
	
	private Optional<ArucoMarker> searchMarker(int markerId, Cam cam, I2cDisplay display) throws RovyException {
		
		cam.switchLightOn();
		
		CameraProcessor camera = new CameraProcessor(false, 0);
		camera.initArucoWithPoseEstimation(ArucoDictionary.DICT_4X4_250, 10f);
		camera.startCapturing();
		
		Optional<ArucoMarker> marker = Optional.empty();
		
		float stepDegrees = 40;
		for (int i = 0; i < (360 / stepDegrees); i++) {
			
			Collection<ArucoMarker> detectedMarkers = camera.detectArucoMarkers();
			marker = detectedMarkers.stream().filter(m -> m.getId() == markerId).findAny();
			if (marker.isPresent())
				break;

			turnInternal(stepDegrees, display);
			RovyUtility.sleep(200);
			
		}
		

		turnToPosition(marker.get().getTranslationVector(), display);
		marker = camera.detectArucoMarkers().stream().filter(m -> m.getId() == markerId).findAny();
		
		camera.stopCapturing();
		cam.switchLightOff();
		
		
		return marker;
	}
	
	public void driveToCharger(CamTower camTower, I2cDisplay display) throws RovyException {

		driveToChargerApproachPosition(camTower, display);
		
		
		if (slideToChargerApproachPosition(camTower, display) == null)
			return;

		doChargerApproach(camTower, display);
	}

	private void driveToChargerApproachPosition(CamTower camTower, I2cDisplay display)
			throws RovyException {
		
		Optional<ArucoMarker> marker = searchMarker(42, camTower.getCam(), display);
		if (!marker.isPresent())
			return;
		
		System.out.println("Marker found at " + marker.get().getTranslationVector());
		
		Matrix markerMatrix = marker.get().getTransformationMatrix();
		
		Vector chargerApproachPos = markerMatrix.mult(new Vector(0.0d, 0.0d, 100.0d));
		driveToPosition(chargerApproachPos,  display);
		
	}
	
	private Double slideToChargerApproachPosition(CamTower camTower, I2cDisplay display)
			throws RovyException {
		
		Double angle = getAngleToCharger(camTower, display);
		if (angle == null)
			return null;
		while (Math.abs(angle) > 5) {
			System.out.println("Angle: " + angle);
			if (angle > 0)
				slide(15, display);
			else 
				slide(-15, display);
			angle = getAngleToCharger(camTower, display);
			if (angle == null)
				return null;
		}
		return angle;
	}

	private Double getAngleToCharger(CamTower camTower, I2cDisplay display) throws RovyException {
		Optional<ArucoMarker> marker;
		marker = searchMarker(42, camTower.getCam(), display);
		if (!marker.isPresent())
			return null;
		
		Vector markerPos = marker.get().getTranslationVector();
		Vector markerZAxis = marker.get().getTransformationMatrix().mult(new Vector(0, 0, -10)).subtract(markerPos);
		markerPos.setY(0);
		markerZAxis.setY(0);
		
		double angle = markerPos.getAngleBetweenAsDeg(markerZAxis);
		return marker.get().getTransformationMatrix().mult(new Vector(0,0,100)).getX() > 0 ? angle : -angle;
	}
	
	public void doChargerApproach(CamTower camTower, I2cDisplay display) throws RovyException {
		
		Optional<ArucoMarker> marker = Optional.empty();
		CameraProcessor camera = new CameraProcessor(false, 0);
		camera.initArucoWithPoseEstimation(ArucoDictionary.DICT_4X4_250, 10f);
		camera.startCapturing();
		
		long millisStart = System.currentTimeMillis();
		while (true) {
			Collection<ArucoMarker> detectedMarkers = camera.detectArucoMarkers();
			marker = detectedMarkers.stream().filter(m -> m.getId() == 42).findAny();
			if (marker.isPresent()) {
				Vector chargerVec = marker.get().getTranslationVector();
				System.out.println("Distance to Charger: " + chargerVec.getLength());
				if (chargerVec.getLength() < 34) 
					break;
				double angle = getAngleToPosition(chargerVec.getX(), chargerVec.getZ());
				if (Math.abs(angle) >= 3) {
					turnFwdBkw((float)angle, true, display);
				} else {
					driveInternal(3, display);
				}
				RovyUtility.sleep(50);

			}
			
			if (System.currentTimeMillis() - millisStart > 1000 * 60)
				break;
		}
		
		camera.stopCapturing();
		
	}
	
	private void driveToPosition(Vector position3D,  I2cDisplay display) throws RovyException {
			driveToPosition(position3D.getX(), position3D.getZ(), display);
	}
	
	private void turnToPosition(Vector position3D,  I2cDisplay display) throws RovyException {
		turnToPosition(position3D.getX(), position3D.getZ(), display);
}

	private void driveToPosition(double x, double z,  I2cDisplay display) throws RovyException {
		
		double turnAngle = turnToPosition(x, z, display);
		
		double dist = Math.sqrt( x*x + z*z);
		
		System.out.println("drive to position: x=" + x + " z=" + z  );
		driveInternal((int)dist,  display);
		
		RovyUtility.sleep(100);
		
		turnInternal((float)-turnAngle, display);
		
		RovyUtility.sleep(100);
		
	}

	private double turnToPosition(double x, double z, I2cDisplay display) {
		
		// Vereinfachung der Formel fuer Winkel zwischen Vectoren:
		double aDeg = getAngleToPosition(x, z);
		
		System.out.println("turn to angle: " + aDeg);
		
		turnInternal((float)aDeg, display);

		RovyUtility.sleep(100);
		
		return aDeg;
	}

	private double getAngleToPosition(double x, double z) {
		double aRad = Math.acos( z / Math.sqrt(x*x + z*z));	
	
		double aDeg = aRad * (double)360 / ((double)2 * Math.PI);
		if (x < 0)
			aDeg = -aDeg;
		return aDeg;
	}

	public boolean isMarkerFollowingMode() {
		return markerFollower != null;
	}

	public void startMarkerFollowingMode(CamTower cam) {
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");
		markerFollower = new MarkerFollower(cam);
		new Thread(markerFollower).start();
	}

	public void stopMarkerFollowingMode() {
		markerFollower.stop();
	}

	public void drive(int cm, Cam cam, I2cDisplay display) throws RovyException {
		if (cm < -1000 || cm > 1000)
			throw new RovyException("cm not between +-1000");
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");

		cam.startCapturing();
		driveInternal(cm, display);
		cam.finishCapturing();
		display.lookNormal();
	}

	private void driveInternal(int cm, I2cDisplay display) {
		float meters = (float) cm / 100.0f;

		float metersAbs = Math.abs(meters);

		int speed = 0;

		boolean foreward = meters > 0;

		if (display != null)
			if (foreward)
				display.lookForeward();
			else
				display.lookBackward();

		rightWheel.start(foreward, speed);
		leftWheel.start(foreward, speed);

		long startTime = System.currentTimeMillis();
		long now = startTime;
		while (now - startTime < WHEEL_TIMEOUT) {
			speed = speedup(startTime, now, 100);

			RovyUtility.sleep(STATUSRECHECK_MILLIS);
			rightWheel.processInput();
			leftWheel.processInput();

			synchronizeWheels(speed / 2, speed);
			if (rightWheel.getDistanceInMeter() >= metersAbs) {
				rightWheel.stop();
			}
			if (leftWheel.getDistanceInMeter() >= metersAbs) {
				leftWheel.stop();
			}
			if (rightWheel.isStopped() && leftWheel.isStopped())
				break;
			now = System.currentTimeMillis();
		}
		stopNow();
	}

	private void synchronizeWheels(int minSpeed, int maxSpeed) {
		if (rightWheel.isStopped() && leftWheel.isStopped())
			return;
		if (rightWheel.isStopped()) {
			leftWheel.setSpeed(maxSpeed);
			return;
		}
		if (leftWheel.isStopped()) {
			rightWheel.setSpeed(maxSpeed);
			return;
		}

		long leftDistance = leftWheel.getDistance();
		long rightDistance = rightWheel.getDistance();

		int leftSpeedNew = maxSpeed;
		int rightSpeedNew = maxSpeed;

		if (leftDistance > rightDistance) {

			rightSpeedNew = maxSpeed;
			leftSpeedNew = minSpeed;

		} else if (leftDistance < rightDistance) {

			rightSpeedNew = minSpeed;
			leftSpeedNew = maxSpeed;

		}

		leftWheel.setSpeed(leftSpeedNew);
		rightWheel.setSpeed(rightSpeedNew);

	}

	public void turn(float degrees, Cam cam, I2cDisplay display) throws RovyException {
		if (degrees < -360 || degrees > 360)
			throw new RovyException("degrees not between +-360");
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");

		cam.startCapturing();
		turnInternal(degrees, display);
		cam.finishCapturing();
	}

	private void turnInternal(float degrees, I2cDisplay display) {
		int speed = 0;
		boolean right = degrees > 0;

		if (right)
			display.lookRight();
		else
			display.lookLeft();

		rightWheel.start(!right, speed);
		leftWheel.start(right, speed);

		int targetDistance = (int) (Math.abs(degrees) * DISTANCE_OF_DEGREE);

		long startTime = System.currentTimeMillis();
		long now = startTime;
		while (now - startTime < WHEEL_TIMEOUT) {
			speed = speedup(startTime, now, 70);

			RovyUtility.sleep(STATUSRECHECK_MILLIS);
			rightWheel.processInput();
			leftWheel.processInput();

			synchronizeWheels(speed / 2, speed);
			if (rightWheel.getDistance() >= targetDistance)
				rightWheel.stop();
			if (leftWheel.getDistance() >= targetDistance)
				leftWheel.stop();
			if (rightWheel.isStopped() && leftWheel.isStopped())
				break;
			now = System.currentTimeMillis();
		}
		stopNow();

		display.lookNormal();
	}

	public void turnFwdBkw(float degrees, boolean fwd, I2cDisplay display) throws RovyException {
		if (degrees < -360 || degrees > 360)
			throw new RovyException("degrees not between +-360");
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");

		int speed = 0;

		Wheel wheel = rightWheel;
		if (fwd) {
			if (degrees > 0) {
				wheel = leftWheel;
				display.lookRight();
			} else {
				display.lookLeft();
			}
		} else {
			if (degrees < 0) {
				wheel = leftWheel;
				display.lookLeft();
			} else {
				display.lookRight();
			}
		}

		wheel.start(fwd, speed);

		int targetDistance = (int) (Math.abs(degrees) * DISTANCE_OF_DEGREE_ONEWHEEL);
		long startTime = System.currentTimeMillis();
		long now = startTime;
		while (now - startTime < WHEEL_TIMEOUT) {
			speed = speedup(startTime, now, 100);

			RovyUtility.sleep(STATUSRECHECK_MILLIS);
			wheel.processInput();

			wheel.setSpeed(speed);

			if (wheel.getDistance() >= targetDistance) {
				wheel.stop();
				break;
			}
			now = System.currentTimeMillis();
		}
		stopNow();
		display.lookNormal();
	}

	public void slide(float degrees, I2cDisplay display) throws RovyException {
		if (degrees < -90 || degrees > 90)
			throw new RovyException("degrees not between +-90");
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");

		turnFwdBkw(-degrees, false, display);
		turnFwdBkw(degrees, false, display);
		turnFwdBkw(degrees, true, display);
		turnFwdBkw(-degrees, true, display);
	}

	private int speedup(long starttime, long now, int maxspeed) {
		return speedup(now - starttime, maxspeed);
	}

	private int speedup(long driveTime, int maxspeed) {
		int speedoffset = maxspeed - SPEEDUP_START;
		int speed;
		speed = SPEEDUP_START + (int) ((float) speedoffset * (float) driveTime / (float) SPEEDUP_MILLIS);
		if (speed > maxspeed)
			speed = maxspeed;
		return speed;
	}

	public void dance(Cam cam) throws RovyException {
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");

		int millis1 = 200;
		int millis2 = 400;
		int millis3 = 200;

		// cam.clear();
		cam.startCapturing();

		int speed = 100;
		rightWheel.start(false, speed);
		leftWheel.start(true, speed);

		RovyUtility.sleep(millis1);
		stopNow();

		rightWheel.start(true, speed);
		leftWheel.start(false, speed);

		RovyUtility.sleep(millis2);
		stopNow();

		rightWheel.start(false, speed);
		leftWheel.start(true, speed);

		RovyUtility.sleep(millis3);
		stopNow();

		cam.finishCapturing();
	}

	private void stopNow() {

		rightWheel.stop();
		leftWheel.stop();
	}

	public void fillTelemetryData(String prefix, RovyTelemetryData telemetryData) {

		telemetryData.getEntries().add(prefix + "markerFollowingMode: " + isMarkerFollowingMode());
		rightWheel.fillTelemetryData(prefix + "rightWheel: ", telemetryData);
		leftWheel.fillTelemetryData(prefix + "leftWheel: ", telemetryData);
	}

	private float DISTANCE_OF_DEGREE = 72.0f / 360.0f;

	private float DISTANCE_OF_DEGREE_ONEWHEEL = 146.0f / 360.0f;

	private int WHEEL_TIMEOUT = 20000;

}
