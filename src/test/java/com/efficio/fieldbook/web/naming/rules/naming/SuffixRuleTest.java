package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;

import com.efficio.fieldbook.web.naming.impl.ProcessCodeServiceImpl;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SuffixRuleTest extends BaseNamingRuleTest{
	
	private SuffixRule rule;
	private Method breedingMethod;
	private String testGermplasmName;
	private Integer breedingMethodSnameType;
	
	@Before
	public void setUp(){
		processCodeService = new ProcessCodeServiceImpl();
		breedingMethodSnameType = 5;
		breedingMethod = new Method();
		breedingMethod.setSnametype(breedingMethodSnameType);
		breedingMethod.setSuffix("test-suffix");
		row  = new AdvancingSource();
		row.setBreedingMethod(breedingMethod);
		testGermplasmName = "CMT1234-B-3-"; 
		rule = new SuffixRule();
	}
	
	@Test
	public void testPrefixGenerationSimple(){		

		List<String> input = new ArrayList<String>();
		input.add(testGermplasmName);

		try{
			input = (List<String>) rule.runRule(createExecutionContext(input));
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Prefix" + row.getBreedingMethod().getSuffix());
		}

		Assert.assertEquals(1, input.size());;
		Assert.assertEquals("Should return the correct name appended with prefix text", testGermplasmName + row.getBreedingMethod().getSuffix(), input.get(0));
	}
	
}
