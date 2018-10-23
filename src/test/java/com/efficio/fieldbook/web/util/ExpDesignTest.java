
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.service.RandomizeCompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableIncompleteBlockDesignService;
import com.efficio.fieldbook.web.common.service.ResolvableRowColumnDesignService;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

@Ignore
public class ExpDesignTest extends AbstractBaseIntegrationTest {

	@Autowired
	private WorkbenchService workbenchService;
	@Autowired
	private FieldbookProperties fieldbookProperties;
	@Autowired
	private ResolvableIncompleteBlockDesignService resolveIncompleteBlockDesign;
	@Autowired
	private ResolvableRowColumnDesignService resolveRowColumn;
	@Autowired
	private RandomizeCompleteBlockDesignService randomizeBlockDesign;
	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Resource
	public FieldbookService fieldbookService;
	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	private static final String PROGRAM_UUID = "12345678";

	@Test
	public void testResolvableIncompleteBlockExpDesignRunToBvDesign() {

		final MainDesign mainDesign = experimentDesignGenerator.createResolvableIncompleteBlockDesign("6", "24", "2", "Treat", "Reps",
				"Subblocks", "Plots", 301, null, "0", "", "", false);

		try {
			final BVDesignOutput output = this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColExpDesignRunToBvDesign() {

		final MainDesign mainDesign = experimentDesignGenerator.createResolvableRowColDesign("50", "2", "5", "10", "Treat", "Reps", "Rows", "Columns",
				"Plots", 301, null, "0", "0", "", "", false);

		try {
			final BVDesignOutput output = this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRandomizeCompleteBlockDesignExpDesignRunToBvDesign() {

		final List<String> treatmentFactor = new ArrayList<String>();
		treatmentFactor.add("ENTRY_NO");
		treatmentFactor.add("FERTILIZER");

		final List<String> levels = new ArrayList<String>();
		levels.add("24");
		levels.add("3");

		final MainDesign mainDesign = experimentDesignGenerator.createRandomizedCompleteBlockDesign("6", "Reps", "Plots", 301, treatmentFactor, levels, "");

		try {
			final BVDesignOutput output = this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, mainDesign);
			Assert.assertTrue(output.isSuccess());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 24);
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setBlockSize("6");
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("1");

			final ExpDesignValidationOutput output = this.resolveIncompleteBlockDesign.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.resolveIncompleteBlockDesign
							.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates, null);
			for (final MeasurementRow measurementRow : measurementRowList) {
				System.out.println(measurementRow.toString());
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockLatinizedAdjacentDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 24);
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setBlockSize("4");
			param.setReplicationsCount("4");
			param.setNoOfEnvironments("1");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(3));
			param.setReplatinGroups("1,1,2");
			param.setNblatin("3"); // should be less than or equal the block level (ntreatment / blocksize)

			final ExpDesignValidationOutput output = this.resolveIncompleteBlockDesign.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.resolveIncompleteBlockDesign
							.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates, null);
			for (final MeasurementRow measurementRow : measurementRowList) {
				System.out.println(measurementRow.toString());
			}
			Assert.assertEquals(96, measurementRowList.size());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockLatinizedRowsDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 24);
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setBlockSize("4");
			param.setReplicationsCount("4");
			param.setNoOfEnvironments("1");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(2));
			param.setReplatinGroups("1,1,1,1");
			param.setNblatin("3"); // should be less than or equal the block level (ntreatment / blocksize)

			final ExpDesignValidationOutput output = this.resolveIncompleteBlockDesign.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.resolveIncompleteBlockDesign
							.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates, null);
			Assert.assertEquals(96, measurementRowList.size());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableIncompleteBlockLatinizedColsDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 24);
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setBlockSize("4");
			param.setReplicationsCount("4");
			param.setNoOfEnvironments("1");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(1));
			param.setReplatinGroups("4");
			param.setNblatin("3"); // should be less than or equal the block level (ntreatment / blocksize)

			final ExpDesignValidationOutput output = this.resolveIncompleteBlockDesign.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.resolveIncompleteBlockDesign
							.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates, null);
			Assert.assertEquals(96, measurementRowList.size());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColumnDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setRowsPerReplications("2");
			param.setColsPerReplications("100");
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("2");

			// number of replicates should be 2 or more
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignValidationOutput output = this.resolveRowColumn.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.resolveRowColumn.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates, null);
			for (final MeasurementRow measurementRow : measurementRowList) {
				System.out.println(measurementRow.toString());
			}

			Assert.assertEquals(800, measurementRowList.size());

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColumnAdjacentDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setRowsPerReplications("4");
			param.setColsPerReplications("50");
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("2");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(3));
			param.setReplatinGroups("1,1");
			param.setNrlatin("2");
			param.setNclatin("2");

			// number of replicates should be 2 or more
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignValidationOutput output = this.resolveRowColumn.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.resolveRowColumn.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates, null);

			Assert.assertEquals(800, measurementRowList.size());

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColumnColsDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setRowsPerReplications("4");
			param.setColsPerReplications("50");
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("2");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(1));
			param.setReplatinGroups("2");
			param.setNrlatin("2");
			param.setNclatin("2");

			// number of replicates should be 2 or more
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignValidationOutput output = this.resolveRowColumn.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.resolveRowColumn.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates, null);

			Assert.assertEquals(800, measurementRowList.size());

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testResolvableRowColumnRowsDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setRowsPerReplications("4");
			param.setColsPerReplications("50");
			param.setReplicationsCount("3");
			param.setNoOfEnvironments("2");
			param.setUseLatenized(true);
			param.setReplicationsArrangement(Integer.valueOf(2));
			param.setReplatinGroups("1,1");
			param.setNrlatin("3");
			param.setNclatin("2");

			// number of replicates should be 2 or more
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignValidationOutput output = this.resolveRowColumn.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.resolveRowColumn.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates, null);

			Assert.assertEquals(1200, measurementRowList.size());

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRandomizeCompleteBlockDesignService() {

		try {
			final List<ImportedGermplasm> germplasmList = this.createGermplasmList("Test", 200);

			final List<TreatmentVariable> treatmentVarList = new ArrayList<TreatmentVariable>();

			final TreatmentVariable treatmentVar1 = new TreatmentVariable();
			treatmentVar1.setLevelVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(8284,PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			treatmentVar1.setValueVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(8282,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final TreatmentVariable treatmentVar2 = new TreatmentVariable();
			treatmentVar2.setLevelVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(8377,PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			treatmentVar2.setValueVariable(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(8263,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			treatmentVarList.add(treatmentVar2);

			final ExpDesignParameterUi param = new ExpDesignParameterUi();
			param.setReplicationsCount("2");
			param.setNoOfEnvironments("2");

			final Map<String, Map<String, List<String>>> treatmentFactorValues = new HashMap<String, Map<String, List<String>>>(); // Key - CVTerm
																																// ID , List
																																// of values
			final Map<String, List<String>> treatmentData = new HashMap<String, List<String>>();
			treatmentData.put("labels", Arrays.asList("100", "200", "300"));

			treatmentFactorValues.put("8284", treatmentData);
			treatmentFactorValues.put("8377", treatmentData);

			param.setTreatmentFactorsData(treatmentFactorValues);

			// number of replicates should be 2 or more
			final List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
			measurementVariables.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),PROGRAM_UUID), Operation.ADD,
					this.fieldbookService));

			final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.GID.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));
			factors.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(TermId.DESIG.getId(),PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();
			variates.add(ExpDesignUtil.convertStandardVariableToMeasurementVariable(
					this.fieldbookMiddlewareService.getStandardVariable(20368,PROGRAM_UUID), Operation.ADD, this.fieldbookService));

			final ExpDesignValidationOutput output = this.randomizeBlockDesign.validate(param, germplasmList);
			Assert.assertTrue(output.isValid());

			final List<MeasurementRow> measurementRowList =
					this.randomizeBlockDesign.generateDesign(germplasmList, param, measurementVariables, factors, factors, variates,
							treatmentVarList);

			Assert.assertEquals(7200, measurementRowList.size());

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private List<ImportedGermplasm> createGermplasmList(final String prefix, final int size) {
		final List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();

		for (int i = 0; i < size; i++) {
			final ImportedGermplasm germplasm = new ImportedGermplasm(i + 1, prefix + (i + 1), null);
			list.add(germplasm);
		}

		return list;
	}

}
