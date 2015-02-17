package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.naming.NamingRuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.naming.service.RulesService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import junit.framework.Assert;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RuleServiceImplTest extends AbstractBaseIntegrationTest{
	
	@Resource
	RulesService rulesService;

	@Resource
	private ProcessCodeService processCodeService;

	@Resource
	private RuleFactory ruleFactory;
	
	private Method breedingMethod;
	private AdvancingSource row;
	private String testGermplasmName; 
	private Integer breedingMethodSnameType;

	@Before
	public void setUp(){
		breedingMethodSnameType = 5;
		//namingConventionService.setMessageSource(Mockito.mock(ResourceBundleMessageSource.class));
		breedingMethod = new Method();
		breedingMethod.setSnametype(breedingMethodSnameType);
		breedingMethod.setPrefix("pre");
		breedingMethod.setSeparator("-");
		breedingMethod.setCount("[NUMBER]");
		breedingMethod.setSuffix("suff");
		row  = new AdvancingSource();
		row.setBreedingMethod(breedingMethod);
		row.setPlantsSelected(2);
		testGermplasmName = "test-germplasm-name"; 
	}
	
	private Name generateNewName(Integer typeId, Integer nStat){
		Name name = new Name();
		name.setTypeId(typeId);
		name.setNstat(nStat);
		name.setNval(testGermplasmName);
		return name;
	}
	
	@Test
	public void testRulesEngine() {
		
		List<Name> names = new ArrayList<Name>();
		names.add(generateNewName(breedingMethodSnameType, 1));
		row.setNames(names);
		
		try {
			List<String> sequenceList = Arrays.asList(ruleFactory.getRuleSequenceForNamespace("naming"));
			NamingRuleExecutionContext ruleExecutionContext =
					new NamingRuleExecutionContext(sequenceList,
							processCodeService, row, new ArrayList<String>());
			List<String> results = (List<String>) rulesService.runRules(ruleExecutionContext);
			Assert.assertFalse(results.isEmpty());
			System.out.println(results);
			Assert.assertEquals("test-germplasm-name-pre1suff", results.get(0));
		} catch (RuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}