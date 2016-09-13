package de.gnox.rovy.server;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import de.gnox.rovy.api.RovyTelemetryData;

public class HwPwmWheel extends Wheel {

//	public HwPwmWheel(int pinForward, int pinBackward) {
//		super(pinForward, pinBackward);
//	}
//
//	public HwPwmWheel(int pinForward, int pinBackward, Pin speedInputPin) {
//		super(pinForward, pinBackward, speedInputPin);
//		// TODO Auto-generated constructor stub
//	}

	private GpioPinPwmOutput speedOutput;
	
	private GpioPinDigitalOutput directionOutput;
	

	public HwPwmWheel(Pin speedPin, Pin directionPin, Pin speedInputPin) {
		super(speedInputPin);
		
		this.speedOutput = GpioFactory.getInstance().provisionPwmOutputPin(speedPin, 0);
		this.directionOutput = GpioFactory.getInstance().provisionDigitalOutputPin(directionPin, PinState.LOW);
		this.speedOutput.setPwmRange(PWM_RANGE);
		this.speed = 0;
//		this.speedOutput.setPwm(0);
//		this.directionOutput.low();
	}
	
	@Override
	public void stop() {
		if (isStopped()) 
			return;
//		System.out.println("stop " + pinForward + "/" + pinBackward );
//
//		setSpeed(0);
//		currentPin = null;
		setSpeed(0);
		stopped = true;
	}
	
	@Override
	public boolean isStopped() {
		return stopped;
//		return currentPin == null;
	}
	
	@Override
	public void setSpeed(int speed) {
//		if (isStopped())
//			throw new IllegalStateException();
		
		int speedPwm = speed * PWM_RANGE / 100;
		
		int pwn = speedPwm;
		if (directionOutput.isHigh())
			pwn = PWM_RANGE - speedPwm;
		System.out.println("speed: " + pwn);
		speedOutput.setPwm(pwn);
		this.speed = speed;
		
//		if (this.speed == speed)
//			return;
//		SoftPwm.softPwmWrite(currentPin, 100 - speed );
//		this.speed = speed;
	}
	
	@Override
	public void start(boolean foreward, int speed ) {
		if (!isStopped())
			throw new IllegalStateException();
		
		
		if (foreward)
			directionOutput.high();
		else
			directionOutput.low();
		
//		lastDirection = foreward;
//		distance = 0;
//		System.out.println("start " + pinForward + "/" + pinBackward + ":" + speed);
//		currentPin = foreward ? pinForward : pinBackward;
//
//		SoftPwm.softPwmCreate(currentPin, 100, 100);
		setDistance(0);
		setSpeed(speed);
		stopped = false;
	}
	
	@Override
	public int getSpeed() {
		return speed;
	}
	
	int speed;
	
	boolean stopped = true;
	
	private int PWM_RANGE = 100;

}
