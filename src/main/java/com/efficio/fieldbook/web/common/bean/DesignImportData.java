
package com.efficio.fieldbook.web.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.PhenotypicType;

public class DesignImportData {

	private List<DesignHeaderItem> unmappedHeaders;
	private Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders;
	private Map<Integer, List<String>> csvData;

	public List<DesignHeaderItem> getUnmappedHeaders() {
		if (this.unmappedHeaders == null) {
			this.unmappedHeaders = new ArrayList<DesignHeaderItem>();
		}
		return this.unmappedHeaders;
	}

	public void setUnmappedHeaders(List<DesignHeaderItem> unmappedHeaders) {
		this.unmappedHeaders = unmappedHeaders;
	}

	public Map<PhenotypicType, List<DesignHeaderItem>> getMappedHeaders() {
		if (this.mappedHeaders == null) {
			this.mappedHeaders = new HashMap<PhenotypicType, List<DesignHeaderItem>>();
		}
		return this.mappedHeaders;
	}

	public void setMappedHeaders(Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders) {
		this.mappedHeaders = mappedHeaders;
	}

	public Map<Integer, List<String>> getCsvData() {
		return this.csvData;
	}

	public void setCsvData(Map<Integer, List<String>> csvData) {
		this.csvData = csvData;
	}

}
