package com.efficio.fieldbook.web.trial.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BVDesignOutput implements Serializable{
	
	private int returnCode;
	private String[] bvHeaders;
	private List<String[]> bvResultList;
	
	public BVDesignOutput(int returnCode){
		super();
		this.returnCode = returnCode;
	}
	
	public void setResults(List<String[]> entries){
		//1st entry is always the header 
		if(entries != null && !entries.isEmpty()){
			bvResultList = new ArrayList<String[]>();
			for(int i = 0 ; i < entries.size() ; i++){
				if(i == 0){
					//this is the header
					setBvHeaders(entries.get(i));					
				}else{
					bvResultList.add(entries.get(i));
				}
			}
		}
	}
	
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String[] getBvHeaders() {
		return bvHeaders;
	}

	public void setBvHeaders(String[] bvHeaders) {
		this.bvHeaders = bvHeaders;
	}

	public List<String[]> getBvResultList() {
		return bvResultList;
	}

	public void setBvResultList(List<String[]> bvResultList) {
		this.bvResultList = bvResultList;
	}
	
	public String getEntryValue(String header, int index){
		String val = null;
		if(header != null && bvResultList != null && index < bvResultList.size() && index > -1){
			
			for(int headerIndex = 0 ; headerIndex < bvHeaders.length ; headerIndex++){
				if(header.equalsIgnoreCase(bvHeaders[headerIndex]) && bvResultList.get(index) != null && headerIndex < bvResultList.get(index).length){					
					return bvResultList.get(index)[headerIndex];
				}
			}
		}
		return val;
	}
	
	public boolean isSuccess(){
		if(returnCode == 0){
			return true;
		}
		return false;
	}
	
}
