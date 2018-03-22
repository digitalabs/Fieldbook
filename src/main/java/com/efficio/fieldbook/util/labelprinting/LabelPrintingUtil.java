package com.efficio.fieldbook.util.labelprinting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.util.labelprinting.comparators.GermplasmListDataDesignationComparator;
import com.efficio.fieldbook.util.labelprinting.comparators.GermplasmListDataEntryNumberComparator;
import com.efficio.fieldbook.util.labelprinting.comparators.GermplasmListDataGIDComparator;
import com.efficio.fieldbook.util.labelprinting.comparators.GermplasmListDataStockIdComparator;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.google.common.collect.Maps;

@Component
public class LabelPrintingUtil {

	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingUtil.class);
	/** The delimiter for the barcode. */
	private static final String DELIMITER = " | ";

	private static final String ENTRY = "entry";
	private static final String DESIGNATION = "designation";
	private static final String GID = "gid";
	private static final String STOCK_ID = "stockId";

	@Resource
	private MessageSource messageSource;

	/**
	 * Iterate trough all the lotRows and construct coma separated string of ids (lotIds or stockIds)
	 *
	 * @param lotRows the inventory details
	 * @param listType the type of list to return. Available values: AVAILABLE_LABEL_SEED_LOT_ID, AVAILABLE_LABEL_FIELDS_STOCK_ID
	 * @return comma separated string of ids (lotIds or stockIds)
	 */
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

	List<ExportColumnHeader> generateColumnHeaders(final List<Integer> selectedFieldIDs, final Map<Integer, String> labelHeaders) {
		final List<ExportColumnHeader> columnHeaders = new ArrayList<>();

		for (final Integer selectedFieldID : selectedFieldIDs) {
			final String headerName = this.getColumnHeader(selectedFieldID, labelHeaders);
			final ExportColumnHeader columnHeader = new ExportColumnHeader(selectedFieldID, headerName, true);
			columnHeaders.add(columnHeader);
		}

		return columnHeaders;
	}

	String appendBarcode(final boolean isBarcodeNeeded, final String mainSelectedFields) {
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
	String getColumnHeader(final Integer headerID, final Map<Integer, String> labelHeaders) {
		final Locale locale = LocaleContextHolder.getLocale();

		final StringBuilder buffer = new StringBuilder();

		try {

			if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.available.fields.list.name", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
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
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_STUDY_NAME.getInt()) {
				buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_STUDY_NAME_KEY, null,
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
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_SOURCE.getInt()) {
				buffer.append(this.messageSource.getMessage("label.printing.seed.inventory.source", null, locale));
			} else {
				String headerName = labelHeaders.get(headerID);
				if (headerName == null) {
					headerName = "";
				}

				buffer.append(headerName);
			}

			return buffer.toString();
		} catch (final NumberFormatException e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException("Could not find header id or it is not a number");
		}
	}



	/**
	 * This method returns retrieve Label headers to be printed in any of the trial instances having advance lines
	 * @param trialInstances list of StudyTrialInstanceInfo objects
	 * @return Map of label header with key as TermId and value as Label
	 */
	Map<Integer, String> getLabelHeadersFromTrialInstances(final List<StudyTrialInstanceInfo> trialInstances){
		Map<Integer, String> labelHeaders = Maps.newHashMap();

		for(final StudyTrialInstanceInfo trialInstanceInfo : trialInstances){
			if(trialInstanceInfo.getTrialInstance() != null && trialInstanceInfo.getTrialInstance().getLabelHeaders() != null
					&& !trialInstanceInfo.getTrialInstance().getLabelHeaders().isEmpty()){
				labelHeaders = trialInstanceInfo.getTrialInstance().getLabelHeaders();
				break;
			}
		}

		return labelHeaders;
	}

	/**
	 * Generate barcode field.
	 *
	 * @param moreFieldInfo the more field info
	 * @param fieldMapLabel the field map label
	 * @param firstField the first field
	 * @param secondField the second field
	 * @param thirdField the third field
	 * @return the string
	 */
	 String generateBarcodeField(final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel, final String firstField,
			final String secondField, final String thirdField, final Map<Integer, String> labelHeaders, final boolean includeLabel) {
		final StringBuilder buffer = new StringBuilder();
		final String fieldList = firstField + "," + secondField + "," + thirdField;

		final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvertToListOfIDs(fieldList);

		for (final Integer selectedFieldID : selectedFieldIDs) {
			if (!"".equalsIgnoreCase(buffer.toString())) {
				buffer.append(DELIMITER);
			}

			buffer.append(this.getValueFromSpecifiedColumn(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, includeLabel));
		}

		return buffer.toString();
	}

	/**
	 * Returns the value of each field of the label data, based on the column required.
	 *
	 * @param moreFieldInfo further information relating to the field
	 * @param fieldMapLabel the field map label
	 * @param headerID the barcode label
	 * @return the value requested
	 */
	 String getValueFromSpecifiedColumn(final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel, final Integer
			headerID, final Map<Integer, String> labelHeaders, final boolean includeHeaderLabel) {
		final StringBuilder buffer = new StringBuilder();

		try {

			final String headerName = this.getColumnHeader(headerID, labelHeaders);

			if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
				buffer.append(fieldMapLabel.getEntryNumber());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
				final String gidTemp = fieldMapLabel.getGid() == null ? "" : fieldMapLabel.getGid().toString();
				buffer.append(gidTemp);
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
				buffer.append(fieldMapLabel.getGermplasmName());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
				buffer.append(fieldMapLabel.getStartYear());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
				buffer.append(fieldMapLabel.getSeason());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
				buffer.append(moreFieldInfo.get(LabelPrintingServiceImpl.SELECTED_NAME));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_STUDY_NAME.getInt()) {
				buffer.append(moreFieldInfo.get(LabelPrintingServiceImpl.SELECTED_NAME));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
				buffer.append(moreFieldInfo.get("trialInstanceNumber"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
				buffer.append(fieldMapLabel.getRep());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt() || headerID == TermId.LOCATION_ID.getId()) {
				buffer.append(moreFieldInfo.get("locationName"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
				buffer.append(moreFieldInfo.get("blockName"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()) {
				buffer.append(fieldMapLabel.getPlotNo());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()) {
				buffer.append(fieldMapLabel.getPedigree() == null ? "" : fieldMapLabel.getPedigree());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
				buffer.append(fieldMapLabel.getPlotCoordinate());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
				buffer.append(moreFieldInfo.get("fieldName"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
				buffer.append(moreFieldInfo.get(LabelPrintingServiceImpl.BARCODE));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt()) {
				buffer.append(fieldMapLabel.getInventoryAmount());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt()) {
				buffer.append(fieldMapLabel.getScaleName());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
				buffer.append(fieldMapLabel.getLotId());
			} else {
				final String value = fieldMapLabel.getUserFields().get(headerID);
				if (value != null) {
					buffer.append(value);
				}

			}

			String stemp = buffer.toString();
			if ("null".equalsIgnoreCase(stemp)) {
				stemp = " ";
			}

			if (includeHeaderLabel && headerName != null) {
				stemp = headerName + " : " + stemp;
			}

			return stemp;
		} catch (final NumberFormatException e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException("Could not find header id or it is not a number");
		}
	}

	Map<String, String> generateAddedInformationField(final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo,
			final StudyTrialInstanceInfo trialInstance, final String barCode) {
		final Map<String, String> moreFieldInfo = new HashMap<>();
		moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
		moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
		moreFieldInfo.put("fieldName", fieldMapTrialInstanceInfo.getFieldName());
		moreFieldInfo.put(LabelPrintingServiceImpl.SELECTED_NAME, trialInstance.getFieldbookName());
		moreFieldInfo.put("trialInstanceNumber", fieldMapTrialInstanceInfo.getTrialInstanceNo());
		moreFieldInfo.put(LabelPrintingServiceImpl.BARCODE, barCode);

		return moreFieldInfo;
	}

	void printHeaderFields(final Map<Integer, String> labelHeaders, final boolean includeHeader,
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

	Map<Integer, String> getLabelHeadersForSeedPreparation(final List<Integer> selectedFieldIDs) {
		final Locale locale = LocaleContextHolder.getLocale();
		final Map<Integer, String> labelHeaders = Maps.newHashMap();
		for (final Integer selectedFieldId : selectedFieldIDs) {
			if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getInt()) {
				labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.list.name", null, locale));
			} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
				labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.entry.num", null, locale));
			} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
				labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.gid", null, locale));
			} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt()) {
				labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.designation", null, locale));
			} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt()) {
				labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.cross", null, locale));
			} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt()) {
				labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.available.fields.stockid", null, locale));
			} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
				labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale));
			} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_SEED_SOURCE.getInt()) {
				labelHeaders.put(selectedFieldId, this.messageSource.getMessage("label.printing.seed.inventory.source", null, locale));
			}
		}
		return labelHeaders;
	}

	private Map<Integer, String> getAllLabelHeadersForSeedPreparation() {
		final Locale locale = LocaleContextHolder.getLocale();
		final Map<Integer, String> labelHeaders = Maps.newHashMap();
		labelHeaders.put(AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getInt(),
				this.messageSource.getMessage("label.printing.available.fields.list.name", null, locale));
		labelHeaders.put(AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt(),
				this.messageSource.getMessage("label.printing.available.fields.entry.num", null, locale));
		labelHeaders.put(AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt(),
				this.messageSource.getMessage("label.printing.available.fields.gid", null, locale));
		labelHeaders.put(AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt(),
				this.messageSource.getMessage("label.printing.available.fields.designation", null, locale));
		labelHeaders.put(AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt(),
				this.messageSource.getMessage("label.printing.available.fields.cross", null, locale));
		labelHeaders.put(AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt(),
				this.messageSource.getMessage("label.printing.available.fields.stockid", null, locale));
		labelHeaders.put(AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt(),
				this.messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale));
		labelHeaders.put(AppConstants.AVAILABLE_LABEL_SEED_SOURCE.getInt(),
				this.messageSource.getMessage("label.printing.seed.inventory.source", null, locale));
		return labelHeaders;
	}

	String getSelectedFieldValue(final int selectedFieldId, final GermplasmListData germplasmListData, final UserLabelPrinting
			userLabelPrinting) {
		return this.getSelectedFieldValue(selectedFieldId, germplasmListData, userLabelPrinting, false);
	}

	/**
	 * *********Seed Preparation extract values for label fields ***********
	 * @param selectedFieldId id of the field selected for printing
	 * @param germplasmListData data for the germplasm list
	 * @param userLabelPrinting data object with selected barcode fields
	 * @param includeHeaderLabel true for pdf printing, label header gets prepended before the value (e.g. "GID: 664968" instead of "664968")
	 * @return selected field value
	 */
	String getSelectedFieldValue(final int selectedFieldId, final GermplasmListData germplasmListData, final UserLabelPrinting
			userLabelPrinting, final boolean includeHeaderLabel) {

		@SuppressWarnings("unchecked")
		final List<ListEntryLotDetails> lotRows = (List<ListEntryLotDetails>) germplasmListData.getInventoryInfo().getLotRows();

		final StringBuilder selectedValueFieldBuffer = new StringBuilder();

		if (includeHeaderLabel) {
			final String headerName = this.getColumnHeader(selectedFieldId, this.getAllLabelHeadersForSeedPreparation());
			selectedValueFieldBuffer.append(headerName).append(" : ");
		}

		if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getInt()) {
			selectedValueFieldBuffer.append(userLabelPrinting.getName());
		} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
			selectedValueFieldBuffer.append(germplasmListData.getEntryId());
		} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
			// GID
			selectedValueFieldBuffer.append(germplasmListData.getGid().toString());
		} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
			// barcode Label
			selectedValueFieldBuffer.append(this.getBarcodeString(germplasmListData, userLabelPrinting));
		} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_DESIGNATION.getInt()) {
			//Designation
			selectedValueFieldBuffer.append(germplasmListData.getDesignation());
		} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_CROSS.getInt()) {
			// Cross
			selectedValueFieldBuffer.append(germplasmListData.getGroupName());
		} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID.getInt()) {
			// Stock ID
			if (lotRows != null) {
				selectedValueFieldBuffer.append(this.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_FIELDS_STOCK_ID));
			} else {
				selectedValueFieldBuffer.append("");
			}
		} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
			// Lot ID
			if (lotRows != null) {
				selectedValueFieldBuffer.append(this.getListOfIDs(lotRows, AppConstants.AVAILABLE_LABEL_SEED_LOT_ID));
			} else {
				selectedValueFieldBuffer.append("");
			}
		} else if (selectedFieldId == AppConstants.AVAILABLE_LABEL_SEED_SOURCE.getInt()) {
			// Seed Source
			selectedValueFieldBuffer.append(germplasmListData.getSeedSource());
		}

		return selectedValueFieldBuffer.toString();
	}

	String getBarcodeString(final GermplasmListData germplasmListData, final UserLabelPrinting userLabelPrinting) {
		return this.getBarcodeString(germplasmListData, userLabelPrinting, false);
	}

	String getBarcodeString(final GermplasmListData germplasmListData, final UserLabelPrinting userLabelPrinting, final boolean
			includeHeaders) {

		@SuppressWarnings("unchecked")
		final List<ListEntryLotDetails> lotRows = (List<ListEntryLotDetails>) germplasmListData.getInventoryInfo().getLotRows();

		final StringBuilder buffer = new StringBuilder();

		final String fieldList = userLabelPrinting.getFirstBarcodeField() + "," + userLabelPrinting.getSecondBarcodeField() + "," + userLabelPrinting.getThirdBarcodeField();

		final List<Integer> selectedBarcodeFieldIDs = SettingsUtil.parseFieldListAndConvertToListOfIDs(fieldList);

		for (final Integer selectedBarcodeFieldID : selectedBarcodeFieldIDs) {

			if (!"".equalsIgnoreCase(buffer.toString())) {
				buffer.append(DELIMITER);
			}

			if (includeHeaders) {
				final String headerName = this.getColumnHeader(selectedBarcodeFieldID, this.getAllLabelHeadersForSeedPreparation());
				buffer.append(headerName).append(" : ");
			}

			if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_FIELDS_LIST_NAME.getInt()) {
				buffer.append(userLabelPrinting.getName());
			} else if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
				buffer.append(germplasmListData.getEntryId());
			} else if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
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
			} else if (selectedBarcodeFieldID == AppConstants.AVAILABLE_LABEL_SEED_SOURCE.getInt()) {
				// Seed Source
				buffer.append(germplasmListData.getSeedSource());
			}
		}
		return buffer.toString();
	}

	/**
	 * Sort seed preparation levels by 1 of the 4 ways (by Entry number, Designation, GID, StockId)
	 * @param fullGermplasmListWithExistingReservations collection to sort
	 * @param sortingType how the collection should be sorted
	 */
	public void sortGermplasmListDataList(final List<GermplasmListData> fullGermplasmListWithExistingReservations, final String sortingType)
			throws LabelPrintingException {
		if (sortingType.equalsIgnoreCase(ENTRY)) {
			this.sortByEntry(fullGermplasmListWithExistingReservations);
		} else if (sortingType.equalsIgnoreCase(DESIGNATION)) {
			this.sortByDesignation(fullGermplasmListWithExistingReservations);
		} else if (sortingType.equalsIgnoreCase(GID)) {
			this.sortByGID(fullGermplasmListWithExistingReservations);
		} else if (sortingType.equalsIgnoreCase(STOCK_ID)) {
			this.sortByStockId(fullGermplasmListWithExistingReservations);
		} else {
			throw new LabelPrintingException("No such type of sorting defined");
		}
	}

	private List<GermplasmListData> sortByStockId(final List<GermplasmListData> fullGermplasmListWithExistingReservations) {
		final GermplasmListDataStockIdComparator comparator = new GermplasmListDataStockIdComparator();
		Collections.sort(fullGermplasmListWithExistingReservations, comparator);
		return fullGermplasmListWithExistingReservations;
	}

	private List<GermplasmListData> sortByGID(final List<GermplasmListData> fullGermplasmListWithExistingReservations) {
		final GermplasmListDataGIDComparator comparator = new GermplasmListDataGIDComparator();
		Collections.sort(fullGermplasmListWithExistingReservations, comparator);
		return fullGermplasmListWithExistingReservations;
	}

	private List<GermplasmListData> sortByDesignation(final List<GermplasmListData> fullGermplasmListWithExistingReservations) {
		final GermplasmListDataDesignationComparator comparator = new GermplasmListDataDesignationComparator();
		Collections.sort(fullGermplasmListWithExistingReservations, comparator);
		return fullGermplasmListWithExistingReservations;
	}

	private List<GermplasmListData> sortByEntry(final List<GermplasmListData> fullGermplasmListWithExistingReservations) {
		final GermplasmListDataEntryNumberComparator comparator = new GermplasmListDataEntryNumberComparator();
		Collections.sort(fullGermplasmListWithExistingReservations, comparator);
		return fullGermplasmListWithExistingReservations;
	}

}
