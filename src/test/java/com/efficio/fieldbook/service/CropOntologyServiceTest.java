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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

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
import com.efficio.pojos.cropontology.CropTerm;
import com.efficio.pojos.cropontology.Ontology;

/**
 * The Class CropOntologyServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class CropOntologyServiceTest extends AbstractJUnit4SpringContextTests {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(CropOntologyServiceTest.class);
    
	/** The crop ontology service. */
	@Autowired
	private CropOntologyService cropOntologyService;
	
	/* =========== search terms =========== */
	
	/**
	 * Test search terms.
	 */
	@Test
	public void testSearchTerms() {
		String query = "stem rust";
		List<CropTerm> cropTerms = cropOntologyService.searchTerms(query);
		assertNotNull(cropTerms);
		assertFalse(cropTerms.isEmpty());
		for (CropTerm cropTerm : cropTerms) {
			LOG.debug(cropTerm.toString());
		}
	}
	
	
	/* =========== get ontology id by name =========== */
	
	/**
	 * Test get ontology id by name.
	 */
	@Test
	public void testGetOntologyIdByName() {
		String name = "cassava";
		String cropId = cropOntologyService.getOntologyIdByName(name);
		assertEquals("CO_334", cropId);
	}
	
	/**
	 * Test get ontology id by name with null param.
	 */
	@Test
	public void testGetOntologyIdByNameWithNullParam() {
		String name = null;
		String cropId = cropOntologyService.getOntologyIdByName(name);
		assertNull(cropId);
	}
	
	/**
	 * Test get ontology id by name that does not exist.
	 */
	@Test(expected = HttpClientErrorException.class)
	public void testGetOntologyIdByNameThatDoesNotExist() {
		String name = "testing-doesnotexist-condition";
		String cropId = cropOntologyService.getOntologyIdByName(name);
		assertNull(cropId);
	}

	/* =========== get ontologies by category =========== */
	
	/**
	 * Test get ontologies by category.
	 */
	@Test
	public void testGetOntologiesByCategory() {
		String category = "010-089 General Germplasm Ontology";
		List<Ontology> ontologies = cropOntologyService.getOntologiesByCategory(category);
		assertNotNull(ontologies);
		assertFalse(ontologies.isEmpty());
		for (Ontology ontology : ontologies) {
			LOG.debug(ontology.toString());
		}
	}
	
}
