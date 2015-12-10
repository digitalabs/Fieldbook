
package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.NoSuchMessageException;

import com.efficio.fieldbook.util.FieldbookException;

import junit.framework.Assert;

public class AdvancingSourceListFactoryTest {

	@Mock
	ContextUtil contextUtil;

	@Mock
	OntologyVariableDataManager ontologyVariableDataManager;

	@InjectMocks
	AdvancingSourceListFactory factory = new AdvancingSourceListFactory();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * A simple test that makes sure proper handling for the season setting on a Workbook is handled for naming rule processing
	 *
	 * @throws FieldbookException
	 */


}
