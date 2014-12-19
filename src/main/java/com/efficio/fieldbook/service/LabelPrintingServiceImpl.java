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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
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
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.util.LabelPaperFactory;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
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
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The Class LabelPrintingServiceImpl.
 */
@Service
public class LabelPrintingServiceImpl implements LabelPrintingService{

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

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
	 */

    
    private static final String BARCODE = "Barcode";
    
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
                    FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();

                    Map<String, String> moreFieldInfo = generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");

                    for (FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {

                        i++;
                        String barcodeLabelForCode = generateBarcodeField(
                                moreFieldInfo, fieldMapLabel, firstBarcodeField,
                                secondBarcodeField, thirdBarcodeField, false);
                        String barcodeLabel = generateBarcodeField(
                                moreFieldInfo, fieldMapLabel, firstBarcodeField,
                                secondBarcodeField, thirdBarcodeField, true);
                        if ("0".equalsIgnoreCase(barcodeNeeded)) {
                            barcodeLabel = " ";
                            barcodeLabelForCode = " ";
                        }
                    	if(barcodeLabelForCode != null && barcodeLabelForCode.length() > 80){
                    		throw new LabelPrintingException("label.printing.label.too.long", barcodeLabelForCode, "label.printing.label.too.long");
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

                                Font fontNormalData = FontFactory.getFont("Arial", 5.0f, Font.NORMAL);
                                PdfPCell cellInnerData = 
                                        new PdfPCell(new Phrase(barcodeLabel, fontNormalData));

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

                            String leftText = generateBarcodeLabel(
                                    moreFieldInfo, fieldMapLabel, leftSelectedFields, row);
                            PdfPCell cellInnerLeft = new PdfPCell(new Paragraph(leftText, fontNormal));

                            cellInnerLeft.setBorder(Rectangle.NO_BORDER);
                            cellInnerLeft.setBackgroundColor(Color.white);
                            cellInnerLeft.setPaddingBottom(0.5f);
                            cellInnerLeft.setPaddingTop(0.5f);

                            innerTableInfo.addCell(cellInnerLeft);

                            String rightText = generateBarcodeLabel(
                                    moreFieldInfo, fieldMapLabel, rightSelectedFields,
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
     * @param barcodeNeeded the barcode needed
     * @return the string
     */
    private String generateBarcodeField(Map<String,String> moreFieldInfo
            , FieldMapLabel fieldMapLabel, String firstField, String secondField
            , String thirdField, boolean includeLabel){
        StringBuilder buffer = new StringBuilder();
        List<String> fieldList = new ArrayList<String>();
        fieldList.add(firstField);
        fieldList.add(secondField);
        fieldList.add(thirdField);
        
        for(String barcodeLabel : fieldList){
            if(("").equalsIgnoreCase(barcodeLabel)){
                continue;
            }
            if(!("").equalsIgnoreCase(buffer.toString())){
                buffer.append(delimiter);
            }
            buffer.append(getSpecificInfo(moreFieldInfo, fieldMapLabel, barcodeLabel, includeLabel));
        }
        return buffer.toString();
    }
    
    /**
     * Generate barcode label.
     *
     * @param moreFieldInfo the more field info
     * @param fieldMapLabel the field map label
     * @param selectedFields the selected fields
     * @param rowNumber the row number
     * @return the string
     */
    private String generateBarcodeLabel(Map<String,String> moreFieldInfo, 
            FieldMapLabel fieldMapLabel, String selectedFields, int rowNumber){
        StringBuilder buffer = new StringBuilder();
        StringTokenizer token = new StringTokenizer(selectedFields, ",");
        int i = 0;
        while(token.hasMoreTokens()){
            String barcodeLabel = token.nextToken();
            
            if(i == rowNumber){
                if(barcodeLabel != null && !("").equalsIgnoreCase(barcodeLabel)){                    
                    buffer.append(getSpecificInfo(moreFieldInfo, fieldMapLabel, barcodeLabel, true));
                    break;
                }
            }
            i++;
            
            
        }
        return buffer.toString();
    }
    
    /**
     * Gets the header.
     *
     * @param headerId the header id
     * @return the header
     */
    private String getHeader(String headerId){
        Locale locale = LocaleContextHolder.getLocale();

        StringBuilder buffer = new StringBuilder();
        int parseInt = Integer.parseInt(headerId);
        if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.entry.num", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.gid", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.germplasm.name", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.year", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.season", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.nursery.name", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.trial.name", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.trial.instance.num", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.rep", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.location", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.block.name", null, locale));
        } else if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.plot", null, locale));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.parentage", null, locale));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
            buffer.append(messageSource.getMessage(
                    "label.printing.available.fields.plot.coordinates", null, locale));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
        	buffer.append(messageSource.getMessage(
        			"label.printing.available.fields.field.name", null, locale));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
        	buffer.append(messageSource.getMessage(
        			"label.printing.available.fields.barcode", null, locale));
        }
        return buffer.toString();
    }
    
