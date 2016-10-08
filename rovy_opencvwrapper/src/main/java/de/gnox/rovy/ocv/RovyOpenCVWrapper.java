package de.gnox.rovy.ocv;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RovyOpenCVWrapper {

	static {
		System.loadLibrary("rovyocv");
	}

	public Collection<ArucoMarker> arucoDetectMarkers(ArucoDictionary markerDict) {
		
		int markerData[] = nArucoDetectMarkers(markerDict.getId());
		
		if (markerData.length == 0)
			return Collections.emptyList();
		
		
		Map<Integer, ArucoMarker> resultMap = new HashMap<>();
		int i = 0;
		while (true) {
			
			if (i + 4 > markerData.length)
				break;
			
			int markerIdx = markerData[i++];
			int markerValue = markerData[i++];
			int markerPointX = markerData[i++];
			int markerPointY = markerData[i++];
			
			if (markerIdx == -1 || markerValue == -1 || markerPointX == -1 || markerPointY == -1)
				break;
			
			ArucoMarker marker = resultMap.get(markerIdx);
			if (marker == null) {
				marker = new ArucoMarker(markerValue);
				resultMap.put(markerIdx, marker);
			}
			
			marker.getPoints().add(new Point(markerPointX, markerPointY));
			
		}
		
		return resultMap.values();
	}
	
	private native int[] nArucoDetectMarkers(int markerDict);

	public void arucoDrawDetectedMarkers() {
		nArucoDrawDetectedMarkers();
	}

	private native int nArucoDrawDetectedMarkers();

	public void openVideoCapture(int cam, boolean debug) {
		nOpenVideoCapture(cam, debug);
	}
	
	private native int nOpenVideoCapture(int cam, boolean debug);

	public void grab() {
		nGrab();
	}
	
	private native int nGrab();

	public void imshow() {
		nImshow();
	}

	private native int nImshow();

	public void releaseVideoCapture() {
		nReleaseVideoCapture();
	}
	
	private native int nReleaseVideoCapture();

	
}
