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

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.ontology.form.OntologyDetailsForm;

@Ignore(value ="BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
public class OntologyDetailsControllerTest extends AbstractBaseIntegrationTest {

	public static final Logger log = LoggerFactory.getLogger(OntologyDetailsControllerTest.class);

	@InjectMocks
	private OntologyDetailsController controller;

	@Mock
	private ContextUtil contextUtil;

	@Override
	@Before
	public void setUp() {
		this.controller = new OntologyDetailsController();
	}

	/**
	 * Test get ontology details.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testGetOntologyDetails() throws Exception {
		final OntologyDetailsForm form = new OntologyDetailsForm();
		final OntologyService ontologyService = Mockito.mock(OntologyService.class);
		final StandardVariable stdvar = this.createStandardVariableTestData();
		Mockito.when(ontologyService.getStandardVariable(8110, this.contextUtil.getCurrentProgramUUID())).thenReturn(stdvar);
		this.controller.setOntologyService(ontologyService);

		Assert.assertEquals(stdvar, form.getVariable());
	}

	/**
	 * Creates the standard variable test data.
	 *
	 * @return the standard variable
	 */
	private StandardVariable createStandardVariableTestData() {
		final Term property = new Term(100, "PROPERTY", "PROPERTY DEF");
		final Term scale = new Term(200, "SCALE", "SCALE DEF");
		final Term method = new Term(300, "METHOD", "METHOD DEF");
		final Term dataType = new Term(400, "DATA TYPE", "DATA TYPE DEF");
		final Term traitClass = new Term(600, "TRAIT CLASS", "TRAIT CLASS DEF");
		final VariableConstraints constraints = new VariableConstraints(10.0, 50.0);
		final List<Enumeration> enumerations = new ArrayList<Enumeration>();
		enumerations.add(new Enumeration(1, "ENUM1", "ENUM1 DESC", 0));
		enumerations.add(new Enumeration(2, "ENUM1", "ENUM2 DESC", 0));
		enumerations.add(new Enumeration(3, "ENUM1", "ENUM3 DESC", 0));
		enumerations.add(new Enumeration(4, "ENUM1", "ENUM4 DESC", 0));

		final StandardVariable stdvar = new StandardVariable(property, scale, method, dataType, traitClass, PhenotypicType.TRIAL_DESIGN);
		stdvar.setConstraints(constraints);
		stdvar.setEnumerations(enumerations);
		stdvar.setName("VARIABLE1");
		stdvar.setDescription("VARIABLE DESCRIPTION");

		return stdvar;
	}

}
