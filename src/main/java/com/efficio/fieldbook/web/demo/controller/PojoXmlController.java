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

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.util.SettingsUtil;

import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.util.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;



/**
 * The Class PojoXmlController.
 */
@Controller
@RequestMapping({"/pojoxml"})
public class PojoXmlController extends AbstractBaseFieldbookController{
	
    private static final Logger LOG = LoggerFactory.getLogger(PojoXmlController.class);
    
    /** The user selection. */

    /** The fieldbook service. */
    private static final int BUFFER_SIZE = 4096 * 4;
	
    /**
     * Show.
     *
     * @param testForm the test form
     * @param model the model
     * @return the string
     */
    @RequestMapping(method = RequestMethod.GET)
    public String show(Model model) {
    	/*	
		PojoXml pojoxml = PojoXmlFactory.createPojoXml();

        TestJavaBean testJavaBean = new TestJavaBean();
        testJavaBean.setAge("50");
        testJavaBean.setName("Daniel Test");
        
        String xml = pojoxml.getXml(testJavaBean);
		TestJavaBean testJavaBean1  = (TestJavaBean) pojoxml.getPojo("<TestJavaBean><name>Daniel Test</name><age>50</age></TestJavaBean>",TestJavaBean.class);

        //pojoxml.saveXml(testJavaBean,"testPojoXml.xml");
        //Employee employee = (Employee) pojoXml.getPojoFrormFile(fullPathNamen,Employee.class);
	*/
    	
    	Dataset dataset = new Dataset();
    	dataset.setName("test dataset");
    	dataset.setConditions(SettingsUtil.generateDummyCondition(10));
    	dataset.setFactors(SettingsUtil.generateDummyFactor(10));
    	dataset.setVariates(SettingsUtil.generateDummyVariate(10));
    	
    	String xml = SettingsUtil.generateSettingsXml(dataset);    	
    	
        Debug.println(0, xml);
        model.addAttribute("xml", xml);
        return super.show(model);
    }
    @RequestMapping(value="parse" , method = RequestMethod.GET)
    public String parse(Model model) {

    	
    	Dataset dataset = new Dataset();
    	dataset.setName("test dataset");
    	dataset.setConditions(SettingsUtil.generateDummyCondition(10));
    	dataset.setFactors(SettingsUtil.generateDummyFactor(10));
    	dataset.setVariates(SettingsUtil.generateDummyVariate(10));
    	
    	String xml = SettingsUtil.generateSettingsXml(dataset);    	
    	//System.out.println(xml);
    	Dataset datasetNew = SettingsUtil.parseXmlToDatasetPojo(xml);
    	/*
		PojoXml pojoXml = PojoXmlFactory.createPojoXml();

		TestJavaBean testJavaBean  = (TestJavaBean) pojoXml.getPojoFromFile("testPojoXml.xml",TestJavaBean.class);
		System.out.println(testJavaBean.getAge());
		System.out.println(testJavaBean.getName());
		*/
		model.addAttribute("xml", datasetNew.getName() + " <br />" +datasetNew.getConditions().size());
        return super.show(model);
    }

   
      
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
     */
    @Override
    public String getContentName() {
        return "demo/pojoxml";
    }
    
}