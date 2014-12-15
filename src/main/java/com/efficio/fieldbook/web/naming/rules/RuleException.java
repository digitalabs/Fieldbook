package com.efficio.fieldbook.web.naming.rules;

import java.util.Locale;


public class RuleException extends Exception {
	
	private static final long serialVersionUID = 5937934311090989339L;

	private Object[] objects;
	
	private Locale locale;
	
	public RuleException(String message, Object[] objects, Locale locale) {
		super(message);
		this.objects = objects;
		this.locale = locale;		
	}

	
	public Object[] getObjects() {
		return objects;
	}

	
	public Locale getLocale() {
		return locale;
	}

}
