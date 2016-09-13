package de.gnox.rovy.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RovyCom extends Remote {

	// public Long login(String pwd) throws RemoteException;

	public boolean performCommand(RovyCommand command) throws RemoteException;

	public String getCamPicture() throws RemoteException;

	public String getCamVideo() throws RemoteException;

	public RovyTelemetryData getTelemetryData() throws RemoteException;

}
