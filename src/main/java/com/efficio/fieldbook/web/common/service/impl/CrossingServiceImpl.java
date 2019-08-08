package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.exception.InvalidInputException;
import com.efficio.fieldbook.web.common.service.CrossingService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.commons.ruleengine.generator.SeedSourceGenerator;
import org.generationcp.commons.service.GermplasmNamingService;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.CrossingUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Methods;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.Progenitor;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.pedigree.PedigreeFactory;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Transactional
public class CrossingServiceImpl implements CrossingService {

	public static final int MAX_CROSS_NAME_SIZE = 240;
	static final Integer GERMPLASM_GNPGS = 2;
	static final Integer GERMPLASM_GRPLCE = 0;
	static final Integer GERMPLASM_LGID = 0;
	static final Integer GERMPLASM_MGID = 0;
	static final Integer GERMPLASM_REFID = 0;
	static final Integer NAME_REFID = 0;
	static final String[] USER_DEF_FIELD_CROSS_NAME = {"CROSS NAME", "CROSSING NAME"};
	static final String DEFAULT_SEPARATOR = "/";

	static final Integer PEDIGREE_NAME_TYPE = 18;
	static final Integer PREFERRED_NAME = 1;
	static final int MAX_SEED_SOURCE_SIZE = 255;
	public static final String TRUNCATED = "(truncated)";

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Resource
	private CrossingTemplateParser crossingTemplateParser;

	@Resource
	private MessageSource messageSource;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private PedigreeService pedigreeService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private PedigreeDataManager pedigreeDataManager;

	@Resource
	private SeedSourceGenerator seedSourceGenerator;

	@Resource
	private GermplasmNamingService germplasmNamingService;

	@Override
	public ImportedCrossesList parseFile(final MultipartFile file) throws FileParsingException {
		return this.crossingTemplateParser.parseFile(file, null);
	}

	/*
		This is used for MANUAL naming of crosses
	 */
	@Override
	public boolean applyCrossSetting(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList, final Integer userId,
		final Workbook workbook) {
		this.applyCrossNameSettingToImportedCrosses(crossSetting.getCrossNameSetting(), importedCrossesList.getImportedCrosses());
		final GermplasmListResult pairsResult = this.getTriples(crossSetting, importedCrossesList, userId, workbook);
		this.save(crossSetting, importedCrossesList, pairsResult.germplasmTriples);
		return pairsResult.isTrimed;
	}

	public class GermplasmListResult {

		private final List<Triple<Germplasm, Name, List<Progenitor>>> germplasmTriples;
		private final Boolean isTrimed;

		public GermplasmListResult(final List<Triple<Germplasm, Name, List<Progenitor>>> germplasmTriples, final Boolean isTrimed) {
			super();
			this.germplasmTriples = germplasmTriples;
			this.isTrimed = isTrimed;
		}

		public List<Triple<Germplasm, Name, List<Progenitor>>> getGermplasmTriples() {
			return this.germplasmTriples;
		}

		public Boolean getIsTrimed() {
			return this.isTrimed;
		}
	}

	@Override
	public boolean applyCrossSettingWithNamingRules(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
		final Integer userId, final Workbook workbook) {

		int entryIdCounter = 1;
		final Map<String, Workbook> workbookMap = new HashMap<>();
		for (final ImportedCrosses importedCross : importedCrossesList.getImportedCrosses()) {
			this.populateSeedSource(importedCross, workbook, workbookMap);
			importedCross.setEntryCode(String.valueOf(entryIdCounter));
			importedCross.setEntryId(entryIdCounter);
			entryIdCounter++;
		}

		final GermplasmListResult pairsResult =
			this.generateGermplasmNameTriples(crossSetting, importedCrossesList.getImportedCrosses(), userId,
				importedCrossesList.hasPlotDuplicate());

		final List<Germplasm> germplasmList = this.extractGermplasmList(pairsResult.germplasmTriples);
		final Integer crossingNameTypeId = this.getIDForUserDefinedFieldCrossingName();

		CrossingUtil.applyMethodNameType(this.germplasmDataManager, pairsResult.germplasmTriples, crossingNameTypeId);

		this.verifyGermplasmMethodPresent(germplasmList);
		this.save(crossSetting, importedCrossesList, pairsResult.germplasmTriples);
		return pairsResult.isTrimed;
	}

