package de.gnox.rovy.server;

import java.util.ArrayList;
import java.util.List;

import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyCommandType;
import de.gnox.rovy.api.RovyTelemetryData;

public class Rovy {

	private Car car;
	private CamTower camTower;
	private I2cDisplay display;
	private Audio audio;

	public Rovy() {
		System.out.println("NEW ROVER");
		initRaspIo();
		System.out.println("raspinit fertig");
		display = new I2cDisplay();
		car = new Car();
		camTower = new CamTower();
		audio = new Audio();
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

	public void drive(RovyCommand command) throws NumberFormatException, RovyException {
		String cm = command.getParameter("cm");
		if (cm == null)
			throw new RovyException("cm is empty");
		// car.driveNew( Float.parseFloat(meters), getCam() );
		car.driveNew(Integer.parseInt(cm), getCam(), display);
	}

	public void turn(RovyCommand command) throws NumberFormatException, RovyException {
		String degrees = command.getParameter("degrees");
		if (degrees == null)
			throw new RovyException("degrees is empty");
		car.turnNew(Float.parseFloat(degrees), getCam(), display);
	}

	public void turnFwd(RovyCommand command) throws NumberFormatException, RovyException {
		String degrees = command.getParameter("degrees");
		if (degrees == null)
			throw new RovyException("degrees is empty");
		getCam().startCapturing();
		car.turnFwdBkw(Float.parseFloat(degrees), true, display);
		getCam().finishCapturing();
	}
	

	public void turnBkw(RovyCommand command) throws NumberFormatException, RovyException {
		String degrees = command.getParameter("degrees");
		if (degrees == null)
			throw new RovyException("degrees is empty");
		getCam().startCapturing();
		car.turnFwdBkw(Float.parseFloat(degrees), false, display);
		getCam().finishCapturing();
	}
	
	public void slide(RovyCommand command) throws NumberFormatException, RovyException {
		String degrees = command.getParameter("degrees");
		if (degrees == null)
			throw new RovyException("degrees is empty");
		getCam().startCapturing();
		car.slide(Float.parseFloat(degrees), display);
		getCam().finishCapturing();
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
			case CamDown:
				camTower.camDown();
				// cam.capturePicture();
				break;
			case CamUp:
				camTower.camUp();
				// cam.capturePicture();
				break;
			case CamForeward:
				camTower.lookForeward();
				break;
			case CamToPowerbank:
				camTower.lookToPowerbank();
				break;
			case Drive:
				drive(command);
				// cam.capture();
				break;
			case Turn:
				turn(command);
				// cam.capture();
				break;
			case TurnFwd:
				turnFwd(command);
				// cam.capture();
				break;
			case TurnBkw:
				turnBkw(command);
				// cam.capture();
				break;
			case Slide:
				slide(command);
				// cam.capture();
				break;
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
				camTower.getCam().useLight(true);
				break;
			case LightOff:
				camTower.getCam().useLight(false);
				break;
			case Dance:
				 car.dance(getCam());
				// replayCommandHistory();
				// car.driveStepped(10, getCam());
				break;
			case ClearMediaCache:
				getCam().clearMediaCache();
//				commandHistory = new ArrayList<>();
				break;
			case SetCapturingMode:
				setCapturingMode(command);
				break;
			case MakeNoise:
				audio.makeNoise();
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
		car.fillTelemetryData("car: ", telemetryData);
		camTower.fillTelemetryData("camTower: ", telemetryData);
		return telemetryData;
	}

	public Cam getCam() {
		return camTower.getCam();
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
