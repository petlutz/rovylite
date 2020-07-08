package de.gnox.rovy.server;

import java.util.Date;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;

public class Button {// implements GpioPinListenerDigital {

	private Date timestampPressed = null;

	GpioPinDigitalInput button = null;

	public Button(Pin pin) {

		button = GpioFactory.getInstance().provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);
		button.setShutdownOptions(true);
//		button.addListener(this);

	}

	public boolean isPressed() {
		return timestampPressed != null;
	}

//	@Override
//	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
//		System.out.println("Button: " + event.getEdge() + " " + event.getState() + " " + event.getEventType());
//		if (!isPressed()) {
//			if (event.getEdge() == PinEdge.FALLING) {
//				timestampPressed = new Date();
//			}
//		} else {
//			if (event.getEdge() == PinEdge.RISING) {
//				onPressed();
//				timestampPressed = null;
//			}
//		}
//	}

	public void onPressed() {

	}

	public void onLongPressed() {

	}

	public void update() {
//		System.out.println("BottonState: " + button.getState());
		if (isPressed()) {
			if (button.getState() == PinState.HIGH) {
				onPressed();
				timestampPressed = null;
			} else {
				long timeLeft = new Date().getTime() - timestampPressed.getTime();
				if (timeLeft > 5000) {
					onLongPressed();
					timestampPressed = null;
				}
			}

		} else {
			if (button.getState() == PinState.LOW) {
				timestampPressed = new Date();
			}
		}
	}

}