	@Override
	public void populateSeedSource(final ImportedCrosses importedCross, final Workbook workbook, final Map<String, Workbook> workbookMap) {
		if (importedCross.getSource() == null || StringUtils.isEmpty(importedCross.getSource()) || importedCross.getSource()
			.equalsIgnoreCase(ImportedCrosses.SEED_SOURCE_PENDING)) {
			// With the current Excel import template, male parents of each cross will come same study
			final String maleStudyName = importedCross.getMaleStudyNames().get(0);
			final Workbook maleStudyWorkbook = this.getMaleStudyWorkbook(maleStudyName, workbook, workbookMap);

			//FIXME Refactor and optimise SeedSourceGenerator/LocationResolver/SeasonResolver to remove dependency on workbook.
			final String generatedSource = this.seedSourceGenerator
				.generateSeedSourceForCross(workbook, importedCross.getMalePlotNumbersAsStringList(),
					importedCross.getFemalePlotNo().toString(),
					maleStudyName, importedCross.getFemaleStudyName(), maleStudyWorkbook);

			if (generatedSource.length() > CrossingServiceImpl.MAX_SEED_SOURCE_SIZE) {
				importedCross.setSource(generatedSource.substring(0, CrossingServiceImpl.MAX_SEED_SOURCE_SIZE));
			} else {
				importedCross.setSource(generatedSource);
			}

		}
	}

	private Workbook getMaleStudyWorkbook(final String maleStudyName, final Workbook workbook, final Map<String, Workbook> workbookMap) {
		if (workbookMap.get(maleStudyName) != null)
			return workbookMap.get(maleStudyName);
		final Workbook maleStudyWorkbook = workbook.getStudyName().equals(maleStudyName) ? workbook :
			this.fieldbookMiddlewareService.getStudyByNameAndProgramUUID(maleStudyName, this.contextUtil.getCurrentProgramUUID());
		workbookMap.put(maleStudyName, maleStudyWorkbook);
		return maleStudyWorkbook;
	}

	/**
	 * @Transactional to make sure Germplasm, Name and Attribute entities save atomically.
	 */
	@Transactional
	private void save(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
		final List<Triple<Germplasm, Name, List<Progenitor>>> germplasmTriples) {
		final List<Integer> savedGermplasmIds = this.germplasmDataManager.addGermplasm(germplasmTriples);
		this.saveAttributes(crossSetting, importedCrossesList, savedGermplasmIds);
	}

	void saveAttributes(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
		final List<Integer> savedGermplasmIds) {
		if (crossSetting.getCrossNameSetting().isSaveParentageDesignationAsAString()) {
			this.savePedigreeDesignationName(importedCrossesList, savedGermplasmIds, crossSetting);
		}

		// We iterate through the cross list here to merge, so we will create
		// the SeedSource attribute list
		// at the same time (each GP is linked to a PlotCode)
		final List<Attribute> attributeList = new ArrayList<>();
		final Iterator<Integer> germplasmIdIterator = savedGermplasmIds.iterator();
		final Integer today = Integer.valueOf(DateUtil.getCurrentDateAsStringValue());
		for (final ImportedCrosses cross : importedCrossesList.getImportedCrosses()) {

			// this will do the merging and using the gid and cross from the
			// initial duplicate
			if (FieldbookUtil
				.isContinueCrossingMerge(importedCrossesList.hasPlotDuplicate(), crossSetting.isPreservePlotDuplicates(), cross)) {
				FieldbookUtil.mergeCrossesPlotDuplicateData(cross, importedCrossesList.getImportedCrosses());
				continue;
			}

			final Integer newGid = germplasmIdIterator.next();
			cross.setGid(newGid.toString());

			// save Attribute for SeedSource as a PlotCode
			final Attribute plotCodeAttribute = new Attribute();
			plotCodeAttribute.setAdate(today);
			plotCodeAttribute.setGermplasmId(newGid);
			plotCodeAttribute.setTypeId(this.germplasmDataManager.getPlotCodeField().getFldno());
			plotCodeAttribute.setAval(cross.getSource());
			plotCodeAttribute.setUserId(this.contextUtil.getCurrentWorkbenchUserId());

			attributeList.add(plotCodeAttribute);
		}

		this.germplasmDataManager.addAttributes(attributeList);
	}

