
package com.efficio.fieldbook.web.common.bean;

public enum ChangeType {

	ADDED_ROWS("confirmation.import.add.rows"), DELETED_ROWS("confirmation.import.delete.rows"), ADDED_TRAITS(
			"confirmation.import.add.cols"), DELETED_TRAITS("confirmation.import.delete.cols");

	private String messageCode;

	private ChangeType(String messageCode) {
		this.messageCode = messageCode;
	}

	/**
	 * @return the messageCode
	 */
	public String getMessageCode() {
		return this.messageCode;
	}

	/**
	 * @param messageCode the messageCode to set
	 */
	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}

}
