/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.util.WorkbookUtil;

/**
 * The Class MeasurementsGeneratorServiceImpl.
 */
@Deprecated
@Service
@Transactional
public class MeasurementsGeneratorServiceImpl implements MeasurementsGeneratorService {

	private static final Logger LOG = LoggerFactory.getLogger(MeasurementsGeneratorServiceImpl.class);

	/** The fieldbook middleware service. */
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService
	 * #generateRealMeasurementRows(com.efficio.fieldbook.web.nursery .bean.UserSelection)
	 */
	@Override
	public List<MeasurementRow> generateRealMeasurementRows(final UserSelection userSelection) throws MiddlewareQueryException {
		final long start = System.currentTimeMillis();
		final List<MeasurementRow> measurementRows = new ArrayList<>();
		final Map<String, Integer> standardVariableMap = new HashMap<>();
		int plotNo;

		final List<ExperimentalDesignInfo> designInfos = this.getExperimentalDesignInfo(userSelection.getTrialEnvironmentValues());

		final MeasurementData[][] treatmentFactorPermutations =
				this.generateTreatmentFactorPermutations(userSelection.getWorkbook().getTreatmentFactors(), standardVariableMap);

		for (final ExperimentalDesignInfo designInfo : designInfos) {

			final int trialNo = designInfo.getTrialNumber();
			plotNo = userSelection.getStartingPlotNo();

			for (int repNo = 1; repNo <= designInfo.getNumberOfReps(); repNo++) {

				for (int blockNo = 1; blockNo <= designInfo.getBlocksPerRep(); blockNo++) {

					for (final ImportedGermplasm germplasm : userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList()
							.getImportedGermplasms()) {

						final List<MeasurementRow> measurementRow =
								this.createMeasurementRows(userSelection, trialNo, repNo, blockNo, germplasm, germplasm.getEntryId(),
										plotNo++, standardVariableMap, treatmentFactorPermutations);
						measurementRows.addAll(measurementRow);
					}
				}
			}
		}
		MeasurementsGeneratorServiceImpl.LOG.info("generateRealMeasurementRows Time duration: " + (System.currentTimeMillis() - start) / 1000);
		return measurementRows;
	}

	private List<MeasurementRow> createMeasurementRows(final UserSelection userSelection, final int trialNo, final int repNo, final int blockNo,
                                                       final ImportedGermplasm germplasm, final int entryNo, final int plotNo, final Map<String, Integer> standardVariableMap,
                                                       final MeasurementData[][] treatmentFactorPermutations) throws MiddlewareQueryException {

		final List<MeasurementRow> measurementRows = new ArrayList<>();

		int count = 1;
		if (treatmentFactorPermutations != null && treatmentFactorPermutations.length > 0) {
			count = treatmentFactorPermutations.length;
		}

		for (int i = 0; i < count; i++) {

			final MeasurementRow measurementRow = new MeasurementRow();
			final List<MeasurementData> dataList = new ArrayList<>();

			if (userSelection.isTrial()) {
				this.createTrialInstanceDataList(dataList, userSelection, trialNo);
			}

			this.createFactorDataList(dataList, userSelection, repNo, blockNo, germplasm, entryNo, plotNo, standardVariableMap);

			if (treatmentFactorPermutations != null && treatmentFactorPermutations.length > 0) {
				for (final MeasurementData treatmentFactor : treatmentFactorPermutations[i]) {
					dataList.add(treatmentFactor);
				}
			}

			this.createVariateDataList(dataList, userSelection);

			measurementRow.setDataList(dataList);
			measurementRows.add(measurementRow);
		}

		return measurementRows;
	}

	private void createTrialInstanceDataList(final List<MeasurementData> dataList, final UserSelection userSelection, final int trialNo) {
		final MeasurementVariable trialInstanceVar =
				WorkbookUtil.getMeasurementVariable(userSelection.getWorkbook().getTrialVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId());
		final MeasurementData measurementData =
				new MeasurementData(trialInstanceVar.getName(), Integer.toString(trialNo), false, trialInstanceVar.getDataType(),
						trialInstanceVar);
		dataList.add(measurementData);
	}

