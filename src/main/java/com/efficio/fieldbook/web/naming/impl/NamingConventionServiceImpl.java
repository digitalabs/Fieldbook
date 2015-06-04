
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
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.bean.AdvanceGermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.naming.expression.RootNameExpression;
import com.efficio.fieldbook.web.naming.rules.naming.EnforceUniqueNameRule;
import com.efficio.fieldbook.web.naming.rules.naming.NamingRuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.util.AppConstants;

@Service
public class NamingConventionServiceImpl implements NamingConventionService {

	private static final Logger LOG = LoggerFactory.getLogger(NamingConventionServiceImpl.class);
	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private RulesService rulesService;

	@Resource
	private GermplasmDataManager germplasmDataManger;

	@Resource
	private AdvancingSourceListFactory factory;

	@Resource
	private ProcessCodeService processCodeService;

	@Resource
	private RuleFactory ruleFactory;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Override
	public AdvanceResult advanceNursery(AdvancingNursery info, Workbook workbook) throws MiddlewareQueryException, RuleException {

		Map<Integer, Method> breedingMethodMap = new HashMap<>();
		Map<String, Method> breedingMethodCodeMap = new HashMap<>();
		List<Method> methodList = this.fieldbookMiddlewareService.getAllBreedingMethods(false);

		for (Method method : methodList) {
			breedingMethodMap.put(method.getMid(), method);
			breedingMethodCodeMap.put(method.getMcode(), method);
		}

		AdvancingSourceList list = this.createAdvancingSourceList(info, workbook, breedingMethodMap, breedingMethodCodeMap);
		this.updatePlantsSelectedIfNecessary(list, info);
		List<ImportedGermplasm> importedGermplasmList = this.generateGermplasmList(list, info.isCheckAdvanceLinesUnique());

		List<AdvanceGermplasmChangeDetail> changeDetails = new ArrayList<>();
		for (AdvancingSource source : list.getRows()) {
			if (source.getChangeDetail() != null) {
				changeDetails.add(source.getChangeDetail());
			}
		}

		AdvanceResult result = new AdvanceResult();
		result.setAdvanceList(importedGermplasmList);
		result.setChangeDetails(changeDetails);

		return result;
	}

	private AdvancingSourceList createAdvancingSourceList(AdvancingNursery advanceInfo, Workbook workbook,
			Map<Integer, Method> breedingMethodMap, Map<String, Method> breedingMethodCodeMap) throws MiddlewareQueryException {

		int nurseryId = advanceInfo.getStudy().getId();
		if (workbook == null) {
			workbook = this.fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
		}
		Study nursery = advanceInfo.getStudy();

		return this.factory.create(workbook, advanceInfo, nursery, breedingMethodMap, breedingMethodCodeMap);
	}

	private void updatePlantsSelectedIfNecessary(AdvancingSourceList list, AdvancingNursery info) {
		boolean lineChoiceSame = info.getLineChoice() != null && "1".equals(info.getLineChoice());
		boolean allPlotsChoice = info.getAllPlotsChoice() != null && "1".equals(info.getAllPlotsChoice());
		int plantsSelected = 0;
		if (info.getLineSelected() != null && NumberUtils.isNumber(info.getLineSelected())) {
			plantsSelected = Integer.valueOf(info.getLineSelected());
		} else {
			lineChoiceSame = false;
		}
		if (list != null && list.getRows() != null && !list.getRows().isEmpty() && (lineChoiceSame && plantsSelected > 0 || allPlotsChoice)) {
			for (AdvancingSource row : list.getRows()) {
				if (!row.isBulk() && lineChoiceSame) {
					row.setPlantsSelected(plantsSelected);
				} else if (row.isBulk() && allPlotsChoice) {
					// set it to 1, it does not matter since it's bulked
					row.setPlantsSelected(1);
				}
			}
		}
	}

