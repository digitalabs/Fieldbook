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
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

public class NamingConventionServiceImplTest extends AbstractBaseIntegrationTest {
	
	@Resource
	private NamingConventionService namingConventionService;
	
	private Method breedingMethod;
	private AdvancingSource row;
	private Integer breedingMethodSnameType;
	
	@Before
	public void setUp(){
		breedingMethodSnameType = 5;
		breedingMethod = new Method();
		breedingMethod.setSnametype(breedingMethodSnameType);
		row  = new AdvancingSource();
		row.setBreedingMethod(breedingMethod);
	}
	
	// Testing without unique name checking in order to be 
	@Test
	public void testGenerateGermplasmList() throws MiddlewareQueryException, RuleException {
		
		AdvancingSourceList rows = new AdvancingSourceList();
		rows.setRows(new ArrayList<AdvancingSource>());
		
		//Set up Advancing sources
		AdvancingSource as1 = new AdvancingSource();
		as1.setNames(new ArrayList<Name>());
		
		//Germplasm
		ImportedGermplasm ig = new ImportedGermplasm();
		ig.setEntryId(1);
		ig.setDesig("BARRA DE ORO DULCE");
		ig.setGid("133");
		ig.setCross("BARRA DE ORO DULCE");
		ig.setBreedingMethodId(31);
		ig.setGpid1(0);
		ig.setGpid2(0);
		ig.setGnpgs(-1);
		as1.setGermplasm(ig);
		
		//Names
		Name name1 = new Name(133);
		name1.setGermplasmId(133);
		name1.setTypeId(6);
		name1.setNstat(1);
		name1.setUserId(3);
		name1.setNval("BARRA DE ORO DULCE");
		name1.setLocationId(9);
		name1.setNdate(19860501);
		name1.setReferenceId(1);
		as1.getNames().add(name1);
		
		Method breedingMethod = new Method(40, "DER", "G", "SLF", "Self and Bulk", "Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490, 1, 0, 19980708);
		breedingMethod.setSnametype(5);
		breedingMethod.setSeparator("-");
		breedingMethod.setPrefix("B");
		breedingMethod.setCount("[NUMBER]");
		as1.setBreedingMethod(breedingMethod);
		
		as1.setPlantsSelected(1);
		as1.setBulk(false);
		as1.setCheck(false);
		as1.setNurseryName("Test One");
		as1.setSeason("201412");
		as1.setCurrentMaxSequence(0);		
		rows.getRows().add(as1);
		
		List<ImportedGermplasm> igList = namingConventionService.generateGermplasmList(rows, false);
		Assert.assertNotNull(igList);
		Assert.assertFalse(igList.isEmpty());
		Assert.assertEquals(1, igList.size());
		
		// germplasm
		ImportedGermplasm resultIG = igList.get(0);
		Assert.assertEquals(new Integer(1), resultIG.getEntryId());
		Assert.assertEquals("BARRA DE ORO DULCE-B", resultIG.getDesig());
		Assert.assertNull(resultIG.getGid());
		Assert.assertEquals("BARRA DE ORO DULCE", resultIG.getCross());
		Assert.assertEquals("Test One:1", resultIG.getSource());
		Assert.assertEquals("E0001", resultIG.getEntryCode());
		Assert.assertEquals(new Integer(40), resultIG.getBreedingMethodId());
		Assert.assertEquals(new Integer(133), resultIG.getGpid1());
		Assert.assertEquals(new Integer(133), resultIG.getGpid2());
		
		// names
		Assert.assertEquals(new Integer(-1), resultIG.getGnpgs());
		Assert.assertEquals(1, resultIG.getNames().size());
		Name resultName = resultIG.getNames().get(0);
		Assert.assertNull(resultName.getNid());
		Assert.assertEquals(new Integer(133), resultName.getGermplasmId());
		Assert.assertEquals(new Integer(5), resultName.getTypeId());
		Assert.assertEquals(new Integer(1), resultName.getNstat());
		Assert.assertEquals("BARRA DE ORO DULCE-B", resultName.getNval());
		
	}

}