	void createFactorDataList(final List<MeasurementData> dataList, final UserSelection userSelection, final int repNo, final int blockNo,
                              final ImportedGermplasm germplasm, final int entryNo, final int plotNo, final Map<String, Integer> standardVariableMap) throws MiddlewareQueryException {

		for (final MeasurementVariable var : userSelection.getWorkbook().getNonTrialFactors()) {

			// do not include treatment factors
			if (var.getTreatmentLabel() == null || "".equals(var.getTreatmentLabel())) {
				final MeasurementData measurementData;

				Integer termId = var.getTermId();
				if (termId == 0) {
					final String key =
							var.getProperty() + ":" + var.getScale() + ":" + var.getMethod() + ":"
									+ PhenotypicType.getPhenotypicTypeForLabel(var.getLabel());
					if (standardVariableMap.get(key) == null) {
						termId =
								this.fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(),
										var.getScale(), var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
						standardVariableMap.put(key, termId);
					} else {
						termId = standardVariableMap.get(key);

					}
				}

				var.setFactor(true);

				if (termId == null) {
					// we default if null, but should not happen
					measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
					var.setFactor(false);
					measurementData.setEditable(true);
				} else {

					if (termId == TermId.ENTRY_NO.getId()) {
						measurementData = new MeasurementData(var.getName(), Integer.toString(entryNo), false, var.getDataType(), var);
					} else if (termId == TermId.SOURCE.getId() || termId == TermId.GERMPLASM_SOURCE.getId()) {
						measurementData =
								new MeasurementData(var.getName(), germplasm.getSource() != null ? germplasm.getSource() : "", false,
										var.getDataType(), var);
					} else if (termId == TermId.GROUPGID.getId()) {
						measurementData =
								new MeasurementData(var.getName(), germplasm.getGroupId() != null ? germplasm.getGroupId().toString() : "", false,
										var.getDataType(), var);
					} else if (termId == TermId.STOCKID.getId()) {
						measurementData =
								new MeasurementData(var.getName(), germplasm.getStockIDs() != null ? germplasm.getStockIDs() : "", false,
										var.getDataType(), var);
					} else if (termId == TermId.CROSS.getId()) {
						measurementData = new MeasurementData(var.getName(), germplasm.getCross(), false, var.getDataType(), var);
					} else if (termId == TermId.DESIG.getId()) {
						measurementData = new MeasurementData(var.getName(), germplasm.getDesig(), false, var.getDataType(), var);
					} else if (termId == TermId.GID.getId()) {
						measurementData = new MeasurementData(var.getName(), germplasm.getGid(), false, var.getDataType(), var);
					} else if (termId == TermId.ENTRY_CODE.getId()) {
						measurementData = new MeasurementData(var.getName(), germplasm.getEntryCode(), false, var.getDataType(), var);
					} else if (termId == TermId.PLOT_NO.getId()) {
						measurementData = new MeasurementData(var.getName(), Integer.toString(plotNo), false, var.getDataType(), var);
					} else if (termId == TermId.ENTRY_TYPE.getId()) {
                        // if germplasm has defined check value, use that
                        if (germplasm.getEntryTypeCategoricalID() != null) {
                            measurementData =
                                    new MeasurementData(var.getName(), germplasm.getEntryTypeName(), false, var.getDataType(),
                                            germplasm.getEntryTypeCategoricalID(), var);
                        } else {
                            // if germplasm does not have a defined check value, but ENTRY_TYPE factor is needed, we provide the current system default
                            measurementData =
                                    new MeasurementData(var.getName(), SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue(), false, var.getDataType(),
                                            SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId(), var);
                        }


					} else if (termId == TermId.REP_NO.getId()) {
						measurementData = new MeasurementData(var.getName(), Integer.toString(repNo), false, var.getDataType(), var);
					} else if (termId == TermId.BLOCK_NO.getId()) {
						measurementData = new MeasurementData(var.getName(), Integer.toString(blockNo), false, var.getDataType(), var);

					} else {
						// meaning non factor
						measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
						var.setFactor(false);
					}
				}

				dataList.add(measurementData);
			}
		}
	}

