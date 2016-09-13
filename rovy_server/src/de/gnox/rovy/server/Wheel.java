package de.gnox.rovy.server;

import java.util.concurrent.atomic.AtomicLong;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.wiringpi.SoftPwm;

import de.gnox.rovy.api.RovyTelemetryData;

public class Wheel {

	private int pinPwm;

	private GpioPinDigitalInput distanceInput;

	private AtomicLong distance = new AtomicLong(0);

	private AtomicLong dDistance = new AtomicLong(0);

	private GpioPinDigitalOutput directionOutput;

	private int speed = 0;
	
	private boolean intelligentSpeedCtrl = false;

	public Wheel(Pin speedInputPin) {
		initSpeedInput(speedInputPin);
	}

	// private GpioPinDigitalOutput digitalOutBackward;

	public Wheel(int pinPwm, Pin pinDirection) {

		// GpioController gpio = GpioFactory.getInstance();
		// digitalOutForward = gpio.provisionDigitalOutputPin(pinOutForward);
		// digitalOutBackward = gpio.provisionDigitalOutputPin(pinOutBackward);
		// digitalOutForward.low();
		// digitalOutBackward.low();
		this.pinPwm = pinPwm;
		this.directionOutput = GpioFactory.getInstance().provisionDigitalOutputPin(pinDirection, PinState.LOW);
		SoftPwm.softPwmCreate(pinPwm, 0, PWM_RANGE);
		SoftPwm.softPwmWrite(pinPwm, 0);

	}

	public Wheel(int pinPwm, Pin pinDirection, Pin speedInputPin) {
		this(pinPwm, pinDirection);
		initSpeedInput(speedInputPin);
	}

	public boolean isForeward() {
		return directionOutput.isHigh();
	}

	public void initSpeedInput(Pin speedInputPin) {
		distanceInput = GpioFactory.getInstance().provisionDigitalInputPin(speedInputPin, PinPullResistance.PULL_DOWN);
		distanceInputLastState = distanceInput.isHigh();

//		Thread pollingThread = new Thread( () -> {
//			boolean lastState = distanceInput.isHigh();
//			while (true) {
//				
//				boolean state = distanceInput.isHigh();
//
//				if (state != lastState) {
//
//					distance.incrementAndGet();
//
//					if (isForeward())
//						dDistance.incrementAndGet();
//					else
//						dDistance.decrementAndGet();
//
////					if (stopTarget != null && distance >= stopTarget) {
////						stop();
////						stopTarget = null;
////					}
//					lastState = state;
//				}
//				
//				RovyUtility.sleep(1);
//			}	
//			
//		} );
//		pollingThread.start();
		
		
//		distanceInput.addListener(new GpioPinListenerDigital() {
//			@Override
//			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent e) {
//				// if (/*!isStopped() &&*/ e.getState().isHigh()) {
//				distance++;
//
//				if (isForeward())
//					dDistance++;
//				else
//					dDistance--;
//
//				// System.out.println("Dist:" + speedInputPin.getAddress() + ":
//				// " + distance);
//				// }
//				if (stopTarget != null && distance >= stopTarget) {
//					stop();
//					stopTarget = null;
//				}
//
//			}
//		});
	}

	public void step(boolean foreward) {
//		System.out.println("step " + pinPwm + " " + pinDirection);
		start(foreward);
		RovyUtility.sleep(20);
		stop();
	}

//	Long stopTarget = null;
//
//	public void stepNew(boolean foreward) {
////		System.out.println("stepNew " + pinPwm + " " + pinDirection);
//		stopTarget = 1l;
//		// lastDirection = foreward;
//		start(foreward);
//	}

	public void start(boolean foreward) {
		start(foreward, 100);
	}

