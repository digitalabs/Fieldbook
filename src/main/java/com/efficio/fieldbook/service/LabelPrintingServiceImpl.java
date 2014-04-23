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

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.util.LabelPaperFactory;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.template.LabelPaper;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
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
import com.lowagie.text.pdf.PdfContentByte;
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
    
    
    /**
     * Gets the cell height.
     *
     * @param numberOfRowsPerPage the number of rows per page
     * @param pageSizeId the page size id
     * @return the cell height
     */
    /*
    private float getCellHeight(int numberOfRowsPerPage, int pageSizeId){
        if(pageSizeId == AppConstants.SIZE_OF_PAPER_A4){
            if(numberOfRowsPerPage == 7){
                return 108f; //ok
            }else if(numberOfRowsPerPage == 8){
                return 97f;//ok
            }else if(numberOfRowsPerPage == 10){
                return 72.5f;
            }
        }else{
            if(numberOfRowsPerPage == 7){
                return 108f; //ok
            }else if(numberOfRowsPerPage == 8){
                return 98.1f;//ok
            }else if(numberOfRowsPerPage == 10){
                return 72.5f;//ok
            }
        }
           
        return 0f;
    }
    */
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
     */
    @Override
    public String generatePDFLabels(List<StudyTrialInstanceInfo> trialInstances
            , UserLabelPrinting userLabelPrinting,
            ByteArrayOutputStream baos) throws MiddlewareQueryException {

        // setUserLabelPrinting(form.getUserLabelPrinting());
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

        String currentDate = DateUtil.getCurrentDate();
        // String fileName = currentDate + ".pdf";

        String fileName = userLabelPrinting.getFilenameDLLocation();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            try {

                // Image image1 = Image.getInstance(imageLocation);

                // PageSize.A4
            	LabelPaper paper = LabelPaperFactory.generateLabelPaper(numberOfLabelPerRow, numberofRowsPerPageOfLabel, pageSizeId);
            	
                Rectangle pageSize = PageSize.LETTER;

                if (pageSizeId == AppConstants.SIZE_OF_PAPER_A4.getInt())
                    pageSize = PageSize.A4;

                Document document = new Document(pageSize);
                /*
                 * 2, 2, 33.3f, 5
                 * 15, 0, 42, 5
                 * 10, 0, 17, 5
                 * 10, 0, 17, 5
                 * 5, 0, 0, 5
                 * 15, 0, 37, 5
                 * 10, 0, 17, 5
                if (pageSizeId == AppConstants.SIZE_OF_PAPER_LETTER) {
                    if (numberofRowsPerPageOfLabel == 7)
                        document.setMargins(10, 0, 17, 5);
                    else if (numberofRowsPerPageOfLabel == 8)
                        document.setMargins(5, 0, 0, 5);
                    else if (numberofRowsPerPageOfLabel == 10)
                        document.setMargins(2, 2, 33.3f, 5);
                } else if (pageSizeId == AppConstants.SIZE_OF_PAPER_A4) {
                    if (numberofRowsPerPageOfLabel == 7)
                        document.setMargins(15, 0, 42, 5);
                    else if (numberofRowsPerPageOfLabel == 8)
                        document.setMargins(15, 0, 37, 5);
                    else if (numberofRowsPerPageOfLabel == 10)
                        document.setMargins(6, 2, 17.5f, 5);
                }
				*/
                //float marginLeft, float marginRight, float marginTop, float marginBottom
                document.setMargins(paper.getMarginLeft(), paper.getMarginRight(), paper.getMarginTop(), paper.getMarginBottom());
                
                // PdfWriter writer = PdfWriter.getInstance(document, baos);
                PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
                // step 3
                document.open();
                // step 4

                PdfContentByte canvas = writer.getDirectContent();

                int i = 0;
                int fixTableRowSize = numberOfLabelPerRow;
                PdfPTable table = new PdfPTable(fixTableRowSize);

                // table.writeSelectedRows(0, -1, 10, 12, canvas);
                float columnWidthSize = 265f;// 180f;
                float[] widthColumns = new float[fixTableRowSize];

                for (int counter = 0; counter < widthColumns.length; counter++) {
                    widthColumns[counter] = columnWidthSize;
                }

                table.setWidths(widthColumns);
                table.setWidthPercentage(100);
                int width = 600;
                int height = 75;

                List<File> filesToBeDeleted = new ArrayList<File>();
                //float cellHeight = getCellHeight(numberofRowsPerPageOfLabel, pageSizeId);
                float cellHeight = paper.getCellHeight();

                for (StudyTrialInstanceInfo trialInstance : trialInstances) {
                    FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();

                    Map<String, String> moreFieldInfo = new HashMap<String, String>();
                    moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
                    moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
                    moreFieldInfo.put("selectedName", trialInstance.getFieldbookName());
                    moreFieldInfo.put("trialInstanceNumber", fieldMapTrialInstanceInfo.getTrialInstanceNo());

                    for (FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {

                        i++;
                        String barcodeLabel = generateBarcodeField(
                                moreFieldInfo, fieldMapLabel, firstBarcodeField,
                                secondBarcodeField, thirdBarcodeField, barcodeNeeded);
                        if ("0".equalsIgnoreCase(barcodeNeeded)) {
                            barcodeLabel = " ";
                        }

                        BitMatrix bitMatrix = new Code128Writer().encode(barcodeLabel, 
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

                        //float fontSize = 6.8f;
                        float fontSize = paper.getFontSize();
                        //if (numberofRowsPerPageOfLabel == 10)
                        //    fontSize = 4.8f;

                        Font fontNormal = FontFactory.getFont("Arial", fontSize, Font.NORMAL);

                        // cell.addElement(mainImage);
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
						
                        //cell.setBorderColor(Color.BLUE);
                        
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

                                //table.setSpacingAfter(9f);
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

                    int needed = fixTableRowSize - numberOfLabelPerRow;
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
            , String thirdField, String barcodeNeeded){
        StringBuffer buffer = new StringBuffer();
        List<String> fieldList = new ArrayList<String>();
        fieldList.add(firstField);
        fieldList.add(secondField);
        fieldList.add(thirdField);
        
        for(String barcodeLabel : fieldList){
            if(barcodeLabel.equalsIgnoreCase("")){
                continue;
            }
            if(!buffer.toString().equalsIgnoreCase("")){
                buffer.append(delimiter);
            }
            buffer.append(getSpecificInfo(moreFieldInfo, fieldMapLabel, barcodeLabel, true));
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
        StringBuffer buffer = new StringBuffer();
        StringTokenizer token = new StringTokenizer(selectedFields, ",");
        int i = 0;
        while(token.hasMoreTokens()){
            String barcodeLabel = token.nextToken();
            
            if(i == rowNumber){
                if(barcodeLabel != null && !barcodeLabel.equalsIgnoreCase("")){                    
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

        StringBuffer buffer = new StringBuffer();
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
        } else {
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
        StringBuffer buffer = new StringBuffer();
        
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
        } else {
        }
        String stemp = buffer.toString();
        if(stemp != null && "null".equalsIgnoreCase(stemp))
        	stemp = " ";
        
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
        int pageSizeId = Integer.parseInt(userLabelPrinting.getSizeOfLabelSheet());
        int numberOfLabelPerRow = Integer.parseInt(userLabelPrinting.getNumberOfLabelPerRow());
        int numberofRowsPerPageOfLabel = 
                Integer.parseInt(userLabelPrinting.getNumberOfRowsPerPageOfLabel());
        int totalPerPage = numberOfLabelPerRow * numberofRowsPerPageOfLabel;
        String leftSelectedFields = userLabelPrinting.getLeftSelectedLabelFields();
        String rightSelectedFields = userLabelPrinting.getRightSelectedLabelFields();
        String barcodeNeeded = userLabelPrinting.getBarcodeNeeded();
        
        String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();
        
        String currentDate = DateUtil.getCurrentDate();
        //String fileName = currentDate + ".xls";
        String fileName = userLabelPrinting.getFilenameDLLocation();
        try {
            
                    HSSFWorkbook workbook = new HSSFWorkbook();
                    String sheetName = SettingsUtil.cleanSheetAndFileName(userLabelPrinting.getName());
                    if(sheetName == null)
                        sheetName = "Labels";
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
                    Row row = labelPrintingSheet.createRow(rowIndex++);
                                        
                    //we add all the selected fields header
                    StringTokenizer token = new StringTokenizer(leftSelectedFields, ",");
                    while(token.hasMoreTokens()){
                        String headerId = token.nextToken();
                        String headerName = getHeader(headerId);
                        Cell summaryCell = row.createCell(columnIndex++);
                        summaryCell.setCellValue(headerName);
                        summaryCell.setCellStyle(labelStyle);
                    }
                    token = new StringTokenizer(rightSelectedFields, ",");
                    while(token.hasMoreTokens()){
                        String headerId = token.nextToken();
                        String headerName = getHeader(headerId);
                        Cell summaryCell = row.createCell(columnIndex++);
                        summaryCell.setCellValue(headerName);
                        summaryCell.setCellStyle(labelStyle);
                    }
                    
                    //we populate the info now
                    int i = 0;
                    for(StudyTrialInstanceInfo trialInstance : trialInstances){
                        FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = 
                                trialInstance.getTrialInstance();
                        
                        Map<String,String> moreFieldInfo = new HashMap<String, String>();
                        moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
                        moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
                        moreFieldInfo.put("selectedName", trialInstance.getFieldbookName());
                        moreFieldInfo.put("trialInstanceNumber", 
                                fieldMapTrialInstanceInfo.getTrialInstanceNo());
                        
                        for(FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()){
                            row = labelPrintingSheet.createRow(rowIndex++);    
                            columnIndex = 0;
                            i++;
                            
                            token = new StringTokenizer(leftSelectedFields, ",");
                            while(token.hasMoreTokens()){
                                String headerId = token.nextToken();
                                String leftText = getSpecificInfo(moreFieldInfo, fieldMapLabel, headerId, false);
                                Cell summaryCell = row.createCell(columnIndex++);
                                summaryCell.setCellValue(leftText);
                                //summaryCell.setCellStyle(labelStyle);
                            }
                            token = new StringTokenizer(rightSelectedFields, ",");
                            while(token.hasMoreTokens()){
                                String headerId = token.nextToken();
                                String rightText = getSpecificInfo(moreFieldInfo, fieldMapLabel, headerId, false);
                                Cell summaryCell = row.createCell(columnIndex++);
                                summaryCell.setCellValue(rightText);
                                //summaryCell.setCellStyle(labelStyle);
                            }
                           
                        }
                    }
                    
                    for(int columnPosition = 0; columnPosition< columnIndex; columnPosition++) {
                        labelPrintingSheet.autoSizeColumn((short) (columnPosition));
                   }

                    //Write the excel file
                    
                    FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                    //workbook.write(baos);
                    workbook.write(fileOutputStream);
                    fileOutputStream.close();
                    //return fileOutputStream;
      
            
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } 
        return fileName;
    }
    
    
}