	private void createVariateDataList(final List<MeasurementData> dataList, final UserSelection userSelection) {
		for (final MeasurementVariable var : userSelection.getWorkbook().getVariates()) {
			final MeasurementData measurementData = new MeasurementData(var.getName(), "", true, var.getDataType(), var);
			var.setFactor(false);

			dataList.add(measurementData);
		}
	}

	private List<ExperimentalDesignInfo> getExperimentalDesignInfo(final List<List<ValueReference>> trialInfo) {
		final List<ExperimentalDesignInfo> result = new ArrayList<>();

		if (trialInfo != null && !trialInfo.isEmpty()) {
			for (final List<ValueReference> list : trialInfo) {
				final ExperimentalDesignInfo info = new ExperimentalDesignInfo();
				for (final ValueReference ref : list) {
					if (ref.getName() != null && NumberUtils.isNumber(ref.getName())) {
						final Integer value = Double.valueOf(ref.getName()).intValue();
						if (ref.getId().equals(TermId.TRIAL_INSTANCE_FACTOR.getId())) {
							info.setTrialNumber(value);
						} else if (ref.getId().equals(TermId.EXPERIMENT_DESIGN_FACTOR.getId())) {
							info.setDesign(value);
						} else if (ref.getId().equals(TermId.NUMBER_OF_REPLICATES.getId())) {
							info.setNumberOfReps(value);
						} else if (ref.getId().equals(TermId.BLOCK_SIZE.getId())) {
							info.setBlockSize(value);
						} else if (ref.getId().equals(TermId.BLOCKS_PER_REPLICATE.getId())) {
							info.setBlocksPerRep(value);
						}
					}
				}
				if (info.getNumberOfReps() == null || info.getNumberOfReps() == 0) {
					info.setNumberOfReps(1);
				}
				if (info.getTrialNumber() == null || info.getTrialNumber() == 0) {
					info.setTrialNumber(1);
				}
				if (info.getBlocksPerRep() == null || info.getBlocksPerRep() == 0) {
					info.setBlocksPerRep(1);
				}
				result.add(info);
			}
		}

		if (result.isEmpty()) {
			result.add(new ExperimentalDesignInfo(1, null, 1, null, 1));
		}

		return result;
	}

	private MeasurementData[][] generateTreatmentFactorPermutations(final List<TreatmentVariable> treatmentVariables,
                                                                    final Map<String, Integer> standardVariableMap) throws MiddlewareQueryException {

		MeasurementData[][] output = null;
		if (treatmentVariables != null && !treatmentVariables.isEmpty()) {
			final List<List<TreatmentVariable>> lists = this.rearrangeTreatmentVariables(treatmentVariables);
			final int totalPermutations = this.getTotalPermutations(lists);
			output = new MeasurementData[totalPermutations][lists.size() * 2];

			int currentPermutation = 1;
			int listIndex = 0;
			for (final List<TreatmentVariable> list : lists) {
				final int size = list.size();
				currentPermutation *= size;
				final int reps = totalPermutations / currentPermutation;

				for (int i = 0; i < currentPermutation; i++) {
					for (int j = 0; j < reps; j++) {
						final TreatmentVariable factor = list.get(i % size);
						final MeasurementData levelData = this.createMeasurementData(factor.getLevelVariable(), standardVariableMap);
						final MeasurementData valueData = this.createMeasurementData(factor.getValueVariable(), standardVariableMap);
						output[reps * i + j][listIndex * 2] = levelData;
						output[reps * i + j][listIndex * 2 + 1] = valueData;
					}
				}
				listIndex++;
			}
		}

		return output;
	}

