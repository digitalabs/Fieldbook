package com.efficio.fieldbook.web.naming.impl;

import junit.framework.Assert;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

public class AdvancingSourceListFactoryTest {
	@Test
	public void testCheckIfGermplasmIsExistingIfGermplasmIsNull(){
		AdvancingSourceListFactory factory = new AdvancingSourceListFactory();
		ResourceBundleMessageSource source = Mockito.mock(ResourceBundleMessageSource.class);
		factory.setMessageSource(source);
		Germplasm germplasm = null;
		boolean throwsError = false;
		try {
			factory.checkIfGermplasmIsExisting(germplasm);
		} catch (MiddlewareQueryException e) {
			throwsError = true;
		} catch (NoSuchMessageException e) {
			throwsError = true;
		}
		
		Assert.assertTrue("Should throw an error since germplasm is null", throwsError);
	}
	
	@Test
	public void testCheckIfGermplasmIsExistingIfGermplasmIsNotNull(){
		AdvancingSourceListFactory factory = new AdvancingSourceListFactory();
		factory.setMessageSource(Mockito.mock(ResourceBundleMessageSource.class));
		Germplasm germplasm = new Germplasm();
		boolean throwsError = false;
		try {
			factory.checkIfGermplasmIsExisting(germplasm);
		} catch (MiddlewareQueryException e) {
			throwsError = true;
		}
		
		Assert.assertFalse("Should not throw an error since germplasm is null", throwsError);
	}
}
