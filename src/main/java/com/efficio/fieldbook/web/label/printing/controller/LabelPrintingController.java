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
package com.efficio.fieldbook.web.label.printing.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.form.LabelPrintingForm;
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


@Controller
@RequestMapping({LabelPrintingController.URL})
public class LabelPrintingController extends AbstractBaseFieldbookController{
 
     /** The Constant LOG. */
     private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingController.class);
    
    /** The Constant URL. */
    public static final String URL = "/LabelPrinting/specifyLabelDetails";
    
    @Resource
    private UserLabelPrinting userLabelPrinting;  
    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    private static final int BUFFER_SIZE = 4096 * 4;
    
    @RequestMapping(value="/trial/{id}", method = RequestMethod.GET)
    public String showTrialLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
            Model model, HttpSession session, @PathVariable int id ) {
        session.invalidate();
        Study study = null;
        List<FieldMapInfo> fieldMapInfoList = null;
        FieldMapInfo fieldMapInfo = null;
        try {
            study = fieldbookMiddlewareService.getStudy(id);
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(id);
            fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfTrial(ids);
            for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
                fieldMapInfo = fieldMapInfoDetail;
            }
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        getUserLabelPrinting().setStudy(study);
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setBarcodeNeeded("1");
        
        /*
        getUserLabelPrinting().setName(study.getName());
        getUserLabelPrinting().setTitle(study.getTitle());
        getUserLabelPrinting().setObjective(study.getObjective());
       
        getUserLabelPrinting().setNumberOfInstances("2");
        getUserLabelPrinting().setTotalNumberOfLabelToPrint("50");
         
        getUserLabelPrinting().setSizeOfLabelSheet("A4");
        getUserLabelPrinting().setNumberOfRowsPerPageOfLabel("10");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        */
        form.setUserLabelPrinting(getUserLabelPrinting());
        
        model.addAttribute("availableFields",getAvailableLabelFields(true, false));
        
        form.setIsTrial(true);
        return super.show(model);
    }
    
    @RequestMapping(value="/nursery/{id}", method = RequestMethod.GET)
    public String showNurseryLabelDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, Model model, 
            HttpSession session, @PathVariable int id) {
        session.invalidate();
        Study study = null;
        List<FieldMapInfo> fieldMapInfoList = null;
        FieldMapInfo fieldMapInfo = null;
        try {
            study = fieldbookMiddlewareService.getStudy(id);
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(id);
            fieldMapInfoList = fieldbookMiddlewareService.getFieldMapInfoOfNursery(ids);
            for (FieldMapInfo fieldMapInfoDetail : fieldMapInfoList) {
                fieldMapInfo = fieldMapInfoDetail;
            }
        } catch (MiddlewareQueryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getUserLabelPrinting().setStudy(study);
        getUserLabelPrinting().setFieldMapInfo(fieldMapInfo);
        getUserLabelPrinting().setBarcodeNeeded("1");
        
        /*
        getUserLabelPrinting().setName(study.getName());
        getUserLabelPrinting().setTitle(study.getTitle());
        getUserLabelPrinting().setObjective(study.getObjective());
       
        getUserLabelPrinting().setNumberOfInstances("2");
        getUserLabelPrinting().setTotalNumberOfLabelToPrint("50");
         
        getUserLabelPrinting().setSizeOfLabelSheet("A4");
        getUserLabelPrinting().setNumberOfRowsPerPageOfLabel("10");
        getUserLabelPrinting().setNumberOfLabelPerRow("3");
        */
        form.setUserLabelPrinting(getUserLabelPrinting());
        model.addAttribute("availableFields", getAvailableLabelFields(false, false));
        form.setIsTrial(false);
        return super.show(model);
    }
    
    private List<LabelFields> getAvailableLabelFields(boolean isTrial, boolean isFromFieldMap){
        List<LabelFields> labelFieldsList = new ArrayList();
        
        
        labelFieldsList.add(new LabelFields("Entry #", AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM));
        labelFieldsList.add(new LabelFields("GID", AppConstants.AVAILABLE_LABEL_FIELDS_GID));
        labelFieldsList.add(new LabelFields("Germplasm Name", AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME));
        labelFieldsList.add(new LabelFields("Year", AppConstants.AVAILABLE_LABEL_FIELDS_YEAR));
        labelFieldsList.add(new LabelFields("Season", AppConstants.AVAILABLE_LABEL_FIELDS_SEASON));
        if(isTrial){
            labelFieldsList.add(new LabelFields("Trial Name", AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME));
            labelFieldsList.add(new LabelFields("Trial Instance #", AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM));
        }else{
            labelFieldsList.add(new LabelFields("Nursery Name", AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME));
        }
        if(isFromFieldMap){
            labelFieldsList.add(new LabelFields("Rep", AppConstants.AVAILABLE_LABEL_FIELDS_REP));
            labelFieldsList.add(new LabelFields("Location", AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION));
            labelFieldsList.add(new LabelFields("Block Name", AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME));
            labelFieldsList.add(new LabelFields("Plot (Range / Column)", AppConstants.AVAILABLE_LABEL_FIELDS_PLOT));
        }
        return labelFieldsList;
    }
    /**
     * Submits the details.
     *
     * @param form the form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String submitDetails(@ModelAttribute("labelPrintingForm") LabelPrintingForm form, 
            BindingResult result, Model model, HttpServletResponse response) {
        
        getUserLabelPrinting().setBarcodeNeeded(form.getUserLabelPrinting().getBarcodeNeeded());
        getUserLabelPrinting().setSizeOfLabelSheet(form.getUserLabelPrinting().getSizeOfLabelSheet());
        getUserLabelPrinting().setNumberOfLabelPerRow(form.getUserLabelPrinting().getNumberOfLabelPerRow());
        getUserLabelPrinting().setNumberOfRowsPerPageOfLabel(form.getUserLabelPrinting().getNumberOfRowsPerPageOfLabel());
        getUserLabelPrinting().setSelectedLabelFields(form.getUserLabelPrinting().getSelectedLabelFields());
        getUserLabelPrinting().setFirstBarcodeField(form.getUserLabelPrinting().getFirstBarcodeField());        
        getUserLabelPrinting().setSecondBarcodeField(form.getUserLabelPrinting().getSecondBarcodeField());
        getUserLabelPrinting().setThirdBarcodeField(form.getUserLabelPrinting().getThirdBarcodeField());
        
        //setUserLabelPrinting(form.getUserLabelPrinting());
        int pageSizeId = Integer.parseInt(getUserLabelPrinting().getSizeOfLabelSheet());
        int numberOfLabelPerRow = Integer.parseInt(getUserLabelPrinting().getNumberOfLabelPerRow());
        int numberofRowsPerPageOfLabel = Integer.parseInt(getUserLabelPrinting().getNumberOfRowsPerPageOfLabel());
        int totalPerPage = numberOfLabelPerRow * numberofRowsPerPageOfLabel;
        String selectedFields = getUserLabelPrinting().getSelectedLabelFields();
        String barcodeNeeded = getUserLabelPrinting().getBarcodeNeeded();
        
        String firstBarcodeField = getUserLabelPrinting().getFirstBarcodeField();
        String secondBarcodeField = getUserLabelPrinting().getSecondBarcodeField();
        String thirdBarcodeField = getUserLabelPrinting().getThirdBarcodeField();
        
        
        
        String delimeter = "|";
        //StringBuilder barCodeString = new StringBuilder();
        /*
        barCodeString.append(barCodeForm.getEntryNo()).append(delimeter)
            .append(barCodeForm.getGid()).append(delimeter)
            .append(barCodeForm.getDesignation()).append(delimeter)
            .append(barCodeForm.getName()).append(delimeter)
            .append(barCodeForm.getRep()).append(delimeter)
            .append(barCodeForm.getColumn()).append(delimeter)
            .append(barCodeForm.getRange());
        */
        FieldMapDatasetInfo datasetInfo =  getUserLabelPrinting().getFieldMapInfo().getDatasets().get(0);
        List<FieldMapLabel> fieldMapLabelsList = new ArrayList();
        
        
        for(int i = 0  ; i < datasetInfo.getTrialInstances().size() ; i++){
            FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = datasetInfo.getTrialInstances().get(i);
            fieldMapLabelsList.addAll(fieldMapTrialInstanceInfo.getFieldMapLabels());
        }
           
        
         try {
             
             
          
             String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
             String fileName = currentDate + ".pdf";

             response.setHeader("Content-disposition","attachment; filename=" + fileName);

             File xls = new File(fileName); // the selected name + current date
             FileInputStream in;
             
             try {
                 
                 //Image image1 = Image.getInstance(imageLocation);
                
                 //PageSize.A4
                 Rectangle pageSize = PageSize.LETTER;
                 
                 if(pageSizeId == AppConstants.SIZE_OF_PAPER_A4)
                     pageSize = PageSize.A4;
                 
                 Document document = new Document(pageSize);
                 // step 2
                 PdfWriter.getInstance(document, new FileOutputStream(fileName));
                 // step 3
                 document.open();
                 // step 4
                 /*
                 PdfPTable table = new PdfPTable(2);  
                 PdfPCell cell = new PdfPCell();
                 Paragraph paragraph1 = new Paragraph();
                 paragraph1.add(textAnswer);
                 cell.addElement(paragraph1);
                 cell.addElement(new Paragraph("Test 1"));
                 cell.addElement(imageAnswer);
                 cell.addElement(new Paragraph("Test 3"));
                 table.addCell(cell);
                 */
                 
                 
                 int i = 0;
                 
                 PdfPTable table = new PdfPTable(numberOfLabelPerRow); 
                 table.setWidthPercentage(100);
                 int width = 60; 
                 int height = 48;
                 for(FieldMapLabel fieldMapLabel : fieldMapLabelsList){
                     i++;
                     String barcodeLabel = generateBarcodeField(fieldMapLabel, firstBarcodeField, secondBarcodeField, thirdBarcodeField, barcodeNeeded);
                     
                     
                     
                     BitMatrix bitMatrix = new Code128Writer().encode(barcodeLabel,BarcodeFormat.CODE_128,width,height,null);
                     String imageLocation = Math.random() + ".png";
                     MatrixToImageWriter.writeToStream(bitMatrix, "png", new FileOutputStream(new File(imageLocation)));
                     Image mainImage = Image.getInstance(imageLocation);
                     
                     
                     if(i % numberOfLabelPerRow == 0){
                         //we go the next line
                         table.completeRow();
                         document.add(table);
                         table = new PdfPTable(numberOfLabelPerRow);  
                         table.setWidthPercentage(100);
                     }
                     
                     if(i % totalPerPage == 0){
                         //we go the next page
                         document.newPage();
                     }
                     
                     PdfPCell cell = new PdfPCell();
                     Paragraph paragraph1 = new Paragraph();
                     
                     //String selectedLabel = "";
                     //paragraph1.add("test" + i);
                     //cell.addElement(paragraph1);  
                     
                     
                     Font fontNormal = FontFactory.getFont("Arial", 8, Font.NORMAL);
                     //mainImage.scaleAbsoluteHeight(50);
                     cell.addElement(mainImage);
                     cell.addElement(new Paragraph());
                     cell.addElement(new Paragraph("test " + i, fontNormal));
                     table.addCell(cell);
                     
                 }
                 
                 document.close();

                 in = new FileInputStream(xls);
                 OutputStream out = response.getOutputStream();

                 byte[] buffer= new byte[BUFFER_SIZE]; // use bigger if you want
                 int length = 0;

                 while ((length = in.read(buffer)) > 0){
                      out.write(buffer, 0, length);
                 }
                 in.close();
                 out.close();
             }catch (FileNotFoundException e) {
                 LOG.error(e.getMessage(), e);
             } catch (IOException e) {
                 LOG.error(e.getMessage(), e);
             }
             
             return "";
             
         } catch (WriterException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } 
         
         
        return "redirect:" + GenerateLabelController.URL;
    } 
    
    private String generateBarcodeField(FieldMapLabel fieldMapLabel, String firstField, String secondField, String thirdField, String barcodeNeeded){
        StringBuffer buffer = new StringBuffer();
        List<String> fieldList = new ArrayList<String>();
        fieldList.add(firstField);
        fieldList.add(secondField);
        fieldList.add(thirdField);
        String delimiter = "|";
        for(String barcodeLabel : fieldList){
            if(barcodeLabel.equalsIgnoreCase(""))
                continue;
            
            if(!buffer.toString().equalsIgnoreCase("")){
                buffer.append(delimiter);
            }
            switch(Integer.parseInt(barcodeLabel)){
                
                
                case AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM:
                    buffer.append(fieldMapLabel.getEntryNumber());
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_GID: 
                    //buffer.append(fieldMapLabel.get());
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME: 
                    buffer.append(fieldMapLabel.getGermplasmName());
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_YEAR: 
                    
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_SEASON: 
                    
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME: 
                    
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM: 
                    
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_REP: 
                    buffer.append(fieldMapLabel.getRep());
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION: 
                    
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME: 
                    
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_PLOT: 
                    buffer.append(fieldMapLabel.getPlotNo());
                    break;
                case AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME: 
                    //buffer.append(fieldMapLabel.getEntryNumber());
                    break;
                default: break;    
            }
        }
        return buffer.toString();
    }
    private String generateBarcodeLabel(FieldMapLabel fieldMapLabel, String selectedFields){
        StringBuffer buffer = new StringBuffer();
        return buffer.toString();
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "LabelPrinting/specifyLabelDetails";
    }

    
    public UserLabelPrinting getUserLabelPrinting() {
        return userLabelPrinting;
    }

    
    public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
        this.userLabelPrinting = userLabelPrinting;
    }
    
    
    
}