	private List<List<TreatmentVariable>> rearrangeTreatmentVariables(final List<TreatmentVariable> treatmentVariables) {
		final List<List<TreatmentVariable>> groupedFactors = new ArrayList<>();
		Integer levelFactorId;
		final Map<Integer, List<TreatmentVariable>> map = new LinkedHashMap<>();
		for (final TreatmentVariable treatmentFactor : treatmentVariables) {
			levelFactorId = treatmentFactor.getLevelVariable().getTermId();
			List<TreatmentVariable> treatments = map.get(levelFactorId);
			if (treatments == null) {
				treatments = new ArrayList<>();
				map.put(levelFactorId, treatments);
			}
			treatments.add(treatmentFactor);
		}
		final Set<Integer> keys = map.keySet();
		for (final Iterator<Integer> iterator = keys.iterator(); iterator.hasNext();) {
			groupedFactors.add(map.get(iterator.next()));
		}
		return groupedFactors;
	}

	private int getTotalPermutations(final List<List<TreatmentVariable>> lists) {
		int totalPermutations = 1;
		for (final List<TreatmentVariable> list : lists) {
			totalPermutations *= list.size();
		}
		return totalPermutations;
	}

	private MeasurementData createMeasurementData(final MeasurementVariable variable, final Map<String, Integer> standardVariableMap)
			throws MiddlewareQueryException {

		Integer termId = variable.getTermId();
		if (termId == 0) {
			termId = this.getTermId(variable, standardVariableMap);
		}

		return new MeasurementData(variable.getName(), variable.getValue(), false, variable.getDataType(), variable);
	}

	private Integer getTermId(final MeasurementVariable var, final Map<String, Integer> standardVariableMap) throws MiddlewareQueryException {

		final Integer termId;
		final String key =
				var.getProperty() + ":" + var.getScale() + ":" + var.getMethod() + ":"
						+ PhenotypicType.getPhenotypicTypeForLabel(var.getLabel());
		if (standardVariableMap.get(key) == null) {
			termId =
					this.fieldbookMiddlewareService.getStandardVariableIdByPropertyScaleMethodRole(var.getProperty(), var.getScale(),
							var.getMethod(), PhenotypicType.getPhenotypicTypeForLabel(var.getLabel()));
			standardVariableMap.put(key, termId);
		} else {
			termId = standardVariableMap.get(key);

		}

		return termId;
	}

	class ExperimentalDesignInfo {

		private Integer trialNumber;
		private Integer design;
		private Integer numberOfReps;
		private Integer blockSize;
		private Integer blocksPerRep;

		public ExperimentalDesignInfo() {

		}

		public ExperimentalDesignInfo(final Integer trialNumber, final Integer design, final Integer numberOfReps, final Integer blockSize, final Integer blocksPerRep) {
			this.trialNumber = trialNumber;
			this.design = design;
			this.numberOfReps = numberOfReps;
			this.blockSize = blockSize;
			this.blocksPerRep = blocksPerRep;
		}

		public Integer getTrialNumber() {
			return this.trialNumber;
		}

		public void setTrialNumber(final Integer trialNumber) {
			this.trialNumber = trialNumber;
		}

		public Integer getDesign() {
			return this.design;
		}

		public void setDesign(final Integer design) {
			this.design = design;
		}

		public Integer getNumberOfReps() {
			return this.numberOfReps;
		}

		public void setNumberOfReps(final Integer numberOfReps) {
			this.numberOfReps = numberOfReps;
		}

		public Integer getBlockSize() {
			return this.blockSize;
		}

		public void setBlockSize(final Integer blockSize) {
			this.blockSize = blockSize;
		}

		public Integer getBlocksPerRep() {
			return this.blocksPerRep;
		}

		public void setBlocksPerRep(final Integer blocksPerRep) {
			this.blocksPerRep = blocksPerRep;
		}

		@Override
		public String toString() {
			return "ExperimentalDesignInfo [trialNumber=" + this.trialNumber + ", design=" + this.design + ", numberOfReps="
					+ this.numberOfReps + ", blockSize=" + this.blockSize + ", blocksPerRep=" + this.blocksPerRep + "]";
		}

	}

}
