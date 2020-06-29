package de.gnox.rovy.server;

import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyTelemetryData;

public class Rovy {

	private Cam cam;
	private I2cDisplay display;

	public Rovy() {
		System.out.println("NEW ROVER");
		initRaspIo();
		System.out.println("raspinit fertig");
		display = new I2cDisplay();
		cam = new Cam();
		// display = new I2cDisplay();
	}

	public void initRaspIo() {
		// GpioUtil.enableNonPrivilegedAccess();
		// try {
		// Gpio.wiringPiSetup();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void setCapturingMode(RovyCommand command) throws NumberFormatException, RovyException {
		String mode = command.getParameter("mode");
		if (mode == null)
			throw new RovyException("mode is empty");
		getCam().setCapturingMode(CamCapturingMode.valueOf(mode));
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
		display.touch();
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
				cam.useLight(true);
				break;
			case LightOff:
				cam.useLight(false);
				break;
			case ClearMediaCache:
				getCam().clearMediaCache();
//				commandHistory = new ArrayList<>();
				break;
			case SetCapturingMode:
				setCapturingMode(command);
				break;
			case EmergencyOff:
				// TODO
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
		cam.fillTelemetryData("camTower: ", telemetryData);
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
