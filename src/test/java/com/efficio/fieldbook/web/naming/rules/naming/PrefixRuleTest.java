package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.naming.impl.ProcessCodeServiceImpl;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class PrefixRuleTest extends AbstractBaseIntegrationTest{
	
	@Resource
	private ProcessCodeService processCodeService;	
	
	private PrefixRule rule;
	private Method breedingMethod;
	private AdvancingSource row;
	private String testGermplasmName; 
	private Integer breedingMethodSnameType;
	
	@Before
	public void setUp(){

		breedingMethodSnameType = 5;
		breedingMethod = new Method();
		breedingMethod.setSnametype(breedingMethodSnameType);
		row  = new AdvancingSource();
		row.setBreedingMethod(breedingMethod);
		testGermplasmName = "CMT1234-"; 
		rule = new PrefixRule();
		rule.init(processCodeService, row);
	}
	
	@Test
	public void testPrefixGenerationSimple(){	
		breedingMethod.setPrefix("B");
		List<String> input = new ArrayList<String>();
		input.add(testGermplasmName);
		try{
			input = rule.runRule(input);
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Prefix" + row.getBreedingMethod().getSeparator());
		}
		Assert.assertEquals(1, input.size());;
		Assert.assertEquals("Should return the correct name appended with prefix text", testGermplasmName + row.getBreedingMethod().getPrefix(), input.get(0));
	}
	
	@Test
	public void testSeasonCodePrefix() {	
		breedingMethod.setPrefix("[SEASON]");
		row.setSeason("Wet");
		List<String> input = new ArrayList<String>();
		input.add(testGermplasmName);
		try{
			input = rule.runRule(input);
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Prefix" + row.getBreedingMethod().getSeparator());
		}
		Assert.assertEquals(1, input.size());;
		Assert.assertEquals("Should return the correct name appended with prefix text", testGermplasmName + row.getSeason(), input.get(0));		
	}
	
}
