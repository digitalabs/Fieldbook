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
	public List<MeasurementRow> generateDesign(final List<ImportedGermplasm> germplasmList, final ExpDesignParameterUi parameter,
			final List<MeasurementVariable> trialVariables, final List<MeasurementVariable> factors, final List<MeasurementVariable> nonTrialFactors,
			final List<MeasurementVariable> variates, final List<TreatmentVariable> treatmentVariables) throws BVDesignException {

		List<MeasurementRow> measurementRowList = new ArrayList<MeasurementRow>();
		
		final Set<Integer> entryIdsOfChecks = this.getEntryIdsOfChecks(germplasmList);
		final Set<Integer> entryIdsOfTestEntries = this.getEntryIdsOfTestEntries(germplasmList);

		final Map<Integer, Integer> designExpectedEntriesMap = this.createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(germplasmList, entryIdsOfChecks, entryIdsOfTestEntries);

		final Integer numberOfBlocks = StringUtil.parseInt(parameter.getNumberOfBlocks(), null);
		final Integer numberOfControls = entryIdsOfChecks.size();
		final Integer numberOfTreatments = germplasmList.size() - numberOfControls;
		final Integer startingPlotNumber = StringUtil.parseInt(parameter.getStartingPlotNo(), null);
		final Integer startingEntryNumber = StringUtil.parseInt(parameter.getStartingEntryNo(), null);

		final int noOfExistingEnvironments = Integer.valueOf(parameter.getNoOfEnvironments());
		final int noOfEnvironmentsToBeAdded = Integer.valueOf(parameter.getNoOfEnvironmentsToAdd());

		final List<StandardVariable> requiredDesignVariables = this.getRequiredDesignVariables();
		final Map<Integer, StandardVariable> standardVariableMap = convertStandardVariableListToMap(requiredDesignVariables);

		final StandardVariable stdvarEntryNo = standardVariableMap.get(TermId.ENTRY_NO.getId());
		final StandardVariable stdvarBlock = standardVariableMap.get(TermId.BLOCK_NO.getId());
		final StandardVariable stdvarPlot = standardVariableMap.get(TermId.PLOT_NO.getId());

		final MainDesign mainDesign = experimentDesignGenerator
				.createAugmentedRandomizedBlockDesign(numberOfBlocks, numberOfTreatments, numberOfControls, startingPlotNumber, startingEntryNumber, stdvarEntryNo.getName(),
						stdvarBlock.getName(), stdvarPlot.getName());

		measurementRowList = experimentDesignGenerator
				.generateExperimentDesignMeasurements(noOfExistingEnvironments, noOfEnvironmentsToBeAdded, trialVariables, factors, nonTrialFactors,
						variates, treatmentVariables, requiredDesignVariables, germplasmList, mainDesign, stdvarEntryNo.getName(), null,
						designExpectedEntriesMap);

		return measurementRowList;
	}

	@Override
	public List<StandardVariable> getRequiredDesignVariables() {
		final List<StandardVariable> varList = new ArrayList<StandardVariable>();

		final StandardVariable stdvarEntryNo =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.ENTRY_NO.getId(), contextUtil.getCurrentProgramUUID());
		final StandardVariable stdvarBlock =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.BLOCK_NO.getId(), contextUtil.getCurrentProgramUUID());
		final StandardVariable stdvarPlot =
				this.fieldbookMiddlewareService.getStandardVariable(TermId.PLOT_NO.getId(), contextUtil.getCurrentProgramUUID());

		stdvarBlock.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
		stdvarPlot.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
		stdvarEntryNo.setPhenotypicType(PhenotypicType.GERMPLASM);

		varList.add(stdvarEntryNo);
		varList.add(stdvarBlock);
		varList.add(stdvarPlot);

		return varList;
	}

	Map<Integer, StandardVariable> convertStandardVariableListToMap(final List<StandardVariable> standardVariables) {

		final Map<Integer, StandardVariable> map = new HashMap<>();

		for (final StandardVariable stdvar : standardVariables) {
			map.put(stdvar.getId(), stdvar);
		}

		return map;

	}

	Map<Integer, Integer> createMapOfDesignExpectedEntriesToGermplasmEntriesInTrial(final List<ImportedGermplasm> importedGermplasmList, final Set<Integer> entryIdsOfChecks, final Set<Integer> entryIdsOfTestEntries) {

		/**
		 * The design engine assumes that the checks are at the end of the germplasm list that is passed to it. This might or might not be
		 * the case in the list that the user has specified for the trial. To make this simpler for the user, when processing the design
		 * file that comes back from BVDesign, the BMS will re-map the output into entry order.
		 *
		 * In a design with 52 entries (48 test entries and 4 check entries), BVDesign assumes the checks are entry numbers 49,50, 51, and
		 * 52. Since this may not be the case for the user's trial list, the BMS will sequentially map 49-52 to the four check entries in
		 * the list.
		 */

		final Map<Integer, Integer> designExpectedEntriesMap = new HashMap<>();

		// Map the last entries to the check entries in the list.
		int index = importedGermplasmList.size() - entryIdsOfChecks.size();
		for (final Integer checkEntryId : entryIdsOfChecks) {
			designExpectedEntriesMap.put(importedGermplasmList.get(index).getEntryId(), checkEntryId);
			index++;
		}
		
		// Map the top entries to the test entries in the list.
		index = 0;
		for (final Integer checkEntryId : entryIdsOfTestEntries) {
			designExpectedEntriesMap.put(importedGermplasmList.get(index).getEntryId(), checkEntryId);
			index++;
		}

		return designExpectedEntriesMap;
	}

	Set<Integer> getEntryIdsOfChecks(final List<ImportedGermplasm> importedGermplasmList) {

		final HashSet<Integer> entryIdsOfChecks = new HashSet<>();

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				entryIdsOfChecks.add(importedGermplasm.getEntryId());
			}
		}

		return entryIdsOfChecks;
	}
	
	Set<Integer> getEntryIdsOfTestEntries(final List<ImportedGermplasm> importedGermplasmList) {

		final HashSet<Integer> entryIdsOfTestEntries = new HashSet<>();

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (!importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				entryIdsOfTestEntries.add(importedGermplasm.getEntryId());
			}
		}

		return entryIdsOfTestEntries;
	}

	@Override
	public ExpDesignValidationOutput validate(final ExpDesignParameterUi expDesignParameter, final List<ImportedGermplasm> germplasmList) {
		final Locale locale = LocaleContextHolder.getLocale();
		ExpDesignValidationOutput output = new ExpDesignValidationOutput(true, "");
		try {

			experimentDesignValidator.validateAugmentedDesign(expDesignParameter, germplasmList);

		} catch (final DesignValidationException e) {
			output = new ExpDesignValidationOutput(false, e.getMessage());
		} catch (final Exception e) {
			output = new ExpDesignValidationOutput(false,
					this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}

		return output;
	}

	/**
	 * Defines if the experimental design requires breeding view licence to run
	 *
	 * @return
	 */
	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	@Override
	public List<Integer> getExperimentalDesignVariables(final ExpDesignParameterUi params) {
		return Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NBLKS.getId());
	}

	void setFieldbookMiddlewareService(final org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

}
