package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.springframework.context.MessageSource;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/14/2015
 * Time: 12:53 AM
 */
public class NamingRuleExecutionContext extends OrderedRuleExecutionContext{


	private ProcessCodeService processCodeService;
	private AdvancingSource advancingSource;
	private GermplasmDataManager germplasmDataManager;
	private List<String> currentData;
	private MessageSource messageSource;

	private List<String> tempData;

	public NamingRuleExecutionContext(List<String> executionOrder,
			ProcessCodeService processCodeService,
			AdvancingSource advancingSource, GermplasmDataManager germplasmDataManager,
			List<String> currentData) {
		super(executionOrder);
		this.processCodeService = processCodeService;
		this.advancingSource = advancingSource;
		this.currentData = currentData;
		this.germplasmDataManager  = germplasmDataManager;

	}

	@Override
	public Object getRuleExecutionOutput() {
		return currentData;
	}

	public ProcessCodeService getProcessCodeService() {
		return processCodeService;
	}

	public void setProcessCodeService(ProcessCodeService processCodeService) {
		this.processCodeService = processCodeService;
	}

	public AdvancingSource getAdvancingSource() {
		return advancingSource;
	}

	public void setAdvancingSource(AdvancingSource advancingSource) {
		this.advancingSource = advancingSource;
	}

	public List<String> getCurrentData() {
		return currentData;
	}

	public void setCurrentData(List<String> currentData) {
		this.currentData = currentData;
	}

	public GermplasmDataManager getGermplasmDataManager() {
		return germplasmDataManager;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public List<String> getTempData() {
		return tempData;
	}

	public void setTempData(List<String> tempData) {
		this.tempData = tempData;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}