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
package com.efficio.fieldbook.web.nursery.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.DataImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.service.api.ImportWorkbookFileService;
import org.generationcp.middleware.domain.etl.Workbook;
import com.efficio.fieldbook.web.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.form.NurseryDetailsForm;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping(NurseryDetailsController.URL)
public class NurseryDetailsController extends AbstractBaseFieldbookController{

    public static final String URL = "/NurseryManager/nurseryDetails";
    
    @Resource
    private UserSelection userSelection;

    @Resource
    private DataImportService dataImportService;
    
    private Map<String, String> returnMessage = new HashMap<String, String>();

    private ImportWorkbookFileService importWorkbookFileService;

    @Override
    public String getContentName() {
        return "NurseryManager/nurseryDetails";
    }
    
    @Override
    public UserSelection getUserSelection() {
        return this.userSelection;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String show(@ModelAttribute("nurseryDetailsForm") NurseryDetailsForm form, Model model, HttpSession session) {
    	return super.show(model);
    }
    
    @ResponseBody
    @RequestMapping(value="startProcess",method = RequestMethod.GET)
    public Map<String, String> startProcess(final HttpSession session, HttpServletResponse response){
                        
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0); // Proxies.

        returnMessage.put("statusCode", "0");
        returnMessage.put("statusMessage", "Import has started.");

        try {

            Workbook datasetWorkbook = dataImportService
                    .parseWorkbook(importWorkbookFileService.retrieveCurrentWorkbookAsFile(userSelection));
            userSelection.setWorkbook(datasetWorkbook);
            
            returnMessage.clear();
            returnMessage.put("statusCode", "1");
            returnMessage.put("statusMessage", "Import is done.");

        } catch (MiddlewareQueryException e) {
            e.printStackTrace();

            synchronized (returnMessage) {
                returnMessage.clear();
                returnMessage.put("statusCode", "-1");
                returnMessage.put("statusMessage", e.getMessage());
                returnMessage.put("errorType", "MiddlewareQueryException");
            }

        } catch (IOException e) {
            e.printStackTrace();
            synchronized (returnMessage) {
                returnMessage.clear();
                returnMessage.put("statusCode", "-1");
                returnMessage.put("statusMessage", e.getMessage());
                returnMessage.put("errorType", "IOException");
            }
        } catch (Exception e) {
            e.printStackTrace();
            synchronized (returnMessage) {
                returnMessage.clear();
                returnMessage.put("statusCode", "-1");
                returnMessage.put("statusMessage", e.getMessage());
                returnMessage.put("errorType", "Exception");
            }
        } catch (Error e) {
            e.printStackTrace();
            synchronized (returnMessage) {
                returnMessage.clear();
                returnMessage.put("statusCode", "-1");
                returnMessage.put("statusMessage", e.getMessage());
                returnMessage.put("errorType", "FormatError");
            }
        }

        return returnMessage;

    }
    

    @RequestMapping(method = RequestMethod.POST)
    public String showDetails(@ModelAttribute("nurseryDetailsForm") NurseryDetailsForm uploadForm, BindingResult result, Model model) {
    	//TODO
        return "redirect:" + ImportGermplasmListController.URL;
    }

}