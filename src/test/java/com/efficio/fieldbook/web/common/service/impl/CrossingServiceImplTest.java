
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

public class CrossingServiceImplTest {

	private static final int BREEDING_METHOD_ID = 1;
	private static final String SAVED_CROSSES_GID1 = "-9999";
	private static final String SAVED_CROSSES_GID2 = "-8888";
	private static final Integer USER_ID = 123;
	private CrossingServiceImpl crossingService;
	private ImportedCrossesList importedCrossesList;

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

	private CrossSetting crossSetting;

	@Before
	public void setUp() throws MiddlewareQueryException {

		MockitoAnnotations.initMocks(this);

		this.importedCrossesList = this.createImportedCrossesList();
		this.importedCrossesList.setImportedGermplasms(this.createImportedCrosses());

		this.crossingService = Mockito.spy(new CrossingServiceImpl());
		this.crossingService.setGermplasmListManager(this.germplasmListManager);
		this.crossingService.setGermplasmDataManager(this.germplasmDataManager);
		this.crossingService.setCrossExpansionProperties(this.crossExpansionProperties);
		this.crossingService.setContextUtil(this.contextUtil);
		this.crossingService.setSeedSourceGenerator(this.seedSourceGenertor);
		Mockito.doReturn(this.createNameTypes()).when(this.germplasmListManager).getGermplasmNameTypes();
		Mockito.doReturn(this.createGermplasmIds()).when(this.germplasmDataManager).addGermplasm(Matchers.anyList());
		Mockito.doReturn(new Method()).when(this.germplasmDataManager).getMethodByName(Matchers.anyString());
		Mockito.doReturn(new Method()).when(this.germplasmDataManager).getMethodByID(BREEDING_METHOD_ID);
		Mockito.doReturn(this.createProject()).when(this.contextUtil).getProjectInContext();
		Mockito.doReturn("generatedSourceString")
				.when(this.seedSourceGenertor)
				.generateSeedSourceForCross(Mockito.any(Workbook.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
						Mockito.anyString());
		Mockito.doReturn(new UserDefinedField(1552)).when(this.germplasmDataManager).getPlotCodeField();

		this.crossSetting = new CrossSetting();
		this.crossSetting.setCrossNameSetting(this.createCrossNameSetting());
		this.crossSetting.setBreedingMethodSetting(this.createBreedingMethodSetting());
		this.crossSetting.setAdditionalDetailsSetting(this.getAdditionalDetailsSetting());
	}

	private Project createProject() {
		final Project project = new Project();
		project.setCropType(new CropType("maize"));
		return project;
	}

	@Test
	public void testApplyCrossSetting() throws MiddlewareQueryException {

		final CrossNameSetting crossNameSetting = this.crossSetting.getCrossNameSetting();

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
		Assert.assertEquals(cross1.getFemaleDesig() + setting.getSeparator() + cross1.getMaleDesig(), cross1.getCross());
		Assert.assertEquals((Integer) 1, cross1.getEntryId());
		Assert.assertEquals("1", cross1.getEntryCode());

		final ImportedCrosses cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertEquals(null, cross2.getGid());
		Assert.assertEquals(setting.getPrefix() + " 0000101 " + setting.getSuffix(), cross2.getDesig());
		Assert.assertEquals(cross2.getFemaleDesig() + setting.getSeparator() + cross2.getMaleDesig(), cross2.getCross());
		Assert.assertEquals((Integer) 2, cross2.getEntryId());
		Assert.assertEquals("2", cross2.getEntryCode());
	}

	@Test
	public void testApplyCrossSetting_WhenSavingOfParentageDesignationNameIsSetToTrue() {
		final List<Pair<Germplasm, Name>> germplasmPairs = new ArrayList<>();
		Mockito.doReturn(germplasmPairs)
				.when(this.crossingService)
				.generateGermplasmNamePairs(this.crossSetting, this.importedCrossesList.getImportedCrosses(),
						CrossingServiceImplTest.USER_ID, this.importedCrossesList.hasPlotDuplicate());

		final List<Integer> savedGermplasmIds = new ArrayList<Integer>();
		savedGermplasmIds.add(1);
		savedGermplasmIds.add(2);
		Mockito.doReturn(savedGermplasmIds).when(this.germplasmDataManager).addGermplasm(germplasmPairs);

		Mockito.doNothing().when(this.crossingService)
				.savePedigreeDesignationName(this.importedCrossesList, savedGermplasmIds, this.crossSetting);

		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		crossNameSetting.setSaveParentageDesignationAsAString(true);

		this.crossSetting.setCrossNameSetting(crossNameSetting);
		this.crossingService
				.applyCrossSetting(this.crossSetting, this.importedCrossesList, CrossingServiceImplTest.USER_ID, new Workbook());

		try {
			Mockito.verify(this.crossingService, Mockito.times(1)).savePedigreeDesignationName(this.importedCrossesList, savedGermplasmIds,
					this.crossSetting);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting to save parentage designation names but didn't.");
		}
	}

	@Test
	public void testApplyCrossSetting_WhenSavingOfParentageDesignationNameIsSetToFalse() {
		final List<Pair<Germplasm, Name>> germplasmPairs = new ArrayList<>();
		Mockito.doReturn(germplasmPairs)
				.when(this.crossingService)
				.generateGermplasmNamePairs(this.crossSetting, this.importedCrossesList.getImportedCrosses(),
						CrossingServiceImplTest.USER_ID, this.importedCrossesList.hasPlotDuplicate());

		final List<Integer> savedGermplasmIds = new ArrayList<Integer>();
		savedGermplasmIds.add(1);
		savedGermplasmIds.add(2);
		Mockito.doReturn(savedGermplasmIds).when(this.germplasmDataManager).addGermplasm(germplasmPairs);

		Mockito.doNothing().when(this.crossingService)
				.savePedigreeDesignationName(this.importedCrossesList, savedGermplasmIds, this.crossSetting);

		final CrossNameSetting crossNameSetting = this.createCrossNameSetting();
		crossNameSetting.setSaveParentageDesignationAsAString(false);

		this.crossSetting.setCrossNameSetting(crossNameSetting);
		this.crossingService
				.applyCrossSetting(this.crossSetting, this.importedCrossesList, CrossingServiceImplTest.USER_ID, new Workbook());

		try {
			Mockito.verify(this.crossingService, Mockito.times(0)).savePedigreeDesignationName(this.importedCrossesList, savedGermplasmIds,
					this.crossSetting);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting to NOT save parentage designation names but didn't.");
		}
	}

	@Test
	public void testBuildCrossName() {

		final CrossNameSetting setting = this.createCrossNameSetting();
		final ImportedCrosses cross = this.createCross();
		final String crossName = this.crossingService.buildCrossName(cross, setting.getSeparator());

		Assert.assertEquals(cross.getFemaleDesig() + setting.getSeparator() + cross.getMaleDesig(), crossName);

	}

	@Test
	public void testFormatHarvestDate() {
		Assert.assertTrue(new Integer(20150500).equals(this.crossingService.getFormattedHarvestDate("2015-05")));
	}

	@Test
	public void testPopulateGermplasmdateWithCrossingDate() {
		final Germplasm germplasm = new Germplasm();
		final String crossingDate = "20150303";
		this.crossingService.populateGermplasmDate(germplasm, crossingDate, "");
		Assert.assertEquals(germplasm.getGdate(), new Integer(crossingDate));
	}

	@Test
	public void testPopulateGermplasmdateWithHarvestDate() {
		final Germplasm germplasm = new Germplasm();
		String harvestedDate = "2015-06";
		this.crossingService.populateGermplasmDate(germplasm, "", harvestedDate);
		harvestedDate = harvestedDate.replace("-", "");
		harvestedDate += "00";
		Assert.assertEquals(germplasm.getGdate(), new Integer(harvestedDate));
	}

	@Test
	public void testPopulateGermplasmdateWithCurrentDate() {
		final Germplasm germplasm = new Germplasm();
		this.crossingService.populateGermplasmDate(germplasm, "", "");
		Assert.assertEquals(germplasm.getGdate(), DateUtil.getCurrentDateAsIntegerValue());
	}

	@Test
	public void testPopulateGermplasmdateWithBothDateReturnHarvestedDate() {
		final Germplasm germplasm = new Germplasm();
		String harvestedDate = "2015-06";
		this.crossingService.populateGermplasmDate(germplasm, "201509", harvestedDate);
		harvestedDate = harvestedDate.replace("-", "");
		harvestedDate += "00";
		Assert.assertEquals(germplasm.getGdate(), new Integer(harvestedDate));
	}

	@Test
	public void testBuildDesignationNameInSequenceDefaultSetting() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("A");
		setting.setSuffix("B");

		crossSetting.setCrossNameSetting(setting);
		final String designationName = this.crossingService.buildDesignationNameInSequence(null, 1, crossSetting);
		Assert.assertEquals("A1B", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceWithSpacesInPrefixSuffix() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("A");
		setting.setSuffix("B");
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);

		crossSetting.setCrossNameSetting(setting);
		final String designationName = this.crossingService.buildDesignationNameInSequence(null, 1, crossSetting);
		Assert.assertEquals("A 1 B", designationName);
	}

