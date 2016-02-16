
package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.service.GermplasmOriginGenerationService;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.TimerWatch;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.AdvanceGermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.naming.rules.naming.EnforceUniqueNameRule;
import com.efficio.fieldbook.web.naming.rules.naming.NamingRuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.GermplasmOriginParameterBuilder;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.util.AppConstants;

@Service
@Transactional
public class NamingConventionServiceImpl implements NamingConventionService {

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private RulesService rulesService;

	@Resource
	private GermplasmDataManager germplasmDataManger;

	@Resource
	private AdvancingSourceListFactory advancingSourceListFactory;

	@Resource
	private ProcessCodeService processCodeService;

	@Resource
	private RuleFactory ruleFactory;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private GermplasmOriginGenerationService germplasmOriginGenerationService;

	@Resource
	private GermplasmOriginParameterBuilder germplasmOriginParameterBuilder;

	@Override
	public AdvanceResult advanceNursery(final AdvancingNursery info, final Workbook workbook) throws RuleException,
			MiddlewareQueryException, FieldbookException {

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

		final AdvanceResult result = new AdvanceResult();
		result.setAdvanceList(importedGermplasmList);
		result.setChangeDetails(changeDetails);

		return result;
	}

	private AdvancingSourceList createAdvancingSourceList(final AdvancingNursery advanceInfo, Workbook workbook,
			final Map<Integer, Method> breedingMethodMap, final Map<String, Method> breedingMethodCodeMap) throws MiddlewareQueryException, FieldbookException {

		final int nurseryId = advanceInfo.getStudy().getId();
		if (workbook == null) {
			workbook = this.fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
		}
		final Study nursery = advanceInfo.getStudy();

		return this.advancingSourceListFactory.createAdvancingSourceList(workbook, advanceInfo, nursery, breedingMethodMap, breedingMethodCodeMap);
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

	private void assignGermplasmAttributes(final ImportedGermplasm germplasm, final int sourceGid, final int sourceGnpgs,
			final int sourceGpid1, final int sourceGpid2, final Method sourceMethod, final Method breedingMethod) {

		if (sourceMethod != null && sourceMethod.getMtype() != null
				&& AppConstants.METHOD_TYPE_GEN.getString().equals(sourceMethod.getMtype()) || sourceGnpgs < 0 && sourceGpid1 == 0
				&& sourceGpid2 == 0) {

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
		final GermplasmOriginGenerationParameters parameters =
				this.germplasmOriginParameterBuilder.build(workbook, source.getPlotNumber(), selectionNumberToApply);
		final String seedSourceOriginString = this.germplasmOriginGenerationService.generateOriginString(parameters);
		final ImportedGermplasm germplasm =
				new ImportedGermplasm(index, newGermplasmName, null /* gid */
						, source.getGermplasm().getCross(), seedSourceOriginString,
						FieldbookUtil.generateEntryCode(index), null /* check */
						, breedingMethod.getMid());
		
		// assign parentage etc for the new Germplasm
		this.assignGermplasmAttributes(germplasm, Integer.valueOf(source.getGermplasm().getGid()), source.getGermplasm().getGnpgs(), source
				.getGermplasm().getGpid1(), source.getGermplasm().getGpid2(), source.getSourceMethod(), breedingMethod);
		
		// assign grouping based on parentage

		// check to see if a group ID (MGID) exists in the parent for this Germplasm, and set
		// newly created germplasm if part of a group ( > 0 )
		if(source.getGermplasm().getMgid() != null && source.getGermplasm().getMgid() > 0){
			germplasm.setMgid(source.getGermplasm().getMgid());
		}

		this.assignNames(germplasm, source);

		list.add(germplasm);
	}

	protected void assignNames(final ImportedGermplasm germplasm, final AdvancingSource source) {
		final List<Name> names = new ArrayList<Name>();

		final Name name = new Name();
		name.setGermplasmId(Integer.valueOf(source.getGermplasm().getGid()));
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
		final TimerWatch timer = new TimerWatch("advance");

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
		timer.stop();
		return list;
	}

	protected RuleExecutionContext setupNamingRuleExecutionContext(final AdvancingSource row, final boolean checkForDuplicateName) {
		List<String> sequenceList = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace("naming"));

		if (checkForDuplicateName) {
			// new array list is required since list generated from asList method does not support adding of more elements
			sequenceList = new ArrayList<>(sequenceList);
			sequenceList.add(EnforceUniqueNameRule.KEY);
		}

		final NamingRuleExecutionContext context =
				new NamingRuleExecutionContext(sequenceList, this.processCodeService, row, this.germplasmDataManger,
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

	void setGermplasmDataManger(GermplasmDataManager germplasmDataManger) {
		this.germplasmDataManger = germplasmDataManger;
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

	void setGermplasmOriginGenerationService(GermplasmOriginGenerationService germplasmOriginGenerationService) {
		this.germplasmOriginGenerationService = germplasmOriginGenerationService;
	}

	void setGermplasmOriginParameterBuilder(GermplasmOriginParameterBuilder germplasmOriginParameterBuilder) {
		this.germplasmOriginParameterBuilder = germplasmOriginParameterBuilder;
	}

}
