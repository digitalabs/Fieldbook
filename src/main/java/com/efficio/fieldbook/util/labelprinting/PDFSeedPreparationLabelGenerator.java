package com.efficio.fieldbook.util.labelprinting;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.util.LabelPaperFactory;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.template.LabelPaper;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

@Component
class PDFSeedPreparationLabelGenerator implements SeedPreparationLabelGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(PDFSeedPreparationLabelGenerator.class);

	@Resource
	private LabelPrintingPDFUtil labelPrintingPDFUtil;

	@Resource
	private LabelPrintingUtil labelPrintingUtil;

	@Override
	public String generateLabels(final List<GermplasmListData> germplasmListDataList,
			final UserLabelPrinting userLabelPrinting) throws LabelPrintingException {

		final int pageSizeId = Integer.parseInt(userLabelPrinting.getSizeOfLabelSheet());
		final int numberOfLabelPerRow = Integer.parseInt(userLabelPrinting.getNumberOfLabelPerRow());
		final int numberOfRowsPerPageOfLabel = Integer.parseInt(userLabelPrinting.getNumberOfRowsPerPageOfLabel());
		final int totalPerPage = numberOfLabelPerRow * numberOfRowsPerPageOfLabel;
		final String leftSelectedFields = userLabelPrinting.getLeftSelectedLabelFields();
		final String rightSelectedFields = userLabelPrinting.getRightSelectedLabelFields();
		final String barcodeNeeded = userLabelPrinting.getBarcodeNeeded();

		final String fileName = userLabelPrinting.getFilenameDLLocation();

		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			final LabelPaper paper = LabelPaperFactory.generateLabelPaper(numberOfLabelPerRow,
					numberOfRowsPerPageOfLabel, pageSizeId);
			final Document document = this.labelPrintingPDFUtil.getDocument(fileOutputStream, paper, pageSizeId);

			int i = 0;
			final int fixTableRowSize = numberOfLabelPerRow;
			final float[] widthColumns = this.labelPrintingPDFUtil.getWidthColumns(fixTableRowSize,
					LabelPrintingPDFUtil.COLUMN_WIDTH_SIZE);

			PdfPTable table = new PdfPTable(fixTableRowSize);
			table.setWidths(widthColumns);
			table.setWidthPercentage(100);

			final List<File> filesToBeDeleted = new ArrayList<File>();
			final float cellHeight = paper.getCellHeight();

			final Map<Integer, Boolean> printedGermplasmListDataMap = new HashMap<>();
			for (final GermplasmListData germplasmListData : germplasmListDataList) {
				if (printedGermplasmListDataMap.get(germplasmListData.getGid()) != null) {
					continue;
				}
				@SuppressWarnings("unchecked")
				final List<ListEntryLotDetails> lotRows = (List<ListEntryLotDetails>) germplasmListData
						.getInventoryInfo().getLotRows();
				for (final ListEntryLotDetails lotRow : lotRows) {
					if (!lotRow.getWithdrawalStatus().equalsIgnoreCase(GermplasmInventory.RESERVED)) {
						continue;
					}
					i++;
					String barcodeLabelForCode = " ";
					String barcodeLabel = " ";

					if (LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(barcodeNeeded)) {
						barcodeLabel = this.labelPrintingUtil.getBarcodeStringForSeedPrep(germplasmListData,
								userLabelPrinting, false, lotRow);
						barcodeLabelForCode = this.labelPrintingUtil.getBarcodeStringForSeedPrep(germplasmListData,
								userLabelPrinting, true, lotRow);
					}

					barcodeLabelForCode = this.labelPrintingPDFUtil.truncateBarcodeLabelForCode(barcodeLabelForCode);

					final Image mainImage = this.labelPrintingPDFUtil.getBarcodeImage(filesToBeDeleted,
							barcodeLabelForCode);

					final PdfPCell cell = new PdfPCell();
					cell.setFixedHeight(cellHeight);
					cell.setNoWrap(false);
					cell.setPadding(5f);
					cell.setPaddingBottom(1f);

					final PdfPTable innerImageTableInfo = new PdfPTable(1);
					innerImageTableInfo.setWidths(new float[] { 1 });
					innerImageTableInfo.setWidthPercentage(82);
					final PdfPCell cellImage = new PdfPCell();
					if (LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(barcodeNeeded)) {
						cellImage.addElement(mainImage);
					} else {
						cellImage.addElement(new Paragraph(" "));
					}
					cellImage.setBorder(Rectangle.NO_BORDER);
					cellImage.setBackgroundColor(Color.white);
					cellImage.setPadding(1.5f);

					innerImageTableInfo.addCell(cellImage);

					final float fontSize = paper.getFontSize();

					final BaseFont unicode = BaseFont.createFont(LabelPrintingPDFUtil.ARIAL_UNI, BaseFont.IDENTITY_H,
							BaseFont.EMBEDDED);
					final com.lowagie.text.Font fontNormal = new com.lowagie.text.Font(unicode, fontSize);
					fontNormal.setStyle(com.lowagie.text.Font.NORMAL);

					cell.addElement(innerImageTableInfo);
					cell.addElement(new Paragraph());
					for (int row = 0; row < 5; row++) {
						if (row == 0) {
							final PdfPTable innerDataTableInfo = new PdfPTable(1);
							innerDataTableInfo.setWidths(new float[] { 1 });
							innerDataTableInfo.setWidthPercentage(85);

							final com.lowagie.text.Font fontNormalData = new com.lowagie.text.Font(unicode, 5.0f);
							fontNormal.setStyle(com.lowagie.text.Font.NORMAL);

							final PdfPCell cellInnerData = new PdfPCell(new Phrase(barcodeLabel, fontNormalData));

							cellInnerData.setBorder(Rectangle.NO_BORDER);
							cellInnerData.setBackgroundColor(Color.white);
							cellInnerData.setPaddingBottom(0.2f);
							cellInnerData.setPaddingTop(0.2f);
							cellInnerData.setHorizontalAlignment(Element.ALIGN_MIDDLE);

							innerDataTableInfo.addCell(cellInnerData);
							innerDataTableInfo.setHorizontalAlignment(Element.ALIGN_MIDDLE);
							cell.addElement(innerDataTableInfo);
						}
						final PdfPTable innerTableInfo = new PdfPTable(2);
						innerTableInfo.setWidths(new float[] { 1, 1 });
						innerTableInfo.setWidthPercentage(85);
						final List<Integer> leftSelectedFieldIDs = SettingsUtil
								.parseFieldListAndConvertToListOfIDs(leftSelectedFields);
						final String leftText = this.generateBarcodeLabel(leftSelectedFieldIDs, row, germplasmListData,
								userLabelPrinting, lotRow);
						final PdfPCell cellInnerLeft = new PdfPCell(new Paragraph(leftText, fontNormal));

						cellInnerLeft.setBorder(Rectangle.NO_BORDER);
						cellInnerLeft.setBackgroundColor(Color.white);
						cellInnerLeft.setPaddingBottom(0.5f);
						cellInnerLeft.setPaddingTop(0.5f);

						innerTableInfo.addCell(cellInnerLeft);

						final List<Integer> rightSelectedFieldIDs = SettingsUtil
								.parseFieldListAndConvertToListOfIDs(rightSelectedFields);
						final String rightText = this.generateBarcodeLabel(rightSelectedFieldIDs, row,
								germplasmListData, userLabelPrinting, lotRow);
						final PdfPCell cellInnerRight = new PdfPCell(new Paragraph(rightText, fontNormal));

						cellInnerRight.setBorder(Rectangle.NO_BORDER);
						cellInnerRight.setBackgroundColor(Color.white);
						cellInnerRight.setPaddingBottom(0.5f);
						cellInnerRight.setPaddingTop(0.5f);

						innerTableInfo.addCell(cellInnerRight);

						cell.addElement(innerTableInfo);
					}

					cell.setBorder(Rectangle.NO_BORDER);
					cell.setBackgroundColor(Color.white);

					table.addCell(cell);

					if (i % numberOfLabelPerRow == 0) {
						// we go the next line
						final int needed = fixTableRowSize - numberOfLabelPerRow;

						for (int neededCount = 0; neededCount < needed; neededCount++) {
							final PdfPCell cellNeeded = new PdfPCell();

							cellNeeded.setBorder(Rectangle.NO_BORDER);
							cellNeeded.setBackgroundColor(Color.white);

							table.addCell(cellNeeded);
						}

						table.completeRow();
						if (numberOfRowsPerPageOfLabel == 10) {
							table.setSpacingAfter(paper.getSpacingAfter());
						}

						document.add(table);

						table = new PdfPTable(fixTableRowSize);
						table.setWidths(widthColumns);
						table.setWidthPercentage(100);

					}

					if (i % totalPerPage == 0) {
						// we go the next page
						document.newPage();
					}
					printedGermplasmListDataMap.put(germplasmListData.getGid(), true);
				}
			}
			// we need to add the last row
			this.labelPrintingPDFUtil.addLastRow(numberOfLabelPerRow, numberOfRowsPerPageOfLabel, paper, document, i,
					fixTableRowSize, table, widthColumns);

			document.close();

			for (final File file : filesToBeDeleted) {
				file.delete();
			}

			fileOutputStream.close();

		} catch (final Exception e) {
			PDFSeedPreparationLabelGenerator.LOG.error(e.getMessage(), e);
			throw new LabelPrintingException(e.getMessage());
		}

		return fileName;
	}

	String generateBarcodeLabel(final List<Integer> selectedFieldIDs, final int rowNumber,
			final GermplasmListData germplasmListData, final UserLabelPrinting userLabelPrinting,
			final ListEntryLotDetails lotRow) {
		int i = 0;

		for (final Integer selectedFieldID : selectedFieldIDs) {
			if (i == rowNumber) {
				return this.labelPrintingUtil.getSelectedFieldValue(selectedFieldID, germplasmListData,
						userLabelPrinting, lotRow, true);
			}
			i++;
		}

		return "";
	}
}
