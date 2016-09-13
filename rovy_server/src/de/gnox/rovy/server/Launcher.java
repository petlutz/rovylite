package de.gnox.rovy.server;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

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
            RovyCom obj = new RovyComImpl();
        	System.out.println("RovyCom server running ...");
            Naming.rebind("rmi://127.0.0.1:1234/RovyCom",obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
	}

}
