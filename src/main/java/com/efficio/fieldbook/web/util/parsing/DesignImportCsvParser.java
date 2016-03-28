
package com.efficio.fieldbook.web.util.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.AbstractCsvFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;

public class DesignImportCsvParser extends AbstractCsvFileParser<DesignImportData> {
	
	@Resource
	private MessageSource messageSource;
	
	@Override
	public DesignImportData parseCsvMap(Map<Integer, List<String>> csvMap) throws FileParsingException {

		DesignImportData data = new DesignImportData();
		if(csvMap.get(0) != null && !csvMap.get(0).isEmpty()) {
			data.setUnmappedHeaders(this.createDesignHeaders(csvMap.get(0)));
			data.setRowDataMap(csvMap);
		} else {
			throw new FileParsingException(this.messageSource.getMessage("common.error.file.empty", null, Locale.ENGLISH));
		}
		
		return data;
	}

	protected List<DesignHeaderItem> createDesignHeaders(List<String> headers) {
		List<DesignHeaderItem> list = new ArrayList<>();
		int columnIndex = 0;
		for (String headerName : headers) {
			DesignHeaderItem headerItem = new DesignHeaderItem();
			headerItem.setName(headerName);
			headerItem.setColumnIndex(columnIndex);
			list.add(headerItem);
			
			columnIndex++;
		}
		return list;
	}

}
