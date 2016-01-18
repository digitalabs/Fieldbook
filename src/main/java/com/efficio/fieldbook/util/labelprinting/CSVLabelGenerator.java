package com.efficio.fieldbook.util.labelprinting;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVLabelGenerator extends BaseLabelGenerator{
    private static final Logger LOG = LoggerFactory.getLogger(CSVLabelGenerator.class);

    @Resource
    private GermplasmExportService germplasmExportService;

    public String generateLabels(final List<StudyTrialInstanceInfo> trialInstances, final UserLabelPrinting userLabelPrinting,
                                    final ByteArrayOutputStream baos) throws LabelPrintingException {
        final String fileName = userLabelPrinting.getFilenameDLLocation();
        String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
        final boolean includeHeader =
                LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
        final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());

        mainSelectedFields = this.appendBarcode(isBarcodeNeeded, mainSelectedFields);

        final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);
        final List<ExportColumnHeader> exportColumnHeaders =
                this.generateColumnHeaders(selectedFieldIDs, trialInstances.get(0).getTrialInstance().getLabelHeaders());

        final List<Map<Integer, ExportColumnValue>> exportColumnValues =
                this.generateColumnValues(trialInstances, selectedFieldIDs, userLabelPrinting);

        try {
            this.germplasmExportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName, includeHeader);
        } catch (IOException e) {
            throw new LabelPrintingException(e);
        }

        return fileName;
    }

    private List<Map<Integer, ExportColumnValue>> generateColumnValues(final List<StudyTrialInstanceInfo> trialInstances,
                                                                       final List<Integer> selectedFieldIDs, final UserLabelPrinting userLabelPrinting) {
        final List<Map<Integer, ExportColumnValue>> columnValues = new ArrayList<>();

        final String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        final String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        final String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

        // we populate the info now
        for (final StudyTrialInstanceInfo trialInstance : trialInstances) {
            final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();

            final Map<String, String> moreFieldInfo = this.generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");
            for (final FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {
                final String barcodeLabelForCode = this.generateBarcodeField(moreFieldInfo, fieldMapLabel, firstBarcodeField,
                        secondBarcodeField, thirdBarcodeField, fieldMapTrialInstanceInfo.getLabelHeaders(), false);
                moreFieldInfo.put(LabelPrintingServiceImpl.BARCODE, barcodeLabelForCode);

                final Map<Integer, ExportColumnValue> rowMap =
                        this.generateRowMap(fieldMapTrialInstanceInfo.getLabelHeaders(), selectedFieldIDs, moreFieldInfo, fieldMapLabel);
                columnValues.add(rowMap);
            }
        }

        return columnValues;
    }


    private List<ExportColumnHeader> generateColumnHeaders(final List<Integer> selectedFieldIDs, final Map<Integer, String> labelHeaders) {
        final List<ExportColumnHeader> columnHeaders = new ArrayList<>();

        for (final Integer selectedFieldID : selectedFieldIDs) {
            final String headerName = this.getHeader(selectedFieldID, labelHeaders);
            final ExportColumnHeader columnHeader = new ExportColumnHeader(selectedFieldID, headerName, true);
            columnHeaders.add(columnHeader);
        }

        return columnHeaders;
    }

    private Map<Integer, ExportColumnValue> generateRowMap(final Map<Integer, String> labelHeaders, final List<Integer> selectedFieldIDs,
                                                           final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel) {
        final Map<Integer, ExportColumnValue> rowMap = new HashMap<>();

        for (final Integer selectedFieldID : selectedFieldIDs) {

            try {

                final String value = this.getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, false);
                final ExportColumnValue columnValue = new ExportColumnValue(selectedFieldID, value);
                rowMap.put(selectedFieldID, columnValue);
            } catch (final NumberFormatException e) {
                CSVLabelGenerator.LOG.error(e.getMessage(), e);
            }
        }

        return rowMap;
    }
}
