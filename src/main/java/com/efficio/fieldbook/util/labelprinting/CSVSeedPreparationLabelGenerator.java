package com.efficio.fieldbook.util.labelprinting;

import java.io.ByteArrayOutputStream;
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

		mainSelectedFields = this.appendBarcode(isBarcodeNeeded, mainSelectedFields);

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
				this.generateColumnHeaders(selectedFieldIDs, labelHeaders);
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
								buffer.append(this.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID));
							} else {
								buffer.append(" ");
							}
						} else if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
							if (lotRows != null) {
								buffer.append(this.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_SEED_LOT_ID));
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
								this.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID)));
					} else {
						exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, ""));
					}
				} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
					// Lot ID
					if (lotRows != null) {
						exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId,
								this.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_SEED_LOT_ID)));
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

	/**
	 * Iterate trough all the lotRows and construct coma separated string of ids (lotIds or stockIds)
	 *
	 * @param lotRows the inventory details
	 * @param listType the type of list to return. Available values: AVAILABLE_LABEL_SEED_LOT_ID, AVAILABLE_LABEL_FIELDS_STOCK_ID
	 * @return comma separated string of ids (lotIds or stockIds)
	 */
	//TODO move to utility class
	private String getListOfIDs(final List<ListEntryLotDetails> lotRows, final AppConstants listType) {
		String listIds = "";
		for (final ListEntryLotDetails lotDetails : lotRows) {
			if (listType.equals(AppConstants.AVAILABLE_LABEL_SEED_LOT_ID)){
				listIds += lotDetails.getLotId() == null ? "" : lotDetails.getLotId().toString();
			} else if (listType.equals(AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID)) {
				listIds += lotDetails.getStockIds() == null ? "" : lotDetails.getStockIds();
			} else {
				throw new IllegalArgumentException("No such type of list. The lists available are: lotId and stockId");
			}
			listIds += ", ";
		}
		// remove the trailing ', ' symbols if they were appended
		return listIds.length() > 2 ? listIds.substring(0, listIds.length() - 2) : listIds;
	}

	//TODO move to utility class
	private List<ExportColumnHeader> generateColumnHeaders(final List<Integer> selectedFieldIDs, final Map<Integer, String> labelHeaders) {
		final List<ExportColumnHeader> columnHeaders = new ArrayList<>();

		for (final Integer selectedFieldID : selectedFieldIDs) {
			final String headerName = this.getColumnHeader(selectedFieldID, labelHeaders);
			final ExportColumnHeader columnHeader = new ExportColumnHeader(selectedFieldID, headerName, true);
			columnHeaders.add(columnHeader);
		}

		return columnHeaders;
	}

	//TODO move to utility class
	private String appendBarcode(final boolean isBarcodeNeeded, final String mainSelectedFields) {
		String processed = mainSelectedFields;
		if (isBarcodeNeeded) {
			processed += "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt();
		}
		return processed;
	}

	/**
	 * Gets the column header from the imported data
	 *
	 * @param headerID the header id
	 * @return the header
	 */
	//TODO move to utility class
	private String getColumnHeader(final Integer headerID, final Map<Integer, String> labelHeaders) {
		final Locale locale = LocaleContextHolder.getLocale();

		final StringBuilder buffer = new StringBuilder();

		try {
			if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.available.fields.entry.num", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.available.fields.gid", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.available.fields.germplasm.name", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
				buffer.append(
						this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
				buffer.append(
						this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
				buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null,
						locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
				buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY, null,
						locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.available.fields.rep", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()) {
				buffer.append(
						this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
				buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY, null,
						locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()) {
				buffer.append(
						this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()) {
				buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null,
						locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
				buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY,
						null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
				buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY, null,
						locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.available.fields.barcode", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.seed.inventory.amount", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.seed.inventory.scale", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale));
			} else {
				String headerName = labelHeaders.get(headerID);
				if (headerName == null) {
					headerName = "";
				}

				buffer.append(headerName);

			}

			return buffer.toString();
		} catch (final NumberFormatException e) {
			// TODO throw proper exception
			LOG.error(e.getMessage(), e);
			return "";
		}
	}

}
