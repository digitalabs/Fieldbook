
package com.efficio.fieldbook.web.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

public class ExpDesignUtil {

	public static final String NCLATIN_PARAM = "nclatin";
	public static final String NRLATIN_PARAM = "nrlatin";
	public static final String REPLATINGROUPS_PARAM = "replatingroups";
	public static final String COLUMNFACTOR_PARAM = "columnfactor";
	public static final String ROWFACTOR_PARAM = "rowfactor";
	public static final String NCOLUMNS_PARAM = "ncolumns";
	public static final String NROWS_PARAM = "nrows";
	public static final String NBLATIN_PARAM = "nblatin";
	public static final String REPLICATEFACTOR_PARAM = "replicatefactor";
	public static final String TREATMENTFACTOR_PARAM = "treatmentfactor";
	public static final String NREPLICATES_PARAM = "nreplicates";
	public static final String NTREATMENTS_PARAM = "ntreatments";
	public static final String BLOCKSIZE_PARAM = "blocksize";
	public static final String TIMELIMIT_PARAM = "timelimit";
	public static final String LEVELS_PARAM = "levels";
	public static final String TREATMENTFACTORS_PARAM = "treatmentfactors";
	public static final String PLOTFACTOR_PARAM = "plotfactor";
	public static final String BLOCKFACTOR_PARAM = "blockfactor";
	public static final String NBLOCKS_PARAM = "nblocks";
	public static final String OUTPUTFILE_PARAM = "outputfile";
	public static final String SEED_PARAM = "seed";

