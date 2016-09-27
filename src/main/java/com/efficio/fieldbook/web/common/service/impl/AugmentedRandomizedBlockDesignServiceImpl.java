package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.AugmentedRandomizedBlockDesignService;
import com.efficio.fieldbook.web.common.service.ExperimentDesignService;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class AugmentedRandomizedBlockDesignServiceImpl implements AugmentedRandomizedBlockDesignService {

	private static final Logger LOG = LoggerFactory.getLogger(AugmentedRandomizedBlockDesignServiceImpl.class);

	@Resource
	public org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	protected WorkbenchService workbenchService;

	@Resource
	protected FieldbookProperties fieldbookProperties;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	public FieldbookService fieldbookService;

	@Resource
	public ContextUtil contextUtil;

	@Override
	public List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList, ExpDesignParameterUi parameter,
			List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors, List<MeasurementVariable> nonTrialFactors,
			List<MeasurementVariable> variates, List<TreatmentVariable> treatmentVariables) throws BVDesignException {

		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();

		Map<Integer, Integer> mapOfChecks = this.createMapOfChecks(germplasmList);

		String nblks = parameter.getNumberOfBlocks();
		String ncontrols = String.valueOf(mapOfChecks.size());
		int nTreatments = germplasmList.size();
		int environments = Integer.valueOf(parameter.getNoOfEnvironments());
		int environmentsToAdd = Integer.valueOf(parameter.getNoOfEnvironmentsToAdd());

		try {

			Map<Integer, StandardVariable> standardVariableMap = retrieveRequiredStandardVariablesMap();

			StandardVariable stdvarEntryNo = standardVariableMap.get(TermId.ENTRY_NO.getId());
			StandardVariable stdvarBlock = standardVariableMap.get(TermId.BLOCK_NO.getId());
			StandardVariable stdvarPlot = standardVariableMap.get(TermId.PLOT_NO.getId());

			Integer plotNo = StringUtil.parseInt(parameter.getStartingPlotNo(), null);
			Integer entryNo = StringUtil.parseInt(parameter.getStartingEntryNo(), null);

			MainDesign mainDesign = ExpDesignUtil
					.createAugmentedRandomizedBlockDesign(nblks, Integer.toString(nTreatments), ncontrols, stdvarEntryNo.getName(),
							stdvarBlock.getName(), stdvarPlot.getName(), String.valueOf(plotNo));

			measurementRowList = ExpDesignUtil
					.generateExpDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors, variates,
							treatmentVariables, new ArrayList(standardVariableMap.values()), germplasmList, mainDesign,
							this.workbenchService, this.fieldbookProperties, stdvarEntryNo.getName(), null, this.fieldbookService);

		} catch (BVDesignException e) {
			throw e;
		} catch (Exception e) {
			AugmentedRandomizedBlockDesignServiceImpl.LOG.error(e.getMessage(), e);
		}
		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredVariable() {
		List<StandardVariable> varList = new ArrayList<StandardVariable>();

		//StandardVariable stdvarRep =
		//		this.fieldbookMiddlewareService.getStandardVariable(TermId.NBLKS.getId(), contextUtil.getCurrentProgramUUID());
		StandardVariable stdvarEntryNo =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), contextUtil.getCurrentProgramUUID());
		StandardVariable stdvarBlock =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), contextUtil.getCurrentProgramUUID());
		StandardVariable stdvarPlot =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), contextUtil.getCurrentProgramUUID());

		//stdvarRep.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
		stdvarBlock.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
		stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
		stdvarEntryNo.setPhenotypicType(PhenotypicType.GERMPLASM);

		varList.add(stdvarEntryNo);
		//varList.add(stdvarRep);
		varList.add(stdvarBlock);
		varList.add(stdvarPlot);

		return varList;
	}

	Map<Integer, StandardVariable> retrieveRequiredStandardVariablesMap() {

		Map<Integer, StandardVariable> map = new HashMap<>();

		for (StandardVariable stdvar : this.getRequiredVariable()) {
			map.put(stdvar.getId(), stdvar);
		}

		return map;

	}

	Map<Integer, Integer> createMapOfChecks(final List<ImportedGermplasm> importedGermplasmList) {

		/**
		 * The design engine assumes that the checks are at the end of the germplasm list that is passed to it. This might or might not be
		 * the case in the list that the user has specified for the trial. To make this simpler for the user, when processing the design
		 * file that comes back from BVDesign, the BMS will re-map the output into entry order.
		 * 
		 * In a design with 52 entries (48 test entries and 4 check entries), BVDesign assumes the checks are entry numbers 49,50, 51, and
		 * 52. Since this may not be the case for the user's trial list, the BMS will sequentially map 49-52 to the four check entries in
		 * the list.
		 */

		Map<Integer, Integer> mapOfChecks = new HashMap<>();

		Set<Integer> entryIdsOfChecks = this.getEntryIdsOfChecks(importedGermplasmList);

		// Map the last entries to the check entries in the list.
		int index = importedGermplasmList.size() - entryIdsOfChecks.size();
		for (Integer checkEntryId : entryIdsOfChecks) {
			mapOfChecks.put(importedGermplasmList.get(index).getEntryId(), checkEntryId);
			index++;
		}

		return mapOfChecks;
	}

	Set<Integer> getEntryIdsOfChecks(final List<ImportedGermplasm> importedGermplasmList) {

		HashSet<Integer> entryIdsOfChecks = new HashSet<>();

		for (ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				entryIdsOfChecks.add(importedGermplasm.getEntryId());
			}
		}

		return entryIdsOfChecks;
	}

	@Override
	public ExpDesignValidationOutput validate(ExpDesignParameterUi expDesignParameter, List<ImportedGermplasm> germplasmList) {
		Locale locale = LocaleContextHolder.getLocale();
		ExpDesignValidationOutput output = new ExpDesignValidationOutput(true, "");
		try {
			if (expDesignParameter != null && germplasmList != null) {
				if (!NumberUtils.isNumber(expDesignParameter.getNumberOfBlocks())) {
					output = new ExpDesignValidationOutput(false, "Number of blocks should be a number");
					return output;
				} else if (expDesignParameter.getStartingPlotNo() != null && !NumberUtils
						.isNumber(expDesignParameter.getStartingPlotNo())) {
					output = new ExpDesignValidationOutput(false,
							this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
					return output;
				} else if (expDesignParameter.getStartingEntryNo() != null && !NumberUtils
						.isNumber(expDesignParameter.getStartingEntryNo())) {
					output = new ExpDesignValidationOutput(false,
							this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
					return output;
				} else {

					int treatmentSize = germplasmList.size();

					final Integer entryNumber = StringUtil.parseInt(expDesignParameter.getStartingEntryNo(), null);
					final Integer plotNumber = StringUtil.parseInt(expDesignParameter.getStartingPlotNo(), null);

					if (Objects.equals(entryNumber, 0)) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
					} else if (Objects.equals(plotNumber, 0)) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
					} else if (entryNumber != null && (treatmentSize + entryNumber) > ExperimentDesignService.MAX_STARTING_ENTRY_PLOT_NO) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
					} else if (entryNumber != null && plotNumber != null && ((treatmentSize + plotNumber)
							> ExperimentDesignService.MAX_STARTING_ENTRY_PLOT_NO)) {
						output = new ExpDesignValidationOutput(false,
								this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
					}
				}
			}
		} catch (Exception e) {
			output = new ExpDesignValidationOutput(false,
					this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}

		return output;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(ExpDesignParameterUi params) {
		if (params.getUseLatenized() != null && params.getUseLatenized()) {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
					TermId.NO_OF_CBLKS_LATINIZE.getId(), TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_REPS_IN_COLS.getId());
		} else {
			return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId());
		}
	}

	void setFieldbookMiddlewareService(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
