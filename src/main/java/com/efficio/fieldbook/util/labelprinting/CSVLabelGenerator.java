package com.efficio.fieldbook.util.labelprinting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportRow;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.SettingsUtil;

@Component
public class CSVLabelGenerator implements LabelGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(CSVLabelGenerator.class);

    @Resource
    private GermplasmExportService germplasmExportService;
    @Resource
    private LabelPrintingUtil labelPrintingUtil;

    @Override
    public String generateLabels(final List<StudyTrialInstanceInfo> trialInstances, final UserLabelPrinting userLabelPrinting) throws LabelPrintingException {

        final String fileName = userLabelPrinting.getFilenameDLLocation();
        String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
        final boolean includeHeader =
                LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
        final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());

        mainSelectedFields = this.labelPrintingUtil.appendBarcode(isBarcodeNeeded, mainSelectedFields);

        final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvertToListOfIDs(mainSelectedFields);
        final List<ExportColumnHeader> exportColumnHeaders =
                this.generateColumnHeaders(selectedFieldIDs, this.labelPrintingUtil.getLabelHeadersFromTrialInstances(trialInstances));

        final List<ExportRow> exportRows =
                this.generateRows(trialInstances, selectedFieldIDs, userLabelPrinting);

        try {
            this.germplasmExportService.generateCSVFile(exportRows, exportColumnHeaders, fileName, includeHeader);
        } catch (final IOException e) {
            throw new LabelPrintingException(e);
        }

        return fileName;
    }

	private List<ExportRow> generateRows(final List<StudyTrialInstanceInfo> trialInstances, final List<Integer> selectedFieldIDs,
			final UserLabelPrinting userLabelPrinting) {
	      final List<ExportRow> exportRows = new ArrayList<>();

        final String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        final String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        final String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();
        final boolean generateAutomatically = (userLabelPrinting.getBarcodeGeneratedAutomatically()
                .equalsIgnoreCase(LabelPrintingServiceImpl.BARCODE_GENERATED_AUTOMATICALLY)) ? true : false;
        // we populate the info now
        for (final StudyTrialInstanceInfo trialInstance : trialInstances) {
            final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();

            final Map<String, String> moreFieldInfo = this.labelPrintingUtil.generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");
            for (final FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {
                final String barcodeLabelForCode;
                if (!generateAutomatically ) {
                    barcodeLabelForCode = this.labelPrintingUtil
                            .generateBarcodeField(moreFieldInfo, fieldMapLabel, firstBarcodeField, secondBarcodeField, thirdBarcodeField,
                                    fieldMapTrialInstanceInfo.getLabelHeaders(), false);
                } else {
                    barcodeLabelForCode = fieldMapLabel.getObsUnitId();
                }
                moreFieldInfo.put(LabelPrintingServiceImpl.BARCODE, barcodeLabelForCode);

				final ExportRow row =
						this.generateExportRow(fieldMapTrialInstanceInfo.getLabelHeaders(), selectedFieldIDs, moreFieldInfo, fieldMapLabel);
				 exportRows.add(row);
            }
        }

        return exportRows;
    }

    private List<ExportColumnHeader> generateColumnHeaders(final List<Integer> selectedFieldIDs, final Map<Integer, String> labelHeaders) {
        final List<ExportColumnHeader> columnHeaders = new ArrayList<>();

        for (final Integer selectedFieldID : selectedFieldIDs) {
            final String headerName = this.labelPrintingUtil.getColumnHeader(selectedFieldID, labelHeaders);
            final ExportColumnHeader columnHeader = new ExportColumnHeader(selectedFieldID, headerName, true);
            columnHeaders.add(columnHeader);
        }

        return columnHeaders;
    }

    private ExportRow generateExportRow(final Map<Integer, String> labelHeaders, final List<Integer> selectedFieldIDs,
                                                           final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel) {
        final ExportRow exportRow = new ExportRow();

        for (final Integer selectedFieldID : selectedFieldIDs) {

            try {

                final String value = this.labelPrintingUtil.getValueFromSpecifiedColumn(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, false);
                exportRow.addColumnValue(selectedFieldID, value);
            } catch (final NumberFormatException e) {
                CSVLabelGenerator.LOG.error(e.getMessage(), e);
            }
        }

        return exportRow;
    }
}
