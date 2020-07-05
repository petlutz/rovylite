package de.gnox.rovy.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import de.gnox.rovy.api.RovyTelemetryData;

public class Cam {

	private static final int VIDEO_BORDER_MILLIS = 500;

	private static final int VIDEO_TIMEOUT_MILLIS = 60000 * 3; // 3 Min

	private boolean lightEnabled = false;

	private AtomicLong timestampMediaCreation = null;

	public Cam(Config config) {
		lightOutput = GpioFactory.getInstance().provisionDigitalOutputPin(config.getPinLight(), "light", PinState.LOW);
		switchLight(false);
	}

//	public void deletePicture() {
//		if (picture != null)
//			picture.delete();
//		picture = null;
//	}
//	
//	public void deleteVideo() {
//		if (video != null)
//			video.delete();
//		video = null;
//	}

//	public void clear() {
//		deletePicture();
//		deleteVideo();
//	}

//	public synchronized void removeOldPictures() {
////		try {
////			Runtime.getRuntime().exec("rm " + THUMPS_PATH + "thump*.jpg");
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
//	}
//	
//	public synchronized void removeOldVideos() {
//		if (getLastVideo())
////		try {
////			Runtime.getRuntime().exec("rm " + THUMPS_PATH + "thump*.h264");
////			Runtime.getRuntime().exec("rm " + THUMPS_PATH + "thump*.mp4");
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
//	}

	public void captureVideo(int millis) {
		deselectMedia();
		captureVideoAsync(VIDEO_TIMEOUT_MILLIS);
		RovyUtility.sleep(millis);
		finishVideo();
		setTimestampMediaCreation();
	}

	private void captureVideoAsync(int millis) {
		captureVideoAsync(millis, VIDEO_BORDER_MILLIS);
	}

