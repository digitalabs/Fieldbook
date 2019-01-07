package com.efficio.fieldbook.util.labelprinting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.middleware.data.initializer.GermplasmListDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.ListEntryLotDetailsTestDataInitializer;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class CSVSeedPreparationLabelGeneratorTest {

	@Mock
	private GermplasmExportService germplasmExportService;

	@Mock
	private LabelPrintingUtil labelPrintingUtil;

	@Mock
	private UserLabelPrinting userLabelPrinting;

	@InjectMocks
	private CSVSeedPreparationLabelGenerator labelGenerator;

	@Test
	public void testGenerateLabels() throws LabelPrintingException {
		Mockito.when(this.userLabelPrinting.getFilenameDLLocation()).thenReturn("Filename");
		Mockito.when(this.userLabelPrinting.getMainSelectedLabelFields()).thenReturn("GID");
		Mockito.when(this.userLabelPrinting.getIncludeColumnHeadinginNonPdf()).thenReturn("0");
		Mockito.when(this.userLabelPrinting.getBarcodeNeeded()).thenReturn("0");
		Mockito.when(this.labelPrintingUtil.appendBarcode(Matchers.anyBoolean(), Matchers.anyString()))
				.thenReturn("GID");
		Mockito.when(this.labelPrintingUtil.getLabelHeadersForSeedPreparation(Matchers.anyListOf(Integer.class)))
				.thenReturn(new HashMap<Integer, String>());
		Mockito.when(this.labelPrintingUtil.generateColumnHeaders(Matchers.anyListOf(Integer.class), Matchers.anyMapOf(Integer.class,
				String.class)))
				.thenReturn(new ArrayList<ExportColumnHeader>());

		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(1);
		germplasmListData.getInventoryInfo().setLotRows(lotRows);
		final List<GermplasmListData> germplasmListDataList = new ArrayList<>(Arrays.asList(germplasmListData));
		final String filename = this.labelGenerator.generateLabels(germplasmListDataList, this.userLabelPrinting);

		Assert.assertEquals("Filename", filename);
		Mockito.verify(this.userLabelPrinting).getFilenameDLLocation();
		Mockito.verify(this.userLabelPrinting).getMainSelectedLabelFields();
		Mockito.verify(this.userLabelPrinting).getIncludeColumnHeadinginNonPdf();
		Mockito.verify(this.userLabelPrinting).getBarcodeNeeded();
		Mockito.verify(this.labelPrintingUtil).appendBarcode(Matchers.anyBoolean(), Matchers.anyString());
		Mockito.verify(this.labelPrintingUtil).getLabelHeadersForSeedPreparation(Matchers.anyListOf(Integer.class));
		Mockito.verify(this.labelPrintingUtil).generateColumnHeaders(Matchers.anyListOf(Integer.class), Matchers.anyMapOf(Integer.class, String.class));
	}
}
