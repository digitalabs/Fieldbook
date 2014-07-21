package com.efficio.fieldbook.web.common.exception;

public class BVDesignException extends Exception {
	private String bvErrorCode;
	
	public BVDesignException(String bvErrorCode) {
        super();
        this.bvErrorCode = bvErrorCode;
    }

	public String getBvErrorCode() {
		return bvErrorCode;
	}

	public void setBvErrorCode(String bvErrorCode) {
		this.bvErrorCode = bvErrorCode;
	}

	
}