	public void start(boolean foreward, int speed) {
		if (!isStopped())
			throw new IllegalStateException();

		measuredSpeedDps = null;
		speedMeasurePointMillis = System.currentTimeMillis();
		speedMeasurePointDistance = 0l;
		targetSpeedDps = null;
		
		distance.set(0);
//		System.out.println("start " + pinPwm + "/" + pinDirection + ":" + speed);
		// currentPin = foreward ? pinPwm : pinDirection;


		
//		SoftPwm.softPwmCreate(pinPwm, 0, PWM_RANGE);
		if (foreward)
			directionOutput.high();
		else
			directionOutput.low();
		
		stopped = false;
		setSpeed(speed);

	}
	
	Float targetSpeedDps = null;

	public void setTargetSpeedDps(float speedDps) {
		if (isStopped())
			throw new IllegalStateException();
		this.targetSpeedDps = speedDps;
	}
	
	public void setSpeed(int speed) {
		if (isStopped())
			throw new IllegalStateException();
		// if (this.speed == speed)
		// return;

		int pwmVal = speed;
		if (isForeward())
			pwmVal = PWM_RANGE - speed;
		
//		System.out.println("speed: " + pwmVal);
		// speedOutput.setPwm(pwn);
		// this.speed = speed;

		SoftPwm.softPwmWrite(pinPwm, pwmVal);
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}

	public void stop() {
		if (isStopped())
			return;
//		System.out.println("stop " + pinPwm + "/" + pinDirection);

		setSpeed(0);
		stopped = true;
		targetSpeedDps = null;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void fillTelemetryData(String prefix, RovyTelemetryData telemetryData) {
		telemetryData.getEntries().add(prefix + "status: " + (isStopped() ? "STOPPED" : "RUNNING"));
		telemetryData.getEntries().add(prefix + "distanceCnt: " + distance);
		telemetryData.getEntries().add(prefix + "dDistanceCnt: " + dDistance);
	}

	public long getDistance() {
		return distance.get();
	}

//	public void setDDistance(long dDistance) {
//		this.dDistance = dDistance;
//	}

	public long getDDistance() {
		return dDistance.get();
	}

	public float getDistanceInMeter() {
		return (float) distance.get() / DISTANCE_OF_METER;
	}

	boolean distanceInputLastState;
	
	Long speedMeasurePointMillis = null;
	Long speedMeasurePointDistance = null;
	Float measuredSpeedDps = null;
	
	public void processInput() {
		
		boolean state = distanceInput.isHigh();

		if (state != distanceInputLastState) {

			distance.incrementAndGet();

			if (isForeward())
				dDistance.incrementAndGet();
			else
				dDistance.decrementAndGet();

//			if (stopTarget != null && distance >= stopTarget) {
//				stop();
//				stopTarget = null;
//			}
			distanceInputLastState = state;
		}
		
		long currentTime = System.currentTimeMillis();
		if (currentTime- speedMeasurePointMillis >= 250) {
			float t = (float)(currentTime - speedMeasurePointMillis) / (float)1000;
			float s = getDistance() - speedMeasurePointDistance;
			measuredSpeedDps = (float)s / (float)t;
			System.out.println("Speed [dps]: s=" + s + " v=" + measuredSpeedDps );
			speedMeasurePointDistance = getDistance();
			speedMeasurePointMillis = currentTime;

			correctSpeedToTargetSpeedDps();
		}

		
	}
	
	private void correctSpeedToTargetSpeedDps() {
		if (targetSpeedDps == null)
			return;
		int newSpeed = (int)((float)getSpeed() * targetSpeedDps / measuredSpeedDps);
		System.out.println("Speed corr: " + getSpeed() + " -> " + newSpeed);
		setSpeed(newSpeed);
	}

	public Float getMeasuredSpeedDps() {
		return measuredSpeedDps;
	}

	protected void setDistance(long distance) {
		this.distance.set( distance );
	}
	// public void brake() {
	// step(!lastDirection);
	// }

	// private int STEP_TIMEOUT = 1000;
	
	private boolean stopped = true;

	public static final float DISTANCE_OF_METER = 185.0f;

	private static int PWM_RANGE = 100;

}
