package jnitoy;

import java.util.Collection;

import de.gnox.rovy.ocv.ArucoDictionary;
import de.gnox.rovy.ocv.ArucoMarker;
import de.gnox.rovy.ocv.CameraProcessor;

public class JniToy {

	
	public static void main(String[] args) {
		
		CameraProcessor detector = new CameraProcessor(true, 0);
		detector.initAruco(ArucoDictionary.DICT_4X4_250);
		detector.startCapturing();
		
		while (true) {
			Collection<ArucoMarker> markers = detector.detectArucoMarkers();
			for (ArucoMarker marker : markers)
				System.out.println(marker.getCenter() + " " + marker.getTranslationVector());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
	}
}
