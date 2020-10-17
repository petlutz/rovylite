package de.gnox.rovy.server;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

import com.pi4j.wiringpi.Gpio;

import de.gnox.rovy.api.RovyCom;

public class Launcher {

	public static void main(String[] args) {
        if(System.getSecurityManager() == null)
        {
            System.setSecurityManager(new RMISecurityManager());
        }
//        if(System.getSecurityManager() == null)
//        {
//            System.setSecurityManager(new RMISecurityManager());
//        }
//	    if (System.getSecurityManager() == null) {
//	        System.setSecurityManager(new SecurityManager());
//	    }
        try {
    		if (Gpio.wiringPiSetup() == -1) {
    			System.out.println(" ==>> GPIO SETUP FAILED");
    			return;
    		}
            RovyCom obj = new RovyComImpl();
            Naming.rebind("rmi://127.0.0.1:1234/RovyCom",obj);
            
            System.out.println("main loop running ...");
            while (true) {
            	Rovy.instance().update();
            	RovyUtility.sleep(10);
            }
            
        }catch (Exception e){
            throw new RuntimeException(e);
        }
	}

}
