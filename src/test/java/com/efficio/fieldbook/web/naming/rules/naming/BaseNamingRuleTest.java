package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/13/2015
 * Time: 6:02 PM
 */
public class BaseNamingRuleTest extends AbstractBaseIntegrationTest{

	@Resource
	protected ProcessCodeService processCodeService;

	protected AdvancingSource row;

	protected NamingRuleExecutionContext createExecutionContext(List<String> input) {
		return new NamingRuleExecutionContext(null, processCodeService, row, null, input );
	}
}
