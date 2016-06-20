
package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.RulesNotConfiguredException;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.TimerWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.AdvanceGermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.naming.rules.naming.EnforceUniqueNameRule;
import com.efficio.fieldbook.web.naming.rules.naming.NamingRuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.util.AppConstants;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Service
@Transactional
public class NamingConventionServiceImpl implements NamingConventionService {

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private RulesService rulesService;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private AdvancingSourceListFactory advancingSourceListFactory;

	@Resource
	private ProcessCodeService processCodeService;

	@Resource
	private RuleFactory ruleFactory;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private SeedSourceGenerator seedSourceGenerator;
	
	public static final Logger LOG = LoggerFactory.getLogger(NamingConventionServiceImpl.class);
			

	@Override
	public AdvanceResult advanceNursery(final AdvancingNursery info, final Workbook workbook) throws RuleException,
			MiddlewareQueryException, FieldbookException {

		final AdvanceResult result = new AdvanceResult();
		
		Monitor monitor = MonitorFactory.start("AdvanceNursery:com.efficio.fieldbook.web.naming.impl.NamingConventionServiceImpl.advanceNursery");

		try{
		final Map<Integer, Method> breedingMethodMap = new HashMap<>();
		final Map<String, Method> breedingMethodCodeMap = new HashMap<>();
		final List<Method> methodList = this.fieldbookMiddlewareService.getAllBreedingMethods(false);

		for (final Method method : methodList) {
			breedingMethodMap.put(method.getMid(), method);
			breedingMethodCodeMap.put(method.getMcode(), method);
		}

		final AdvancingSourceList list = this.createAdvancingSourceList(info, workbook, breedingMethodMap, breedingMethodCodeMap);
		this.updatePlantsSelectedIfNecessary(list, info);
		final List<ImportedGermplasm> importedGermplasmList = this.generateGermplasmList(list, info, workbook);

		final List<AdvanceGermplasmChangeDetail> changeDetails = new ArrayList<>();
		for (final AdvancingSource source : list.getRows()) {
			if (source.getChangeDetail() != null) {
				changeDetails.add(source.getChangeDetail());
			}
		}

		result.setAdvanceList(importedGermplasmList);
		result.setChangeDetails(changeDetails);
		} finally {
			LOG.info("Exiting NamingConventionService: " + monitor.stop());
		}

		return result;
	}

	private AdvancingSourceList createAdvancingSourceList(final AdvancingNursery advanceInfo, Workbook workbook,
			final Map<Integer, Method> breedingMethodMap, final Map<String, Method> breedingMethodCodeMap) throws FieldbookException {
		Monitor monitor = MonitorFactory.start("AdvanceNursery:com.efficio.fieldbook.web.naming.impl.NamingConventionServiceImpl.createAdvancingSourceList");
		final Study study = advanceInfo.getStudy();
		try {
  		if (workbook == null) {
  			if (study.getType().equals("N")) {
  				workbook = this.fieldbookMiddlewareService.getNurseryDataSet(study.getId());
  			} else if (study.getType().equals("T")) {
  				workbook = this.fieldbookMiddlewareService.getTrialDataSet(study.getId());
  			}
  		} 
		} finally {
			LOG.info("Exit createAdvancingSourceList : " + monitor.stop());
		}
		return this.advancingSourceListFactory.createAdvancingSourceList(workbook, advanceInfo, study, breedingMethodMap, breedingMethodCodeMap);
	}

	private void updatePlantsSelectedIfNecessary(final AdvancingSourceList list, final AdvancingNursery info) {
		boolean lineChoiceSame = info.getLineChoice() != null && "1".equals(info.getLineChoice());
		final boolean allPlotsChoice = info.getAllPlotsChoice() != null && "1".equals(info.getAllPlotsChoice());
		int plantsSelected = 0;
		if (info.getLineSelected() != null && NumberUtils.isNumber(info.getLineSelected())) {
			plantsSelected = Integer.valueOf(info.getLineSelected());
		} else {
			lineChoiceSame = false;
		}
		if (list != null && list.getRows() != null && !list.getRows().isEmpty() && (lineChoiceSame && plantsSelected > 0 || allPlotsChoice)) {
			for (final AdvancingSource row : list.getRows()) {
				if (!row.isBulk() && lineChoiceSame) {
					row.setPlantsSelected(plantsSelected);
				} else if (row.isBulk() && allPlotsChoice) {
					// set it to 1, it does not matter since it's bulked
					row.setPlantsSelected(1);
				}
			}
		}
	}

