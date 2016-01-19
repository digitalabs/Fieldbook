
package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import junit.framework.Assert;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.commons.service.GermplasmOriginGenerationService;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
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
		AdvancingSource as1 = new AdvancingSource();
		as1.setNames(new ArrayList<Name>());

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
		as1.setGermplasm(ig);

		// Names
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

		Method breedingMethod =
				new Method(40, "DER", "G", "SLF", "Self and Bulk", "Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490,
						1, 0, 19980708, "");
		breedingMethod.setSnametype(5);
		breedingMethod.setSeparator("-");
		breedingMethod.setPrefix("B");
		breedingMethod.setCount("");

		as1.setBreedingMethod(breedingMethod);
		as1.setPlantsSelected(1);
		as1.setBulk(false);
		as1.setCheck(false);
		as1.setNurseryName("Test One");
		as1.setSeason("201412");
		as1.setCurrentMaxSequence(0);
		rows.getRows().add(as1);

		Mockito.when(this.ruleFactory.getRuleSequenceForNamespace(Mockito.eq("naming"))).thenReturn(new String[] {"RootNameGenerator"});
		final String ruleGeneratedName = name1.getNval() + "-B";
		Mockito.when(this.rulesService.runRules(Mockito.any(RuleExecutionContext.class))).thenReturn(
				Lists.newArrayList(ruleGeneratedName));
		final String testPlotCode = "NurseryName:Plot#";
		Mockito.when(this.germplasmOriginGenerationService.generateOriginString(Mockito.any(GermplasmOriginGenerationParameters.class)))
		.thenReturn(testPlotCode);

		List<ImportedGermplasm> igList = this.namingConventionService.generateGermplasmList(rows, false, null);
		Assert.assertNotNull(igList);
		Assert.assertFalse(igList.isEmpty());
		Assert.assertEquals(1, igList.size());

		// germplasm
		ImportedGermplasm resultIG = igList.get(0);
		Assert.assertEquals(new Integer(1), resultIG.getEntryId());
		Assert.assertEquals(ruleGeneratedName, resultIG.getDesig());
		Assert.assertNull(resultIG.getGid());
		Assert.assertEquals(ig.getCross(), resultIG.getCross());
		Assert.assertEquals(testPlotCode, resultIG.getSource());
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
		Assert.assertEquals(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID(), resultName.getTypeId().intValue());
		Assert.assertEquals(new Integer(1), resultName.getNstat());
		Assert.assertEquals(ruleGeneratedName, resultName.getNval());
	}

    @Test
    public void testAdvanceStudyForNurserySuccess() throws MiddlewareQueryException, RuleException, FieldbookException {
        Method breedingMethod =
                new Method(40, "DER", "G", "SLF", "Self and Bulk", "Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490,
                        1, 0, 19980708, "");
        breedingMethod.setSnametype(5);
        breedingMethod.setSeparator("-");
        breedingMethod.setPrefix("B");
        breedingMethod.setCount("");

        final List<Method> methodList = Lists.newArrayList();
        methodList.add(breedingMethod);

        Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(Mockito.anyBoolean())).thenReturn(methodList);

        Workbook workbook = new Workbook();
        Mockito.when(this.fieldbookMiddlewareService.getNurseryDataSet(Mockito.anyInt())).thenReturn(workbook);
        AdvancingSourceList rows = new AdvancingSourceList();
        rows.setRows(new ArrayList<AdvancingSource>());

        AdvancingSource as1 = new AdvancingSource();
        as1.setNames(new ArrayList<Name>());

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


        as1.setBreedingMethod(breedingMethod);
        as1.setPlantsSelected(1);
        as1.setBulk(false);
        as1.setCheck(false);
        as1.setNurseryName("Test One");
        as1.setSeason("201412");
        as1.setCurrentMaxSequence(0);
        rows.getRows().add(as1);

        Mockito.when(this.advancingSourceListFactory.createAdvancingSourceList(Mockito.isA(Workbook.class),Mockito.isA(AdvancingNursery.class),Mockito.isA(Study.class),Mockito.isA(Map.class),Mockito.isA(Map.class)))
                .thenReturn(rows);

        Mockito.when(this.ruleFactory.getRuleSequenceForNamespace(Mockito.eq("naming"))).thenReturn(new String[] {"RootNameGenerator"});
        final String ruleGeneratedName = name1.getNval() + "-B";
        Mockito.when(this.rulesService.runRules(Mockito.any(RuleExecutionContext.class))).thenReturn(
                Lists.newArrayList(ruleGeneratedName));
        final String testPlotCode = "NurseryName:Plot#";
        Mockito.when(this.germplasmOriginGenerationService.generateOriginString(Mockito.any(GermplasmOriginGenerationParameters.class)))
                .thenReturn(testPlotCode);

        AdvancingNursery info = new AdvancingNursery();
        info.setMethodChoice("1");
        info.setLineChoice("1");
        info.setLineSelected("1");
        info.setAllPlotsChoice("1");
        info.setLineSelected("1");

        Study study = new Study();
        study.setId(2345);
        info.setStudy(study);

        AdvanceResult advanceResult = namingConventionService.advanceNursery(info,null);

        Assert.assertNotNull(advanceResult);
        Assert.assertNotNull(advanceResult.getChangeDetails());
        Assert.assertEquals(0,advanceResult.getChangeDetails().size());

        Assert.assertNotNull(advanceResult.getAdvanceList());
        Assert.assertEquals(1, advanceResult.getAdvanceList().size());

        ImportedGermplasm resultIG = advanceResult.getAdvanceList().get(0);
        Assert.assertEquals(new Integer(1), resultIG.getEntryId());
        Assert.assertEquals(ruleGeneratedName, resultIG.getDesig());
        Assert.assertNull(resultIG.getGid());
        Assert.assertEquals(ig.getCross(), resultIG.getCross());
        Assert.assertEquals(testPlotCode, resultIG.getSource());
        Assert.assertEquals("E0001", resultIG.getEntryCode());
        Assert.assertEquals(new Integer(40), resultIG.getBreedingMethodId());
        Assert.assertEquals(new Integer(133), resultIG.getGpid1());
        Assert.assertEquals(new Integer(133), resultIG.getGpid2());

        Assert.assertEquals(new Integer(-1), resultIG.getGnpgs());
        Assert.assertEquals(1, resultIG.getNames().size());
        Name resultName = resultIG.getNames().get(0);
        Assert.assertNull(resultName.getNid());
        Assert.assertEquals(new Integer(133), resultName.getGermplasmId());
        Assert.assertEquals(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID(), resultName.getTypeId().intValue());
        Assert.assertEquals(new Integer(1), resultName.getNstat());
        Assert.assertEquals(ruleGeneratedName, resultName.getNval());

    }

}
