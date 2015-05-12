package com.efficio.fieldbook.web.common.bean;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.PhenotypicType;

public class DesignImportData {

	private List<DesignHeaderItem> unmappedHeaders;
	private Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders;
	private Map<Integer, List<String>> csvData;

	public List<DesignHeaderItem> getUnmappedHeaders() {
		return unmappedHeaders;
	}
	
	public void setUnmappedHeaders(List<DesignHeaderItem> unmappedHeaders) {
		this.unmappedHeaders = unmappedHeaders;
	}
	
	public Map<PhenotypicType, List<DesignHeaderItem>> getMappedHeaders() {
		return mappedHeaders;
	}
	
	public void setMappedHeaders(Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders) {
		this.mappedHeaders = mappedHeaders;
	}
	
	public Map<Integer, List<String>> getCsvData() {
		return csvData;
	}

	public void setCsvData(Map<Integer, List<String>> csvData) {
		this.csvData = csvData;
	}
	
}
