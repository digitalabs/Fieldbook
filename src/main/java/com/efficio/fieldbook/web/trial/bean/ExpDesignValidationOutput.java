package com.efficio.fieldbook.web.trial.bean;

import java.io.Serializable;

public class ExpDesignValidationOutput implements Serializable {
	private boolean isValid;
	private String message;
	
	public ExpDesignValidationOutput() {
		super();		
	}
	public ExpDesignValidationOutput(boolean isValid, String message) {
		super();
		this.isValid = isValid;
		this.message = message;
	}
	public boolean isValid() {
		return isValid;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
