package com.efficio.fieldbook.web.common.bean;

public class GermplasmChangeDetail {

	private int index;
	private String originalDesig;
	private String originalGid;
	private String newDesig;
	private String newGid;
	private String message;
	private int status;
	
	public GermplasmChangeDetail() {
	}
	
	public GermplasmChangeDetail(int index, String originalDesig,
			String originalGid, String newDesig, String newGid) {
		super();
		this.index = index;
		this.originalDesig = originalDesig;
		this.originalGid = originalGid;
		this.newDesig = newDesig;
		this.newGid = newGid;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	/**
	 * @return the originalDesig
	 */
	public String getOriginalDesig() {
		return originalDesig;
	}
	/**
	 * @param originalDesig the originalDesig to set
	 */
	public void setOriginalDesig(String originalDesig) {
		this.originalDesig = originalDesig;
	}
	/**
	 * @return the originalGid
	 */
	public String getOriginalGid() {
		return originalGid;
	}
	/**
	 * @param originalGid the originalGid to set
	 */
	public void setOriginalGid(String originalGid) {
		this.originalGid = originalGid;
	}
	/**
	 * @return the newDesig
	 */
	public String getNewDesig() {
		return newDesig;
	}
	/**
	 * @param newDesig the newDesig to set
	 */
	public void setNewDesig(String newDesig) {
		this.newDesig = newDesig;
	}
	/**
	 * @return the newGid
	 */
	public String getNewGid() {
		return newGid;
	}
	/**
	 * @param newGid the newGid to set
	 */
	public void setNewGid(String newGid) {
		this.newGid = newGid;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}


}
