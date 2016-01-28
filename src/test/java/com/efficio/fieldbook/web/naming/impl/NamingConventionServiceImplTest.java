
package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.service.GermplasmOriginGenerationService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.efficio.fieldbook.web.naming.service.GermplasmOriginParameterBuilder;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.google.common.collect.Lists;

public class NamingConventionServiceImplTest {

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private RulesService rulesService;

	@Mock
	private GermplasmDataManager germplasmDataManger;

	@Mock
	private AdvancingSourceListFactory advancingSourceListFactory;

	@Mock
	private ProcessCodeService processCodeService;

	@Mock
	private RuleFactory ruleFactory;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmOriginGenerationService germplasmOriginGenerationService;

	@Mock
	private GermplasmOriginParameterBuilder germplasmOriginParameterBuilder;

	@InjectMocks
	private NamingConventionServiceImpl namingConventionService = new NamingConventionServiceImpl();

	private Method breedingMethod;
	private AdvancingSource row;
	private Integer breedingMethodSnameType;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.breedingMethodSnameType = 5;
		this.breedingMethod = new Method();
		this.breedingMethod.setSnametype(this.breedingMethodSnameType);
		this.row = new AdvancingSource();
		this.row.setBreedingMethod(this.breedingMethod);

	}

	@Test
	public void testGenerateGermplasmList() throws MiddlewareQueryException, RuleException {

		AdvancingSourceList rows = new AdvancingSourceList();
		rows.setRows(new ArrayList<AdvancingSource>());

		// Set up Advancing sources
		AdvancingSource advancingSource = new AdvancingSource();
		advancingSource.setNames(new ArrayList<Name>());

		// Germplasm
		ImportedGermplasm ig = new ImportedGermplasm();
		ig.setEntryId(1);
		ig.setDesig("BARRA DE ORO DULCE");
		ig.setGid("133");
		ig.setCross("BARRA DE ORO DULCE");
		ig.setBreedingMethodId(31);
		ig.setGpid1(0);
		ig.setGpid2(0);
		ig.setGnpgs(-1);
		advancingSource.setGermplasm(ig);

		// Names
		Name sourceGermplasmName = new Name(133);
		sourceGermplasmName.setGermplasmId(133);
		sourceGermplasmName.setTypeId(6);
		sourceGermplasmName.setNstat(1);
		sourceGermplasmName.setUserId(3);
		sourceGermplasmName.setNval("BARRA DE ORO DULCE");
		sourceGermplasmName.setLocationId(9);
		sourceGermplasmName.setNdate(19860501);
		sourceGermplasmName.setReferenceId(1);
		advancingSource.getNames().add(sourceGermplasmName);

		Method breedingMethod =
				new Method(40, "DER", "G", "SLF", "Self and Bulk", "Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490,
						1, 0, 19980708, "");
		breedingMethod.setSnametype(5);
		breedingMethod.setSeparator("-");
		breedingMethod.setPrefix("B");
		breedingMethod.setCount("");

		advancingSource.setBreedingMethod(breedingMethod);
		advancingSource.setPlantsSelected(1);
		advancingSource.setBulk(false);
		advancingSource.setCheck(false);
		advancingSource.setNurseryName("Test One");
		advancingSource.setSeason("201412");
		advancingSource.setCurrentMaxSequence(0);
		rows.getRows().add(advancingSource);

		Mockito.when(this.ruleFactory.getRuleSequenceForNamespace(Mockito.eq("naming"))).thenReturn(new String[] {"RootNameGenerator"});
		final String ruleGeneratedName1 = sourceGermplasmName.getNval() + "-B1";
		final String ruleGeneratedName2 = sourceGermplasmName.getNval() + "-B2";
		Mockito.when(this.rulesService.runRules(Mockito.any(RuleExecutionContext.class))).thenReturn(Lists.newArrayList(ruleGeneratedName1, ruleGeneratedName2));
		final String testPlotCode = "NurseryName:Plot#";
		Mockito.when(this.germplasmOriginGenerationService.generateOriginString(Mockito.any(GermplasmOriginGenerationParameters.class))).thenReturn(testPlotCode);

		AdvancingNursery advancingParameters = new AdvancingNursery();
		advancingParameters.setCheckAdvanceLinesUnique(false);
		List<ImportedGermplasm> igList = this.namingConventionService.generateGermplasmList(rows, advancingParameters, null);
		Assert.assertNotNull(igList);
		Assert.assertFalse(igList.isEmpty());
		Assert.assertEquals(2, igList.size());

		// germplasm1
		ImportedGermplasm advanceResult1 = igList.get(0);
		Assert.assertEquals(new Integer(1), advanceResult1.getEntryId());
		Assert.assertEquals(ruleGeneratedName1, advanceResult1.getDesig());
		Assert.assertNull(advanceResult1.getGid());
		Assert.assertEquals(ig.getCross(), advanceResult1.getCross());
		Assert.assertEquals(testPlotCode, advanceResult1.getSource());
		Assert.assertEquals("E0001", advanceResult1.getEntryCode());
		Assert.assertEquals(new Integer(40), advanceResult1.getBreedingMethodId());
		Assert.assertEquals(new Integer(133), advanceResult1.getGpid1());
		Assert.assertEquals(new Integer(133), advanceResult1.getGpid2());

		// germplasm1 names
		Assert.assertEquals(new Integer(-1), advanceResult1.getGnpgs());
		Assert.assertEquals(1, advanceResult1.getNames().size());
		Name resultName1 = advanceResult1.getNames().get(0);
		Assert.assertNull(resultName1.getNid());
		Assert.assertEquals(new Integer(133), resultName1.getGermplasmId());
		Assert.assertEquals(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID(), resultName1.getTypeId().intValue());
		Assert.assertEquals(new Integer(1), resultName1.getNstat());
		Assert.assertEquals(ruleGeneratedName1, resultName1.getNval());
		
		// germplasm2
		ImportedGermplasm advanceResult2 = igList.get(1);
		Assert.assertEquals(new Integer(2), advanceResult2.getEntryId());
		Assert.assertEquals(ruleGeneratedName2, advanceResult2.getDesig());
		Assert.assertNull(advanceResult2.getGid());
		Assert.assertEquals(ig.getCross(), advanceResult2.getCross());
		Assert.assertEquals(testPlotCode, advanceResult2.getSource());
		Assert.assertEquals("E0002", advanceResult2.getEntryCode());
		Assert.assertEquals(new Integer(40), advanceResult2.getBreedingMethodId());
		Assert.assertEquals(new Integer(133), advanceResult2.getGpid1());
		Assert.assertEquals(new Integer(133), advanceResult2.getGpid2());

		// germplasm2 names
		Assert.assertEquals(new Integer(-1), advanceResult2.getGnpgs());
		Assert.assertEquals(1, advanceResult2.getNames().size());
		Name resultName2 = advanceResult2.getNames().get(0);
		Assert.assertNull(resultName2.getNid());
		Assert.assertEquals(new Integer(133), resultName2.getGermplasmId());
		Assert.assertEquals(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID(), resultName2.getTypeId().intValue());
		Assert.assertEquals(new Integer(1), resultName2.getNstat());
		Assert.assertEquals(ruleGeneratedName2, resultName2.getNval());
	}
}
