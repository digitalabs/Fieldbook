
package com.efficio.etl.web.controller.rest;

import com.efficio.etl.web.bean.VariableDTO;
import com.efficio.etl.web.util.AppConstants;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.dms.DatasetType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class StandardVariableRESTControllerTest {

	private static final String DUMMY_PROGRAM_UUID = "!234567";

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private StandardVariableRESTController standardVariableRESTController;

	@Before
	public void setUp() {
		Mockito.doReturn(StandardVariableRESTControllerTest.DUMMY_PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
	}

	@Test
	public void testRetrieveOntologyVariablesForPlotData() {

		final List<Variable> expectedPlotDataVariables = this.createVariablesTestData(DatasetType.PLOT_DATA);
		Mockito.doReturn(expectedPlotDataVariables).when(this.ontologyVariableDataManager)
			.getWithFilter(Matchers.any(VariableFilter.class));

		final List<VariableDTO> plotDataVariables = this.standardVariableRESTController.retrieveOntologyVariables(DatasetType.PLOT_DATA);
		Assert.assertNotNull(plotDataVariables);

		Mockito.verify(this.ontologyVariableDataManager).getWithFilter(Matchers.any(VariableFilter.class));

		final Set<VariableType> variableTypes = this.createValidVariableTypes(DatasetType.PLOT_DATA);
		for (final VariableDTO variableDTO : plotDataVariables) {
			if (variableDTO.getPhenotype() == null) {
				Assert.fail("All variables should have a phenotype.");
			} else {
				final PhenotypicType phenotypicType = this.mapToPhenotypicType(variableDTO.getPhenotype());
				if (!this.hasPhenotypicType(variableTypes, phenotypicType)) {
					Assert.fail("All variables should be part of the filtered variable types");
				}
			}
		}
	}

	private List<Variable> createVariablesTestData(final Integer datasetTypeId) {
		final List<Variable> variables = new ArrayList<>();
		int variableId = 0;
		int propertyId = 0;
		int scaleId = 0;
		int methodId = 0;
		variables.add(this.createVariableTestData(++variableId, ++propertyId, ++scaleId, ++methodId,
			Arrays.asList(new VariableType[] {VariableType.ENVIRONMENT_DETAIL})));
		variables.add(this.createVariableTestData(++variableId, ++propertyId, ++scaleId, ++methodId,
			Arrays.asList(new VariableType[] {VariableType.GERMPLASM_DESCRIPTOR})));
		variables.add(this.createVariableTestData(++variableId, ++propertyId, ++scaleId, ++methodId,
			Arrays.asList(new VariableType[] {VariableType.EXPERIMENTAL_DESIGN})));
		if (DatasetType.MEANS_DATA == datasetTypeId) {
			variables.add(this.createVariableTestData(++variableId, ++propertyId, ++scaleId, ++methodId,
				Arrays.asList(new VariableType[] {VariableType.ANALYSIS})));
		} else {
			variables.add(this.createVariableTestData(++variableId, ++propertyId, ++scaleId, ++methodId,
				Arrays.asList(new VariableType[] {VariableType.TRAIT})));
			variables.add(this.createVariableTestData(++variableId, ++propertyId, ++scaleId, ++methodId,
				Arrays.asList(new VariableType[] {VariableType.STUDY_CONDITION})));
			variables.add(this.createVariableTestData(++variableId, ++propertyId, ++scaleId, ++methodId,
				Arrays.asList(new VariableType[] {VariableType.STUDY_CONDITION})));
			variables.add(this.createVariableTestData(++variableId, ++propertyId, ++scaleId, ++methodId,
				Arrays.asList(new VariableType[] {VariableType.SELECTION_METHOD})));
		}
		return variables;
	}

	private Variable createVariableTestData(
		final int id, final int propertyId, final int scaleId, final int methodId,
		final List<VariableType> variableTypes) {
		final Variable variable = new Variable();
		variable.setId(id);
		variable.setName("VARIABLE-" + id);

		final Property property = new Property();
		property.setId(propertyId);
		property.setName("PROPERTY-" + propertyId);
		property.setDefinition("PROPERTY-DEF-" + propertyId);
		variable.setProperty(property);

		final Scale scale = new Scale();
		scale.setId(scaleId);
		scale.setName("SCALE-" + scaleId);
		scale.setDefinition("SCALE-DEF-" + scaleId);
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		variable.setScale(scale);

		final Method method = new Method();
		method.setId(methodId);
		method.setName("METHOD-" + methodId);
		method.setDefinition("METHOD-DEF-" + methodId);
		variable.setMethod(method);

		variable.getVariableTypes().addAll(variableTypes);

		return variable;
	}

	private boolean hasPhenotypicType(final Set<VariableType> variableTypes, final PhenotypicType phenotypicType) {
		boolean isPhenotypicTypeFound = false;
		for (final VariableType variableType : variableTypes) {
			if (variableType.getRole() == phenotypicType) {
				isPhenotypicTypeFound = true;
				break;
			}
		}
		return isPhenotypicTypeFound;
	}

	private PhenotypicType mapToPhenotypicType(final String phenotype) {
		PhenotypicType mappedPhenotypicType = null;
		if (phenotype != null) {
			switch (phenotype) {
				case AppConstants.TYPE_TRIAL_ENVIRONMENT:
					mappedPhenotypicType = PhenotypicType.TRIAL_ENVIRONMENT;
					break;
				case AppConstants.TYPE_TRIAL_DESIGN:
					mappedPhenotypicType = PhenotypicType.TRIAL_DESIGN;
					break;
				case AppConstants.TYPE_GERMPLASM_ENTRY:
					mappedPhenotypicType = PhenotypicType.GERMPLASM;
					break;
				case AppConstants.TYPE_VARIATE:
					mappedPhenotypicType = PhenotypicType.VARIATE;
					break;
				default:
					break;
			}
		}
		return mappedPhenotypicType;
	}

	private Set<VariableType> createValidVariableTypes(final Integer datasetTypeId) {
		final Set<VariableType> variableTypes = new HashSet<>();
		if (DatasetType.MEANS_DATA == datasetTypeId) {
			variableTypes.add(VariableType.ENVIRONMENT_DETAIL);
			variableTypes.add(VariableType.GERMPLASM_DESCRIPTOR);
			variableTypes.add(VariableType.EXPERIMENTAL_DESIGN);
			variableTypes.add(VariableType.TREATMENT_FACTOR);
			variableTypes.add(VariableType.ANALYSIS);
		} else {
			variableTypes.add(VariableType.ENVIRONMENT_DETAIL);
			variableTypes.add(VariableType.GERMPLASM_DESCRIPTOR);
			variableTypes.add(VariableType.EXPERIMENTAL_DESIGN);
			variableTypes.add(VariableType.TREATMENT_FACTOR);
			variableTypes.add(VariableType.STUDY_CONDITION);
			variableTypes.add(VariableType.SELECTION_METHOD);
			variableTypes.add(VariableType.TRAIT);
		}
		return variableTypes;
	}

	@Test
	public void testRetrieveOntologyVariablesForMeansData() {

		final List<Variable> expectedPlotDataVariables = this.createVariablesTestData(DatasetType.MEANS_DATA);
		Mockito.doReturn(expectedPlotDataVariables).when(this.ontologyVariableDataManager)
			.getWithFilter(Matchers.any(VariableFilter.class));

		final List<VariableDTO> plotDataVariables = this.standardVariableRESTController.retrieveOntologyVariables(DatasetType.MEANS_DATA);
		Assert.assertNotNull(plotDataVariables);

		Mockito.verify(this.ontologyVariableDataManager).getWithFilter(Matchers.any(VariableFilter.class));

		final Set<VariableType> variableTypes = this.createValidVariableTypes(DatasetType.MEANS_DATA);
		for (final VariableDTO variableDTO : plotDataVariables) {
			if (variableDTO.getPhenotype() == null) {
				Assert.fail("All variables should have a phenotype.");
			} else {
				final PhenotypicType phenotypicType = this.mapToPhenotypicType(variableDTO.getPhenotype());
				if (!this.hasPhenotypicType(variableTypes, phenotypicType)) {
					Assert.fail("All variables should be part of the filtered variable types");
				}
			}
		}
	}
}
