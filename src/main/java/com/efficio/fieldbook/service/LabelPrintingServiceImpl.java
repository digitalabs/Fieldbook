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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class LabelPrintingServiceImpl implements LabelPrintingService{

    private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceImpl.class);
    
   
    
    private String delimiter = "|";
    
    @Resource
    private ResourceBundleMessageSource messageSource;
    
    
    private float getCellHeight(int numberOfRowsPerPage){
        if(numberOfRowsPerPage == 7)
            return 104f;
        else if(numberOfRowsPerPage == 8)
            return 93f;
        else if(numberOfRowsPerPage == 10)
            return 96f;
        return 0f;
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
     */
    @Override
    public String generatePDFLabels(List<StudyTrialInstanceInfo> trialInstances, UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos)
            throws MiddlewareQueryException {
        
      //setUserLabelPrinting(form.getUserLabelPrinting());
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
        
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = currentDate + ".pdf";
        try {
            

            
            
            try {
                
                //Image image1 = Image.getInstance(imageLocation);
               
                //PageSize.A4
                Rectangle pageSize = PageSize.LETTER;
                
                if(pageSizeId == AppConstants.SIZE_OF_PAPER_A4)
                    pageSize = PageSize.A4;
                
                Document document = new Document(pageSize);
                if(numberofRowsPerPageOfLabel == 7)
                    document.setMargins(5, 0, 10, 5);
                else if(numberofRowsPerPageOfLabel == 8)
                    document.setMargins(5, 0, 0, 5);
                else if(numberofRowsPerPageOfLabel == 10)
                    document.setMargins(5, 5, 20, 20);
                // step 2
                
                //PdfWriter.getInstance(document, new FileOutputStream(fileName));
                PdfWriter.getInstance(document, baos);
                // step 3
                document.open();
                // step 4
               
                
                int i = 0;
                int fixTableRowSize = numberOfLabelPerRow;
                PdfPTable table = new PdfPTable(fixTableRowSize);
                float columnWidthSize = 265f;//180f;
                float[] widthColumns = new float[fixTableRowSize];
                
                for(int counter = 0 ; counter < widthColumns.length ; counter++){
                    widthColumns[counter] = columnWidthSize;
                }
                
                table.setWidths(widthColumns);
                table.setWidthPercentage(100);
                int width = 600; 
                int height = 95;
                
                
                List<File> filesToBeDeleted = new ArrayList<File>(); 
                float cellHeight = getCellHeight(numberofRowsPerPageOfLabel);                                
                
                for(StudyTrialInstanceInfo trialInstance : trialInstances){
                    FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance(); 
                    /*
                    ;
                     ; //trial or nursery
                    
                    fieldMapTrialInstanceInfo.get //trial instance number ??? tiff
                    */
                    
                    Map<String,String> moreFieldInfo = new HashMap<String, String>();
                    moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
                    moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
                    moreFieldInfo.put("selectedName", trialInstance.getFieldbookName());
                    moreFieldInfo.put("trialInstanceNumber", fieldMapTrialInstanceInfo.getTrialInstanceNo());
                    
                    for(FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()){
                           
          
                        i++;
                        String barcodeLabel = generateBarcodeField(moreFieldInfo, fieldMapLabel, firstBarcodeField, secondBarcodeField, thirdBarcodeField, barcodeNeeded);
                        
                        
                       
                        BitMatrix bitMatrix = new Code128Writer().encode(barcodeLabel,BarcodeFormat.CODE_128,width,height,null);
                        String imageLocation = Math.random() + ".png";
                        File imageFile = new File(imageLocation);
                        FileOutputStream fout = new FileOutputStream(imageFile);
                        MatrixToImageWriter.writeToStream(bitMatrix, "png", fout);
                        filesToBeDeleted.add(imageFile);
                        
                        /*
                        BufferedImage src = ImageIO.read(imageFile);
                        BufferedImage thumbnail =
                                Scalr.resize(src, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT,
                                             200, 50, Scalr.OP_ANTIALIAS);
                        ImageIO.write(thumbnail, "png", imageFile);
                        */
                        
                        Image mainImage = Image.getInstance(imageLocation);
                        //File.createTempFile();
                        
                        
                        PdfPCell cell = new PdfPCell();
                        cell.setFixedHeight(cellHeight);
                        cell.setNoWrap(false);
                        cell.setPadding(5f);
                        //cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        
                        //Paragraph paragraph1 = new Paragraph();
                        
                        //String selectedLabel = "";
                        //paragraph1.add("test" + i);
                        //cell.addElement(paragraph1);  
                        
                        
                        Font fontNormal = FontFactory.getFont("Arial", 6, Font.NORMAL);
                        cell.addElement(mainImage);
                        
                        
                        
                        
                        cell.addElement(new Paragraph());
                        for(int row = 0 ; row < 5 ; row++){
                            PdfPTable innerTableInfo = new PdfPTable(2);
                            String leftText = generateBarcodeLabel(moreFieldInfo, fieldMapLabel, leftSelectedFields, row);
                            PdfPCell cellInnerLeft = new PdfPCell(new Paragraph(leftText, fontNormal));
                            cellInnerLeft.setBorder(Rectangle.NO_BORDER);                         
                            cellInnerLeft.setBackgroundColor(Color.white);
                            innerTableInfo.addCell(cellInnerLeft);
                            
                            String rightText = generateBarcodeLabel(moreFieldInfo, fieldMapLabel, rightSelectedFields, row);
                            PdfPCell cellInnerRight = new PdfPCell(new Paragraph(rightText, fontNormal));
                            cellInnerRight.setBorder(Rectangle.NO_BORDER);                         
                            cellInnerRight.setBackgroundColor(Color.white);
                            innerTableInfo.addCell(cellInnerRight);
                            
                            cell.addElement(innerTableInfo);
                        }
                        
                        /*
                        cell.setBorder(Rectangle.NO_BORDER);                         
                        cell.setBackgroundColor(Color.white);
                        */
                        //cell.addElement(new Paragraph("\n"));
                        
                        table.addCell(cell);
                                     
                        if(i % numberOfLabelPerRow == 0){
                            //we go the next line
                            
                            int needed = fixTableRowSize - numberOfLabelPerRow;
                            
                            for(int neededCount = 0 ; neededCount < needed ; neededCount++){
                                PdfPCell cellNeeded = new PdfPCell(); 
                                cellNeeded.setBorder(Rectangle.NO_BORDER);                         
                                cellNeeded.setBackgroundColor(Color.white);
                                table.addCell(cellNeeded);
                            }
                            
                            table.completeRow();
                            document.add(table);
                            table = new PdfPTable(fixTableRowSize);  
                            //table.setWidthPercentage(100);
                            table.setWidths(widthColumns);
                            table.setWidthPercentage(100);
                            
                        }
                        if(i % totalPerPage == 0){
                            //we go the next page
                            document.newPage();
                        } 
                        fout.flush();
                        fout.close();
                        
                        
                    }
                }
          
                
                document.close();
                for(File file : filesToBeDeleted){
                    
                    file.delete();
                }
                
            }catch (FileNotFoundException e) {
                LOG.error(e.getMessage(), e);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
                       
            
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return fileName;
    }
    
    private String generateBarcodeField(Map<String,String> moreFieldInfo, FieldMapLabel fieldMapLabel, String firstField, String secondField, String thirdField, String barcodeNeeded){
        StringBuffer buffer = new StringBuffer();
        List<String> fieldList = new ArrayList<String>();
        fieldList.add(firstField);
        fieldList.add(secondField);
        fieldList.add(thirdField);
        
        for(String barcodeLabel : fieldList){
            if(barcodeLabel.equalsIgnoreCase(""))
                continue;
            
            if(!buffer.toString().equalsIgnoreCase("")){
                buffer.append(delimiter);
            }
            buffer.append(getSpecificInfo(moreFieldInfo, fieldMapLabel, barcodeLabel));
        }
        return buffer.toString();
    }
    private String generateBarcodeLabel(Map<String,String> moreFieldInfo, 
            FieldMapLabel fieldMapLabel, String selectedFields, int rowNumber){
        StringBuffer buffer = new StringBuffer();
        StringTokenizer token = new StringTokenizer(selectedFields, ",");
        int i = 0;
        while(token.hasMoreTokens()){
            String barcodeLabel = token.nextToken();
            
            if(i == rowNumber){
                if(barcodeLabel != null && !barcodeLabel.equalsIgnoreCase("")){                    
                    buffer.append(getSpecificInfo(moreFieldInfo, fieldMapLabel, barcodeLabel));
                    break;
                }
            }
            i++;
            
            
        }
        return buffer.toString();
    }
    private String getHeader(String headerId){
        Locale locale = LocaleContextHolder.getLocale();

        StringBuffer buffer = new StringBuffer();
        switch(Integer.parseInt(headerId)){           
            
            case AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM:
                buffer.append(messageSource.getMessage("label.printing.available.fields.entry.num", null, locale));                 
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_GID: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.gid", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.germplasm.name", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_YEAR: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.year", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_SEASON: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.season", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.nursery.name", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.trial.name", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_REP: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.rep", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.location", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.block.name", null, locale));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_PLOT: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.plot", null, locale));
                break;
                
            case AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE: 
                buffer.append(messageSource.getMessage("label.printing.available.fields.parentage", null, locale));
                break;
           
            default: break;    
        }
        return buffer.toString();
    }
    private String getSpecificInfo(Map<String,String> moreFieldInfo, FieldMapLabel fieldMapLabel, String barcodeLabel){
        StringBuffer buffer = new StringBuffer();
        switch(Integer.parseInt(barcodeLabel)){
            
            
            case AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM:
                buffer.append(fieldMapLabel.getEntryNumber());
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_GID: 
                buffer.append(fieldMapLabel.getGid().toString());
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME: 
                buffer.append(fieldMapLabel.getGermplasmName());
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_YEAR: 
                buffer.append(fieldMapLabel.getStartYear());
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_SEASON: 
                buffer.append(fieldMapLabel.getSeason());
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME: 
                buffer.append(moreFieldInfo.get("selectedName"));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME: 
                buffer.append(moreFieldInfo.get("selectedName"));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM: 
                buffer.append(moreFieldInfo.get("trialInstanceNumber"));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_REP: 
                buffer.append(fieldMapLabel.getRep());
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION: 
                buffer.append(moreFieldInfo.get("locationName")); 
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME: 
                buffer.append(moreFieldInfo.get("blockName"));
                break;
            case AppConstants.AVAILABLE_LABEL_FIELDS_PLOT: 
                buffer.append(fieldMapLabel.getPlotNo());
                break;
                
            case AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE: 
                buffer.append(fieldMapLabel.getPedigree() == null ? "" : fieldMapLabel.getPedigree());
                break;
           
            default: break;    
        }
        return buffer.toString();
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
        int numberofRowsPerPageOfLabel = Integer.parseInt(userLabelPrinting.getNumberOfRowsPerPageOfLabel());
        int totalPerPage = numberOfLabelPerRow * numberofRowsPerPageOfLabel;
        String leftSelectedFields = userLabelPrinting.getLeftSelectedLabelFields();
        String rightSelectedFields = userLabelPrinting.getRightSelectedLabelFields();
        String barcodeNeeded = userLabelPrinting.getBarcodeNeeded();
        
        String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();
        
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = currentDate + ".xls";
        try {
            
                    HSSFWorkbook workbook = new HSSFWorkbook();
                    Sheet labelPrintingSheet = workbook.createSheet(userLabelPrinting.getName());
                
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
                        FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();
                        
                        Map<String,String> moreFieldInfo = new HashMap<String, String>();
                        moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
                        moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
                        moreFieldInfo.put("selectedName", trialInstance.getFieldbookName());
                        moreFieldInfo.put("trialInstanceNumber", fieldMapTrialInstanceInfo.getTrialInstanceNo());
                        
                        for(FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()){
                            row = labelPrintingSheet.createRow(rowIndex++);    
                            columnIndex = 0;
                            i++;
                            
                            
                            token = new StringTokenizer(leftSelectedFields, ",");
                            while(token.hasMoreTokens()){
                                String headerId = token.nextToken();
                                String leftText = getSpecificInfo(moreFieldInfo, fieldMapLabel, headerId);
                                Cell summaryCell = row.createCell(columnIndex++);
                                summaryCell.setCellValue(leftText);
                                //summaryCell.setCellStyle(labelStyle);
                            }
                            token = new StringTokenizer(rightSelectedFields, ",");
                            while(token.hasMoreTokens()){
                                String headerId = token.nextToken();
                                String rightText = getSpecificInfo(moreFieldInfo, fieldMapLabel, headerId);
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
                    
                    //FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                    workbook.write(baos);
                    //fileOutputStream.close();
                    //return fileOutputStream;
      
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return fileName;
    }
    
    
}
