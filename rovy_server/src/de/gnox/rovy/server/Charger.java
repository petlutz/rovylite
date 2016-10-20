package de.gnox.rovy.server;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.gnox.rovy.ocv.ArucoDictionary;
import de.gnox.rovy.ocv.ArucoMarker;
import de.gnox.rovy.ocv.CameraProcessor;
import de.gnox.rovy.ocv.Matrix;
import de.gnox.rovy.ocv.Vector;

public class Charger {
	
	private static final String CALIBDATA_FILENAME = "chargercalib.xml";

	private static final double APPROACH_DISTANCE = 50;

	private static final int MARKER_ID = 42;
	
	private Optional<ArucoMarker> marker = null;

	private Vector markerCorrVector = new Vector(0, 0, 0);
	
	
	public Charger() {
		File file = new File(CALIBDATA_FILENAME);
		if (file.exists())
			loadCalibData(file);
	}

	private void loadCalibData(File file) {
		try {

			JAXBContext jaxbContext = JAXBContext.newInstance(ChargerCalibData.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ChargerCalibData calibData = (ChargerCalibData) jaxbUnmarshaller.unmarshal(file);
			
			markerCorrVector = new Vector(calibData.getChargerMarkerCorrX(), 0, calibData.getChargerMarkerCorrZ());
			System.out.println("charger marker corr vector: " + markerCorrVector);
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public boolean detect(CameraProcessor cp) {
		
		Collection<ArucoMarker> detectedMarkers = cp.detectArucoMarkers();
		marker = detectedMarkers.stream().filter(m -> m.getId() == MARKER_ID).findAny();
		return marker.isPresent();
		
	}
	
	public Vector getChargingPosition() {
		assert (marker.isPresent());
		
		Vector pos = marker.get().getTransformationMatrix().mult(markerCorrVector);
		return new Vector(pos.getX(), 0, pos.getZ());
	}
	
	public Vector getApproachPosition() {
		assert (marker.isPresent());
	
		Matrix markerMatrix = marker.get().getTransformationMatrix();
		Vector vector3d = markerMatrix.mult(new Vector(markerCorrVector.getX(), markerCorrVector.getY(), markerCorrVector.getZ() + APPROACH_DISTANCE));
		return  new Vector(vector3d.getX(), 0, vector3d.getZ()); 
	}
	
	public double getDockDistance() {
		return 10;
	}
	
	public Vector getApproachVector() {
		assert (marker.isPresent());
		
		//Vector vector3d = getApproachPosition().subtract(getPosition());
		return  getChargingPosition().subtract(getApproachPosition());
	}
	
	public double getAngleToApproachVector() {
		assert (marker.isPresent());
		
		Vector chargerVector = getChargingPosition();
		Vector approachVector = getApproachVector();
		
		double angle = chargerVector.getAngleBetweenAsDeg(approachVector);
		return approachVector.getX() < 0 ? angle : -angle;
	}
	
	public boolean calibrate() {
		
		boolean result = false;
		
		CameraProcessor cp = new CameraProcessor(false, 0);
		cp.initArucoWithPoseEstimation(ArucoDictionary.DICT_4X4_50, 10);
		cp.startCapturing();
		
		if (detect(cp)) {

			ChargerCalibData calibData = new ChargerCalibData();
			calibData.setChargerMarkerCorrX(marker.get().getTranslationVector().getX());
			calibData.setChargerMarkerCorrZ(marker.get().getTranslationVector().getZ());

			try {

				File file = new File(CALIBDATA_FILENAME);
				JAXBContext jaxbContext = JAXBContext.newInstance(ChargerCalibData.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

				// output pretty printed
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				jaxbMarshaller.marshal(calibData, file);
				jaxbMarshaller.marshal(calibData, System.out);

				result = true;
			} catch (JAXBException e) {
				e.printStackTrace();
			}

		} 
		
		cp.stopCapturing();
		
		
		return result;
		
	}

}
