package de.gnox.rovy.server;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class Config {
	
//	private Properties properties = new Properties();
	
	public Config() {
		
//		try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream("config.properties"))) {
//			properties.load(stream);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
		
	}
	
	public int getPinDHT22Data() {
		return 2;
		//Integer.parseInt(properties.getProperty("pin.dht22data"));
	}
	
	public String getPowerSwitchSystemCode() {
		return "00011";
	}
	
	public String getPowerSwitchUnitCode() {
		return "3";
	}
	
	public Pin getPinLight() {
		return RaspiPin.GPIO_05;
	}
	
	
}
