
package com.efficio.fieldbook.web.trial.bean.bvdesign;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BVDesignOutput implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1079217702697901056L;
	private int returnCode;
	private String[] bvHeaders;
	private List<String[]> bvResultList;
	private List<Map<String, String>> bvResultMap;

	public BVDesignOutput(int returnCode) {
		super();
		this.returnCode = returnCode;
	}

	public void setResults(List<String[]> entries) {
		// 1st entry is always the header
		if (entries != null && !entries.isEmpty()) {
			this.bvResultList = new ArrayList<String[]>();

			this.bvResultMap = new ArrayList<Map<String, String>>();
			for (int i = 0; i < entries.size(); i++) {
				if (i == 0) {
					// this is the header
					this.setBvHeaders(entries.get(i));
				} else {
					Map<String, String> dataMap = new HashMap<String, String>();
					this.bvResultList.add(entries.get(i));
					for (int index = 0; index < this.bvHeaders.length; index++) {
						dataMap.put(this.bvHeaders[index], entries.get(i)[index]);
					}
					this.bvResultMap.add(dataMap);
				}
			}
		}
	}

	public Map<String, String> getEntryMap(int index) {
		if (index < this.bvResultMap.size() && index >= 0) {
			return this.bvResultMap.get(index);
		}
		return null;
	}

	public int getReturnCode() {
		return this.returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String[] getBvHeaders() {
		return this.bvHeaders;
	}

	public void setBvHeaders(String[] bvHeaders) {
		this.bvHeaders = bvHeaders;
	}

	public List<String[]> getBvResultList() {
		return this.bvResultList;
	}

	public void setBvResultList(List<String[]> bvResultList) {
		this.bvResultList = bvResultList;
	}

	public String getEntryValue(String header, int index) {
		String val = null;
		if (header != null && this.bvResultList != null && index < this.bvResultList.size() && index > -1) {

			for (int headerIndex = 0; headerIndex < this.bvHeaders.length; headerIndex++) {
				if (header.equalsIgnoreCase(this.bvHeaders[headerIndex]) && this.bvResultList.get(index) != null
						&& headerIndex < this.bvResultList.get(index).length) {
					return this.bvResultList.get(index)[headerIndex];
				}
			}
		}
		return val;
	}

	public boolean isSuccess() {
		if (this.returnCode == 0) {
			return true;
		}
		return false;
	}

}