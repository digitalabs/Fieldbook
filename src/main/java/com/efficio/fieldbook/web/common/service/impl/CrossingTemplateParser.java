
package com.efficio.fieldbook.web.common.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.CrossesListDescriptionSheetParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCondition;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedFactor;
import org.generationcp.commons.parsing.pojo.ImportedVariate;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.ListDataProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.util.AppConstants;

/**
 * This parses a Crossing Template Excel file Note that this class is stateful, declare in spring app context
 * as prototyped scope
 */
public class CrossingTemplateParser extends AbstractExcelFileParser<ImportedCrossesList> {

	public static final String NO_REFERENCES_ERROR_DESC = "study.import.crosses.error.no.references";
	protected static final int OBSERVATION_SHEET_NO = 1;

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CrossingTemplateParser.class);
	private static final Map<StudyType, GermplasmListType> STUDY_TYPE_TO_LIST_TYPE_MAP = new HashMap<>();

	static {
		CrossingTemplateParser.STUDY_TYPE_TO_LIST_TYPE_MAP.put(StudyType.N, GermplasmListType.NURSERY);
		CrossingTemplateParser.STUDY_TYPE_TO_LIST_TYPE_MAP.put(StudyType.T, GermplasmListType.TRIAL);
	}

	private final Map<String, Integer> observationColumnMap = new HashMap<>();
	private ImportedCrossesList importedCrossesList;

	/**
	 * Resources
	 */
	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private UserSelection studySelection;

	@Resource
	private CrossingService crossingService;

	@Resource
	private UserDataManager userDataManager;

	public CrossingTemplateParser() {

	}

	@Override
	public ImportedCrossesList parseWorkbook(final Workbook workbook, final Map<String, Object> additionalParams) throws FileParsingException {
		this.workbook = workbook;
		try {

			final CrossesListDescriptionSheetParser<ImportedCrossesList> crossesListDescriptionSheetParser =
					new CrossesListDescriptionSheetParser<>(new ImportedCrossesList(), this.userDataManager);

			this.importedCrossesList = crossesListDescriptionSheetParser.parseWorkbook(this.workbook, additionalParams);

			this.parseObservationSheet(this.contextUtil.getCurrentProgramUUID());
		} catch (final MiddlewareQueryException e) {
			CrossingTemplateParser.LOG.debug(e.getMessage(), e);
			throw new FileParsingException(this.messageSource.getMessage(CrossingTemplateParser.NO_REFERENCES_ERROR_DESC, new Object[] {},
					Locale.getDefault()));
		}

		return this.importedCrossesList;
	}

	/**
	 * @throws org.generationcp.commons.parsing.FileParsingException
	 */
	protected void parseObservationSheet(final String programUUID) throws FileParsingException {
		this.validateObservationsHeader();

		String femaleNursery = null;
		final List<ImportedCondition> importedConditions = this.importedCrossesList.getImportedConditions();
		for (final ImportedCondition importedCondition : importedConditions) {
			final String condition = importedCondition.getCondition();
			if (condition != null && condition.equals(AppConstants.FEMALE_NURSERY.getString())) {
				femaleNursery = importedCondition.getValue();
			}
		}

		validateFemaleNursery(femaleNursery);

		int currentRow = 1;
		final int headerSize = this.getLastCellNum(CrossingTemplateParser.OBSERVATION_SHEET_NO, 0);

		Set<Integer> femalePlotNos = new HashSet<>();
		// map key is male nursery name while map value is set of male plot nos for that male nursery
		Map<String, Set<Integer>> maleNurseriesWithPlotNos = new HashMap<>();
		while (!this.isRowEmpty(CrossingTemplateParser.OBSERVATION_SHEET_NO, currentRow,
				headerSize)) {

			final String femalePlotNoStr = this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, currentRow,
					this.observationColumnMap.get(AppConstants.FEMALE_PLOT.getString()));
			String maleNursery = this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, currentRow,
					this.observationColumnMap.get(AppConstants.MALE_NURSERY.getString()));
			final String malePlotNoStr = this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, currentRow,
					this.observationColumnMap.get(AppConstants.MALE_PLOT.getString()));
			final String breedingMethod = this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, currentRow,
					this.observationColumnMap.get(AppConstants.BREEDING_METHOD.getString()));
			final String strCrossingDate = this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, currentRow,
					this.observationColumnMap.get(AppConstants.CROSSING_DATE.getString()));
			final String notes = this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, currentRow,
					this.observationColumnMap.get(AppConstants.NOTES.getString()));

			validateObservationRow(femalePlotNoStr, malePlotNoStr, currentRow, strCrossingDate);

			Integer crossingDate = null;
			if (!StringUtils.isBlank(strCrossingDate)) {
				crossingDate = Integer.valueOf(strCrossingDate);
			}

			if (StringUtils.isBlank(maleNursery)) {
				maleNursery = femaleNursery;
			}

			// collect female plot nos for one-off Middleware lookup
			final Integer femalePlotNo = Integer.valueOf(femalePlotNoStr);
			femalePlotNos.add(femalePlotNo);
			
			// group together male plots by male nursery for one-off Middleware lookup
			final Integer malePlotNo = Integer.valueOf(malePlotNoStr);
			if (maleNurseriesWithPlotNos.containsKey(maleNursery)){
				maleNurseriesWithPlotNos.get(maleNursery).add(malePlotNo);
			} else {
				final Set<Integer> plotSet = new HashSet<>();
				plotSet.add(malePlotNo);
				maleNurseriesWithPlotNos.put(maleNursery, plotSet);
			}
			
			final ImportedCrosses importedCrosses =
					new ImportedCrosses(femaleNursery, maleNursery, femalePlotNoStr, malePlotNoStr, currentRow);
			// Show source as "Pending" in initial dialogue.
			// Source (Plot Code) string is generated later in the proces and will be displayed in the final list generated.
			importedCrosses.setSource(ImportedCrosses.SEED_SOURCE_PENDING);
			importedCrosses.setOptionalFields(breedingMethod, crossingDate, notes);

			this.importedCrossesList.addImportedCrosses(importedCrosses);

			currentRow++;
		}
		
		lookupCrossParents(this.studySelection.getWorkbook().getStudyName(), 
				femalePlotNos, maleNurseriesWithPlotNos, programUUID);

	}

	protected void validateFemaleNursery(String femaleNursery) throws FileParsingException {

		if (femaleNursery == null || femaleNursery == "") {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.female.nursery.empty", new String[] {},
					LocaleContextHolder.getLocale()));
		}

		if (!femaleNursery.equals(this.studySelection.getWorkbook().getStudyName())) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.female.nursery.match", new String[] {},
					LocaleContextHolder.getLocale()));
		}
	}

	protected void validateObservationRow(final String femalePlotNo, final String malePlotNo, int currentRow, String strCrossingDate)
			throws FileParsingException {

		if (!(StringUtils.isNotBlank(femalePlotNo) && StringUtils.isNumeric(femalePlotNo))) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.row.femalePlot",
					new Integer[] {currentRow}, LocaleContextHolder.getLocale()));
		}

		if (!(StringUtils.isNotBlank(malePlotNo) && StringUtils.isNumeric(malePlotNo))) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.row.malePlot",
					new Integer[] {currentRow}, LocaleContextHolder.getLocale()));
		}

		if (!StringUtils.isBlank(strCrossingDate)) {
			if (!DateUtil.isValidDate(strCrossingDate)) {
				throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.row.crossing.date",
						new Integer[] {currentRow}, LocaleContextHolder.getLocale()));
			}
		}
	}

	/**
	 * Add warnings for non standard columns.
	 *
	 * @return
	 *
	 * @see <a href=
	 *      "https://leafnode.atlassian.net/wiki/display/CD/Cleanup+of+Flow+for+Importing+Crosses+and+Recording+Inventory#CleanupofFlowforImportingCrossesandRecordingInventory-ErrorHandling">Cleanup
	 *      of Flow for Importing Crosses and Recording Inventory</a>
	 * @return
	 * @throws FileParsingException If description columns are not present in observation sheet
	 */
	protected void validateObservationsHeader() throws FileParsingException {
		final Set<String> importedFactors = new HashSet<>();

		for (final ImportedFactor factor : this.importedCrossesList.getImportedFactors()) {
			importedFactors.add(factor.getFactor());
		}

		final Set<String> importedVariates = new HashSet<>();

		for (final ImportedVariate variate : this.importedCrossesList.getImportedVariates()) {
			importedVariates.add(variate.getVariate());
		}

		final int headerSize = this.getLastCellNum(CrossingTemplateParser.OBSERVATION_SHEET_NO, 0);

		final Set<String> invalidColumns = new HashSet<>();

		for (int i = 0; i < headerSize; i++) {
			// search the current header
			final String obsHeader = this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, 0, i);

			final boolean inFactors = importedFactors.contains(obsHeader);
			final boolean inVariates = importedVariates.contains(obsHeader);

			if (!inFactors && !inVariates) {
				invalidColumns.add(obsHeader);
			} else {
				this.observationColumnMap.put(obsHeader, i);
			}
		}

		if (!invalidColumns.isEmpty()) {
			this.importedCrossesList
					.addWarningMessages(this.messageSource.getMessage("error.import.crosses.observation.headers.invalid.columns",
							new String[] {StringUtils.join(invalidColumns, ", ")}, LocaleContextHolder.getLocale()));
		}

		// Validate Factor and Variates columns

		Set<String> mandatoryColumns = new HashSet<>();
		Set<String> missingColumns = new HashSet<>();

		mandatoryColumns.addAll(importedFactors);
		mandatoryColumns.addAll(importedVariates);

		for (final String mandatoryCol : mandatoryColumns) {
			if (!this.observationColumnMap.containsKey(mandatoryCol)) {
				missingColumns.add(mandatoryCol);
			}
		}

		if (!missingColumns.isEmpty()) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.headers",
					new String[] {StringUtils.join(missingColumns, ", ")}, LocaleContextHolder.getLocale()));
		}
	}


	void lookupCrossParents(final String femaleNurseryName, final Set<Integer> femalePlotNos,
			final Map<String, Set<Integer>> maleNurseriesWithPlotNos, final String programUUID)
					throws MiddlewareQueryException, FileParsingException {
		
		final Map<Integer, ListDataProject> femalePlotMap = getPlotToListDataProjectMapForNursery(femaleNurseryName, femalePlotNos, programUUID);
		// Create map of male nurseries to its plotToListDataProject lookup
		// for each male nursery, lookup the associated ListDataProject of specified male plot #s
		Map<String, Map<Integer, ListDataProject>> maleNurseriesPlotMap = new HashMap<>();
		for (Map.Entry<String, Set<Integer>> entry : maleNurseriesWithPlotNos.entrySet()) {
			final String maleNurseryName = entry.getKey();
			maleNurseriesPlotMap.put(maleNurseryName, getPlotToListDataProjectMapForNursery(maleNurseryName, entry.getValue(), programUUID));
		}
		
		
		// Set looked up GIDs and names of parents to ImportedCrosses object
		for (ImportedCrosses crosses : this.importedCrossesList.getImportedCrosses()){
			final String crossMaleNursery = crosses.getMaleStudyName();
			final Map<Integer, ListDataProject> malePlotMap = maleNurseriesPlotMap.get(crossMaleNursery);
			final Integer crossFemalePlotNo = Integer.valueOf(crosses.getFemalePlotNo());
			final Integer crossMalePlotNo = Integer.valueOf(crosses.getMalePlotNo());
			
			if (femalePlotMap.containsKey(crossFemalePlotNo)){
				final ListDataProject femaleListData = femalePlotMap.get(crossFemalePlotNo);
				crosses.setFemaleGid(femaleListData.getGermplasmId().toString());
				crosses.setFemaleDesig(femaleListData.getDesignation());
			} else {
				throw new FileParsingException(this.messageSource.getMessage("no.list.data.for.plot",
						new Object[] {femaleNurseryName, crossFemalePlotNo}, LocaleContextHolder.getLocale()));
			}
			if (malePlotMap != null && malePlotMap.containsKey(crossMalePlotNo)){
				final ListDataProject maleListData = malePlotMap.get(crossMalePlotNo);
				crosses.setMaleGid(maleListData.getGermplasmId().toString());
				crosses.setMaleDesig(maleListData.getDesignation());
			} else {
				throw new FileParsingException(this.messageSource.getMessage("no.list.data.for.plot",
						new Object[] {crossMaleNursery, crossMalePlotNo}, LocaleContextHolder.getLocale()));
			}
			
			final Germplasm germplasm = new Germplasm();
			germplasm.setGnpgs(2);
			germplasm.setGid(Integer.MAX_VALUE);
			germplasm.setGpid1(Integer.valueOf(crosses.getFemaleGid()));
			germplasm.setGpid2(Integer.valueOf(crosses.getMaleGid()));
			final String crossString = this.crossingService.getCross(germplasm, crosses, "/");
			crosses.setCross(crossString);
		}

	}
	
	/**
	 * Returns map of plot numbers to corresponding ListDataProject record from specified plot numbers
	 * of given nursery 
	 * 
	 * @param nurseryName
	 * @param plotNos
	 * @param programUUID
	 * @return
	 * @throws FileParsingException
	 */
	Map<Integer, ListDataProject> getPlotToListDataProjectMapForNursery(final String nurseryName, final Set<Integer> plotNos,
			final String programUUID) throws FileParsingException {
		// 1. retrieve study ID of parent nursery
		final Integer studyId =
				this.studyDataManager.getStudyIdByNameAndProgramUUID(nurseryName, programUUID);

		if (null == studyId) {
			throw new FileParsingException(this.messageSource.getMessage("no.such.study.exists", new String[] {nurseryName},
					LocaleContextHolder.getLocale()));
		}

		// 2. Lookup Listdataproject of parent nursery and given plot #s
		final StudyType studyType = this.studyDataManager.getStudyType(studyId);
		Map<Integer, ListDataProject> plotToGIDMap = this.fieldbookMiddlewareService.getListDataProjectByStudy(studyId,
				CrossingTemplateParser.STUDY_TYPE_TO_LIST_TYPE_MAP.get(studyType), plotNos);
		
		return plotToGIDMap;
	}
	

}
