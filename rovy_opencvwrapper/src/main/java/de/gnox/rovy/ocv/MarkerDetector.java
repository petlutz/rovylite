package de.gnox.rovy.ocv;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MarkerDetector {

	static {
		System.loadLibrary("rovyocv");
	}
	
	public void init(boolean debug, int cam, Dictionary markerDict) {
		nInit(debug, cam, markerDict.getId());
	}
	
	public Collection<Marker> detectMarkers() {

		int[] markerData = nDetectMarkers();
		
		if (markerData.length == 0)
			return Collections.emptyList();
		
		
		Map<Integer, Marker> resultMap = new HashMap<>();
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
			
			Marker marker = resultMap.get(markerIdx);
			if (marker == null) {
				marker = new Marker(markerValue);
				resultMap.put(markerIdx, marker);
			}
			
			marker.getPoints().add(new Point(markerPointX, markerPointY));
			
		}
		
		return resultMap.values();
	}
	
	public void releaseCamera() {
		nReleaseCamera();
	}
	
	private native int nInit(boolean debug, int cam, int markerDict);

	private native int[] nDetectMarkers();

	private native int nReleaseCamera();
	
}
