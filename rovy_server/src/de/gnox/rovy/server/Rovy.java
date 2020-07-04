package de.gnox.rovy.server;

import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyTelemetryData;

public class Rovy {

	private Config config;
	
	private Cam cam;

	private Transmitter transmitter = new Transmitter();

	private DHT22 dht22;

//	private I2cDisplay display;

	public Rovy() {
		System.out.println("NEW ROVER");
		initRaspIo();
		System.out.println("raspinit fertig");
//		display = new I2cDisplay();
		
		// display = new I2cDisplay();
	}

	public void initRaspIo() {
		
		config = new Config();
		dht22 = new DHT22(config.getPinDHT22Data());
		cam = new Cam(config);
		
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
			case LightOn:
				cam.switchLight(true);
				break;
			case LightOff:
				cam.switchLight(false);
				break;
			case ClearMediaCache:
				getCam().clearMediaCache();
//				commandHistory = new ArrayList<>();
				break;
			case PowerOn:
//				send 00011 3 1
				cam.switchLight(true);
				transmitter.send(config.getPowerSwitchSystemCode(), config.getPowerSwitchUnitCode(), "1");
				break;
			case PowerOff:
//				send 00011 3 1
				transmitter.send(config.getPowerSwitchSystemCode(), config.getPowerSwitchUnitCode(), "0");
				cam.switchLight(false);
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

//	private void replayCommandHistory() {
//		for (RovyCommand cmd : commandHistory) {
//			performCommandInternal(cmd);
//			RovyUtility.sleep(300);
//		}
//	}

	public RovyTelemetryData getTelemetryData() {
		RovyTelemetryData telemetryData = new RovyTelemetryData();
		telemetryData.getEntries().add("lastCommand: " + lastCommand);

		try {
			dht22.refreshData();
			telemetryData.getEntries().add("temp: " + dht22.getTemperature() + "*C");
			telemetryData.getEntries().add("humidity: " + dht22.getHumidity() + "%");
		} catch (Exception e) {
			e.printStackTrace();
			telemetryData.getEntries().add("temp: ERROR: ");
			telemetryData.getEntries().add("humidity: ERROR");
		}

		cam.fillTelemetryData("cam: ", telemetryData);
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

}
