package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;

import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class SeparatorRuleTest {
	
	@Resource
	private ProcessCodeService processCodeService;
	
	private SeparatorRule rule;
	private Method breedingMethod;
	private AdvancingSource row;
	private String testGermplasmName; 
	private Integer breedingMethodSnameType;
	
	@Before
	public void setUp(){
		breedingMethodSnameType = 5;
		breedingMethod = new Method();
		breedingMethod.setSnametype(breedingMethodSnameType);
		breedingMethod.setSeparator("-");
		row  = new AdvancingSource();
		row.setBreedingMethod(breedingMethod);
		testGermplasmName = "CMT1234"; 
		rule = new SeparatorRule();
		rule.init(processCodeService, row);
	}
	
	@Test
	public void testGetGermplasmRootNameWithTheSameSnameTypeWithMethod(){		

		List<String> input = new ArrayList<String>();
		input.add(testGermplasmName);
		try{
			input = rule.runRule(input);
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Separator" + row.getBreedingMethod().getSeparator());
		}
		Assert.assertEquals(1, input.size());;
		Assert.assertEquals("Should return the correct name appended with a separator", testGermplasmName + row.getBreedingMethod().getSeparator(), input.get(0));
	}
	
}