	private void assignGermplasmAttributes(ImportedGermplasm germplasm, int sourceGid, int sourceGnpgs, int sourceGpid1, int sourceGpid2,
			Method sourceMethod, Method breedingMethod) {

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

	protected void addImportedGermplasmToList(List<ImportedGermplasm> list, AdvancingSource source, String newGermplasmName,
			Method breedingMethod, int index, String nurseryName) throws MiddlewareQueryException {
		// GCP-7652 use the entry number of the originial : index
		ImportedGermplasm germplasm =
				new ImportedGermplasm(index, newGermplasmName, null /* gid */
				, source.getGermplasm().getCross(), nurseryName + ":" + source.getGermplasm().getEntryId(),
						FieldbookUtil.generateEntryCode(index), null /* check */
						, breedingMethod.getMid());

		this.assignGermplasmAttributes(germplasm, Integer.valueOf(source.getGermplasm().getGid()), source.getGermplasm().getGnpgs(), source
				.getGermplasm().getGpid1(), source.getGermplasm().getGpid2(), source.getSourceMethod(), breedingMethod);

		this.assignNames(germplasm, source);

		list.add(germplasm);
	}

	protected void assignNames(ImportedGermplasm germplasm, AdvancingSource source) {
		List<Name> names = new ArrayList<Name>();

		Name name = new Name();
		name.setGermplasmId(Integer.valueOf(source.getGermplasm().getGid()));
		name.setTypeId(source.getRootNameType());

		name.setNval(germplasm.getDesig());
		name.setNstat(1);
		names.add(name);

		germplasm.setNames(names);
	}

	@Override
	public List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows, boolean isCheckForDuplicateName)
			throws MiddlewareQueryException, RuleException {
		List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
		int index = 1;
		TimerWatch timer = new TimerWatch("advance");

		for (AdvancingSource row : rows.getRows()) {
			if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getBreedingMethod() != null
					&& row.getPlantsSelected() > 0 && row.getBreedingMethod().isBulkingMethod() != null) {

				List<String> names;
				try {
					RuleExecutionContext namingExecutionContext = this.setupNamingRuleExecutionContext(row, isCheckForDuplicateName);
					names = (List<String>) this.rulesService.runRules(namingExecutionContext);

					// if change detail object is created due to a duplicate being encountered somewhere during processing, provide a
					// reference index
					if (row.getChangeDetail() != null) {
						// index - 1 is used because Java uses 0-based referencing
						row.getChangeDetail().setIndex(index - 1);
					}

					for (String name : names) {
						this.addImportedGermplasmToList(list, row, name, row.getBreedingMethod(), index++, row.getNurseryName());
					}

				} catch (RuleException e) {
					NamingConventionServiceImpl.LOG.error(e.getMessage(), e);
				}
			}
		}

		timer.stop();
		return list;
	}

	protected RuleExecutionContext setupNamingRuleExecutionContext(AdvancingSource row, boolean checkForDuplicateName) {
		List<String> sequenceList = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace("naming"));

		if (checkForDuplicateName) {
			// new array list is required since list generated from asList method does not support adding of more elements
			sequenceList = new ArrayList<>(sequenceList);
			sequenceList.add(EnforceUniqueNameRule.KEY);
		}

		NamingRuleExecutionContext context =
				new NamingRuleExecutionContext(sequenceList, this.processCodeService, row, this.germplasmDataManger,
						new ArrayList<String>());
		context.setMessageSource(this.messageSource);

		return context;
	}

	// 1. RootNameGeneratorRule
	// FIXME : breedingMethodNameType NOT USED : hard coded 1 in the 'Expression'
	protected String getGermplasmRootName(Integer breedingMethodSnameType, AdvancingSource row) throws MiddlewareQueryException {

		RootNameExpression expression = new RootNameExpression();
		List<StringBuilder> builders = new ArrayList<StringBuilder>();
		builders.add(new StringBuilder());
		expression.apply(builders, row);
		String name = builders.get(0).toString();
		if (name.length() == 0) {
			throw new MiddlewareQueryException(this.messageSource.getMessage("error.advancing.nursery.no.root.name.found",
					new Object[] {row.getGermplasm().getEntryId()}, LocaleContextHolder.getLocale()));
		}
		return name;

	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
