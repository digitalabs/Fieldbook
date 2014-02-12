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
package com.efficio.fieldbook.web.demo.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.form.BarCodeForm;
import com.efficio.fieldbook.web.demo.bean.UserSelection;
import com.efficio.fieldbook.web.util.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The Class Test3Controller.
 */
@Controller
@RequestMapping({"/barcode"})
public class BarCodeController extends AbstractBaseFieldbookController{
	
    private static final Logger LOG = LoggerFactory.getLogger(BarCodeController.class);
    
    /** The user selection. */
    @Resource
    private UserSelection userSelection;
    
    private static final int BUFFER_SIZE = 4096 * 4;
	
    /**
     * Show.
     *
     * @param testForm the test form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("barCodeForm") BarCodeForm barCodeForm,  Model model) {
        return super.show(model);
    }

    /**
     * Upload file.
     *
     * @param uploadForm the upload form
     * @param result the result
     * @param model the model
     * @return the string
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("barCodeForm") BarCodeForm barCodeForm, 
            BindingResult result, Model model, HttpServletResponse response) {
        int width = 60; 
        int height = 48;
        String delimeter = "|";
        StringBuilder barCodeString = new StringBuilder();
        
        barCodeString.append(barCodeForm.getEntryNo()).append(delimeter)
            .append(barCodeForm.getGid()).append(delimeter)
            .append(barCodeForm.getDesignation()).append(delimeter)
            .append(barCodeForm.getName()).append(delimeter)
            .append(barCodeForm.getRep()).append(delimeter)
            .append(barCodeForm.getColumn()).append(delimeter)
            .append(barCodeForm.getRange());
           
        BitMatrix bitMatrix;
         try {
             String barCodeLabel = "3|SM114-1A-1-1-1B";
             //barCodeLabel = barCodeString.toString();
             bitMatrix = new Code128Writer().encode(barCodeLabel,BarcodeFormat.CODE_128,width,height,null);
             String imageLocation = Math.random() +".png"; //"src/test/resources/barcode/zxing_barcode.png";
             MatrixToImageWriter.writeToStream(bitMatrix, "png", new FileOutputStream(new File(imageLocation)));
             
             String currentDate = DateUtil.getCurrentDate();
             String fileName = currentDate + ".doc";

             response.setHeader("Content-disposition","attachment; filename=" + fileName);

             File xls = new File(fileName); // the selected name + current date
             FileInputStream in;
             
             try {
                 
                 Image image1 = Image.getInstance(imageLocation);
                 //image1.scalePercent(50);
                 //image1.setAbsolutePosition(10f, 10f);
                 //document.add(image1);
                 
                 //PageSize.A4
                 Document document = new Document(PageSize.LETTER);
                 // step 2
                 PdfWriter.getInstance(document, new FileOutputStream(fileName));
                 // step 3
                 document.open();
                 // step 4
                 // we'll use 4 images in this example
                 /*
                 Image[] img = {
                         Image.getInstance(String.format(RESOURCE, "0120903")),
                         Image.getInstance(String.format(RESOURCE, "0290334")),
                         Image.getInstance(String.format(RESOURCE, "0376994")),
                         Image.getInstance(String.format(RESOURCE, "0348150"))
                 };*/
                 
                 PdfPTable table = new PdfPTable(4);
                 table.setWidthPercentage(100);
                 // first movie
                 table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                 Phrase phrase = new Phrase();
                 phrase.add("Entry # " + barCodeForm.getEntryNo());
                 phrase.add("\n");
                 phrase.add("Designation " + barCodeForm.getDesignation());
                 phrase.add("\n");
                 phrase.add("Rep " + barCodeForm.getRep());
                 phrase.add("\n");
                 phrase.add("GID " + barCodeForm.getGid());
                 phrase.add("\n");
                 phrase.add("Trial/Nursery Name " + barCodeForm.getName());
                 phrase.add("\n");
                 phrase.add("Col " + barCodeForm.getColumn());
                 phrase.add("\n");
                 phrase.add("Range " + barCodeForm.getRange());                
                 table.addCell(phrase);
                 // we add the image with addCell()
                 table.addCell(image1);
                 
                 table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                 table.addCell("X-Men: The Last Stand");
                 // we add the image with addCell()
                 table.addCell(image1);
                 
                 table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                 table.addCell("X-Men: The Last Stand");
                 // we add the image with addCell()
                 table.addCell(image1);
                 
                 
                 // we complete the table (otherwise the last row won't be rendered)
                 table.completeRow();
                 document.add(table);
                 
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
             LOG.error(e.getMessage(), e);
         } catch (Exception e) {
             LOG.error(e.getMessage(), e);
         } 

         return show(barCodeForm, model);
    }
      
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "demo/barcode";
    }
    
    /**
     * Gets the user selection.
     *
     * @return the user selection
     */
    public UserSelection getUserSelection() {
        return this.userSelection;
    }
}