	@Test
	public void testBuildDesignationNameInSequenceWithNumOfDigits() {

		final CrossSetting crossSetting = this.createCrossSetting();
		final CrossNameSetting setting = crossSetting.getCrossNameSetting();
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setNumOfDigits(3);
		setting.setPrefix("A");
		setting.setSuffix("B");

		final String designationName = this.crossingService.buildDesignationNameInSequence(null, 1, crossSetting);
		Assert.assertEquals("A 001 B", designationName);
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

		final List<Pair<Germplasm, Name>> germplasmPairs =
				this.crossingService.generateGermplasmNamePairs(crossSetting, this.importedCrossesList.getImportedCrosses(),
						CrossingServiceImplTest.USER_ID, false);

		Pair<Germplasm, Name> germplasmNamePair = germplasmPairs.get(0);
		final Germplasm germplasm1 = germplasmNamePair.getLeft();
		final Name name1 = germplasmNamePair.getRight();
		final ImportedCrosses cross1 = this.importedCrossesList.getImportedCrosses().get(0);

		Assert.assertNull(germplasm1.getGid());
		Assert.assertEquals(20150101, germplasm1.getGdate().intValue());
		Assert.assertEquals(2, germplasm1.getGnpgs().intValue());
		Assert.assertEquals(cross1.getFemaleGid(), germplasm1.getGpid1().toString());
		Assert.assertEquals(cross1.getMaleGid(), germplasm1.getGpid2().toString());
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
		Assert.assertEquals(null, name1.getNval());
		Assert.assertEquals(0, name1.getReferenceId().intValue());
		Assert.assertEquals(null, name1.getTypeId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, name1.getUserId());

		germplasmNamePair = germplasmPairs.get(1);
		final Germplasm germplasm2 = germplasmNamePair.getLeft();
		final Name name2 = germplasmNamePair.getRight();
		final ImportedCrosses cross2 = this.importedCrossesList.getImportedCrosses().get(1);

		Assert.assertNull(null, germplasm2.getGid());
		Assert.assertEquals(20150101, germplasm2.getGdate().intValue());
		Assert.assertEquals(2, germplasm2.getGnpgs().intValue());
		Assert.assertEquals(cross2.getFemaleGid(), germplasm2.getGpid1().toString());
		Assert.assertEquals(cross2.getMaleGid(), germplasm2.getGpid2().toString());
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
		Assert.assertEquals(null, name1.getNval());
		Assert.assertEquals(0, name2.getReferenceId().intValue());
		Assert.assertEquals(null, name2.getTypeId());
		Assert.assertEquals(CrossingServiceImplTest.USER_ID, name2.getUserId());
	}

