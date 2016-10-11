package com.efficio.fieldbook.util.labelprinting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.util.AppConstants;

@Component
class LabelPrintingUtil {

	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingUtil.class);

	@Resource
	private MessageSource messageSource;

	/**
	 * Iterate trough all the lotRows and construct coma separated string of ids (lotIds or stockIds)
	 *
	 * @param lotRows the inventory details
	 * @param listType the type of list to return. Available values: AVAILABLE_LABEL_SEED_LOT_ID, AVAILABLE_LABEL_FIELDS_STOCK_ID
	 * @return comma separated string of ids (lotIds or stockIds)
	 */
	String getListOfIDs(final List<ListEntryLotDetails> lotRows, final AppConstants listType) {
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
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException("Could not find header id or it is not a number");
		}
	}

}
