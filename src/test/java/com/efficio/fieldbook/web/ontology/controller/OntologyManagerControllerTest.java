/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.ontology.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermProperty;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.efficio.fieldbook.web.AbstractBaseControllerIntegrationTest;
import com.efficio.fieldbook.web.ontology.form.OntologyBrowserForm;
import com.efficio.fieldbook.web.ontology.form.OntologyPropertyForm;

/**
 * The Class OntologyManagerControllerTest.
 *
 * @author Chezka Camille Arevalo
 */
public class OntologyManagerControllerTest extends AbstractBaseControllerIntegrationTest {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(OntologyControllerTest.class);

	/** The ontology service. */
	@Autowired
	OntologyService ontologyService;

	/** The standard variable. */
	private StandardVariable standardVariable;

	private OntologyService mockOntologyService;
	private OntologyBrowserForm form;
	private OntologyPropertyForm propForm;
	private Term term;
	private OntologyManagerController ontologyManagerController;

	private static final int VARIABLE_ID = 1;
	private static final String VARIABLE_NAME = "NREP";
	private static final String VARIABLE_DEFINITION = "Number of replications in an experiment";

	private static final int PROPERTY_ID = 2;
	private static final String PROPERTY_NAME = "ACQ_DATE";
	private static final String PROPERTY_DEFINITION = "ACQ_DATE definition";

	/**
	 * Sets the up.
	 */
	@Before
	public void setUp() {
		List<TermProperty> termProperties = new ArrayList<TermProperty>();
		termProperties.add(new TermProperty(1, TermId.CROP_ONTOLOGY_ID.getId(), "CO:12345", 0));

		try {
			String propertyName = "property name " + new Random().nextInt(10000);
			this.ontologyService.addProperty(propertyName, "test property", 1087);
			Property property = this.ontologyService.getProperty(propertyName);

			String scaleName = "scale name " + new Random().nextInt(10000);
			Term scale = this.ontologyService.addTerm(scaleName, "test scale", CvId.SCALES);

			String methodName = "method name " + new Random().nextInt(10000);
			Term method = this.ontologyService.addTerm(methodName, methodName, CvId.METHODS);

			Term dataType = new Term(400, "DATA TYPE", "DATA TYPE DEF");
			Term storedIn = new Term(1010, "STORED IN", "STORED IN DEF");
			Term traitClass = new Term(600, "TRAIT CLASS", "TRAIT CLASS DEF");

			this.standardVariable = new StandardVariable();
			this.standardVariable.setName("TestVariable" + new Random().nextInt(10000));
			this.standardVariable.setDescription("Test Desc");
			this.standardVariable.setProperty(property.getTerm());
			this.standardVariable.setMethod(method);
			this.standardVariable.setScale(scale);
			this.standardVariable.setDataType(dataType);
			this.standardVariable.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
			this.standardVariable.setIsA(traitClass);
			this.standardVariable.setStoredIn(storedIn);
			this.standardVariable.setCropOntologyId("CO:1200");
			this.standardVariable.setConstraints(new VariableConstraints(1.0, 10.0));
			this.ontologyService.addStandardVariable(this.standardVariable);
		} catch (MiddlewareQueryException e) {
			OntologyManagerControllerTest.LOG.error(e.getMessage(), e);
		}

		this.ontologyManagerController = new OntologyManagerController();
		this.form = new OntologyBrowserForm();
		this.propForm = new OntologyPropertyForm();
		this.mockOntologyService = Mockito.mock(OntologyService.class);
		this.term = Mockito.mock(Term.class);
	}

	@Test
	public void testSaveNewVariableConfirmPreselectVariableIdIsMaintainedIfFromPopup() throws MiddlewareQueryException {
		int variableId = 10180;
		String dataTypeId = "1";

		Mockito.when(this.term.getName()).thenReturn("Categorical");
		Mockito.when(this.mockOntologyService.getStandardVariable(10180)).thenReturn(Mockito.mock(StandardVariable.class));
		Mockito.when(this.mockOntologyService.getTermById(Integer.parseInt(dataTypeId))).thenReturn(this.term);

		BindingResult result = Mockito.mock(BindingResult.class);
		Model model = new ExtendedModelMap();
		this.form.setIsDelete(0);
		this.form.setFromPopup("1");
		this.form.setPreselectVariableId(variableId);
		this.form.setVariableId(variableId);
		this.form.setDataTypeId(dataTypeId);
		this.form.setProperty("1");
		this.form.setMethod("1");
		this.form.setScale("1");
		this.form.setDataType("1");
		this.form.setRole("1");

		this.ontologyManagerController.setOntologyService(this.mockOntologyService);
		this.ontologyManagerController.saveNewVariable(this.form, result, model);
		Assert.assertEquals("Should return in model the same preselect variable id that was set in the form when it is a popup",
				variableId, model.asMap().get("preselectVariableId"));
	}

