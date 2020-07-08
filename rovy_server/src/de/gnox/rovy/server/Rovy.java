package de.gnox.rovy.server;

import java.util.Date;

import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyTelemetryData;

public class Rovy  {

	private Config config;

	private Cam cam;

	private Transmitter transmitter = new Transmitter();

	private PwmFan fan;

	private DHT22 dht22;

	private I2cDisplay display;

	private int targetTemperature = 25;

	private Button btn1;

	private Button btn2;

	public Rovy() {
		System.out.println("NEW ROVER");
		initRaspIo();
		System.out.println("raspinit fertig");

		new Thread(new SlowUpdater()).start();
		new Thread(new FastUpdater()).start();
		// display = new I2cDisplay();

		display.switchOn();
		RovyUtility.sleep(5000);
		display.switchOff();
	}

	public void initRaspIo() {

		config = new Config();
		dht22 = new DHT22(config.getPinDHT22Data());
		cam = new Cam(config);
		fan = new PwmFan(config.getPinFanControl());
		display = new I2cDisplay();

		btn1 = new Button(config.getPinButton1()) {
			@Override
			public void onPressed() {
				cam.toggleLight();
			}

			@Override
			public void onLongPressed() {
				powerOn();
			}
		};

		btn2 = new Button(config.getPinButton2()) {
			@Override
			public void onPressed() {
				display.toggleOnOff();
			}

			@Override
			public void onLongPressed() {
				powerOff();
			}
		};

//		GpioUtil.enableNonPrivilegedAccess();
//		try {
//			Gpio.wiringPiSetup();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	private void captureVideo(RovyCommand command) throws RovyException {
		String seconds = command.getParameter("seconds");
		if (seconds == null)
			throw new RovyException("degrees is empty");
		int secondsInt = Integer.parseInt(seconds);
		if (secondsInt < 0 || secondsInt > 60 * 5)
			throw new RovyException("seconds < 0 || seconds > 60*5");
		// getCam().clear();
		getCam().captureVideo(secondsInt * 1000);
	}

	public boolean performCommand(RovyCommand command) {
		boolean result = performCommandInternal(command);
//		if (result)
//			commandHistory.add(command);
		return result;
	}

	private boolean performCommandInternal(RovyCommand command) {
//		display.touch();
		getCam().deselectMedia();
		boolean result = true;
		try {
			switch (command.getType()) {
			case CapturePicture:
				getCam().capturePicture(false);
				break;
			case CaptureBigPicture:
				getCam().captureBigPicture();
				break;
			case CaptureVideo:
				captureVideo(command);
				break;
			case SetTargetTemperatur:
				setTargetTemperatur(command);
				break;
			case LightOn:
				lightOn();
				break;
			case LightOff:
				lightOff();
				break;
			case ClearMediaCache:
				getCam().clearMediaCache();
//				commandHistory = new ArrayList<>();
				break;
			case PowerOn:
//				send 00011 3 1
				powerOn();
				break;
			case PowerOff:
//				send 00011 3 1
				powerOff();
				break;
			case DisplayOn:
				display.switchOn();
				break;
			case DisplayOff:
				display.switchOff();
				break;
			default:
				throw new RovyException("unknown command");
			}
			command.setPerformed(null);
		} catch (Exception e) {
			e.printStackTrace();
			command.setPerformed(e);
			result = false;
		}
		lastCommand = command;
		return result;
	}

	private void lightOff() {
		cam.switchLight(false);
	}

	private void lightOn() {
		cam.switchLight(true);
	}

	
	private void powerOff() {
		transmitter.send(config.getPowerSwitchSystemCode(), config.getPowerSwitchUnitCode(), "0");
		lightOff();
		display.switchOff();
	}

	private void powerOn() {
		lightOn();
		transmitter.send(config.getPowerSwitchSystemCode(), config.getPowerSwitchUnitCode(), "1");
		display.switchOn();
	}

	private void setTargetTemperatur(RovyCommand command) {
		String temp = command.getParameter("temp");
		try {
			int targetTemp = Integer.parseInt(temp);
			if (targetTemp < 0)
				targetTemp = 0;
			if (targetTemp > 99)
				targetTemp = 99;
			this.targetTemperature = targetTemp;
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
	}

//	private void replayCommandHistory() {
//		for (RovyCommand cmd : commandHistory) {
//			performCommandInternal(cmd);
//			RovyUtility.sleep(300);
//		}
//	}

	public RovyTelemetryData getTelemetryData() {
		RovyTelemetryData telemetryData = new RovyTelemetryData();

		telemetryData.getEntries().add("displaystatus: " + (display.isEnabled() ? "on" : "off"));
		telemetryData.getEntries().add("lastCommand: " + lastCommand);

		telemetryData.getEntries().add("temp: " + dht22.getTemperature() + "°");
		telemetryData.getEntries().add("targettemp: " + targetTemperature + "°");
		telemetryData.getEntries().add("humidity: " + dht22.getHumidity() + "%");

		cam.fillTelemetryData("cam: ", telemetryData);
		fan.fillTelemetryData("fan: ", telemetryData);
		return telemetryData;
	}

	public Cam getCam() {
		return cam;
	}

	private RovyCommand lastCommand = null;

//	private List<RovyCommand> commandHistory = new ArrayList<>();

	public static Rovy instance() {
		if (roverInstance == null)
			roverInstance = new Rovy();
		return roverInstance;
	}

	// public static void destroy() {
	//// roverInstance.cam.stopThumpCapturing();
	//// roverInstance = null;
	// }

	private static Rovy roverInstance = null;

	private class SlowUpdater implements Runnable {

		public void run() {
			boolean blink = false;
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				updateDisplay(blink);
				updateCam();
				updateFan();

				blink = !blink;

			}
		}

		private void updateFan() {
			fan.update(dht22.getTemperature(), (float) targetTemperature);
//			fan.setSpeed(targetTemperature);
		}

		private void updateCam() {
			cam.update();
		}

		private void updateDisplay(boolean blink) {
			if (display.isEnabled()) {
				display.getCurrentBuffer().clear();
				try {
					dht22.refreshData();
					char tr = ':';// blink ? ':' : ' ';

					display.getCurrentBuffer().drawString("T " + tr + " " + dht22.getTemperature() + "°", 0, 0);
					display.getCurrentBuffer().drawString("T!" + tr + " " + targetTemperature + "°", 0, 16);
					display.getCurrentBuffer().drawString("H " + tr + " " + dht22.getHumidity() + "%", 0, 32);
					display.getCurrentBuffer().drawString("F " + tr + " " + fan.getSpeed() + "%", 0, 48);
				} catch (Exception e) {
					e.printStackTrace();
					display.getCurrentBuffer().drawString("%99%", 0, 0);
				}
				if (blink) {
					display.getCurrentBuffer().setPixel(126, 0, true);
					display.getCurrentBuffer().setPixel(126, 1, true);
					display.getCurrentBuffer().setPixel(127, 0, true);
					display.getCurrentBuffer().setPixel(127, 1, true);
				}
				display.update();
			}
		}
	}

	private class FastUpdater implements Runnable {
		
		public void run() {
			while (true) { 
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				btn1.update();
				btn2.update();
			}
		}
		
	}

}
