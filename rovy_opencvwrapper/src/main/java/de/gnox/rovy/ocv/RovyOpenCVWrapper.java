package de.gnox.rovy.ocv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Mat;

public class RovyOpenCVWrapper {

	static {
		System.loadLibrary("rovyocv");
	}

	public Collection<ArucoMarker> arucoDetectMarkers(Mat frame) {
		
		int detectedMarkers = nArucoDetectMarkers(frame.nativeObj);
		
		if (detectedMarkers == 0)
			return Collections.emptyList();
		
		List<ArucoMarker> resultList = new ArrayList<>(detectedMarkers);
	
		for (int markerIdx = 0; markerIdx < detectedMarkers; markerIdx++) {
			ArucoMarker marker = new ArucoMarker(nArucoGetMarkerId(markerIdx));
			int[] markerCornersData = nArucoGetMarkerCorners(markerIdx);
			if (markerCornersData.length % 2 != 0)
				throw new RuntimeException("error in marker corners data");
			for (int i = 0; i < markerCornersData.length; i += 2)
				marker.getCorners().add(new Point2i(markerCornersData[i], markerCornersData[i + 1]));
			
			double[] rMat = nArucoGetMarkerRotationMatrix(markerIdx);
			double[] tVec = nArucoGetMarkerTranslationVector(markerIdx);

			if (rMat != null) {
				Matrix rMatrix = Matrix.getIdentityMatrix();
				int row = 0; 
				int col = 0;
				for (int i = 0; i < 9; i++) {
					rMatrix.getValues()[row][col] = rMat[i];
					col++;
					if (col >= 3) {
						col = 0;
						row ++;
					}
				}
				marker.setRotationMatrix(rMatrix);
			} 
			
			if (tVec != null) {
				marker.setTranslationVector(new Vector(tVec[0], tVec[1], tVec[2]));			
			}
				
			resultList.add(marker);
		}
		
		
		
		return resultList;
	}
	
	public void arucoInit(ArucoDictionary dict) {
		nArucoInit(dict.getId());
	}
	
	public void arucoInitWithPoseEstimation(ArucoDictionary dict, float markerLength) {
		nArucoInitWithPoseEstimation(dict.getId(), markerLength);
	}
 	
	public void arucoDrawDetectedMarkers(Mat frame) {
		nArucoDrawDetectedMarkers(frame.nativeObj);
	}
	
	private native int nArucoInit(int markerDict);
	
	private native int nArucoInitWithPoseEstimation(int markerDict, float markerLength);
	
	private native int nArucoDetectMarkers(long frameMatAddr);

	private native int[] nArucoGetMarkerCorners(int markerIndex);
	
	private native double[] nArucoGetMarkerRotationMatrix(int markerIndex);
	
	private native double[] nArucoGetMarkerTranslationVector(int markerIndex);
	
	private native int nArucoGetMarkerId(int markerIndex);

	private native int nArucoDrawDetectedMarkers(long frameMatAddr);

	private native int nReleaseVideoCapture();

	
}
