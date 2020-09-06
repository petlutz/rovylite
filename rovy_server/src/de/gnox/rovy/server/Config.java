package de.gnox.rovy.server;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;

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
		return "10001";
	}
	
	public String getPowerSwitchUnitCode() {
		return "1";
	}
	
	public Pin getPinLight() {
		return RaspiPin.GPIO_05;
	}

	public Pin getPinFanControl() {
		return RaspiPin.GPIO_26;
	}
	
	public Pin getPinFanPower() {
		return RaspiPin.GPIO_22;
	}
	
	public Pin getPinButton1() {
		return RaspiPin.GPIO_28;
	}
	
	public Pin getPinButton2() {
		return RaspiPin.GPIO_25;
	}

	public int getDisplayI2cBusNr() {
		return I2CBus.BUS_1;
	}
	
	
}
