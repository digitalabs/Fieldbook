
package com.efficio.fieldbook.web.common.service.impl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.ruleengine.ProcessCodeOrderedRule;
import org.generationcp.commons.ruleengine.ProcessCodeRuleFactory;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.cross.CrossingRuleExecutionContext;
import org.generationcp.commons.settings.AdditionalDetailsSetting;
import org.generationcp.commons.settings.BreedingMethodSetting;
import org.generationcp.commons.settings.CrossNameSetting;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.CrossingUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.util.ExpressionHelper;
import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.common.service.CrossingService;

/**
 * Created by cyrus on 1/23/15.
 */
public class CrossingServiceImpl implements CrossingService {

	public static final Integer GERMPLASM_GNPGS = 2;
	public static final Integer GERMPLASM_GRPLCE = 0;
	public static final Integer GERMPLASM_LGID = 0;
	public static final Integer GERMPLASM_MGID = 0;
	public static final Integer GERMPLASM_REFID = 0;
	public static final Integer NAME_REFID = 0;
	public static final String[] USER_DEF_FIELD_CROSS_NAME = {"CROSS NAME", "CROSSING NAME"};

	private static final Logger LOG = LoggerFactory.getLogger(CrossingServiceImpl.class);

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

	@Override
	public ImportedCrossesList parseFile(MultipartFile file) throws FileParsingException {
		return this.crossingTemplateParser.parseFile(file, null);
	}

	@Override
	public void applyCrossSetting(CrossSetting crossSetting, ImportedCrossesList importedCrossesList, Integer userId)
			throws MiddlewareQueryException {

		this.applyCrossNameSettingToImportedCrosses(crossSetting, importedCrossesList.getImportedCrosses());
		Map<Germplasm, Name> germplasmToBeSaved =
				this.generateGermplasmNameMap(crossSetting, importedCrossesList.getImportedCrosses(), userId,
						importedCrossesList.hasPlotDuplicate());

		boolean isValid = this.verifyGermplasmMethodPresent(germplasmToBeSaved);

		if (!isValid) {
			throw new MiddlewareQueryException(this.messageSource.getMessage("error.save.cross.methods.unavailable", new Object[] {},
					Locale.getDefault()));
		}

		List<Integer> savedGermplasmIds = this.saveGermplasm(germplasmToBeSaved);

		Iterator<Integer> germplasmIdIterator = savedGermplasmIds.iterator();
		for (ImportedCrosses cross : importedCrossesList.getImportedCrosses()) {
			// this will do the merging and using the gid and cross from the initial duplicate
			if (FieldbookUtil.isContinueCrossingMerge(importedCrossesList.hasPlotDuplicate(), crossSetting.isPreservePlotDuplicates(),
					cross)) {
				FieldbookUtil.mergeCrossesPlotDuplicateData(cross, importedCrossesList.getImportedCrosses());
				continue;
			}
			Integer newGid = germplasmIdIterator.next();
			cross.setGid(newGid.toString());
		}

	}

	protected boolean verifyGermplasmMethodPresent(Map<Germplasm, Name> germplasmNameMap) {
		for (Germplasm germplasm : germplasmNameMap.keySet()) {
			if (germplasm.getMethodId() == null || germplasm.getMethodId() == 0) {
				return false;
			}
		}

		return true;
	}

	protected void applyCrossNameSettingToImportedCrosses(CrossSetting setting, List<ImportedCrosses> importedCrosses)
			throws MiddlewareQueryException {

		processBreedingMethodProcessCodes(setting);

		Integer nextNumberInSequence = this.getNextNumberInSequence(setting.getCrossNameSetting());
		Integer entryIdCounter = 0;

		for (ImportedCrosses cross : importedCrosses) {
			updateCrossNameSettingIfNecessary(setting.getCrossNameSetting(), cross);
			entryIdCounter++;
			cross.setEntryId(entryIdCounter);
			cross.setEntryCode(String.valueOf(entryIdCounter));
			cross.setDesig(this.buildDesignationNameInSequence(cross, nextNumberInSequence++, setting));

			// this would set the correct cross string depending if the use is cimmyt wheat
			Germplasm germplasm = new Germplasm();
			germplasm.setGnpgs(2);
			germplasm.setGid(Integer.MAX_VALUE);
			germplasm.setGpid1(Integer.valueOf(cross.getFemaleGid()));
			germplasm.setGpid2(Integer.valueOf(cross.getMaleGid()));
			String crossString = this.getCross(germplasm, cross, setting.getCrossNameSetting().getSeparator());

			cross.setCross(crossString);
		}
	}

