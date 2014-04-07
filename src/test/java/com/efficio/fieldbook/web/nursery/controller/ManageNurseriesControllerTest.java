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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.settings.Condition;
import org.generationcp.middleware.pojos.workbench.settings.Dataset;
import org.generationcp.middleware.pojos.workbench.settings.Factor;
import org.generationcp.middleware.pojos.workbench.settings.Variate;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.web.nursery.form.ManageNurseriesForm;
import com.efficio.fieldbook.web.util.SettingsUtil;

/**
 * The Class ManageNurseriesControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class ManageNurseriesControllerTest extends AbstractJUnit4SpringContextTests {
	
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ManageNurseriesControllerTest.class);
    
    /** The fieldbook middleware service. */
    @Autowired
    FieldbookService fieldbookMiddlewareService;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {

    }
	
	/**
	 * Test manage nurseries pagination.
	 */
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

            if (form.getPaginatedNurseryDetailsList().size() > 10 
                    && form.getPaginatedNurseryDetailsList().size() <= 20) {
                form.setCurrentPage(2);
                assertEquals(form.getPaginatedNurseryDetailsList().size()
                        , form.getNurseryDetailsList().size() - 10);
            }
	    }
	}
	
	@Test
	public void testConvertXmlDatasetToWorkbookAndBack() {
		Dataset dataset = new Dataset();
		
		dataset.setConditions(new ArrayList<Condition>());
		dataset.setFactors(new ArrayList<Factor>());
		dataset.setVariates(new ArrayList<Variate>());
		
		dataset.getConditions().add(new Condition("CONDITION1", "CONDITION1", "PERSON", "DBCV", "ASSIGNED", PhenotypicType.STUDY.toString(), "C", "Meeh", null, null, null));
		dataset.getFactors().add(new Factor("FACTOR1", "FACTOR1", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED", PhenotypicType.GERMPLASM.toString(), "N"));
		dataset.getVariates().add(new Variate("VARIATE1", "VARIATE1", "YIELD (GRAIN)", "Kg/ha", "Paddy Rice", PhenotypicType.VARIATE.toString(), "N", TermId.NUMERIC_VARIABLE.getId(), new ArrayList<ValueReference>(), 0.0, 0.0));
		
		Workbook workbook = SettingsUtil.convertXmlDatasetToWorkbook(dataset);
		System.out.println(workbook);
		
		Dataset newDataset = (Dataset)SettingsUtil.convertWorkbookToXmlDataset(workbook);
		Assert.assertEquals(dataset.getConditions().get(0).getName(), newDataset.getConditions().get(0).getName());
		Assert.assertEquals(dataset.getConditions().get(0).getDescription(), newDataset.getConditions().get(0).getDescription());
		Assert.assertEquals(dataset.getConditions().get(0).getProperty(), newDataset.getConditions().get(0).getProperty());
		Assert.assertEquals(dataset.getConditions().get(0).getScale(), newDataset.getConditions().get(0).getScale());
		Assert.assertEquals(dataset.getConditions().get(0).getMethod(), newDataset.getConditions().get(0).getMethod());
		Assert.assertEquals(dataset.getConditions().get(0).getRole(), newDataset.getConditions().get(0).getRole());
		Assert.assertEquals(dataset.getConditions().get(0).getDatatype(), newDataset.getConditions().get(0).getDatatype());

		Assert.assertEquals(dataset.getFactors().get(0).getName(), newDataset.getFactors().get(0).getName());
		Assert.assertEquals(dataset.getFactors().get(0).getDescription(), newDataset.getFactors().get(0).getDescription());
		Assert.assertEquals(dataset.getFactors().get(0).getProperty(), newDataset.getFactors().get(0).getProperty());
		Assert.assertEquals(dataset.getFactors().get(0).getScale(), newDataset.getFactors().get(0).getScale());
		Assert.assertEquals(dataset.getFactors().get(0).getMethod(), newDataset.getFactors().get(0).getMethod());
		Assert.assertEquals(dataset.getFactors().get(0).getRole(), newDataset.getFactors().get(0).getRole());
		Assert.assertEquals(dataset.getFactors().get(0).getDatatype(), newDataset.getFactors().get(0).getDatatype());

		Assert.assertEquals(dataset.getVariates().get(0).getName(), newDataset.getVariates().get(0).getName());
		Assert.assertEquals(dataset.getVariates().get(0).getDescription(), newDataset.getVariates().get(0).getDescription());
		Assert.assertEquals(dataset.getVariates().get(0).getProperty(), newDataset.getVariates().get(0).getProperty());
		Assert.assertEquals(dataset.getVariates().get(0).getScale(), newDataset.getVariates().get(0).getScale());
		Assert.assertEquals(dataset.getVariates().get(0).getMethod(), newDataset.getVariates().get(0).getMethod());
		Assert.assertEquals(dataset.getVariates().get(0).getRole(), newDataset.getVariates().get(0).getRole());
		Assert.assertEquals(dataset.getVariates().get(0).getDatatype(), newDataset.getVariates().get(0).getDatatype());

	}
}