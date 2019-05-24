package com.efficio.fieldbook.util.labelprinting;

import java.io.FileOutputStream;
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
import com.efficio.fieldbook.web.label.printing.template.LabelPaper;
import org.generationcp.commons.constant.AppConstants;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class PDFSeedPreparationLabelGeneratorTest {

	@Mock
	private UserLabelPrinting userLabelPrinting;

	@Mock
	private LabelPrintingPDFUtil labelPrintingPDFUtil;

	@Mock
	private LabelPrintingUtil labelPrintingUtil;

	@InjectMocks
	private PDFSeedPreparationLabelGenerator labelGenerator;

	@Test
	public void testGenerateLabels() throws LabelPrintingException, DocumentException {
		Mockito.when(this.userLabelPrinting.getSizeOfLabelSheet()).thenReturn("1");
		Mockito.when(this.userLabelPrinting.getNumberOfLabelPerRow()).thenReturn("1");
		Mockito.when(this.userLabelPrinting.getNumberOfRowsPerPageOfLabel()).thenReturn("5");
		Mockito.when(this.userLabelPrinting.getLeftSelectedLabelFields())
				.thenReturn(AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getString());
		Mockito.when(this.userLabelPrinting.getRightSelectedLabelFields())
				.thenReturn(AppConstants.AVAILABLE_LABEL_FIELDS_GID.getString());
		Mockito.when(this.userLabelPrinting.getBarcodeNeeded()).thenReturn("false");
		Mockito.when(this.userLabelPrinting.getFilenameDLLocation()).thenReturn("Filename");
		Mockito.when(this.labelPrintingPDFUtil.getDocument(Matchers.any(FileOutputStream.class),
				Matchers.any(LabelPaper.class), Matchers.anyInt())).thenReturn(Mockito.mock(Document.class));
		Mockito.when(this.labelPrintingPDFUtil.getWidthColumns(Matchers.anyInt(), Matchers.anyFloat()))
				.thenReturn(new float[] { LabelPrintingPDFUtil.COLUMN_WIDTH_SIZE });
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(1);
		germplasmListData.getInventoryInfo().setLotRows(lotRows);
		final List<GermplasmListData> germplasmListDataList = new ArrayList<>(Arrays.asList(germplasmListData));
		final String filename = this.labelGenerator.generateLabels(germplasmListDataList, this.userLabelPrinting);

		Assert.assertEquals("Filename", filename);
		Mockito.verify(this.userLabelPrinting).getSizeOfLabelSheet();
		Mockito.verify(this.userLabelPrinting).getNumberOfLabelPerRow();
		Mockito.verify(this.userLabelPrinting).getNumberOfRowsPerPageOfLabel();
		Mockito.verify(this.userLabelPrinting).getLeftSelectedLabelFields();
		Mockito.verify(this.userLabelPrinting).getRightSelectedLabelFields();
		Mockito.verify(this.userLabelPrinting).getBarcodeNeeded();
		Mockito.verify(this.userLabelPrinting).getFilenameDLLocation();
		Mockito.verify(this.labelPrintingPDFUtil).getDocument(Matchers.any(FileOutputStream.class),
				Matchers.any(LabelPaper.class), Matchers.anyInt());
		Mockito.verify(this.labelPrintingPDFUtil).getWidthColumns(Matchers.anyInt(), Matchers.anyFloat());
	}

	@Test
	public void testGenerateBarcodeLabel() {
		final GermplasmListData germplasmListData = GermplasmListDataTestDataInitializer
				.createGermplasmListDataWithInventoryInfo(new GermplasmList(), 1, 1);
		final List<ListEntryLotDetails> lotRows = ListEntryLotDetailsTestDataInitializer
				.createListEntryLotDetailsList(1);
		germplasmListData.getInventoryInfo().setLotRows(lotRows);
		Mockito.when(this.labelPrintingUtil.getSelectedFieldValue(1, germplasmListData, this.userLabelPrinting,
				lotRows.get(0), true)).thenReturn("value");
		final String value = this.labelGenerator.generateBarcodeLabel(Arrays.asList(1), 0, germplasmListData,
				this.userLabelPrinting, lotRows.get(0));
		Assert.assertEquals("value", value);
	}
}
