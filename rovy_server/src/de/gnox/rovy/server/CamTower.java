package de.gnox.rovy.server;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

import de.gnox.rovy.api.RovyTelemetryData;

public class CamTower {
	

	private Wheel camWheel;
	
	private Cam cam;

	private GpioPinDigitalInput upStopInput;
	
	private GpioPinDigitalInput downStopInput;
	
	private AllowedDirection allowedDirection = AllowedDirection.BOTH;
	
	public void fillTelemetryData(String prefix, RovyTelemetryData telemetryData) {
		camWheel.fillTelemetryData(prefix + "camWheel: ", telemetryData);
		cam.fillTelemetryData(prefix + "cam: ", telemetryData);
		telemetryData.getEntries().add(prefix + "upStopInputPin: " + upStopInput.getState());
		telemetryData.getEntries().add(prefix + "downStopInputPin: " + downStopInput.getState());
		telemetryData.getEntries().add(prefix + "allowedDirection: " + allowedDirection);
	}
	
	public CamTower() {
		GpioController gpioCtrl = GpioFactory.getInstance();
		
		upStopInput = gpioCtrl.provisionDigitalInputPin(RaspiPin.GPIO_24, PinPullResistance.PULL_DOWN);
		downStopInput = gpioCtrl.provisionDigitalInputPin(RaspiPin.GPIO_25, PinPullResistance.PULL_DOWN);
		cam = new Cam();
		camWheel = new Wheel(0, RaspiPin.GPIO_02);
		updateAllowedDirection();

	}

	public void updateAllowedDirection() {
		if (upStopInput.getState().isHigh())
			allowedDirection = AllowedDirection.DOWN;
		else if (downStopInput.getState().isHigh())
			allowedDirection = AllowedDirection.UP;
		else 
			allowedDirection = AllowedDirection.BOTH;
	}
		
	public void lookForeward() {
		lookAt(70);
	}
	
	public void lookToPowerbank() {
		lookAt(20);
	}

	private void lookAt(int millis) {
		cam.startCapturing();
		moveToDownStop();
//		cam.clear();
		RovyUtility.sleep(200);
		camWheel.start(true, 40);
		RovyUtility.sleep(millis);
		camWheel.stop();
		cam.finishCapturing();
		updateAllowedDirection();
	}
	
	
	public boolean isDownAllowed() {
		return allowedDirection != AllowedDirection.UP;
	}
	
	public boolean isUpAllowed() {
		return allowedDirection != AllowedDirection.DOWN;
	}

	private void moveToDownStop() {
		if (!isDownAllowed())
			return;
		long startTime = System.currentTimeMillis();
		long now = startTime;
		camWheel.start(false, 40);
		while (isDownAllowed() && now - startTime < WHEEL_TIMEOUT) {
			RovyUtility.sleep(10);
			now = System.currentTimeMillis();
			updateAllowedDirection();
		}
		camWheel.stop();
		updateAllowedDirection();
	}
	
	
	public Cam getCam() {
		return cam;
	}
	
	public void camUp() {
		if (!isUpAllowed())
			return;
//		cam.clear();
		cam.startCapturing();
		camWheel.step(true);
		cam.finishCapturing();
		updateAllowedDirection();
	}

	public void camDown() {
		if (!isDownAllowed())
			return;
//		cam.clear();
		cam.startCapturing();
		camWheel.step(false);
		cam.finishCapturing();
		updateAllowedDirection();
	}
	
	private enum AllowedDirection {
		UP, DOWN, BOTH;
	}
	
	private int WHEEL_TIMEOUT = 3000;

}
