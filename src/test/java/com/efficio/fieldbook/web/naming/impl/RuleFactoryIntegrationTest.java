package com.efficio.fieldbook.web.naming.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/16/2015
 * Time: 2:05 PM
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RuleFactoryIntegrationTest {

	@Resource
	private RuleFactory factory;

	@Test
	public void testSuccessfulRuleRegistration() {
		assertTrue("Rules not successfully registered to factory", factory.getAvailableRuleCount() > 0);
	}

	@Test
	public void testRuleConfiguration() {
		Collection<String> configuredNamespaces = factory.getAvailableConfiguredNamespaces();
		assertNotNull(configuredNamespaces);
		assertFalse(configuredNamespaces.isEmpty());
	}
}