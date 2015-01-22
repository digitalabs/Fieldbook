/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.service;

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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.ExportService;
import org.generationcp.commons.spring.util.ProgramUUIDFactory;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * The Class LabelPrintingServiceImpl.
 */
@Service
public class LabelPrintingServiceImpl implements LabelPrintingService{

	private static final String SELECTED_NAME = "selectedName";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_YEAR = "label.printing.available.fields.year";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME = "label.printing.available.fields.trial.name";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_SEASON = "label.printing.available.fields.season";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES = "label.printing.available.fields.plot.coordinates";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_PLOT = "label.printing.available.fields.plot";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE = "label.printing.available.fields.parentage";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME = "label.printing.available.fields.nursery.name";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION = "label.printing.available.fields.location";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME = "label.printing.available.fields.field.name";

	private static final String LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME = "label.printing.available.fields.block.name";

	private static final String BARCODE = "barcode";

	/** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceImpl.class);

	/** The delimiter. */
    private String delimiter = " | ";
    
    /** The message source. */
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    @Resource
    private ExportService exportService;


	@Resource
	private WorkbenchService workbenchService;

	@Resource
	private PresetDataManager presetDataManager;

    @Resource
    private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

    @Resource
    private SettingsService settingsService;
    
    @Resource
    private ProgramUUIDFactory uuidFactory;

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
	 */