	protected void updateCrossNameSettingIfNecessary(CrossNameSetting setting, ImportedCrosses cross) throws MiddlewareQueryException{
		if (StringUtils.isEmpty(cross.getRawBreedingMethod())) {
			setting.setSuffix("");
			return;
		}

		Method method = germplasmDataManager.getMethodByCode(cross.getRawBreedingMethod());

		if (method == null) {
			setting.setSuffix("");
			return;
		}

		if (!StringUtils.isEmpty(method.getSuffix())) {
			setting.setSuffix(method.getSuffix());
		} else {
			setting.setSuffix("");
		}
		

	}

	protected void processBreedingMethodProcessCodes(CrossSetting setting) {
		CrossNameSetting nameSetting = setting.getCrossNameSetting();
		BreedingMethodSetting breedingMethodSetting = setting.getBreedingMethodSetting();

		try {
			Method method = germplasmDataManager.getMethodByID(breedingMethodSetting.getMethodId());

			// overwrite other name setting items using method values here

			if (method != null && method.getSuffix() !=null) {
				nameSetting.setSuffix(method.getSuffix());
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getCross(final Germplasm germplasm, ImportedCrosses crosses, String separator) {
		try {
			if (CrossingUtil.isCimmytWheat(this.crossExpansionProperties.getProfile(), this.contextUtil.getProjectInContext().getCropType()
					.getCropName())) {
				return this.pedigreeService.getCrossExpansion(germplasm, null, this.crossExpansionProperties);
			}
			return this.buildCrossName(crosses, separator);
		} catch (MiddlewareQueryException e) {
			throw new RuntimeException("There was a problem accessing communicating with the database. "
					+ "Please contact support for further help.", e);
		}

	}

	protected Map<Germplasm, Name> generateGermplasmNameMap(CrossSetting crossSetting, List<ImportedCrosses> importedCrosses,
			Integer userId, boolean hasPlotDuplicate) throws MiddlewareQueryException {

		Map<Germplasm, Name> germplasmNameMap = new LinkedHashMap<>();
		Integer crossingNameTypeId = this.getIDForUserDefinedFieldCrossingName();
		AdditionalDetailsSetting additionalDetailsSetting = crossSetting.getAdditionalDetailsSetting();

		Integer dateIntValue = 0;
		Integer harvestLocationId = 0;

		if (additionalDetailsSetting.getHarvestLocationId() != null) {
			harvestLocationId = additionalDetailsSetting.getHarvestLocationId();
		}

		if (additionalDetailsSetting.getHarvestDate() != null) {
			String dateString = additionalDetailsSetting.getHarvestDate();
			String replacedDateString = dateString.replace("-", "");
			dateIntValue = Integer.parseInt(replacedDateString);
		}

		for (ImportedCrosses cross : importedCrosses) {

			if (FieldbookUtil.isContinueCrossingMerge(hasPlotDuplicate, crossSetting.isPreservePlotDuplicates(), cross)) {
				continue;
			}
			Germplasm germplasm = new Germplasm();
			Name name = new Name();

			this.updateConstantFields(germplasm, name, userId);

			germplasm.setGpid1(Integer.valueOf(cross.getFemaleGid()));
			germplasm.setGpid2(Integer.valueOf(cross.getMaleGid()));
			germplasm.setGdate(dateIntValue);
			germplasm.setLocationId(harvestLocationId);

			germplasm.setMethodId(0);

			Method breedingMethod = this.germplasmDataManager.getMethodByCode(cross.getRawBreedingMethod());

			if (breedingMethod != null && breedingMethod.getMid() != null && breedingMethod.getMid() != 0) {
				germplasm.setMethodId(breedingMethod.getMid());
			}

			name.setNval(cross.getDesig());
			name.setNdate(dateIntValue);
			name.setTypeId(crossingNameTypeId);
			name.setLocationId(harvestLocationId);

			List<Name> names = new ArrayList<>();
			names.add(name);
			cross.setNames(names);

			germplasmNameMap.put(germplasm, name);

		}

		CrossingUtil.applyBreedingMethodSetting(this.germplasmDataManager, crossSetting, germplasmNameMap);
		CrossingUtil.applyMethodNameType(this.germplasmDataManager, germplasmNameMap, crossingNameTypeId);
		return germplasmNameMap;
	}

	protected void updateConstantFields(Germplasm germplasm, Name name, Integer userId) {
		germplasm.setGnpgs(CrossingServiceImpl.GERMPLASM_GNPGS);
		germplasm.setGrplce(CrossingServiceImpl.GERMPLASM_GRPLCE);
		germplasm.setLgid(CrossingServiceImpl.GERMPLASM_LGID);
		germplasm.setMgid(CrossingServiceImpl.GERMPLASM_MGID);
		germplasm.setUserId(userId);
		germplasm.setReferenceId(CrossingServiceImpl.GERMPLASM_REFID);

		name.setReferenceId(CrossingServiceImpl.NAME_REFID);
		name.setUserId(userId);
	}

	protected List<Integer> saveGermplasm(Map<Germplasm, Name> germplasmNameMap) throws MiddlewareQueryException {
		return this.germplasmDataManager.addGermplasm(germplasmNameMap);
	}

	protected Integer getNextNumberInSequence(CrossNameSetting setting) throws MiddlewareQueryException {

		String lastPrefixUsed = this.buildPrefixString(setting);
		int nextNumberInSequence = 1;

		Integer startNumber = setting.getStartNumber();
		if (startNumber != null && startNumber > 0) {
			nextNumberInSequence = startNumber;
		} else {
			String nextSequenceNumberString =
					this.germplasmDataManager.getNextSequenceNumberForCrossName(lastPrefixUsed.toUpperCase().trim());
			nextNumberInSequence = Integer.parseInt(nextSequenceNumberString);
		}

		return nextNumberInSequence;

	}

	protected String buildDesignationNameInSequence(ImportedCrosses importedCrosses, Integer number, CrossSetting setting) {
		CrossNameSetting nameSetting = setting.getCrossNameSetting();

		StringBuilder sb = new StringBuilder();
		sb.append(this.buildPrefixString(nameSetting));
		sb.append(this.getNumberWithLeadingZeroesAsString(number, nameSetting));


		if (!StringUtils.isEmpty(nameSetting.getSuffix())) {
			String suffix = nameSetting.getSuffix().trim();
			Pattern pattern = Pattern.compile(ExpressionHelper.PROCESS_CODE_PATTERN);
			Matcher matcher = pattern.matcher(suffix);

			if (matcher.find()) {
				suffix = evaluateSuffixProcessCode(importedCrosses, setting,matcher.group());
			}

			sb.append(this.buildSuffixString(nameSetting, suffix));
		}
		return sb.toString();
	}

	protected String evaluateSuffixProcessCode(ImportedCrosses crosses, CrossSetting setting, String processCode) {
		ProcessCodeOrderedRule rule = processCodeRuleFactory.getRuleByProcessCode(processCode);

		CrossingRuleExecutionContext crossingRuleExecutionContext = new CrossingRuleExecutionContext(new ArrayList<String>(), setting,
				crosses.getMaleGid() != null ? Integer.valueOf(crosses.getMaleGid()) : 0,
				crosses.getFemaleGid() != null ? Integer.valueOf(crosses.getFemaleGid()) : 0,
				germplasmDataManager, pedigreeDataManager);

		try {
			return (String) rule.runRule(crossingRuleExecutionContext);
		} catch (RuleException e) {
			LOG.error(e.getMessage(), e);
			return "";
		}
	}

	protected String buildCrossName(ImportedCrosses crosses, String separator) {
		return crosses.getFemaleDesig() + separator + crosses.getMaleDesig();
	}

	protected String buildPrefixString(CrossNameSetting setting) {
		String prefix = setting.getPrefix().trim();
		if (setting.isAddSpaceBetweenPrefixAndCode()) {
			return prefix + " ";
		}
		return prefix;
	}

	protected String buildSuffixString(CrossNameSetting setting, String suffix) {

		if (setting.isAddSpaceBetweenSuffixAndCode()) {
			return " " + suffix;
		}
		return suffix;
	}

	protected String getNumberWithLeadingZeroesAsString(Integer number, CrossNameSetting setting) {
		StringBuilder sb = new StringBuilder();
		String numberString = number.toString();
		Integer numOfDigits = setting.getNumOfDigits();

		if (numOfDigits != null && numOfDigits > 0) {
			int numOfZerosNeeded = numOfDigits - numberString.length();
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

		List<UserDefinedField> nameTypes = this.germplasmListManager.getGermplasmNameTypes();
		for (UserDefinedField type : nameTypes) {
			for (String crossNameValue : CrossingServiceImpl.USER_DEF_FIELD_CROSS_NAME) {
				if (crossNameValue.equalsIgnoreCase(type.getFcode()) || crossNameValue.equalsIgnoreCase(type.getFname())) {
					return type.getFldno();
				}
			}
		}

		return null;
	}

	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;

	}
}
