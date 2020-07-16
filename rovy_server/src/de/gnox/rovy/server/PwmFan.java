package de.gnox.rovy.server;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import de.gnox.rovy.api.RovyTelemetryData;

public class PwmFan {

//	private int pinPwm;

	private int speed = 0;

	private GpioPinPwmOutput pwm;
	
	private GpioPinDigitalOutput power;

	public PwmFan(Pin pinFanControl, Pin pinFanPower) {
//		this.pinPwm = pinPwm;

		pwm = GpioFactory.getInstance().provisionPwmOutputPin(pinFanControl);
		
		power = GpioFactory.getInstance().provisionDigitalOutputPin(pinFanPower, "fanPower", PinState.LOW);

		com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
		com.pi4j.wiringpi.Gpio.pwmSetRange(PWM_RANGE);
		com.pi4j.wiringpi.Gpio.pwmSetClock(500);

//		SoftPwm.softPwmCreate(pinPwm, 0, PWM_RANGE);
		setSpeed(0);
	}

	public synchronized void setSpeed(int speed) {
		int newspeed = speed;
		if (newspeed > 100)
			newspeed = 100;
		if (newspeed < 0)
			newspeed = 0;
		this.speed = newspeed;
		power.setState(speed > 0 ? PinState.HIGH : PinState.LOW);
		pwm.setPwm(newspeed);
//		SoftPwm.softPwmWrite(pinPwm, this.speed);
	}
	
	public void update(float currentTemperature, float targetTemperature) {
		float tempDiff = currentTemperature - (float) targetTemperature;
		if (tempDiff > 0) {
			setSpeed((int) (tempDiff * 40.0f));
		} else {
			stop();
		}
	}

	public int getSpeed() {
		return speed;
	}

	public void stop() {
		setSpeed(0);
	}

	public void fillTelemetryData(String prefix, RovyTelemetryData telemetryData) {
		telemetryData.getEntries().add(prefix + "speed: " + speed + "%");
	}

	private static int PWM_RANGE = 100;

}
