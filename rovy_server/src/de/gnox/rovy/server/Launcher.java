package de.gnox.rovy.server;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

import com.pi4j.wiringpi.Gpio;

import de.gnox.rovy.api.RovyCom;

public class Launcher {

	public static void main(String[] args) {O
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
//    		try {
//			Gpio.wiringPiSetup();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
            RovyCom obj = new RovyComImpl();
            Naming.rebind("rmi://127.0.0.1:1234/RovyCom",obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
	}

}
