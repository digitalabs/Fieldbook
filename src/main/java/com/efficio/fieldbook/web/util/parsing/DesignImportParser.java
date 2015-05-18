package com.efficio.fieldbook.web.util.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.AbstractCsvFileParser;
import org.generationcp.commons.parsing.FileParsingException;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;

public class DesignImportParser extends AbstractCsvFileParser<DesignImportData> {

	@Override
	public DesignImportData parseCsvMap(Map<Integer, List<String>> csvMap)
			throws FileParsingException {
		
		DesignImportData data = new DesignImportData();
		data.setUnmappedHeaders(createDesignHeaders(csvMap.get(0)));
		data.setCsvData(csvMap);
		
		return data;
	}
	
	private List<DesignHeaderItem> createDesignHeaders(List<String> headers){
		List<DesignHeaderItem> list = new ArrayList<>();
		int columnIndex = 0;
		for (String headerName: headers){
			DesignHeaderItem headerItem = new DesignHeaderItem();
			headerItem.setName(headerName);
			headerItem.setColumnIndex(columnIndex);
			list.add(headerItem);
			columnIndex++;
		}
		return list;
	}

}
