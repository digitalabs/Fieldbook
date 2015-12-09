
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
	@Test
	public void testGetSeason() throws FieldbookException {
		
		boolean special = false;
		
		Integer termId = 8371;
		Integer categoryId = 10030;
		String programUUID = "TESTUUID";

		TermSummary ts = new TermSummary(categoryId, "testChoice", "SelectedSeasonCode");
		Scale scale = new Scale();
		scale.addCategory(ts);
		Variable variable = new Variable();
		variable.setScale(scale);

		Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(programUUID);
		Mockito.when(ontologyVariableDataManager.getVariable(programUUID, termId, true, false)).thenReturn(variable);

		// 5 possible season settings to test
		TermId[] seasonTerms = new TermId[7];
		seasonTerms[0] = TermId.SEASON;
		seasonTerms[1] = TermId.SEASON_DRY;
		seasonTerms[2] = TermId.SEASON_MONTH;
		seasonTerms[3] = TermId.SEASON_VAR;
		seasonTerms[4] = TermId.SEASON_VAR_TEXT;
		seasonTerms[5] = TermId.SEASON_WET;
		seasonTerms[6] = TermId.SEASON_VAR; // special case where SEASON_VAR = text instead of number
		for (int i = 0; i < seasonTerms.length; i++) {
			if(i == 6) {
				special  = true;
			}
			List<MeasurementVariable> conditions = new ArrayList<>();
			conditions.add(createConditionFixture(seasonTerms[i], special));
			Workbook workbook = new Workbook();
			workbook.setConditions(conditions);
			String season = factory.getSeason(workbook);
			Assert.assertNotNull(season);
			Assert.assertNotSame("", season);
			if (i == 0) {
				Assert.assertEquals("SEASON", season);
			}
			if (i == 1) {
				Assert.assertEquals("SEASONDRY", season);
			}
			if (i == 2) {
				Assert.assertEquals("SEASONMONTH", season);
			}
			if (i == 3) {
				Assert.assertEquals("SelectedSeasonCode", season);
			}
			if (i == 4) {
				Assert.assertEquals("FreeTextSeason", season);
			}
			if (i == 5) {
				Assert.assertEquals("SEASONWET", season);
			}
			if (i == 6) {
				Assert.assertEquals("SpecialTextSeason", season);
			}
		}
	}

	/**
	 * A test that makes sure the exception is exercised for the AdvancingSourceListFactory method
	 * 
	 * @throws FieldbookException
	 */
	@Test(expected = FieldbookException.class)
	public void testSeasonFail() throws FieldbookException {
		AdvancingSourceListFactory factory = new AdvancingSourceListFactory();
		String season = "";

		// Test Exception Case
		// This is the case where the season code has been added to the Trial but no selection made from the dropdown
		List<MeasurementVariable> conditions = new ArrayList<>();
		MeasurementVariable mv = new MeasurementVariable();
		mv.setTermId(TermId.SEASON_VAR.getId());
		mv.setValue("");
		conditions.add(mv);
		Workbook workbook = new Workbook();
		workbook.setConditions(conditions);
		season = factory.getSeason(workbook);
		Assert.assertEquals(season, "");

	}

	private MeasurementVariable createConditionFixture(TermId term, boolean special) {
		String season = "";
		MeasurementVariable mv = new MeasurementVariable();
		mv.setTermId(term.getId());
		if (term.getId() == TermId.SEASON.getId()) {
			season = "SEASON";
		} else if (mv.getTermId() == TermId.SEASON_DRY.getId()) {
			season = "SEASONDRY";
		} else if (mv.getTermId() == TermId.SEASON_MONTH.getId()) {
			season = "SEASONMONTH";
		} else if (mv.getTermId() == TermId.SEASON_VAR.getId()) {
			if(special) {
				season  = "SpecialTextSeason";
			} else {
				season = "10030";
			}
		} else if (mv.getTermId() == TermId.SEASON_VAR_TEXT.getId()) {
			season = "FreeTextSeason";
		} else if (mv.getTermId() == TermId.SEASON_WET.getId()) {
			season = "SEASONWET";
		} 
		mv.setValue(season);

		return mv;
	}

}
