package de.gnox.rovy.server;

import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyTelemetryData;

public class Rovy {

	private Config config;

	private Cam cam;

	private Transmitter transmitter = new Transmitter();

	private PwmFan fan;

	private DHT22Simple dht22;

	private I2cDisplay display;

	private int targetTemperature = 25;

	private Button btn1;

	private Button btn2;

	private OctoPrintClient octoPrint;

	private ControlMode ctrlMode = ControlMode.InfoDisplay;

	public Rovy() {
		System.out.println("Welcome at rovylite-server!");
		initRaspIo();
		// display = new I2cDisplay();

		display.switchOn();
		RovyUtility.sleep(2000);
		display.switchOff();
	}

	public void initRaspIo() {

		config = new Config();
		System.out.println("init sensors (dht22) ..");
		dht22 = new DHT22Simple(config.getPinDHT22Data());
		System.out.println("init cam ..");
		cam = new Cam(config);
		System.out.println("init fan ..");
		fan = new PwmFan(config.getPinFanControl(), config.getPinFanPower());
		System.out.println("init display ..");
		display = new I2cDisplay(config.getDisplayI2cBusNr());
		System.out.println("init buttons ..");
		btn1 = new Button(config.getPinButton1()) {
			@Override
			public void onPressed() {
				cam.toggleLight();
			}

			@Override
			public void onLongPressed() {
				if (ctrlMode.getCommand() != null) {
					performCommand(ctrlMode.getCommand());
					ctrlMode = ControlMode.InfoDisplay;
					updateDisplay(blink);
				}
			}
		};

		btn2 = new Button(config.getPinButton2()) {
			@Override
			public void onPressed() {
				int ctrlModeIndex = ctrlMode.ordinal();
				ctrlModeIndex++;
				if (ctrlModeIndex >= ControlMode.values().length)
					ctrlModeIndex = 0;
				ctrlMode = ControlMode.values()[ctrlModeIndex];
				updateDisplay(blink);
			}

		};

		octoPrint = new OctoPrintClient();

		System.out.println("init done");

//		GpioUtil.enableNonPrivilegedAccess();
//		try {
//			Gpio.wiringPiSetup();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public void startUpdating() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					Rovy.instance().updateRare();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		while (true) {
			Rovy.instance().updateOften();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

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
			case AbortJob:
				octoPrint.cancleJob();
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
	}

	private void powerOn() {
		transmitter.send(config.getPowerSwitchSystemCode(), config.getPowerSwitchUnitCode(), "1");
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
		octoPrint.fillTelemetryData("octoprint: ", telemetryData);

		return telemetryData;
	}

	public Cam getCam() {
		return cam;
	}

	private RovyCommand lastCommand = null;

//	private List<RovyCommand> commandHistory = new ArrayList<>();<

	public static Rovy instance() {
		if (roverInstance == null)
			roverInstance = new Rovy();
		return roverInstance;
	}

	// public static void destroy() {
	//// roverInstance.cam.stopThumpCapturing();
	//// roverInstance = null;
	// }

	private void updateOften() {
		btn1.update();
		btn2.update();
	}

	private boolean blink = false;

	private void updateRare() {
		octoPrint.update();
		try {
			dht22.refreshData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateDisplay(blink);
		updateCam();
		updateFan();
		blink = !blink;
		Runtime.getRuntime().gc();
	}

	private void updateFan() {
		Float temp = dht22.getTemperature();
		if (temp == null) { // handling dht22 read error
			fan.setSpeed(100);
		} else {
			fan.update(temp, (double) targetTemperature);
		}
	}

	private void updateCam() {
		cam.update();
	}

	private synchronized void updateDisplay(boolean blink) {
		if (ctrlMode == ControlMode.DisplayOff) {
			if (display.isEnabled())
				display.switchOff();
			return;
		}

		if (!display.isEnabled())
			display.switchOn();

		display.getCurrentBuffer().clear();
		try {
			char tr = ':';// blink ? ':' : ' ';
			Float temperature = dht22.getTemperature();
			
			if (ctrlMode.getCommand() == null) {
			
				if (temperature != null)
					display.getCurrentBuffer().drawString("T " + tr + StringUtil.fillBefore(4, ' ', "" + temperature) + "°"
							+ "/" + StringUtil.fillBefore(3, ' ', targetTemperature + "") + "°", 0, 0);
			} else {
				display.getCurrentBuffer().drawString("> " + ctrlMode.getCaption(), 0, 0);
			}
			
			if (octoPrint.getPrinterBedTempActual() != null)
				display.getCurrentBuffer()
						.drawString("BT" + tr
								+ StringUtil.fillBefore(5, ' ',
										StringUtil.valueWithUnitToString(octoPrint.getPrinterBedTempActual(), "°"))
								+ "/"
								+ StringUtil.fillBefore(4, ' ',
										StringUtil.valueWithUnitToString(octoPrint.getPrinterBedTempTarget(), "°")),
								0, 16);
			if (octoPrint.getPrinterHotendTempActual() != null)
				display.getCurrentBuffer().drawString(
						"HT" + tr
								+ StringUtil.fillBefore(5, ' ',
										StringUtil.valueWithUnitToString(octoPrint.getPrinterHotendTempActual(), "°"))
								+ "/"
								+ StringUtil.fillBefore(4, ' ',
										StringUtil.valueWithUnitToString(octoPrint.getPrinterHotendTempTarget(), "°")),
						0, 32);
			if (octoPrint.getProgressCompletion() != null)
				display.getCurrentBuffer().drawString(
						"P " + tr
								+ StringUtil.fillBefore(5, ' ',
										StringUtil.valueWithUnitToString(octoPrint.getProgressCompletion(), "%")),
						0, 48);
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

	private static Rovy roverInstance = null;

}
