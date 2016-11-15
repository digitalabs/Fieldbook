
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.ruleengine.ProcessCodeOrderedRule;
import org.generationcp.commons.ruleengine.ProcessCodeRuleFactory;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.cross.CrossingRuleExecutionContext;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.CrossingUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.pedigree.PedigreeFactory;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.util.ExpressionHelper;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.service.CrossingService;

public class CrossingServiceImpl implements CrossingService {

	public static final Integer GERMPLASM_GNPGS = 2;
	public static final Integer GERMPLASM_GRPLCE = 0;
	public static final Integer GERMPLASM_LGID = 0;
	public static final Integer GERMPLASM_MGID = 0;
	public static final Integer GERMPLASM_REFID = 0;
	public static final Integer NAME_REFID = 0;
	public static final String[] USER_DEF_FIELD_CROSS_NAME = {"CROSS NAME", "CROSSING NAME"};

	private static final Logger LOG = LoggerFactory.getLogger(CrossingServiceImpl.class);
	private static final Integer PEDIGREE_NAME_TYPE = 18;
	private static final Integer PREFERRED_NAME = 1;
	public static final int MAX_CROSS_NAME_SIZE = 240;
	public static final String TRUNCATED = "(truncated)";

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
	private ProcessCodeRuleFactory processCodeRuleFactory;

	@Resource
	private PedigreeDataManager pedigreeDataManager;

	@Resource
	private SeedSourceGenerator seedSourceGenerator;

	@Override
	public ImportedCrossesList parseFile(final MultipartFile file) throws FileParsingException {
		return this.crossingTemplateParser.parseFile(file, null);
	}

	@Override
	public boolean applyCrossSetting(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList, final Integer userId,
			final Workbook workbook) throws MiddlewareQueryException {
		this.applyCrossNameSettingToImportedCrosses(crossSetting, importedCrossesList.getImportedCrosses());
		boolean isTrimed = false;
		final List<Pair<Germplasm, Name>> germplasmPairs = this.getPairs(crossSetting, importedCrossesList, userId, workbook, isTrimed);
		this.save(crossSetting, importedCrossesList, germplasmPairs);
		return isTrimed;
	}

	@Override
	public boolean applyCrossSettingWithNamingRules(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
			final Integer userId, final Workbook workbook) {

		int entryIdCounter = 1;
		// apply the source string here, before we save germplasm if there is no existing source
		for (final ImportedCrosses importedCross : importedCrossesList.getImportedCrosses()) {

			String malePlotNo = "";
			String femalePlotNo = "";

			// Look at the observation rows of Nursery to find plot number assigned to the male/female parent germplasm of the cross.
			for (final MeasurementRow row : workbook.getObservations()) {
				final MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
				final MeasurementData plotNumberData = row.getMeasurementData(TermId.PLOT_NO.getId());

				if (gidData != null && gidData.getValue().equals(importedCross.getFemaleGid())) {
					if (plotNumberData != null) {
						femalePlotNo = plotNumberData.getValue();
					}
				}

				if (gidData != null && gidData.getValue().equals(importedCross.getMaleGid())) {
					if (plotNumberData != null) {
						malePlotNo = plotNumberData.getValue();
					}
				}
			}

			final String generatedSource =
					this.seedSourceGenerator.generateSeedSourceForCross(workbook, malePlotNo, femalePlotNo, workbook.getStudyName(),
							workbook.getStudyName());
			importedCross.setSource(generatedSource);
			importedCross.setEntryId(entryIdCounter);
			importedCross.setEntryCode(String.valueOf(entryIdCounter++));
		}

		boolean isTrimed = false;
		final List<Pair<Germplasm, Name>> germplasmPairs =
				this.generateGermplasmNamePairs(crossSetting, importedCrossesList.getImportedCrosses(), userId,
						importedCrossesList.hasPlotDuplicate(), isTrimed );

		final List<Germplasm> germplasmList = this.extractGermplasmList(germplasmPairs);
		final Integer crossingNameTypeId = this.getIDForUserDefinedFieldCrossingName();

		CrossingUtil.applyMethodNameType(this.germplasmDataManager, germplasmPairs, crossingNameTypeId);

		this.verifyGermplasmMethodPresent(germplasmList);
		this.save(crossSetting, importedCrossesList, germplasmPairs);
		return isTrimed;
	}