	public CrossingServiceImpl() {
		super();
	}

	// FIXME the methods getTriples() and generateGermplasmNameTriples() should be
	// combined into one
	private GermplasmListResult getTriples(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
		final Integer userId, final Workbook workbook) {
		final Map<String, Workbook> workbookMap = new HashMap<>();
		for (final ImportedCrosses importedCross : importedCrossesList.getImportedCrosses()) {
			this.populateSeedSource(importedCross, workbook, workbookMap);
		}

		final GermplasmListResult pairsResult =
			this.generateGermplasmNameTriples(crossSetting, importedCrossesList.getImportedCrosses(), userId,
				importedCrossesList.hasPlotDuplicate());

		final List<Germplasm> germplasmList = this.extractGermplasmList(pairsResult.germplasmTriples);
		final Integer crossingNameTypeId = this.getIDForUserDefinedFieldCrossingName();

		CrossingUtil.applyMethodNameType(this.germplasmDataManager, pairsResult.germplasmTriples, crossingNameTypeId);

		this.verifyGermplasmMethodPresent(germplasmList);
		return new GermplasmListResult(pairsResult.germplasmTriples, pairsResult.isTrimed);
	}

	private List<Germplasm> extractGermplasmList(final List<Triple<Germplasm, Name, List<Progenitor>>> germplasmTriples) {
		final List<Germplasm> returnValue = new ArrayList<>();

		for (final Triple<Germplasm, Name, List<Progenitor>> germplasmTriple : germplasmTriples) {
			returnValue.add(germplasmTriple.getLeft());
		}

		return returnValue;
	}

	void savePedigreeDesignationName(final ImportedCrossesList importedCrossesList, final List<Integer> germplasmIDs,
		final CrossSetting crossSetting) {

		final List<Name> parentageDesignationNames = new ArrayList<>();
		final Iterator<Integer> germplasmIdIterator = germplasmIDs.iterator();
		final Integer nstatValue =
			crossSetting.getCrossNameSetting().isSaveParentageDesignationAsAString() ? 0 : CrossingServiceImpl.PREFERRED_NAME;

		for (final ImportedCrosses entry : importedCrossesList.getImportedCrosses()) {

			final Integer gid = germplasmIdIterator.next();
			final String parentageDesignation = entry.getFemaleDesignation() + DEFAULT_SEPARATOR + entry.getMaleDesignationsAsString();

			Integer locationId = 0;

			if (crossSetting.getAdditionalDetailsSetting().getHarvestLocationId() != null) {
				locationId = crossSetting.getAdditionalDetailsSetting().getHarvestLocationId();
			}

			final Name parentageDesignationName = new Name();
			parentageDesignationName.setGermplasmId(gid);
			parentageDesignationName.setTypeId(CrossingServiceImpl.PEDIGREE_NAME_TYPE);
			parentageDesignationName.setUserId(this.contextUtil.getCurrentWorkbenchUserId());

			parentageDesignationName.setNval(this.truncateName(parentageDesignation));
			parentageDesignationName.setNstat(nstatValue);
			parentageDesignationName.setLocationId(locationId);
			parentageDesignationName.setNdate(Util.getCurrentDateAsIntegerValue());
			parentageDesignationName.setReferenceId(0);

			parentageDesignationNames.add(parentageDesignationName);

		}

		this.germplasmDataManager.addGermplasmName(parentageDesignationNames);
	}

	private void verifyGermplasmMethodPresent(final List<Germplasm> germplasmList) {
		for (final Germplasm germplasm : germplasmList) {
			if (germplasm.getMethodId() == null || germplasm.getMethodId() == 0) {
				throw new MiddlewareQueryException(
					this.messageSource.getMessage("error.save.cross.methods.unavailable", new Object[] {}, Locale.getDefault()));
			}
		}
	}

