
package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.CrossingService;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.generationcp.commons.constant.AppConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.CrossesListDescriptionSheetParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCondition;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedFactor;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.commons.parsing.pojo.ImportedVariate;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.germplasm.ImportedCrossParent;
import org.generationcp.middleware.service.api.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This parses a Crossing Template Excel file Note that this class is stateful, declare in spring app context as prototyped scope
 */
public class CrossingTemplateParser extends AbstractExcelFileParser<ImportedCrossesList> {

	private static final String NO_REFERENCES_ERROR_DESC = "study.import.crosses.error.no.references";
	private static final String OBSERVATION_SHEET_NAME = "Observation";

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CrossingTemplateParser.class);

	private final Map<String, Integer> observationColumnMap = new HashMap<>();
	private ImportedCrossesList importedCrossesList;
	private Integer observationSheetIndex;

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
	private UserService userService;

	public CrossingTemplateParser() {

	}

	@Override
	public ImportedCrossesList parseWorkbook(final Workbook workbook, final Map<String, Object> additionalParams)
			throws FileParsingException {
		this.workbook = workbook;
		this.observationSheetIndex = this.getSheetIndex(CrossingTemplateParser.OBSERVATION_SHEET_NAME);
		try {

			final CrossesListDescriptionSheetParser<ImportedCrossesList> crossesListDescriptionSheetParser =
					new CrossesListDescriptionSheetParser<>(new ImportedCrossesList(), this.userService);

			this.importedCrossesList = crossesListDescriptionSheetParser.parseWorkbook(this.workbook, additionalParams);

			this.parseObservationSheet(this.contextUtil.getCurrentProgramUUID());
		} catch (final MiddlewareQueryException e) {
			CrossingTemplateParser.LOG.debug(e.getMessage(), e);
			throw new FileParsingException(
					this.messageSource.getMessage(CrossingTemplateParser.NO_REFERENCES_ERROR_DESC, new Object[] {}, Locale.getDefault()));
		}

		return this.importedCrossesList;
	}

	/**
	 * @throws org.generationcp.commons.parsing.FileParsingException
	 */
	private void parseObservationSheet(final String programUUID) throws FileParsingException {
		this.validateObservationsHeader();

		String femaleStudy = null;
		final List<ImportedCondition> importedConditions = this.importedCrossesList.getImportedConditions();
		for (final ImportedCondition importedCondition : importedConditions) {
			final String condition = importedCondition.getCondition();
			if (condition != null && condition.equals(AppConstants.FEMALE_STUDY.getString())) {
				femaleStudy = importedCondition.getValue();
			}
		}

		this.validateFemaleStudy(femaleStudy);

		int currentRow = 1;
		final int headerSize = this.getLastCellNum(this.observationSheetIndex, 0);
		Set<Integer> femalePlotNos = new HashSet<>();
		// map key is male study name while map value is set of male plot nos for that male study
		Map<String, Set<Integer>> maleStudiesWithPlotNos = new HashMap<>();
		final Map<Integer, Triple<String, Integer, List<Integer>>> entryIdToCrossInfoMap = new HashMap<>();
		while (!this.isRowEmpty(this.observationSheetIndex, currentRow, headerSize)) {

			final String femalePlotNoString = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.FEMALE_PLOT.getString()));
			String maleStudy = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.MALE_STUDY.getString()));
			final String malePlotNoString = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.MALE_PLOT.getString()));
			final String breedingMethod = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.BREEDING_METHOD.getString()));
			final String strCrossingDate = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.CROSSING_DATE.getString()));
			final String notes = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.NOTES.getString()));

			this.validateObservationRow(femalePlotNoString, malePlotNoString, currentRow, strCrossingDate);

			Integer crossingDate = null;
			if (!StringUtils.isBlank(strCrossingDate)) {
				crossingDate = Integer.valueOf(strCrossingDate);
			}

			if (StringUtils.isBlank(maleStudy)) {
				maleStudy = femaleStudy;
			}


			// collect female plot nos for one-off Middleware lookup
			final Integer femalePlotNo = Integer.valueOf(femalePlotNoString);
			femalePlotNos.add(femalePlotNo);

			final List<Integer> malePlotNumbers = convertCommaSeparatedStringToList(malePlotNoString, currentRow);
			// group together male plots by male study for one-off Middleware lookup
			if (maleStudiesWithPlotNos.containsKey(maleStudy)){
				maleStudiesWithPlotNos.get(maleStudy).addAll(malePlotNumbers);
			} else {
				final Set<Integer> plotSet = new HashSet<>(malePlotNumbers);
				maleStudiesWithPlotNos.put(maleStudy, plotSet);
			}

			entryIdToCrossInfoMap.put(currentRow, new ImmutableTriple<>(maleStudy, femalePlotNo, malePlotNumbers));

			final ImportedCrosses importedCrosses = new ImportedCrosses(currentRow);
			// Show source as "Pending" in initial dialogue.
			// Source (Plot Code) string is generated later in the process and will be displayed in the final list generated.
			importedCrosses.setSource(ImportedCrosses.SEED_SOURCE_PENDING);
			importedCrosses.setOptionalFields(breedingMethod, crossingDate, notes);

			this.importedCrossesList.addImportedCrosses(importedCrosses);

			currentRow++;
		}

		this.lookupCrossParents(this.studySelection.getWorkbook().getStudyName(),
			femalePlotNos, maleStudiesWithPlotNos, entryIdToCrossInfoMap, programUUID);
	}


	/** Query for cross parents by looking up male and female plots then update the imported crosses in importedCrossesList
	 * with the values from parent germplasm.
	 * @param femaleStudyName - name of female study
	 * @param femalePlotNos - plot numbers to look up for female study
	 * @param maleStudiesWithPlotNos - map of male studies to corresponsing male plot numbers
	 * @param entryIdToCrossInfoMap - map of entry id to study name
	 * @param programUUID - program unique ID
	 * @throws FileParsingException
	 */

	void lookupCrossParents(final String femaleStudyName, final Set<Integer> femalePlotNos,
		final Map<String, Set<Integer>> maleStudiesWithPlotNos, final Map<Integer, Triple<String, Integer, List<Integer>>> entryIdToCrossInfoMap,
		final String programUUID) throws FileParsingException {

		final Map<Integer, ImportedCrossParent> femalePlotMap =
			getPlotToImportedCrossParentMapForStudy(femaleStudyName, femalePlotNos, programUUID);
		// Create map of male studies to its plotToListDataProject lookup
		// for each male study, lookup the associated ListDataProject of specified male plot #s
		Map<String, Map<Integer, ImportedCrossParent>> maleNurseriesPlotMap = new HashMap<>();
		for (Map.Entry<String, Set<Integer>> entry : maleStudiesWithPlotNos.entrySet()) {
			final String maleStudyName = entry.getKey();
			maleNurseriesPlotMap
				.put(maleStudyName, getPlotToImportedCrossParentMapForStudy(maleStudyName, entry.getValue(), programUUID));
		}

		// Set looked up GIDs and names of parents to ImportedCrosses object
		for (ImportedCrosses cross : this.importedCrossesList.getImportedCrosses()) {
			final Triple<String, Integer, List<Integer>> crossInfo = entryIdToCrossInfoMap.get(cross.getEntryId());
			if (femalePlotMap.containsKey(crossInfo.getMiddle())) {
				final ImportedCrossParent femaleCrossParent = femalePlotMap.get(crossInfo.getMiddle());
				final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(femaleCrossParent.getGid(), femaleCrossParent.getDesignation(), crossInfo.getMiddle(),
					femaleStudyName);
				cross.setFemaleParent(femaleParent);
			} else {
				throw new FileParsingException(this.messageSource.getMessage("no.list.data.for.plot",
					new Object[] {femaleStudyName, crossInfo.getMiddle()}, LocaleContextHolder.getLocale()));
			}


			final String crossMaleStudy = crossInfo.getLeft();
			final Map<Integer, ImportedCrossParent> malePlotMap = maleNurseriesPlotMap.get(crossMaleStudy);
			final List<Integer> crossMalePlotNos = crossInfo.getRight();
			final List<ImportedGermplasmParent> maleParents = new ArrayList<>();
			for(Integer crossMalePlotNo: crossMalePlotNos) {
				if (malePlotMap != null && malePlotMap.containsKey(crossMalePlotNo)) {
					final ImportedCrossParent maleCrossParent = malePlotMap.get(crossMalePlotNo);
					final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(maleCrossParent.getGid(),
						maleCrossParent.getDesignation(), crossMalePlotNo, crossMaleStudy);
					maleParents.add(maleParent);
				} else if(crossMalePlotNo == 0 && crossMalePlotNos.size() == 1) {
					final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(crossMalePlotNo,
						Name.UNKNOWN, crossMalePlotNo, crossMaleStudy);
					maleParents.add(maleParent);
				} else {
					throw new FileParsingException(this.messageSource.getMessage("no.list.data.for.plot",
						new Object[] {crossMaleStudy, crossMalePlotNo}, LocaleContextHolder.getLocale()));
				}
			}
			cross.setMaleParents(maleParents);

			final Germplasm germplasm = new Germplasm();
			germplasm.setGnpgs(2);
			germplasm.setGid(Integer.MAX_VALUE);
			germplasm.setGpid1(Integer.valueOf(cross.getFemaleGid()));
			germplasm.setGpid2(cross.getMaleParents().get(0).getGid());
			final String crossString = this.crossingService.getCross(germplasm, cross, "/");
			cross.setCross(crossString);
		}
	}

	/**
	 * Returns map of plot numbers to corresponding ImportedCrossParent record from specified plot numbers
	 * of given study
	 *
	 * @param studyName
	 * @param plotNos
	 * @param programUUID
	 * @return
	 * @throws FileParsingException
	 */
	Map<Integer, ImportedCrossParent> getPlotToImportedCrossParentMapForStudy(final String studyName, final Set<Integer> plotNos,
		final String programUUID) throws FileParsingException {
		// 1. retrieve study ID of parent study
		final Integer studyId = this.studyDataManager.getStudyIdByNameAndProgramUUID(studyName, programUUID);
		if (null == studyId) {
			throw new FileParsingException(this.messageSource.getMessage("no.such.study.exists", new String[] {studyName},
				LocaleContextHolder.getLocale()));
		}

		Map<Integer, ImportedCrossParent> plotToListDataProjectMap = this.fieldbookMiddlewareService.getPlotNoToImportedCrossParentMap(studyId, plotNos);

		return plotToListDataProjectMap;
	}



	private void validateFemaleStudy(final String femaleStudy) throws FileParsingException {

		if (femaleStudy == null || Objects.equals(femaleStudy, "")) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.female.study.empty", new Object[] {},
					LocaleContextHolder.getLocale()));
		}

		if (!femaleStudy.equals(this.studySelection.getWorkbook().getStudyName().trim())) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.female.study.match", new Object[] {},
					LocaleContextHolder.getLocale()));
		}
	}

	void validateObservationRow(final String femalePlotNo, final String malePlotNo, final int currentRow,
			final String strCrossingDate) throws FileParsingException {

		if (!(StringUtils.isNotBlank(femalePlotNo) && StringUtils.isNumeric(femalePlotNo))) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.row.femalePlot",
					new Integer[] {currentRow}, LocaleContextHolder.getLocale()));
		}

		if (StringUtils.isBlank(malePlotNo)) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.row.malePlot",
					new Integer[] {currentRow}, LocaleContextHolder.getLocale()));
		}

		if (!StringUtils.isBlank(strCrossingDate) && !DateUtil.isValidDate(strCrossingDate)) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.row.crossing.date",
					new Integer[] {currentRow}, LocaleContextHolder.getLocale()));
		}
	}

	List<Integer> convertCommaSeparatedStringToList(final String commaSeparatedValuesString, final int currentRow)  throws FileParsingException {

		final List<Integer> list = new ArrayList<>();
		final String[] values = commaSeparatedValuesString.split(",");
		try {
			for (final String value : values) {

				final Integer convertedValue = Integer.parseInt(value.trim());

				if (values.length > 1 && convertedValue <= 0) {
					// Do not allow to import UNKNOWN germplasm if there are multiple male plot numbers are specified.
					throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.row.malePlot.must.be.greater.than.zero",
						null, LocaleContextHolder.getLocale()));
				} else {
					list.add(convertedValue);
				}

			}
		} catch (final NumberFormatException e) {
			throw new FileParsingException(this.messageSource.getMessage("error.import.crosses.observation.row.malePlot",
				new Integer[] {currentRow}, LocaleContextHolder.getLocale()));
		}
		return list;
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
	private void validateObservationsHeader() throws FileParsingException {
		final Set<String> importedFactors = new HashSet<>();

		for (final ImportedFactor factor : this.importedCrossesList.getImportedFactors()) {
			importedFactors.add(factor.getFactor());
		}

		final Set<String> importedVariates = new HashSet<>();

		for (final ImportedVariate variate : this.importedCrossesList.getImportedVariates()) {
			importedVariates.add(variate.getVariate());
		}

		final int headerSize = this.getLastCellNum(this.observationSheetIndex, 0);

		final Set<String> invalidColumns = new HashSet<>();

		for (int i = 0; i < headerSize; i++) {
			// search the current header
			final String obsHeader = this.getCellStringValue(this.observationSheetIndex, 0, i);

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

		final Set<String> mandatoryColumns = new HashSet<>();
		final Set<String> missingColumns = new HashSet<>();

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

	public void setImportedCrossesList(final ImportedCrossesList importedCrossesList) {
		this.importedCrossesList = importedCrossesList;
	}
}
