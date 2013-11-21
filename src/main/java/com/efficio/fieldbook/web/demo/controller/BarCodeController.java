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

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.form.BarCodeForm;
import com.efficio.fieldbook.web.demo.bean.UserSelection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.FileOutputStream;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

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

    /** The fieldbook service. */
    @Resource
    private FieldbookService fieldbookService;
	
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
    @RequestMapping(method = RequestMethod.POST)
    public String uploadFile(@ModelAttribute("barCodeForm") BarCodeForm barCodeForm, BindingResult result, Model model) {
        int width = 440; 
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
             bitMatrix = new Code128Writer().encode(barCodeString.toString(),BarcodeFormat.CODE_128,width,height,null);
             MatrixToImageWriter.writeToStream(bitMatrix, "png", new FileOutputStream(new File("src/test/resources/barcode/zxing_barcode.png")));
         } catch (WriterException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
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