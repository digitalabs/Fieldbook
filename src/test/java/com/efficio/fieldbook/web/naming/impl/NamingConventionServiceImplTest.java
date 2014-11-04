package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

public class NamingConventionServiceImplTest {
	
	private NamingConventionServiceImpl namingConventionService;
	private Method breedingMethod;
	private AdvancingSource row;
	private String testGermplasmName; 
	private Integer breedingMethodSnameType;
	@Before
	public void setUp(){
		breedingMethodSnameType = 5;
		namingConventionService = new NamingConventionServiceImpl(); 
		namingConventionService.setMessageSource(Mockito.mock(ResourceBundleMessageSource.class));
		breedingMethod = new Method();
		breedingMethod.setSnametype(breedingMethodSnameType);
		row  = new AdvancingSource();
		row.setBreedingMethod(breedingMethod);
		testGermplasmName = "advance-germplasm-name"; 
	}
	
	private Name generateNewName(Integer typeId, Integer nStat){
		Name name = new Name();
		name.setTypeId(typeId);
		name.setNstat(nStat);
		name.setNval(testGermplasmName);
		return name;
	}
	
	@Test
	public void testGetGermplasmRootNameWithTheSameSnameTypeWithMethod(){		
		List<Name> names = new ArrayList<Name>();
		names.add(generateNewName(breedingMethodSnameType, 1));
		row.setNames(names);
		String rootName = "";
		try{
			rootName = namingConventionService.getGermplasmRootName(breedingMethodSnameType, row);
		}catch(MiddlewareQueryException e){
			Assert.fail("Should return the correct root name if the methd snametype is equal to the names' type id");
		}
		Assert.assertEquals("Should return the correct root name if the methd snametype is equal to the names' type id", testGermplasmName, rootName);
	}
	
	@Test
	public void testGetGermplasmRootNameWithTheDifferentSnameTypeWithMethodButWithNstatEqualTo1(){		
		List<Name> names = new ArrayList<Name>();
		names.add(generateNewName( 2, 1));
		row.setNames(names);
		String rootName = "";
		try{
			rootName = namingConventionService.getGermplasmRootName(breedingMethodSnameType, row);
		}catch(MiddlewareQueryException e){
			Assert.fail("Should return the correct root name if the methd snametype is equal to the names' type id");
		}
		Assert.assertEquals("Should return the correct root name if the names' nstat is equal to 1", testGermplasmName, rootName);
	}
	
	@Test
	public void testGetGermplasmRootNameWithTheDifferentSnameTypeWithMethodWithnstatNotEqualTo1(){
		List<Name> names = new ArrayList<Name>();
		names.add(generateNewName( 2, 0));
		row.setNames(names);
		row.setGermplasm(new ImportedGermplasm());
		boolean throwsException = false;
		try{
			namingConventionService.getGermplasmRootName(breedingMethodSnameType, row);
		}catch(MiddlewareQueryException e){
			throwsException = true;
		}catch(NoSuchMessageException e){
			throwsException = true;
		}
		Assert.assertTrue("Should throw an exception if there is no germplasm root name retrieved", throwsException);
	}
}
