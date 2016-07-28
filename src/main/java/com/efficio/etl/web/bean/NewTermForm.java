
package com.efficio.etl.web.bean;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
@Deprecated
public class NewTermForm {

	private String name;
	private String description;
	private int cvId;
	private int classId;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getCvId() {
		return this.cvId;
	}

	public void setCvId(int cvId) {
		this.cvId = cvId;
	}

	public int getClassId() {
		return this.classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}
}
