package com.efficio.fieldbook.web.common.bean;

public class AdvanceGermplasmChangeDetail {
	private int status;
	private int index;
	private String newAdvanceName;
	private String oldAdvanceName;
	private String questionText;
	private String addSequenceText;
	public AdvanceGermplasmChangeDetail() {
		super();
	}
	public AdvanceGermplasmChangeDetail(int status, int index,
			String newAdvanceName, String oldAdvanceName) {
		super();
		this.status = status;
		this.index = index;
		this.newAdvanceName = newAdvanceName;
		this.oldAdvanceName = oldAdvanceName;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getNewAdvanceName() {
		return newAdvanceName;
	}
	public void setNewAdvanceName(String newAdvanceName) {
		this.newAdvanceName = newAdvanceName;
	}
	public String getOldAdvanceName() {
		return oldAdvanceName;
	}
	public void setOldAdvanceName(String oldAdvanceName) {
		this.oldAdvanceName = oldAdvanceName;
	}
	/**
	 * @return the questionText
	 */
	public String getQuestionText() {
		return questionText;
	}
	/**
	 * @param questionText the questionText to set
	 */
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	/**
	 * @return the addSequenceText
	 */
	public String getAddSequenceText() {
		return addSequenceText;
	}
	/**
	 * @param addSequenceText the addSequenceText to set
	 */
	public void setAddSequenceText(String addSequenceText) {
		this.addSequenceText = addSequenceText;
	}
	
	
}
