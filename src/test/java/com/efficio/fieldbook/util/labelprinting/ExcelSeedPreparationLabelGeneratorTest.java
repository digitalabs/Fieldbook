package com.efficio.fieldbook.util.labelprinting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.mockito.junit.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ExcelSeedPreparationLabelGeneratorTest {

	@Mock
	private LabelPrintingUtil labelPrintingUtil;

	@Mock
	private UserLabelPrinting userLabelPrinting;

	@InjectMocks
	private ExcelSeedPreparationLabelGenerator labelGenerator;

	@Test
	public void testGenerateLabels() throws LabelPrintingException {
		Mockito.when(this.userLabelPrinting.getMainSelectedLabelFields()).thenReturn("GID");
		Mockito.when(this.userLabelPrinting.getIncludeColumnHeadinginNonPdf()).thenReturn("0");
		Mockito.when(this.userLabelPrinting.getBarcodeNeeded()).thenReturn("0");
		Mockito.when(this.userLabelPrinting.getFilenameDLLocation()).thenReturn("Filename");
		Mockito.when(this.labelPrintingUtil.appendBarcode(Matchers.anyBoolean(), Matchers.anyString()))
				.thenReturn("GID");

		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(1);
		germplasmListData.getInventoryInfo().setLotRows(lotRows);
		final List<GermplasmListData> germplasmListDataList = new ArrayList<>(Arrays.asList(germplasmListData));
		final String filename = this.labelGenerator.generateLabels(germplasmListDataList, this.userLabelPrinting);

		Assert.assertEquals("Filename", filename);
		Mockito.verify(this.userLabelPrinting).getMainSelectedLabelFields();
		Mockito.verify(this.userLabelPrinting).getIncludeColumnHeadinginNonPdf();
		Mockito.verify(this.userLabelPrinting).getBarcodeNeeded();
		Mockito.verify(this.userLabelPrinting).getFilenameDLLocation();
		Mockito.verify(this.labelPrintingUtil).appendBarcode(Matchers.anyBoolean(), Matchers.anyString());
	}
}
