package com.efficio.fieldbook.web.common.bean;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.PhenotypicType;

public class DesignImportData {

	private List<String> unmappedHeaders;
	private Map<PhenotypicType, List<MappedHeaderItem>> mappedHeaders;
	private Map<Integer, List<String>> csvData;

	public List<String> getUnmappedHeaders() {
		return unmappedHeaders;
	}
	
	public void setUnmappedHeaders(List<String> unmappedHeaders) {
		this.unmappedHeaders = unmappedHeaders;
	}
	
	public Map<PhenotypicType, List<MappedHeaderItem>> getMappedHeaders() {
		return mappedHeaders;
	}
	
	public void setMappedHeaders(Map<PhenotypicType, List<MappedHeaderItem>> mappedHeaders) {
		this.mappedHeaders = mappedHeaders;
	}
	
	public Map<Integer, List<String>> getCsvData() {
		return csvData;
	}

	public void setCsvData(Map<Integer, List<String>> csvData) {
		this.csvData = csvData;
	}
	
}
