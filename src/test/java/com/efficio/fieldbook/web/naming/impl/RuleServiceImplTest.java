package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.naming.rules.naming.EnforceUniqueNameRule;
import com.efficio.fieldbook.web.naming.rules.naming.NamingRuleExecutionContext;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

import junit.framework.Assert;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RuleServiceImplTest extends AbstractBaseIntegrationTest{
	
	@Resource
	RulesService rulesService;

	@Resource
	private ProcessCodeService processCodeService;

	@Resource
	private RuleFactory ruleFactory;

	@Resource
	private MessageSource messageSource;

	private GermplasmDataManager germplasmDataManager;
	
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
	public void testRulesEngineUniqueCheckPass() {
		
		List<Name> names = new ArrayList<Name>();
		names.add(generateNewName(breedingMethodSnameType, 1));
		row.setNames(names);

		germplasmDataManager = mock(GermplasmDataManager.class);

		
		try {
			when(germplasmDataManager.checkIfMatches(anyString())).thenReturn(false);
			List<String> sequenceList = Arrays.asList(ruleFactory.getRuleSequenceForNamespace("naming"));
			sequenceList = new ArrayList<>(sequenceList);
			sequenceList.add(EnforceUniqueNameRule.KEY);
			NamingRuleExecutionContext ruleExecutionContext =
					new NamingRuleExecutionContext(sequenceList,
							processCodeService, row, germplasmDataManager, new ArrayList<String>());
			ruleExecutionContext.setMessageSource(messageSource);
			List<String> results = (List<String>) rulesService.runRules(ruleExecutionContext);

			assertFalse(results.isEmpty());
			System.out.println(results);

			assertEquals("test-germplasm-name-pre1suff", results.get(0));

			assertNull(row.getChangeDetail());
		} catch (RuleException | MiddlewareQueryException e) {

			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testRulesEngineUniqueCheckFail() {

		List<Name> names = new ArrayList<Name>();
		names.add(generateNewName(breedingMethodSnameType, 1));
		row.setNames(names);

		germplasmDataManager = mock(GermplasmDataManager.class);

		try {
			// set the test up so that the unique check fails twice before passing
			when(germplasmDataManager.checkIfMatches(anyString())).thenReturn(true).thenReturn(true).thenReturn(false);

			List<String> sequenceList = Arrays
					.asList(ruleFactory.getRuleSequenceForNamespace("naming"));
			sequenceList = new ArrayList<>(sequenceList);
			sequenceList.add(EnforceUniqueNameRule.KEY);

			NamingRuleExecutionContext ruleExecutionContext =
					new NamingRuleExecutionContext(sequenceList,
							processCodeService, row, germplasmDataManager, new ArrayList<String>());
			ruleExecutionContext.setMessageSource(messageSource);

			List<String> results = (List<String>) rulesService.runRules(ruleExecutionContext);
			assertFalse(results.isEmpty());

			System.out.println(results);

			assertEquals("test-germplasm-name-pre3suff", results.get(0));
			assertNotNull(row.getChangeDetail());
			assertNotNull("Sequence text not properly set for change detail object", row.getChangeDetail().getAddSequenceText());
			assertNotNull("Question text not properly set for change detail object", row.getChangeDetail().getQuestionText());
		} catch (RuleException | MiddlewareQueryException e) {

			Assert.fail(e.getMessage());
		}
	}

}