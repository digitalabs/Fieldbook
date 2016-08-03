
package com.efficio.fieldbook.web.naming.expression.dataprocessor;

import java.util.ArrayList;
import java.util.Arrays;

import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.ValueReferenceTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class SeasonExpressionDataProcessorTest {

	private static final String EMPTY_STRING = "";
	private static final String SEASON_CATEGORY_ID = "10290";
	private static final String SEASON_CATEGORY_VALUE = "Dry Season";
	private static final String SEASON_MONTH_VALUE = "201608";

	@InjectMocks
	private SeasonExpressionDataProcessor seasonExpressionDataProcessor;

	private WorkbookTestDataInitializer workbookTDI;
	private MeasurementVariableTestDataInitializer measurementVarTDI;
	private MeasurementDataTestDataInitializer measurementDataTDI;
	private ValueReferenceTestDataInitializer valueReferenceTDI;
	private AdvancingSource advancingSource;

	@Before
	public void setUp() {
		this.workbookTDI = new WorkbookTestDataInitializer();
		this.measurementVarTDI = new MeasurementVariableTestDataInitializer();
		this.measurementDataTDI = new MeasurementDataTestDataInitializer();
		this.valueReferenceTDI = new ValueReferenceTestDataInitializer();
		this.advancingSource = new AdvancingSource();
	}

	@Test
	public void testProcessEnvironmentLevelDataWithSeasonMonthVariable() {
		final Workbook workbook = this.workbookTDI.createWorkbook(StudyType.N);

		final MeasurementVariable seasonMV = this.measurementVarTDI.createMeasurementVariable(TermId.SEASON_MONTH.getId(),
				SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE);

		workbook.setConditions(Lists.newArrayList(seasonMV));

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessEnvironmentLevelDataWithSeasonVarTextVariable() {
		final Workbook workbook = this.workbookTDI.createWorkbook(StudyType.N);

		final MeasurementVariable seasonMV = this.measurementVarTDI.createMeasurementVariable(TermId.SEASON_VAR_TEXT.getId(),
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE);

		workbook.setConditions(Lists.newArrayList(seasonMV));

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessEnvironmentLevelDataWithSeasonVarVariable() {
		final Workbook workbook = this.workbookTDI.createWorkbook(StudyType.N);

		final MeasurementVariable seasonMV = this.measurementVarTDI.createMeasurementVariable(TermId.SEASON_VAR.getId(),
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE);

		workbook.setConditions(Lists.newArrayList(seasonMV));

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessEnvironmentLevelDataWithNumericSeasonVarVariable() {
		final Workbook workbook = this.workbookTDI.createWorkbook(StudyType.N);

		final MeasurementVariable seasonMV = this.measurementVarTDI.createMeasurementVariable(TermId.SEASON_VAR.getId(),
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_ID);
		seasonMV.setPossibleValues(Arrays.asList(this.valueReferenceTDI.createValueReference(Integer.parseInt(SEASON_CATEGORY_ID), SEASON_CATEGORY_VALUE)));

		workbook.setConditions(Lists.newArrayList(seasonMV));

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessEnvironmentLevelDataWithNoSeasonVariable() {
		final Workbook workbook = this.workbookTDI.createWorkbook(StudyType.N);
		workbook.setConditions(new ArrayList<MeasurementVariable>());

		this.seasonExpressionDataProcessor.processEnvironmentLevelData(this.advancingSource, workbook, null, null);
		Assert.assertEquals("The season should be an empty String", SeasonExpressionDataProcessorTest.EMPTY_STRING,
				this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithSeasonMonthVariable() {
		final MeasurementVariable instance1SeasonMV = this.measurementVarTDI.createMeasurementVariable(TermId.SEASON_MONTH.getId(), "");
		final MeasurementData instance1SeasonMD =
				this.measurementDataTDI.createMeasurementData(SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE, instance1SeasonMV);

		final MeasurementVariable instance1MV = this.measurementVarTDI.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData instance1MD = this.measurementDataTDI.createMeasurementData("1", instance1MV);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(instance1MD, instance1SeasonMD));

		this.advancingSource.setStudyType(StudyType.T);
		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_MONTH_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithSeasonVarTextVariable() {
		final MeasurementVariable instance1SeasonMV = this.measurementVarTDI.createMeasurementVariable(TermId.SEASON_VAR_TEXT.getId(), "");
		final MeasurementData instance1SeasonMD =
				this.measurementDataTDI.createMeasurementData(SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, instance1SeasonMV);

		final MeasurementVariable instance1MV = this.measurementVarTDI.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData instance1MD = this.measurementDataTDI.createMeasurementData("1", instance1MV);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(instance1MD, instance1SeasonMD));

		this.advancingSource.setStudyType(StudyType.T);
		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithSeasonVarVariable() {
		final MeasurementVariable instance1SeasonMV = this.measurementVarTDI.createMeasurementVariable(TermId.SEASON_VAR.getId(), "");
		final MeasurementData instance1SeasonMD =
				this.measurementDataTDI.createMeasurementData(SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, instance1SeasonMV);

		final MeasurementVariable instance1MV = this.measurementVarTDI.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData instance1MD = this.measurementDataTDI.createMeasurementData("1", instance1MV);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(instance1MD, instance1SeasonMD));

		this.advancingSource.setStudyType(StudyType.T);
		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithNumericSeasonVarVariable() {
		final MeasurementVariable instance1SeasonMV = this.measurementVarTDI.createMeasurementVariable(TermId.SEASON_VAR.getId(), "");
		instance1SeasonMV.setPossibleValues(Arrays.asList(this.valueReferenceTDI.createValueReference(Integer.parseInt(SEASON_CATEGORY_ID), SEASON_CATEGORY_VALUE)));
		final MeasurementData instance1SeasonMD =
				this.measurementDataTDI.createMeasurementData(SeasonExpressionDataProcessorTest.SEASON_CATEGORY_ID, instance1SeasonMV);

		final MeasurementVariable instance1MV = this.measurementVarTDI.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData instance1MD = this.measurementDataTDI.createMeasurementData("1", instance1MV);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(instance1MD, instance1SeasonMD));

		this.advancingSource.setStudyType(StudyType.T);
		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be " + SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE,
				SeasonExpressionDataProcessorTest.SEASON_CATEGORY_VALUE, this.advancingSource.getSeason());
	}

	@Test
	public void testProcessPlotLevelDataWithNoSeasonVariable() {
		final MeasurementVariable instance1MV = this.measurementVarTDI.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "");
		final MeasurementData instance1MD = this.measurementDataTDI.createMeasurementData("1", instance1MV);

		final MeasurementRow trialInstanceObservation = new MeasurementRow();
		trialInstanceObservation.setDataList(Lists.newArrayList(instance1MD));

		this.advancingSource.setStudyType(StudyType.T);
		this.advancingSource.setTrailInstanceObservation(trialInstanceObservation);

		this.seasonExpressionDataProcessor.processPlotLevelData(this.advancingSource, trialInstanceObservation);
		Assert.assertEquals("The season should be an empty String", SeasonExpressionDataProcessorTest.EMPTY_STRING,
				this.advancingSource.getSeason());
	}

}
