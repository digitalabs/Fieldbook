
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
	public DesignImportData parseWorkbook(final Workbook workbook, final Map<String, Object> additionalParams) throws FileParsingException {

		final DesignImportData data = new DesignImportData();

		final Sheet sheet = workbook.getSheetAt(DesignImportExcelParser.SHEET_INDEX);

		final List<DesignHeaderItem> designHeaderItems = this.createDesignHeaders(sheet.getRow(DesignImportExcelParser.HEADER_ROW_INDEX));
		data.setUnmappedHeaders(designHeaderItems);
		data.setRowDataMap(this.convertRowsToMap(sheet, designHeaderItems.size()));

		return data;
	}

	protected List<DesignHeaderItem> createDesignHeaders(final Row header) throws FileParsingException {
		final List<DesignHeaderItem> list = new ArrayList<>();
		int columnIndex = 0;
		if (header != null) {
			final Iterator<Cell> cellIterator = header.cellIterator();
			while (cellIterator.hasNext()) {
				final Cell cell = cellIterator.next();
				final DesignHeaderItem headerItem = new DesignHeaderItem();
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

	protected Map<Integer, List<String>> convertRowsToMap(final Sheet sheet, final int maxColumns) {

		final Map<Integer, List<String>> rowsMap = new HashMap<>();
		int rowNo = DesignImportExcelParser.HEADER_ROW_INDEX;
		while (!PoiUtil.rowIsEmpty(sheet, rowNo, 0, maxColumns)) {

			rowsMap.put(rowNo, this.convertRowtoList(sheet.getRow(rowNo), maxColumns));
			rowNo++;
		}

		return rowsMap;
	}

	private List<String> convertRowtoList(final Row row, final int maxColumns) {
		final List<String> list = new ArrayList<>();
		for (int i = 0; i < maxColumns; i++) {
			final Cell cell = row.getCell(i);

			String value = "";
			if (cell != null) {
				value = PoiUtil.getCellStringValue(cell);
			}
			list.add(value);
		}
		return list;
	}

}
