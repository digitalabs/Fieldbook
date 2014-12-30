package com.efficio.fieldbook.web.naming.rules.naming;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class CountRuleTest extends AbstractBaseIntegrationTest {
	
	@Resource
	ProcessCodeService processCodeService;
	
	private Rule rule;
	private Method breedingMethod;
	private AdvancingSource row;
	private Integer breedingMethodSnameType;
	private String name;
	
	@Before
	public void setUp(){
		breedingMethodSnameType = 5;
		breedingMethod = new Method();
		breedingMethod.setSnametype(breedingMethodSnameType);
		breedingMethod.setSeparator("-");
		breedingMethod.setCount("[NUMBER]");
		row  = new AdvancingSource();
		row.setBreedingMethod(breedingMethod);
		rule = new CountRule();
		rule.init(row);
		rule.setProcessCodeService(processCodeService);
		name = "TestGP";
	}
	
	@Test
	public void testNumberCount(){		
		row.setPlantsSelected(3);		
		setBulking(breedingMethod, true);
		List<String> input = new ArrayList<String>();
		input.add(name);
		try{
			input = rule.runRule(input);
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Count" + row.getBreedingMethod().getCount());
		}
		Assert.assertEquals(1, input.size());
		Assert.assertEquals("Should return the correct countr", "TestGP3", input.get(0));
	}
	
	@Test
	public void testNumberCountNoPlantsSelected(){		
		row.setPlantsSelected(0);
		setBulking(breedingMethod, true);
		List<String> input = new ArrayList<String>();
		input.add(name);
		try{
			input = rule.runRule(input);
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Count" + row.getBreedingMethod().getCount());
		}
		Assert.assertEquals(1, input.size());
		Assert.assertEquals("Should return the correct countr", "TestGP", input.get(0));
	}
	
	@Test
	public void testSequenceCountNoPlantsSelected(){
		row.setPlantsSelected(0);
		setBulking(breedingMethod, false);
		List<String> input = new ArrayList<String>();
		input.add(name);
		try{
			input = rule.runRule(input);
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Count" + row.getBreedingMethod().getCount());
		}
		Assert.assertEquals(1, input.size());
		Assert.assertEquals("Should return the correct countr", "TestGP", input.get(0));
		
	}
	
	@Test
	public void testSequenceCount(){		
		row.setPlantsSelected(3);
		setBulking(breedingMethod, false);
		List<String> input = new ArrayList<String>();
		input.add(name);
		try{
			input = rule.runRule(input);
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Count" + row.getBreedingMethod().getCount());
		}
		Assert.assertEquals(3, input.size());
		Assert.assertEquals("Should return the correct countr", "TestGP1", input.get(0));
		Assert.assertEquals("Should return the correct countr", "TestGP2", input.get(1));
		Assert.assertEquals("Should return the correct countr", "TestGP3", input.get(2));
		
	}
	
	
	private Method setBulking(Method method, boolean isBulking) {
		
		if (isBulking) {
			method.setGeneq(TermId.BULKING_BREEDING_METHOD_CLASS.getId());
		}
		else {
			method.setGeneq(TermId.NON_BULKING_BREEDING_METHOD_CLASS.getId());
		}
		return method;
		
	}
	
}
