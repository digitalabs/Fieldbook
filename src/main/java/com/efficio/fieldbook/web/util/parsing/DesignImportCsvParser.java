
package com.efficio.fieldbook.web.util.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.generationcp.commons.parsing.AbstractCsvFileProcessor;
import org.generationcp.commons.parsing.FileParsingException;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;

public class DesignImportCsvParser extends AbstractCsvFileProcessor<DesignImportData> {

	private static final int HEADER_ROW_INDEX = 0;
	
	@Override
	public DesignImportData parseCsvMap(final Map<Integer, List<String>> csvMap) throws FileParsingException {

		final DesignImportData data = new DesignImportData();
		if (csvMap.get(DesignImportCsvParser.HEADER_ROW_INDEX) != null && !csvMap.get(DesignImportCsvParser.HEADER_ROW_INDEX).isEmpty()) {
			data.setUnmappedHeaders(this.createDesignHeaders(csvMap.get(DesignImportCsvParser.HEADER_ROW_INDEX)));
			data.setRowDataMap(csvMap);
		} else {
			throw new FileParsingException(this.messageSource.getMessage("common.error.file.empty", null, Locale.ENGLISH));
		}

		return data;
	}

	protected List<DesignHeaderItem> createDesignHeaders(final List<String> headers) {
		final List<DesignHeaderItem> list = new ArrayList<>();
		int columnIndex = 0;
		for (final String headerName : headers) {
			final DesignHeaderItem headerItem = new DesignHeaderItem();
			headerItem.setName(headerName);
			headerItem.setColumnIndex(columnIndex);
			list.add(headerItem);

			columnIndex++;
		}
		return list;
	}

}