    public LabelPrintingServiceImpl(){
    	super();
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
     */
    @Override
    public String generatePDFLabels(List<StudyTrialInstanceInfo> trialInstances
            , UserLabelPrinting userLabelPrinting,
            ByteArrayOutputStream baos) throws LabelPrintingException {

        int pageSizeId = Integer.parseInt(userLabelPrinting.getSizeOfLabelSheet());
        int numberOfLabelPerRow = Integer.parseInt(userLabelPrinting.getNumberOfLabelPerRow());
        int numberofRowsPerPageOfLabel = Integer.parseInt(userLabelPrinting.getNumberOfRowsPerPageOfLabel());
        int totalPerPage = numberOfLabelPerRow * numberofRowsPerPageOfLabel;
        String leftSelectedFields = userLabelPrinting.getLeftSelectedLabelFields();
        String rightSelectedFields = userLabelPrinting.getRightSelectedLabelFields();
        String barcodeNeeded = userLabelPrinting.getBarcodeNeeded();

        String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

        String fileName = userLabelPrinting.getFilenameDLLocation();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            try {
            	LabelPaper paper = LabelPaperFactory.generateLabelPaper(numberOfLabelPerRow, numberofRowsPerPageOfLabel, pageSizeId);
            	
                Rectangle pageSize = PageSize.LETTER;

                if (pageSizeId == AppConstants.SIZE_OF_PAPER_A4.getInt()) {
                    pageSize = PageSize.A4;
                }

                Document document = new Document(pageSize);
                
                //float marginLeft, float marginRight, float marginTop, float marginBottom
                document.setMargins(paper.getMarginLeft(), paper.getMarginRight(), paper.getMarginTop(), paper.getMarginBottom());
                
                PdfWriter.getInstance(document, fileOutputStream);
                
                // step 3
                document.open();

                int i = 0;
                int fixTableRowSize = numberOfLabelPerRow;
                PdfPTable table = new PdfPTable(fixTableRowSize);

                float columnWidthSize = 265f;
                float[] widthColumns = new float[fixTableRowSize];

                for (int counter = 0; counter < widthColumns.length; counter++) {
                    widthColumns[counter] = columnWidthSize;
                }

                table.setWidths(widthColumns);
                table.setWidthPercentage(100);
                int width = 600;
                int height = 75;

                List<File> filesToBeDeleted = new ArrayList<File>();
                float cellHeight = paper.getCellHeight();

                for (StudyTrialInstanceInfo trialInstance : trialInstances) {
                    FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance
                            .getTrialInstance();

                    Map<String, String> moreFieldInfo = generateAddedInformationField(
                            fieldMapTrialInstanceInfo, trialInstance, "");

                    for (FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo
                            .getFieldMapLabels()) {

                        i++;
                        String barcodeLabelForCode = "";
                        String barcodeLabel = "";

                        if ("0".equalsIgnoreCase(barcodeNeeded)) {
                            barcodeLabel = " ";
                            barcodeLabelForCode = " ";
                        } else {
                            barcodeLabel = generateBarcodeField(moreFieldInfo, fieldMapLabel,
                                    firstBarcodeField, secondBarcodeField, thirdBarcodeField,
                                    false);
                            barcodeLabelForCode = generateBarcodeField(
                                    moreFieldInfo, fieldMapLabel, firstBarcodeField,
                                    secondBarcodeField, thirdBarcodeField, true);
                        }

                        if (barcodeLabelForCode != null && barcodeLabelForCode.length() > 80) {
                            throw new LabelPrintingException("label.printing.label.too.long",
                                    barcodeLabelForCode, "label.printing.label.too.long");
                        }
                        BitMatrix bitMatrix = new Code128Writer().encode(barcodeLabelForCode,
                                BarcodeFormat.CODE_128, width, height, null);
                        String imageLocation = System.getProperty("user.home")
                                + "/" + Math.random() + ".png";
                        File imageFile = new File(imageLocation);
                        FileOutputStream fout = new FileOutputStream(imageFile);
                        MatrixToImageWriter.writeToStream(bitMatrix, "png", fout);
                        filesToBeDeleted.add(imageFile);

                        Image mainImage = Image.getInstance(imageLocation);

                        PdfPCell cell = new PdfPCell();
                        cell.setFixedHeight(cellHeight);
                        cell.setNoWrap(false);
                        cell.setPadding(5f);
                        cell.setPaddingBottom(1f);

                        PdfPTable innerImageTableInfo = new PdfPTable(1);
                        innerImageTableInfo.setWidths(new float[] { 1 });
                        innerImageTableInfo.setWidthPercentage(82);
                        PdfPCell cellImage = new PdfPCell();
                        if ("1".equalsIgnoreCase(barcodeNeeded)) {
                            cellImage.addElement(mainImage);
                        } else {
                            cellImage.addElement(new Paragraph(" "));
                        }
                        cellImage.setBorder(Rectangle.NO_BORDER);
                        cellImage.setBackgroundColor(Color.white);
                        cellImage.setPadding(1.5f);

                        innerImageTableInfo.addCell(cellImage);

                        float fontSize = paper.getFontSize();

                        Font fontNormal = FontFactory.getFont("Arial", fontSize, Font.NORMAL);

                        cell.addElement(innerImageTableInfo);
                        cell.addElement(new Paragraph());
                        for (int row = 0; row < 5; row++) {
                            if (row == 0) {
                                PdfPTable innerDataTableInfo = new PdfPTable(1);
                                innerDataTableInfo.setWidths(new float[] { 1 });
                                innerDataTableInfo.setWidthPercentage(85);

                                Font fontNormalData = FontFactory
                                        .getFont("Arial", 5.0f, Font.NORMAL);
                                PdfPCell cellInnerData = new PdfPCell(new Phrase(barcodeLabel, fontNormalData));

                                cellInnerData.setBorder(Rectangle.NO_BORDER);
                                cellInnerData.setBackgroundColor(Color.white);
                                cellInnerData.setPaddingBottom(0.2f);
                                cellInnerData.setPaddingTop(0.2f);
                                cellInnerData.setHorizontalAlignment(Element.ALIGN_MIDDLE);

                                innerDataTableInfo.addCell(cellInnerData);
                                innerDataTableInfo.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                                cell.addElement(innerDataTableInfo);
                            }
                            PdfPTable innerTableInfo = new PdfPTable(2);
                            innerTableInfo.setWidths(new float[] { 1, 1 });
                            innerTableInfo.setWidthPercentage(85);
                            List<Integer> leftSelectedFieldIDs = SettingsUtil.parseFieldListAndConvert(leftSelectedFields);
                            String leftText = generateBarcodeLabel(
                                    moreFieldInfo, fieldMapLabel, leftSelectedFieldIDs, row);
                            PdfPCell cellInnerLeft = new PdfPCell(
                                    new Paragraph(leftText, fontNormal));

                            cellInnerLeft.setBorder(Rectangle.NO_BORDER);
                            cellInnerLeft.setBackgroundColor(Color.white);
                            cellInnerLeft.setPaddingBottom(0.5f);
                            cellInnerLeft.setPaddingTop(0.5f);

                            innerTableInfo.addCell(cellInnerLeft);

                            List<Integer> rightSelectedFieldIDs = SettingsUtil.parseFieldListAndConvert(rightSelectedFields);
                            String rightText = generateBarcodeLabel(
                                    moreFieldInfo, fieldMapLabel, rightSelectedFieldIDs,
                                    row);
                            PdfPCell cellInnerRight = new PdfPCell(
                                    new Paragraph(rightText, fontNormal));

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
                            int needed = fixTableRowSize - numberOfLabelPerRow;

                            for (int neededCount = 0; neededCount < needed; neededCount++) {
                                PdfPCell cellNeeded = new PdfPCell();

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
                        fout.flush();
                        fout.close();

                    }
                }
                // we need to add the last row
                if (i % numberOfLabelPerRow != 0) {
                    // we go the next line

                    int remaining = numberOfLabelPerRow - (i % numberOfLabelPerRow);
                    for (int neededCount = 0; neededCount < remaining; neededCount++) {
                        PdfPCell cellNeeded = new PdfPCell();

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
                for (File file : filesToBeDeleted) {
                    file.delete();
                }
                fileOutputStream.close();

            } catch (FileNotFoundException e) {
                LOG.error(e.getMessage(), e);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            } 

        } catch (WriterException e) {
            LOG.error(e.getMessage(), e);
        } catch(LabelPrintingException e){
        	throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
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
    private String generateBarcodeField(Map<String,String> moreFieldInfo
            , FieldMapLabel fieldMapLabel, String firstField, String secondField
            , String thirdField, boolean includeLabel){
        StringBuilder buffer = new StringBuilder();
        String fieldList = firstField + "," + secondField + "," + thirdField;

        List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(fieldList);

        for (Integer selectedFieldID : selectedFieldIDs) {
            if (!("").equalsIgnoreCase(buffer.toString())) {
                buffer.append(delimiter);
            }

            buffer.append(
                    getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, includeLabel));
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
    private String generateBarcodeLabel(Map<String,String> moreFieldInfo, 
            FieldMapLabel fieldMapLabel, List<Integer> selectedFieldIDs, int rowNumber){
        StringBuilder buffer = new StringBuilder();

        int i = 0;

        for (Integer selectedFieldID : selectedFieldIDs) {
            if (i == rowNumber) {
                buffer.append(getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, true));
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
    private String getHeader(Integer headerID){
        Locale locale = LocaleContextHolder.getLocale();

        StringBuilder buffer = new StringBuilder();

        try {
            if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.entry.num", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.gid", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.germplasm.name", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_YEAR, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_SEASON, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.trial.instance.num", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.rep", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_PLOT, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
						LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.barcode", null, locale));
			} else {
				try {
					buffer.append(fieldbookMiddlewareService.getStandardVariable(headerID).getName());
				} catch (MiddlewareQueryException e) {
					LOG.error(e.getMessage(),e);
				}
			}

            return buffer.toString();
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage(),e);
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
    private String getSpecificInfo(
            Map<String,String> moreFieldInfo, FieldMapLabel fieldMapLabel, Integer headerID, boolean includeHeaderLabel){
        StringBuilder buffer = new StringBuilder();

        try {

            String headerName = getHeader(headerID);

            if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
				buffer.append(fieldMapLabel.getEntryNumber());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
				String gidTemp = fieldMapLabel.getGid() == null
								? "" : fieldMapLabel.getGid().toString();
				buffer.append(gidTemp);
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
				buffer.append(fieldMapLabel.getGermplasmName());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
				buffer.append(fieldMapLabel.getStartYear());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
				buffer.append(fieldMapLabel.getSeason());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
				buffer.append(moreFieldInfo.get(SELECTED_NAME));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
				buffer.append(moreFieldInfo.get(SELECTED_NAME));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
				buffer.append(moreFieldInfo.get("trialInstanceNumber"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
				buffer.append(fieldMapLabel.getRep());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()) {
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
				buffer.append(moreFieldInfo.get(BARCODE));
			} else {
				String value = fieldMapLabel.getUserFields().get(headerID);
				if (value != null) {
					buffer.append(value);
				}

			}

            String stemp = buffer.toString();
            if(stemp != null && "null".equalsIgnoreCase(stemp)) {
				stemp = " ";
			}

            if(includeHeaderLabel && headerName != null){
				stemp = headerName + " : " + stemp;
			}

            return stemp;
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage(),e);
            return "";
        }
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateXlSLabels(org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo, com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting, java.io.ByteArrayOutputStream)
     */
    @Override
    public String generateXlSLabels(List<StudyTrialInstanceInfo> trialInstances,
            UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos)
            throws MiddlewareQueryException {

        String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
        boolean includeHeader = "1".equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf()) ? true : false;
        boolean isBarcodeNeeded = "1".equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded()) ? true : false;
        String fileName = userLabelPrinting.getFilenameDLLocation();
        String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

        try {

            HSSFWorkbook workbook = new HSSFWorkbook();
            String sheetName = SettingsUtil.cleanSheetAndFileName(userLabelPrinting.getName());
            if (sheetName == null) {
                sheetName = "Labels";
            }
            Sheet labelPrintingSheet = workbook.createSheet(sheetName);

            CellStyle labelStyle = workbook.createCellStyle();
            HSSFFont font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            labelStyle.setFont(font);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setAlignment(CellStyle.ALIGN_CENTER);

            CellStyle mainHeaderStyle = workbook.createCellStyle();

            HSSFPalette palette = workbook.getCustomPalette();
            // get the color which most closely matches the color you want to use
            HSSFColor myColor = palette.findSimilarColor(179, 165, 165);
            // get the palette index of that color
            short palIndex = myColor.getIndex();
            // code to get the style for the cell goes here
            mainHeaderStyle.setFillForegroundColor(palIndex);
            mainHeaderStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            CellStyle mainSubHeaderStyle = workbook.createCellStyle();

            HSSFPalette paletteSubHeader = workbook.getCustomPalette();
            // get the color which most closely matches the color you want to use
            HSSFColor myColorSubHeader = paletteSubHeader.findSimilarColor(190, 190, 186);
            // get the palette index of that color
            short palIndexSubHeader = myColorSubHeader.getIndex();
            // code to get the style for the cell goes here
            mainSubHeaderStyle.setFillForegroundColor(palIndexSubHeader);
            mainSubHeaderStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            mainSubHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);

            int rowIndex = 0;
            int columnIndex = 0;

            // Create Header Information

            // Row 1: SUMMARY OF TRIAL, FIELD AND PLANTING DETAILS
            Row row = null;
            mainSelectedFields = appendBarcode(isBarcodeNeeded, mainSelectedFields);

            List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);

            if (includeHeader) {
                row = labelPrintingSheet.createRow(rowIndex++);
                //we add all the selected fields header
                printHeaderFields(includeHeader, selectedFieldIDs, row, columnIndex, labelStyle);
            }

            //we populate the info now
            for (StudyTrialInstanceInfo trialInstance : trialInstances) {
                FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo =
                        trialInstance.getTrialInstance();

                Map<String, String> moreFieldInfo = generateAddedInformationField(
                        fieldMapTrialInstanceInfo, trialInstance, "");

                for (FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {
                    row = labelPrintingSheet.createRow(rowIndex++);
                    columnIndex = 0;

                    String barcodeLabelForCode = generateBarcodeField(
                            moreFieldInfo, fieldMapLabel, firstBarcodeField,
                            secondBarcodeField, thirdBarcodeField, false);
                    moreFieldInfo.put(BARCODE, barcodeLabelForCode);

                    for (Integer selectedFieldID : selectedFieldIDs) {
                        String leftText = getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID,
                                false);
                        Cell summaryCell = row.createCell(columnIndex++);
                        summaryCell.setCellValue(leftText);
                    }
                }
            }

            for (int columnPosition = 0; columnPosition < columnIndex; columnPosition++) {
                labelPrintingSheet.autoSizeColumn((short) (columnPosition));
            }

            //Write the excel file
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            workbook.write(fileOutputStream);
            fileOutputStream.close();

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } 
        return fileName;
    }
    protected String appendBarcode(boolean isBarcodeNeeded, String selectedFields){
    	String mainSelectedFields = selectedFields;
    	if(isBarcodeNeeded){
    		mainSelectedFields += "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt();
    	}
    	return mainSelectedFields;
    }

    protected void printHeaderFields(boolean includeHeader, List<Integer> selectedFieldIDs, Row row, int columnIndex, CellStyle labelStyle){
    	if(includeHeader){
            for (Integer selectedFieldID : selectedFieldIDs) {
                String headerName = getHeader(selectedFieldID);
                Cell summaryCell = row.createCell(columnIndex++);
                summaryCell.setCellValue(headerName);
                summaryCell.setCellStyle(labelStyle);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateCSVLabels(org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo, com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting, java.io.ByteArrayOutputStream)
     */
    @Override
    public String generateCSVLabels(List<StudyTrialInstanceInfo> trialInstances,
            UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos)
            throws IOException {
    	String fileName = userLabelPrinting.getFilenameDLLocation();
    	String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
        boolean includeHeader = "1".equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf()) ? true : false;            	    
        boolean isBarcodeNeeded = "1".equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded()) ? true : false;        
        
        mainSelectedFields = appendBarcode(isBarcodeNeeded, mainSelectedFields);

        List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);
    	List<ExportColumnHeader> exportColumnHeaders = generateColumnHeaders(selectedFieldIDs);
    	
		List<Map<Integer, ExportColumnValue>> exportColumnValues = generateColumnValues(trialInstances, selectedFieldIDs, userLabelPrinting);
		
		exportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName, includeHeader);
    	
    	return fileName;
    }
    
    private List<Map<Integer, ExportColumnValue>> generateColumnValues(List<StudyTrialInstanceInfo> trialInstances, List<Integer> selectedFieldIDs, UserLabelPrinting userLabelPrinting) {
    	List<Map<Integer, ExportColumnValue>> columnValues = new ArrayList<>();
    	
        String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();
        
    	//we populate the info now
        for(StudyTrialInstanceInfo trialInstance : trialInstances){
            FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();
            
            Map<String,String> moreFieldInfo = generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");
            for(FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()){
            	
            	String barcodeLabelForCode = generateBarcodeField(
                        moreFieldInfo, fieldMapLabel, firstBarcodeField,
                        secondBarcodeField, thirdBarcodeField, false);
                moreFieldInfo.put(BARCODE, barcodeLabelForCode);
                
            	Map<Integer, ExportColumnValue> rowMap = generateRowMap(selectedFieldIDs, moreFieldInfo, fieldMapLabel);
            	columnValues.add(rowMap);
            }
        }
        
        return columnValues;
	}

    public void populateUserSpecifiedLabelFields(List<FieldMapTrialInstanceInfo> trialFieldMap, Workbook workbook, String selectedFields, boolean isTrial) {
        Map<Integer, MeasurementVariable> variableMap = convertToMap(workbook.getStudyConditions(), workbook.getFactors());
        Map<String, List<MeasurementRow>> measurementData = null;

        // this variable is defined as a map with list of measurementrow as data, but in actuality we are expecting only one
        // a list is used so that existing procedure can be reused
        Map<String, MeasurementRow> environmentData = null;

        if (isTrial) {
            environmentData = extractEnvironmentMeasurementDataPerTrialInstance(workbook);
            measurementData = extractMeasurementRowsPerTrialInstance(
                    workbook.getObservations());
        }



        List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(selectedFields);

        for (FieldMapTrialInstanceInfo instanceInfo : trialFieldMap) {
            if (isTrial) {
                List<MeasurementRow> trialMeasurements = measurementData
                        .get(instanceInfo.getTrialInstanceNo());

                processUserSpecificLabelsForInstance(instanceInfo, trialMeasurements,
                        selectedFieldIDs, variableMap, environmentData.get(instanceInfo.getTrialInstanceNo()));
            } else {
                processUserSpecificLabelsForInstance(instanceInfo, workbook.getObservations(),
                        selectedFieldIDs, variableMap, null);
            }

        }
    }

    @Override
    public void deleteProgramPreset(Integer programPresetId)
            throws MiddlewareQueryException {

        presetDataManager.deleteProgramPreset(programPresetId);

    }

    protected Map<String, MeasurementRow> extractEnvironmentMeasurementDataPerTrialInstance(Workbook workbook) {
        Map<String, MeasurementRow> data = new HashMap<>();

        for (MeasurementRow row : workbook.getTrialObservations()) {
            String trialInstance = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
            data.put(trialInstance, row);
        }

        return data;
    }


    protected void processUserSpecificLabelsForInstance(FieldMapTrialInstanceInfo instance,
            List<MeasurementRow> instanceMeasurements,
            List<Integer> selectedFieldIDs, Map<Integer, MeasurementVariable> variableMap, MeasurementRow environmentData) {
        for (MeasurementRow measurement : instanceMeasurements) {
            FieldMapLabel label = instance.getFieldMapLabel(measurement.getExperimentId());
            Map<Integer, String> userSpecifiedLabels = extractDataForUserSpecifiedLabels(
                    selectedFieldIDs, measurement);

            // cover the case where the data is not stored in the measurement row
            if (userSpecifiedLabels.size() < selectedFieldIDs.size()) {
                for (Integer selectedFieldID : selectedFieldIDs) {
                    if (!userSpecifiedLabels.containsKey(selectedFieldID) && variableMap
                            .containsKey(selectedFieldID)) {
                        userSpecifiedLabels
                                .put(selectedFieldID, variableMap.get(selectedFieldID).getValue());
                    }
                }
            }

            if (environmentData != null) {
                for (Integer selectedFieldID : selectedFieldIDs) {
                    MeasurementData data = environmentData.getMeasurementData(selectedFieldID);
                    if (!userSpecifiedLabels.containsKey(selectedFieldID) && data != null) {
                        userSpecifiedLabels.put(selectedFieldID, data.getValue());
                    }
                }
            }

            label.setUserFields(userSpecifiedLabels);
        }
    }

    protected Map<Integer, MeasurementVariable> convertToMap(List<MeasurementVariable>... variables) {
        Map<Integer, MeasurementVariable> map = new HashMap<>();

        for (List<MeasurementVariable> variableList : variables) {
            for (MeasurementVariable variable : variableList) {
                map.put(variable.getTermId(), variable);
            }
        }

        return map;
    }

    protected Map<Integer, String> extractDataForUserSpecifiedLabels(List<Integer> selectedFieldIDs, MeasurementRow measurementRow) {
        Map<Integer, String> values = new HashMap<>();

        for (Integer termID : selectedFieldIDs) {
            try {

                MeasurementData data = measurementRow.getMeasurementData(termID);
                if (data != null) {
                    String value = measurementRow.getMeasurementData(termID).getDisplayValue();
                    values.put(termID, value);
                }


            } catch (NumberFormatException e) {
                LOG.error(e.getMessage(),e);
            }

        }

        return values;
    }

    protected Map<String, List<MeasurementRow>> extractMeasurementRowsPerTrialInstance(List<MeasurementRow> dataRows) {
        // sort the observations by instance number, and then by experiment ID to simplify later process
        Collections.sort(dataRows, new Comparator<MeasurementRow>() {
            @Override public int compare(MeasurementRow o1, MeasurementRow o2) {
                String instanceID1 = o1.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
                String instanceID2 = o2.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();

                if (instanceID1.equals(instanceID2)) {
                    return new Integer(o1.getExperimentId()).compareTo(new Integer(o2.getExperimentId()));
                } else {
                    return instanceID1.compareTo(instanceID2);
                }
            }
        });

        Map<String, List<MeasurementRow>> measurements = new HashMap<>();

        for (MeasurementRow row : dataRows) {
            String trialInstance = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
            List<MeasurementRow> list = measurements.get(trialInstance);

            if (list == null) {
                list = new ArrayList<>();
                measurements.put(trialInstance, list);
            }

            list.add(row);
        }

        return measurements;
    }



    protected Map<String, String> generateAddedInformationField(FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo, StudyTrialInstanceInfo trialInstance, String barCode){
    	 Map<String,String> moreFieldInfo = new HashMap<String, String>();
         moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
         moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
         moreFieldInfo.put("fieldName", fieldMapTrialInstanceInfo.getFieldName());
         moreFieldInfo.put(SELECTED_NAME, trialInstance.getFieldbookName());
         moreFieldInfo.put("trialInstanceNumber", 
                 fieldMapTrialInstanceInfo.getTrialInstanceNo());
         moreFieldInfo.put(BARCODE, barCode);
         
         return moreFieldInfo;
    }

	private Map<Integer, ExportColumnValue> generateRowMap(List<Integer> selectedFieldIDs,
			Map<String, String> moreFieldInfo, FieldMapLabel fieldMapLabel) {
		Map<Integer, ExportColumnValue> rowMap = new HashMap<Integer, ExportColumnValue>();

        for (Integer selectedFieldID : selectedFieldIDs) {

            try {

                String value = getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, false);
                ExportColumnValue columnValue = new ExportColumnValue(selectedFieldID, value);
                rowMap.put(selectedFieldID, columnValue);
            } catch (NumberFormatException e) {
                LOG.error(e.getMessage(),e);
            }
        }

        return rowMap;
	}

	private List<ExportColumnHeader> generateColumnHeaders(List<Integer> selectedFieldIDs) {
    	List<ExportColumnHeader> columnHeaders = new ArrayList<ExportColumnHeader>();

        for (Integer selectedFieldID : selectedFieldIDs) {
            String headerName = getHeader(selectedFieldID);
            ExportColumnHeader columnHeader = new ExportColumnHeader(selectedFieldID,
                    headerName, true);
            columnHeaders.add(columnHeader);
        }


    	return columnHeaders;
	}

	/**
	 * Gets the available label fields.
	 * @param isTrial     the is trial
	 * @param hasFieldMap the has field map
	 * @param locale      the locale
	 * @return
	 */
	public List<LabelFields> getAvailableLabelFields(boolean isTrial, boolean hasFieldMap, Locale locale){
        List<LabelFields> labelFieldsList = new ArrayList<LabelFields>();
        
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.entry.num", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.gid", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.germplasm.name", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_YEAR, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_SEASON, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()));
        
        if(isTrial){
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()));
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()));
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.rep", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()));
        }else{
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()));
        }
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PLOT, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()));
        if(hasFieldMap){
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()));
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()));
            labelFieldsList.add(new LabelFields(
            		messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME, null, locale)
            		, AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()));
        }
        return labelFieldsList;
    }


    public List<LabelFields> getAvailableLabelFields(boolean isTrial, boolean hasFieldMap,
            Locale locale, int studyID) {
        List<LabelFields> labelFieldsList = new ArrayList<>();

        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_YEAR, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_SEASON, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()));

        labelFieldsList.add(new LabelFields(
                        messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PLOT, null, locale)
                        , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()));

        if (isTrial) {
            labelFieldsList.add(new LabelFields(
                                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME, null, locale)
                                , AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()));
            try {
                Workbook workbook = fieldbookMiddlewareService.getTrialDataSet(studyID);

                labelFieldsList.addAll(settingsService.retrieveTrialSettingsAsLabels(workbook));
                labelFieldsList.addAll(settingsService.retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(
                        workbook));
                labelFieldsList.addAll(settingsService.retrieveGermplasmDescriptorsAsLabels(
                        workbook));

            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(),e);
            }
        } else {
            labelFieldsList.add(new LabelFields(
                                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME, null, locale)
                                , AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()));
            try {
                Workbook workbook = fieldbookMiddlewareService.getNurseryDataSet(studyID);

                labelFieldsList.addAll(settingsService.retrieveNurseryManagementDetailsAsLabels(workbook));
                labelFieldsList.addAll(settingsService.retrieveGermplasmDescriptorsAsLabels(
                                        workbook));
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(),e);
            }
        }

        if (hasFieldMap) {
            labelFieldsList.add(new LabelFields(
                    messageSource
                            .getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()));
            labelFieldsList.add(new LabelFields(
                    messageSource
                            .getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES, null,
                                    locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()));
            labelFieldsList.add(new LabelFields(
                    messageSource
                            .getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()));
        }

        return labelFieldsList;
    }


    public boolean checkAndSetFieldmapProperties(UserLabelPrinting userLabelPrinting, FieldMapInfo fieldMapInfoDetail) {
    	//if there are datasets with fieldmap, check if all trial instances of the study have fieldmaps
        if (!fieldMapInfoDetail.getDatasetsWithFieldMap().isEmpty()) {
        	for (FieldMapDatasetInfo dataset : fieldMapInfoDetail.getDatasetsWithFieldMap()) {
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
    public LabelPrintingPresets getLabelPrintingPreset(Integer presetId, Integer presetType)
            throws MiddlewareQueryException {
        if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
            StandardPreset standardPreset = workbenchService.getStandardPresetById(presetId);

            return new LabelPrintingPresets(presetId,standardPreset.getName(),LabelPrintingPresets.STANDARD_PRESET);

        } else {
            ProgramPreset programPreset = presetDataManager.getProgramPresetById(presetId);

            return new LabelPrintingPresets(presetId,programPreset.getName(),LabelPrintingPresets.PROGRAM_PRESET);
        }
    }

    @Override
    public ProgramPreset getLabelPrintingProgramPreset(Integer programPresetId)
            throws MiddlewareQueryException {
        return  presetDataManager.getProgramPresetById(programPresetId);
    }

    @Override
    public List<LabelPrintingPresets> getAllLabelPrintingPresetsByName(String presetName,
            Integer programId, Integer presetType)
            throws MiddlewareQueryException {
        final List<LabelPrintingPresets> out = new ArrayList<>();

        final Project project = workbenchService.getProjectById(programId.longValue());

        if (LabelPrintingPresets.PROGRAM_PRESET == presetType) {
            List<ProgramPreset> presets = presetDataManager.getProgramPresetFromProgramAndToolByName(
                    presetName, uuidFactory.getCurrentProgramUUID(), workbenchService.getFieldbookWebTool().getToolId().intValue(), ToolSection.FBK_LABEL_PRINTING.name());

            for (ProgramPreset preset : presets) {
                out.add(new LabelPrintingPresets(preset.getProgramPresetId(),preset.getName(),LabelPrintingPresets.PROGRAM_PRESET));
            }
        } else {
            final String cropName = project.getCropType().getCropName();

            List<StandardPreset> standardPresets = workbenchService.getStandardPresetByCropAndPresetName(presetName,workbenchService.getFieldbookWebTool().getToolId().intValue(),cropName,ToolSection.FBK_LABEL_PRINTING.name());

            for (StandardPreset preset : standardPresets) {
                out.add(new LabelPrintingPresets(preset.getStandardPresetId(),preset.getName(),LabelPrintingPresets.STANDARD_PRESET));
            }
        }

        return out;
    }

	@Override
	public List<LabelPrintingPresets> getAllLabelPrintingPresets(Integer programId)
			throws LabelPrintingException {
		try {
			List<LabelPrintingPresets> allLabelPrintingPresets = new ArrayList<LabelPrintingPresets>();

			// 1. get the crop name of the particular programId,
			final Project project = workbenchService.getProjectById(programId.longValue());
			final String cropName = project.getCropType().getCropName();
            final Integer fieldbookToolId = workbenchService.getFieldbookWebTool().getToolId().intValue();

            // 2. retrieve the standard presets
			for (StandardPreset preset : workbenchService.getStandardPresetByCrop(
                    fieldbookToolId,cropName,ToolSection.FBK_LABEL_PRINTING.name())) {
				allLabelPrintingPresets.add(new LabelPrintingPresets(preset.getStandardPresetId(), preset.getName(),
						LabelPrintingPresets.STANDARD_PRESET));
			}

            // 3. add all program presets for fieldbook
			for (ProgramPreset preset : presetDataManager.getProgramPresetFromProgramAndTool(
                    uuidFactory.getCurrentProgramUUID(), fieldbookToolId,ToolSection.FBK_LABEL_PRINTING.name())) {
				allLabelPrintingPresets.add(new LabelPrintingPresets(preset.getProgramPresetId(), preset.getName(),
						LabelPrintingPresets.PROGRAM_PRESET));
			}

			return allLabelPrintingPresets;

		} catch (MiddlewareQueryException e) {
			throw new LabelPrintingException("label.printing.cannot.retrieve.presets",
					"database.connectivity.error", e.getMessage());
		}
	}

	@Override
	public String getLabelPrintingPresetConfig(int presetId,int presetType) throws LabelPrintingException {
		try {
			if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
				return workbenchService.getStandardPresetById(presetId).getConfiguration();
			} else {
				return presetDataManager.getProgramPresetById(presetId).getConfiguration();
			}
		} catch (MiddlewareQueryException e) {
			throw new LabelPrintingException("label.printing.cannot.retrieve.presets",
					"database.connectivity.error",e.getMessage());
		} catch (NullPointerException e) {
			throw new LabelPrintingException("label.printing.preset.does.not.exists",
					"label.printing.preset.does.not.exists",e.getMessage());
		}
	}

    @Override
    public void saveOrUpdateLabelPrintingPresetConfig(String settingsName,
            String xmlConfig,
            Integer programId) throws MiddlewareQueryException {
        // check if exists, override if true else add new
        List<LabelPrintingPresets> searchPresetList = this.getAllLabelPrintingPresetsByName(
                settingsName, programId, LabelPrintingPresets.PROGRAM_PRESET);

        if (!searchPresetList.isEmpty()) {
            // update
            ProgramPreset currentLabelPrintingPreset = this.getLabelPrintingProgramPreset(
                    searchPresetList.get(0).getId());
            currentLabelPrintingPreset.setConfiguration(xmlConfig);

            presetDataManager.saveOrUpdateProgramPreset(currentLabelPrintingPreset);
        } else {
            // add new
            ProgramPreset preset = new ProgramPreset();
            preset.setName(settingsName);
            preset.setProgramUuid(uuidFactory.getCurrentProgramUUID());
            preset.setToolId(workbenchService.getFieldbookWebTool().getToolId().intValue());
            preset.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
            preset.setConfiguration(xmlConfig);

            presetDataManager.saveOrUpdateProgramPreset(preset);
        }
    }

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setUuidFactory(ProgramUUIDFactory uuidFactory) {
		this.uuidFactory = uuidFactory;
	}
}