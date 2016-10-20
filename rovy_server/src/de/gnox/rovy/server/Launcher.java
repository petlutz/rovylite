package de.gnox.rovy.server;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

import de.gnox.rovy.api.RovyCom;

public class Launcher {

	public static void main(String[] args) {
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}


		if (args.length > 0 && "-cp".equals(args[0])) {
			calibCharger();	
		}
		
		try {
			RovyCom obj = new RovyComImpl();
			System.out.println("RovyCom server running ...");
			Naming.rebind("rmi://127.0.0.1:1234/RovyCom", obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	private static void calibCharger() {
		System.out.println("calibrate charger ...");
		Charger charger = new Charger();
		
		if (charger.calibrate()) {
			System.out.println("... calibration successful");
		} else {
			System.out.println("... calibration failed");			
		}
 		  
	}

}
