package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.impl.RuleFactory;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/14/2015
 * Time: 12:53 AM
 */
public class NamingRuleExecutionContext implements RuleExecutionContext{

	private List<String> executionOrder;
	private ProcessCodeService processCodeService;
	private AdvancingSource advancingSource;
	private List<String> currentData;

	public NamingRuleExecutionContext(List<String> executionOrder,
			ProcessCodeService processCodeService,
			AdvancingSource advancingSource, List<String> currentData) {
		this.executionOrder = executionOrder;
		this.processCodeService = processCodeService;
		this.advancingSource = advancingSource;
		this.currentData = currentData;
	}

	@Override
	public Object getRuleExecutionOutput() {
		return currentData;
	}

	public List<String> getExecutionOrder() {
		return executionOrder;
	}

	public void setExecutionOrder(List<String> executionOrder) {
		this.executionOrder = executionOrder;
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
}