	@Test
	public void testSaveNewVariableConfirmPreselectVariableIdIsIsNotMaintainedIfNotFromPopup() throws MiddlewareQueryException {
		int variableId = 10180;
		String dataTypeId = "1";

		Mockito.when(this.term.getName()).thenReturn("Categorical");
		Mockito.when(this.mockOntologyService.getStandardVariable(10180)).thenReturn(Mockito.mock(StandardVariable.class));
		Mockito.when(this.mockOntologyService.getTermById(Integer.parseInt(dataTypeId))).thenReturn(this.term);

		BindingResult result = Mockito.mock(BindingResult.class);
		Model model = new ExtendedModelMap();
		this.form.setIsDelete(0);
		this.form.setFromPopup("0");
		this.form.setPreselectVariableId(variableId);
		this.form.setVariableId(variableId);
		this.form.setDataTypeId(dataTypeId);
		this.form.setProperty("1");
		this.form.setMethod("1");
		this.form.setScale("1");
		this.form.setDataType("1");
		this.form.setRole("1");

		this.ontologyManagerController.setOntologyService(this.mockOntologyService);
		this.ontologyManagerController.saveNewVariable(this.form, result, model);

		Assert.assertEquals("Should return in model the 0 as preselect variable id when it is not from a popup", 0,
				model.asMap().get("preselectVariableId"));
	}

	@Test
	public void testValidateNewVariableNameExisting() throws MiddlewareQueryException, Exception {

		Term termWithValue =
				new Term(OntologyManagerControllerTest.VARIABLE_ID, OntologyManagerControllerTest.VARIABLE_NAME,
						OntologyManagerControllerTest.VARIABLE_DEFINITION);
		BindingResult result = null;
		Model model = null;

		this.form.setVariableName(OntologyManagerControllerTest.VARIABLE_NAME);
		Mockito.when(this.mockOntologyService.findTermByName(OntologyManagerControllerTest.VARIABLE_NAME, CvId.VARIABLES)).thenReturn(
				termWithValue);

		// set the mocked ontology service in the controller
		this.ontologyManagerController.setOntologyService(this.mockOntologyService);
		String status = this.ontologyManagerController.validateNewVariableName(this.form, result, model);

		Assert.assertEquals("error", status);
	}

	@Test
	public void testValidateNewVariableNameNonExisting() throws MiddlewareQueryException, Exception {
		BindingResult result = null;
		Model model = null;
		String variableName = OntologyManagerControllerTest.VARIABLE_NAME + new Random().nextInt();

		this.form.setVariableName(variableName);
		Mockito.when(this.mockOntologyService.findTermByName(variableName, CvId.VARIABLES)).thenReturn(null);

		this.ontologyManagerController.setOntologyService(this.mockOntologyService);
		String status = this.ontologyManagerController.validateNewVariableName(this.form, result, model);

		Assert.assertEquals("success", status);
	}

	@Test
	public void testValidationOfPropertyNameExisting() {
		Term termWithValue =
				new Term(OntologyManagerControllerTest.PROPERTY_ID, OntologyManagerControllerTest.PROPERTY_NAME,
						OntologyManagerControllerTest.PROPERTY_DEFINITION);
		this.propForm.setManagePropertyName(OntologyManagerControllerTest.PROPERTY_NAME);
		try {
			Mockito.when(this.mockOntologyService.findTermByName(OntologyManagerControllerTest.PROPERTY_NAME, CvId.PROPERTIES)).thenReturn(
					termWithValue);
			this.ontologyManagerController.setOntologyService(this.mockOntologyService);

			this.ontologyManagerController.validatePropertyName(this.propForm);
		} catch (MiddlewareQueryException e) {
			Assert.assertEquals("Expected error code error.ontology.property.exists but got " + e.getCode() + "instead",
					"error.ontology.property.exists", e.getCode());
		}
	}

	@Test
	public void testValidationOfPropertyNameNonExisting() throws MiddlewareQueryException {
		this.propForm.setManagePropertyName(OntologyManagerControllerTest.PROPERTY_NAME);
		boolean hasError = false;
		try {
			Mockito.when(this.mockOntologyService.findTermByName(OntologyManagerControllerTest.PROPERTY_NAME, CvId.PROPERTIES)).thenReturn(
					null);
			this.ontologyManagerController.setOntologyService(this.mockOntologyService);

			this.ontologyManagerController.validatePropertyName(this.propForm);
		} catch (MiddlewareQueryException e) {
			hasError = true;
		}

		Assert.assertEquals("Not expecting an error but encountered an error instead.", false, hasError);
	}

	@Test
	public void testValidateDeleteIfVariableIdIsPositive() throws MiddlewareQueryException {
		BindingResult result = Mockito.mock(BindingResult.class);
		this.form.setVariableId(1);
		this.ontologyManagerController.setOntologyService(this.mockOntologyService);
		this.ontologyManagerController.validateDelete(this.form, result);
		Mockito.verify(this.mockOntologyService, Mockito.times(1)).countProjectsByVariable(1);
	}

}
