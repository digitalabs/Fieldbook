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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.generationcp.middleware.domain.fieldbook.settings.Dataset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;

import com.efficio.fieldbook.service.api.CropOntologyService;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.pojos.cropontology.CropTerm;
import com.efficio.pojos.cropontology.Ontology;

/**
 * The Class CropOntologyServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class PojoXmlTester extends AbstractJUnit4SpringContextTests {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(PojoXmlTester.class);
    
	
	
	/* =========== search terms =========== */
	
	/**
	 * Test search terms.
	 */
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
	
	
}
