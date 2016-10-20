package de.gnox.rovy.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ChargerCalibData {

	private double chargerMarkerCorrX = 0;
	
	private double chargerMarkerCorrZ = 0;

	public double getChargerMarkerCorrX() {
		return chargerMarkerCorrX;
	}

	@XmlElement
	public void setChargerMarkerCorrX(double chargerMarkerCorrX) {
		this.chargerMarkerCorrX = chargerMarkerCorrX;
	}

	public double getChargerMarkerCorrZ() {
		return chargerMarkerCorrZ;
	}

	@XmlElement
	public void setChargerMarkerCorrZ(double chargerMarkerCorrZ) {
		this.chargerMarkerCorrZ = chargerMarkerCorrZ;
	}
	
	
}
