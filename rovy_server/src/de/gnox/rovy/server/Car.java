package de.gnox.rovy.server;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import com.pi4j.io.gpio.RaspiPin;

import de.gnox.rovy.api.RovyTelemetryData;
import de.gnox.rovy.ocv.ArucoDictionary;
import de.gnox.rovy.ocv.ArucoMarker;
import de.gnox.rovy.ocv.MarkerDetector;
import de.gnox.rovy.ocv.Point;

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

		// rightWheel = new HwPwmWheel(RaspiPin.GPIO_01, RaspiPin.GPIO_03,
		// RaspiPin.GPIO_22);
		// leftWheel = new HwPwmWheel(RaspiPin.GPIO_23, RaspiPin.GPIO_29,
		// RaspiPin.GPIO_28);

		// leftWheel = new Wheel(40, 38);
	}

	private class MarkerFollower implements Runnable {

		private CamTower camTower;

		public MarkerFollower(CamTower camTower) {
			this.camTower = camTower;
		}

		boolean stop = false;

		@Override
		public void run() {
			MarkerDetector detector = new MarkerDetector(false, 0, ArucoDictionary.DICT_4X4_250);
			detector.startCapturing();
			Point camCenter = new Point(340, 220);
			camTower.getCam().switchLightOn();

//			boolean driving = false;
//			long drivingStartTime = 0;


			while (!stop) {

				Collection<ArucoMarker> markers = detector.detectMarkers();
				long startTime = System.currentTimeMillis();
				
				Optional<ArucoMarker> marker42 = markers.stream().filter(marker -> marker.getValue() == 42).findAny();

				rightWheel.stop();
				leftWheel.stop();
				
				if (marker42.isPresent()) {

					Point markerCenter = marker42.get().getCenter();
					System.out.println("Marker 42 detected: " + markerCenter + " " + marker42.get().getSize());

					int xDiff = markerCenter.getX() - camCenter.getX();

					if (Math.abs(xDiff) > 50) {

							boolean direction = xDiff > 0;
							rightWheel.start(!direction, 70);
							leftWheel.start(direction, 70);
							RovyUtility.sleep(100);
							rightWheel.stop();
							leftWheel.stop();

		
					} else {

						int sizeDiff = 70 - marker42.get().getSize();
						if (Math.abs(sizeDiff) > 20) {

								boolean direction = sizeDiff > 0;
								rightWheel.start(direction, 100);
								leftWheel.start(direction, 100);
								RovyUtility.sleep(100);
//								rightWheel.stop();
//								leftWheel.stop();
						
						} 

					}

					int yDiff = markerCenter.getY() - camCenter.getY();

					if (Math.abs(yDiff) > 50) {
						boolean direction = yDiff > 0;

						if (direction)
							camTower.camDownNoCapturing();
						else
							camTower.camUpNoCapturing();
					}
					
				} else {
						rightWheel.stop();
						leftWheel.stop();
					
				}
				
				long duration = System.currentTimeMillis() - startTime;
				long sleepTime = 100 - duration;
				if (sleepTime < 1)
					sleepTime = 1;
				RovyUtility.sleep(sleepTime);
			}
			camTower.getCam().switchLightOff();
			detector.stopCapturing();
			Car.this.markerFollower = null;
		}

		public void stop() {
			stop = true;
		}

	};

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

	// public void drive(float meters, Cam cam) throws RovyException {
	// if (meters < -10 || meters > 10)
	// throw new RovyException("meters not between +-10");
	//
	// int millis = (int) (Math.abs(meters) * 4166.0f);
	//// cam.clear();
	// cam.captureVideoAsync(millis);
	//
	// int speed = 100;
	// if (meters > 0) {
	// rightWheel.start(true, speed);
	// leftWheel.start(true, speed);
	// } else {
	// rightWheel.start(false, speed);
	// leftWheel.start(false, speed);
	// }
	//
	// RovyUtility.sleep(millis);
	// // RoverUtility.sleep((int)(1000f));
	// stopNow();
	// cam.waitForVideo();
	// }

	public void driveNew(int cm, Cam cam, I2cDisplay display) throws RovyException {
		if (cm < -1000 || cm > 1000)
			throw new RovyException("cm not between +-1000");
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");

		cam.startCapturing();

		driveNewInternal(cm, display);

		// cam.waitForVideo();
		cam.finishCapturing();
		display.lookNormal();
	}

	private void driveNewInternal(int cm, I2cDisplay display) {
		float meters = (float) cm / 100.0f;

		float metersAbs = Math.abs(meters);

		// int millis = (int) (metersAbs * 4166.0f);

		// cam.captureVideoAsync(millis);

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
				// rightWheel.brake();
			}
			if (leftWheel.getDistanceInMeter() >= metersAbs) {
				leftWheel.stop();
				// leftWheel.brake();
			}
			if (rightWheel.isStopped() && leftWheel.isStopped())
				break;
			now = System.currentTimeMillis();
		}
		stopNow();
		// System.out.println("video millis: " + millis);
		// System.out.println("needed millis: " + (now - startTime));

		// RovyUtility.sleep(millis);
		// // RoverUtility.sleep((int)(1000f));
		// stopNow();
	}

	// public void driveStepped(float meters, Cam cam) throws RovyException {
	// if (meters < -10 || meters > 10)
	// throw new RovyException("meters not between +-10");
	//
	// float metersAbs = Math.abs(meters);
	//
	// int targetDistance = (int)(Wheel.DISTANCE_OF_METER * metersAbs);
	//
	//// int targetDistance = (int)(metersAbs);
	//
	//// int millis = (int) (metersAbs * 4166.0f);
	////// cam.clear();
	//// cam.captureVideoAsync(millis);
	//
	//
	// boolean foreward = meters >= 0;
	//
	// rightWheel.stepNew(true);
	// leftWheel.stepNew(true);
	//
	// // distance to
	//// rightWheel.stop();
	//// leftWheel.stop();
	//// rightWheel.setDDistance(0);
	//// leftWheel.setDDistance(0);
	//
	//// for (int i = 0; i < 100; i++) {
	//// if (rightWheel.getDDistance() == 1 && leftWheel.getDDistance() == 1)
	//// break;
	////
	//// if (leftWheel.getDDistance() > 1)
	//// leftWheel.stepNew(false);
	//// else if (leftWheel.getDDistance() < 1)
	//// leftWheel.stepNew(true);
	////
	//// if (rightWheel.getDDistance() > 1)
	//// rightWheel.stepNew(false);
	//// else if (rightWheel.getDDistance() < 1)
	//// rightWheel.stepNew(true);
	////
	//// RovyUtility.sleep(100);
	//// }
	//
	//// long startTime = System.currentTimeMillis();
	//// long now = startTime;
	//// while (now - startTime < WHEEL_TIMEOUT) {
	////
	//// if (rightWheel.getDistance() <= leftWheel.getDistance())
	//// rightWheel.stepNew(foreward);
	////
	//// if (leftWheel.getDistance() <= rightWheel.getDistance())
	//// leftWheel.stepNew(foreward);
	////
	//// if (rightWheel.getDistance() >= targetDistance &&
	// leftWheel.getDistance() >= targetDistance)
	//// break;
	////
	////
	//// RovyUtility.sleep(100);
	//// now = System.currentTimeMillis();
	//// }
	//
	//
	//// RovyUtility.sleep(millis);
	//// // RoverUtility.sleep((int)(1000f));
	//// stopNow();
	//// cam.waitForVideo();
	// }

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
		// int leftSpeed = leftWheel.getSpeed();
		long rightDistance = rightWheel.getDistance();
		// int rightSpeed = rightWheel.getSpeed();

		int leftSpeedNew = maxSpeed;
		int rightSpeedNew = maxSpeed;

		if (leftDistance > rightDistance) {

			rightSpeedNew = maxSpeed;
			leftSpeedNew = minSpeed;

		} else if (leftDistance < rightDistance) {

			rightSpeedNew = minSpeed;
			leftSpeedNew = maxSpeed;

		}

		// System.out.println("speed sync: L=" + leftSpeedNew + " R=" +
		// rightSpeedNew);

		leftWheel.setSpeed(leftSpeedNew);
		rightWheel.setSpeed(rightSpeedNew);

	}

	// private void synchronizeWheels() {
	// if (rightWheel.isStopped() || leftWheel.isStopped())
	// return;
	//
	// long leftDistance = leftWheel.getDistance();
	// int leftSpeed = leftWheel.getSpeed();
	// long rightDistance = rightWheel.getDistance();
	// int rightSpeed = rightWheel.getSpeed();
	//
	//
	// int leftSpeedNew = leftSpeed;
	// int rightSpeedNew = rightSpeed;
	//
	// if (leftDistance > rightDistance) {
	//
	// long distanceDiff = leftDistance - rightDistance;
	// int speedKorr = (int)(40 * distanceDiff / 2);
	//
	// rightSpeedNew += speedKorr;
	// if (rightSpeedNew > 100) {
	// leftSpeedNew -= (rightSpeedNew - 100);
	// rightSpeedNew = 100;
	// }
	//
	// } else if (leftDistance < rightDistance) {
	//
	// long distanceDiff = rightDistance - leftDistance;
	// int speedKorr = (int)(40 * distanceDiff / 2);
	//
	// leftSpeedNew += speedKorr;
	// if (leftSpeedNew > 100) {
	// rightSpeedNew -= (leftSpeedNew - 100);
	// leftSpeedNew = 100;
	// }
	//
	// }
	//
	//
	// if (leftSpeedNew < 70)
	// leftSpeedNew = 70;
	// if (rightSpeedNew < 70)
	// rightSpeedNew = 70;
	//
	// System.out.println("speed sync: L=" + leftSpeedNew + " R=" +
	// rightSpeedNew);
	//
	// leftWheel.setSpeed(leftSpeedNew);
	// rightWheel.setSpeed(rightSpeedNew);
	//
	// }

	// public void turn(float degrees, Cam cam) throws RovyException {
	// if (degrees < -360 || degrees > 360)
	// throw new RovyException("degrees not between +-360");
	//
	// int millis = (int) (Math.abs(degrees) * 5.5f);
	//// cam.clear();
	// cam.captureVideoAsync(millis);
	//
	// int speed = 50;
	// if (degrees > 0) {
	// rightWheel.start(false, speed);
	// leftWheel.start(true, speed);
	// } else {
	// rightWheel.start(true, speed);
	// leftWheel.start(false, speed);
	// }
	//
	// RovyUtility.sleep(millis);
	// stopNow();
	// cam.waitForVideo();
	// }

	public void turnNew(float degrees, Cam cam, I2cDisplay display) throws RovyException {
		if (degrees < -360 || degrees > 360)
			throw new RovyException("degrees not between +-360");
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");

		// int millis = (int) (Math.abs(degrees) * 6.5f);
		// cam.clear();
		cam.startCapturing();

		int speed = 0;
		boolean right = degrees > 0;

		if (right)
			display.lookRight();
		else
			display.lookLeft();

		rightWheel.start(!right, speed);
		leftWheel.start(right, speed);

		int targetDistance = (int) (Math.abs(degrees) * DISTANCE_OF_DEGREE);
		// targetDistance = Math.max(0, targetDistance - 3); // Wegen des
		// Schwungs nochmal 3 abziehen
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
		// System.out.println("video millis: " + millis);
		// System.out.println("needed millis: " + (now - startTime));

		// cam.waitForVideo();
		cam.finishCapturing();
		display.lookNormal();
	}

	public void turnFwdBkw(float degrees, boolean fwd, I2cDisplay display) throws RovyException {
		if (degrees < -360 || degrees > 360)
			throw new RovyException("degrees not between +-360");
		if (isMarkerFollowingMode())
			throw new IllegalStateException("stop marker following mode first");

		// int millis = (int) (Math.abs(degrees) * 6.5f);
		// cam.clear();
		// cam.startCapturing();

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
		// System.out.println("video millis: " + millis);
		// System.out.println("needed millis: " + (now - startTime));

		// cam.waitForVideo();
		// cam.finishCapturing();
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
		int millis = millis1 + millis2 + millis3;

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
