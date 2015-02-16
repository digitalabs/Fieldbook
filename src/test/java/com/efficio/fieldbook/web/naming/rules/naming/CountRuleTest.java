package com.efficio.fieldbook.web.naming.rules.naming;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.RuleExecutionContext;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import junit.framework.Assert;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CountRuleTest extends BaseNamingRuleTest {
	
	private Rule rule;
	private Method breedingMethod;
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

		name = "TestGP";
	}
	
	@Test
	public void testNumberCount(){		
		row.setPlantsSelected(3);		
		setBulking(breedingMethod, true);

		List<String> input = new ArrayList<String>();

		input.add(name);
		RuleExecutionContext context = createExecutionContext(input);

		try{
			rule.runRule(context);
			input = (List<String>) context.getRuleExecutionOutput();
			assertEquals(1, input.size());
			assertEquals("Should return the correct countr", "TestGP3", input.get(0));

		} catch(RuleException re){
			fail("Rule failed to run for Count" + row.getBreedingMethod().getCount());
		}
	}
	
	@Test
	public void testNumberCountNoPlantsSelected(){		
		row.setPlantsSelected(0);
		setBulking(breedingMethod, true);

		List<String> input = new ArrayList<String>();
		input.add(name);

		RuleExecutionContext context = createExecutionContext(input);

		try{
			rule.runRule(context);

			input = (List<String>) context.getRuleExecutionOutput();

			Assert.assertEquals(1, input.size());
			Assert.assertEquals("Should return the correct countr", "TestGP", input.get(0));
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Count" + row.getBreedingMethod().getCount());
		}

	}
	
	@Test
	public void testSequenceCountNoPlantsSelected(){
		row.setPlantsSelected(0);
		setBulking(breedingMethod, false);
		List<String> input = new ArrayList<String>();
		input.add(name);

		RuleExecutionContext context = createExecutionContext(input);
		try{
			input = (List<String>) rule.runRule(context);
			Assert.assertEquals(1, input.size());
			Assert.assertEquals("Should return the correct countr", "TestGP", input.get(0));
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Count" + row.getBreedingMethod().getCount());
		}


		
	}
	
	@Test
	public void testSequenceCount(){		
		row.setPlantsSelected(3);
		row.setBreedingMethod(setBulking(breedingMethod, false));
		List<String> input = new ArrayList<String>();
		input.add(name);

		RuleExecutionContext context = createExecutionContext(input);
		try{
			input = (List<String>) rule.runRule(context);

			Assert.assertEquals(3, input.size());
					Assert.assertEquals("Should return the correct countr", "TestGP1", input.get(0));
					Assert.assertEquals("Should return the correct countr", "TestGP2", input.get(1));
					Assert.assertEquals("Should return the correct countr", "TestGP3", input.get(2));
		}catch(RuleException re){
			Assert.fail("Rule failed to run for Count" + row.getBreedingMethod().getCount());
		}

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
