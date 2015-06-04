
package com.efficio.fieldbook.web.common.bean;

public class DuplicateType {

	Integer listDataProjectId;
	String duplicate;

	public DuplicateType(Integer listDataProjectId, String duplicate) {
		super();
		this.listDataProjectId = listDataProjectId;
		this.duplicate = duplicate;
	}

	public Integer getListDataProjectId() {
		return this.listDataProjectId;
	}

	public Boolean isPedigreeDupe() {
		if (this.duplicate != null) {
			return this.duplicate.contains("Pedigree Dupe");
		}
		return false;
	}

	public Boolean isPlotDupe() {
		if (this.duplicate != null) {
			return this.duplicate.contains("Plot Dupe");
		}
		return false;
	}

	public Boolean isPedigreeRecip() {
		if (this.duplicate != null) {
			return this.duplicate.contains("Pedigree Recip");
		}
		return false;
	}

	public Boolean isPlotRecip() {
		if (this.duplicate != null) {
			return this.duplicate.contains("Plot Recip");
		}
		return false;
	}
}
