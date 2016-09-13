package de.gnox.rovy.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Audio implements Runnable {
	
	private static final int AMP_TIMEOUT_MILLIS = 60000 * 3;

	private String SOUNDS_PATH = "sounds/";

	private GpioPinDigitalOutput ampOnOffOut;
	
	private List<File> soundFiles;

	public Audio() {
		GpioController gpioCtrl = GpioFactory.getInstance();
		ampOnOffOut = gpioCtrl.provisionDigitalOutputPin(RaspiPin.GPIO_04, PinState.LOW);
		

		soundFiles = new ArrayList<>();
		
		File path = new File(SOUNDS_PATH);
		for (File f : path.listFiles()) {
			if (f.isFile() && f.getName().toLowerCase().endsWith(".wav")) {
				System.out.println("use sound file "  + f.getAbsolutePath() );
				soundFiles.add(f);		
			}
		}	
		
		lastTouchTime = System.currentTimeMillis();
		new Thread(this).start();
	}

	public void makeNoise() {
		touch(); 
		int rnd = (int)(Math.random() * (double)soundFiles.size());	
		try {
			String[] cmd = { "aplay", soundFiles.get(rnd).getAbsolutePath() };
			System.out.println(cmd);
			Runtime.getRuntime().exec(cmd).waitFor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	private long lastTouchTime;

	private void touch() {
		lastTouchTime = System.currentTimeMillis();
		if (ampOnOffOut.isLow()) {
			ampOnOffOut.high();
			RovyUtility.sleep(500);
		}
	}

	@Override
	public void run() {
		while (true) {
			if (ampOnOffOut.isHigh() && System.currentTimeMillis() - lastTouchTime > AMP_TIMEOUT_MILLIS)
				ampOnOffOut.low();
			RovyUtility.sleep(1000);
		}
	}

}