    /**
     * Gets the specific info.
     *
     * @param moreFieldInfo the more field info
     * @param fieldMapLabel the field map label
     * @param barcodeLabel the barcode label
     * @return the specific info
     */
    private String getSpecificInfo(
            Map<String,String> moreFieldInfo, FieldMapLabel fieldMapLabel, String barcodeLabel, boolean includeHeaderLabel){
        StringBuilder buffer = new StringBuilder();
        
        int parseInt = Integer.parseInt(barcodeLabel);
        String headerName = getHeader(barcodeLabel);
        if (parseInt ==  AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
            buffer.append(fieldMapLabel.getEntryNumber());
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
            String gidTemp = fieldMapLabel.getGid() == null 
                            ? "" : fieldMapLabel.getGid().toString();
            buffer.append(gidTemp);
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
            buffer.append(fieldMapLabel.getGermplasmName());
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
            buffer.append(fieldMapLabel.getStartYear());
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
            buffer.append(fieldMapLabel.getSeason());
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
            buffer.append(moreFieldInfo.get("selectedName"));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
            buffer.append(moreFieldInfo.get("selectedName"));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
            buffer.append(moreFieldInfo.get("trialInstanceNumber"));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
            buffer.append(fieldMapLabel.getRep());
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()) {
            buffer.append(moreFieldInfo.get("locationName"));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
            buffer.append(moreFieldInfo.get("blockName"));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()) {
            buffer.append(fieldMapLabel.getPlotNo());
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()) {
            buffer.append(fieldMapLabel.getPedigree() == null ? "" : fieldMapLabel.getPedigree());
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
            buffer.append(fieldMapLabel.getPlotCoordinate());
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
        	buffer.append(moreFieldInfo.get("fieldName"));
        } else if (parseInt == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
        	buffer.append(moreFieldInfo.get("barcode"));
        }
        String stemp = buffer.toString();
        if(stemp != null && "null".equalsIgnoreCase(stemp)) {
        	stemp = " ";
        }
        
        if(includeHeaderLabel && headerName != null){
        	stemp = headerName + " : " + stemp;
        }
        
    	return stemp;
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
                    if(sheetName == null) {
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
                    HSSFColor myColor = palette.findSimilarColor(179,165, 165);
                    // get the palette index of that color 
                    short palIndex = myColor.getIndex();
                    // code to get the style for the cell goes here
                    mainHeaderStyle.setFillForegroundColor(palIndex);           
                    mainHeaderStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                    
                    CellStyle mainSubHeaderStyle = workbook.createCellStyle();
                    
                    HSSFPalette paletteSubHeader = workbook.getCustomPalette();
                    // get the color which most closely matches the color you want to use
                    HSSFColor myColorSubHeader = paletteSubHeader.findSimilarColor(190,190, 186);
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
                    
                    if(includeHeader){
                    	row = labelPrintingSheet.createRow(rowIndex++);
                    	//we add all the selected fields header                                       
                        printHeaderFields(includeHeader, mainSelectedFields, row, columnIndex, labelStyle);
                    }
                    
                    
                    //we populate the info now
                    for(StudyTrialInstanceInfo trialInstance : trialInstances){
                        FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = 
                                trialInstance.getTrialInstance();
                        
                        Map<String,String> moreFieldInfo = generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");
                        
                       
                        
                        for(FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()){
                            row = labelPrintingSheet.createRow(rowIndex++);    
                            columnIndex = 0;
                            
                            String barcodeLabelForCode = generateBarcodeField(
                                    moreFieldInfo, fieldMapLabel, firstBarcodeField,
                                    secondBarcodeField, thirdBarcodeField, false);
                            moreFieldInfo.put("barcode", barcodeLabelForCode);
                            
                            StringTokenizer token = new StringTokenizer(mainSelectedFields, ",");
                            while(token.hasMoreTokens()){
                                String headerId = token.nextToken();
                                String leftText = getSpecificInfo(moreFieldInfo, fieldMapLabel, headerId, false);
                                Cell summaryCell = row.createCell(columnIndex++);
                                summaryCell.setCellValue(leftText);
                            }                           
                        }
                    }
                    
                    for(int columnPosition = 0; columnPosition< columnIndex; columnPosition++) {
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
    protected String appendBarcode(boolean isBarcodeNeeded, String mainSelectedFields){
    	if(isBarcodeNeeded){
    		mainSelectedFields += "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt();
    	}
    	return mainSelectedFields;
    }
    protected void printHeaderFields(boolean includeHeader, String mainSelectedFields, Row row, int columnIndex, CellStyle labelStyle){
    	if(includeHeader){
            StringTokenizer token = new StringTokenizer(mainSelectedFields, ",");
            while(token.hasMoreTokens()){
                String headerId = token.nextToken();
                String headerName = getHeader(headerId);
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
    	List<ExportColumnHeader> exportColumnHeaders = generateColumnHeaders(mainSelectedFields);
    	
		List<Map<Integer, ExportColumnValue>> exportColumnValues = generateColumnValues(trialInstances, mainSelectedFields, userLabelPrinting);
		
		exportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName, includeHeader);
    	
    	return fileName;
    }
    
    private List<Map<Integer, ExportColumnValue>> generateColumnValues(List<StudyTrialInstanceInfo> trialInstances, String mainSelectedFields, UserLabelPrinting userLabelPrinting) {
    	List<Map<Integer, ExportColumnValue>> columnValues = new ArrayList<Map<Integer, ExportColumnValue>>();
    	
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
                moreFieldInfo.put("barcode", barcodeLabelForCode);
                
            	Map<Integer, ExportColumnValue> rowMap = generateRowMap(mainSelectedFields, moreFieldInfo, fieldMapLabel);
            	columnValues.add(rowMap);
            }
        }
        
        return columnValues;
	}

    protected Map<String, String> generateAddedInformationField(FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo, StudyTrialInstanceInfo trialInstance, String barCode){
    	 Map<String,String> moreFieldInfo = new HashMap<String, String>();
         moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
         moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
         moreFieldInfo.put("fieldName", fieldMapTrialInstanceInfo.getFieldName());
         moreFieldInfo.put("selectedName", trialInstance.getFieldbookName());
         moreFieldInfo.put("trialInstanceNumber", 
                 fieldMapTrialInstanceInfo.getTrialInstanceNo());
         moreFieldInfo.put("barcode", barCode);
         
         return moreFieldInfo;
    }
	private Map<Integer, ExportColumnValue> generateRowMap(String leftSelectedFields,
			Map<String, String> moreFieldInfo, FieldMapLabel fieldMapLabel) {
		Map<Integer, ExportColumnValue> rowMap = new HashMap<Integer, ExportColumnValue>();    
        
        StringTokenizer token = new StringTokenizer(leftSelectedFields, ",");
        while(token.hasMoreTokens()){
            String headerId = token.nextToken();
            String value = getSpecificInfo(moreFieldInfo, fieldMapLabel, headerId, false);
            ExportColumnValue columnValue = new ExportColumnValue(Integer.parseInt(headerId), value);
            rowMap.put(Integer.parseInt(headerId), columnValue);
        }
        
        return rowMap;
	}

	private List<ExportColumnHeader> generateColumnHeaders(String selectedFields) {
    	List<ExportColumnHeader> columnHeaders = new ArrayList<ExportColumnHeader>();
    	StringTokenizer token = new StringTokenizer(selectedFields, ",");
    	
    	while (token.hasMoreTokens()) {
    		String headerId = token.nextToken();
            String headerName = getHeader(headerId);
            ExportColumnHeader columnHeader = new ExportColumnHeader(Integer.parseInt(headerId), headerName, true);
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
                messageSource.getMessage("label.printing.available.fields.parentage", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.year", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.season", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.location", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()));
        
        if(isTrial){
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.trial.name", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()));
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()));
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.rep", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()));
        }else{
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.nursery.name", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()));
        }
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.plot", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()));
        if(hasFieldMap){
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.block.name", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()));
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.plot.coordinates", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()));
            labelFieldsList.add(new LabelFields(
            		messageSource.getMessage("label.printing.available.fields.field.name", null, locale)
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
                    programId, fieldbookToolId,ToolSection.FBK_LABEL_PRINTING.name())) {
				allLabelPrintingPresets.add(new LabelPrintingPresets(preset.getProgramPresetsId(), preset.getName(),
						LabelPrintingPresets.PROGRAM_PRESET));
			}

			return allLabelPrintingPresets;

		} catch (MiddlewareQueryException e) {
			throw new LabelPrintingException("label.printing.cannot.retrieve.presets",
					"database.connectivity.error", e.getMessage());
		}
	}

	@Override
	public String getLabelPrintingPresetConfig(int presetType, int presetId) throws LabelPrintingException {
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

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
    
    

}