	private static String RANDOMIZED_COMPLETE_BLOCK_DESIGN = "RandomizedBlock";
	private static String RESOLVABLE_INCOMPLETE_BLOCK_DESIGN = "ResolvableIncompleteBlock";
	private static String RESOLVABLE_ROW_COL_DESIGN = "ResolvableRowColumn";

	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);

	private ExpDesignUtil() {
		// hide implicit public constructor
	}

	public static String getXmlStringForSetting(MainDesign mainDesign) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(mainDesign, writer);
		return writer.toString();
	}

	public static MainDesign readXmlStringForSetting(String xmlString) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (MainDesign) unmarshaller.unmarshal(new StringReader(xmlString));
	}

	public static ExpDesignParameter createExpDesignParameter(String name, String value, List<ListItem> items) {

		ExpDesignParameter designParam = new ExpDesignParameter(name, value);
		if (items != null && !items.isEmpty()) {
			designParam.setListItem(items);
		}
		return designParam;
	}

	public static MainDesign createRandomizedCompleteBlockDesign(String nBlock, String blockFactor, String plotFactor,
			List<String> treatmentFactor, List<String> levels, String outputfile) {

		String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.SEED_PARAM, "", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NBLOCKS_PARAM, nBlock, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.PLOTFACTOR_PARAM, plotFactor, null));
		List<ListItem> itemsTreatmentFactor = new ArrayList<ListItem>();
		List<ListItem> itemsLevels = new ArrayList<ListItem>();
		if (treatmentFactor != null) {
			for (String treatment : treatmentFactor) {
				ListItem listItem = new ListItem(treatment);
				itemsTreatmentFactor.add(listItem);
			}
		}
		if (levels != null) {
			for (String level : levels) {
				ListItem listItem = new ListItem(level);
				itemsLevels.add(listItem);
			}
		}
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.TREATMENTFACTORS_PARAM, null, itemsTreatmentFactor));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.LEVELS_PARAM, null, itemsLevels));

		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.OUTPUTFILE_PARAM, outputfile, null));

		ExpDesign design = new ExpDesign(ExpDesignUtil.RANDOMIZED_COMPLETE_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public static MainDesign createResolvableIncompleteBlockDesign(String blockSize, String nTreatments, String nReplicates,
			String treatmentFactor, String replicateFactor, String blockFactor, String plotFactor, String nBlatin, String replatingGroups,
			String outputfile, boolean useLatinize) {

		String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.SEED_PARAM, "", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.BLOCKSIZE_PARAM, blockSize, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NTREATMENTS_PARAM, nTreatments, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NREPLICATES_PARAM, nReplicates, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.TREATMENTFACTOR_PARAM, treatmentFactor, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.PLOTFACTOR_PARAM, plotFactor, null));
		if (useLatinize) {
			paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NBLATIN_PARAM, nBlatin, null));
			// we add the string tokenize replating groups
			// we tokenize the replating groups
			StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			List<ListItem> replatingList = new ArrayList<ListItem>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NBLATIN_PARAM, "0", null));
		}

		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.OUTPUTFILE_PARAM, outputfile, null));

		ExpDesign design = new ExpDesign(ExpDesignUtil.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public static MainDesign createResolvableRowColDesign(String nTreatments, String nReplicates, String nRows, String nColumns,
			String treatmentFactor, String replicateFactor, String rowFactor, String columnFactor, String plotFactor, String nrLatin,
			String ncLatin, String replatingGroups, String outputfile, Boolean useLatinize) {

		String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		List<ExpDesignParameter> paramList = new ArrayList<ExpDesignParameter>();
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.SEED_PARAM, "", null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NTREATMENTS_PARAM, nTreatments, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NREPLICATES_PARAM, nReplicates, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NROWS_PARAM, nRows, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NCOLUMNS_PARAM, nColumns, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.TREATMENTFACTOR_PARAM, treatmentFactor, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.ROWFACTOR_PARAM, rowFactor, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.COLUMNFACTOR_PARAM, columnFactor, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.PLOTFACTOR_PARAM, plotFactor, null));
		if (useLatinize != null && useLatinize.booleanValue()) {
			paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NRLATIN_PARAM, nrLatin, null));
			paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NCLATIN_PARAM, ncLatin, null));
			// we tokenize the replating groups
			StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			List<ListItem> replatingList = new ArrayList<ListItem>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NRLATIN_PARAM, "0", null));
			paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.NCLATIN_PARAM, "0", null));
		}
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(ExpDesignUtil.createExpDesignParameter(ExpDesignUtil.OUTPUTFILE_PARAM, outputfile, null));

		ExpDesign design = new ExpDesign(ExpDesignUtil.RESOLVABLE_ROW_COL_DESIGN, paramList);

		return new MainDesign(design);
	}

	public static MeasurementVariable convertStandardVariableToMeasurementVariable(StandardVariable var, Operation operation,
			FieldbookService fieldbookService) {
		MeasurementVariable mvar =
				new MeasurementVariable(var.getName(), var.getDescription(), var.getScale().getName(), var.getMethod().getName(), var
						.getProperty().getName(), var.getDataType().getName(), null, var.getPhenotypicType().getLabelList().get(0));
		mvar.setFactor(true);
		mvar.setOperation(operation);
		mvar.setRole(PhenotypicType.TRIAL_DESIGN);
		mvar.setTermId(var.getId());
		mvar.setDataTypeId(var.getDataType().getId());

		try {
			mvar.setPossibleValues(fieldbookService.getAllPossibleValues(var.getId()));
		} catch (MiddlewareException e) {
			ExpDesignUtil.LOG.error(e.getMessage(), e);
		}
		return mvar;
	}

	public static MeasurementRow createMeasurementRow(List<MeasurementVariable> headerVariable, ImportedGermplasm germplasm,
			Map<String, String> bvEntryMap, Map<String, List<String>> treatmentFactorValues, List<MeasurementVariable> trialVariables,
			int trialNo, List<MeasurementVariable> factors, String entryNo) throws MiddlewareQueryException {
		MeasurementRow measurementRow = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();
		MeasurementData treatmentLevelData = null;
		MeasurementData measurementData = null;

		MeasurementVariable trialInstanceVar = WorkbookUtil.getMeasurementVariable(trialVariables, TermId.TRIAL_INSTANCE_FACTOR.getId());
		measurementData =
				new MeasurementData(trialInstanceVar.getName(), Integer.toString(trialNo), false, trialInstanceVar.getDataType(),
						trialInstanceVar);
		dataList.add(measurementData);

		for (MeasurementVariable var : headerVariable) {

			measurementData = null;

			Integer termId = var.getTermId();

			if (termId.intValue() == TermId.ENTRY_NO.getId()) {
				measurementData = new MeasurementData(var.getName(), entryNo, false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.SOURCE.getId() || termId.intValue() == TermId.GERMPLASM_SOURCE.getId()) {
				measurementData =
						new MeasurementData(var.getName(), germplasm.getSource() != null ? germplasm.getSource() : "", false,
								var.getDataType(), var);
			} else if (termId.intValue() == TermId.CROSS.getId()) {
				measurementData = new MeasurementData(var.getName(), germplasm.getCross(), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.DESIG.getId()) {
				measurementData = new MeasurementData(var.getName(), germplasm.getDesig(), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.GID.getId()) {
				measurementData = new MeasurementData(var.getName(), germplasm.getGid(), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.ENTRY_CODE.getId()) {
				measurementData = new MeasurementData(var.getName(), germplasm.getEntryCode(), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.PLOT_NO.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);
			} else if (termId.intValue() == TermId.CHECK.getId()) {
				measurementData =
						new MeasurementData(var.getName(), Integer.toString(germplasm.getCheckId()), false, var.getDataType(),
								germplasm.getCheckId(), var);

			} else if (termId.intValue() == TermId.REP_NO.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);

			} else if (termId.intValue() == TermId.BLOCK_NO.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);

			} else if (termId.intValue() == TermId.ROW.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);

			} else if (termId.intValue() == TermId.COL.getId()) {
				measurementData = new MeasurementData(var.getName(), bvEntryMap.get(var.getName()), false, var.getDataType(), var);

			} else if (termId.intValue() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				measurementData = new MeasurementData(var.getName(), Integer.toString(trialNo), false, var.getDataType(), var);

			} else if (var.getTreatmentLabel() != null && !"".equals(var.getTreatmentLabel())) {
				if (treatmentLevelData == null) {
					measurementData =
							new MeasurementData(var.getName(), bvEntryMap.get(ExpDesignUtil.cleanBVDesingKey(Integer.toString(var
									.getTermId()))), false, var.getDataType(), var);
					treatmentLevelData = measurementData;
				} else {
					String level = treatmentLevelData.getValue();
					if (NumberUtils.isNumber(level)) {
						int index = Integer.valueOf(level) - 1;
						if (treatmentFactorValues != null
								&& treatmentFactorValues.containsKey(String
										.valueOf(treatmentLevelData.getMeasurementVariable().getTermId()))) {
							Object tempObj =
									treatmentFactorValues.get(String.valueOf(treatmentLevelData.getMeasurementVariable().getTermId())).get(
											index);
							String value = "";
							if (tempObj != null) {
								if (tempObj instanceof String) {
									value = (String) tempObj;
								} else {
									value = Integer.toString((Integer) tempObj);
								}
							}
							if (var.getDataTypeId() != null && var.getDataTypeId().intValue() == TermId.DATE_VARIABLE.getId()) {
								value = DateUtil.convertToDBDateFormat(var.getDataTypeId(), value);
								measurementData = new MeasurementData(var.getName(), value, false, var.getDataType(), var);
							} else if (var.getPossibleValues() != null && !var.getPossibleValues().isEmpty() && NumberUtils.isNumber(value)) {
								measurementData =
										new MeasurementData(var.getName(), value, false, var.getDataType(), Integer.parseInt(value), var);
							} else {
								measurementData = new MeasurementData(var.getName(), value, false, var.getDataType(), var);
							}
						}
					}
					treatmentLevelData = null;
				}

			} else {
				// meaning non factor
				measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
			}

			dataList.add(measurementData);
		}
		measurementRow.setDataList(dataList);
		return measurementRow;
	}

	public static List<MeasurementRow> generateExpDesignMeasurements(int environments, int environmentsToAdd,
			List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors, List<MeasurementVariable> nonTrialFactors,
			List<MeasurementVariable> variates, List<TreatmentVariable> treatmentVariables,
			List<StandardVariable> requiredExpDesignVariable, List<ImportedGermplasm> germplasmList, MainDesign mainDesign,
			WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, String entryNumberIdentifier,
			Map<String, List<String>> treatmentFactorValues, FieldbookService fieldbookService) throws BVDesignException {
		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		List<MeasurementVariable> varList = new ArrayList<MeasurementVariable>();
		varList.addAll(nonTrialFactors);
		for (StandardVariable var : requiredExpDesignVariable) {
			if (WorkbookUtil.getMeasurementVariable(nonTrialFactors, var.getId()) == null) {
				MeasurementVariable measureVar =
						ExpDesignUtil.convertStandardVariableToMeasurementVariable(var, Operation.ADD, fieldbookService);
				varList.add(measureVar);
				if (WorkbookUtil.getMeasurementVariable(factors, var.getId()) == null) {
					factors.add(measureVar);
				}
			}
		}

		if (treatmentVariables != null) {
			for (int i = 0; i < treatmentVariables.size(); i++) {
				varList.add(treatmentVariables.get(i).getLevelVariable());
				varList.add(treatmentVariables.get(i).getValueVariable());
				if (WorkbookUtil.getMeasurementVariable(factors, treatmentVariables.get(i).getLevelVariable().getTermId()) == null) {
					factors.add(treatmentVariables.get(i).getLevelVariable());
				}
				if (WorkbookUtil.getMeasurementVariable(factors, treatmentVariables.get(i).getValueVariable().getTermId()) == null) {
					factors.add(treatmentVariables.get(i).getValueVariable());
				}
			}
		}
		for (MeasurementVariable var : varList) {
			var.setFactor(true);
		}

		varList.addAll(variates);

		int trialInstanceStart = environments - environmentsToAdd + 1;
		for (int i = trialInstanceStart; i <= environments; i++) {
			int trialNo = i;
			BVDesignOutput bvOutput = null;
			try {
				bvOutput = fieldbookService.runBVDesign(workbenchService, fieldbookProperties, mainDesign);
			} catch (Exception e) {
				ExpDesignUtil.LOG.error(e.getMessage(), e);
				throw new BVDesignException("experiment.design.bv.exe.error.generate.generic.error");
			}
			if (bvOutput != null && bvOutput.isSuccess()) {
				for (int counter = 0; counter < bvOutput.getBvResultList().size(); counter++) {
					String entryNo = bvOutput.getEntryValue(entryNumberIdentifier, counter);
					if (NumberUtils.isNumber(entryNo)) {
						int germplasmIndex = Integer.valueOf(entryNo) - 1;
						if (germplasmIndex >= 0 && germplasmIndex < germplasmList.size()) {
							ImportedGermplasm importedGermplasm = germplasmList.get(germplasmIndex);
							try {
								MeasurementRow row =
										ExpDesignUtil.createMeasurementRow(varList, importedGermplasm, bvOutput.getEntryMap(counter),
												treatmentFactorValues, trialVariables, trialNo, factors, entryNo);
								measurementRowList.add(row);
							} catch (MiddlewareQueryException e) {
								ExpDesignUtil.LOG.error(e.getMessage(), e);
							}

						}
					}
				}
			} else {
				throw new BVDesignException("experiment.design.generate.generic.error");
			}

		}
		return measurementRowList;
	}

	public static String cleanBVDesingKey(String key) {
		if (key != null) {
			return "_" + key.replace("-", "_");
		}
		return key;
	}
}