	private void captureVideoAsync(int millis, int borderMillis) {
//		waitForVideo();
//		deleteVideo();
//		removeOldVideos();

		int millisWithBorder = millis + borderMillis;

		String cmd = "raspivid -o " + MEDIA_PATH + "video.h264 -fps 30 -w 640 -h 480 -t " + millisWithBorder;
		try {
			System.out.println(cmd);
			switchLightOn();
			videoCapturingProcess = Runtime.getRuntime().exec(cmd);
			RovyUtility.sleep(borderMillis);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void finishVideo() {
		if (videoCapturingProcess == null)
			return;

		RovyUtility.sleep(VIDEO_BORDER_MILLIS);
		videoCapturingProcess.destroy();
		System.out.println("video stopped");

		switchLight();

		String vidNameLocal = newFilename() + ".mp4";
		String vidPath = MEDIA_PATH + vidNameLocal;

		String cmd = "MP4Box -fps 30 -add " + MEDIA_PATH + "video.h264 " + vidPath;
		try {
			System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			video = new File(vidPath);
			picture = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	private void waitForVideo()  {
//		if (videoCapturingProcess == null)
//			return;
//		
//		try {
//			videoCapturingProcess.waitFor();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		switchLightOff();
//		
//		String vidNameLocal = newFilename() + ".mp4";
//		String vidPath = MEDIA_PATH + vidNameLocal;
//		
//		String cmd = "MP4Box -fps 30 -add " + MEDIA_PATH + "video.h264 " + vidPath;
//		try {
//			System.out.println(cmd);
//			Process p = Runtime.getRuntime().exec(cmd);
//			p.waitFor();
//			video = new File(vidPath);
//			picture = null;
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//	}

	private String newFilename() {
		return "rovy-" + DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS").format(LocalDateTime.now());
	}

	public void captureBigPicture() {
		deselectMedia();
		String picNameLocal = newFilename() + ".jpg";
		String picPath = MEDIA_PATH + picNameLocal;
		String cmd = "raspistill -o " + picPath;
		switchLightOn();
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			picture = new File(picPath);
			video = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switchLight();
		setTimestampMediaCreation();
	}

	public void capturePicture(boolean thump) {
		deselectMedia();
		String picNameLocal = newFilename() + ".jpg";
		String picPath = MEDIA_PATH + picNameLocal;
		String cmd = "raspistill -o " + picPath + " -w 640 -h 480 -q 50";
		if (thump)
			cmd += " -t 100";

		switchLightOn();
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			picture = new File(picPath);
			video = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switchLight();
		setTimestampMediaCreation();
	}

	private void setTimestampMediaCreation() {
		timestampMediaCreation = new AtomicLong(new Date().getTime());
	}

	public Date getTimestampMediaCreation() {
		return timestampMediaCreation != null ? new Date(timestampMediaCreation.get()) : null;
	}

	public File getPicture() {
		return picture;
	}

	public File getVideo() {
		return video;
	}

	public void switchLight(boolean enabled) {
		this.lightEnabled = enabled;
		switchLight();
	}

	private void switchLight() {
		if (lightEnabled)
			switchLightOn();
		else
			switchLightOff();
	}

	private void switchLightOn() {
		lightOutput.high();
	}

	private void switchLightOff() {
		lightOutput.low();
	}

	private String callCmdAndGetOutput(String cmd) {

		try {
			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = null;
			String result = null;
			while ((line = reader.readLine()) != null) {
				if (line != null)
					result = line.trim();
			}
			;

			p.waitFor();

			reader.close();

			return result;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return "error";

	}

	public void clearMediaCache() {

		File dir = new File(MEDIA_PATH);

		String[] files = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("rovy") && (name.endsWith(".jpg") || name.endsWith(".mp4"));
			}
		});

		for (String file : files) {
			File f = new File(MEDIA_PATH + file);
			System.out.println("delete " + f.getName());
			f.delete();
		}

	}

	public void deselectMedia() {
		video = null;
		picture = null;
		timestampMediaCreation = null;
	}

	public void fillTelemetryData(String prefix, RovyTelemetryData telemetryData) {

		String cmdMediaPathSize = "du -h -s ./" + MEDIA_PATH;
		String cmdDf = "df --output=avail -h ./" + MEDIA_PATH;

		String mediaPathSize = callCmdAndGetOutput(cmdMediaPathSize);
		String df = callCmdAndGetOutput(cmdDf);

		telemetryData.getEntries().add(prefix + "mediaPathSize: " + mediaPathSize);
		telemetryData.getEntries().add(prefix + "df: " + df);
		telemetryData.getEntries().add(prefix + "lightEnabled: " + lightEnabled);
		telemetryData.getEntries().add(prefix + "lightPinState: " + lightOutput.getState());
		telemetryData.getEntries().add(prefix + "picture: " + picture);
		telemetryData.getEntries().add(prefix + "video: " + video);
		Date mediaCreation = getTimestampMediaCreation();
		telemetryData.getEntries().add(prefix + "mediacreation: " + (mediaCreation != null ? DateFormat.getInstance().format(mediaCreation) : "null"));
		telemetryData.getEntries().add(prefix + "videoCapturingProcessAlive: "
				+ (videoCapturingProcess != null ? videoCapturingProcess.isAlive() : "no process"));
	}

//	public synchronized void takeThump()  {
//		String localThumpName = "thump_" + new Date().getTime() + ".jpg";
//		String thumpName = THUMPS_PATH + localThumpName;
//		String cmd = "raspistill -o " + thumpName + " -w 640 -h 480 -q 50";
//		try {
////			System.out.println(cmd);
//			Process p = Runtime.getRuntime().exec(cmd);
//			p.waitFor();
//			thumpsQueue.add(thumpName);
//			lastThumpFile = localThumpName;
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public synchronized void removeOldThumps(int imagesToKeep) {
////		System.out.println(thumpsQueue.size());
//		while (thumpsQueue.size() > imagesToKeep) {
//			String thumpName = thumpsQueue.getFirst();
//			new File(thumpName).delete();
//			thumpsQueue.remove(thumpName);
//		}
//	}
//	
//	public synchronized void removeThumps() {
//		try {
//			Runtime.getRuntime().exec("rm " + THUMPS_PATH + "thump*.jpg");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public synchronized void startThumpCapturing() {
//		if (thumpCapturing)
//			throw new IllegalStateException();
//		
//		Runnable r = () -> {
//			while (thumpCapturing) {
//				takeThump();
//				removeOldThumps(THUMPS_QUEUE_SIZE);
//				RoverUtility.sleep(500);
//			}
//			removeOldThumps(0);
//		};
//		
//		removeThumps();
//		thumpCapturing = true;
//		new Thread(r).start();
//	}
//	
//	public synchronized void stopThumpCapturing() {
//		thumpCapturing = false;
//	}
//	
//	public synchronized String getLastThumpFile() {
//		return lastThumpFile;
//	}
//	
//	boolean thumpCapturing = false;
//	
//	private LinkedList<String> thumpsQueue = new LinkedList<>();
//	
//	private final int THUMPS_QUEUE_SIZE = 20;
//	
//	private String lastThumpFile;

	private GpioPinDigitalOutput lightOutput;

	private File picture;

	private Process videoCapturingProcess;

	private File video;

	private String MEDIA_PATH = "media/";
}
