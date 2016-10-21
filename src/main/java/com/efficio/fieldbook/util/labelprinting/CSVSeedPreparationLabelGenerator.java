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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.google.common.collect.Maps;

@Component
public class CSVSeedPreparationLabelGenerator implements LabelGenerator {

	@Resource
	private GermplasmExportService germplasmExportService;
	@Resource
	private LabelPrintingUtil labelPrintingUtil;

	private static final Logger LOG = LoggerFactory.getLogger(CSVSeedPreparationLabelGenerator.class);

	@Override
	public String generateLabels (final List<?> dataList, final UserLabelPrinting
			userLabelPrinting) throws LabelPrintingException {

		@SuppressWarnings("unchecked")
		final List<GermplasmListData> germplasmListDataList = (List<GermplasmListData>) dataList;

		final String fileName = userLabelPrinting.getFilenameDLLocation();
		String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
		final boolean includeHeader =
				LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
		final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());

		mainSelectedFields = this.labelPrintingUtil.appendBarcode(isBarcodeNeeded, mainSelectedFields);

		final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvertToListOfIDs(mainSelectedFields);

		//Label Headers
		final Map<Integer, String> labelHeaders = this.labelPrintingUtil.getLabelHeadersForSeedPreparation(selectedFieldIDs);

		final List<ExportColumnHeader> exportColumnHeaders =
				this.labelPrintingUtil.generateColumnHeaders(selectedFieldIDs, labelHeaders);
		final List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

		// Values in the columns
		for (final GermplasmListData germplasmListData : germplasmListDataList){
			final Map<Integer, ExportColumnValue> exportColumnValueMap = Maps.newHashMap();

			for (final Integer selectedFieldId : selectedFieldIDs) {
				exportColumnValueMap.put(selectedFieldId, new ExportColumnValue(selectedFieldId, this.labelPrintingUtil
						.getSelectedFieldValue(selectedFieldId, germplasmListData, userLabelPrinting)));
			}

			exportColumnValues.add(exportColumnValueMap);
		}

		try {
			this.germplasmExportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName, includeHeader);
		} catch (final IOException e) {
			LOG.debug(e.getMessage());
			throw new LabelPrintingException(e);
		}

		return fileName;
	}

}
