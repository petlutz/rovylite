package de.gnox.rovy.server;

import java.util.Collections;

import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyCommandType;

public enum ControlMode {
	
	DisplayOff(null, null), 
	InfoDisplay(null, null), 
	CmdSwitchOn("ON", new RovyCommand(RovyCommandType.PowerOn, Collections.emptyMap())), 
	CmdSwitchOff("OFF", new RovyCommand(RovyCommandType.PowerOff, Collections.emptyMap())), 
	CmdAbortJob("ABORT", new RovyCommand(RovyCommandType.AbortJob, Collections.emptyMap()));
	
	private ControlMode(String caption, RovyCommand command) {
		this.caption = caption;
		this.command = command; 
	}
	
	public String getCaption() {
		return caption;
	}
	

	public RovyCommand getCommand() {
		return command;
	}

	private RovyCommand command;
	
	private String caption;
	
}
