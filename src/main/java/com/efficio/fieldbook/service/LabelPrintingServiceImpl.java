/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.LabelPaperFactory;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.template.LabelPaper;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The Class LabelPrintingServiceImpl.
 */
@Service
@Transactional
public class LabelPrintingServiceImpl implements LabelPrintingService {

	private static final String ADVANCED = "ADVANCED";
	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceImpl.class);
	public static final String BARCODE = "barcode";
	public static final String SELECTED_NAME = "selectedName";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY = "label.printing.available.fields.field.name";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY = "label.printing.available.fields.plot";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY = "label.printing.available.fields.parentage";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY = "label.printing.available.fields.plot.coordinates";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY = "label.printing.available.fields.year";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY = "label.printing.available.fields.season";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY = "label.printing.available.fields.nursery.name";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY = "label.printing.available.fields.trial.name";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY = "label.printing.available.fields.location";
	public static final String LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY = "label.printing.available.fields.block.name";

	public static final Integer[] BASE_LABEL_PRINTING_FIELD_IDS = new Integer[] {AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt(),
			AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(),
			AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt(), AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()};

	public static final Integer[] BASE_LABEL_PRINTING_FIELD_MAP_LABEL_IDS =
			new Integer[] {AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(),
					AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(), AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()};
	public static final String INCLUDE_NON_PDF_HEADERS = "1";
	public static final String BARCODE_NEEDED = "1";

	/** The delimiter. */
	private final String delimiter = " | ";

	/** The message source. */
	@Resource
	private MessageSource messageSource;

	@Resource
	private GermplasmExportService germplasmExportService;

	@Resource
	private WorkbenchService workbenchService;

	@Resource
	private PresetDataManager presetDataManager;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private SettingsService settingsService;

	@Resource
	protected FieldbookService fieldbookService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private InventoryService inventoryMiddlewareService;

	@Resource
	private PedigreeService pedigreeService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private OntologyDataManager ontologyDataManager;

	private static final String UNSUPPORTED_CHARSET_IMG = "unsupported-char-set.png";

	private static final String ARIAL_UNI = "arialuni.ttf";

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
	 */

	public LabelPrintingServiceImpl() {
		super();
	}

	protected BitMatrix encodeBarcode(final String barcodeLabelForCode, final int width, final int height) {
		BitMatrix bitMatrix = null;
		try {
			bitMatrix = new Code128Writer().encode(barcodeLabelForCode, BarcodeFormat.CODE_128, width, height, null);
		} catch (final WriterException e) {
			LabelPrintingServiceImpl.LOG.debug(e.getMessage(), e);
		} catch (final IllegalArgumentException e) {
			LabelPrintingServiceImpl.LOG.debug(e.getMessage(), e);
		}
		return bitMatrix;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
	 */
	@Override
	public String generatePDFLabels(final List<StudyTrialInstanceInfo> trialInstances, final UserLabelPrinting userLabelPrinting,
			final ByteArrayOutputStream baos) throws LabelPrintingException {

		final int pageSizeId = Integer.parseInt(userLabelPrinting.getSizeOfLabelSheet());
		final int numberOfLabelPerRow = Integer.parseInt(userLabelPrinting.getNumberOfLabelPerRow());
		final int numberofRowsPerPageOfLabel = Integer.parseInt(userLabelPrinting.getNumberOfRowsPerPageOfLabel());
		final int totalPerPage = numberOfLabelPerRow * numberofRowsPerPageOfLabel;
		final String leftSelectedFields = userLabelPrinting.getLeftSelectedLabelFields();
		final String rightSelectedFields = userLabelPrinting.getRightSelectedLabelFields();
		final String barcodeNeeded = userLabelPrinting.getBarcodeNeeded();

		final String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
		final String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
		final String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

		final String fileName = userLabelPrinting.getFilenameDLLocation();

		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(fileName);

			final LabelPaper paper = LabelPaperFactory.generateLabelPaper(numberOfLabelPerRow, numberofRowsPerPageOfLabel, pageSizeId);

			Rectangle pageSize = PageSize.LETTER;

			if (pageSizeId == AppConstants.SIZE_OF_PAPER_A4.getInt()) {
				pageSize = PageSize.A4;
			}

			final Document document = new Document(pageSize);

			// float marginLeft, float marginRight, float marginTop, float marginBottom
			document.setMargins(paper.getMarginLeft(), paper.getMarginRight(), paper.getMarginTop(), paper.getMarginBottom());

			PdfWriter.getInstance(document, fileOutputStream);

			// step 3
			document.open();

			int i = 0;
			final int fixTableRowSize = numberOfLabelPerRow;
			PdfPTable table = new PdfPTable(fixTableRowSize);

			final float columnWidthSize = 265f;
			final float[] widthColumns = new float[fixTableRowSize];

			for (int counter = 0; counter < widthColumns.length; counter++) {
				widthColumns[counter] = columnWidthSize;
			}

			table.setWidths(widthColumns);
			table.setWidthPercentage(100);
			final int width = 600;
			final int height = 75;

			final List<File> filesToBeDeleted = new ArrayList<File>();
			final float cellHeight = paper.getCellHeight();

			for (final StudyTrialInstanceInfo trialInstance : trialInstances) {
				final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();

				final Map<String, String> moreFieldInfo = this.generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");

				for (final FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {

					i++;
					String barcodeLabelForCode = "";
					String barcodeLabel = "";

					if (!LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(barcodeNeeded)) {
						barcodeLabel = " ";
						barcodeLabelForCode = " ";
					} else {
						barcodeLabel = this.generateBarcodeField(moreFieldInfo, fieldMapLabel, firstBarcodeField, secondBarcodeField,
								thirdBarcodeField, fieldMapTrialInstanceInfo.getLabelHeaders(), false);
						barcodeLabelForCode = this.generateBarcodeField(moreFieldInfo, fieldMapLabel, firstBarcodeField, secondBarcodeField,
								thirdBarcodeField, fieldMapTrialInstanceInfo.getLabelHeaders(), true);
					}

					if (barcodeLabelForCode != null && barcodeLabelForCode.length() > 80) {
						throw new LabelPrintingException("label.printing.label.too.long", barcodeLabelForCode,
								"label.printing.label.too.long");
					}

					Image mainImage = Image.getInstance(
							LabelPrintingServiceImpl.class.getClassLoader().getResource(LabelPrintingServiceImpl.UNSUPPORTED_CHARSET_IMG));
					FileOutputStream fout = null;

					final BitMatrix bitMatrix = this.encodeBarcode(barcodeLabelForCode, width, height);
					if (bitMatrix != null) {
						final String imageLocation = System.getProperty("user.home") + "/" + Math.random() + ".png";
						final File imageFile = new File(imageLocation);
						fout = new FileOutputStream(imageFile);
						MatrixToImageWriter.writeToStream(bitMatrix, "png", fout);
						filesToBeDeleted.add(imageFile);

						mainImage = Image.getInstance(imageLocation);
					}

					final PdfPCell cell = new PdfPCell();
					cell.setFixedHeight(cellHeight);
					cell.setNoWrap(false);
					cell.setPadding(5f);
					cell.setPaddingBottom(1f);

					final PdfPTable innerImageTableInfo = new PdfPTable(1);
					innerImageTableInfo.setWidths(new float[] {1});
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

					final BaseFont unicode =
							BaseFont.createFont(LabelPrintingServiceImpl.ARIAL_UNI, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
					final Font fontNormal = new Font(unicode, fontSize);
					fontNormal.setStyle(Font.NORMAL);

					cell.addElement(innerImageTableInfo);
					cell.addElement(new Paragraph());
					for (int row = 0; row < 5; row++) {
						if (row == 0) {
							final PdfPTable innerDataTableInfo = new PdfPTable(1);
							innerDataTableInfo.setWidths(new float[] {1});
							innerDataTableInfo.setWidthPercentage(85);

							final Font fontNormalData = new Font(unicode, 5.0f);
							fontNormal.setStyle(Font.NORMAL);

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
						innerTableInfo.setWidths(new float[] {1, 1});
						innerTableInfo.setWidthPercentage(85);
						final List<Integer> leftSelectedFieldIDs = SettingsUtil.parseFieldListAndConvert(leftSelectedFields);
						final String leftText = this.generateBarcodeLabel(moreFieldInfo, fieldMapLabel, leftSelectedFieldIDs,
								fieldMapTrialInstanceInfo.getLabelHeaders(), row);
						final PdfPCell cellInnerLeft = new PdfPCell(new Paragraph(leftText, fontNormal));

						cellInnerLeft.setBorder(Rectangle.NO_BORDER);
						cellInnerLeft.setBackgroundColor(Color.white);
						cellInnerLeft.setPaddingBottom(0.5f);
						cellInnerLeft.setPaddingTop(0.5f);

						innerTableInfo.addCell(cellInnerLeft);

						final List<Integer> rightSelectedFieldIDs = SettingsUtil.parseFieldListAndConvert(rightSelectedFields);
						final String rightText = this.generateBarcodeLabel(moreFieldInfo, fieldMapLabel, rightSelectedFieldIDs,
								fieldMapTrialInstanceInfo.getLabelHeaders(), row);
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
						if (numberofRowsPerPageOfLabel == 10) {
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
					if (fout != null) {
						fout.flush();
						fout.close();
					}

				}
			}
			// we need to add the last row
			if (i % numberOfLabelPerRow != 0) {
				// we go the next line

				final int remaining = numberOfLabelPerRow - i % numberOfLabelPerRow;
				for (int neededCount = 0; neededCount < remaining; neededCount++) {
					final PdfPCell cellNeeded = new PdfPCell();

					cellNeeded.setBorder(Rectangle.NO_BORDER);
					cellNeeded.setBackgroundColor(Color.white);

					table.addCell(cellNeeded);
				}

				table.completeRow();
				if (numberofRowsPerPageOfLabel == 10) {

					table.setSpacingAfter(paper.getSpacingAfter());
				}

				document.add(table);

				table = new PdfPTable(fixTableRowSize);
				table.setWidths(widthColumns);
				table.setWidthPercentage(100);

			}

			document.close();
			for (final File file : filesToBeDeleted) {
				file.delete();
			}
			fileOutputStream.close();

		} catch (final Exception e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		return fileName;
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
	private String generateBarcodeField(final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel, final String firstField,
			final String secondField, final String thirdField, final Map<Integer, String> labelHeaders, final boolean includeLabel) {
		final StringBuilder buffer = new StringBuilder();
		final String fieldList = firstField + "," + secondField + "," + thirdField;

		final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(fieldList);

		for (final Integer selectedFieldID : selectedFieldIDs) {
			if (!"".equalsIgnoreCase(buffer.toString())) {
				buffer.append(this.delimiter);
			}

			buffer.append(this.getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, includeLabel));
		}

		return buffer.toString();
	}

	/**
	 * Generate barcode label.
	 *
	 * @param moreFieldInfo the more field info
	 * @param fieldMapLabel the field map label
	 * @param selectedFieldIDs the selected fields
	 * @param rowNumber the row number
	 * @return the string
	 */
	private String generateBarcodeLabel(final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel,
			final List<Integer> selectedFieldIDs, final Map<Integer, String> labelHeaders, final int rowNumber) {
		final StringBuilder buffer = new StringBuilder();

		int i = 0;

		for (final Integer selectedFieldID : selectedFieldIDs) {
			if (i == rowNumber) {
				buffer.append(this.getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, true));
				break;
			}
			i++;
		}

		return buffer.toString();
	}

	/**
	 * Gets the header.
	 *
	 * @param headerID the header id
	 * @return the header
	 */
	private String getHeader(final Integer headerID, final Map<Integer, String> labelHeaders) {
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
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			return "";
		}
	}

	/**
	 * Gets the specific info.
	 *
	 * @param moreFieldInfo the more field info
	 * @param fieldMapLabel the field map label
	 * @param headerID the barcode label
	 * @return the specific info
	 */
	private String getSpecificInfo(final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel, final Integer headerID,
			final Map<Integer, String> labelHeaders, final boolean includeHeaderLabel) {
		final StringBuilder buffer = new StringBuilder();

		try {

			final String headerName = this.getHeader(headerID, labelHeaders);

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
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
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
			if (stemp != null && "null".equalsIgnoreCase(stemp)) {
				stemp = " ";
			}

			if (includeHeaderLabel && headerName != null) {
				stemp = headerName + " : " + stemp;
			}

			return stemp;
		} catch (final NumberFormatException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateXlSLabels(org.generationcp.middleware.domain.fieldbook.
	 * FieldMapDatasetInfo , com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting, java.io.ByteArrayOutputStream)
	 */
	@Override
	public String generateXlSLabels(final List<StudyTrialInstanceInfo> trialInstances, final UserLabelPrinting userLabelPrinting,
			final ByteArrayOutputStream baos) throws MiddlewareQueryException {

		String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
		final boolean includeHeader =
				LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
		final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());
		final String fileName = userLabelPrinting.getFilenameDLLocation();
		final String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
		final String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
		final String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

		try {

			final HSSFWorkbook workbook = new HSSFWorkbook();
			String sheetName = WorkbookUtil.createSafeSheetName(userLabelPrinting.getName());
			if (sheetName == null) {
				sheetName = "Labels";
			}
			final Sheet labelPrintingSheet = workbook.createSheet(sheetName);

			final CellStyle labelStyle = workbook.createCellStyle();
			final HSSFFont font = workbook.createFont();
			font.setBoldweight(org.apache.poi.ss.usermodel.Font.BOLDWEIGHT_BOLD);
			labelStyle.setFont(font);

			final CellStyle wrapStyle = workbook.createCellStyle();
			wrapStyle.setWrapText(true);
			wrapStyle.setAlignment(CellStyle.ALIGN_CENTER);

			final CellStyle mainHeaderStyle = workbook.createCellStyle();

			final HSSFPalette palette = workbook.getCustomPalette();
			// get the color which most closely matches the color you want to use
			final HSSFColor myColor = palette.findSimilarColor(179, 165, 165);
			// get the palette index of that color
			final short palIndex = myColor.getIndex();
			// code to get the style for the cell goes here
			mainHeaderStyle.setFillForegroundColor(palIndex);
			mainHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

			final CellStyle mainSubHeaderStyle = workbook.createCellStyle();

			final HSSFPalette paletteSubHeader = workbook.getCustomPalette();
			// get the color which most closely matches the color you want to use
			final HSSFColor myColorSubHeader = paletteSubHeader.findSimilarColor(190, 190, 186);
			// get the palette index of that color
			final short palIndexSubHeader = myColorSubHeader.getIndex();
			// code to get the style for the cell goes here
			mainSubHeaderStyle.setFillForegroundColor(palIndexSubHeader);
			mainSubHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			mainSubHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);

			int rowIndex = 0;
			int columnIndex = 0;

			// Create Header Information

			// Row 1: SUMMARY OF TRIAL, FIELD AND PLANTING DETAILS
			Row row = null;
			mainSelectedFields = this.appendBarcode(isBarcodeNeeded, mainSelectedFields);

			final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);

			if (includeHeader) {
				row = labelPrintingSheet.createRow(rowIndex++);
				// we add all the selected fields header
				this.printHeaderFields(trialInstances.get(0).getTrialInstance().getLabelHeaders(), includeHeader, selectedFieldIDs, row,
						columnIndex, labelStyle);
			}

			// we populate the info now
			for (final StudyTrialInstanceInfo trialInstance : trialInstances) {
				final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();

				final Map<String, String> moreFieldInfo = this.generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");

				for (final FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {

					row = labelPrintingSheet.createRow(rowIndex++);
					columnIndex = 0;

					final String barcodeLabelForCode = this.generateBarcodeField(moreFieldInfo, fieldMapLabel, firstBarcodeField,
							secondBarcodeField, thirdBarcodeField, fieldMapTrialInstanceInfo.getLabelHeaders(), false);
					moreFieldInfo.put(LabelPrintingServiceImpl.BARCODE, barcodeLabelForCode);

					for (final Integer selectedFieldID : selectedFieldIDs) {
						final String leftText = this.getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID,
								fieldMapTrialInstanceInfo.getLabelHeaders(), false);
						final Cell summaryCell = row.createCell(columnIndex++);
						summaryCell.setCellValue(leftText);
					}
				}
			}

			for (int columnPosition = 0; columnPosition < columnIndex; columnPosition++) {
				labelPrintingSheet.autoSizeColumn((short) columnPosition);
			}

			// Write the excel file
			final FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			workbook.write(fileOutputStream);
			fileOutputStream.close();

		} catch (final Exception e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}
		return fileName;
	}

	protected String appendBarcode(final boolean isBarcodeNeeded, final String mainSelectedFields) {
		String processed = mainSelectedFields;
		if (isBarcodeNeeded) {
			processed += "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt();
		}
		return processed;
	}

	protected void printHeaderFields(final Map<Integer, String> labelHeaders, final boolean includeHeader,
			final List<Integer> selectedFieldIDs, final Row row, final int columnIndex, final CellStyle labelStyle) {
		if (includeHeader) {
			int currentIndex = columnIndex;
			for (final Integer selectedFieldID : selectedFieldIDs) {
				final String headerName = this.getHeader(selectedFieldID, labelHeaders);
				final Cell summaryCell = row.createCell(currentIndex++);
				summaryCell.setCellValue(headerName);
				summaryCell.setCellStyle(labelStyle);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateCSVLabels(org.generationcp.middleware.domain.fieldbook.
	 * FieldMapDatasetInfo , com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting, java.io.ByteArrayOutputStream)
	 */
	@Override
	public String generateCSVLabels(final List<StudyTrialInstanceInfo> trialInstances, final UserLabelPrinting userLabelPrinting,
			final ByteArrayOutputStream baos) throws IOException {
		final String fileName = userLabelPrinting.getFilenameDLLocation();
		String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
		final boolean includeHeader =
				LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS.equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf());
		final boolean isBarcodeNeeded = LabelPrintingServiceImpl.BARCODE_NEEDED.equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded());

		mainSelectedFields = this.appendBarcode(isBarcodeNeeded, mainSelectedFields);

		final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);
		final List<ExportColumnHeader> exportColumnHeaders =
				this.generateColumnHeaders(selectedFieldIDs, trialInstances.get(0).getTrialInstance().getLabelHeaders());

		final List<Map<Integer, ExportColumnValue>> exportColumnValues =
				this.generateColumnValues(trialInstances, selectedFieldIDs, userLabelPrinting);

		this.germplasmExportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName, includeHeader);

		return fileName;
	}

	private List<Map<Integer, ExportColumnValue>> generateColumnValues(final List<StudyTrialInstanceInfo> trialInstances,
			final List<Integer> selectedFieldIDs, final UserLabelPrinting userLabelPrinting) {
		final List<Map<Integer, ExportColumnValue>> columnValues = new ArrayList<>();

		final String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
		final String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
		final String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

		// we populate the info now
		for (final StudyTrialInstanceInfo trialInstance : trialInstances) {
			final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();

			final Map<String, String> moreFieldInfo = this.generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");
			for (final FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {
				final String barcodeLabelForCode = this.generateBarcodeField(moreFieldInfo, fieldMapLabel, firstBarcodeField,
						secondBarcodeField, thirdBarcodeField, fieldMapTrialInstanceInfo.getLabelHeaders(), false);
				moreFieldInfo.put(LabelPrintingServiceImpl.BARCODE, barcodeLabelForCode);

				final Map<Integer, ExportColumnValue> rowMap =
						this.generateRowMap(fieldMapTrialInstanceInfo.getLabelHeaders(), selectedFieldIDs, moreFieldInfo, fieldMapLabel);
				columnValues.add(rowMap);
			}
		}

		return columnValues;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void populateUserSpecifiedLabelFields(final List<FieldMapTrialInstanceInfo> trialFieldMap, final Workbook workbook,
			final String selectedFields, final boolean isTrial, final boolean isStockList) {

		final LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
		params.setVariableMap(this.convertToMap(workbook.getConditions(), workbook.getFactors()));
		params.setSelectedFieldIDs(SettingsUtil.parseFieldListAndConvert(selectedFields));

		if (isStockList) {
			final GermplasmList stockList = trialFieldMap.get(0).getStockList();
			params.setAllFieldIDs(this.convertToListInteger(this.getAvailableLabelFieldsForStockList(
					this.getStockListType(stockList.getType()), Locale.ENGLISH, workbook.getStudyDetails().getId())));
		} else {
			params.setAllFieldIDs(this.convertToListInteger(
					this.getAvailableLabelFieldsForStudy(isTrial, true, Locale.ENGLISH, workbook.getStudyDetails().getId())));
		}

		Map<String, List<MeasurementRow>> measurementData = null;
		Map<String, MeasurementRow> environmentData = null;

		if (isTrial) {
			measurementData = this.extractMeasurementRowsPerTrialInstance(workbook.getObservations());
			environmentData = this.extractEnvironmentMeasurementDataPerTrialInstance(workbook);
		}

		this.checkAndSetFieldMapInstanceInfo(trialFieldMap, workbook, isTrial, isStockList, params, measurementData, environmentData);
	}

	void checkAndSetFieldMapInstanceInfo(final List<FieldMapTrialInstanceInfo> trialFieldMap, final Workbook workbook,
			final boolean isTrial, final boolean isStockList, final LabelPrintingProcessingParams params,
			final Map<String, List<MeasurementRow>> measurementData, final Map<String, MeasurementRow> environmentData) {

		for (final FieldMapTrialInstanceInfo instanceInfo : trialFieldMap) {
			params.setInstanceInfo(instanceInfo);

			if (isStockList) {
				params.setStockList(instanceInfo.getStockList());
				params.setIsStockList(true);
				params.setInventoryDetailsMap(this.getInventoryDetailsMap(params.getStockList()));
			}

			if (isTrial) {
				params.setInstanceMeasurements(measurementData.get(instanceInfo.getTrialInstanceNo()));
				params.setEnvironmentData(environmentData.get(instanceInfo.getTrialInstanceNo()));
			} else {
				params.setInstanceMeasurements(workbook.getObservations());
			}

			this.processUserSpecificLabelsForInstance(params, workbook);

			if (!isStockList) {
				this.processInventorySpecificLabelsForInstance(params, workbook);
			}
		}
	}

	private List<Integer> convertToListInteger(final List<LabelFields> availableLabelFields) {
		final List<Integer> list = new ArrayList<Integer>();
		for (final LabelFields field : availableLabelFields) {
			list.add(field.getId());
		}
		return list;
	}

	private void processInventorySpecificLabelsForInstance(final LabelPrintingProcessingParams params, final Workbook workbook) {
		final Integer studyId = workbook.getStudyDetails().getId();
		final Map<Integer, InventoryDetails> inventoryDetailsMap = this.retrieveInventoryDetailsMap(studyId, workbook);

		if (!inventoryDetailsMap.isEmpty()) {
			for (final MeasurementRow measurement : params.getInstanceMeasurements()) {
				final FieldMapLabel label = params.getInstanceInfo().getFieldMapLabel(measurement.getExperimentId());

				final InventoryDetails inventoryDetails = inventoryDetailsMap.get(label.getGid());
				if (inventoryDetails != null) {
					label.setInventoryAmount(inventoryDetails.getAmount());
					label.setScaleName(inventoryDetails.getScaleName());
					label.setLotId(inventoryDetails.getLotId());
				}
			}
		}
	}

	private Map<Integer, InventoryDetails> retrieveInventoryDetailsMap(final Integer studyId, final Workbook workbook) {
		final Map<Integer, InventoryDetails> inventoryDetailsMap = new HashMap<Integer, InventoryDetails>();

		try {
			GermplasmList germplasmList = null;
			final GermplasmListType listType = workbook.isNursery() ? GermplasmListType.NURSERY : GermplasmListType.TRIAL;
			final List<GermplasmList> germplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, listType);
			if (!germplasmLists.isEmpty()) {
				germplasmList = germplasmLists.get(0);
			}

			if (germplasmList != null) {
				final Integer listId = germplasmList.getId();
				final String germplasmListType = germplasmList.getType();
				final List<InventoryDetails> inventoryDetailList =
						this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId, germplasmListType);

				for (final InventoryDetails inventoryDetails : inventoryDetailList) {
					if (inventoryDetails.getLotId() != null) {
						inventoryDetailsMap.put(inventoryDetails.getGid(), inventoryDetails);
					}
				}
			}
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		return inventoryDetailsMap;
	}

	@Override
	public void deleteProgramPreset(final Integer programPresetId) throws MiddlewareQueryException {

		this.presetDataManager.deleteProgramPreset(programPresetId);

	}

	protected Map<String, MeasurementRow> extractEnvironmentMeasurementDataPerTrialInstance(final Workbook workbook) {
		final Map<String, MeasurementRow> data = new HashMap<>();

		for (final MeasurementRow row : workbook.getTrialObservations()) {
			final String trialInstance = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
			data.put(trialInstance, row);
		}

		return data;
	}

	protected void processUserSpecificLabelsForInstance(final LabelPrintingProcessingParams params, final Workbook workbook) {

		params.setLabelHeaders(new HashMap<Integer, String>());
		boolean firstEntry = true;

		if (params.isStockList()) {
			final List<FieldMapLabel> fieldMapLabels = new ArrayList<FieldMapLabel>();
			for (final Map.Entry<String, InventoryDetails> entry : params.getInventoryDetailsMap().entrySet()) {
				final InventoryDetails inventoryDetail = entry.getValue();

				final FieldMapLabel label = new FieldMapLabel();

				final Map<Integer, String> userSpecifiedLabels =
						this.extractDataForUserSpecifiedLabels(params, null, inventoryDetail, firstEntry, workbook);

				params.setUserSpecifiedLabels(userSpecifiedLabels);

				label.setUserFields(userSpecifiedLabels);

				fieldMapLabels.add(label);

				if (firstEntry) {
					firstEntry = false;
				}

				params.getInstanceInfo().setLabelHeaders(params.getLabelHeaders());
			}

			// this overrides the existing fieldMapLabel objects so that it will retrieve details from stock list
			// and not from germplasm list of the nursery
			params.getInstanceInfo().setFieldMapLabels(fieldMapLabels);

		} else {
			for (final MeasurementRow measurement : params.getInstanceMeasurements()) {
				final FieldMapLabel label = params.getInstanceInfo().getFieldMapLabel(measurement.getExperimentId());

				final Map<Integer, String> userSpecifiedLabels =
						this.extractDataForUserSpecifiedLabels(params, measurement, null, firstEntry, workbook);

				params.setUserSpecifiedLabels(userSpecifiedLabels);

				label.setUserFields(userSpecifiedLabels);

				if (firstEntry) {
					firstEntry = false;
				}

				params.getInstanceInfo().setLabelHeaders(params.getLabelHeaders());
			}
		}
	}

	@Override
	public Map<String, InventoryDetails> getInventoryDetailsMap(final GermplasmList stockList) {
		final Map<String, InventoryDetails> inventoryDetailsMap = new HashMap<String, InventoryDetails>();
		final List<InventoryDetails> listDataProjects;
		try {
			listDataProjects = this.inventoryMiddlewareService.getInventoryListByListDataProjectListId(stockList.getId(),
					this.getStockListType(stockList.getType()));

			for (final InventoryDetails entry : listDataProjects) {
				this.setCross(entry);
				inventoryDetailsMap.put(entry.getEntryId().toString(), entry);
			}
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}
		return inventoryDetailsMap;
	}

	private void setCross(final InventoryDetails entry) {
		final Integer gid = entry.getGid();
		try {
			final String cross = this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties);
			entry.setCross(cross);
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}
	}

	protected Map<Integer, MeasurementVariable> convertToMap(final List<MeasurementVariable>... variables) {
		final Map<Integer, MeasurementVariable> map = new HashMap<>();

		for (final List<MeasurementVariable> variableList : variables) {
			for (final MeasurementVariable variable : variableList) {
				map.put(variable.getTermId(), variable);
			}
		}

		return map;
	}

	protected Map<Integer, String> extractDataForUserSpecifiedLabels(final LabelPrintingProcessingParams params,
			final MeasurementRow measurementRow, final InventoryDetails inventoryDetail, final boolean populateHeaders,
			final Workbook workbook) {

		final Map<Integer, String> values = new HashMap<>();

		for (final Integer termID : params.getAllFieldIDs()) {

			if (params.isStockList()) {

				this.populateValuesForStockList(params, inventoryDetail, termID, values, populateHeaders);
				this.populateValuesForNurseryManagement(params, workbook, termID, values, populateHeaders);

			} else if (!this.populateValuesFromMeasurement(params, measurementRow, termID, values, populateHeaders)) {

				if (workbook.isNursery()) {

					this.populateValuesForNursery(params, workbook, termID, values, populateHeaders);

				} else {

					this.populateValuesForTrial(params, workbook, termID, values, populateHeaders);

				}

			}

		}

		return values;

	}

	private void populateValuesForNurseryManagement(final LabelPrintingProcessingParams params, final Workbook workbook,
			final Integer termID, final Map<Integer, String> values, final boolean populateHeaders) {
		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.addAll(workbook.getConditions());

		final Integer newTermId = this.getCounterpartTermId(termID);

		final MeasurementVariable factorVariable = this.getMeasurementVariableByTermId(newTermId, variables);

		if (factorVariable != null) {
			values.put(newTermId, factorVariable.getValue());

			if (populateHeaders) {
				params.getLabelHeaders().put(newTermId, factorVariable.getName());
			}

		}
	}

	private void populateValuesForStockList(final LabelPrintingProcessingParams params, final InventoryDetails inventoryDetails,
			final Integer termID, final Map<Integer, String> values, final boolean populateHeaders) {

		String value = null;

		value = this.populateStockListFromGermplasmDescriptorVariables(termID, inventoryDetails);

		if (value == null) {
			value = this.populateStockListFromInventoryVariables(termID, inventoryDetails);
		}

		if (value == null) {
			value = this.populateStockListFromCrossingVariables(termID, inventoryDetails);
		}

		if (value != null) {

			values.put(termID, value);

			if (populateHeaders) {
				try {
					params.getLabelHeaders().put(termID, this.ontologyDataManager.getTermById(termID).getName());
				} catch (final MiddlewareQueryException e) {
					LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
				}
			}
		}

	}

	private String populateStockListFromGermplasmDescriptorVariables(final Integer termID, final InventoryDetails row) {
		String value = null;
		if (termID.equals(TermId.GID.getId())) {
			value = this.getValueForStockList(row.getGid());
		} else if (termID.equals(TermId.DESIG.getId())) {
			value = this.getValueForStockList(row.getGermplasmName());
		} else if (termID.equals(TermId.ENTRY_NO.getId())) {
			value = this.getValueForStockList(row.getEntryId());
		} else if (termID.equals(TermId.CROSS.getId())) {
			value = this.getValueForStockList(row.getCross());
		} else if (termID.equals(TermId.SEED_SOURCE.getId())) {
			value = this.getValueForStockList(row.getSource());
		}
		return value;
	}

	private String populateStockListFromInventoryVariables(final Integer termID, final InventoryDetails row) {
		String value = null;
		if (termID.equals(TermId.STOCKID.getId())) {
			value = this.getValueForStockList(row.getInventoryID());
		} else if (termID.equals(TermId.LOT_LOCATION_INVENTORY.getId())) {
			value = this.getValueForStockList(row.getLocationName());
		} else if (termID.equals(TermId.AMOUNT_INVENTORY.getId())) {
			value = this.getValueForStockList(row.getAmount());
		} else if (termID.equals(TermId.UNITS_INVENTORY.getId())) {
			value = this.getValueForStockList(row.getScaleName());
		} else if (termID.equals(TermId.COMMENT_INVENTORY.getId())) {
			value = this.getValueForStockList(row.getComment());
		}
		return value;
	}

	private String populateStockListFromCrossingVariables(final Integer termID, final InventoryDetails row) {
		String value = null;
		if (termID.equals(TermId.DUPLICATE.getId())) {
			value = this.getValueForStockList(row.getDuplicate());
		} else if (termID.equals(TermId.BULK_WITH.getId())) {
			value = this.getValueForStockList(row.getBulkWith());
		} else if (termID.equals(TermId.BULK_COMPL.getId())) {
			value = this.getValueForStockList(row.getBulkCompl());
		}
		return value;
	}

	private String getValueForStockList(final Object value) {
		if (value != null) {
			return value.toString();
		}
		return "";
	}

	@Override
	public GermplasmListType getStockListType(final String type) {
		return type.equalsIgnoreCase(LabelPrintingServiceImpl.ADVANCED) ? GermplasmListType.ADVANCED : GermplasmListType.CROSSES;
	}

	protected Boolean populateValuesFromMeasurement(final LabelPrintingProcessingParams params, final MeasurementRow measurementRow,
			final Integer termID, final Map<Integer, String> values, final boolean populateHeaders) {

		try {

			final MeasurementData data = measurementRow.getMeasurementData(termID);

			if (data != null) {

				final String value = data.getDisplayValue();
				values.put(termID, value);

				if (populateHeaders) {
					params.getLabelHeaders().put(termID, data.getMeasurementVariable().getName());
				}
				return true;
			}

		} catch (final NumberFormatException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	protected Integer getCounterpartTermId(final Integer termId) {

		final String nameTermId = SettingsUtil.getNameCounterpart(termId, AppConstants.ID_NAME_COMBINATION.getString());

		if (!StringUtils.isEmpty(nameTermId)) {
			return Integer.valueOf(nameTermId);
		} else {
			return termId;
		}
	}

	protected void populateValuesForTrial(final LabelPrintingProcessingParams params, final Workbook workbook, final Integer termID,
			final Map<Integer, String> values, final boolean populateHeaders) {

		final Integer newTermId = this.getCounterpartTermId(termID);

		final MeasurementVariable conditionData = params.getVariableMap().get(newTermId);

		if (conditionData != null) {
			values.put(newTermId, conditionData.getValue());

			if (populateHeaders) {
				params.getLabelHeaders().put(newTermId, conditionData.getName());
			}
		}

		if (params.getEnvironmentData() == null) {
			return;
		}

		final MeasurementData enviromentData = params.getEnvironmentData().getMeasurementData(newTermId);

		if (enviromentData != null) {
			values.put(newTermId, enviromentData.getValue());

			if (populateHeaders) {
				params.getLabelHeaders().put(newTermId, enviromentData.getLabel());
			}
		}

	}

	protected void populateValuesForNursery(final LabelPrintingProcessingParams params, final Workbook workbook, final Integer termID,
			final Map<Integer, String> values, final boolean populateHeaders) {

		final List<MeasurementVariable> variables = new ArrayList<>();
		variables.addAll(workbook.getFactors());
		variables.addAll(workbook.getConditions());
		variables.addAll(workbook.getConstants());

		final Integer newTermId = this.getCounterpartTermId(termID);

		final MeasurementVariable factorVariable = this.getMeasurementVariableByTermId(newTermId, variables);

		if (factorVariable != null) {
			values.put(newTermId, factorVariable.getValue());

			if (populateHeaders) {
				params.getLabelHeaders().put(newTermId, factorVariable.getName());
			}

		}

	}

	private MeasurementVariable getMeasurementVariableByTermId(final Integer termId, final List<MeasurementVariable> measumentVariables) {
		for (final MeasurementVariable measurementVariable : measumentVariables) {
			if (measurementVariable.getTermId() == termId) {
				return measurementVariable;
			}
		}
		return null;
	}

	protected Map<String, List<MeasurementRow>> extractMeasurementRowsPerTrialInstance(final List<MeasurementRow> dataRows) {
		// sort the observations by instance number, and then by experiment ID to simplify later process
		Collections.sort(dataRows, new Comparator<MeasurementRow>() {

			@Override
			public int compare(final MeasurementRow o1, final MeasurementRow o2) {
				final String instanceID1 = o1.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
				final String instanceID2 = o2.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();

				if (instanceID1.equals(instanceID2)) {
					return new Integer(o1.getExperimentId()).compareTo(new Integer(o2.getExperimentId()));
				} else {
					return instanceID1.compareTo(instanceID2);
				}
			}
		});

		final Map<String, List<MeasurementRow>> measurements = new HashMap<>();

		for (final MeasurementRow row : dataRows) {
			final String trialInstance = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
			List<MeasurementRow> list = measurements.get(trialInstance);

			if (list == null) {
				list = new ArrayList<>();
				measurements.put(trialInstance, list);
			}

			list.add(row);
		}

		return measurements;
	}

	protected Map<String, String> generateAddedInformationField(final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo,
			final StudyTrialInstanceInfo trialInstance, final String barCode) {
		final Map<String, String> moreFieldInfo = new HashMap<String, String>();
		moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
		moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
		moreFieldInfo.put("fieldName", fieldMapTrialInstanceInfo.getFieldName());
		moreFieldInfo.put(LabelPrintingServiceImpl.SELECTED_NAME, trialInstance.getFieldbookName());
		moreFieldInfo.put("trialInstanceNumber", fieldMapTrialInstanceInfo.getTrialInstanceNo());
		moreFieldInfo.put(LabelPrintingServiceImpl.BARCODE, barCode);

		return moreFieldInfo;
	}

	private Map<Integer, ExportColumnValue> generateRowMap(final Map<Integer, String> labelHeaders, final List<Integer> selectedFieldIDs,
			final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel) {
		final Map<Integer, ExportColumnValue> rowMap = new HashMap<Integer, ExportColumnValue>();

		for (final Integer selectedFieldID : selectedFieldIDs) {

			try {

				final String value = this.getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, false);
				final ExportColumnValue columnValue = new ExportColumnValue(selectedFieldID, value);
				rowMap.put(selectedFieldID, columnValue);
			} catch (final NumberFormatException e) {
				LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			}
		}

		return rowMap;
	}

	private List<ExportColumnHeader> generateColumnHeaders(final List<Integer> selectedFieldIDs, final Map<Integer, String> labelHeaders) {
		final List<ExportColumnHeader> columnHeaders = new ArrayList<ExportColumnHeader>();

		for (final Integer selectedFieldID : selectedFieldIDs) {
			final String headerName = this.getHeader(selectedFieldID, labelHeaders);
			final ExportColumnHeader columnHeader = new ExportColumnHeader(selectedFieldID, headerName, true);
			columnHeaders.add(columnHeader);
		}

		return columnHeaders;
	}

	/**
	 * Gets the available label fields.
	 *
	 * @param isTrial the is trial
	 * @param hasFieldMap the has field map
	 * @param locale the locale
	 * @return
	 */
	@Override
	public List<LabelFields> getAvailableLabelFieldsForFieldMap(final boolean isTrial, final boolean hasFieldMap, final Locale locale) {
		final List<LabelFields> labelFieldsList = new ArrayList<LabelFields>();

		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.entry.num", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.gid", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt(), true));
		labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.germplasm.name", null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt(), true));
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt(), true));
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), false));
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(), false));
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt(), false));

		if (isTrial) {
			labelFieldsList.add(new LabelFields(
					this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY, null, locale),
					AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt(), false));
			labelFieldsList
					.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale),
							AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt(), false));
			labelFieldsList.add(new LabelFields(this.messageSource.getMessage("label.printing.available.fields.rep", null, locale),
					AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt(), false));
		} else {
			labelFieldsList.add(new LabelFields(
					this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null, locale),
					AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt(), false));
		}
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt(), false));

		this.addAvailableFieldsForFieldMap(hasFieldMap, locale, labelFieldsList);

		return labelFieldsList;
	}

	@Override
	public List<LabelFields> getAvailableLabelFieldsForStudy(final boolean isTrial, final boolean hasFieldMap, final Locale locale,
			final int studyID) {
		final List<LabelFields> labelFieldsList = new ArrayList<>();

		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt(), true));
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), false));
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(), false));
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt(), false));
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt(), false));

		Workbook workbook = null;
		if (isTrial) {
			labelFieldsList.add(new LabelFields(
					this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY, null, locale),
					AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt(), false));

			try {
				workbook = this.fieldbookMiddlewareService.getTrialDataSet(studyID);

				labelFieldsList.addAll(this.settingsService.retrieveTrialSettingsAsLabels(workbook));
				labelFieldsList.addAll(this.settingsService.retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(workbook));
				labelFieldsList.addAll(this.settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));

			} catch (final MiddlewareException e) {
				LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			}

		} else {

			labelFieldsList.add(new LabelFields(
					this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null, locale),
					AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt(), false));
			try {
				workbook = this.fieldbookMiddlewareService.getNurseryDataSet(studyID);

				labelFieldsList.addAll(this.settingsService.retrieveNurseryManagementDetailsAsLabels(workbook));
				labelFieldsList.addAll(this.settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));

			} catch (final MiddlewareException e) {
				LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			}
		}

		// add trait fields
		labelFieldsList.addAll(this.settingsService.retrieveTraitsAsLabels(workbook));

		// add field map fields
		this.addAvailableFieldsForFieldMap(hasFieldMap, locale, labelFieldsList);

		// add inventory fields if any
		if (this.hasInventoryValues(studyID, workbook.isNursery())) {
			labelFieldsList.addAll(this.addInventoryRelatedLabelFields(studyID, locale));
		}

		return labelFieldsList;
	}

	@Override
	public List<LabelFields> getAvailableLabelFieldsForStockList(final GermplasmListType listType, final Locale locale, final int studyID) {
		final List<LabelFields> labelFieldsList = new ArrayList<>();

		// Nursery Management Fields
		labelFieldsList.add(new LabelFields(
				this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null, locale),
				AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt(), false));

		Workbook workbook = null;
		try {
			workbook = this.fieldbookMiddlewareService.getNurseryDataSet(studyID);

			labelFieldsList.addAll(this.settingsService.retrieveNurseryManagementDetailsAsLabels(workbook));
			labelFieldsList.addAll(this.settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));

		} catch (final MiddlewareException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		// Stock List Specific Fields
		labelFieldsList.addAll(this.addStockListDetailsFields(locale, listType));

		return labelFieldsList;
	}

	private List<LabelFields> addStockListDetailsFields(final Locale locale, final GermplasmListType listType) {
		final List<LabelFields> labelFieldList = new ArrayList<LabelFields>();

		labelFieldList
				.add(new LabelFields(ColumnLabels.STOCKID.getTermNameFromOntology(this.ontologyDataManager), TermId.STOCKID.getId(), true));

		labelFieldList.add(new LabelFields(ColumnLabels.LOT_LOCATION.getTermNameFromOntology(this.ontologyDataManager),
				TermId.LOT_LOCATION_INVENTORY.getId(), true));

		labelFieldList.add(new LabelFields(ColumnLabels.AMOUNT.getTermNameFromOntology(this.ontologyDataManager),
				TermId.AMOUNT_INVENTORY.getId(), true));

		labelFieldList.add(new LabelFields(ColumnLabels.UNITS.getTermNameFromOntology(this.ontologyDataManager),
				TermId.UNITS_INVENTORY.getId(), true));

		labelFieldList.add(new LabelFields(ColumnLabels.COMMENT.getTermNameFromOntology(this.ontologyDataManager),
				TermId.COMMENT_INVENTORY.getId(), true));

		if (listType.equals(GermplasmListType.CROSSES)) {

			labelFieldList.add(new LabelFields(ColumnLabels.DUPLICATE.getTermNameFromOntology(this.ontologyDataManager),
					TermId.DUPLICATE.getId(), true));

			labelFieldList.add(new LabelFields(ColumnLabels.BULK_WITH.getTermNameFromOntology(this.ontologyDataManager),
					TermId.BULK_WITH.getId(), true));

			labelFieldList.add(new LabelFields(ColumnLabels.BULK_COMPL.getTermNameFromOntology(this.ontologyDataManager),
					TermId.BULK_COMPL.getId(), true));

		}

		return labelFieldList;
	}

	private void addAvailableFieldsForFieldMap(final boolean hasFieldMap, final Locale locale, final List<LabelFields> labelFieldsList) {
		if (hasFieldMap) {
			labelFieldsList.add(new LabelFields(
					this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY, null, locale),
					AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(), false));
			labelFieldsList.add(new LabelFields(this.messageSource
					.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY, null, locale),
					AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(), false));
			labelFieldsList.add(new LabelFields(
					this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY, null, locale),
					AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt(), false));
		}
	}

	/***
	 * Returned true if the current study's germplasm list has inventory details
	 *
	 * @param studyID
	 * @param isNursery
	 * @return
	 */
	protected boolean hasInventoryValues(final int studyID, final boolean isNursery) {
		try {
			GermplasmList germplasmList = null;
			final GermplasmListType listType = isNursery ? GermplasmListType.NURSERY : GermplasmListType.TRIAL;
			final List<GermplasmList> germplasmLists = this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyID, listType);
			if (!germplasmLists.isEmpty()) {
				germplasmList = germplasmLists.get(0);
			}

			if (germplasmList != null) {
				final Integer listId = germplasmList.getId();
				final String germplasmListType = germplasmList.getType();
				final List<InventoryDetails> inventoryDetailList =
						this.inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId, germplasmListType);

				for (final InventoryDetails inventoryDetails : inventoryDetailList) {
					if (inventoryDetails.getLotId() != null) {
						return true;
					}
				}
			}
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	protected List<LabelFields> addInventoryRelatedLabelFields(final int studyID, final Locale locale) {
		final List<LabelFields> labelFieldList = new ArrayList<LabelFields>();

		labelFieldList.add(new LabelFields(this.messageSource.getMessage("label.printing.seed.inventory.amount", null, locale),
				AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt(), false));

		labelFieldList.add(new LabelFields(this.messageSource.getMessage("label.printing.seed.inventory.scale", null, locale),
				AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt(), false));

		labelFieldList.add(new LabelFields(this.messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale),
				AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt(), false));

		return labelFieldList;
	}

	@Override
	public boolean checkAndSetFieldmapProperties(final UserLabelPrinting userLabelPrinting, final FieldMapInfo fieldMapInfoDetail) {
		// if there are datasets with fieldmap, check if all trial instances of the study have fieldmaps
		if (!fieldMapInfoDetail.getDatasetsWithFieldMap().isEmpty()) {
			for (final FieldMapDatasetInfo dataset : fieldMapInfoDetail.getDatasetsWithFieldMap()) {
				if (dataset.getTrialInstances().size() == dataset.getTrialInstancesWithFieldMap().size()) {
					userLabelPrinting.setFieldMapsExisting(true);
				} else {
					userLabelPrinting.setFieldMapsExisting(false);
				}
			}
			return true;
		} else {
			userLabelPrinting.setFieldMapsExisting(false);
			return false;
		}
	}

	@Override
	public LabelPrintingPresets getLabelPrintingPreset(final Integer presetId, final Integer presetType) throws MiddlewareQueryException {
		if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
			final StandardPreset standardPreset = this.workbenchService.getStandardPresetById(presetId);

			return new LabelPrintingPresets(presetId, standardPreset.getName(), LabelPrintingPresets.STANDARD_PRESET);

		} else {
			final ProgramPreset programPreset = this.presetDataManager.getProgramPresetById(presetId);

			return new LabelPrintingPresets(presetId, programPreset.getName(), LabelPrintingPresets.PROGRAM_PRESET);
		}
	}

	@Override
	public ProgramPreset getLabelPrintingProgramPreset(final Integer programPresetId) throws MiddlewareQueryException {
		return this.presetDataManager.getProgramPresetById(programPresetId);
	}

	@Override
	public List<LabelPrintingPresets> getAllLabelPrintingPresetsByName(final String presetName, final Integer programId,
			final Integer presetType) throws MiddlewareQueryException {
		final List<LabelPrintingPresets> out = new ArrayList<>();

		final Project project = this.workbenchService.getProjectById(programId.longValue());

		if (LabelPrintingPresets.PROGRAM_PRESET == presetType) {
			final List<ProgramPreset> presets =
					this.presetDataManager.getProgramPresetFromProgramAndToolByName(presetName, this.contextUtil.getCurrentProgramUUID(),
							this.workbenchService.getFieldbookWebTool().getToolId().intValue(), ToolSection.FBK_LABEL_PRINTING.name());

			for (final ProgramPreset preset : presets) {
				out.add(new LabelPrintingPresets(preset.getProgramPresetId(), preset.getName(), LabelPrintingPresets.PROGRAM_PRESET));
			}
		} else {
			final String cropName = project.getCropType().getCropName();

			final List<StandardPreset> standardPresets = this.workbenchService.getStandardPresetByCropAndPresetName(presetName,
					this.workbenchService.getFieldbookWebTool().getToolId().intValue(), cropName, ToolSection.FBK_LABEL_PRINTING.name());

			for (final StandardPreset preset : standardPresets) {
				out.add(new LabelPrintingPresets(preset.getStandardPresetId(), preset.getName(), LabelPrintingPresets.STANDARD_PRESET));
			}
		}

		return out;
	}

	@Override
	public List<LabelPrintingPresets> getAllLabelPrintingPresets(final Integer programId) throws LabelPrintingException {
		try {
			final List<LabelPrintingPresets> allLabelPrintingPresets = new ArrayList<LabelPrintingPresets>();

			// 1. get the crop name of the particular programId,
			final Project project = this.workbenchService.getProjectById(programId.longValue());
			final String cropName = project.getCropType().getCropName();
			final Integer fieldbookToolId = this.workbenchService.getFieldbookWebTool().getToolId().intValue();

			// 2. retrieve the standard presets
			for (final StandardPreset preset : this.workbenchService.getStandardPresetByCrop(fieldbookToolId, cropName,
					ToolSection.FBK_LABEL_PRINTING.name())) {
				allLabelPrintingPresets.add(
						new LabelPrintingPresets(preset.getStandardPresetId(), preset.getName(), LabelPrintingPresets.STANDARD_PRESET));
			}

			// 3. add all program presets for fieldbook
			for (final ProgramPreset preset : this.presetDataManager.getProgramPresetFromProgramAndTool(
					this.contextUtil.getCurrentProgramUUID(), fieldbookToolId, ToolSection.FBK_LABEL_PRINTING.name())) {
				allLabelPrintingPresets
						.add(new LabelPrintingPresets(preset.getProgramPresetId(), preset.getName(), LabelPrintingPresets.PROGRAM_PRESET));
			}

			return allLabelPrintingPresets;

		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			throw new LabelPrintingException("label.printing.cannot.retrieve.presets", "database.connectivity.error", e.getMessage());

		}
	}

	@Override
	public String getLabelPrintingPresetConfig(final int presetId, final int presetType) throws LabelPrintingException {
		try {
			if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
				return this.workbenchService.getStandardPresetById(presetId).getConfiguration();
			} else {
				return this.presetDataManager.getProgramPresetById(presetId).getConfiguration();
			}
		} catch (final MiddlewareQueryException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			throw new LabelPrintingException("label.printing.cannot.retrieve.presets", "database.connectivity.error", e.getMessage());
		} catch (final NullPointerException e) {
			LabelPrintingServiceImpl.LOG.error(e.getMessage(), e);
			throw new LabelPrintingException("label.printing.preset.does.not.exists", "label.printing.preset.does.not.exists",
					e.getMessage());
		}
	}

	@Override
	public void saveOrUpdateLabelPrintingPresetConfig(final String settingsName, final String xmlConfig, final Integer programId)
			throws MiddlewareQueryException {
		// check if exists, override if true else add new
		final List<LabelPrintingPresets> searchPresetList =
				this.getAllLabelPrintingPresetsByName(settingsName, programId, LabelPrintingPresets.PROGRAM_PRESET);

		if (!searchPresetList.isEmpty()) {
			// update
			final ProgramPreset currentLabelPrintingPreset = this.getLabelPrintingProgramPreset(searchPresetList.get(0).getId());
			currentLabelPrintingPreset.setConfiguration(xmlConfig);

			this.presetDataManager.saveOrUpdateProgramPreset(currentLabelPrintingPreset);
		} else {
			// add new
			final ProgramPreset preset = new ProgramPreset();
			preset.setName(settingsName);
			preset.setProgramUuid(this.contextUtil.getCurrentProgramUUID());
			preset.setToolId(this.workbenchService.getFieldbookWebTool().getToolId().intValue());
			preset.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
			preset.setConfiguration(xmlConfig);

			this.presetDataManager.saveOrUpdateProgramPreset(preset);
		}
	}

	public void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setFieldbookMiddlewareService(final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	public void setInventoryMiddlewareService(final InventoryService inventoryMiddlewareService) {
		this.inventoryMiddlewareService = inventoryMiddlewareService;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}
}
