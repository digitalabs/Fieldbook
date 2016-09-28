package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.AugmentedRandomizedBlockDesignService;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignValidator;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
	private MessageSource messageSource;

	@Resource
	public FieldbookService fieldbookService;

	@Resource
	public ContextUtil contextUtil;

	@Resource
	public ExperimentDesignGenerator experimentDesignGenerator;

	@Resource
	public ExperimentDesignValidator experimentDesignValidator;

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

			List<StandardVariable> requiredDesignVariables = this.getRequiredVariable();
			Map<Integer, StandardVariable> standardVariableMap = convertStandardVariableListToMap(requiredDesignVariables);

			StandardVariable stdvarEntryNo = standardVariableMap.get(TermId.ENTRY_NO.getId());
			StandardVariable stdvarBlock = standardVariableMap.get(TermId.BLOCK_NO.getId());
			StandardVariable stdvarPlot = standardVariableMap.get(TermId.PLOT_NO.getId());

			Integer plotNo = StringUtil.parseInt(parameter.getStartingPlotNo(), null);

			MainDesign mainDesign = experimentDesignGenerator
					.createAugmentedRandomizedBlockDesign(nblks, Integer.toString(nTreatments), ncontrols, stdvarEntryNo.getName(),
							stdvarBlock.getName(), stdvarPlot.getName(), String.valueOf(plotNo));

			measurementRowList = experimentDesignGenerator
					.generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors, variates,
							treatmentVariables, requiredDesignVariables, germplasmList, mainDesign,
							stdvarEntryNo.getName(), null, mapOfChecks);

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

		StandardVariable stdvarEntryNo =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), contextUtil.getCurrentProgramUUID());
		StandardVariable stdvarBlock =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), contextUtil.getCurrentProgramUUID());
		StandardVariable stdvarPlot =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), contextUtil.getCurrentProgramUUID());


		stdvarBlock.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
		stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
		stdvarEntryNo.setPhenotypicType(PhenotypicType.GERMPLASM);

		varList.add(stdvarEntryNo);
		varList.add(stdvarBlock);
		varList.add(stdvarPlot);

		return varList;
	}

	Map<Integer, StandardVariable> convertStandardVariableListToMap(List<StandardVariable> standardVariables) {

		Map<Integer, StandardVariable> map = new HashMap<>();

		for (StandardVariable stdvar : standardVariables) {
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

			experimentDesignValidator.validateAugmentedDesign(expDesignParameter, germplasmList);

		} catch (DesignValidationException e) {
			output = new ExpDesignValidationOutput(false,
					e.getMessage());
		} catch (Exception e) {
			output = new ExpDesignValidationOutput(false,
					this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}

		return output;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(ExpDesignParameterUi params) {
		return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NBLKS.getId());
	}

	void setFieldbookMiddlewareService(org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
