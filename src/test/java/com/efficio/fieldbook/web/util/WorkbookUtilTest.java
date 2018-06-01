
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.commons.data.initializer.ImportedGermplasmTestDataInitializer;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.data.initializer.MeasurementRowTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.data.initializer.DesignImportTestDataInitializer;
import com.google.common.base.Optional;

import junit.framework.Assert;

public class WorkbookUtilTest {

	@Test
	public void testUpdateTrialObservations() {
		final Workbook currentWorkbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 3);

		final Workbook temporaryWorkbook = WorkbookDataUtil.getTestWorkbookForStudy(10, 2);

		WorkbookUtil.updateTrialObservations(currentWorkbook, temporaryWorkbook);

		Assert.assertEquals(
				"Expecting that the study observations of temporary workbook is copied to current workbook. ",
				currentWorkbook.getTrialObservations(), temporaryWorkbook.getTrialObservations());
	}

	@Test
	public void testResetObservationToDefaultDesign() {
		final Workbook nursery = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto("N"));
		final List<MeasurementRow> observations = nursery.getObservations();

		DesignImportTestDataInitializer.updatePlotNoValue(observations);

		WorkbookUtil.resetObservationToDefaultDesign(observations);

		for (final MeasurementRow row : observations) {
			final List<MeasurementData> dataList = row.getDataList();
			final MeasurementData entryNoData = WorkbookUtil
					.retrieveMeasurementDataFromMeasurementRow(TermId.ENTRY_NO.getId(), dataList);
			final MeasurementData plotNoData = WorkbookUtil
					.retrieveMeasurementDataFromMeasurementRow(TermId.PLOT_NO.getId(), dataList);
			Assert.assertEquals("Expecting that the PLOT_NO value is equal to ENTRY_NO.", entryNoData.getValue(),
					plotNoData.getValue());
		}

	}

	@Test
	public void testFindMeasurementVariableByName() {

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		final String variable1 = "VARIABLE1";
		measurementVariable1.setName(variable1);
		measurementVariables.add(measurementVariable1);

		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		final String variable2 = "VARIABLE2";
		measurementVariable2.setName(variable2);
		measurementVariables.add(measurementVariable2);

		final MeasurementVariable measurementVariable3 = new MeasurementVariable();
		final String variable3 = "VARIABLE_3";
		measurementVariable3.setName(variable3);
		measurementVariables.add(measurementVariable3);

		final Optional<MeasurementVariable> result = WorkbookUtil.findMeasurementVariableByName(measurementVariables,
				variable1);

		Assert.assertTrue(result.isPresent());
		Assert.assertEquals(measurementVariable1, result.get());
	}

	@Test
	public void testAddFactorsToMeasurementRowDataList() {
		final MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		final StandardVariable stdVariable = StandardVariableTestDataInitializer
				.createStandardVariable(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name());
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.PLOT_CODE.getId(), TermId.PLOT_CODE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals("", row.getDataList().get(4).getValue());
	}

	@Test
	public void testAddFactorsToMeasurementRowDataListForGroupGID() {
		final MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		final StandardVariable stdVariable = StandardVariableTestDataInitializer
				.createStandardVariable(TermId.GROUPGID.getId(), TermId.GROUPGID.name());
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.GROUPGID.getId(), TermId.GROUPGID.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		final ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getGroupId().toString(), row.getDataList().get(4).getValue());
	}

	@Test
	public void testAddFactorsToMeasurementRowDataListForSEED_SOURCE() {
		final MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		final StandardVariable stdVariable = StandardVariableTestDataInitializer
				.createStandardVariable(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name());
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		final ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getSource(), row.getDataList().get(4).getValue());
	}

	@Test
	public void testAddMeasurementDataToRowsExp() {
		final List<MeasurementVariable> variableList = Arrays.asList(MeasurementVariableTestDataInitializer
				.createMeasurementVariableWithOperation(TermId.SEED_SOURCE.getId(), TermId.SEED_SOURCE.name(),
						TermId.SEED_SOURCE.name(), Operation.ADD));
		final List<MeasurementRow> observations = Arrays
				.asList(MeasurementRowTestDataInitializer.createMeasurementRow());
		final int previousMeasurementDataSize = observations.get(0).getDataList().size();
		final OntologyService ontologyService = Mockito.mock(OntologyService.class);
		Mockito.when(ontologyService.getProperty(Matchers.anyString())).thenReturn(new Property(
				new Term(TermId.BREEDING_METHOD_PROP.getId(), TermId.BREEDING_METHOD_PROP.name(), "definition")));
		WorkbookUtil.addMeasurementDataToRowsExp(variableList, observations, true, ontologyService,
				Mockito.mock(FieldbookService.class), "10101");

		final int measurementDataSize = observations.get(0).getDataList().size();
		// The SEED_SOURCE Variable should be added
		Assert.assertEquals(previousMeasurementDataSize + 1, measurementDataSize);

		WorkbookUtil.addMeasurementDataToRowsExp(variableList, observations, true, ontologyService,
				Mockito.mock(FieldbookService.class), "10101");
		// The SEED_SOURCE Variable should not be added since it's already in
		// the data list
		Assert.assertEquals(measurementDataSize, observations.get(0).getDataList().size());
	}

	@Test
	public void testAddFactorsToMeasurementRowDataListForSOURCE() {
		final MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		final StandardVariable stdVariable = StandardVariableTestDataInitializer
				.createStandardVariable(TermId.GERMPLASM_SOURCE.getId(), TermId.GERMPLASM_SOURCE.name());
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.GERMPLASM_SOURCE.getId(), TermId.GERMPLASM_SOURCE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		final ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getSource(), row.getDataList().get(4).getValue());
	}

	@Test
	public void testAddFactorsToMeasurementRowDataListForSTOCKID() {
		final MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		final StandardVariable stdVariable = StandardVariableTestDataInitializer
				.createStandardVariable(TermId.STOCKID.getId(), TermId.STOCKID.name());
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.STOCKID.getId(), TermId.STOCKID.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		final ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getStockIDs(), row.getDataList().get(4).getValue());
	}

	@Test
	public void testAddFactorsToMeasurementRowDataListForENTRY_CODE() {
		final MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		final StandardVariable stdVariable = StandardVariableTestDataInitializer
				.createStandardVariable(TermId.ENTRY_CODE.getId(), TermId.ENTRY_CODE.name());
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.ENTRY_CODE.getId(), TermId.ENTRY_CODE.name(), null);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		final ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getEntryCode(), row.getDataList().get(4).getValue());
	}

	@Test
	public void testAddFactorsToMeasurementRowDataListForCROSS() {
		final MeasurementRow row = MeasurementRowTestDataInitializer.createMeasurementRow();
		final StandardVariable stdVariable = StandardVariableTestDataInitializer
				.createStandardVariable(TermId.CROSS.getId(), TermId.CROSS.name());
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.CROSS.getId(), TermId.CROSS.name(), null);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		final UserSelection userSelection = Mockito.mock(UserSelection.class);
		final ImportedGermplasm importedGermplasm = this.setUpUserSelection(userSelection);
		WorkbookUtil.addFactorsToMeasurementRowDataList(row, stdVariable, true, variable, userSelection);
		Assert.assertEquals(importedGermplasm.getCross(), row.getDataList().get(4).getValue());
	}

	private ImportedGermplasm setUpUserSelection(final UserSelection userSelection) {
		final ImportedGermplasmMainInfo importedGermplasmMainInfo = Mockito.mock(ImportedGermplasmMainInfo.class);
		final ImportedGermplasmList importedGermplasmList = Mockito.mock(ImportedGermplasmList.class);
		final ImportedGermplasm importedGermplasm = ImportedGermplasmTestDataInitializer.createImportedGermplasm();

		Mockito.when(userSelection.getImportedGermplasmMainInfo()).thenReturn(importedGermplasmMainInfo);
		Mockito.when(importedGermplasmMainInfo.getImportedGermplasmList()).thenReturn(importedGermplasmList);
		Mockito.when(importedGermplasmList.getImportedGermplasms()).thenReturn(Arrays.asList(importedGermplasm));
		return importedGermplasm;
	}

}
