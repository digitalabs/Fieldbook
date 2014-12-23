package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.rules.naming.CountRule;
import com.efficio.fieldbook.web.naming.rules.naming.PrefixRule;
import com.efficio.fieldbook.web.naming.rules.naming.RootNameGeneratorRule;
import com.efficio.fieldbook.web.naming.rules.naming.SeparatorRule;
import com.efficio.fieldbook.web.naming.rules.naming.SuffixRule;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.naming.service.RulesService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class RuleServiceImplTest extends AbstractBaseIntegrationTest{
	
	@Resource
	RulesService rulesService;
	
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
			rulesService.setInitObject(row);
			List<String> results = rulesService.runRules();
			Assert.assertFalse(results.isEmpty());
			System.out.println(results);
			Assert.assertEquals("test-germplasm-name-pre1suff", results.get(0));
		} catch (RuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
