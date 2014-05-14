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
package com.efficio.fieldbook.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.nursery.bean.UserSelection;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class CropOntologyServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class PojoXmlTester extends AbstractJUnit4SpringContextTests {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(PojoXmlTester.class);
    
    @Autowired
    FieldbookService fieldbookService;
    
    @Autowired
    org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
    
    Dataset dataset;
    String datasetName;
    /**
     * Sets the up.
     */
    /* since no more export xml
    @Before
    public void setUp() {
    	
		
		List<SettingDetail> nurseryLevelConditions = new ArrayList<SettingDetail>();
		nurseryLevelConditions.add(new SettingDetail(getTestSettingVariable("1"),
				new ArrayList<ValueReference>(), "Test 1", true));
		nurseryLevelConditions.add(new SettingDetail(getTestSettingVariable("2"),
				new ArrayList<ValueReference>(), "Test 2", false));
		List<SettingDetail> plotsLevelList = new ArrayList<SettingDetail>();
		plotsLevelList.add(new SettingDetail(getTestSettingVariable("3"),
				new ArrayList<ValueReference>(), "Test 3", true));
		plotsLevelList.add(new SettingDetail(getTestSettingVariable("4"),
				new ArrayList<ValueReference>(), "Test 4", false));
		
		List<SettingDetail> baselineTraitsList = new ArrayList<SettingDetail>();
		baselineTraitsList.add(new SettingDetail(getTestSettingVariable("5"),
				new ArrayList<ValueReference>(), "Test 5", true));
		baselineTraitsList.add(new SettingDetail(getTestSettingVariable("6"),
				new ArrayList<ValueReference>(), "Test 6", false));
		
		datasetName = "test name";
		dataset = (Dataset)SettingsUtil.convertPojoToXmlDataset(fieldbookMiddlewareService, datasetName, nurseryLevelConditions, plotsLevelList, baselineTraitsList, null);
		
    }
	*/
	/* =========== search terms =========== */
	/*
	@Test
	public void testCreatePojoXmlAndParse() {
		Dataset dataset = new Dataset();
    	dataset.setName("test dataset");
    	dataset.setConditions(SettingsUtil.generateDummyCondition(10));
    	dataset.setFactors(SettingsUtil.generateDummyFactor(20));
    	dataset.setVariates(SettingsUtil.generateDummyVariate(30));
    	
    	String xml = SettingsUtil.generateSettingsXml(dataset);    	
    	Dataset newDataset = SettingsUtil.parseXmlToDatasetPojo(xml);
		assertEquals(dataset.getName(), newDataset.getName());
		assertEquals(dataset.getConditions().size(), newDataset.getConditions().size());
		assertEquals(dataset.getFactors().size(), newDataset.getFactors().size());
		assertEquals(dataset.getVariates().size(), newDataset.getVariates().size());

	}
	private SettingVariable getTestSettingVariable(String prefix){
		return new SettingVariable(prefix + " name", prefix + " description", prefix + "  property",
				prefix + "  scale", prefix + "  method", prefix + "  role", prefix + "  dataType");
	}
	@Test
	public void testConvertPojoToDataset() {
		//tests the conversion of the POJO to dataset equivalent
		
		assertEquals(datasetName, dataset.getName());
		assertEquals("Test 1", dataset.getConditions().get(0).getValue());
		assertEquals("Test 2", dataset.getConditions().get(1).getValue());
		assertEquals("1 name", dataset.getConditions().get(0).getName());
		assertEquals("4 name", dataset.getFactors().get(1).getName());
		assertEquals("6 name", dataset.getVariates().get(1).getName());
		
	
	}
	
	@Test
	public void testConvertDatasetToPojo() throws MiddlewareQueryException {
		//tests the conversion of the POJO to dataset equivalent
		UserSelection userSelection = new UserSelection();
		SettingsUtil.convertXmlDatasetToPojo(fieldbookMiddlewareService, fieldbookService, dataset, userSelection, "1");
		
		assertEquals(userSelection.getStudyLevelConditions().size(), dataset.getConditions().size());
		assertEquals(userSelection.getPlotsLevelList().size(), dataset.getFactors().size());
		assertEquals(userSelection.getBaselineTraitsList().size(), dataset.getVariates().size());
		
		assertEquals(userSelection.getStudyLevelConditions().get(0).getVariable().getName(), dataset.getConditions().get(0).getName());
		assertEquals(userSelection.getPlotsLevelList().get(0).getVariable().getName(), dataset.getFactors().get(0).getName());
		assertEquals(userSelection.getBaselineTraitsList().get(0).getVariable().getName(), dataset.getVariates().get(0).getName());
		
	}
	*/
}
