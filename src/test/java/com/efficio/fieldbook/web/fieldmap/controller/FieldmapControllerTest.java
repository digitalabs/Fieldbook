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
package com.efficio.fieldbook.web.fieldmap.controller;

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
import com.efficio.fieldbook.web.trial.form.ManageTrialForm;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class FieldmapControllerTest extends AbstractJUnit4SpringContextTests {
	
    private static final Logger LOG = LoggerFactory.getLogger(FieldmapControllerTest.class);
    
    @Autowired
    FieldbookService fieldbookMiddlewareService;

    @Before
    public void setUp() {

    }
	/*
	@Test
	public void testManageNurseriesPagination(){
	    ManageTrialForm form = new ManageTrialForm();
	    
	    try {    
            List<StudyDetails> nurseryDetailsList = fieldbookMiddlewareService.getAllLocalTrialStudyDetails();
            form.setTrialDetailsList(nurseryDetailsList);
	    } catch (MiddlewareQueryException e) {
	        LOG.error(e.getMessage(), e);
	    }
            
	    if (form.getPaginatedTrialDetailsList() != null) {
            if (form.getPaginatedTrialDetailsList().size() > 0) {
                form.setCurrentPage(1);
                if (form.getPaginatedTrialDetailsList().size() > 10) {
                    assertEquals(form.getPaginatedTrialDetailsList().size(), form.getResultPerPage());
                }
            }

            if (form.getPaginatedTrialDetailsList().size() > 10 && form.getPaginatedTrialDetailsList().size() <= 20) {
                form.setCurrentPage(2);
                assertEquals(form.getPaginatedTrialDetailsList().size(), form.getTrialDetailsList().size() - 10);
            }
	    }
	}
	*/
}