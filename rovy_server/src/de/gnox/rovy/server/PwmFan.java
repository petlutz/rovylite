package de.gnox.rovy.server;

import com.pi4j.wiringpi.SoftPwm;

import de.gnox.rovy.api.RovyTelemetryData;

public class PwmFan {

	private int pinPwm;

	private int speed = 0;
	
	public PwmFan(int pinPwm) {
		this.pinPwm = pinPwm;
		SoftPwm.softPwmCreate(pinPwm, 0, PWM_RANGE);
		setSpeed(0);
	}
	
	public synchronized void setSpeed(int speed) {
		this.speed = speed;
		if (speed > 100)
			speed = 100;
		if (speed < 0)
			speed = 0;

		SoftPwm.softPwmWrite(pinPwm, this.speed);
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
