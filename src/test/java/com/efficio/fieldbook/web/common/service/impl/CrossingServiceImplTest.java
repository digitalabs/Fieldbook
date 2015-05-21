package com.efficio.fieldbook.web.common.service.impl;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class CrossingServiceImplTest {

	private static final String SAVED_CROSSES_GID1 = "-9999";
	private static final String SAVED_CROSSES_GID2 = "-8888";
	private static final Integer USER_ID = 123;
	private CrossingServiceImpl crossingService;
	private ImportedCrossesList importedCrossesList;
	
	@Mock
	private GermplasmListManager germplasmListManager;
	
	@Mock
	private GermplasmDataManager germplasmDataManager;
	
	@Before
	public void setUp() throws MiddlewareQueryException {
		
		MockitoAnnotations.initMocks(this);
		
		importedCrossesList = createImportedCrossesList();
		importedCrossesList.setImportedGermplasms(createImportedCrosses());
		
		crossingService = spy(new CrossingServiceImpl());
		crossingService.setGermplasmListManager(germplasmListManager);
		crossingService.setGermplasmDataManager(germplasmDataManager);
		
		doReturn(createNameTypes()).when(germplasmListManager).getGermplasmNameTypes();
		doReturn(createGermplasmIds()).when(germplasmDataManager).addGermplasm(anyMap());
		doReturn(new Method()).when(germplasmDataManager).getMethodByName(anyString());
		
	}
	
	
	@Test
	public void testApplyCrossSetting() throws MiddlewareQueryException{
		
		CrossSetting crossSetting = new CrossSetting();
		CrossNameSetting crossNameSetting = createCrossNameSetting();
		BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting();
		AdditionalDetailsSetting additionalDetailsSetting = new AdditionalDetailsSetting();
		crossSetting.setCrossNameSetting(crossNameSetting);
		crossSetting.setBreedingMethodSetting(breedingMethodSetting);
		crossSetting.setAdditionalDetailsSetting(additionalDetailsSetting);
		
		crossingService.applyCrossSetting(crossSetting, importedCrossesList, USER_ID);
		
		ImportedCrosses cross1 = importedCrossesList.getImportedCrosses().get(0);
		
		assertEquals(SAVED_CROSSES_GID1, cross1.getGid());
		assertEquals(crossNameSetting.getPrefix() + " 0000100 " + crossNameSetting.getSuffix() , cross1.getDesig());
		assertEquals((Integer)1, cross1.getEntryId());
		assertEquals("1", cross1.getEntryCode());
		assertEquals(null, cross1.getNames().get(0).getGermplasmId());
		assertEquals((Integer)0, cross1.getNames().get(0).getLocationId());
		assertEquals(USER_ID, cross1.getNames().get(0).getUserId());
		
		ImportedCrosses cross2 = importedCrossesList.getImportedCrosses().get(1);
		
		assertEquals(SAVED_CROSSES_GID2, cross2.getGid());
		assertEquals(crossNameSetting.getPrefix() + " 0000101 " + crossNameSetting.getSuffix() , cross2.getDesig());
		assertEquals((Integer)2, cross2.getEntryId());
		assertEquals("2", cross2.getEntryCode());
		assertEquals(null, cross2.getNames().get(0).getGermplasmId());
		assertEquals((Integer)0, cross2.getNames().get(0).getLocationId());
		assertEquals(USER_ID, cross2.getNames().get(0).getUserId());
		
		
	}
	
	@Test
	public void testApplyCrossNameSettingToImportedCrosses() throws MiddlewareQueryException{
		
		CrossNameSetting setting = createCrossNameSetting();
		crossingService.applyCrossNameSettingToImportedCrosses(setting, importedCrossesList.getImportedCrosses());
		
		ImportedCrosses cross1 = importedCrossesList.getImportedCrosses().get(0);
		
		assertEquals(null, cross1.getGid());
		assertEquals(setting.getPrefix() + " 0000100 " + setting.getSuffix() , cross1.getDesig());
		assertEquals(cross1.getFemaleDesig() + setting.getSeparator() + cross1.getMaleDesig() , cross1.getCross());
		assertEquals((Integer)1, cross1.getEntryId());
		assertEquals("1", cross1.getEntryCode());
		
		ImportedCrosses cross2 = importedCrossesList.getImportedCrosses().get(1);
		
		assertEquals(null, cross2.getGid());
		assertEquals(setting.getPrefix() + " 0000101 " + setting.getSuffix() , cross2.getDesig());
		assertEquals(cross2.getFemaleDesig() + setting.getSeparator() + cross2.getMaleDesig() , cross2.getCross());
		assertEquals((Integer)2, cross2.getEntryId());
		assertEquals("2", cross2.getEntryCode());
	}
	
	@Test
	public void testBuildCrossName(){
		
		CrossNameSetting setting = createCrossNameSetting();
		ImportedCrosses cross = createCross();
		String crossName = crossingService.buildCrossName(cross.getFemaleDesig(), cross.getMaleDesig(), setting.getSeparator());
		
		assertEquals(cross.getFemaleDesig() + setting.getSeparator() + cross.getMaleDesig() , crossName);
		
	}
	
	@Test
	public void testBuildDesignationNameInSequenceDefaultSetting(){
		
		CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("A");
		setting.setSuffix("B");
		
		String designationName = crossingService.buildDesignationNameInSequence(1, setting);
		assertEquals("A1B", designationName);
	}
	
	@Test
	public void testBuildDesignationNameInSequenceWithSpacesInPrefixSuffix(){
		
		CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("A");
		setting.setSuffix("B");
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		
		String designationName = crossingService.buildDesignationNameInSequence(1, setting);
		assertEquals("A 1 B", designationName);
	}
	
	@Test
	public void testBuildDesignationNameInSequenceWithNumOfDigits(){
		
		CrossNameSetting setting = createCrossNameSetting();
		setting.setAddSpaceBetweenPrefixAndCode(true);
		setting.setAddSpaceBetweenSuffixAndCode(true);
		setting.setNumOfDigits(3);
		setting.setPrefix("A");
		setting.setSuffix("B");
		
		String designationName = crossingService.buildDesignationNameInSequence(1, setting);
		assertEquals("A 001 B", designationName);
	}
	
	@Test
	public void testBuildPrefixStringDefault(){
		CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix(" A  ");
		String prefix = crossingService.buildPrefixString(setting);
		
		assertEquals("A", prefix);
	}
	
	@Test
	public void testBuildPrefixStringWithSpace(){
		CrossNameSetting setting = new CrossNameSetting();
		setting.setPrefix("   A");
		setting.setAddSpaceBetweenPrefixAndCode(true);
		String prefix = crossingService.buildPrefixString(setting);
		
		assertEquals("A ", prefix);
	}
	
	@Test
	public void testBuildSuffixStringDefault(){
		CrossNameSetting setting = new CrossNameSetting();
		setting.setSuffix("  B   ");
		String suffix = crossingService.buildSuffixString(setting);
		
		assertEquals("B", suffix);
	}
	
	@Test
	public void testBuildSuffixStringWithSpace(){
		CrossNameSetting setting = new CrossNameSetting();
		setting.setSuffix("   B   ");
		setting.setAddSpaceBetweenSuffixAndCode(true);
		String suffix = crossingService.buildSuffixString(setting);
		
		assertEquals(" B", suffix);
	}
	
	@Test
	public void testGenerateGermplasmNameMap() throws MiddlewareQueryException{


		CrossSetting crossSetting = new CrossSetting();
		CrossNameSetting crossNameSetting = createCrossNameSetting();
		BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting();
		AdditionalDetailsSetting additionalDetailsSetting = createAdditionalDetailsSetting();
		crossSetting.setCrossNameSetting(crossNameSetting);
		crossSetting.setBreedingMethodSetting(breedingMethodSetting);
		crossSetting.setAdditionalDetailsSetting(additionalDetailsSetting);
		
		Map<Germplasm, Name> germplasmNameMap = crossingService.generateGermplasmNameMap(crossSetting, importedCrossesList.getImportedCrosses(), USER_ID, false);
	
		Iterator<Entry<Germplasm, Name>> iterator = germplasmNameMap.entrySet().iterator();
		Entry<Germplasm, Name> entry1 = iterator.next();
		Germplasm germplasm1 = entry1.getKey();
		Name name1 = entry1.getValue();
		ImportedCrosses cross1 = importedCrossesList.getImportedCrosses().get(0);
		
		assertEquals(null ,germplasm1.getGid());
		assertEquals(20150101 ,germplasm1.getGdate().intValue());
		assertEquals(2 ,germplasm1.getGnpgs().intValue());
		assertEquals(cross1.getFemaleGid() ,germplasm1.getGpid1().toString());
		assertEquals(cross1.getMaleGid() ,germplasm1.getGpid2().toString());
		assertEquals(0 ,germplasm1.getGrplce().intValue());
		assertEquals(0 ,germplasm1.getLgid().intValue());
		assertEquals(0 ,germplasm1.getGrplce().intValue());
		assertEquals(0 ,germplasm1.getLgid().intValue());
		assertEquals(99 ,germplasm1.getLocationId().intValue());
		assertEquals(null ,germplasm1.getMethodId());
		assertEquals(0 ,germplasm1.getMgid().intValue());
		assertEquals(null ,germplasm1.getPreferredAbbreviation());
		assertEquals(null ,germplasm1.getPreferredName());
		assertEquals(0 ,germplasm1.getReferenceId().intValue());
		assertEquals(USER_ID ,germplasm1.getUserId());
		
		assertEquals(null, name1.getGermplasmId());
		assertEquals(99, name1.getLocationId().intValue());
		assertEquals(20150101, name1.getNdate().intValue());
		assertEquals(null, name1.getNid());
		assertEquals(null, name1.getNstat());
		assertEquals(null, name1.getNval());
		assertEquals(0, name1.getReferenceId().intValue());
		assertEquals(null, name1.getTypeId());
		assertEquals(USER_ID, name1.getUserId());
		
		Entry<Germplasm, Name> entry2 = iterator.next();
		Germplasm germplasm2 = entry2.getKey();
		Name name2 = entry2.getValue();
		ImportedCrosses cross2 = importedCrossesList.getImportedCrosses().get(1);
		
		assertEquals(null ,germplasm2.getGid());
		assertEquals(20150101 ,germplasm2.getGdate().intValue());
		assertEquals(2 ,germplasm2.getGnpgs().intValue());
		assertEquals(cross2.getFemaleGid() ,germplasm2.getGpid1().toString());
		assertEquals(cross2.getMaleGid() ,germplasm2.getGpid2().toString());
		assertEquals(0 ,germplasm2.getGrplce().intValue());
		assertEquals(0 ,germplasm2.getLgid().intValue());
		assertEquals(0 ,germplasm2.getGrplce().intValue());
		assertEquals(0 ,germplasm2.getLgid().intValue());
		assertEquals(99 ,germplasm2.getLocationId().intValue());
		assertEquals(null ,germplasm2.getMethodId());
		assertEquals(0 ,germplasm2.getMgid().intValue());
		assertEquals(null ,germplasm2.getPreferredAbbreviation());
		assertEquals(null ,germplasm2.getPreferredName());
		assertEquals(0 ,germplasm2.getReferenceId().intValue());
		assertEquals(USER_ID ,germplasm2.getUserId());
		
		assertEquals(null, name2.getGermplasmId());
		assertEquals(99, name2.getLocationId().intValue());
		assertEquals(20150101, name2.getNdate().intValue());
		assertEquals(null, name2.getNid());
		assertEquals(null, name2.getNstat());
		assertEquals(null, name1.getNval());
		assertEquals(0, name2.getReferenceId().intValue());
		assertEquals(null, name2.getTypeId());
		assertEquals(USER_ID, name2.getUserId());
	}
	
	
	@Test
	public void testGetNextNumberInSequenceDefault() throws MiddlewareQueryException{
		
		CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("A");
		doReturn("1").when(germplasmDataManager).getNextSequenceNumberForCrossName(anyString());
		
		int nextNumber = crossingService.getNextNumberInSequence(setting);
		
		assertEquals(1,nextNumber);
	}
	
	@Test
	public void testGetNextNumberInSequenceStartNumberIsSpecified() throws MiddlewareQueryException{
		
		CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(1);
		setting.setPrefix("A");
		doReturn("100").when(germplasmDataManager).getNextSequenceNumberForCrossName(anyString());
		
		int nextNumber = crossingService.getNextNumberInSequence(setting);
		
		assertEquals(1,nextNumber);
	}
	
	@Test
	public void testGetNextNumberInSequenceStartNumberIsNotSpecified() throws MiddlewareQueryException{
		
		CrossNameSetting setting = new CrossNameSetting();
		setting.setStartNumber(0);
		setting.setPrefix("A");
		doReturn("100").when(germplasmDataManager).getNextSequenceNumberForCrossName(anyString());
		
		int nextNumber = crossingService.getNextNumberInSequence(setting);
		
		assertEquals(100,nextNumber);
	}
	
	@Test
	public void testGetNumberWithLeadingZeroesAsStringDefault(){
		CrossNameSetting setting = new CrossNameSetting();
		setting.setNumOfDigits(0);
		String formattedString = crossingService.getNumberWithLeadingZeroesAsString(1, setting);
		
		assertEquals("1", formattedString);
	}
	
	@Test
	public void testGetNumberWithLeadingZeroesAsStringWithNumOfDigitsSpecified(){
		CrossNameSetting setting = new CrossNameSetting();
		setting.setNumOfDigits(8);
		String formattedString = crossingService.getNumberWithLeadingZeroesAsString(1, setting);
		
		assertEquals("00000001", formattedString);
	}
	
	private ImportedCrossesList createImportedCrossesList(){
		
		ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		List<ImportedCrosses> importedCrosses = new ArrayList<>();
    	importedCrossesList.setImportedGermplasms(importedCrosses);
    	return importedCrossesList;
		
	}
	
	private List<ImportedCrosses> createImportedCrosses(){
		
    	List<ImportedCrosses> importedCrosses = new ArrayList<>();
    	ImportedCrosses cross = new ImportedCrosses();
    	cross.setFemaleDesig("FEMALE-12345");
    	cross.setFemaleGid("12345");
    	cross.setMaleDesig("MALE-54321");
    	cross.setMaleGid("54321");
    	cross.setCross("CROSS");
    	cross.setSource("SOURCE");
    	importedCrosses.add(cross);
    	ImportedCrosses cross2 = new ImportedCrosses();
    	cross2.setFemaleDesig("FEMALE-9999");
    	cross2.setFemaleGid("9999");
    	cross2.setMaleDesig("MALE-8888");
    	cross2.setMaleGid("8888");
    	cross2.setCross("CROSS");
    	cross2.setSource("SOURCE");
    	importedCrosses.add(cross2);
    	
    	return importedCrosses;
    	
	}
	
	private ImportedCrosses createCross(){
		ImportedCrosses cross = new ImportedCrosses();
    	cross.setFemaleDesig("FEMALE-12345");
    	cross.setFemaleGid("12345");
    	cross.setMaleDesig("MALE-54321");
    	cross.setMaleGid("54321");
    	return cross;
	}
	
	private CrossNameSetting createCrossNameSetting(){
		CrossNameSetting setting = new CrossNameSetting();
		
    	setting.setPrefix("PREFIX");
    	setting.setSuffix("SUFFIX");
    	setting.setAddSpaceBetweenPrefixAndCode(true);
    	setting.setAddSpaceBetweenSuffixAndCode(true);
    	setting.setSeparator("|");
    	setting.setStartNumber(100);
    	setting.setNumOfDigits(7);
    	
    	return setting;
	}
	
	private AdditionalDetailsSetting createAdditionalDetailsSetting(){
		AdditionalDetailsSetting setting = new AdditionalDetailsSetting();
		
    	setting.setHarvestDate("20150101");
    	setting.setHarvestLocationId(99);
    	
    	return setting;
	}
	
	private List<UserDefinedField> createNameTypes(){
		List<UserDefinedField> nameTypes = new ArrayList<>();
		UserDefinedField udf = new UserDefinedField();
		udf.setFcode(CrossingServiceImpl.USER_DEF_FIELD_CROSS_NAME[0]);
		nameTypes.add(udf);
		return nameTypes;
	}
	
	private List<Integer> createGermplasmIds(){
		List<Integer> ids = new ArrayList<>();
		ids.add(Integer.valueOf(SAVED_CROSSES_GID1));
		ids.add(Integer.valueOf(SAVED_CROSSES_GID2));
		return ids;
	}
	
	
	
}
