package de.gnox.rovy.ocv;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CameraProcessor {

	private boolean debug;

	private int cam;

	AtomicBoolean stopCapturingNow = new AtomicBoolean(false);

	AtomicBoolean capturing = new AtomicBoolean(false);

	private RovyOpenCVWrapper ocv;

	private CapturingRunnable capturingRunnable;
	
	public CameraProcessor(boolean debug, int cam) {
		ocv = new RovyOpenCVWrapper();
		this.cam = cam;
		this.debug = debug;
	}
	
	public RovyOpenCVWrapper getOpenCVWrapper() {
		return ocv;
	}

	public void startCapturing() {
		if (capturing.get())
			throw new IllegalStateException();
		capturingRunnable = new CapturingRunnable();
		capturing.set(true);
		stopCapturingNow.set(false);
		new Thread(capturingRunnable).start();
	}

	public void stopCapturing() {
		capturingRunnable = null;
		stopCapturingNow.set(true);
	}
	
	public <T> T processUpcommingFrame(Callable<T> callable) {
		if (capturingRunnable == null) 
			return null;
		
		FutureTask<T> futureTask = new FutureTask<>(callable);
		
		capturingRunnable.setNextFrameFutureTask(futureTask);
		
		try {
			return futureTask.get(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			return null;
		}
	}

	public Collection<ArucoMarker> detectArcucoMarkers(ArucoDictionary markerDict) {
		
		Collection<ArucoMarker> result = processUpcommingFrame(() -> {
			Collection<ArucoMarker> markers = getOpenCVWrapper().arucoDetectMarkers(markerDict);
			if (debug) 
				getOpenCVWrapper().arucoDrawDetectedMarkers();
			return markers;
		});
		
		if (result == null)
			return Collections.emptyList();
		return result;
		
	}

	private class CapturingRunnable implements Runnable {

		private AtomicReference<FutureTask<?>> nextFrameFutureTask = new AtomicReference<>(null);
		
		public void setNextFrameFutureTask(FutureTask<?> nextFrameFutureTask) {
			this.nextFrameFutureTask.set( nextFrameFutureTask );
		}
		
		
		@Override
		public void run() {

			ocv.openVideoCapture(cam, debug);

			while (!stopCapturingNow.get()) {
				
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
					e.printStackTrace();
				}

			}

			ocv.releaseVideoCapture();

			capturing.set(false);
		}

	};

}
