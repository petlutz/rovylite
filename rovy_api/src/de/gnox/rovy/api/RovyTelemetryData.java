package de.gnox.rovy.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RovyTelemetryData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7342950004137522579L;
	
	private List<String> entries = new ArrayList<>();
	

	public void setEntries(List<String> entries) {
		this.entries = entries;
	}
	
	public List<String> getEntries() {
		return entries;
	}

}
