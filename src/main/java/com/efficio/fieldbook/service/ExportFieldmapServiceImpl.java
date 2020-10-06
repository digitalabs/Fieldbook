/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.ExportFieldmapService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.fieldmap.bean.Plot;
import com.efficio.fieldbook.web.fieldmap.bean.SelectedFieldmapRow;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * The Class ExcelExportServiceImpl.
 */
@SuppressWarnings("deprecation")
public class ExportFieldmapServiceImpl implements ExportFieldmapService {

	private static final Logger LOG = LoggerFactory.getLogger(ExportFieldmapServiceImpl.class);

	private final static String UP = "  UP  ";
	private final static String DOWN = "  DOWN  ";

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Override
	public FileOutputStream exportFieldMapToExcel(final String fileName, final UserFieldmap userFieldMap) throws FieldbookException {
		final Locale locale = LocaleContextHolder.getLocale();

		// Summary of Study, Field and Planting Details
		final String summaryOfFieldbookFieldPlantingDetailsLabel =
				this.messageSource.getMessage("fieldmap.header.summary.for.study", null, locale);
		// SUMMARY OF STUDY, FIELD AND PLANTING DETAILS
		final String selectedFieldbookLabel = this.messageSource.getMessage("fieldmap.study.selected.studies", null, locale); // Selected Study:

		final String orderHeader = this.messageSource.getMessage("fieldmap.study.order", null, locale);
		final String studyHeader = this.messageSource.getMessage("fieldmap.study", null, locale);
		final String instanceHeader = this.messageSource.getMessage("fieldmap.trial.instance", null, locale);
		final String entriesCountHeader = this.messageSource.getMessage("fieldmap.study.entry.count", null, locale);
		final String repsCountHeader = this.messageSource.getMessage("fieldmap.study.reps.count", null, locale);
		final String plotsNeededHeader = this.messageSource.getMessage("fieldmap.study.plots.needed", null, locale);
		final String totalPlotsHeader = this.messageSource.getMessage("fieldmap.study.total.number.of.plots", null, locale);
		final String datasetNameHeader = this.messageSource.getMessage("fieldmap.study.dataset", null, locale);

		// Row, Range & Plot Details
		final String rowRangePlotDetailsLabel = this.messageSource.getMessage("fieldmap.study.row.and.range.and.plot.details", null, locale);
		// ROW, RANGE AND PLOT DETAILS
		final String blockCapacityLabel = this.messageSource.getMessage("fieldmap.label.block.capacity", null, locale);
		// Block Capacity
		final String blockCapacityValue = userFieldMap.getBlockCapacityString(this.messageSource); // e.g. "10 Columns, 10 Ranges"
		final String rowsPerPlotLabel = this.messageSource.getMessage("fieldmap.label.rows.per.plot", null, locale); // Rows per Plot
		final int rowsPerPlotValue = userFieldMap.getNumberOfRowsPerPlot();
		final String columnsLabel = this.messageSource.getMessage("fieldmap.label.columns", null, locale); // Columns
		final Integer columnsValue = userFieldMap.getNumberOfColumnsInBlock(); // 10
		final String machineCapacityLabel = this.messageSource.getMessage("fieldmap.label.row.capacity.machine", null, locale);
		// machine row capacity
		final Integer machineCapacityValue = userFieldMap.getMachineRowCapacity();

		// Planting Details
		final String plantingDetailsLabel = this.messageSource.getMessage("fieldmap.header.planting.details", null, locale);
		// PLANTING DETAILS
		final String startingCoordinatesLabel = this.messageSource.getMessage("fieldmap.label.starting.coordinates", null, locale);
		// Starting Coordinates
		final String startingCoordinatesValue = userFieldMap.getStartingCoordinateString(this.messageSource); // Column 1, Range 1
		final String plantingOrderLabel = this.messageSource.getMessage("fieldmap.label.planting.order", null, locale); // Planting Order
		final String plantingOrderValue = userFieldMap.getPlantingOrderString(this.messageSource); // "Row/Column" or "Serpentine"

		// FieldMap
		final String fieldMapLabel = this.messageSource.getMessage("fieldmap.header.fieldmap", null, locale); // FIELD MAP
		final String rowsLabel = this.messageSource.getMessage("fieldmap.label.rows", null, locale); // Rows
		final String columnLabel = this.messageSource.getMessage("fieldmap.label.capitalized.column", null, locale); // Column
		final String rangeLabel = this.messageSource.getMessage("fieldmap.label.capitalized.range", null, locale); // Range

		try {
			// Create workbook
			final HSSFWorkbook workbook = new HSSFWorkbook();
			final String summaryLabelSheet = this.messageSource.getMessage("fieldmap.header.excel.summary", null, locale);
			final Sheet summarySheet = workbook.createSheet(summaryLabelSheet);
			final Sheet fieldMapSheet = workbook.createSheet(fieldMapLabel);

			final CellStyle labelStyle = workbook.createCellStyle();
			HSSFFont font = workbook.createFont();
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			labelStyle.setFont(font);

			final CellStyle wrapStyle = workbook.createCellStyle();
			wrapStyle.setWrapText(true);
			wrapStyle.setAlignment(CellStyle.ALIGN_CENTER);

			final CellStyle mainHeaderStyle = workbook.createCellStyle();

			final HSSFPalette palette = workbook.getCustomPalette();
			// get the color which most closely matches the color you want to use
			final HSSFColor myColor = palette.findSimilarColor(179, 165, 165);
			// get the palette index of that color
			final short palIndex = myColor.getIndex();
			// code to get the style for the cell goes here
			mainHeaderStyle.setFillForegroundColor(palIndex);
			mainHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

			final CellStyle mainSubHeaderStyle = workbook.createCellStyle();

			final HSSFPalette paletteSubHeader = workbook.getCustomPalette();
			// get the color which most closely matches the color you want to use
			final HSSFColor myColorSubHeader = paletteSubHeader.findSimilarColor(190, 190, 186);
			// get the palette index of that color
			final short palIndexSubHeader = myColorSubHeader.getIndex();
			// code to get the style for the cell goes here
			mainSubHeaderStyle.setFillForegroundColor(palIndexSubHeader);
			mainSubHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			mainSubHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);

			int rowIndex = 0;
			int columnIndex = 0;

			// Create Header Information

			// Row 1: SUMMARY OF STUDY, FIELD AND PLANTING DETAILS
			final CellStyle headerLabelStyle = workbook.createCellStyle();
			font = workbook.createFont();
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerLabelStyle.setFont(font);
			headerLabelStyle.setAlignment(CellStyle.ALIGN_CENTER);

			Row row = summarySheet.createRow(rowIndex++);
			final Cell summaryCell = row.createCell(columnIndex);
			summaryCell.setCellValue(summaryOfFieldbookFieldPlantingDetailsLabel);

			summaryCell.setCellStyle(headerLabelStyle);

			summarySheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, // first row (0-based)
					rowIndex - 1, // last row (0-based)
					columnIndex, // first column (0-based)
					columnIndex + 5 // last column (0-based)
					));

			// Row 2: Space
			row = summarySheet.createRow(rowIndex++);

			// Row 3: Fieldbook Name, Entries, Reps, Plots
			row = summarySheet.createRow(rowIndex++);

			// Selected Study : [Fieldbook Name] TABLE SECTION
			Cell labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(selectedFieldbookLabel);

			row = summarySheet.createRow(rowIndex++);
			columnIndex = 0;
			Cell headerCell = row.createCell(columnIndex++);
			headerCell.setCellValue(orderHeader);
			headerCell.setCellStyle(labelStyle);
			headerCell = row.createCell(columnIndex++);
			headerCell.setCellValue(studyHeader);
			headerCell.setCellStyle(labelStyle);
			headerCell = row.createCell(columnIndex++);
			headerCell.setCellValue(instanceHeader);
			headerCell.setCellStyle(labelStyle);
			headerCell = row.createCell(columnIndex++);
			headerCell.setCellValue(entriesCountHeader);
			headerCell.setCellStyle(labelStyle);
			headerCell = row.createCell(columnIndex++);
			headerCell.setCellValue(repsCountHeader);
			headerCell.setCellStyle(labelStyle);
			headerCell = row.createCell(columnIndex++);
			headerCell.setCellValue(plotsNeededHeader);
			headerCell.setCellStyle(labelStyle);

			int order = 1;
			for (final SelectedFieldmapRow rec : userFieldMap.getSelectedFieldmapList().getRows()) {
				row = summarySheet.createRow(rowIndex++);
				columnIndex = 0;
				row.createCell(columnIndex++).setCellValue(order++);
				row.createCell(columnIndex++).setCellValue(rec.getStudyName());
				row.createCell(columnIndex++).setCellValue(rec.getTrialInstanceNo());
				row.createCell(columnIndex++).setCellValue(String.valueOf(rec.getEntryCount()));
				row.createCell(columnIndex++).setCellValue(String.valueOf(rec.getRepCount()));
				row.createCell(columnIndex++).setCellValue(String.valueOf(rec.getPlotCount()));
			}

			row = summarySheet.createRow(rowIndex++);
			columnIndex = 0;
			headerCell = row.createCell(columnIndex++);
			headerCell.setCellValue(totalPlotsHeader);
			headerCell.setCellStyle(labelStyle);
			row.createCell(columnIndex++).setCellValue(String.valueOf(userFieldMap.getSelectedFieldmapList().getTotalNumberOfPlots()));

			// Row 4: Space
			row = summarySheet.createRow(rowIndex++);

			// Row 5: Header - Details Heading
			row = summarySheet.createRow(rowIndex++);
			columnIndex = 0;

			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(rowRangePlotDetailsLabel);
			labelCell.setCellStyle(headerLabelStyle);

			summarySheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, // first row (0-based)
					rowIndex - 1, // last row (0-based)
					columnIndex - 1, // first column (0-based)
					columnIndex // last column (0-based)
					));

			row.createCell(columnIndex++);
			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(plantingDetailsLabel);
			labelCell.setCellStyle(headerLabelStyle);

			summarySheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, // first row (0-based)
					rowIndex - 1, // last row (0-based)
					columnIndex - 1, // first column (0-based)
					columnIndex // last column (0-based)
					));

			row.createCell(columnIndex++);

			// Row 6: Field Location, Block Capacity, Starting Coordinates
			row = summarySheet.createRow(rowIndex++);
			columnIndex = 0;

			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(blockCapacityLabel);
			labelCell.setCellStyle(labelStyle);

			row.createCell(columnIndex++).setCellValue(blockCapacityValue);

			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(startingCoordinatesLabel);
			labelCell.setCellStyle(labelStyle);

			row.createCell(columnIndex++).setCellValue(startingCoordinatesValue);

			// Row 7: Field Name, Rows Per Plot, Planting Order
			row = summarySheet.createRow(rowIndex++);
			columnIndex = 0;

			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(rowsPerPlotLabel);
			labelCell.setCellStyle(labelStyle);

			row.createCell(columnIndex++).setCellValue(String.valueOf(rowsPerPlotValue));
			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(plantingOrderLabel);
			labelCell.setCellStyle(labelStyle);

			row.createCell(columnIndex++).setCellValue(plantingOrderValue);

			// Row 8: Block Name, Columns
			row = summarySheet.createRow(rowIndex++);
			columnIndex = 0;

			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(columnsLabel);
			labelCell.setCellStyle(labelStyle);

			row.createCell(columnIndex++).setCellValue(String.valueOf(columnsValue));

			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(machineCapacityLabel);
			labelCell.setCellStyle(labelStyle);

			row.createCell(columnIndex++).setCellValue(String.valueOf(machineCapacityValue));

			// Row 9: Space
			row = summarySheet.createRow(rowIndex++);

			for (int columnsResize = 0; columnsResize < columnIndex; columnsResize++) {
				summarySheet.autoSizeColumn(columnsResize);
			}

			// Get FieldMap data
			// we reset the row index
			rowIndex = 0;

			// Row 10: FIELD MAP
			row = fieldMapSheet.createRow(rowIndex++);
			columnIndex = 0;
			labelCell = row.createCell(columnIndex++);
			labelCell.setCellValue(fieldMapLabel);
			labelCell.setCellStyle(labelStyle);

			// Row 11: Space
			row = fieldMapSheet.createRow(rowIndex++);

			final Plot[][] plots = userFieldMap.getFieldmap();
			final int range = userFieldMap.getNumberOfRangesInBlock();
			final int col = userFieldMap.getNumberOfColumnsInBlock();
			final int rowsPerPlot = userFieldMap.getNumberOfRowsPerPlot();
			final int machineRowCapacity = userFieldMap.getMachineRowCapacity();
			final int rows = userFieldMap.getNumberOfRowsInBlock();
			final boolean isSerpentine = userFieldMap.getPlantingOrder() == 2;

			for (int j = range - 1; j >= 0; j--) {

				if (j == range - 1) { // TOP TABLE LABELS

					// Row 12: Rows Header
					rowIndex =
							this.printRowHeader(fieldMapSheet, userFieldMap.getNumberOfRowsInBlock(), rowIndex, rowsLabel, mainHeaderStyle,
									mainSubHeaderStyle);

					// Row 13: UP, DOWN Direction
					rowIndex =
							this.printDirectionHeader(fieldMapSheet, plots, j, rows, rowIndex, machineRowCapacity, mainHeaderStyle,
									mainSubHeaderStyle, isSerpentine);

					// Row 14: Column labels
					rowIndex =
							this.printColumnHeader(fieldMapSheet, col, rowIndex, columnLabel, rowsPerPlot, mainHeaderStyle,
									mainSubHeaderStyle);
				}

				// Rows 15 onwards: Ranges and Row Data
				row = fieldMapSheet.createRow(rowIndex);
				row.setHeightInPoints(45);
				columnIndex = 0;
				final int rangeValue = j + 1;
				final Cell cellRange = row.createCell(columnIndex++);
				cellRange.setCellValue(rangeLabel + " " + rangeValue);
				cellRange.setCellStyle(mainSubHeaderStyle);
				for (int i = 0; i < col; i++) {
					String displayString = plots[i][j].getDisplayString().replace("<br/>", "\n");
					if (plots[i][j].isPlotDeleted()) {
						displayString = "  X  ";
					}
					final Cell dataCell = row.createCell(columnIndex++);
					dataCell.setCellValue(new HSSFRichTextString(displayString));
					dataCell.setCellStyle(wrapStyle);

					for (int k = 0; k < rowsPerPlot - 1; k++) {
						row.createCell(columnIndex++).setCellValue("");
					}

					fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, // first row (0-based)
							rowIndex, // last row (0-based)
							columnIndex - rowsPerPlot, // first column (0-based)
							columnIndex - 1 // last column (0-based)
							));
				}
				rowIndex++;

				if (j == 0) {
					// BOTTOM TABLE LABELS
					rowIndex =
							this.printColumnHeader(fieldMapSheet, col, rowIndex, columnLabel, rowsPerPlot, mainHeaderStyle,
									mainSubHeaderStyle);
					rowIndex =
							this.printDirectionHeader(fieldMapSheet, plots, j, rows, rowIndex, machineRowCapacity, mainHeaderStyle,
									mainSubHeaderStyle, isSerpentine);
					rowIndex =
							this.printRowHeader(fieldMapSheet, userFieldMap.getNumberOfRowsInBlock(), rowIndex, rowsLabel, mainHeaderStyle,
									mainSubHeaderStyle);
				}

			}

			// Write the excel file
			final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			workbook.write(fileOutputStream);
			fileOutputStream.close();
			return fileOutputStream;

		} catch (final FileNotFoundException e) {
			ExportFieldmapServiceImpl.LOG.error(e.getMessage(), e);
			throw new FieldbookException("Error writing to file: " + fileName, e);
		} catch (final IOException e) {
			ExportFieldmapServiceImpl.LOG.error(e.getMessage(), e);
			throw new FieldbookException("Error writing to file: " + fileName, e);
		}

	}

	private int printRowHeader(final Sheet fieldMapSheet, final int numOfRows, int rowIndex, final String rowsLabel, final CellStyle mainHeader,
			final CellStyle subHeaderStyle) {
		final Row row = fieldMapSheet.createRow(rowIndex++);
		int columnIndex = 0;
		final Cell cell = row.createCell(columnIndex++);
		cell.setCellValue(rowsLabel);
		cell.setCellStyle(mainHeader);
		for (int i = 0; i < numOfRows; i++) {
			final Cell tableCell = row.createCell(columnIndex++);
			tableCell.setCellValue(i + 1);
			tableCell.setCellStyle(subHeaderStyle);
		}
		return rowIndex;

	}

	private int printColumnHeader(final Sheet fieldMapSheet, final int numberOfColumns, int rowIndex, final String columnLabel, final int rowsPerPlot,
			final CellStyle mainHeader, final CellStyle subHeaderStyle) {
		final Row row = fieldMapSheet.createRow(rowIndex);
		int columnIndex = 0;
		final Cell mainCell = row.createCell(columnIndex++);
		mainCell.setCellValue("");
		mainCell.setCellStyle(mainHeader);
		for (int i = 0; i < numberOfColumns; i++) {
			final int columnValue = i + 1;
			final Cell cell = row.createCell(columnIndex++);
			cell.setCellValue(columnLabel + " " + columnValue);
			cell.setCellStyle(subHeaderStyle);
			for (int j = 0; j < rowsPerPlot - 1; j++) {
				final Cell cell1 = row.createCell(columnIndex++);
				cell1.setCellValue("");
				cell.setCellStyle(subHeaderStyle);
			}

			fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, columnIndex - rowsPerPlot, columnIndex - 1));
		}
		rowIndex++;
		return rowIndex;

	}

	private int printDirectionHeader(final Sheet fieldMapSheet, final Plot[][] plots, final int range, final int numberOfRows, int rowIndex,
			final int machineRowCapacity, final CellStyle mainHeader, final CellStyle subHeaderStyle, final boolean isSerpentine) {

		final Row row = fieldMapSheet.createRow(rowIndex);
		int columnIndex = 0;
		final Cell cell1 = row.createCell(columnIndex++);
		cell1.setCellValue("");
		cell1.setCellStyle(mainHeader);

		int numberOfDirections = numberOfRows / machineRowCapacity;
		final int remainingRows = numberOfRows % machineRowCapacity;
		if (remainingRows > 0) {
			numberOfDirections++;
		}

		for (int i = 0; i < numberOfDirections; i++) {
			final int startCol = machineRowCapacity * i + 1;
			if (i % 2 == 1) {
				final Cell cell = row.createCell(startCol);
				cell.setCellValue(ExportFieldmapServiceImpl.DOWN);
				cell.setCellStyle(subHeaderStyle);
			} else {
				final Cell cell = row.createCell(startCol);
				cell.setCellValue(ExportFieldmapServiceImpl.UP);
				cell.setCellStyle(subHeaderStyle);
			}
			if (i == numberOfDirections - 1 && remainingRows > 0) { // last item
				fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, startCol, machineRowCapacity * i + remainingRows));
			} else {
				fieldMapSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, startCol, machineRowCapacity * (i + 1)));
			}
		}
		rowIndex++;
		return rowIndex;
	}

}
