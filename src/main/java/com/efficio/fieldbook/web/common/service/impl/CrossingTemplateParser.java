
package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.util.AppConstants;

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
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Resource;
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

	public static final String NO_REFERENCES_ERROR_DESC = "study.import.crosses.error.no.references";
	protected static final String OBSERVATION_SHEET_NAME = "Observation";

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
	private UserDataManager userDataManager;
	
	public CrossingTemplateParser() {

	}

	@Override
	public ImportedCrossesList parseWorkbook(final Workbook workbook, final Map<String, Object> additionalParams)
			throws FileParsingException {
		this.workbook = workbook;
		this.observationSheetIndex = this.getSheetIndex(CrossingTemplateParser.OBSERVATION_SHEET_NAME);
		try {

			final CrossesListDescriptionSheetParser<ImportedCrossesList> crossesListDescriptionSheetParser =
					new CrossesListDescriptionSheetParser<>(new ImportedCrossesList(), this.userDataManager);

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

		while (!this.isRowEmpty(this.observationSheetIndex, currentRow, headerSize)) {

			final String femalePlotNo = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.FEMALE_PLOT.getString()));
				  String maleStudy = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.MALE_STUDY.getString()));
			final String malePlotNo = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.MALE_PLOT.getString()));
			final String breedingMethod = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.BREEDING_METHOD.getString()));
			final String strCrossingDate = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.CROSSING_DATE.getString()));
			final String notes = this.getCellStringValue(this.observationSheetIndex, currentRow,
				this.observationColumnMap.get(AppConstants.NOTES.getString()));

			this.validateObservationRow(femalePlotNo, malePlotNo, currentRow, strCrossingDate);

			Integer crossingDate = null;
			if (!StringUtils.isBlank(strCrossingDate)) {
				crossingDate = Integer.valueOf(strCrossingDate);
			}

			if (StringUtils.isBlank(maleStudy)) {
				maleStudy = femaleStudy;
			}

			// process female + male parent entries, will throw middleware query exception if no study valid or null
			final ListDataProject femaleListData = this.getCrossingListProjectData(femaleStudy, Integer.valueOf(femalePlotNo), programUUID, false);
			ListDataProject maleListData = this.getCrossingListProjectData(maleStudy, Integer.valueOf(malePlotNo), programUUID, true);

			final ImportedCrosses importedCrosses =
					new ImportedCrosses(femaleListData, maleListData, femaleStudy, maleStudy, femalePlotNo, malePlotNo, currentRow);
			// Show source as "Pending" in initial dialogue.
			// Source (Plot Code) string is generated later in the process and will be displayed in the final list generated.
			importedCrosses.setSource(ImportedCrosses.SEED_SOURCE_PENDING);
			importedCrosses.setOptionalFields(breedingMethod, crossingDate, notes);
			// this would set the correct cross string depending if the use is cimmyt wheat
			final Germplasm germplasm = new Germplasm();
			germplasm.setGnpgs(2);
			germplasm.setGid(Integer.MAX_VALUE);
			germplasm.setGpid1(femaleListData.getGermplasmId());
			germplasm.setGpid2(maleListData.getGermplasmId());
			final String crossString = this.crossingService.getCross(germplasm, importedCrosses, "/");
			importedCrosses.setCross(crossString);

			this.importedCrossesList.addImportedCrosses(importedCrosses);

			currentRow++;
		}
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

	private void validateObservationRow(final String femalePlotNo, final String malePlotNo, final int currentRow,
			final String strCrossingDate) throws FileParsingException {

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

	/**
	 * Returns the ListProjectData given a female or male plot no using the current plot position on the template.
	 *
	 * @param studyName - femaleStudy/maleStudy equivalent from the template
	 * @param genderedPlotNo - femalePlot/malePlot equivalent from the template
	 * @param programUUID - unique program ID
	 * @param isMaleParent - flag whether the current germplasm to be looked up was used as a male parent
	 * @return ListDataProject - We need the Design, and female/male gids information that we can retrieve using this data structure
	 * @throws FileParsingException
	 */
	ListDataProject getCrossingListProjectData(final String studyName, final Integer genderedPlotNo, final String programUUID, final Boolean isMaleParent)
			throws FileParsingException {
		// Only allow male parents to be unknown (GID = 0)
		if (genderedPlotNo == 0 && isMaleParent) {
			final ListDataProject maleListData = new ListDataProject();
			maleListData.setDesignation(Name.UNKNOWN);
			maleListData.setGermplasmId(genderedPlotNo);
			return maleListData;
		}

		final String instanceNumber = "1";

		// 1 get the particular study's list
		final Integer studyId = this.studyDataManager.getStudyIdByNameAndProgramUUID(studyName, programUUID);

		if (null == studyId) {
			throw new FileParsingException(
					this.messageSource.getMessage("no.such.study.exists", new String[] {studyName}, LocaleContextHolder.getLocale()));
		}

		// 2. retrieve the list id of the particular study
		final ListDataProject listdataResult =
			this.fieldbookMiddlewareService.getListDataProjectByStudy(studyId, GermplasmListType.STUDY, genderedPlotNo, instanceNumber);

		if (null == listdataResult) {
			throw new FileParsingException(this.messageSource.getMessage("no.list.data.for.plot", new Object[] {studyName, genderedPlotNo},
					LocaleContextHolder.getLocale()));
		}

		return listdataResult;
	}
}