	// for MANUAL naming of crosses
	void applyCrossNameSettingToImportedCrosses(final CrossNameSetting crossNameSetting, final List<ImportedCrosses> importedCrosses) {
		Integer entryIdCounter = 0;
		synchronized (CrossingServiceImpl.class) {
			for (final ImportedCrosses cross : importedCrosses) {
				entryIdCounter++;
				cross.setEntryId(entryIdCounter);
				cross.setEntryCode(String.valueOf(entryIdCounter));
				cross.setDesig(
					this.germplasmNamingService.generateNextNameAndIncrementSequence(this.getGermplasmNameSetting(crossNameSetting)));

				// this would set the correct cross string depending if the use is
				// cimmyt wheat
				final Germplasm germplasm = new Germplasm();
				germplasm.setGnpgs(2);
				germplasm.setGid(Integer.MAX_VALUE);
				germplasm.setGpid1(Integer.valueOf(cross.getFemaleGid()));
				germplasm.setGpid2(cross.getMaleGids().get(0));
				final String crossString = this.getCross(germplasm, cross, crossNameSetting.getSeparator());

				cross.setCross(crossString);
			}
		}
	}

	@Override
	public String getCross(final Germplasm germplasm, final ImportedCrosses crosses, final String separator) {
		try {
			if (PedigreeFactory.isCimmytWheat(this.crossExpansionProperties.getProfile(),
				this.contextUtil.getProjectInContext().getCropType().getCropName())) {
				return this.pedigreeService.getCrossExpansion(germplasm, null, this.crossExpansionProperties);
			}
			return this.buildCrossName(crosses, separator);
		} catch (final MiddlewareQueryException e) {
			throw new RuntimeException(
				"There was a problem accessing communicating with the database. " + "Please contact support for further help.", e);
		}

	}

	Integer getFormattedHarvestDate(final String harvestDate) {
		Integer dateIntValue = 0;
		if (harvestDate != null && !StringUtil.isEmpty(harvestDate)) {
			String replacedDateString = harvestDate.replace("-", "");
			if (replacedDateString.length() == 6) {
				replacedDateString += "00";
			}
			dateIntValue = Integer.parseInt(replacedDateString);
		}
		return dateIntValue;
	}

	/**
	 * <p>
	 * This method will set germplasm gdate from given date as per rules.
	 * </p>
	 * <ol>
	 * <li>If harvested date is provided then it will be used as gdate.</li>
	 * <li>If not then current date will be used as gdate.</li>
	 * </ol>
	 *
	 * @param germplasm   germplasm instance into which gdate need to be set.
	 * @param harvestDate date given using user form.
	 */
	void populateGermplasmDate(final Germplasm germplasm, final String harvestDate) {
		final Integer formattedHarvestDate = this.getFormattedHarvestDate(harvestDate);

		if (formattedHarvestDate != 0) {
			germplasm.setGdate(formattedHarvestDate);
			return;
		}

		germplasm.setGdate(DateUtil.getCurrentDateAsIntegerValue());
	}

	// FIXME the methods getTriples() and generateGermplasmNameTriples() should be combined into one
	GermplasmListResult generateGermplasmNameTriples(final CrossSetting crossSetting, final List<ImportedCrosses> importedCrosses,
		final Integer userId, final boolean hasPlotDuplicate) {

		boolean isTrimed = false;
		final List<Triple<Germplasm, Name, List<Progenitor>>> triples = new ArrayList<>();

		final AdditionalDetailsSetting additionalDetailsSetting = crossSetting.getAdditionalDetailsSetting();

		Integer harvestLocationId = 0;

		if (additionalDetailsSetting.getHarvestLocationId() != null) {
			harvestLocationId = additionalDetailsSetting.getHarvestLocationId();
		}

		for (final ImportedCrosses cross : importedCrosses) {

			if (FieldbookUtil.isContinueCrossingMerge(hasPlotDuplicate, crossSetting.isPreservePlotDuplicates(), cross)) {
				continue;
			}

			final Germplasm germplasm = this.createGermplasm(cross, userId, harvestLocationId, additionalDetailsSetting.getHarvestDate());
			final Name name = this.createName(germplasm, cross, userId, harvestLocationId);
			final List<Progenitor> progenitors = this.createProgenitors(cross, germplasm);

			final List<Name> names = new ArrayList<>();
			names.add(name);
			cross.setNames(names);

			isTrimed = cross.getDesig().length() > CrossingServiceImpl.MAX_CROSS_NAME_SIZE;

			triples.add(ImmutableTriple.of(germplasm, name, progenitors));
		}

		return new GermplasmListResult(triples, isTrimed);
	}

