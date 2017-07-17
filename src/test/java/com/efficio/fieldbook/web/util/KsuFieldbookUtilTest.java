package com.efficio.fieldbook.web.util;

import com.google.common.collect.Lists;
import org.generationcp.middleware.data.initializer.MeasurementTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class KsuFieldbookUtilTest {

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	// TERM_ID
	private static final int HGRAMINC_E_PCT_ID = 18180;
	private static final int ACDTOL_E_1TO_5_ID = 18180;
	private static final int ANT_DATE_YMD_ID = 18180;
	private static final int R_SECA_SEV_E_00_TO_99_TEXT_ID = 18180;

	// NAME
	private static final String HGRAMINC_E_PCT_NAME = "HgramInc_E_pct";
	private static final String ACDTOL_E_1TO_5_NAME = "AcdTol _E_1to5";
	private static final String ANT_DATE_YMD_NAME = "Ant_date_ymd";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_NAME = "RSecaSev_E_00to99text";

	// DESCRIPTION
	private static final String HGRAMINC_E_PCT_DESCRIPTION = "Barley Helminthosporium leaf stripe incidence-BY-HgramInc Estimation-IN-%";
	private static final String ACDTOL_E_1TO_5_DESCRIPTION = "Acid soil tolerance-BY-AcdTol Estimation-IN-1-5 TOL scale";
	private static final String ANT_DATE_YMD_DESCRIPTION = "Anthesis time-BY-Ant DS65 date Estimation-IN-yyyymmdd";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_DESCRIPTION =
		"Barley scald severity-BY-Double digit Estimation-IN-Double digit (00-99) + text";

	// SCALES
	private static final String PERCENTAGE_SCALE = "%";
	private static final String SCALE_1_5 = "1-5 TOL scale";
	private static final String YYYYMMDD_SCALE = "yyyymmdd";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_SCALE = "Double digit (00-99) + text";

	// METHODS
	private static final String HGRAMINC_E_PCT_METHOD = "HgramInc Estimation";
	private static final String ACDTOL_E_1TO_5_METHOD = "AcdTol Estimation";
	private static final String ANT_DATE_YMD_METHOD = "Ant DS65 date Estimation";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_METHOD = "Double digit Estimation";

	// PROPERTIES
	private static final String HGRAMINC_E_PCT_PROPERTY = "Barley Helminthosporium leaf stripe incidence";
	private static final String ACDTOL_E_1TO_5_PROPERTY = "Acid soil tolerance";
	private static final String ANT_DATE_YMD_PROPERTY = "Anthesis time";
	private static final String R_SECA_SEV_E_00_TO_99_TEXT_PROPERTY = "Barley scald severity";

	// DATA TYPES
	private static final String NUMBER = "numeric";
	private static final String CATEGORICAL = "categorical";
	private static final String DATE = "date";
	private static final String CHARACTER = "Character";
	private static final String TEXT = "text";

	// LABELS
	private static final String STUDY = "STUDY";
	private static final String PLOT = "PLOT";

	private static final String CHAR = "C";
	private static final String NUMERIC = "N";

	private static final String NUMERIC_VALUE = "1";
	private static final String DATE_VALUE = "20171010";

	@Test
	public void testIsValidHeaderNamesReturnsTrueIfAllRequiredColumnsArePresent() {
		String[] headerNames = {"PLOT_NO", "ENTRY_NO", "DESIGNATION", "GID", "PLOT_ID"};
		Assert.assertTrue("Expecting that the headers are valid when all of the required column are present but didn't.",
			KsuFieldbookUtil.isValidHeaderNames(headerNames));
	}

	@Test
	public void testIsValidHeaderNamesReturnsFalseIfAtLeastOneOfTheRequiredColumnsIsNotPresent() {
		String[] headerNames = {"PLOT_NO", "ENTRY_NO", "DESIGNATION"};
		Assert.assertFalse("Expecting that the headers are not valid if at least one of the required column is not present but didn't.",
			KsuFieldbookUtil.isValidHeaderNames(headerNames));
	}

	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdPresentInEnum() {
		MeasurementTestDataInitializer measurementTestDataInitializer = new MeasurementTestDataInitializer();
		MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(TermId.ENTRY_NO.getId(), 1);
		String label = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(mVar);
		Assert.assertEquals("The label should be ENTRY_NO", "ENTRY_NO", label);

	}

	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdNotPresentInEnum() {
		MeasurementTestDataInitializer measurementTestDataInitializer = new MeasurementTestDataInitializer();
		MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(1, 1);
		String label = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(mVar);
		Assert.assertEquals("The label should be " + mVar.getName(), mVar.getName(), label);

	}

	@Test
	public void testConvertTraitsData() {

		List<MeasurementVariable> traits = initializeTraitTest();
		List<List<String>> traitsList = KsuFieldbookUtil.convertTraitsData(traits, fieldbookMiddlewareService, ontologyService);
		assertThat(traitsList, hasSize(5));
		assertThat(NUMBER, is(equalTo(traitsList.get(1).get(1))));
		assertThat(CATEGORICAL, is(equalTo(traitsList.get(2).get(1))));
		assertThat(DATE, is(equalTo(traitsList.get(3).get(1))));
		assertThat(TEXT, is(equalTo(traitsList.get(4).get(1))));

	}

	private List<MeasurementVariable> initializeListOfTraits() {
		final List<MeasurementVariable> variates = new ArrayList<>();

		MeasurementVariable measurementVariable =
			this.createMeasurementVariable(HGRAMINC_E_PCT_ID, HGRAMINC_E_PCT_NAME, HGRAMINC_E_PCT_DESCRIPTION, PERCENTAGE_SCALE,
				HGRAMINC_E_PCT_METHOD, HGRAMINC_E_PCT_PROPERTY, NUMERIC, NUMERIC_VALUE, PLOT, TermId.NUMERIC_VARIABLE.getId(),
				PhenotypicType.VARIATE);

		variates.add(measurementVariable);

		measurementVariable = this.createMeasurementVariable(ACDTOL_E_1TO_5_ID, ACDTOL_E_1TO_5_NAME, ACDTOL_E_1TO_5_DESCRIPTION, SCALE_1_5,
			ACDTOL_E_1TO_5_METHOD, ACDTOL_E_1TO_5_PROPERTY, CHARACTER, NUMERIC_VALUE, PLOT, TermId.CATEGORICAL_VARIABLE.getId(),
			PhenotypicType.VARIATE);

		variates.add(measurementVariable);

		measurementVariable = this.createMeasurementVariable(ANT_DATE_YMD_ID, ANT_DATE_YMD_NAME, ANT_DATE_YMD_DESCRIPTION, YYYYMMDD_SCALE,
			ANT_DATE_YMD_METHOD, ANT_DATE_YMD_PROPERTY, DATE, DATE_VALUE, STUDY, TermId.DATE_VARIABLE.getId(), PhenotypicType.VARIATE);
		variates.add(measurementVariable);

		measurementVariable = this.createMeasurementVariable(R_SECA_SEV_E_00_TO_99_TEXT_ID, R_SECA_SEV_E_00_TO_99_TEXT_NAME,
			R_SECA_SEV_E_00_TO_99_TEXT_DESCRIPTION, R_SECA_SEV_E_00_TO_99_TEXT_SCALE, R_SECA_SEV_E_00_TO_99_TEXT_METHOD,
			R_SECA_SEV_E_00_TO_99_TEXT_PROPERTY, TEXT, "", STUDY, TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.VARIATE);
		variates.add(measurementVariable);

		return variates;
	}

	private MeasurementVariable createMeasurementVariable(final int termId, final String name, final String description,
		final String scale, final String method, final String property, final String dataType, final String value, final String label,
		final int dataTypeId, final PhenotypicType role) {
		final MeasurementVariable variable =
			new MeasurementVariable(termId, name, description, scale, method, property, dataType, value, label);
		variable.setRole(role);
		variable.setDataTypeId(dataTypeId);
		variable.setVariableType(VariableType.TRAIT);
		return variable;
	}

	private List<MeasurementVariable> initializeTraitTest(){
		List<MeasurementVariable> traits = initializeListOfTraits();
		Method breedingMethod =
			new Method(40, "DER", "G", "SLF", "Self and Bulk", "Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490, 1,
				0, 19980708, "");

		final List<Method> methodList = Lists.newArrayList();
		methodList.add(breedingMethod);
		Term term = new Term();
		term.setId(2);
		term.setName("Breeding method");
		term.setDefinition("Breeding method");
		Property property = new Property(term);
		Mockito.when(fieldbookMiddlewareService.getAllBreedingMethods(false)).thenReturn(methodList);
		Mockito.when(ontologyService.getProperty(TermId.BREEDING_METHOD_PROP.getId())).thenReturn(property);

		return traits;
	}
}
