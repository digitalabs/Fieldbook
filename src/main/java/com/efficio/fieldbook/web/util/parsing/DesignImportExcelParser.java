
package com.efficio.fieldbook.web.util.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.util.PoiUtil;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;

public class DesignImportExcelParser extends AbstractExcelFileParser<DesignImportData> {

	// the data should be at the first sheet of workbook.
	public static final int SHEET_INDEX = 0;
	public static final int HEADER_ROW_INDEX = 0;

	@Override
	public DesignImportData parseWorkbook(Workbook workbook, Map<String, Object> additionalParams) throws FileParsingException {

		DesignImportData data = new DesignImportData();

		Sheet sheet = workbook.getSheetAt(SHEET_INDEX);

		List<DesignHeaderItem> designHeaderItems = this.createDesignHeaders(sheet.getRow(HEADER_ROW_INDEX));
		data.setUnmappedHeaders(designHeaderItems);
		data.setRowDataMap(this.convertRowsToMap(sheet, designHeaderItems.size()));

		return data;
	}

	protected List<DesignHeaderItem> createDesignHeaders(Row header) throws FileParsingException {
		List<DesignHeaderItem> list = new ArrayList<>();
		int columnIndex = 0;
		if(header != null){
			Iterator<Cell> cellIterator = header.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				DesignHeaderItem headerItem = new DesignHeaderItem();
				headerItem.setName(PoiUtil.getCellStringValue(cell));
				headerItem.setColumnIndex(columnIndex);
				list.add(headerItem);
				columnIndex++;
			}
		} else {
			throw new FileParsingException(this.messageSource.getMessage("common.error.file.empty", null, Locale.ENGLISH));
		}
		return list;
	}

	protected Map<Integer, List<String>> convertRowsToMap(Sheet sheet, int maxColumns) {

		Map<Integer, List<String>> rowsMap = new HashMap<>();
		int rowNo = HEADER_ROW_INDEX;
		while (!PoiUtil.rowIsEmpty(sheet, rowNo, 0, maxColumns)) {

			rowsMap.put(rowNo, this.convertRowtoList(sheet.getRow(rowNo), maxColumns));
			rowNo++;
		}

		return rowsMap;
	}

	private List<String> convertRowtoList(Row row, int maxColumns) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < maxColumns; i++) {
			Cell cell = row.getCell(i);

			String value = "";
			if (cell != null) {
				value = PoiUtil.getCellStringValue(cell);
			}
			list.add(value);
		}
		return list;
	}

}
