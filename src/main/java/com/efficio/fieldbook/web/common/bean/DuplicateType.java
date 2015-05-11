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
		if(duplicate != null){
			return duplicate.contains("Pedigree Dupe");
		}
		return false;
	}	

	public Boolean isPlotDupe() {
		if(duplicate != null){
			return duplicate.contains("Plot Dupe");
		}
		return false;
	}
	
	public Boolean isPedigreeRecip() {
		if(duplicate != null){
			return duplicate.contains("Pedigree Recip");
		}
		return false;
	}
	
	public Boolean isPlotRecip() {
		if(duplicate != null){
			return duplicate.contains("Plot Recip");
		}
		return false;
	}
}
