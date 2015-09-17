
package com.efficio.fieldbook;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.service.api.FieldbookService;

public class ContextInitTest extends AbstractBaseIntegrationTest {

	@Autowired
	private FieldbookService fieldbookService;

	@Test
	public void testContextLoadsSuccessfully() {
		Assert.assertNotNull("Spring application context loading fails.", this.fieldbookService);
	}
}
