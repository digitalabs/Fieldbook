
package com.efficio.etl.web.bean;

import java.util.List;

import org.generationcp.middleware.util.Message;

@Deprecated
public class ImportResult {

	private boolean success = false;
	private List<Message> errorMessages;
	private String url;

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ImportResult() {
		// TODO Auto-generated constructor stub
	}

	public boolean isSuccess() {
		return this.success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public List<Message> getErrorMessages() {
		return this.errorMessages;
	}

	public void setErrorMessages(List<Message> errorMessages) {
		this.errorMessages = errorMessages;
	}

}
