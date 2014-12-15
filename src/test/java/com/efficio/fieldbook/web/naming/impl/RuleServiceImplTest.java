package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.naming.rules.CountRule;
import com.efficio.fieldbook.web.naming.rules.PrefixRule;
import com.efficio.fieldbook.web.naming.rules.RootNameGeneratorRule;
import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.SeparatorRule;
import com.efficio.fieldbook.web.naming.rules.SuffixRule;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class RuleServiceImplTest extends AbstractBaseIntegrationTest{
	
	@Resource
	ProcessCodeService processCodeService;
	
	private RulesServiceImpl rulesServiceImpl = new RulesServiceImpl();
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
		
		Rule rngr = new RootNameGeneratorRule();
		rngr.init(processCodeService, row);
		Rule separatorRule = new SeparatorRule();
		separatorRule.init(processCodeService, row);
		Rule prefixRule = new PrefixRule();
		prefixRule.init(processCodeService, row);
		Rule countRule = new CountRule();
		countRule.init(processCodeService, row);
		Rule suffixRule = new SuffixRule();
		suffixRule.init(processCodeService, row);		
		
		List<Rule> rules = new ArrayList<>();
		rules.add(rngr);
		rules.add(separatorRule);
		rules.add(prefixRule);
		rules.add(countRule);
		rules.add(suffixRule);
		
		rulesServiceImpl.setRules(rules);
		
		try {
			List<String> results = rulesServiceImpl.runRules();
			Assert.assertFalse(results.isEmpty());
			System.out.println(results);
			Assert.assertEquals("test-germplasm-name-pre2suff", results.get(0));
		} catch (RuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
