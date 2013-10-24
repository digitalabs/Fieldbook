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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class ManageNurseriesControllerTest extends AbstractJUnit4SpringContextTests {
	
    private static final Logger LOG = LoggerFactory.getLogger(ManageNurseriesControllerTest.class);
    
        @Autowired
        FieldbookService fieldbookMiddlewareService;
    
	@Before
        public void setUp() {
	    
        }
	
	@Test
	public void testManageNurseriesPagination(){
	    ManageNurseriesForm form = new ManageNurseriesForm();
	    
	    try {    
                List<StudyDetails> nurseryDetailsList = fieldbookMiddlewareService.getAllLocalNurseryDetails();
                form.setNurseryDetailsList(nurseryDetailsList);
	    } catch (MiddlewareQueryException e) {
	        LOG.error(e.getMessage(), e);
	    }
            
	    if (form.getPaginatedNurseryDetailsList() != null) {
    	        if (form.getPaginatedNurseryDetailsList().size() > 0) {
                    form.setCurrentPage(1);            
                    if (form.getPaginatedNurseryDetailsList().size() > 10) {
                        assertEquals(form.getPaginatedNurseryDetailsList().size(), form.getResultPerPage());
                    } 
    	        }
                
    	        if (form.getPaginatedNurseryDetailsList().size() > 10 && form.getPaginatedNurseryDetailsList().size() <= 20) {
                    form.setCurrentPage(2);
                    assertEquals(form.getPaginatedNurseryDetailsList().size(), form.getNurseryDetailsList().size()-10);
    	        }
	    }
	}
}