package de.gnox.rovy.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Controls DHT22 sensor.
 * 
 * Uses Adafruit_Python_DHT
 */
public class DHT22Simple {

	private Float temperature;
	private Float humidity;
	private int pinNumber;

	public DHT22Simple(int pinNumber) {
		this.pinNumber = pinNumber;
	}


	public void refreshData() {
		try {
			Process p = Runtime.getRuntime().exec("./read_dht22.py 22 " + pinNumber);
			BufferedReader reader = new BufferedReader( new InputStreamReader( p.getInputStream()) );
			String output = reader.readLine();
			System.out.println("DHT22: " + output);
			String[] splittedOutput = output.split(" ");
			temperature = Float.parseFloat(splittedOutput[0]);
			humidity = Float.parseFloat(splittedOutput[1]);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			temperature = humidity = null;
		}
	}

	public Float getHumidity() {
		return humidity;
	}

	public Float getTemperature() {
		return temperature;
	}



}
