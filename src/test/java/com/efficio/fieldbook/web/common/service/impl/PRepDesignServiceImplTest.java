package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class PRepDesignServiceImplTest {

	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String ENTRY_NO = "ENTRY_NO";
	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private ExperimentDesignGenerator experimentDesignGenerator;

	@InjectMocks
	private PRepDesignServiceImpl pRepDesignService;

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

	private static final String PROGRAM_UUID = UUID.randomUUID().toString();

	private final Locale locale = LocaleContextHolder.getLocale();

	@Before
	public void init() {

		this.messageSource.setUseCodeAsDefaultMessage(true);
		this.pRepDesignService.setMessageSource(this.messageSource);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(PRepDesignServiceImplTest.PROGRAM_UUID);
		this.mockRequiredStandardVariables();

	}

	@Test
	public void testGenerateDesign() throws BVDesignException {

		final int startingEntryNo = 99;
		final int startingPlotNo = 100;
		final int blockSize = 3;
		final int replicationPercentage = 50;
		final int replicationNumber = 2;
		final int environments = 2;
		final int environmentsToAdd = 1;
		final List<ListItem> nRepeatsListItems = Arrays.asList(new ListItem("1"));
		final MainDesign generatedMainDesign = new MainDesign();

		final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", startingEntryNo, 10);
		final List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
		trialVariables.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "name", "desc", "scale", "method",
			"property", "dataType", 1));
		final int nTreatments = germplasmList.size();

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments(String.valueOf(environments));
		param.setReplicationPercentage(50);
		param.setReplicationsCount(String.valueOf(replicationNumber));
		param.setStartingEntryNo(String.valueOf(startingEntryNo));
		param.setStartingPlotNo(String.valueOf(startingPlotNo));
		param.setBlockSize(String.valueOf(blockSize));
		param.setNoOfEnvironmentsToAdd(String.valueOf(environmentsToAdd));
		param.setTreatmentFactorsData(new HashMap());
		final ExpDesignValidationOutput output = this.pRepDesignService.validate(param, germplasmList);
		Assert.assertEquals(true, output.isValid());

		Mockito.when(this.experimentDesignGenerator.createReplicationListItemForPRepDesign(germplasmList, param.getReplicationPercentage(),
			Integer.parseInt(param.getReplicationsCount()))).thenReturn(nRepeatsListItems);
		Mockito.when(this.experimentDesignGenerator.createPRepDesign(blockSize, nTreatments, nRepeatsListItems, ENTRY_NO,
			BLOCK_NO, PLOT_NO, startingPlotNo, startingEntryNo)).thenReturn(generatedMainDesign);

		final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
		final List<MeasurementVariable> nonTrialFactors = new ArrayList<MeasurementVariable>();
		final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
		this.pRepDesignService.generateDesign(germplasmList, param, trialVariables,
			new ArrayList<MeasurementVariable>(), new ArrayList<MeasurementVariable>(), new ArrayList<MeasurementVariable>(),
			null);

		Mockito.verify(this.experimentDesignGenerator, Mockito.times(1))
			.createReplicationListItemForPRepDesign(germplasmList, param.getReplicationPercentage(),
				Integer.parseInt(param.getReplicationsCount()));
		Mockito.verify(this.experimentDesignGenerator, Mockito.times(1))
			.createPRepDesign(blockSize, nTreatments, nRepeatsListItems, ENTRY_NO,
				BLOCK_NO, PLOT_NO, startingPlotNo, startingEntryNo);
		Mockito.verify(this.experimentDesignGenerator, Mockito.times(1))
			.generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
				variates, null, new ArrayList<StandardVariable>(this.pRepDesignService.getRequiredDesignVariablesMap().values()),
				germplasmList, generatedMainDesign,
				ENTRY_NO, null,
				new HashMap<Integer, Integer>());

	}

	@Test(expected = BVDesignException.class)
	public void testGenerateDesignBVDesignException() throws BVDesignException {

		final int startingEntryNo = 99;
		final int startingPlotNo = 100;
		final int blockSize = 3;
		final int replicationPercentage = 50;
		final int replicationNumber = 2;
		final int environments = 2;
		final int environmentsToAdd = 1;
		final List<ListItem> nRepeatsListItems = Arrays.asList(new ListItem("1"));
		final MainDesign generatedMainDesign = new MainDesign();

		final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", startingEntryNo, 10);
		final List<MeasurementVariable> trialVariables = new ArrayList<MeasurementVariable>();
		trialVariables.add(this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "name", "desc", "scale", "method",
			"property", "dataType", 1));
		final int nTreatments = germplasmList.size();

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments(String.valueOf(environments));
		param.setReplicationPercentage(50);
		param.setReplicationsCount(String.valueOf(replicationNumber));
		param.setStartingEntryNo(String.valueOf(startingEntryNo));
		param.setStartingPlotNo(String.valueOf(startingPlotNo));
		param.setBlockSize(String.valueOf(blockSize));
		param.setNoOfEnvironmentsToAdd(String.valueOf(environmentsToAdd));
		param.setTreatmentFactorsData(new HashMap());

		final ExpDesignValidationOutput output = this.pRepDesignService.validate(param, germplasmList);
		Assert.assertEquals(true, output.isValid());

		final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
		final List<MeasurementVariable> nonTrialFactors = new ArrayList<MeasurementVariable>();
		final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();

		Mockito.when(this.experimentDesignGenerator.createReplicationListItemForPRepDesign(germplasmList, param.getReplicationPercentage(),
			Integer.parseInt(param.getReplicationsCount()))).thenReturn(nRepeatsListItems);
		Mockito.when(this.experimentDesignGenerator.createPRepDesign(blockSize, nTreatments, nRepeatsListItems, ENTRY_NO,
			BLOCK_NO, PLOT_NO, startingPlotNo, startingEntryNo)).thenReturn(generatedMainDesign);
		Mockito.when(this.experimentDesignGenerator
			.generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
				variates, null, new ArrayList<StandardVariable>(this.pRepDesignService.getRequiredDesignVariablesMap().values()),
				germplasmList, generatedMainDesign,
				ENTRY_NO, null,
				new HashMap<Integer, Integer>())).thenThrow(BVDesignException.class);

		this.pRepDesignService.generateDesign(germplasmList, param, trialVariables,
			new ArrayList<MeasurementVariable>(), new ArrayList<MeasurementVariable>(), new ArrayList<MeasurementVariable>(),
			null);

		Mockito.verify(this.experimentDesignGenerator, Mockito.times(1))
			.createReplicationListItemForPRepDesign(germplasmList, param.getReplicationPercentage(),
				Integer.parseInt(param.getReplicationsCount()));
		Mockito.verify(this.experimentDesignGenerator, Mockito.times(1))
			.createPRepDesign(blockSize, nTreatments, nRepeatsListItems, ENTRY_NO,
				BLOCK_NO, PLOT_NO, startingPlotNo, startingEntryNo);
		Mockito.verify(this.experimentDesignGenerator, Mockito.times(1))
			.generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
				variates, null, new ArrayList<StandardVariable>(this.pRepDesignService.getRequiredDesignVariablesMap().values()),
				germplasmList, generatedMainDesign,
				ENTRY_NO, null,
				new HashMap<Integer, Integer>());

	}

	@Test
	public void testValidate_ReplicationPercentage() {

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments("100");
		param.setReplicationsCount("2");
		param.setStartingEntryNo("1");
		param.setStartingPlotNo("2");
		param.setBlockSize("1");
		param.setTreatmentFactorsData(new HashMap());

		param.setReplicationPercentage(101);
		Assert.assertEquals(
			PRepDesignServiceImpl.EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setReplicationPercentage(-1);
		Assert.assertEquals(
			PRepDesignServiceImpl.EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setReplicationPercentage(1);
		Assert.assertTrue(this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).isValid());

		param.setReplicationPercentage(100);
		Assert.assertTrue(this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).isValid());

	}

	@Test
	public void testValidate_BlockSize() {

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments("100");
		param.setReplicationsCount("2");
		param.setStartingEntryNo("1");
		param.setStartingPlotNo("2");
		param.setReplicationPercentage(50);
		param.setTreatmentFactorsData(new HashMap());
		param.setBlockSize("ABC");
		Assert.assertEquals(
			PRepDesignServiceImpl.EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_NUMBER,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setBlockSize("");
		Assert.assertEquals(
			PRepDesignServiceImpl.EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_NUMBER,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setBlockSize("1");
		Assert.assertTrue(this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).isValid());

	}

	@Test
	public void testValidate_ReplicationsCount() {

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments("100");
		param.setBlockSize("1");
		param.setStartingEntryNo("1");
		param.setStartingPlotNo("2");
		param.setReplicationPercentage(50);
		param.setTreatmentFactorsData(new HashMap());
		param.setReplicationsCount("ABC");
		Assert.assertEquals(
			PRepDesignServiceImpl.EXPERIMENT_DESIGN_REPLICATION_COUNT_SHOULD_BE_A_NUMBER,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setReplicationsCount("");
		Assert.assertEquals(
			PRepDesignServiceImpl.EXPERIMENT_DESIGN_REPLICATION_COUNT_SHOULD_BE_A_NUMBER,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setReplicationsCount("1");
		Assert.assertTrue(this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).isValid());

	}

	@Test
	public void testValidate_StartingEntryNumber() {

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments("100");
		param.setBlockSize("1");
		param.setStartingPlotNo("2");
		param.setReplicationPercentage(50);
		param.setReplicationsCount("1");
		param.setTreatmentFactorsData(new HashMap());

		param.setStartingEntryNo("ABC");
		Assert.assertEquals(
			PRepDesignServiceImpl.ENTRY_NUMBER_SHOULD_BE_IN_RANGE,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setStartingEntryNo("");
		Assert.assertEquals(
			PRepDesignServiceImpl.ENTRY_NUMBER_SHOULD_BE_IN_RANGE,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setStartingEntryNo("0");
		Assert.assertEquals(
			PRepDesignServiceImpl.ENTRY_NUMBER_SHOULD_BE_IN_RANGE,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setStartingEntryNo("1");
		Assert.assertTrue(this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).isValid());

	}

	@Test
	public void testValidate_StartingPlotNumber() {

		final ExpDesignParameterUi param = new ExpDesignParameterUi();
		param.setNoOfEnvironments("100");
		param.setBlockSize("1");
		param.setStartingEntryNo("2");
		param.setReplicationPercentage(50);
		param.setReplicationsCount("1");
		param.setTreatmentFactorsData(new HashMap());
		param.setStartingPlotNo("ABC");
		Assert.assertEquals(
			PRepDesignServiceImpl.PLOT_NUMBER_SHOULD_BE_IN_RANGE,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setStartingPlotNo("");
		Assert.assertEquals(
			PRepDesignServiceImpl.PLOT_NUMBER_SHOULD_BE_IN_RANGE,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setStartingPlotNo("0");
		Assert.assertEquals(
			PRepDesignServiceImpl.PLOT_NUMBER_SHOULD_BE_IN_RANGE,
			this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).getMessage());

		param.setStartingPlotNo("1");
		Assert.assertTrue(this.pRepDesignService.validate(param, new ArrayList<ImportedGermplasm>()).isValid());

	}

	@Test
	public void testRequiresBreedingViewLicence() {
		Assert.assertTrue(this.pRepDesignService.requiresBreedingViewLicence());
	}

	@Test
	public void testGetExperimentalDesignVariables() {
		final List<Integer> result = this.pRepDesignService.getExperimentalDesignVariables(null);
		Assert.assertTrue(result.contains(TermId.EXPERIMENT_DESIGN_FACTOR.getId()));
		Assert.assertTrue(result.contains(TermId.NUMBER_OF_REPLICATES.getId()));
		Assert.assertTrue(result.contains(TermId.BLOCK_SIZE.getId()));
		Assert.assertTrue(result.contains(TermId.PERCENTAGE_OF_REPLICATION.getId()));
		Assert.assertEquals(4, result.size());
	}

	@Test
	public void testGetRequiredDesignVariables() {
		final List<StandardVariable> result = this.pRepDesignService.getRequiredDesignVariables();
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(TermId.BLOCK_NO.getId(), result.get(0).getId());
		Assert.assertEquals(TermId.PLOT_NO.getId(), result.get(1).getId());
	}

	private void mockRequiredStandardVariables() {

		final StandardVariable blockVar = this.createStandardVariable(TermId.BLOCK_NO, BLOCK_NO);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), PRepDesignServiceImplTest.PROGRAM_UUID))
			.thenReturn(blockVar);

		final StandardVariable plotVar = this.createStandardVariable(TermId.PLOT_NO, PLOT_NO);
		plotVar.setId(TermId.PLOT_NO.getId());
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), PRepDesignServiceImplTest.PROGRAM_UUID))
			.thenReturn(plotVar);

		final StandardVariable entryVar = this.createStandardVariable(TermId.ENTRY_NO, ENTRY_NO);
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), PRepDesignServiceImplTest.PROGRAM_UUID))
			.thenReturn(entryVar);

	}

	private StandardVariable createStandardVariable(final TermId termId, final String name) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(termId.getId());
		standardVariable.setName(name);
		standardVariable.setProperty(new Term());
		standardVariable.setMethod(new Term());
		standardVariable.setScale(new Term());
		standardVariable.setDataType(new Term());
		return standardVariable;
	}

	private MeasurementVariable createMeasurementVariable(
		final int varId, final String name, final String desc, final String scale,
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
