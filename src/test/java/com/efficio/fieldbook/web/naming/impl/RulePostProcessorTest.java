package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.web.naming.rules.Rule;
import com.efficio.fieldbook.web.naming.rules.naming.CountRule;
import com.efficio.fieldbook.web.trial.controller.CreateTrialController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/16/2015
 * Time: 12:52 PM
 */

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class RulePostProcessorTest {

	@Mock
	private RuleFactory factory;

	@InjectMocks
	private RulesPostProcessor postProcessor;

	@Test
	public void testPostProcessingForRule() {
		// verify that Rule objects are processed correctly by the class
		Rule rule = new CountRule();

		postProcessor.postProcessAfterInitialization(rule, rule.getKey());
		verify(factory).addRule(rule);
	}

	@Test
	public void testPostProcessingOtherObjects() {
		// verify that other objects in component scan do not affect processor
		CreateTrialController controller = new CreateTrialController();

		postProcessor.postProcessAfterInitialization(controller, "CreateTrialController");
		verify(factory, never()).addRule(any(Rule.class));
	}
}
