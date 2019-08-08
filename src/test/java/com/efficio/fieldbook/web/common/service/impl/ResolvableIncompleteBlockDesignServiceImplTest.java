
package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.bvdesign.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ResolvableIncompleteBlockDesignServiceImplTest {

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@InjectMocks
	private ExperimentDesignGenerator experimentDesignGenerator;

	@InjectMocks
	private ResolvableIncompleteBlockDesignServiceImpl resolveIncompleteBlockDesignImpl;

	private static final String PROGRAM_UUID = "2191a54c-7d98-40d0-ae6f-6a400e4546ce";

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

	Locale locale = LocaleContextHolder.getLocale();

	@Before
	public void beforeEachTest() throws IOException {

		this.messageSource.setUseCodeAsDefaultMessage(true);
		this.resolveIncompleteBlockDesignImpl.setMessageSource(this.messageSource);

		this.resolveIncompleteBlockDesignImpl.setExperimentDesignGenerator(this.experimentDesignGenerator);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(ResolvableIncompleteBlockDesignServiceImplTest.PROGRAM_UUID);

		final StandardVariable entryVar = new StandardVariable();
		entryVar.setId(TermId.ENTRY_NO.getId());
		entryVar.setName("ENTRY_NO");
		entryVar.setProperty(new Term());
		entryVar.setMethod(new Term());
		entryVar.setScale(new Term());
		entryVar.setDataType(new Term());
		Mockito.when(
				this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),
						ResolvableIncompleteBlockDesignServiceImplTest.PROGRAM_UUID)).thenReturn(entryVar);

		final StandardVariable repVar = new StandardVariable();
		repVar.setId(TermId.REP_NO.getId());
		repVar.setName("REP_NO");
		repVar.setProperty(new Term());
		repVar.setMethod(new Term());
		repVar.setScale(new Term());
		repVar.setDataType(new Term());
		Mockito.when(
				this.fieldbookMiddlewareService.getStandardVariable(TermId.REP_NO.getId(),
						ResolvableIncompleteBlockDesignServiceImplTest.PROGRAM_UUID)).thenReturn(repVar);

		final StandardVariable blockVar = new StandardVariable();
		blockVar.setId(TermId.BLOCK_NO.getId());
		blockVar.setName("BLOCK_NO");
		blockVar.setProperty(new Term());
		blockVar.setMethod(new Term());
		blockVar.setScale(new Term());
		blockVar.setDataType(new Term());
		Mockito.when(
				this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(),
						ResolvableIncompleteBlockDesignServiceImplTest.PROGRAM_UUID)).thenReturn(blockVar);

		final StandardVariable plotVar = new StandardVariable();
		plotVar.setId(TermId.PLOT_NO.getId());
		plotVar.setName("PLOT_NO");
		plotVar.setProperty(new Term());
		plotVar.setMethod(new Term());
		plotVar.setScale(new Term());
		plotVar.setDataType(new Term());
		Mockito.when(
				this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(),
						ResolvableIncompleteBlockDesignServiceImplTest.PROGRAM_UUID)).thenReturn(plotVar);

		Mockito.when(
				this.fieldbookService.runBVDesign(Matchers.any(FieldbookProperties.class),
						Matchers.any(MainDesign.class))).thenReturn(this.mockDesignOutPut());
	}

	private BVDesignOutput mockDesignOutPut() {
		final List<String[]> csvLines = new ArrayList<>();
		csvLines.add(new String[] {"PLOT_NO", "REP_NO", "BLOCK_NO", "ENTRY_NO"});

		csvLines.add(StringUtils.split("6,1,1,13", ","));
		csvLines.add(StringUtils.split("7,1,1,8", ","));
		csvLines.add(StringUtils.split("8,1,2,14", ","));
		csvLines.add(StringUtils.split("9,1,2,7", ","));
		csvLines.add(StringUtils.split("10,1,3,6", ","));
		csvLines.add(StringUtils.split("11,1,3,5", ","));
		csvLines.add(StringUtils.split("12,1,4,9", ","));
		csvLines.add(StringUtils.split("13,1,4,11", ","));
		csvLines.add(StringUtils.split("14,1,5,12", ","));
		csvLines.add(StringUtils.split("15,1,5,10", ","));

		csvLines.add(StringUtils.split("16,2,1,11", ","));
		csvLines.add(StringUtils.split("17,2,1,8", ","));
		csvLines.add(StringUtils.split("18,2,2,12", ","));
		csvLines.add(StringUtils.split("19,2,2,9", ","));
		csvLines.add(StringUtils.split("20,2,3,13", ","));
		csvLines.add(StringUtils.split("21,2,3,7", ","));
		csvLines.add(StringUtils.split("22,2,4,6", ","));
		csvLines.add(StringUtils.split("23,2,4,10", ","));
		csvLines.add(StringUtils.split("24,2,5,14", ","));
		csvLines.add(StringUtils.split("25,2,5,5", ","));

		final BVDesignOutput output = new BVDesignOutput(0);
		output.setResults(csvLines);
		return output;
	}

	/**
	 * Tests design generation with entry numbers and plot numbers that are not starting at 1.
	 */
	@Test
	public void testGenerateDesign() throws Exception {



		final int startingEntryNo = 5;
		final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", startingEntryNo, 10);
		final List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
		trialVariables.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "name", "desc", "scale", "method",
				"property", "dataType", 1));

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments("2");
		param.setReplicationsCount("2");
		param.setStartingEntryNo(String.valueOf(startingEntryNo));
		param.setStartingPlotNo("6");
		param.setBlockSize("5");
		param.setUseLatenized(false);
		param.setTreatmentFactorsData(new HashMap());
		// Dont understand what this is for adding in test data to avoid number format exception.
		param.setNoOfEnvironmentsToAdd("1");

		final ExpDesignValidationOutput output = this.resolveIncompleteBlockDesignImpl.validate(param, germplasmList);
		Assert.assertEquals(true, output.isValid());

		final List<MeasurementRow> measurementRowList =
				this.resolveIncompleteBlockDesignImpl.generateDesign(germplasmList, param, trialVariables,
						new ArrayList<MeasurementVariable>(), new ArrayList<MeasurementVariable>(), new ArrayList<MeasurementVariable>(),
						null);

		Assert.assertTrue(!measurementRowList.isEmpty());
		Assert.assertEquals("Expected number of measurement rows were not generated: ", 20, measurementRowList.size());
	}

	@Test
	public void testValidateInvalidEntryNumber(){

		final int treatmentSize = 200;
		final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 100, treatmentSize);

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setReplicationsCount("2");
		param.setNoOfEnvironments("2");
		param.setBlockSize("25");
		final String startingEntryNo = "99990";
		param.setStartingEntryNo(startingEntryNo);
		param.setStartingPlotNo("400");

		final Map<String, Map<String, List<String>>> treatmentFactorValues = new HashMap<String, Map<String, List<String>>>(); // Key - CVTerm
		param.setTreatmentFactorsData(treatmentFactorValues);

		final Integer maxEntry = treatmentSize + Integer.valueOf(startingEntryNo) - 1;
		final String errorMessage = this.messageSource.getMessage("experiment.design.entry.number.should.not.exceed", new Object[] {maxEntry},
			this.locale);

		final ExpDesignValidationOutput output = this.resolveIncompleteBlockDesignImpl.validate(param, germplasmList);

		Assert.assertEquals("Invalid entry number", output.getMessage(), errorMessage);
	}

	@Test
	public void testValidateInvalidPlotNumber(){

		final int treatmentSize = 200;
		final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 100, treatmentSize);

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		final String replicationsCount = "2";
		param.setReplicationsCount(replicationsCount);
		param.setNoOfEnvironments("2");
		param.setBlockSize("25");
		param.setStartingEntryNo("200");
		final String startingPlotNo = "99999970";
		param.setStartingPlotNo(startingPlotNo);

		final Map<String, Map<String, List<String>>> treatmentFactorValues = new HashMap<String, Map<String, List<String>>>(); // Key - CVTerm
		param.setTreatmentFactorsData(treatmentFactorValues);

		final int total = (treatmentSize * Integer.valueOf(replicationsCount)) + Integer.valueOf(startingPlotNo) - 1;
		final String errorMessage = this.messageSource.getMessage("experiment.design.plot.number.should.not.exceed", new Object[] {total},
			this.locale);

		final ExpDesignValidationOutput output = this.resolveIncompleteBlockDesignImpl.validate(param, germplasmList);

		Assert.assertEquals("Invalid plot number", output.getMessage(), errorMessage);
	}

	@Test
	public void validate_NbLatinGreaterOrEqualToBlockLevel_ErrorMessageAddedToOutput() {
		final int startingEntryNo = 5;
		final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", startingEntryNo, 10);
		final List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
		final String errorMessage = this.messageSource.getMessage("experiment.design.nblatin.should.not.be.greater.than.block.level", null, LocaleContextHolder.getLocale());

		trialVariables.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "name", "desc", "scale", "method",
				"property", "dataType", 1));

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments("2");
		param.setReplicationsCount("2");
		param.setStartingEntryNo(String.valueOf(startingEntryNo));
		param.setStartingPlotNo("6");
		param.setBlockSize("5");
		param.setUseLatenized(true);
		param.setTreatmentFactorsData(new HashMap());
		// Blocks to latinize is equal to block level
		param.setNblatin("2");
		ExpDesignValidationOutput output = this.resolveIncompleteBlockDesignImpl.validate(param, germplasmList);
		Assert.assertEquals("Number of blocks to latinize should be less than block level", output.getMessage(), errorMessage);

		// Blocks to latinize is greater than block level
		param.setNblatin("3");
		output = this.resolveIncompleteBlockDesignImpl.validate(param, germplasmList);
		Assert.assertEquals("Number of blocks to latinize should be less than block level", output.getMessage(), errorMessage);

	}

	@Test
	public void validate_NbLatinLessThanBlockLevel_NoErrorMessage() {
		final int startingEntryNo = 5;
		final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", startingEntryNo, 10);
		final List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
		final String errorMessage = "Number of contiguous block should be less than the block level.";

		trialVariables.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "name", "desc", "scale", "method",
				"property", "dataType", 1));

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments("2");
		param.setReplicationsCount("2");
		param.setStartingEntryNo(String.valueOf(startingEntryNo));
		param.setStartingPlotNo("6");
		param.setBlockSize("5");
		param.setUseLatenized(true);
		param.setTreatmentFactorsData(new HashMap());

		// Blocks to latinize is less than block level
		param.setNblatin("1");
		final ExpDesignValidationOutput output = this.resolveIncompleteBlockDesignImpl.validate(param, germplasmList);
		Assert.assertTrue("Valid number of blocks to latinize", output.getMessage().isEmpty());
	}

	private MeasurementVariable createMeasurementVariable(final int varId, final String name, final String desc, final String scale,
			final String method, final String property, final String dataType, final int dataTypeId) {
		final MeasurementVariable mvar = new MeasurementVariable(name, desc, scale, method, property, dataType, null, "");
		mvar.setFactor(true);
		mvar.setTermId(varId);
		mvar.setDataTypeId(dataTypeId);
		return mvar;
	}
	
	private List<ImportedGermplasm> createGermplasmList(final String prefix, final int startingEntryNo, final int size) {
		final List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
		for (int i = startingEntryNo; i < startingEntryNo + size; i++) {
			final ImportedGermplasm germplasm = new ImportedGermplasm(i, prefix + i, null);
			list.add(germplasm);
		}
		return list;
	}
}
