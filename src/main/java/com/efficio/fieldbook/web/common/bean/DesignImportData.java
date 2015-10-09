
package com.efficio.fieldbook.web.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.middleware.domain.dms.PhenotypicType;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class DesignImportData {

	private List<DesignHeaderItem> unmappedHeaders;
	private Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders;
	private Map<Integer, List<String>> csvData;
	private Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeadersWithDesignHeaderItemsMappedToStdVarId;

	public List<DesignHeaderItem> getUnmappedHeaders() {
		if (this.unmappedHeaders == null) {
			this.unmappedHeaders = new ArrayList<DesignHeaderItem>();
		}
		return this.unmappedHeaders;
	}

	public void setUnmappedHeaders(final List<DesignHeaderItem> unmappedHeaders) {
		this.unmappedHeaders = unmappedHeaders;
	}

	public Map<PhenotypicType, List<DesignHeaderItem>> getMappedHeaders() {
		if (this.mappedHeaders == null) {
			this.mappedHeaders = new HashMap<PhenotypicType, List<DesignHeaderItem>>();
		}
		return this.mappedHeaders;
	}

	public void setMappedHeaders(final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders) {
		this.mappedHeaders = mappedHeaders;

		this.mappedHeadersWithDesignHeaderItemsMappedToStdVarId = new HashMap<PhenotypicType, Map<Integer, DesignHeaderItem>>();

		if (mappedHeaders != null && !mappedHeaders.isEmpty()) {
			for (final Entry<PhenotypicType, List<DesignHeaderItem>> designHeaderItem : mappedHeaders.entrySet()) {
				final ImmutableMap<Integer, DesignHeaderItem> convertedMap =
						Maps.uniqueIndex(designHeaderItem.getValue(), new Function<DesignHeaderItem, Integer>() {

							@Override
							public Integer apply(final DesignHeaderItem from) {
								return from.getVariable().getId();
							}
						});
				this.mappedHeadersWithDesignHeaderItemsMappedToStdVarId.put(designHeaderItem.getKey(), convertedMap);
			}
		}
	}

	public Map<Integer, List<String>> getCsvData() {
		return this.csvData;
	}

	public void setCsvData(final Map<Integer, List<String>> csvData) {
		this.csvData = csvData;
	}

	public Map<PhenotypicType, Map<Integer, DesignHeaderItem>> getMappedHeadersWithDesignHeaderItemsMappedToStdVarId() {
		if (this.mappedHeadersWithDesignHeaderItemsMappedToStdVarId == null) {
			this.mappedHeadersWithDesignHeaderItemsMappedToStdVarId = new HashMap<PhenotypicType, Map<Integer, DesignHeaderItem>>();
		}
		return this.mappedHeadersWithDesignHeaderItemsMappedToStdVarId;
	}

}
