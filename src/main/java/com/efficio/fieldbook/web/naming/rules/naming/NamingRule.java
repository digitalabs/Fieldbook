package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/10/2015
 * Time: 4:58 PM
 */
public abstract class NamingRule implements Rule{

	public static final String CONTEXT_SOURCE_KEY = "source";
	public static final String CONTEXT_PROCESS_CODE_SERVICE_KEY = "processCodeService";

	@Resource
	protected ProcessCodeService processCodeService;

	protected AdvancingSource advancingSource;

	@Override public void init(Map<String, Object> context) {
		advancingSource = (AdvancingSource) context.get(CONTEXT_SOURCE_KEY);
		processCodeService = (ProcessCodeService) context.get(CONTEXT_PROCESS_CODE_SERVICE_KEY);
	}
}
