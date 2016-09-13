package de.gnox.rovy.server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.gnox.rovy.api.RovyCom;
import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyTelemetryData;

public class RovyComImpl extends UnicastRemoteObject implements RovyCom {

	protected RovyComImpl() throws RemoteException {
		super();
		Rovy.instance();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8810196916471087329L;

	// @Override
	// public Long login(String pwd) throws RemoteException {
	// System.out.println("login");
	// if (!PWD.equals(pwd)) {
	// RovyUtility.sleep(3000);
	// return null;
	// }
	// currendSessionId = random.nextLong();
	// System.out.println(currendSessionId);
	// return currendSessionId;
	// }

	// private void checkSessionId(long sessionId) {
	// if (currendSessionId == null || currendSessionId.longValue() !=
	// sessionId)
	// throw new RuntimeException("wrong sessionId or login state");
	// }

	@Override
	public boolean performCommand(RovyCommand command) throws RemoteException {
		// checkSessionId(sessionId);
		return Rovy.instance().performCommand(command);
	}

	@Override
	public String getCamPicture() throws RemoteException {
		// checkSessionId(sessionId);
		System.out.println("getCamPicture()");
		File camPicture = Rovy.instance().getCam().getPicture();
		System.out.println("picture graped");
		return camPicture != null ? camPicture.getName() : null;
	}

	@Override
	public String getCamVideo() throws RemoteException {
		// checkSessionId(sessionId);
		System.out.println("getCamVideo()");
		File camVideo = Rovy.instance().getCam().getVideo();
		return camVideo != null ? camVideo.getName() : null;
	}

	@Override
	public RovyTelemetryData getTelemetryData() throws RemoteException {
		// checkSessionId(sessionId);
		return Rovy.instance().getTelemetryData();
	}

}