	private void assignGermplasmAttributes(final ImportedGermplasm germplasm, final Integer sourceGid, final Integer sourceGnpgs,
			final Integer sourceGpid1, final Integer sourceGpid2, final Method sourceMethod, final Method breedingMethod) {

		if ((sourceMethod != null && sourceMethod.getMtype() != null
				&& AppConstants.METHOD_TYPE_GEN.getString().equals(sourceMethod.getMtype())) || sourceGnpgs < 0 &&
				(sourceGpid1 != null && sourceGpid1.equals(0)) && (sourceGpid2 != null && sourceGpid2.equals(0))) {

			germplasm.setGpid1(sourceGid);
		} else {
			germplasm.setGpid1(sourceGpid1);
		}

		germplasm.setGpid2(sourceGid);

		if (breedingMethod != null) {
			germplasm.setGnpgs(breedingMethod.getMprgn());
		}
	}

	protected void addImportedGermplasmToList(final List<ImportedGermplasm> list, final AdvancingSource source,
			final String newGermplasmName, final Method breedingMethod, final int index, Workbook workbook, int selectionNumber, AdvancingNursery advancingParameters) {
		
		String selectionNumberToApply = null;
		boolean allPlotsSelected = "1".equals(advancingParameters.getAllPlotsChoice());
		if (source.isBulk()) {
			if (allPlotsSelected) {
				selectionNumberToApply = null;
			} else {
				selectionNumberToApply = String.valueOf(source.getPlantsSelected());
			}
		} else {
			selectionNumberToApply = String.valueOf(selectionNumber);
		}
		
		// set the seed source string for the new Germplasm
		final String seedSource =
				this.seedSourceGenerator.generateSeedSource(workbook, source.getTrialInstanceNumber(), selectionNumberToApply,
						source.getPlotNumber(), workbook.getStudyName());
		final ImportedGermplasm germplasm =
				new ImportedGermplasm(index, newGermplasmName, null /* gid */
						, source.getGermplasm().getCross(), seedSource,
						FieldbookUtil.generateEntryCode(index), null /* check */
						, breedingMethod.getMid());
		
		// assign parentage etc for the new Germplasm
		final Integer sourceGid = source.getGermplasm().getGid() != null ? Integer.valueOf(source.getGermplasm().getGid()) : -1;
		final Integer gnpgs = source.getGermplasm().getGnpgs() != null ? source.getGermplasm().getGnpgs() : -1;
		this.assignGermplasmAttributes(germplasm, sourceGid, gnpgs, source.getGermplasm().getGpid1(), source.getGermplasm().getGpid2(),
				source.getSourceMethod(), breedingMethod);
		
		// assign grouping based on parentage

		// check to see if a group ID (MGID) exists in the parent for this Germplasm, and set
		// newly created germplasm if part of a group ( > 0 )
		if(source.getGermplasm().getMgid() != null && source.getGermplasm().getMgid() > 0){
			germplasm.setMgid(source.getGermplasm().getMgid());
		}

		this.assignNames(germplasm, source);

		germplasm.setTrialInstanceNumber(source.getTrialInstanceNumber());
		germplasm.setReplicationNumber(source.getReplicationNumber());
        germplasm.setPlotNumber(source.getPlotNumber());

		list.add(germplasm);
	}

	protected void assignNames(final ImportedGermplasm germplasm, final AdvancingSource source) {
		final List<Name> names = new ArrayList<Name>();

		final Name name = new Name();
		name.setTypeId(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID());
		name.setNval(germplasm.getDesig());
		name.setNstat(1);
		names.add(name);

		germplasm.setNames(names);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ImportedGermplasm> generateGermplasmList(final AdvancingSourceList rows, final AdvancingNursery advancingParameters,
			Workbook workbook) throws RuleException {

		final List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
		int index = 1;
		Monitor monitor = MonitorFactory.start("AdvanceNursery:com.efficio.fieldbook.web.naming.impl.NamingConventionServiceImpl.generateGermplasmList");
		try {
  		for (final AdvancingSource row : rows.getRows()) {
  			if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getBreedingMethod() != null
  					&& row.getPlantsSelected() > 0 && row.getBreedingMethod().isBulkingMethod() != null) {
  
  				final List<String> names;
  				final RuleExecutionContext namingExecutionContext = this.setupNamingRuleExecutionContext(row, advancingParameters.isCheckAdvanceLinesUnique());
  				names = (List<String>) this.rulesService.runRules(namingExecutionContext);
  
  				// if change detail object is created due to a duplicate being encountered somewhere during processing, provide a
  				// reference index
  				if (row.getChangeDetail() != null) {
  					// index - 1 is used because Java uses 0-based referencing
  					row.getChangeDetail().setIndex(index - 1);
  				}
  
  				// One plot may result in multiple plants/ears selected depending on selection method.
  				int selectionNumber = row.getCurrentMaxSequence() + 1;
  				for (final String name : names) {
  					this.addImportedGermplasmToList(list, row, name, row.getBreedingMethod(), index++, workbook, selectionNumber, advancingParameters);
  					selectionNumber++;
  				}
  			}
  		}
		} finally {
		  LOG.info("Exiting generateGermplasmList " + monitor.stop());
		}
		return list;
	}