	/**
	 * @Transactional to make sure Germplasm, Name and Attribute entities updated atomically.
	 */
	@Override
	@Transactional
	public void updateCrossSetting(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList) {
		this.saveAttributes(crossSetting, importedCrossesList, this.getImportedCrossesGidsList(importedCrossesList));
	}

	/**
	 * @Transactional to make sure Germplasm, Name and Attribute entities save atomically.
	 */
	@Transactional
	private void save(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
			final List<Pair<Germplasm, Name>> germplasmPairs) {
		final List<Integer> savedGermplasmIds = this.germplasmDataManager.addGermplasm(germplasmPairs);
		this.saveAttributes(crossSetting, importedCrossesList, savedGermplasmIds);
	}

	private void saveAttributes(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
			final List<Integer> savedGermplasmIds) {
		if (crossSetting.getCrossNameSetting().isSaveParentageDesignationAsAString()) {
			this.savePedigreeDesignationName(importedCrossesList, savedGermplasmIds, crossSetting);
		}

		// We iterate through the cross list here to merge, so we will create the SeedSource attribute list
		// at the same time (each GP is linked to a PlotCode)
		final List<Attribute> attributeList = new ArrayList<>();
		final Iterator<Integer> germplasmIdIterator = savedGermplasmIds.iterator();
		final Integer today = Integer.valueOf(DateUtil.getCurrentDateAsStringValue());
		for (final ImportedCrosses cross : importedCrossesList.getImportedCrosses()) {

			// this will do the merging and using the gid and cross from the initial duplicate
			if (FieldbookUtil.isContinueCrossingMerge(importedCrossesList.hasPlotDuplicate(), crossSetting.isPreservePlotDuplicates(),
					cross)) {
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

	// FIXME the methods getPairs() and generateGermplasmNamePairs() should be combined into one
	private List<Pair<Germplasm, Name>> getPairs(final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList,
			final Integer userId, final Workbook workbook, boolean isTrimed) {

		// apply the source string here, before we save germplasm if there is no existing source
		for (final ImportedCrosses importedCross : importedCrossesList.getImportedCrosses()) {
			if (importedCross.getSource() == null || StringUtils.isEmpty(importedCross.getSource())
					|| importedCross.getSource().equalsIgnoreCase(ImportedCrosses.SEED_SOURCE_PENDING)) {
				final String generatedSource =
						this.seedSourceGenerator.generateSeedSourceForCross(workbook, importedCross.getMalePlotNo(),
								importedCross.getFemalePlotNo(), importedCross.getMaleStudyName(), importedCross.getFemaleStudyName());
				importedCross.setSource(generatedSource);
			}
		}

		final List<Pair<Germplasm, Name>> germplasmNamePairs =
				this.generateGermplasmNamePairs(crossSetting, importedCrossesList.getImportedCrosses(), userId,
						importedCrossesList.hasPlotDuplicate(), isTrimed);

		final List<Germplasm> germplasmList = this.extractGermplasmList(germplasmNamePairs);
		final Integer crossingNameTypeId = this.getIDForUserDefinedFieldCrossingName();

		CrossingUtil.applyMethodNameType(this.germplasmDataManager, germplasmNamePairs, crossingNameTypeId);

		this.verifyGermplasmMethodPresent(germplasmList);
		return germplasmNamePairs;
	}

	private List<Integer> getImportedCrossesGidsList(final ImportedCrossesList importedCrossesList) {
		final List<Integer> gids = new ArrayList<>();

		if (importedCrossesList == null || importedCrossesList.getImportedCrosses() == null) {
			return gids;
		}

		for (final ImportedCrosses importedCrosses : importedCrossesList.getImportedCrosses()) {
			final Integer gid = importedCrosses.getGid() != null ? Integer.parseInt(importedCrosses.getGid()) : null;
			if (gid != null) {
				gids.add(gid);
			}
		}
		return gids;
	}

	protected List<Germplasm> extractGermplasmList(final List<Pair<Germplasm, Name>> germplasmPairs) {
		final List<Germplasm> returnValue = new ArrayList<>();

		for (final Pair<Germplasm, Name> germplasmPair : germplasmPairs) {
			returnValue.add(germplasmPair.getLeft());
		}

		return returnValue;
	}

	void savePedigreeDesignationName(final ImportedCrossesList importedCrossesList, final List<Integer> germplasmIDs,
			final CrossSetting crossSetting) throws MiddlewareQueryException {

		final List<Name> parentageDesignationNames = new ArrayList<Name>();
		final Iterator<Integer> germplasmIdIterator = germplasmIDs.iterator();
		final Integer nstatValue = crossSetting.getCrossNameSetting().isSaveParentageDesignationAsAString() ? 0 : PREFERRED_NAME;

		for (final ImportedCrosses entry : importedCrossesList.getImportedCrosses()) {

			final Integer gid = germplasmIdIterator.next();
			final String parentageDesignation = entry.getFemaleDesig() + "/" + entry.getMaleDesig();

			final Integer locationId = crossSetting.getAdditionalDetailsSetting().getHarvestLocationId();

			final Name parentageDesignationName = new Name();
			parentageDesignationName.setGermplasmId(gid);
			parentageDesignationName.setTypeId(PEDIGREE_NAME_TYPE);
			parentageDesignationName.setUserId(this.contextUtil.getCurrentUserLocalId());
			parentageDesignationName.setNval(parentageDesignation);
			parentageDesignationName.setNstat(nstatValue);
			parentageDesignationName.setLocationId(locationId);
			parentageDesignationName.setNdate(Util.getCurrentDateAsIntegerValue());
			parentageDesignationName.setReferenceId(0);

			parentageDesignationNames.add(parentageDesignationName);

		}

		this.germplasmDataManager.addGermplasmName(parentageDesignationNames);
	}

	protected void verifyGermplasmMethodPresent(final List<Germplasm> germplasmList) {
		for (final Germplasm germplasm : germplasmList) {
			if (germplasm.getMethodId() == null || germplasm.getMethodId() == 0) {
				throw new MiddlewareQueryException(this.messageSource.getMessage("error.save.cross.methods.unavailable", new Object[] {},
						Locale.getDefault()));
			}
		}
	}

	protected void applyCrossNameSettingToImportedCrosses(final CrossSetting setting, final List<ImportedCrosses> importedCrosses) {
		Integer nextNumberInSequence = this.getNextNumberInSequence(setting.getCrossNameSetting());
		Integer entryIdCounter = 0;

		for (final ImportedCrosses cross : importedCrosses) {
			entryIdCounter++;
			cross.setEntryId(entryIdCounter);
			cross.setEntryCode(String.valueOf(entryIdCounter));
			cross.setDesig(this.buildDesignationNameInSequence(cross, nextNumberInSequence++, setting));

			// this would set the correct cross string depending if the use is cimmyt wheat
			final Germplasm germplasm = new Germplasm();
			germplasm.setGnpgs(2);
			germplasm.setGid(Integer.MAX_VALUE);
			germplasm.setGpid1(Integer.valueOf(cross.getFemaleGid()));
			germplasm.setGpid2(Integer.valueOf(cross.getMaleGid()));
			final String crossString = this.getCross(germplasm, cross, setting.getCrossNameSetting().getSeparator());

			cross.setCross(crossString);
		}
	}

	/**
	 * this method overwrites the naming settings with the defined rules from the DB if the breeding method was provided
	 *
	 * @param setting
	 */
	protected void processBreedingMethodProcessCodes(final CrossSetting setting) {
		final CrossNameSetting nameSetting = setting.getCrossNameSetting();
		final BreedingMethodSetting breedingMethodSetting = setting.getBreedingMethodSetting();

		final Method method = this.germplasmDataManager.getMethodByID(breedingMethodSetting.getMethodId());

		// overwrite other name setting items using method values here
		if (method != null && method.getSuffix() != null) {
			nameSetting.setSuffix(method.getSuffix());
		}
	}

	@Override
	public String getCross(final Germplasm germplasm, final ImportedCrosses crosses, final String separator) {
		try {
			if (PedigreeFactory.isCimmytWheat(this.crossExpansionProperties.getProfile(), this.contextUtil.getProjectInContext()
					.getCropType().getCropName())) {
				return this.pedigreeService.getCrossExpansion(germplasm, null, this.crossExpansionProperties);
			}
			return this.buildCrossName(crosses, separator);
		} catch (final MiddlewareQueryException e) {
			throw new RuntimeException("There was a problem accessing communicating with the database. "
					+ "Please contact support for further help.", e);
		}

	}

	public Integer getFormattedHarvestDate(final String harvestDate) {
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
	 * @param germplasm germplasm instance into which gdate need to be set.
	 * @param harvestDate date given using user form.
	 */
	public void populateGermplasmDate(final Germplasm germplasm, final String harvestDate) {
		final Integer formattedHarvestDate = this.getFormattedHarvestDate(harvestDate);

		if (formattedHarvestDate != 0) {
			germplasm.setGdate(formattedHarvestDate);
			return;
		}

		germplasm.setGdate(DateUtil.getCurrentDateAsIntegerValue());
	}

	// FIXME the methods getPairs() and generateGermplasmNamePairs() should be combined into one
	protected List<Pair<Germplasm, Name>> generateGermplasmNamePairs(final CrossSetting crossSetting,
			final List<ImportedCrosses> importedCrosses, final Integer userId, final boolean hasPlotDuplicate, boolean isTrimed)
			throws MiddlewareQueryException {

		final List<Pair<Germplasm, Name>> pairList = new ArrayList<>();

		final AdditionalDetailsSetting additionalDetailsSetting = crossSetting.getAdditionalDetailsSetting();

		Integer harvestLocationId = 0;

		if (additionalDetailsSetting.getHarvestLocationId() != null) {
			harvestLocationId = additionalDetailsSetting.getHarvestLocationId();
		}

		for (final ImportedCrosses cross : importedCrosses) {

			if (FieldbookUtil.isContinueCrossingMerge(hasPlotDuplicate, crossSetting.isPreservePlotDuplicates(), cross)) {
				continue;
			}

			Germplasm germplasm = null;
			Name name = null;

			// Retrieve the germplasm (cross) from database: In case of Nursery -> Crossing workflows, we expect the GID to always
			// exist as crosses are created in crossing manager and persisted.
			if (cross.getGid() != null) {
				germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(cross.getGid()));

				// Find the existing name that was created in crossing manager. There should only be one and must be preferred.
				name = germplasm.findPreferredName();
			} else {
				germplasm = new Germplasm();
				// In case of importing crosses, the crosses are not yet persisted, GID will be null. We populate data from spreadsheet,
				// create new Germplasm.
				this.updateConstantFields(germplasm, userId);
				germplasm.setGpid1(Integer.valueOf(cross.getFemaleGid()));
				germplasm.setGpid2(Integer.valueOf(cross.getMaleGid()));

				germplasm.setMethodId(cross.getBreedingMethodId());

				// For import we always create new name
				name = new Name();
				name.setReferenceId(CrossingServiceImpl.NAME_REFID);
			}

			// Set germplasm date based on user input or information from source data.
			this.populateGermplasmDate(germplasm, additionalDetailsSetting.getHarvestDate());
			// Set the location based on what is selected as harvest location in both cases of crossing.
			germplasm.setLocationId(harvestLocationId);

			// Common name updates
			String designation = cross.getDesig();
			
			if(designation.length() > MAX_CROSS_NAME_SIZE){
				designation = designation.substring(0, MAX_CROSS_NAME_SIZE - 1);
				designation = designation + TRUNCATED;
				isTrimed = true;
			}
			
			name.setNval(designation);
			name.setUserId(userId);
			name.setNdate(germplasm.getGdate());
			name.setLocationId(harvestLocationId);

			final List<Name> names = new ArrayList<>();
			names.add(name);
			cross.setNames(names);

			pairList.add(new ImmutablePair<>(germplasm, name));
		}
		return pairList;
	}

	protected void updateConstantFields(final Germplasm germplasm, final Integer userId) {
		germplasm.setGnpgs(CrossingServiceImpl.GERMPLASM_GNPGS);
		germplasm.setGrplce(CrossingServiceImpl.GERMPLASM_GRPLCE);
		germplasm.setLgid(CrossingServiceImpl.GERMPLASM_LGID);
		germplasm.setMgid(CrossingServiceImpl.GERMPLASM_MGID);
		germplasm.setUserId(userId);
		germplasm.setReferenceId(CrossingServiceImpl.GERMPLASM_REFID);
	}

	protected Integer getNextNumberInSequence(final CrossNameSetting setting) throws MiddlewareQueryException {

		final String lastPrefixUsed = this.buildPrefixString(setting);
		int nextNumberInSequence = 1;

		final Integer startNumber = setting.getStartNumber();
		if (startNumber != null && startNumber > 0) {
			nextNumberInSequence = startNumber;
		} else {
			final String nextSequenceNumberString =
					this.germplasmDataManager.getNextSequenceNumberForCrossName(lastPrefixUsed.toUpperCase().trim());
			nextNumberInSequence = Integer.parseInt(nextSequenceNumberString);
		}

		return nextNumberInSequence;

	}

	protected String buildDesignationNameInSequence(final ImportedCrosses importedCrosses, final Integer number, final CrossSetting setting) {
		final CrossNameSetting nameSetting = setting.getCrossNameSetting();
		final Pattern pattern = Pattern.compile(ExpressionHelper.PROCESS_CODE_PATTERN);
		final StringBuilder sb = new StringBuilder();
		final String uDSuffix = nameSetting.getSuffix();
		sb.append(this.buildPrefixString(nameSetting));
		sb.append(this.getNumberWithLeadingZeroesAsString(number, nameSetting));

		if (importedCrosses != null && !StringUtils.isEmpty(importedCrosses.getRawBreedingMethod())) {
			final Method method = this.germplasmDataManager.getMethodByCode(importedCrosses.getRawBreedingMethod());
			if (!StringUtils.isEmpty(method.getSuffix())) {
				nameSetting.setSuffix(method.getSuffix());
			}
		}

		if (!StringUtils.isEmpty(nameSetting.getSuffix())) {
			String suffix = nameSetting.getSuffix().trim();
			final Matcher matcher = pattern.matcher(suffix);

			if (matcher.find()) {
				suffix = this.evaluateSuffixProcessCode(importedCrosses, setting, matcher.group());
			}

			sb.append(this.buildSuffixString(nameSetting, suffix));
		}
		nameSetting.setSuffix(uDSuffix);
		return sb.toString();
	}

	protected String evaluateSuffixProcessCode(final ImportedCrosses crosses, final CrossSetting setting, final String processCode) {
		final ProcessCodeOrderedRule rule = this.processCodeRuleFactory.getRuleByProcessCode(processCode);

		final CrossingRuleExecutionContext crossingRuleExecutionContext =
				new CrossingRuleExecutionContext(new ArrayList<String>(), setting, crosses.getMaleGid() != null ? Integer.valueOf(crosses
						.getMaleGid()) : 0, crosses.getFemaleGid() != null ? Integer.valueOf(crosses.getFemaleGid()) : 0,
						this.germplasmDataManager, this.pedigreeDataManager);

		try {
			return (String) rule.runRule(crossingRuleExecutionContext);
		} catch (final RuleException e) {
			LOG.error(e.getMessage(), e);
			return "";
		}
	}

	protected String buildCrossName(final ImportedCrosses crosses, final String separator) {
		return crosses.getFemaleDesig() + separator + crosses.getMaleDesig();
	}

	protected String buildPrefixString(final CrossNameSetting setting) {
		final String prefix = !StringUtils.isEmpty(setting.getPrefix()) ? setting.getPrefix().trim() : "";
		if (setting.isAddSpaceBetweenPrefixAndCode()) {
			return prefix + " ";
		}
		return prefix;
	}

	protected String buildSuffixString(final CrossNameSetting setting, final String suffix) {
		if (setting.isAddSpaceBetweenSuffixAndCode()) {
			return " " + suffix.trim();
		}
		return suffix.trim();
	}

	protected String getNumberWithLeadingZeroesAsString(final Integer number, final CrossNameSetting setting) {
		final StringBuilder sb = new StringBuilder();
		final String numberString = number.toString();
		final Integer numOfDigits = setting.getNumOfDigits();

		if (numOfDigits != null && numOfDigits > 0) {
			final int numOfZerosNeeded = numOfDigits - numberString.length();
			if (numOfZerosNeeded > 0) {
				for (int i = 0; i < numOfZerosNeeded; i++) {
					sb.append("0");
				}
			}

		}
		sb.append(number);
		return sb.toString();
	}

	public Integer getIDForUserDefinedFieldCrossingName() throws MiddlewareQueryException {

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
	public void processCrossBreedingMethod(CrossSetting crossSetting, ImportedCrossesList importedCrossesList) {
		final BreedingMethodSetting methodSetting = crossSetting.getBreedingMethodSetting();

		for (ImportedCrosses importedCrosses : importedCrossesList.getImportedCrosses()) {
			// if imported cross contains raw breeding method code we use that to populate the breeding method
			if (!StringUtils.isEmpty(importedCrosses.getRawBreedingMethod())) {
				final Method breedingMethod = this.germplasmDataManager.getMethodByCode(importedCrosses.getRawBreedingMethod());

				if (breedingMethod != null && breedingMethod.getMid() != null && breedingMethod.getMid() != 0) {
					importedCrosses.setBreedingMethodId(breedingMethod.getMid());
				} else {
                    // TODO address case where breeding method does not exist in the parser level to avoid having this case during the saving flow
                    importedCrosses.setBreedingMethodId(0);
                }
			}

			// if at this point, there is already breeding method info available on the imported cross (from import file, etc, we proceed to next)
			if (importedCrosses.isBreedingMethodInformationAvailable()) {
				continue;
			}

			// if breeding method is based on status of parental lines, we calculate the resulting breeding method per germplasm
            // currently, the convention is that parental lines will be used as basis if user does not select any method
			if (methodSetting.getMethodId() == null || methodSetting.getMethodId() == 0) {
				final Integer femaleGid = Integer.parseInt(importedCrosses.getFemaleGid());
				final Integer maleGid = Integer.parseInt(importedCrosses.getMaleGid());

				Triple<Germplasm, Germplasm, Germplasm> femaleLine = retrieveParentGermplasmObjects(femaleGid);
				Triple<Germplasm, Germplasm, Germplasm> maleLine = retrieveParentGermplasmObjects(maleGid);

				importedCrosses.setBreedingMethodId(CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleLine.getLeft(),
						maleLine.getLeft(), femaleLine.getMiddle(), femaleLine.getRight(), maleLine.getMiddle(), maleLine.getRight()));
			} else {
				// otherwise, we use the breeding method selected
				importedCrosses.setBreedingMethodId(methodSetting.getMethodId());

			}
		}
	}

	protected Triple<Germplasm, Germplasm, Germplasm> retrieveParentGermplasmObjects(final Integer germplasmID) {
		final Germplasm parent = germplasmDataManager.getGermplasmByGID(germplasmID);

		Germplasm motherOfParent = null;
		Germplasm fatherOfParent = null;
		if (parent != null) {
			motherOfParent = germplasmDataManager.getGermplasmByGID(parent.getGpid1());
			fatherOfParent = germplasmDataManager.getGermplasmByGID(parent.getGpid2());
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
}
