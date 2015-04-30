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
		return listDataProjectId;
	}
	
	public Boolean isPedigreeDupe() {
		return duplicate.contains("Pedigree Dupe");
	}	

	public Boolean isPlotDupe() {
		return duplicate.contains("Plot Dupe");
	}
	
	public Boolean isPedigreeRecip() {
		return duplicate.contains("Pedigree Recip");
	}
	
	public Boolean isPlotRecip() {
		return duplicate.contains("Plot Recip");
	}
}
