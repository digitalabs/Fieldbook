
package com.efficio.fieldbook.web.naming.impl;

import java.util.Collection;

import javax.annotation.Resource;

import org.generationcp.commons.ruleengine.RuleFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 2/16/2015 Time: 2:05 PM
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RuleFactoryIntegrationTest {

	@Resource
	private RuleFactory factory;

	@Test
	public void testSuccessfulRuleRegistration() {
		Assert.assertTrue("Rules not successfully registered to factory", this.factory.getAvailableRuleCount() > 0);
	}

	@Test
	public void testRuleConfiguration() {
		Collection<String> configuredNamespaces = this.factory.getAvailableConfiguredNamespaces();
		Assert.assertNotNull(configuredNamespaces);
		Assert.assertFalse(configuredNamespaces.isEmpty());
	}
}
