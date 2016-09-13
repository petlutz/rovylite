package de.gnox.rovy.server;

import java.util.concurrent.TimeUnit;

public class RovyUtility {

	public static void sleep(long millis) {
		try {
			TimeUnit.MILLISECONDS.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
