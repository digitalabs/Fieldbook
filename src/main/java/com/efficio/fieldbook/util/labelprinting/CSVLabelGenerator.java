package com.efficio.fieldbook.util.labelprinting;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.google.common.collect.Maps;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.report.LotReportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
                this.generateColumnHeaders(selectedFieldIDs, getLabelHeadersFromTrialInstances(trialInstances));

        final List<Map<Integer, ExportColumnValue>> exportColumnValues =
                this.generateColumnValues(trialInstances, selectedFieldIDs, userLabelPrinting);

        try {
            this.germplasmExportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName, includeHeader);
        } catch (IOException e) {
            throw new LabelPrintingException(e);
        }

        return fileName;
    }

    @Override
    public String generateLabelsForGermplasmList(final List<GermplasmListData> germplasmListDataList, final UserLabelPrinting
            userLabelPrinting, final ByteArrayOutputStream baos) throws LabelPrintingException {

        final Locale locale = LocaleContextHolder.getLocale();

        final String fileName = userLabelPrinting.getFilenameDLLocation();
        String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
        final boolean includeHeader =
                LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
        final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());

        mainSelectedFields = this.appendBarcode(isBarcodeNeeded, mainSelectedFields);

        final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);

        //Label Headers
        //TODO move this block to separate function
        final Map<Integer, String> labelHeaders = Maps.newHashMap();
        for (final Integer selectedFieldId : selectedFieldIDs) {
            if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
                labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.gid", null, locale));
            } else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt()) {
                labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.designation", null, locale));
            } else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt()) {
                labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.cross", null, locale));
            } else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt()) {
                labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.stockid", null, locale));
            } else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
                labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale));
            }
        }

        final List<ExportColumnHeader> exportColumnHeaders =
                this.generateColumnHeaders(selectedFieldIDs, labelHeaders);
        final List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

        for (final GermplasmListData germplasmListData : germplasmListDataList){
            final Map<Integer, ExportColumnValue> exportColumnValueMap = Maps.newHashMap();

            for (final Integer selectedFieldId : selectedFieldIDs) {
                if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
                    // GID
                    exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, germplasmListData.getGid().toString()));
                } else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
                    // Barcode
                    final StringBuilder buffer = new StringBuilder();
                    final String fieldList = userLabelPrinting.getFirstBarcodeField() + "," + userLabelPrinting.getSecondBarcodeField() + "," + userLabelPrinting.getThirdBarcodeField();

                    final List<Integer> selectedBarcodeFieldIDs = SettingsUtil.parseFieldListAndConvert(fieldList);

                    for (final Integer selectedBarcodeFieldID : selectedBarcodeFieldIDs) {
                        if (!"".equalsIgnoreCase(buffer.toString())) {
                            buffer.append(this.delimiter);
                        }
                        //TODO Fix barcode to use the rest of the fields
                        buffer.append(germplasmListData.getGid().toString());
                    }

                    final String barcodeLabel =  buffer.toString();
                    exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, barcodeLabel));
                } else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt()) {
                    //Designation
                    exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, germplasmListData.getDesignation()));
                } else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt()) {
                    // Cross
                    exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, germplasmListData.getGroupName()));
                } else {
                    final List<ListEntryLotDetails> lotRows = (List<ListEntryLotDetails>) germplasmListData.getInventoryInfo().getLotRows();
                    if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt()) {
						// Stock ID
                        if (lotRows != null){
                            String stockIds = "";
                            for ( int i = 0; i < lotRows.size(); i++) {
                                final ListEntryLotDetails lotRow = lotRows.get(i);
                                stockIds += lotRow.getStockIds();
                                if (i != (lotRows.size() - 1)) {
                                    stockIds +=  ", ";
                                }
                            }
                            exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, stockIds));
                        } else {
                            exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, ""));
                        }
						exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, lotRows == null ? "" : lotRows.get(0).getStockIds()));
					} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
						// Lot ID
						if (lotRows != null){
                            String lotIds = "";
						    for ( int i = 0; i < lotRows.size(); i++) {
                                final ListEntryLotDetails lotRow = lotRows.get(i);
                                lotIds += lotRow.getLotId().toString();
                                if (i != (lotRows.size() - 1)) {
                                    lotIds +=  ", ";
                                }
                            }
                            exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, lotIds));
                        } else {
                            exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, ""));
                        }
					}
                }

            }

            exportColumnValues.add(exportColumnValueMap);
        }

        try {
            this.germplasmExportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName, includeHeader);
        } catch (final IOException e) {
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
            final String headerName = this.getColumnHeader(selectedFieldID, labelHeaders);
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

                final String value = this.getValueFromSpecifiedColumn(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, false);
                final ExportColumnValue columnValue = new ExportColumnValue(selectedFieldID, value);
                rowMap.put(selectedFieldID, columnValue);
            } catch (final NumberFormatException e) {
                CSVLabelGenerator.LOG.error(e.getMessage(), e);
            }
        }

        return rowMap;
    }
}
