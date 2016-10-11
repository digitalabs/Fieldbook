package com.efficio.fieldbook.util.labelprinting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.google.common.collect.Maps;

@Component
public class CSVSeedPreparationLabelGenerator implements LabelGenerator {

	@Resource
	private MessageSource messageSource;
	@Resource
	private GermplasmExportService germplasmExportService;
	@Resource
	private LabelPrintingUtil labelPrintingUtil;

	/** The delimiter for the barcode. */
	private static final String DELIMITER = " | ";
	private static final Logger LOG = LoggerFactory.getLogger(CSVSeedPreparationLabelGenerator.class);

	@Override
	public String generateLabels (final List<?> dataList, final UserLabelPrinting
			userLabelPrinting) throws LabelPrintingException {

		@SuppressWarnings("unchecked")
		final List<GermplasmListData> germplasmListDataList = (List<GermplasmListData>) dataList;

		final Locale locale = LocaleContextHolder.getLocale();

		final String fileName = userLabelPrinting.getFilenameDLLocation();
		String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
		final boolean includeHeader =
				LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
		final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());

		mainSelectedFields = this.labelPrintingUtil.appendBarcode(isBarcodeNeeded, mainSelectedFields);

		final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvertToListOfIDs(mainSelectedFields);

		//Label Headers
		//TODO move this block to separate utility function
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
				this.labelPrintingUtil.generateColumnHeaders(selectedFieldIDs, labelHeaders);
		final List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

		// Values in the columns
		//TODO move this block to separate utility function
		for (final GermplasmListData germplasmListData : germplasmListDataList){
			final Map<Integer, ExportColumnValue> exportColumnValueMap = Maps.newHashMap();
			@SuppressWarnings("unchecked")
			final List<ListEntryLotDetails> lotRows = (List<ListEntryLotDetails>) germplasmListData.getInventoryInfo().getLotRows();

			for (final Integer selectedFieldId : selectedFieldIDs) {
				if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
					// GID
					exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, germplasmListData.getGid().toString()));
				} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
					// Barcode
					final StringBuilder buffer = new StringBuilder();
					final String fieldList = userLabelPrinting.getFirstBarcodeField() + "," + userLabelPrinting.getSecondBarcodeField() + "," + userLabelPrinting.getThirdBarcodeField();

					final List<Integer> selectedBarcodeFieldIDs = SettingsUtil.parseFieldListAndConvertToListOfIDs(fieldList);

					for (final Integer selectedBarcodeFieldID : selectedBarcodeFieldIDs) {
						if (!"".equalsIgnoreCase(buffer.toString())) {
							buffer.append(DELIMITER);
						}
						//TODO Move barcode implementation to separate utility function
						// GID
						if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
							buffer.append(germplasmListData.getGid().toString());
						} else if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt()) {
							buffer.append(germplasmListData.getDesignation());
						} else if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt()) {
							buffer.append(germplasmListData.getGroupName());
						} else if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt()) {
							if (lotRows != null) {
								buffer.append(this.labelPrintingUtil.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID));
							} else {
								buffer.append(" ");
							}
						} else if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
							if (lotRows != null) {
								buffer.append(this.labelPrintingUtil.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_SEED_LOT_ID));
							} else {
								buffer.append(" ");
							}
						}
					}

					final String barcodeLabel =  buffer.toString();
					exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, barcodeLabel));
				} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt()) {
					//Designation
					exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, germplasmListData.getDesignation()));
				} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt()) {
					// Cross
					exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, germplasmListData.getGroupName()));
				} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt()) {
					// Stock ID
					if (lotRows != null) {
						exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId,
								this.labelPrintingUtil.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID)));
					} else {
						exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, ""));
					}
				} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
					// Lot ID
					if (lotRows != null) {
						exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId,
								this.labelPrintingUtil.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_SEED_LOT_ID)));
					} else {
						exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, ""));
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



}