	Germplasm createGermplasm(final ImportedCrosses cross, final Integer userId, final Integer harvestLocationId,
		final String harvestDate) {

		Germplasm germplasm = null;

		// Retrieve the germplasm (cross) from database: In case of Study
		// -> Crossing workflows, we expect the GID to always
		// exist as crosses are created in crossing manager and persisted.
		if (cross.getGid() != null) {
			germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(cross.getGid()));
			germplasm.setMethodId(cross.getBreedingMethodId());
		} else {
			germplasm = new Germplasm();
			// In case of importing crosses, the crosses are not yet
			// persisted, GID will be null. We populate data from
			// spreadsheet,
			// create new Germplasm.
			this.updateConstantFields(germplasm, userId);
			germplasm.setGpid1(Integer.valueOf(cross.getFemaleGid()));
			germplasm.setGpid2(cross.getMaleGids().get(0));
			germplasm.setMethodId(cross.getBreedingMethodId());
		}

		// Set germplasm date based on user input or information from source
		// data.
		this.populateGermplasmDate(germplasm, harvestDate);

		// Set the location based on what is selected as harvest location in
		// both cases of crossing.
		germplasm.setLocationId(harvestLocationId);

		return germplasm;
	}

	Name createName(final Germplasm germplasm, final ImportedCrosses cross, final Integer userId, final Integer harvestLocationId) {

		Name name = null;

		// In case of Study
		// -> Crossing workflows, we expect the GID to always
		// exist as crosses are created in crossing manager and persisted.
		if (germplasm.getGid() != null) {
			// Find the existing name that was created in crossing manager.
			// There should only be one and must be preferred.
			name = germplasm.findPreferredName();
		} else {
			// For import we always create new name
			name = new Name();
			name.setReferenceId(CrossingServiceImpl.NAME_REFID);
		}

		// Common name updates
		final String designation = cross.getDesig();
		name.setNval(this.truncateName(designation));
		name.setUserId(userId);
		name.setNdate(germplasm.getGdate());
		name.setLocationId(harvestLocationId);

		return name;

	}

	List<Progenitor> createProgenitors(final ImportedCrosses cross, final Germplasm germplasm) {

		final List<Progenitor> progenitors = new ArrayList<>();

		// In case of Study
		// -> Crossing workflows, we expect the GID to always
		// exist as crosses are created in crossing manager and persisted.
		if (cross.getGid() != null) {
			final List<Progenitor> existingProgenitors = this.pedigreeDataManager.getProgenitorsByGID(Integer.valueOf(cross.getGid()));
			if (existingProgenitors != null) {
				return existingProgenitors;
			}
		} else if (cross.isPolyCross()) {

			// Create progenitors if the cross has multiple male parents.
			// Start the progenitor number at 3
			int progenitorNumber = 3;
			final Iterator<ImportedGermplasmParent> iterator = cross.getMaleParents().iterator();

			// Skip the first male parent as it is already assigned to germplasm's gpid2.
			iterator.next();

			while (iterator.hasNext()) {
				final ImportedGermplasmParent importedGermplasmParent = iterator.next();
				progenitors.add(new Progenitor(germplasm, progenitorNumber, importedGermplasmParent.getGid()));
				progenitorNumber++;
			}
		}

		return progenitors;

	}

	private String truncateName(final String designation) {

		if (designation.length() > CrossingServiceImpl.MAX_CROSS_NAME_SIZE) {
			return designation.substring(0, CrossingServiceImpl.MAX_CROSS_NAME_SIZE - 1) + CrossingServiceImpl.TRUNCATED;
		}

		return designation;
	}

	private void updateConstantFields(final Germplasm germplasm, final Integer userId) {
		germplasm.setGnpgs(CrossingServiceImpl.GERMPLASM_GNPGS);
		germplasm.setGrplce(CrossingServiceImpl.GERMPLASM_GRPLCE);
		germplasm.setLgid(CrossingServiceImpl.GERMPLASM_LGID);
		germplasm.setMgid(CrossingServiceImpl.GERMPLASM_MGID);
		germplasm.setUserId(userId);
		germplasm.setReferenceId(CrossingServiceImpl.GERMPLASM_REFID);
	}

	Integer getNextNumberInSequence(final String prefix) {

		int nextNumberInSequence = 1;

		if (!StringUtils.isEmpty(prefix)) {
			nextNumberInSequence = this.germplasmNamingService.getNextSequence(prefix.trim());
		}

		return nextNumberInSequence;

	}

	@Override
	public String getNextNameInSequence(final CrossNameSetting setting) throws InvalidInputException {

		try {
			return this.germplasmNamingService.getNextNameInSequence(this.getGermplasmNameSetting(setting));
		} catch (final InvalidGermplasmNameSettingException e) {
			final String invalidStatingNumberErrorMessage = this.messageSource
				.getMessage("error.not.valid.starting.sequence", new Object[] {this.getNextNumberInSequence(setting.getPrefix()) - 1},
					LocaleContextHolder.getLocale());
			throw new InvalidInputException(invalidStatingNumberErrorMessage);
		}
	}

	// TODO extract to utility class or method
	private GermplasmNameSetting getGermplasmNameSetting(final CrossNameSetting crossNameSetting) {
		final GermplasmNameSetting nameSetting = new GermplasmNameSetting();
		nameSetting.setPrefix(crossNameSetting.getPrefix());
		nameSetting.setSuffix(crossNameSetting.getSuffix());
		nameSetting.setAddSpaceBetweenPrefixAndCode(crossNameSetting.isAddSpaceBetweenPrefixAndCode());
		nameSetting.setAddSpaceBetweenSuffixAndCode(crossNameSetting.isAddSpaceBetweenSuffixAndCode());
		nameSetting.setNumOfDigits(crossNameSetting.getNumOfDigits());
		nameSetting.setStartNumber(crossNameSetting.getStartNumber());
		return nameSetting;
	}

	String buildCrossName(final ImportedCrosses crosses, final String separator) {
		return crosses.getFemaleDesignation() + separator + crosses.getMaleDesignationsAsString();
	}

	private Integer getIDForUserDefinedFieldCrossingName() {

		final List<UserDefinedField> nameTypes = this.germplasmListManager.getGermplasmNameTypes();
		for (final UserDefinedField type : nameTypes) {
			for (final String crossNameValue : CrossingServiceImpl.USER_DEF_FIELD_CROSS_NAME) {
				if (crossNameValue.equalsIgnoreCase(type.getFcode()) || crossNameValue.equalsIgnoreCase(type.getFname())) {
					return type.getFldno();
				}
			}
		}

		return null;
	}

	@Override
	public void processCrossBreedingMethod(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList) {
		final BreedingMethodSetting methodSetting = crossSetting.getBreedingMethodSetting();
		final boolean basedOnImportFile = methodSetting.isBasedOnImportFile();
		final boolean basedOnStatusOfParentalLines = methodSetting.isBasedOnStatusOfParentalLines();

		for (final ImportedCrosses importedCrosses : importedCrossesList.getImportedCrosses()) {
			this.processCrossBreedingMethod(methodSetting, basedOnImportFile, basedOnStatusOfParentalLines, importedCrosses);
		}
	}

	private void processCrossBreedingMethod(final BreedingMethodSetting methodSetting, final boolean basedOnImportFile,
		final boolean basedOnStatusOfParentalLines, final ImportedCrosses importedCrosses) {

		final String rawBreedingMethod = importedCrosses.getRawBreedingMethod();

		// if imported cross contains raw breeding method code we use that to
		// populate the breeding method
		if (!StringUtils.isEmpty(rawBreedingMethod) && basedOnImportFile && this
			.processBreedingMethodImport(importedCrosses, rawBreedingMethod)) {
			return;
		}

		if (!basedOnStatusOfParentalLines && !basedOnImportFile && methodSetting.getMethodId() != null
			&& methodSetting.getMethodId() != 0) {
			importedCrosses.setBreedingMethodId(methodSetting.getMethodId());
			this.setBreedingMethodNameByMethodId(importedCrosses);
			return;
		}

		// if breeding method is based on status of parental lines, we calculate
		// the resulting breeding method per germplasm
		// currently, the convention is that parental lines will be used as
		// basis if user does not select any method
		this.processBreedingMethodParental(importedCrosses);
	}

	private void processBreedingMethodParental(final ImportedCrosses importedCrosses) {
		// If polycross, automatically return as "Single Cross""
		if (importedCrosses.isPolyCross()) {
			importedCrosses.setBreedingMethodId(Methods.SINGLE_CROSS.getMethodID());
		} else {
			final Integer femaleGid = Integer.parseInt(importedCrosses.getFemaleGid());
			final Integer maleGid = importedCrosses.getMaleGids().get(0);

			final Triple<Germplasm, Germplasm, Germplasm> femaleLine = this.retrieveParentGermplasmObjects(femaleGid);
			final Triple<Germplasm, Germplasm, Germplasm> maleLine = this.retrieveParentGermplasmObjects(maleGid);

			importedCrosses.setBreedingMethodId(CrossingUtil
				.determineBreedingMethodBasedOnParentalLine(femaleLine.getLeft(), maleLine.getLeft(), femaleLine.getMiddle(),
					femaleLine.getRight(), maleLine.getMiddle(), maleLine.getRight()));
		}

		this.setBreedingMethodNameByMethodId(importedCrosses);
	}

	private boolean processBreedingMethodImport(final ImportedCrosses importedCrosses, final String rawBreedingMethod) {
		final Method breedingMethod = this.germplasmDataManager.getMethodByCode(rawBreedingMethod);

		if (breedingMethod != null && breedingMethod.getMid() != null && breedingMethod.getMid() != 0) {
			importedCrosses.setBreedingMethodId(breedingMethod.getMid());
			importedCrosses.setBreedingMethodName(breedingMethod.getMname());
		} else {
			// TODO address case where breeding method does not exist in the
			// parser level to avoid having this case during the saving flow
			importedCrosses.setBreedingMethodId(0);
		}

		// if at this point, if there is already breeding method info available
		// on the imported cross
		// (from import file, etc, we proceed to next cross)
		return importedCrosses.isBreedingMethodInformationAvailable();
	}

	private void setBreedingMethodNameByMethodId(final ImportedCrosses importedCrosses) {
		final Method method = this.germplasmDataManager.getMethodByID(importedCrosses.getBreedingMethodId());
		if (method == null) {
			return;
		}
		final String breedingMethodName = method.getMname();
		importedCrosses.setBreedingMethodName(breedingMethodName);
	}

	private Triple<Germplasm, Germplasm, Germplasm> retrieveParentGermplasmObjects(final Integer germplasmID) {
		final Germplasm parent = this.germplasmDataManager.getGermplasmByGID(germplasmID);

		Germplasm motherOfParent = null;
		Germplasm fatherOfParent = null;
		if (parent != null) {
			motherOfParent = this.germplasmDataManager.getGermplasmByGID(parent.getGpid1());
			fatherOfParent = this.germplasmDataManager.getGermplasmByGID(parent.getGpid2());
		}

		return new ImmutableTriple<>(parent, motherOfParent, fatherOfParent);
	}

	/**
	 * For Test Only
	 *
	 * @param germplasmListManager
	 */
	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	/**
	 * For Test Only
	 *
	 * @param germplasmDataManager
	 */
	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;

	}

	/**
	 * For Test Only
	 *
	 * @param crossExpansionProperties
	 */
	void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}

	/**
	 * For Test Only
	 *
	 * @param contextUtil
	 */
	void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	/**
	 * For Test Only
	 *
	 * @param seedSourceGenerator
	 */
	void setSeedSourceGenerator(final SeedSourceGenerator seedSourceGenerator) {
		this.seedSourceGenerator = seedSourceGenerator;
	}

	public void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
