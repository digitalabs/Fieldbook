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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.demo.bean.TestJavaBean;
import com.efficio.fieldbook.web.demo.form.Test2JavaForm;
import com.efficio.fieldbook.web.demo.validation.TestValidator;
import com.efficio.fieldbook.web.demo.bean.UserSelection;
import com.efficio.pojos.histogram.HistogramNode;

import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@Controller
@RequestMapping({"/histogram"})
public class HistogramController extends AbstractBaseFieldbookController{

    private static final Logger LOG = LoggerFactory.getLogger(HistogramController.class);
    
    

    /**
     * Show the form
     *
     * @param testForm the test form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(Model model) {
    	//model.addAttribute("histogramList", getHistogramList());
    	return super.show(model);
    }
    
    /**
     * Gets the scale suggestions.
     *
     * @return the scale suggestions
     */
    @ModelAttribute("histogramList")
    public List<HistogramNode> getHistogramList() {
        List<HistogramNode> histogramNodes = new ArrayList<HistogramNode>();
        histogramNodes.add(new HistogramNode("Test 1", 5));
        histogramNodes.add(new HistogramNode("Test 2", 15));
        histogramNodes.add(new HistogramNode("Test 3", 25));
        histogramNodes.add(new HistogramNode("Test 4", 35));
        histogramNodes.add(new HistogramNode("Test 5", 45));
        histogramNodes.add(new HistogramNode("Test 6", 55));
        histogramNodes.add(new HistogramNode("Test 7", 65));
        histogramNodes.add(new HistogramNode("Test 8", 75));
        histogramNodes.add(new HistogramNode("Test 9", 85));
        histogramNodes.add(new HistogramNode("Test 10", 95));
        return histogramNodes;
    }

    

    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "demo/histogram";
    }
   
    
}