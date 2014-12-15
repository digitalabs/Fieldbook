package com.efficio.fieldbook.web.naming.rules;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;

import com.efficio.fieldbook.web.naming.impl.ProcessCodeServiceImpl;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SuffixRuleTest {
	
	private ProcessCodeService processCodeService;	
	private SuffixRule rule;
	private Method breedingMethod;
	private AdvancingSource row;
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
		rule.init(processCodeService, row);
	}
	
	@Test
	public void testPrefixGenerationSimple(){		

		List<String> input = new ArrayList<String>();
		input.add(testGermplasmName);
		try{
			input = rule.runRule(input);
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Prefix" + row.getBreedingMethod().getSuffix());
		}
		Assert.assertEquals(1, input.size());;
		Assert.assertEquals("Should return the correct name appended with prefix text", testGermplasmName + row.getBreedingMethod().getSuffix(), input.get(0));
	}
	
}
