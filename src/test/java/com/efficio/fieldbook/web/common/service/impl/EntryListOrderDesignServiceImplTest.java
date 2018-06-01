package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class EntryListOrderDesignServiceImplTest {

	private ExpDesignParameterUi expDesignParameterUi;
	private List<ImportedGermplasm> germplasmList;
	private List<MeasurementVariable> measurementVariables;
	private List<MeasurementVariable> factors;
	private List<MeasurementVariable> nonStudyFactors;
	private List<MeasurementVariable> variates;
	private List<TreatmentVariable> treatmentVariables;

	private static final String PROGRAM_UUID = "2191a54c-7d98-40d0-ae6f-6a400e4546ce";

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private MessageSource messageSource;

	@Mock
	public ContextUtil contextUtil;

	@InjectMocks
	private EntryListOrderDesignServiceImpl entryListOrderDesignServiceImpl;

	@Before
	public void init() {
		final StandardVariable plotNoVariable =
				StandardVariableTestDataInitializer.createStandardVariable(TermId.PLOT_NO.getId(), "Plot No");

		expDesignParameterUi = new ExpDesignParameterUi();
		expDesignParameterUi.setNoOfEnvironments("1");
		expDesignParameterUi.setNoOfEnvironmentsToAdd("1");
		expDesignParameterUi.setDesignType(5);
		expDesignParameterUi.setTotalGermplasmListCount("5");
		expDesignParameterUi.setStartingPlotNo("1");
		expDesignParameterUi.setStartingEntryNo("1");
		expDesignParameterUi.setCheckStartingPosition("1");
		expDesignParameterUi.setCheckSpacing("2");
		expDesignParameterUi.setCheckInsertionManner(InsertionMannerItem.INSERT_EACH_IN_TURN.getId().toString());

		germplasmList = new ArrayList<>();

		final ImportedGermplasm importedGermplasm1 = new ImportedGermplasm();
		importedGermplasm1.setEntryId(1);
		importedGermplasm1.setGid("3");
		importedGermplasm1.setEntryTypeCategoricalID(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		importedGermplasm1.setEntryTypeValue(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue());
		importedGermplasm1.setEntryTypeName(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName());

		final ImportedGermplasm importedGermplasm2 = new ImportedGermplasm();
		importedGermplasm2.setEntryId(2);
		importedGermplasm2.setGid("4");
		importedGermplasm2.setEntryTypeCategoricalID(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		importedGermplasm2.setEntryTypeValue(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue());
		importedGermplasm2.setEntryTypeName(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName());

		final ImportedGermplasm importedGermplasm3 = new ImportedGermplasm();
		importedGermplasm3.setEntryId(3);
		importedGermplasm3.setGid("5");
		importedGermplasm3.setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		importedGermplasm3.setEntryTypeValue(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeValue());
		importedGermplasm3.setEntryTypeName(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeName());

		final ImportedGermplasm importedGermplasm4 = new ImportedGermplasm();
		importedGermplasm4.setEntryId(4);
		importedGermplasm4.setGid("6");
		importedGermplasm4.setEntryTypeCategoricalID(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		importedGermplasm4.setEntryTypeValue(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue());
		importedGermplasm4.setEntryTypeName(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName());

		final ImportedGermplasm importedGermplasm5 = new ImportedGermplasm();
		importedGermplasm5.setEntryId(5);
		importedGermplasm5.setGid("7");
		importedGermplasm5.setEntryTypeCategoricalID(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		importedGermplasm5.setEntryTypeValue(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue());
		importedGermplasm5.setEntryTypeName(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeName());

		germplasmList.add(importedGermplasm1);
		germplasmList.add(importedGermplasm2);
		germplasmList.add(importedGermplasm3);
		germplasmList.add(importedGermplasm4);
		germplasmList.add(importedGermplasm5);

		measurementVariables = new ArrayList<>();
		final MeasurementVariable instanceFactorVariable =
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "Instance No", "");
		measurementVariables.add(instanceFactorVariable);

		factors = new ArrayList<>();
		final MeasurementVariable entryTypeMeasurementVariable =
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.ENTRY_TYPE.getId(), "Entry type", "");
		final MeasurementVariable gidMeasurementVariable =
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GID.getId(), "Gid", "");
		final MeasurementVariable designationMeasurementVariable =
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(), "Designation", "");
		final MeasurementVariable entryNoMeasurementVariable =
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.ENTRY_NO.getId(), "Entry No", "");
		final MeasurementVariable plotIdMeasurementVariable =
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.PLOT_ID.getId(), "Plot ID", "");
		factors.add(entryTypeMeasurementVariable);
		factors.add(gidMeasurementVariable);
		factors.add(designationMeasurementVariable);
		factors.add(entryNoMeasurementVariable);
		factors.add(plotIdMeasurementVariable);
		nonStudyFactors = factors;

		variates = new ArrayList<>();
		treatmentVariables = new ArrayList<>();

		Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), PROGRAM_UUID)).thenReturn(plotNoVariable);

	}

	private Integer getCheckEntryTypeValue(final MeasurementRow measurementRow) {
		for (final MeasurementData measurementData : measurementRow.getDataList()) {
			if (measurementData.getMeasurementVariable().getTermId() == TermId.ENTRY_TYPE.getId()) {
				return Integer.parseInt(measurementData.getcValueId());
			}
		}
		return null;
	}

	private String getGidValue(final MeasurementRow measurementRow) {
		for (final MeasurementData measurementData : measurementRow.getDataList()) {
			if (measurementData.getMeasurementVariable().getTermId() == TermId.GID.getId()) {
				return measurementData.getValue();
			}
		}
		return null;
	}

	@Test
	public void generateDesignTest() throws BVDesignException {
		final List<MeasurementRow> measurementRows = entryListOrderDesignServiceImpl
				.generateDesign(germplasmList, expDesignParameterUi, measurementVariables, factors, nonStudyFactors, variates,
						treatmentVariables);
		assertThat(measurementRows, hasSize(6));
		assertThat(getCheckEntryTypeValue(measurementRows.get(0)), equalTo(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()));
		assertThat(getCheckEntryTypeValue(measurementRows.get(1)), equalTo(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()));
		assertThat(getCheckEntryTypeValue(measurementRows.get(2)), equalTo(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()));
		assertThat(getCheckEntryTypeValue(measurementRows.get(3)), equalTo(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId()));
		assertThat(getCheckEntryTypeValue(measurementRows.get(4)), equalTo(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()));
		assertThat(getCheckEntryTypeValue(measurementRows.get(5)), equalTo(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()));

		assertThat(getGidValue(measurementRows.get(0)), equalTo(germplasmList.get(2).getGid()));
		assertThat(getGidValue(measurementRows.get(1)), equalTo(germplasmList.get(0).getGid()));
		assertThat(getGidValue(measurementRows.get(2)), equalTo(germplasmList.get(1).getGid()));
		assertThat(getGidValue(measurementRows.get(3)), equalTo(germplasmList.get(2).getGid()));
		assertThat(getGidValue(measurementRows.get(4)), equalTo(germplasmList.get(3).getGid()));
		assertThat(getGidValue(measurementRows.get(5)), equalTo(germplasmList.get(4).getGid()));
	}

}