	@Test
	public void testGetNextNumberInSequenceDefault() throws MiddlewareQueryException {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("A");
		Mockito.doReturn("1").when(this.germplasmDataManager).getNextSequenceNumberForCrossName(Matchers.anyString());

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);

		Assert.assertEquals(1, nextNumber);
	}

	@Test
	public void testGetNextNumberInSequenceStartNumberIsSpecified() throws MiddlewareQueryException {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("A");
		Mockito.doReturn("100").when(this.germplasmDataManager).getNextSequenceNumberForCrossName(Matchers.anyString());

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);

		Assert.assertEquals(1, nextNumber);
	}

	@Test
	public void testGetNextNumberInSequenceStartNumberIsNotSpecified() throws MiddlewareQueryException {

		final CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(0);
		setting.setPrefix("A");
		Mockito.doReturn("100").when(this.germplasmDataManager).getNextSequenceNumberForCrossName(Matchers.anyString());

		final int nextNumber = this.crossingService.getNextNumberInSequence(setting);

		Assert.assertEquals(100, nextNumber);
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

	private ImportedCrossesList createImportedCrossesList() {

		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		importedCrossesList.setImportedGermplasms(importedCrosses);
		return importedCrossesList;

	}

	private List<ImportedCrosses> createImportedCrosses() {

		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		final ImportedCrosses cross = new ImportedCrosses();
		cross.setFemaleDesig("FEMALE-12345");
		cross.setFemaleGid("12345");
		cross.setMaleDesig("MALE-54321");
		cross.setMaleGid("54321");
		cross.setCross("CROSS");
		cross.setSource("MALE:1:FEMALE:1");
		importedCrosses.add(cross);
		final ImportedCrosses cross2 = new ImportedCrosses();
		cross2.setFemaleDesig("FEMALE-9999");
		cross2.setFemaleGid("9999");
		cross2.setMaleDesig("MALE-8888");
		cross2.setMaleGid("8888");
		cross2.setCross("CROSS");
		cross2.setSource("MALE:2:FEMALE:2");
		importedCrosses.add(cross2);

		return importedCrosses;

	}

	private ImportedCrosses createCross() {
		final ImportedCrosses cross = new ImportedCrosses();
		cross.setFemaleDesig("FEMALE-12345");
		cross.setFemaleGid("12345");
		cross.setMaleDesig("MALE-54321");
		cross.setMaleGid("54321");
		return cross;
	}

	private CrossSetting createCrossSetting() {
		return new CrossSetting(null, null, this.createCrossNameSetting(), null);
	}

	private CrossNameSetting createCrossNameSetting() {
		final CrossNameSetting setting = new CrossNameSetting();

		setting.setPrefix("PREFIX");
		setting.setSuffix("SUFFIX");
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
		breedingMethodSetting.setMethodId(BREEDING_METHOD_ID);
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
