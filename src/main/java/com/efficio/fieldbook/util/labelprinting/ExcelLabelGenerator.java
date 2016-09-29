package com.efficio.fieldbook.util.labelprinting;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelLabelGenerator extends BaseLabelGenerator{

    private static final Logger LOG = LoggerFactory.getLogger(ExcelLabelGenerator.class);

    public String generateLabels(final List<StudyTrialInstanceInfo> trialInstances, final UserLabelPrinting userLabelPrinting,
                                    final ByteArrayOutputStream baos) throws LabelPrintingException {

        String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
        final boolean includeHeader =
                LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
        final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());
        final String fileName = userLabelPrinting.getFilenameDLLocation();
        final String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        final String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        final String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

        try {

            final HSSFWorkbook workbook = new HSSFWorkbook();
            String sheetName = WorkbookUtil.createSafeSheetName(userLabelPrinting.getName());
            if (sheetName == null) {
                sheetName = "Labels";
            }
            final Sheet labelPrintingSheet = workbook.createSheet(sheetName);

            final CellStyle labelStyle = workbook.createCellStyle();
            final HSSFFont font = workbook.createFont();
            font.setBoldweight(org.apache.poi.ss.usermodel.Font.BOLDWEIGHT_BOLD);
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

            // Row 1: SUMMARY OF TRIAL, FIELD AND PLANTING DETAILS
            Row row = null;
            mainSelectedFields = this.appendBarcode(isBarcodeNeeded, mainSelectedFields);

            final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);

            if (includeHeader) {
                row = labelPrintingSheet.createRow(rowIndex++);
                // we add all the selected fields header
                this.printHeaderFields(getLabelHeadersFromTrialInstances(trialInstances), includeHeader, selectedFieldIDs, row,
                        columnIndex, labelStyle);
            }

            // we populate the info now
            for (final StudyTrialInstanceInfo trialInstance : trialInstances) {
                final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();

                final Map<String, String> moreFieldInfo = this.generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");

                for (final FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {

                    row = labelPrintingSheet.createRow(rowIndex++);
                    columnIndex = 0;

                    final String barcodeLabelForCode = this.generateBarcodeField(moreFieldInfo, fieldMapLabel, firstBarcodeField,
                            secondBarcodeField, thirdBarcodeField, fieldMapTrialInstanceInfo.getLabelHeaders(), false);
                    moreFieldInfo.put(LabelPrintingServiceImpl.BARCODE, barcodeLabelForCode);

                    for (final Integer selectedFieldID : selectedFieldIDs) {
                        final String leftText = this.getValueFromSpecifiedColumn(moreFieldInfo, fieldMapLabel, selectedFieldID,
                                fieldMapTrialInstanceInfo.getLabelHeaders(), false);
                        final Cell summaryCell = row.createCell(columnIndex++);
                        summaryCell.setCellValue(leftText);
                    }
                }
            }

            for (int columnPosition = 0; columnPosition < columnIndex; columnPosition++) {
                labelPrintingSheet.autoSizeColumn((short) columnPosition);
            }

            // Write the excel file
            final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            workbook.write(fileOutputStream);
            fileOutputStream.close();

        } catch (final Exception e) {
            ExcelLabelGenerator.LOG.error(e.getMessage(), e);
        }
        return fileName;
    }

    @Override
    public String generateLabelsForGermplasmList(final List<GermplasmListData> germplasmListDataList, UserLabelPrinting userLabelPrinting, ByteArrayOutputStream
            baos)
            throws LabelPrintingException {
        //FIXME Implement !!!!!!
        return null;
    }

    protected void printHeaderFields(final Map<Integer, String> labelHeaders, final boolean includeHeader,
                                     final List<Integer> selectedFieldIDs, final Row row, final int columnIndex, final CellStyle labelStyle) {
        if (includeHeader) {
            int currentIndex = columnIndex;
            for (final Integer selectedFieldID : selectedFieldIDs) {
                final String headerName = this.getColumnHeader(selectedFieldID, labelHeaders);
                final Cell summaryCell = row.createCell(currentIndex++);
                summaryCell.setCellValue(headerName);
                summaryCell.setCellStyle(labelStyle);
            }
        }
    }
}
