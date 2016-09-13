package de.gnox.rovy.api;

import java.io.Serializable;
import java.util.Map;

public class RovyCommand implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3127419504397153600L;
	
	public RovyCommand(RovyCommandType type, Map<String, String[]> parameter) {
		super();
		this.type = type;
		this.parameter = parameter;
	}

	public RovyCommandType getType() {
		return type;
	}
	
	public void setType(RovyCommandType type) {
		this.type = type;
	}
	
	public void setParameter(Map<String, String[]> parameter) {
		this.parameter = parameter;
	}
	
	public Map<String, String[]> getParameter() {
		return parameter;
	}
	
	public String getParameter(String key) {
		String[] values = parameter.get(key);
		if (values.length > 0)
			return values[0];
		return null;
	}
	
	public void setPerformed(Exception e) {
		performed = true;
		error = e;
	}
	
	public boolean isPerformed() {
		return performed;
	}
	
	public Exception getError() {
		return error;
	}
	
	@Override
	public String toString() {
		String str = type.toString();
		if (isPerformed()) {
			str += " PERFORMED! ";
			if (error != null)
				str += " WITH ERROR: " + error.getMessage();
		}
		
		return str;
	}
 	
	private RovyCommandType type;
	
	private Map<String,String[]> parameter;
	
	private boolean performed;
	
	private transient Exception error;

	

}
