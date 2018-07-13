package com.efficio.fieldbook.util.labelprinting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.google.common.collect.Maps;

@Component
public class CSVSeedPreparationLabelGenerator implements SeedPreparationLabelGenerator {

	@Resource
	private GermplasmExportService germplasmExportService;
	@Resource
	private LabelPrintingUtil labelPrintingUtil;

	private static final Logger LOG = LoggerFactory.getLogger(CSVSeedPreparationLabelGenerator.class);

	@Override
	public String generateLabels(final List<GermplasmListData> germplasmListDataList,
			final UserLabelPrinting userLabelPrinting) throws LabelPrintingException {

		final String fileName = userLabelPrinting.getFilenameDLLocation();
		String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
		final boolean includeHeader = LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS
				.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
		final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED
				.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());

		mainSelectedFields = this.labelPrintingUtil.appendBarcode(isBarcodeNeeded, mainSelectedFields);

		final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvertToListOfIDs(mainSelectedFields);

		// Label Headers
		final Map<Integer, String> labelHeaders = this.labelPrintingUtil
				.getLabelHeadersForSeedPreparation(selectedFieldIDs);

		final List<ExportColumnHeader> exportColumnHeaders = this.labelPrintingUtil
				.generateColumnHeaders(selectedFieldIDs, labelHeaders);
		final List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

		// Values in the columns
		final Map<Integer, Boolean> printedGermplasmListDataMap = new HashMap<>();
		for (final GermplasmListData germplasmListData : germplasmListDataList) {
			if (printedGermplasmListDataMap.get(germplasmListData.getGid()) != null) {
				continue;
			}

			@SuppressWarnings("unchecked")
			final List<ListEntryLotDetails> lotRows = (List<ListEntryLotDetails>) germplasmListData.getInventoryInfo()
					.getLotRows();
			for (final ListEntryLotDetails lotRow : lotRows) {
				if (!lotRow.getWithdrawalStatus().equalsIgnoreCase(GermplasmInventory.RESERVED)) {
					continue;
				}
				final Map<Integer, ExportColumnValue> exportColumnValueMap = Maps.newHashMap();

				for (final Integer selectedFieldId : selectedFieldIDs) {
					exportColumnValueMap.put(selectedFieldId,
							new ExportColumnValue(selectedFieldId, this.labelPrintingUtil.getSelectedFieldValue(
									selectedFieldId, germplasmListData, userLabelPrinting, lotRow)));
				}
				exportColumnValues.add(exportColumnValueMap);
			}
			printedGermplasmListDataMap.put(germplasmListData.getGid(), true);
		}

		try {
			this.germplasmExportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName,
					includeHeader);
		} catch (final IOException e) {
			CSVSeedPreparationLabelGenerator.LOG.debug(e.getMessage());
			throw new LabelPrintingException(e);
		}

		return fileName;
	}

}
