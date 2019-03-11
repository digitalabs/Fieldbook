package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.web.common.exception.InvalidInputException;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class CrossingServiceImplTest {

	private static final int PLOT_CODE_FLD_NO = 1552;
	private static final String SUFFIX = "SUFFIX";
	private static final String PREFIX = "PREFIX";
	private static final int BREEDING_METHOD_ID = 1;
	private static final String SAVED_CROSSES_GID1 = "-9999";
	private static final String SAVED_CROSSES_GID2 = "-8888";
	private static final Integer USER_ID = 123;
	public static final String TEST_BREEDING_METHOD_CODE = "GEN";
	public static final Integer TEST_BREEDING_METHOD_ID = 5;
	public static final Integer TEST_FEMALE_GID_1 = 12345;
	public static final Integer TEST_MALE_GID_1 = 54321;
	public static final Integer TEST_FEMALE_GID_2 = 9999;
	public static final Integer TEST_MALE_GID_2 = 8888;

	private static final Integer NEXT_NUMBER = 100;

	private ImportedCrossesList importedCrossesList;
	
	@Captor
	private ArgumentCaptor<List<Attribute>> attributesListCaptor;

	@Mock
	private FieldbookService fieldbookMiddlewareService;
	
	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private SeedSourceGenerator seedSourceGenertor;

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private CrossingServiceImpl crossingService;

	private CrossSetting crossSetting;
	
	private Integer localUserId;

	@Before
	public void setUp() throws MiddlewareQueryException {
		this.importedCrossesList = this.createImportedCrossesList();
		this.importedCrossesList.setImportedGermplasms(this.createImportedCrosses());


		Mockito.doReturn(this.createNameTypes()).when(this.germplasmListManager).getGermplasmNameTypes();
		Mockito.doReturn(this.createGermplasmIds()).when(this.germplasmDataManager).addGermplasm(
				ArgumentMatchers.<List<Pair<Germplasm, Name>>>any());
		Mockito.doReturn(new Method()).when(this.germplasmDataManager).getMethodByID(CrossingServiceImplTest.BREEDING_METHOD_ID);
		Mockito.doReturn(this.createProject()).when(this.contextUtil).getProjectInContext();
		Mockito.doReturn(new UserDefinedField(PLOT_CODE_FLD_NO)).when(this.germplasmDataManager).getPlotCodeField();

		this.crossSetting = new CrossSetting();
		this.crossSetting.setCrossNameSetting(this.createCrossNameSetting());
		this.crossSetting.setBreedingMethodSetting(this.createBreedingMethodSetting());
		this.crossSetting.setAdditionalDetailsSetting(this.getAdditionalDetailsSetting());

		Mockito.doReturn(String.valueOf(CrossingServiceImplTest.NEXT_NUMBER)).when(this.germplasmDataManager)
				.getNextSequenceNumberForCrossName(Matchers.anyString(), Matchers.anyString());
		
		this.localUserId = new Random().nextInt(Integer.MAX_VALUE);
		Mockito.doReturn(this.localUserId).when(this.contextUtil).getCurrentUserLocalId();
	}

	private Project createProject() {
		final Project project = new Project();
		project.setCropType(new CropType("maize"));
		return project;
	}

	@Test
	public void testProcessCrossBreedingMethodCodeAlreadyAvailable() {
		final List<ImportedCrosses> crosses = this.importedCrossesList.getImportedCrosses();

		this.crossSetting.getBreedingMethodSetting().setBasedOnImportFile(true);

		// we modify the data such that one of the entries already have a raw
		// breeding method code (i.e., from import file)
		crosses.get(0).setRawBreedingMethod(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE);
		final Method method = new Method(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);
		Mockito.doReturn(method).when(this.germplasmDataManager).getMethodByCode(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE);

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		Assert.assertEquals("Raw breeding method codes after processing should resolve to breeding method IDs in the imported cross",
				CrossingServiceImplTest.TEST_BREEDING_METHOD_ID, crosses.get(0).getBreedingMethodId());
	}

	@Test
	public void testProcessCrossBreedingMethodIDAlreadyAvailable() {

		final List<ImportedCrosses> crosses = this.importedCrossesList.getImportedCrosses();

		this.crossSetting.getBreedingMethodSetting().setBasedOnImportFile(true);
		final Method breedingMethod = new Method();
		breedingMethod.setMid(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);
		Mockito.when(this.germplasmDataManager.getMethodByCode(Matchers.anyString())).thenReturn(breedingMethod);

		for (final ImportedCrosses cross : crosses) {
			cross.setRawBreedingMethod(String.valueOf(CrossingServiceImplTest.TEST_BREEDING_METHOD_CODE));
		}

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		for (final ImportedCrosses cross : crosses) {
			Assert.assertEquals("Breeding method ID should not be overridden if it is already present in the imported cross info",
					CrossingServiceImplTest.TEST_BREEDING_METHOD_ID.intValue(), cross.getBreedingMethodId().intValue());
		}
	}

	@Test
	public void testProcessCrossBreedingMethodUseSetting() {
		this.crossSetting.getBreedingMethodSetting().setMethodId(CrossingServiceImplTest.TEST_BREEDING_METHOD_ID);

		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		for (final ImportedCrosses importedCrosses : this.importedCrossesList.getImportedCrosses()) {
			Assert.assertEquals("User provided breeding method must be applied to all objects",
					CrossingServiceImplTest.TEST_BREEDING_METHOD_ID, importedCrosses.getBreedingMethodId());
		}
	}

	@Test
	public void testProcessCrossBreedingMethodNoSetting() {
		this.crossSetting.getBreedingMethodSetting().setMethodId(null);
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);

		this.setupMockCallsForGermplasm(CrossingServiceImplTest.TEST_FEMALE_GID_1);
		this.setupMockCallsForGermplasm(CrossingServiceImplTest.TEST_MALE_GID_1);
		this.setupMockCallsForGermplasm(CrossingServiceImplTest.TEST_FEMALE_GID_2);
		this.setupMockCallsForGermplasm(CrossingServiceImplTest.TEST_MALE_GID_2);

		for (final ImportedCrosses importedCrosses : this.importedCrossesList.getImportedCrosses()) {
			Assert.assertNotNull(
					"A method based on parental lines must be assigned to germplasms if user does not select a breeding method",
					importedCrosses.getBreedingMethodId());
			Assert.assertNotSame(
					"A method based on parental lines must be assigned to germplasms if user does not select a breeding method", 0,
					importedCrosses.getBreedingMethodId());
		}
	}

	void setupMockCallsForGermplasm(final Integer gid) {
		final Germplasm germplasm = new Germplasm(gid);
		germplasm.setGnpgs(-1);
	}

	@Test
	public void testApplyCrossSetting() throws MiddlewareQueryException {

		final CrossNameSetting crossNameSetting = this.crossSetting.getCrossNameSetting();
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		this.crossingService.applyCrossSetting(this.crossSetting, this.importedCrossesList, CrossingServiceImplTest.USER_ID, null);

		final ImportedCrosses cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		Assert.assertEquals(CrossingServiceImplTest.SAVED_CROSSES_GID1, cross1.getGid());
		Assert.assertEquals(crossNameSetting.getPrefix() + " 0000100 " + crossNameSetting.getSuffix(), cross1.getDesig());
		Assert.assertEquals((Integer) 1, cross1.getEntryId());
		Assert.assertEquals("1", cross1.getEntryCode());
		Assert.assertEquals(null, cross1.getNames().get(0).getGermplasmId());
		Assert.assertEquals((Integer) 0, cross1.getNames().get(0).getLocationId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, cross1.getNames().get(0).getUserId());

		final ImportedCrosses cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertEquals(CrossingServiceImplTest.SAVED_CROSSES_GID2, cross2.getGid());
		Assert.assertEquals(crossNameSetting.getPrefix() + " 0000101 " + crossNameSetting.getSuffix(), cross2.getDesig());
		Assert.assertEquals((Integer) 2, cross2.getEntryId());
		Assert.assertEquals("2", cross2.getEntryCode());
		Assert.assertEquals(null, cross2.getNames().get(0).getGermplasmId());
		Assert.assertEquals((Integer) 0, cross2.getNames().get(0).getLocationId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, cross2.getNames().get(0).getUserId());

	}

	@Test
	public void testApplyCrossNameSettingToImportedCrosses() throws MiddlewareQueryException {

		final CrossNameSetting setting = this.crossSetting.getCrossNameSetting();

		this.crossingService.applyCrossNameSettingToImportedCrosses(this.crossSetting, this.importedCrossesList.getImportedCrosses());

		final ImportedCrosses cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		Assert.assertEquals(null, cross1.getGid());
		Assert.assertEquals(setting.getPrefix() + " 0000100 " + setting.getSuffix(), cross1.getDesig());
		Assert.assertEquals(cross1.getFemaleDesignation() + setting.getSeparator() + cross1.getMaleDesignationsAsString(), cross1.getCross());
		Assert.assertEquals((Integer) 1, cross1.getEntryId());
		Assert.assertEquals("1", cross1.getEntryCode());

		final ImportedCrosses cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertEquals(null, cross2.getGid());
		Assert.assertEquals(setting.getPrefix() + " 0000101 " + setting.getSuffix(), cross2.getDesig());
		Assert.assertEquals(cross2.getFemaleDesignation() + setting.getSeparator() + cross2.getMaleDesignationsAsString(), cross2.getCross());
		Assert.assertEquals((Integer) 2, cross2.getEntryId());
		Assert.assertEquals("2", cross2.getEntryCode());
	}

	@Test
	public void testApplyCrossSetting_WhenSavingOfParentageDesignationNameIsSetToTrue() {
		final List<Integer> savedGermplasmIds = new ArrayList<Integer>();
		savedGermplasmIds.add(1);
		savedGermplasmIds.add(2);

		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		crossNameSetting.setSaveParentageDesignationAsAString(true);

		this.crossSetting.setCrossNameSetting(crossNameSetting);
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		this.crossingService
				.applyCrossSetting(this.crossSetting, this.importedCrossesList, CrossingServiceImplTest.USER_ID, new Workbook());

		// TODO prepare descriptive messages for verification failure once
		// Mockito has stable 2.0 version
		Mockito.verify(this.germplasmDataManager, Mockito.atLeastOnce()).addGermplasmName(Matchers.any(List.class));

	}

	@Test
	public void testApplyCrossSetting_WhenSavingOfParentageDesignationNameIsSetToFalse() {
		final List<Integer> savedGermplasmIds = new ArrayList<Integer>();
		savedGermplasmIds.add(1);
		savedGermplasmIds.add(2);

		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		crossNameSetting.setSaveParentageDesignationAsAString(false);

		this.crossSetting.setCrossNameSetting(crossNameSetting);
		this.crossingService.processCrossBreedingMethod(this.crossSetting, this.importedCrossesList);
		this.crossingService
				.applyCrossSetting(this.crossSetting, this.importedCrossesList, CrossingServiceImplTest.USER_ID, new Workbook());

		Mockito.verify(this.germplasmDataManager, Mockito.never()).addGermplasmName(ArgumentMatchers.<List<Name>>any());

	}

	@Test
	public void testBuildCrossName() {

		final CrossNameSetting setting = this.createCrossNameSetting();
		final ImportedCrosses cross = this.createCross();
		final String crossName = this.crossingService.buildCrossName(cross, setting.getSeparator());

		Assert.assertEquals(cross.getFemaleDesignation() + setting.getSeparator() + cross.getMaleDesignationsAsString(), crossName);

	}

	@Test
	public void testFormatHarvestDate() {
		Assert.assertTrue(new Integer(20150500).equals(this.crossingService.getFormattedHarvestDate("2015-05")));
	}

	@Test
	public void testPopulateGermplasmdateWithHarvestDate() {
		final Germplasm germplasm = new Germplasm();
		String harvestedDate = "2015-06";
		this.crossingService.populateGermplasmDate(germplasm, harvestedDate);
		harvestedDate = harvestedDate.replace("-", "");
		harvestedDate += "00";
		Assert.assertEquals(germplasm.getGdate(), new Integer(harvestedDate));
	}

	@Test
	public void testPopulateGermplasmdateWithCurrentDate() {
		final Germplasm germplasm = new Germplasm();
		this.crossingService.populateGermplasmDate(germplasm, "");
		Assert.assertEquals(germplasm.getGdate(), DateUtil.getCurrentDateAsIntegerValue());
	}

	@Test
	public void testBuildDesignationNameInSequenceDefaultSetting() {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("A");
		setting.setSuffix("B");

		final String designationName = this.crossingService.buildDesignationNameInSequence(1, setting);
		Assert.assertEquals("A1B", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceWithSpacesInPrefixSuffix() {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("A");
		setting.setSuffix("B");
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);

		final String designationName = this.crossingService.buildDesignationNameInSequence(1, setting);
		Assert.assertEquals("A 1 B", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceWithNumOfDigits() {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setNumOfDigits(3);
		setting.setPrefix("A");
		setting.setSuffix("B");

		final String designationName = this.crossingService.buildDesignationNameInSequence(1, setting);
		Assert.assertEquals("A 001 B", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceSuffixIsAvailable() throws RuleException {
		final String specifiedSuffix = "AAA";

		final CrossNameSetting crossNameSetting = new CrossNameSetting();
		crossNameSetting.setSuffix(specifiedSuffix);

		final int sequenceNumber = 1;
		final String designationName = this.crossingService.buildDesignationNameInSequence(sequenceNumber, crossNameSetting);

		final String expectedResult = sequenceNumber + specifiedSuffix;
		Assert.assertEquals("The designation name should be " + expectedResult, expectedResult, designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceSuffixAndPrefixAreAvailable() throws RuleException {
		final String specifiedSuffix = "AAA";
		final CrossNameSetting crossNameSetting = new CrossNameSetting();
		crossNameSetting.setSuffix(specifiedSuffix);
		final String prefix = "B";
		crossNameSetting.setPrefix(prefix);

		final int sequenceNumber = 1;
		final String designationName = this.crossingService.buildDesignationNameInSequence(sequenceNumber, crossNameSetting);

		final String expectedResult = prefix + sequenceNumber + specifiedSuffix;
		Assert.assertEquals("The designation name should be " + expectedResult, expectedResult, designationName);
	}

	@Test
	public void testBuildPrefixStringDefault() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix(" A  ");
		final String prefix = this.crossingService.buildPrefixString(setting);

		Assert.assertEquals("A", prefix);
	}

	@Test
	public void testBuildPrefixStringWithSpace() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("   A");
		setting.setAddSpaceBetweenPrefixAndCode(true);
		final String prefix = this.crossingService.buildPrefixString(setting);

		Assert.assertEquals("A ", prefix);
	}

	@Test
	public void testBuildSuffixStringDefault() {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setSuffix("  B   ");
		final String suffix = this.crossingService.buildSuffixString(setting, setting.getSuffix());

		Assert.assertEquals("B", suffix);
	}

	@Test
	public void testBuildSuffixStringWithSpace() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setSuffix("   B   ");
		setting.setAddSpaceBetweenSuffixAndCode(true);
		final String suffix = this.crossingService.buildSuffixString(setting, setting.getSuffix());

		Assert.assertEquals(" B", suffix);
	}

	@Test
	public void testGenerateGermplasmNamePairs() throws MiddlewareQueryException {

		final CrossSetting crossSetting = new CrossSetting();
		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		final BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting();
		final AdditionalDetailsSetting additionalDetailsSetting = this.createAdditionalDetailsSetting();
		crossSetting.setCrossNameSetting(crossNameSetting);
		crossSetting.setBreedingMethodSetting(breedingMethodSetting);
		crossSetting.setAdditionalDetailsSetting(additionalDetailsSetting);

		final CrossingServiceImpl.GermplasmListResult result = this.crossingService
				.generateGermplasmNamePairs(crossSetting, this.importedCrossesList.getImportedCrosses(), CrossingServiceImplTest.USER_ID,
						false);

		Pair<Germplasm, Name> germplasmNamePair = result.getGermplasmPairs().get(0);
		final Germplasm germplasm1 = germplasmNamePair.getLeft();
		final Name name1 = germplasmNamePair.getRight();
		final ImportedCrosses cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		Assert.assertTrue(result.getIsTrimed());
		Assert.assertNull(germplasm1.getGid());
		Assert.assertEquals(20150101, germplasm1.getGdate().intValue());
		Assert.assertEquals(2, germplasm1.getGnpgs().intValue());
		Assert.assertEquals(cross1.getFemaleGid(), germplasm1.getGpid1().toString());
		Assert.assertEquals(cross1.getMaleGids().get(0), germplasm1.getGpid2().toString());
		Assert.assertEquals(0, germplasm1.getGrplce().intValue());
		Assert.assertEquals(0, germplasm1.getLgid().intValue());
		Assert.assertEquals(0, germplasm1.getGrplce().intValue());
		Assert.assertEquals(0, germplasm1.getLgid().intValue());
		Assert.assertEquals(99, germplasm1.getLocationId().intValue());
		Assert.assertEquals(0, germplasm1.getMgid().intValue());
		Assert.assertNull(germplasm1.getPreferredAbbreviation());
		Assert.assertNull(germplasm1.getPreferredName());
		Assert.assertEquals(0, germplasm1.getReferenceId().intValue());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, germplasm1.getUserId());

		Assert.assertEquals(null, name1.getGermplasmId());
		Assert.assertEquals(99, name1.getLocationId().intValue());
		Assert.assertEquals(20150101, name1.getNdate().intValue());
		Assert.assertEquals(null, name1.getNid());
		Assert.assertEquals(null, name1.getNstat());
		Assert.assertFalse(name1.getNval().contains("(truncated)"));
		Assert.assertEquals(0, name1.getReferenceId().intValue());
		Assert.assertEquals(null, name1.getTypeId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, name1.getUserId());

		germplasmNamePair = result.getGermplasmPairs().get(1);
		final Germplasm germplasm2 = germplasmNamePair.getLeft();
		final Name name2 = germplasmNamePair.getRight();
		final ImportedCrosses cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertNull(null, germplasm2.getGid());
		Assert.assertEquals(20150101, germplasm2.getGdate().intValue());
		Assert.assertEquals(2, germplasm2.getGnpgs().intValue());
		Assert.assertEquals(cross2.getFemaleGid(), germplasm2.getGpid1().toString());
		Assert.assertEquals(cross2.getMaleGids().get(0), germplasm2.getGpid2().toString());
		Assert.assertEquals(0, germplasm2.getGrplce().intValue());
		Assert.assertEquals(0, germplasm2.getLgid().intValue());
		Assert.assertEquals(0, germplasm2.getGrplce().intValue());
		Assert.assertEquals(0, germplasm2.getLgid().intValue());
		Assert.assertEquals(99, germplasm2.getLocationId().intValue());
		Assert.assertEquals(0, germplasm2.getMgid().intValue());
		Assert.assertNull(null, germplasm2.getPreferredAbbreviation());
		Assert.assertNull(null, germplasm2.getPreferredName());
		Assert.assertEquals(0, germplasm2.getReferenceId().intValue());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, germplasm2.getUserId());

		Assert.assertEquals(null, name2.getGermplasmId());
		Assert.assertEquals(99, name2.getLocationId().intValue());
		Assert.assertEquals(20150101, name2.getNdate().intValue());
		Assert.assertEquals(null, name2.getNid());
		Assert.assertEquals(null, name2.getNstat());
		Assert.assertTrue(name2.getNval().contains("(truncated)"));
		Assert.assertEquals(0, name2.getReferenceId().intValue());
		Assert.assertEquals(null, name2.getTypeId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, name2.getUserId());
	}

	@Test
	public void testGetNextNumberInSequenceDefault() {
		final CrossNameSetting setting = new CrossNameSetting();
		final String prefix = "A";
		setting.setPrefix(prefix);

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);
		Assert.assertEquals(CrossingServiceImplTest.NEXT_NUMBER.intValue(), nextNumber);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> suffixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmDataManager).getNextSequenceNumberForCrossName(prefixCaptor.capture(), suffixCaptor.capture());
		Assert.assertEquals(prefix, prefixCaptor.getValue());
		Assert.assertEquals("", suffixCaptor.getValue());
	}

	@Test
	public void testGetNextNumberInSequenceStartNumberIsSpecified() {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("A");

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);
		Assert.assertEquals(CrossingServiceImplTest.NEXT_NUMBER.intValue(), nextNumber);
	}

	@Test
	public void testGetNextNumberInSequenceStartNumberIsNotSpecified() {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(0);
		setting.setPrefix("A");

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);

		Assert.assertEquals(CrossingServiceImplTest.NEXT_NUMBER.intValue(), nextNumber);
	}

	@Test
	public void testGetNextNumberInSequenceWhenPrefixIsEmpty() {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("");

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);
		Assert.assertEquals(1, nextNumber);
		Mockito.verify(this.germplasmDataManager, Mockito.never())
				.getNextSequenceNumberForCrossName(Matchers.anyString(), Matchers.anyString());
	}

	@Test
	public void testGetNextNumberInSequenceWhenSuffixIsSupplied() {

		final CrossNameSetting setting = new CrossNameSetting();
		final String prefix = "A";
		setting.setPrefix(prefix);
		final String suffix = "CDE";
		setting.setSuffix(suffix);

		this.crossingService.getNextNumberInSequence(setting);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> suffixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmDataManager).getNextSequenceNumberForCrossName(prefixCaptor.capture(), suffixCaptor.capture());
		Assert.assertEquals(prefix, prefixCaptor.getValue());
		Assert.assertEquals(suffix, suffixCaptor.getValue());
	}

	@Test
	public void testGetNextNumberInSequenceWhenSpaceSuppliedBetweenPrefixAndCode() {

		final CrossNameSetting setting = new CrossNameSetting();
		final String prefix = "A";
		setting.setPrefix(prefix);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		final String suffix = "CDE";
		setting.setSuffix(suffix);

		this.crossingService.getNextNumberInSequence(setting);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> suffixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmDataManager).getNextSequenceNumberForCrossName(prefixCaptor.capture(), suffixCaptor.capture());
		Assert.assertEquals(prefix + " ", prefixCaptor.getValue());
		Assert.assertEquals(suffix, suffixCaptor.getValue());
	}

	@Test
	public void testGetNextNumberInSequenceWhenSpaceSuppliedBetweenSuffixAndCode() {

		final CrossNameSetting setting = new CrossNameSetting();
		final String prefix = "A";
		setting.setPrefix(prefix);
		final String suffix = "CDE";
		setting.setSuffix(suffix);
		setting.setAddSpaceBetweenSuffixAndCode(true);

		this.crossingService.getNextNumberInSequence(setting);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> suffixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmDataManager).getNextSequenceNumberForCrossName(prefixCaptor.capture(), suffixCaptor.capture());
		Assert.assertEquals(prefix, prefixCaptor.getValue());
		Assert.assertEquals(" " + suffix, suffixCaptor.getValue());
	}

	@Test
	public void testGetNextNumberInSequenceWhenSpaceSuppliedAfterPrefixAndBeforeSuffix() {

		final CrossNameSetting setting = new CrossNameSetting();
		final String prefix = "A";
		setting.setPrefix(prefix);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		final String suffix = "CDE";
		setting.setSuffix(suffix);
		setting.setAddSpaceBetweenSuffixAndCode(true);

		this.crossingService.getNextNumberInSequence(setting);
		final ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> suffixCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmDataManager).getNextSequenceNumberForCrossName(prefixCaptor.capture(), suffixCaptor.capture());
		Assert.assertEquals(prefix + " ", prefixCaptor.getValue());
		Assert.assertEquals(" " + suffix, suffixCaptor.getValue());
	}

	@Test
	public void testGetNumberWithLeadingZeroesAsStringDefault() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setNumOfDigits(0);
		final String formattedString = this.crossingService.getNumberWithLeadingZeroesAsString(1, setting);

		Assert.assertEquals("1", formattedString);
	}

	@Test
	public void testGetNumberWithLeadingZeroesAsStringWithNumOfDigitsSpecified() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setNumOfDigits(8);
		final String formattedString = this.crossingService.getNumberWithLeadingZeroesAsString(1, setting);

		Assert.assertEquals("00000001", formattedString);
	}

	@Test
	public void testGenerateSeedSource() {
		final String newSeedSource = "newSeedSource";
		Mockito.doReturn(newSeedSource).when(this.seedSourceGenertor)
				.generateSeedSourceForCross(Matchers.any(Workbook.class), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String>isNull(),
						ArgumentMatchers.<String>isNull(), ArgumentMatchers.<Workbook>isNull());

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook();
		// Case 1 - No seed source present. Generate new.
		final ImportedCrosses importedCross1 = new ImportedCrosses();
		importedCross1.setSource(null);
		this.crossingService.populateSeedSource(importedCross1, workbook, new HashMap<String, Workbook>());
		Assert.assertEquals(newSeedSource, importedCross1.getSource());

		// Case 2 - Seed source is present. Keep.
		final ImportedCrosses importedCross2 = new ImportedCrosses();
		final String existingSeedSource = "existingSeedSource";
		importedCross2.setSource(existingSeedSource);
		this.crossingService.populateSeedSource(importedCross2, workbook, new HashMap<String, Workbook>());
		Assert.assertEquals(existingSeedSource, importedCross2.getSource());

		// Case 3 - Seed source is presend but is PENDING indicator. Generate
		// new.
		final ImportedCrosses importedCross3 = new ImportedCrosses();
		importedCross3.setSource(ImportedCrosses.SEED_SOURCE_PENDING);
		this.crossingService.populateSeedSource(importedCross3, workbook, new HashMap<String, Workbook>());
		Assert.assertEquals(newSeedSource, importedCross3.getSource());

		// Case 4 - Seed source is present but empty string. Generate new.
		final ImportedCrosses importedCross4 = new ImportedCrosses();
		importedCross4.setSource("");
		this.crossingService.populateSeedSource(importedCross4, workbook, new HashMap<String, Workbook>());
		Assert.assertEquals(newSeedSource, importedCross4.getSource());

	}

	@Test
	public void testApplyCrossSettingWithNamingRules() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final ImportedCrossesList importedCrossesList = this.createImportedCrossesList();
		final Integer userId = 123456;
		final Workbook workbook = new Workbook();

		this.importedCrossesList.addImportedCrosses(this.createCross());
		this.importedCrossesList.addImportedCrosses(this.createSecondCross());

		this.crossingService.applyCrossSettingWithNamingRules(crossSetting, importedCrossesList, userId, workbook);

		int counter = 1;
		for (final ImportedCrosses importedCross : importedCrossesList.getImportedCrosses()) {
			Assert.assertEquals(importedCross.getEntryCode(), importedCross.getEntryId());
			Assert.assertTrue(importedCross.getEntryCode().equals(counter));
			Assert.assertTrue(importedCross.getEntryId().equals(counter));
			counter++;
		}
	}

	@Test
	public void testGetNextNameInSequence() throws InvalidInputException {
		final String nextNameInSequence = this.crossingService.getNextNameInSequence(this.crossSetting.getCrossNameSetting());
		Assert.assertEquals(CrossingServiceImplTest.PREFIX + " 0000100 " + CrossingServiceImplTest.SUFFIX, nextNameInSequence);
	}

	@Test
	public void testGetNextNameInSequenceWhenSpecifiedSequenceStartingNumberIsGreater() throws InvalidInputException {
		this.crossSetting.getCrossNameSetting().setStartNumber(1000);

		final String nextNameInSequence = this.crossingService.getNextNameInSequence(this.crossSetting.getCrossNameSetting());
		Assert.assertEquals("The specified starting sequence number will be used since it's larger.",
				CrossingServiceImplTest.PREFIX + " 0001000 " + CrossingServiceImplTest.SUFFIX, nextNameInSequence);
	}

	@Test
	public void testGetNextNameInSequenceWhenSpecifiedSequenceStartingNumberIsLower() {
		this.crossSetting.getCrossNameSetting().setPrefix("ABC");
		this.crossSetting.getCrossNameSetting().setStartNumber(1);

		Mockito.when(this.messageSource.getMessage(Matchers.isA(String.class), Matchers.any(Object[].class), Matchers.isA(Locale.class)))
				.thenReturn("The starting sequence number specified will generate conflict with already existing cross codes.");

		try {
			this.crossingService.getNextNameInSequence(this.crossSetting.getCrossNameSetting());
			Assert.fail("Should have thrown InvalidInputException but did not.");
		} catch (final InvalidInputException e) {
			Assert.assertEquals("The starting sequence number specified will generate conflict with already existing cross codes.",
					e.getMessage());
		}
	}
	
	@Test
	public void testGetStartingSequenceNumberWhereCrossSettingStartNumberHasValue() {
		this.crossSetting.getCrossNameSetting().setStartNumber(10);
		final Integer startingSequenceNumber = this.crossingService.getStartingSequenceNumber(this.crossSetting.getCrossNameSetting());
		Assert.assertEquals("The starting sequence number should be " + startingSequenceNumber, this.crossSetting.getCrossNameSetting().getStartNumber(), startingSequenceNumber); 
	}
	
	@Test
	public void testGetStartingSequenceNumberWhereCrossSettingStartNumberIsNull() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(null);
		setting.setPrefix("A");

		final int startingSequenceNumber = this.crossingService.getStartingSequenceNumber(setting);
		Assert.assertEquals("The starting sequence number should be " + startingSequenceNumber, CrossingServiceImplTest.NEXT_NUMBER.intValue(), startingSequenceNumber); 
	}
	
	@Test
	public void testGetStartingSequenceNumberWhereCrossSettingStartNumberIsZero() {
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(0);
		setting.setPrefix("A");

		final int startingSequenceNumber = this.crossingService.getStartingSequenceNumber(setting);
		Assert.assertEquals("The starting sequence number should be " + startingSequenceNumber, CrossingServiceImplTest.NEXT_NUMBER.intValue(), startingSequenceNumber); 
	}
	
	@Test
	public void testSaveAttributes() {
		final List<Integer> germplasmIds = Arrays.asList(101, 102);
		this.crossingService.saveAttributes(crossSetting, importedCrossesList, germplasmIds);
		Mockito.verify(this.germplasmDataManager).addAttributes(attributesListCaptor.capture());
		
		final List<Attribute> attributesList = attributesListCaptor.getValue();
		Assert.assertEquals(germplasmIds.size(), attributesList.size());
		final Iterator<Integer> idsIterator = germplasmIds.iterator();
		final Iterator<ImportedCrosses> crossesIterator = importedCrossesList.getImportedCrosses().iterator();
		for (final Attribute attribute : attributesList) {
			final Integer gid = idsIterator.next();
			final ImportedCrosses cross = crossesIterator.next();
			this.verifyPlotCodeAttributeValues(attribute, gid, cross);
		}
	}
	
	@Test
	public void testSaveAttributesWhenMergingPlotDuplicates() {
		final ImportedCrosses secondCross = this.importedCrossesList.getImportedCrosses().get(1);
		// Set 2nd cross as plot duplicate of first cross
		secondCross.setDuplicateEntries(new HashSet<>(Arrays.asList(1)));
		secondCross.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		this.crossSetting.setPreservePlotDuplicates(false);
		
		final List<Integer> germplasmIds = Arrays.asList(101, 102);
		this.crossingService.saveAttributes(crossSetting, importedCrossesList, germplasmIds);
		Mockito.verify(this.germplasmDataManager).addAttributes(attributesListCaptor.capture());
		final List<Attribute> attributesList = attributesListCaptor.getValue();
		Assert.assertEquals("Attribute will be saved only for first entry",  1, attributesList.size());
		final ImportedCrosses firstCross = this.importedCrossesList.getImportedCrosses().get(0);
		this.verifyPlotCodeAttributeValues(attributesList.get(0), germplasmIds.get(0),
				firstCross);
		// Verify that gid, cross and designation from 1st cross was copied to 2nd cross
		Assert.assertEquals(firstCross.getGid(), secondCross.getGid());
		Assert.assertEquals(firstCross.getCross(), secondCross.getCross());
		Assert.assertEquals(firstCross.getDesig(), secondCross.getDesig());
	}
	
	@Test
	public void testSaveAttributesWhenPreservingPlotDuplicates() {
		final ImportedCrosses secondCross = this.importedCrossesList.getImportedCrosses().get(1);
		// Set 2nd cross as plot duplicate of first cross
		secondCross.setDuplicateEntries(new HashSet<>(Arrays.asList(1)));
		secondCross.setDuplicatePrefix(ImportedCrosses.PLOT_DUPE_PREFIX);
		this.crossSetting.setPreservePlotDuplicates(true);
		
		final List<Integer> germplasmIds = Arrays.asList(101, 102);
		this.crossingService.saveAttributes(crossSetting, importedCrossesList, germplasmIds);
		Mockito.verify(this.germplasmDataManager).addAttributes(attributesListCaptor.capture());
		final List<Attribute> attributesList = attributesListCaptor.getValue();
		Assert.assertEquals("Expecting plot duplicate crosses to be preserved", germplasmIds.size(), attributesList.size());
		final Iterator<Integer> idsIterator = germplasmIds.iterator();
		final Iterator<ImportedCrosses> crossesIterator = importedCrossesList.getImportedCrosses().iterator();
		for (final Attribute attribute : attributesList) {
			final Integer gid = idsIterator.next();
			final ImportedCrosses cross = crossesIterator.next();
			this.verifyPlotCodeAttributeValues(attribute, gid, cross);
		}
	}

	private void verifyPlotCodeAttributeValues(final Attribute attribute, final Integer gid, final ImportedCrosses cross) {
		Assert.assertEquals(Integer.valueOf(DateUtil.getCurrentDateAsStringValue()), attribute.getAdate());
		Assert.assertEquals(gid, attribute.getGermplasmId());
		Assert.assertEquals(gid.toString(), cross.getGid());
		Assert.assertEquals(cross.getSource(), attribute.getAval());
		Assert.assertEquals(PLOT_CODE_FLD_NO, attribute.getTypeId().intValue());
		Assert.assertEquals(this.localUserId, attribute.getUserId());
	}
	
	
	private ImportedCrossesList createImportedCrossesList() {

		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		importedCrossesList.setImportedGermplasms(importedCrosses);
		return importedCrossesList;

	}

	private List<ImportedCrosses> createImportedCrosses() {

		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		final ImportedCrosses cross = new ImportedCrosses();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_FEMALE_GID_1, "FEMALE-12345", "");
		cross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_MALE_GID_1, "MALE-54321", "");
		cross.setMaleParents(Lists.newArrayList(maleParent));
		cross.setCross("CROSS 1");
		cross.setSource("MALE:1:FEMALE:1");
		cross.setDesig(
				"G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML150xCLG2501)-B-31-1-B-1-BBB/CML193-BB)-B-1-BB(NonQ)-B*8)-B/((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML161xCML451)-B-18-1-BBB/CML1612345");
		cross.setEntryId(1);
		importedCrosses.add(cross);
		final ImportedCrosses cross2 = this.createSecondCross();
		importedCrosses.add(cross2);

		return importedCrosses;

	}

	private ImportedCrosses createSecondCross() {
		final ImportedCrosses cross2 = new ImportedCrosses();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_FEMALE_GID_2, "FEMALE-9999", "");
		cross2.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_MALE_GID_2, "MALE-8888", "");
		cross2.setMaleParents(Lists.newArrayList(maleParent));
		cross2.setCross("CROSS 2");
		cross2.setSource("MALE:2:FEMALE:2");
		cross2.setDesig(
				"((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML150xCLG2501)-B-31-1-B-1-BBB/CML193-BB)-B-1-BB(NonQ)-B*8)-B((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML150xCLG2501)-B-31-1-B-1-BBB/CML193-BB)-B-1-BB(NonQ)-B*8)-B/((G9BC0RL34-1P-5P-2-1P-3P-B/G9BC1TSR8P-1P-1P-5P-3P-1P-1P)-3-1-1-1-B*8/((CML161xCML451)-B-18-1-BBB/CML161");
		cross2.setEntryId(2);
		return cross2;
	}

	private ImportedCrosses createCross() {
		final ImportedCrosses cross = new ImportedCrosses();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_FEMALE_GID_1, "FEMALE-12345", "");
		cross.setFemaleParent(femaleParent);
		final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(CrossingServiceImplTest.TEST_MALE_GID_1, "MALE-54321", "");
		cross.setMaleParents(Lists.newArrayList(maleParent));
		cross.setDesig("Cros12345");
		return cross;
	}

	private CrossSetting createCrossSetting() {
		return new CrossSetting(null, null, this.createCrossNameSetting(), this.createAdditionalDetailsSetting());
	}

	private CrossNameSetting createCrossNameSetting() {
		final CrossNameSetting setting = new CrossNameSetting();

		setting.setPrefix(CrossingServiceImplTest.PREFIX);
		setting.setSuffix(CrossingServiceImplTest.SUFFIX);
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setSeparator("|");
		setting.setStartNumber(100);
		setting.setNumOfDigits(7);

		return setting;
	}

	private AdditionalDetailsSetting getAdditionalDetailsSetting() {
		return new AdditionalDetailsSetting();
	}

	private BreedingMethodSetting createBreedingMethodSetting() {
		final BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting();
		breedingMethodSetting.setMethodId(CrossingServiceImplTest.BREEDING_METHOD_ID);
		breedingMethodSetting.setBasedOnImportFile(false);
		breedingMethodSetting.setBasedOnStatusOfParentalLines(false);
		return breedingMethodSetting;
	}

	private AdditionalDetailsSetting createAdditionalDetailsSetting() {
		final AdditionalDetailsSetting setting = new AdditionalDetailsSetting();

		setting.setHarvestDate("20150101");
		setting.setHarvestLocationId(99);

		return setting;
	}

	private List<UserDefinedField> createNameTypes() {
		final List<UserDefinedField> nameTypes = new ArrayList<>();
		final UserDefinedField udf = new UserDefinedField();
		udf.setFcode(CrossingServiceImpl.USER_DEF_FIELD_CROSS_NAME[0]);
		nameTypes.add(udf);
		return nameTypes;
	}

	private List<Integer> createGermplasmIds() {
		final List<Integer> ids = new ArrayList<>();
		ids.add(Integer.valueOf(CrossingServiceImplTest.SAVED_CROSSES_GID1));
		ids.add(Integer.valueOf(CrossingServiceImplTest.SAVED_CROSSES_GID2));
		return ids;
	}

}
