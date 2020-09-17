
package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.AdvanceType;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import com.google.common.collect.Lists;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.commons.pojo.AdvancingSource;
import org.generationcp.commons.pojo.AdvancingSourceList;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.generator.SeedSourceGenerator;
import org.generationcp.commons.ruleengine.naming.service.ProcessCodeService;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyInt;

public class NamingConventionServiceImplTest {

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private RulesService rulesService;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private ProcessCodeService processCodeService;

	@Mock
	private RuleFactory ruleFactory;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@InjectMocks
	private final NamingConventionServiceImpl namingConventionService = new NamingConventionServiceImpl();

	private Method breedingMethod;
	private AdvancingSource row;
	private Integer breedingMethodSnameType;
	private Workbook workbook;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.breedingMethodSnameType = 5;
		this.breedingMethod = new Method();
		this.breedingMethod.setSnametype(this.breedingMethodSnameType);
		this.row = new AdvancingSource();
		this.row.setBreedingMethod(this.breedingMethod);
		this.workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyTypeDto.getNurseryDto());
		studyDetails.setStudyName("STUDY:ABC");
		studyDetails.setId(new Random().nextInt());
		this.workbook.setStudyDetails(studyDetails);

	}

    @Test
    public void testAdvanceStudyForNurserySuccess() throws MiddlewareQueryException, RuleException, FieldbookException {
        final Method breedingMethod =
                new Method(40, "DER", "G", "SLF", "Self and Bulk", "Selfing a Single Plant or population and bulk seed", 0, -1, 1, 0, 1490,
                        1, 0, 19980708, "");
        breedingMethod.setSnametype(5);
        breedingMethod.setSeparator("-");
        breedingMethod.setPrefix("B");
        breedingMethod.setCount("");

        final List<Method> methodList = Lists.newArrayList();
        methodList.add(breedingMethod);

        Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(ArgumentMatchers.anyBoolean())).thenReturn(methodList);
		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(anyInt())).thenReturn(this.workbook);

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyName("Test1");
		studyDetails.setStudyType(StudyTypeDto.getNurseryDto());
		workbook.setStudyDetails(studyDetails);

        final AdvancingSourceList rows = new AdvancingSourceList();
        rows.setRows(new ArrayList<AdvancingSource>());

        final AdvancingSource as1 = new AdvancingSource();
        as1.setNames(new ArrayList<Name>());

        final ImportedGermplasm ig = new ImportedGermplasm();
        ig.setEntryNumber(1);
        ig.setDesig("BARRA DE ORO DULCE");
        ig.setGid("133");
        ig.setCross("BARRA DE ORO DULCE");
        ig.setBreedingMethodId(31);
        ig.setGpid1(0);
        ig.setGpid2(0);
        ig.setGnpgs(-1);
        as1.setGermplasm(ig);

        final Name name1 = new Name(133);
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
        as1.setStudyName("Test One");
        as1.setSeason("201412");
        as1.setCurrentMaxSequence(0);
        as1.setTrialInstanceNumber("1");
        rows.getRows().add(as1);

        final String ruleGeneratedName = name1.getNval() + "-B";
        Mockito.when(this.rulesService.runRules(ArgumentMatchers.any(RuleExecutionContext.class))).thenReturn(
                Lists.newArrayList(ruleGeneratedName));
		final String testSeedSource = "MEX-DrySeason-N1-1-2";

		final AdvancingStudy info = new AdvancingStudy();
        info.setMethodChoice("1");
        info.setLineChoice("1");
        info.setLineSelected("1");
        info.setAllPlotsChoice("1");
        info.setLineSelected("1");
		info.setAdvanceType(AdvanceType.STUDY);

        final Study study = new Study();
        study.setId(2345);
        info.setStudy(study);

//		final AdvanceResult advanceResult = new AdvanceResult(); // this.namingConventionService.advanceStudy(info, this.workbook);
//
//        Assert.assertNotNull(advanceResult);
//        Assert.assertNotNull(advanceResult.getChangeDetails());
//        Assert.assertEquals(0,advanceResult.getChangeDetails().size());
//
//        Assert.assertNotNull(advanceResult.getAdvanceList());
//        Assert.assertEquals(1, advanceResult.getAdvanceList().size());

        final ImportedGermplasm resultIG = new ImportedGermplasm(); // = advanceResult.getAdvanceList().get(0);
        Assert.assertEquals(new Integer(1), resultIG.getEntryNumber());
        Assert.assertEquals(ruleGeneratedName, resultIG.getDesig());
        Assert.assertNull(resultIG.getGid());
        Assert.assertEquals(ig.getCross(), resultIG.getCross());
		Assert.assertEquals(testSeedSource, resultIG.getSource());
        Assert.assertEquals("E0001", resultIG.getEntryCode());
        Assert.assertEquals(new Integer(40), resultIG.getBreedingMethodId());
        Assert.assertEquals(new Integer(133), resultIG.getGpid1());
        Assert.assertEquals(new Integer(133), resultIG.getGpid2());

        Assert.assertEquals(new Integer(-1), resultIG.getGnpgs());
        Assert.assertEquals(1, resultIG.getNames().size());
        final Name resultName = resultIG.getNames().get(0);
        Assert.assertNull(resultName.getNid());
        Assert.assertNull(resultName.getGermplasmId());
        Assert.assertEquals(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID(), resultName.getTypeId().intValue());
        Assert.assertEquals(new Integer(1), resultName.getNstat());
        Assert.assertEquals(ruleGeneratedName, resultName.getNval());

    }
	@Test
	public void testGenerateCrossesList() throws RuleException{
		final List<ImportedCross> importedCrosses = new ArrayList<>();
		final ImportedCross importedCross = new ImportedCross();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(1, "femaleDesig", "femalePedig");
		importedCross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(2, "maleDesig", "malePedig");
		importedCross.setMaleParents(Collections.singletonList(maleParent));
		importedCrosses.add(importedCross);

		final AdvancingSourceList rows = new AdvancingSourceList();
		final AdvancingSource advancingSource = new AdvancingSource();
		advancingSource.setBreedingMethodId(101);
		rows.setRows(Collections.singletonList(advancingSource));

		final AdvancingStudy advancingParameters = Mockito.mock(AdvancingStudy.class);
		final Workbook workbook = Mockito.mock(Workbook.class);
		final List<Integer> gids = Collections.singletonList(1);
		final Method method = new Method();
		method.setMid(101);
		method.setPrefix("IB");
		method.setCount("[COUNT]");

		Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(false)).thenReturn(Collections.singletonList(method));
		Mockito.when(this.germplasmDataManager.isMethodNamingConfigurationValid(method)).thenReturn(true);
		Mockito.when(this.rulesService.runRules(ArgumentMatchers.any(RuleExecutionContext.class))).thenReturn(Collections.singletonList("name"));
		Mockito.when(this.ruleFactory.getRuleSequenceForNamespace("naming")).thenReturn(new String[] {"[COUNT]"});

		this.namingConventionService.generateCrossesList(importedCrosses, rows, advancingParameters, workbook, gids);
		Assert.assertEquals("name", importedCross.getDesig());
		Mockito.verify(this.fieldbookMiddlewareService).getAllBreedingMethods(false);
		Mockito.verify(this.germplasmDataManager).isMethodNamingConfigurationValid(method);
		Mockito.verify(this.rulesService).runRules(ArgumentMatchers.any(RuleExecutionContext.class));
		Mockito.verify(this.ruleFactory).getRuleSequenceForNamespace("naming");
		Assert.assertEquals(0, advancingSource.getCurrentMaxSequence());
	}
}
