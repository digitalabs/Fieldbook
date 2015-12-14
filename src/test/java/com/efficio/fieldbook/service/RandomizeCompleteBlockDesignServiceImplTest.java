package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.DesignRunner;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.impl.RandomizeCompleteBlockDesignServiceImpl;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import junit.framework.Assert;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RandomizeCompleteBlockDesignServiceImplTest {

	@Mock
	private UserSelection userSelection;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private DesignRunner designRunner;

	private RandomizeCompleteBlockDesignServiceImpl randomizeCompleteBlockDesignService;
	private static final String NUMERIC_VARIABLE = "NUMERIC VARIABLE";
	private static final String TEST_METHOD = "TEST METHOD";
	private static final String TEST_SCALE = "TEST SCALE";
	private static final String TEST_PROPERTY = "TEST PROPERTY";
	private static final String TEST_DESCRIPTION = "TEST DESCRIPTION";
	private static final String ENTRY_NO = "ENTRY_NO";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String REP_NO = "REP_NO";


	@Before
	public void setUp() {
		randomizeCompleteBlockDesignService = new RandomizeCompleteBlockDesignServiceImpl();
		randomizeCompleteBlockDesignService.setContextUtil(this.contextUtil);
		randomizeCompleteBlockDesignService.setUserSelection(this.userSelection);
		randomizeCompleteBlockDesignService.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		randomizeCompleteBlockDesignService.setFieldbookService(this.fieldbookService);
		randomizeCompleteBlockDesignService.setFieldbookProperties(this.fieldbookProperties);
		randomizeCompleteBlockDesignService.setWorkbenchService(this.workbenchService);
	}

	/**
	 * Test to verify generateDesign method
	 */
	@Test
	public void testGenerateDesign() throws Exception {

		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();

		List<ImportedGermplasm> germplasmList = this.fillGermplasmList(importedGermplasm);

		ExpDesignParameterUi parameter = new ExpDesignParameterUi();
		List<MeasurementVariable> factors = new ArrayList<>();
		List<MeasurementVariable> nonTrialFactors = new ArrayList<>();
		List<MeasurementVariable> variates = new ArrayList<>();
		List<TreatmentVariable> treatmentVariables = new ArrayList<>();

		parameter.setReplicationsCount("2");
		parameter.setNoOfEnvironments("1");
		parameter.setNoOfEnvironmentsToAdd("2");

		Map<String, Map<String, Object>> treatmentFactorValues = new HashMap<>();
		Map<String, Object> treatmentData = new HashMap<>();
		treatmentData.put("labels", Arrays.asList("100"));
		treatmentData.put("variableId", "10");

		treatmentFactorValues.put("8284", treatmentData);

		parameter.setTreatmentFactorsData(treatmentFactorValues);

		SettingDetail settingDetail = new SettingDetail();
		SettingVariable settingVariable = new SettingVariable();
		settingVariable.setCvTermId(TermId.PLOT_NO.getId());
		settingVariable.setName("PLOT_NO");
		settingDetail.setVariable(settingVariable);

		List<SettingDetail> treatmentFactorsList = new ArrayList<>();
		treatmentFactorsList.add(settingDetail);

		MeasurementVariable trialVariable = new MeasurementVariable();
		List<MeasurementVariable> trialVariables = this.createTrialVariable(trialVariable);

		StandardVariable stdVar1 = this.createStandardVariable(TermId.TRIAL_DESIGN_INFO_STORAGE.getId(), "TRIAL_DESIGN");
		StandardVariable stdVar3 = this.createStandardVariable(TermId.ENTRY_NO.getId(), "ENTRY_NO");
		StandardVariable stdVar4 = this.createStandardVariable(TermId.REP_NO.getId(), "REP_NO");
		StandardVariable stdVar5 = this.createStandardVariable(TermId.PLOT_NO.getId(), "PLOT_NO");

		Mockito.when(this.userSelection.getTreatmentFactors()).thenReturn(treatmentFactorsList);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn("uuid", "uuid", "uuid", "uuid", "uuid");
		Mockito.when(this.fieldbookMiddlewareService.getStandardVariable(Mockito.anyInt(), Mockito.anyString())).thenReturn(stdVar1,
				stdVar1, stdVar3, stdVar4, stdVar5);
		Mockito.when(this.fieldbookService.getAllPossibleValues(Mockito.anyInt())).thenReturn(new ArrayList<ValueReference>());
		Mockito.when(this.userSelection.getStartingPlotNo()).thenReturn(501);
		Mockito.when(this.fieldbookService.runBVDesign(Mockito.isA(WorkbenchService.class), Mockito.isA(FieldbookProperties.class),
				Mockito.isA(MainDesign.class))).thenReturn(this.createBvOutput());

		List<MeasurementRow> measurementRowList = randomizeCompleteBlockDesignService.generateDesign(germplasmList, parameter,
				trialVariables, factors, nonTrialFactors, variates, treatmentVariables);

		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(5)).getStandardVariable(Mockito.anyInt(), Mockito.anyString());

		Assert.assertNotNull("Measurement Row List", measurementRowList);
		Assert.assertEquals("Rep Number", "REP_NO", measurementRowList.get(0).getDataList().get(1).getLabel());
		Assert.assertEquals("Trial Design", "TRIAL_DESIGN", measurementRowList.get(0).getDataList().get(3).getLabel());
		Assert.assertEquals("Plot Number", "PLOT_NO", measurementRowList.get(1).getDataList().get(2).getLabel());
	}


	private StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(RandomizeCompleteBlockDesignServiceImplTest.TEST_DESCRIPTION);

		final Term prop = new Term();
		prop.setName(RandomizeCompleteBlockDesignServiceImplTest.TEST_PROPERTY);
		stdVar.setProperty(prop);

		final Term scale = new Term();
		scale.setName(RandomizeCompleteBlockDesignServiceImplTest.TEST_SCALE);
		stdVar.setScale(scale);

		final Term method = new Term();
		method.setName(RandomizeCompleteBlockDesignServiceImplTest.TEST_METHOD);
		stdVar.setMethod(method);

		final Term dataType = new Term();
		dataType.setName(RandomizeCompleteBlockDesignServiceImplTest.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}

	private List<String[]> createEntries() {
		List<String[]> entries = new ArrayList<>();
		String[] headers = new String[] {RandomizeCompleteBlockDesignServiceImplTest.PLOT_NO, RandomizeCompleteBlockDesignServiceImplTest.REP_NO, RandomizeCompleteBlockDesignServiceImplTest.ENTRY_NO};

		entries.add(headers);

		for (int i = 0; i < 6; i++) {
			String value = String.valueOf(i);
			String[] data = new String[] {value, value, value};
			entries.add(data);
		}
		return entries;
	}

	private BVDesignOutput createBvOutput() {
		BVDesignOutput bvOutput = new BVDesignOutput(0);

		List<String[]> entries = this.createEntries();
		bvOutput.setResults(entries);

		return bvOutput;
	}

	private List<ImportedGermplasm> fillGermplasmList(final ImportedGermplasm importedGermplasm) {
		List<ImportedGermplasm> germplasmList = new ArrayList<>();
		importedGermplasm.setEntryId(1);
		importedGermplasm.setDesig("(CML454 X CML451)-B-4-1-112");
		importedGermplasm.setEntryCode("101");
		importedGermplasm.setGid("123");
		importedGermplasm.setCheck("1");
		importedGermplasm.setCross("CML454/CML451");
		importedGermplasm.setSource("AF07A-412-205-1");
		importedGermplasm.setGroupName("Group");
		germplasmList.add(importedGermplasm);

		return germplasmList;
	}

	private List<MeasurementVariable> createTrialVariable(MeasurementVariable trialVariable) {
		List<MeasurementVariable> trialVariableList = new ArrayList<>();
		trialVariable.setTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		trialVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		trialVariable.setDataType("Numeric");
		trialVariable.setName("Trial Variable");
		trialVariable.setDescription("Trial Description");
		trialVariable.setFactor(true);
		trialVariable.setLabel("Label");
		trialVariable.setMethod("Method");
		trialVariableList.add(trialVariable);

		return trialVariableList;
	}
}
