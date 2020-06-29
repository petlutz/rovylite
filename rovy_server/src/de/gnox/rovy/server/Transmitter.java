package de.gnox.rovy.server;

import java.io.IOException;

public class Transmitter {

	public void send(String systemCode, String unitCode, String command) {
		
		String[] cmd = { "433Utils/RPi_utils/send", systemCode, unitCode, command };
		
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