	@Override
	public List<ImportedCrosses> generateCrossesList(final List<ImportedCrosses> importedCrosses, final AdvancingSourceList rows,
			final AdvancingNursery advancingParameters, final Workbook workbook, final List<Integer> gids) throws RuleException {

		final List<Method> methodList = this.fieldbookMiddlewareService.getAllBreedingMethods(false);
		final Map<Integer, Method> breedingMethodMap = new HashMap<>();
		for (final Method method : methodList) {
			breedingMethodMap.put(method.getMid(), method);
		}

		final Integer breedingMethodId = Integer.valueOf(advancingParameters.getBreedingMethodId());
		final Method selectedMethod = breedingMethodMap.get(breedingMethodId);

		if (!this.germplasmDataManager.isMethodNamingConfigurationValid(breedingMethodId)) {
			throw new RulesNotConfiguredException(this.messageSource.getMessage("error.save.cross.rules.not.configured", null, "The rules"
					+ " were not configured", LocaleContextHolder.getLocale()));
		}

		int index = 0;
		final TimerWatch timer = new TimerWatch("cross");

		// FIXME previousMaxSequence is a "quick fix" solution to propagate previous max sequence to the next cross entry to process.
		// Rules engine is currently not designed to handle this (even for advancing case). Next sequence choice is managed this via user
		// interaction for advancing. There is no user interaction in case of cross list.
		int previousMaxSequence = 0;
		for (final AdvancingSource advancingSource : rows.getRows()) {
			final List<String> names;
			advancingSource.setCurrentMaxSequence(previousMaxSequence);

			advancingSource.setBreedingMethod(selectedMethod);
			//default plants selected value to 1 for list of crosses because sequence is not working if plants selected value is not set
			advancingSource.setPlantsSelected(1);

			final RuleExecutionContext namingExecutionContext = this.setupNamingRuleExecutionContext(advancingSource, advancingParameters.isCheckAdvanceLinesUnique());
			names = (List<String>) this.rulesService.runRules(namingExecutionContext);

			// Save away the current max sequence once rules have been run for this entry.
			previousMaxSequence = advancingSource.getCurrentMaxSequence() + 1;
			for (final String name : names) {
				importedCrosses.get(index++).setDesig(name);
			}
		}
		timer.stop();
		return importedCrosses;
	}

	protected RuleExecutionContext setupNamingRuleExecutionContext(final AdvancingSource row, final boolean checkForDuplicateName) {
		List<String> sequenceList = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace("naming"));

		if (checkForDuplicateName) {
			// new array list is required since list generated from asList method does not support adding of more elements
			sequenceList = new ArrayList<>(sequenceList);
			sequenceList.add(EnforceUniqueNameRule.KEY);
		}

		final NamingRuleExecutionContext context =
				new NamingRuleExecutionContext(sequenceList, this.processCodeService, row, this.germplasmDataManager,
						new ArrayList<String>());
		context.setMessageSource(this.messageSource);

		return context;
	}

	void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	void setRulesService(RulesService rulesService) {
		this.rulesService = rulesService;
	}

	void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setAdvancingSourceListFactory(AdvancingSourceListFactory advancingSourceListFactory) {
		this.advancingSourceListFactory = advancingSourceListFactory;
	}

	void setProcessCodeService(ProcessCodeService processCodeService) {
		this.processCodeService = processCodeService;
	}

	void setRuleFactory(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	void setSeedSourceGenerator(SeedSourceGenerator seedSourceGenerator) {
		this.seedSourceGenerator = seedSourceGenerator;
	}

}
