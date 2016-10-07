package de.gnox.rovy.ocv;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MarkerDetector {

	private boolean debug;

	private int cam;

	AtomicBoolean stopNow = new AtomicBoolean(false);

	AtomicBoolean capturing = new AtomicBoolean(false);

//
//	AtomicBoolean detectNow = new AtomicBoolean(false);
//
//	AtomicReference<Collection<ArucoMarker>> detectedMarkers = new AtomicReference<Collection<ArucoMarker>>(null);

	private RovyOpenCVWrapper ocv;

	private ArucoDictionary markerDict;

	private CapturingRunnable capturingRunnable;
	
	public MarkerDetector(boolean debug, int cam, ArucoDictionary markerDict) {
		ocv = new RovyOpenCVWrapper();
		this.cam = cam;
		this.debug = debug;
		this.markerDict = markerDict;
	}

	public void startCapturing() {
		if (capturing.get())
			throw new IllegalStateException();
		capturingRunnable = new CapturingRunnable();
		capturing.set(true);
		stopNow.set(false);
		new Thread(capturingRunnable).start();
	}

	public void stopCapturing() {
		capturingRunnable = null;
		stopNow.set(true);
	}

	public Collection<ArucoMarker> detectMarkers() {
		
		if (capturingRunnable == null) 
			return Collections.emptyList();
		
		FutureTask<Collection<ArucoMarker>> futureTask = new FutureTask<>(() -> {
			Collection<ArucoMarker> markers = ocv.arucoDetectMarkers(markerDict);
			if (debug) 
				ocv.arucoDrawDetectedMarkers();
			return markers;
		});
		
		capturingRunnable.setNextFrameFutureTask(futureTask);
		
		try {
			return futureTask.get(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private class CapturingRunnable implements Runnable {

		private AtomicReference<FutureTask<?>> nextFrameFutureTask = new AtomicReference<>(null);
		
		public void setNextFrameFutureTask(FutureTask<?> nextFrameFutureTask) {
			this.nextFrameFutureTask.set( nextFrameFutureTask );
		}
		
		
		@Override
		public void run() {

			ocv.openVideoCapture(cam, debug);

			while (!stopNow.get()) {
				
				long startTime = System.currentTimeMillis();

				ocv.grab();
				FutureTask<?> task = nextFrameFutureTask.get();
				if (task != null)
					task.run();

				if (debug) 
					ocv.imshow();
				
					
				try {
					
					long duration = System.currentTimeMillis() - startTime;
					long sleepTime = 20 - duration;
					if (sleepTime < 1)
						sleepTime = 1;
					TimeUnit.MILLISECONDS.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			ocv.releaseVideoCapture();

			capturing.set(false);
		}

	};

}
