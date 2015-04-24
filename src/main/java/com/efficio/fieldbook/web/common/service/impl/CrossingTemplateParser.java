package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.util.parsing.DescriptionSheetParser;
import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.util.parsing.AbstractExcelFileParser;
import com.efficio.fieldbook.web.nursery.bean.*;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

/**
 * Created by cyrus on 1/22/15.
 * This parses a Crossing Template Excel file
 * Note that this class is stateful, declare in spring app context as prototyped scope
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
		STUDY_TYPE_TO_LIST_TYPE_MAP.put(StudyType.N, GermplasmListType.NURSERY);
		STUDY_TYPE_TO_LIST_TYPE_MAP.put(StudyType.T, GermplasmListType.TRIAL);
	}

	private Map<String, Integer> observationColumnMap = new HashMap<>();
	private ImportedCrossesList importedCrossesList;
	private boolean importFileIsValid = true;
	private int currentRow = 0;

	/**
	 * Resources
	 */
	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private ContextUtil contextUtil;

	private DescriptionSheetParser<ImportedCrossesList> descriptionSheetParser;

	public CrossingTemplateParser() {

	}

	@Override
	public ImportedCrossesList parseWorkbook(Workbook workbook) throws
			FileParsingException {
		this.workbook = workbook;
		this.importedCrossesList = new ImportedCrossesList();

		try {
			descriptionSheetParser = new DescriptionSheetParser<>(importedCrossesList,workbook);

			descriptionSheetParser.parseDescriptionSheet();

			parseObservationSheet(contextUtil.getCurrentProgramUUID());
		} catch (ParseException e) {
			LOG.debug(e.getMessage(), e);
			throw new FileParsingException(messageSource.getMessage(FILE_INVALID, new Object[]{}, Locale.getDefault()));
		} catch (MiddlewareQueryException e) {
			LOG.debug(e.getMessage(), e);
			throw new FileParsingException(messageSource.getMessage(NO_REFERENCES_ERROR_DESC, new Object[]{}, Locale.getDefault()));
		}

		return importedCrossesList;
	}

	/**
	 * FIXME: For now, the headers are referenced to a static (APP_CONSTANTS) lookup
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	protected void parseObservationSheet(String programUUID)
			throws FileParsingException, MiddlewareQueryException {
		if (isObservationsHeaderInvalid()) {
			throw new FileParsingException("Invalid Observation headers");
		}

		currentRow = 1;
		while (importFileIsValid && !isRowEmpty(OBSERVATION_SHEET_NO, currentRow,
				importedCrossesList.sizeOfObservationHeader())) {

			String femaleNursery = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.FEMALE_NURSERY.getString()));
			String femaleEntry = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.FEMALE_ENTRY.getString()));
			String maleNursery = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.MALE_NURSERY.getString()));
			String maleEntry = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.MALE_ENTRY.getString()));
			String breedingMethod = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.BREEDING_METHOD.getString()));
			String crossingDate = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.CROSSING_DATE.getString()));
			String seedsHarvested = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.SEEDS_HARVESTED.getString()));
			String notes = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.NOTES.getString()));

			if (!isObservationRowValid(femaleNursery, femaleEntry, maleNursery, maleEntry,
					crossingDate,
					seedsHarvested)) {

				throw new FileParsingException("Invalid Observation on row: " + currentRow);
			}

			// proceess female + male parent entries, will throw middleware query exception if no study valid or null
			ListDataProject femaleListData = this
					.getCrossingListProjectData(femaleNursery, Integer.valueOf(femaleEntry),programUUID);
			ListDataProject maleListData = this
					.getCrossingListProjectData(maleNursery, Integer.valueOf(maleEntry),programUUID);

			this.importedCrossesList.addImportedCrosses(
					new ImportedCrosses(femaleListData, maleListData, femaleNursery, maleNursery,
							breedingMethod,
							crossingDate, seedsHarvested, notes, currentRow));

			currentRow++;
		}
	}

	protected boolean isObservationRowValid(String femaleNursery, String femaleEntry,
			String maleNursery, String maleEntry, String crossingDate, String seedsHarvested) {
		return StringUtils.isNotBlank(femaleNursery) && StringUtils.isNotBlank(femaleEntry)
				&& StringUtils.isNotBlank(maleNursery) && StringUtils
				.isNotBlank(maleEntry) && StringUtils.isNumeric(femaleEntry)
				&& StringUtils.isNumeric(maleEntry) && (
				(!StringUtils.isNotBlank(seedsHarvested)) || StringUtils
						.isNumeric(seedsHarvested)) && (
				(!StringUtils.isNotBlank(crossingDate)) || DateUtil
						.isValidDate(crossingDate));
	}

	protected boolean isObservationsHeaderInvalid() {
		final List<ImportedFactor> importedFactors = new ArrayList<ImportedFactor>() {
			@Override
			public boolean contains(Object o) {
				for (ImportedFactor i : this) {
					if (i.getFactor().equalsIgnoreCase(o.toString())) {
						return true;
					}
				}
				return false;
			}
		};

		importedFactors.addAll(importedCrossesList.getImportedFactors());

		final List<ImportedVariate> importedVariates = new ArrayList<ImportedVariate>() {
			@Override
			public boolean contains(Object o) {
				for (ImportedVariate i : this) {
					if (i.getVariate().equalsIgnoreCase(o.toString())) {
						return true;
					}
				}
				return false;
			}
		};

		importedVariates.addAll(importedCrossesList.getImportedVariates());

		final int headerSize = importedCrossesList.sizeOfObservationHeader();

		for (int i = 0; i < headerSize; i++) {
			// search the current header
			String obsHeader = getCellStringValue(OBSERVATION_SHEET_NO, 0, i);

			boolean inFactors = importedFactors.contains(obsHeader);
			boolean inVariates = importedVariates.contains(obsHeader);

			if (!inFactors && !inVariates) {
				return true;
			} else {
				observationColumnMap.put(obsHeader, i);
			}
		}

		return false;
	}

	/**
	 * Returns the ListProjectData given a female or male entries using the current entry position on the template.
	 *
	 * @param studyName     - femaleNursery/maleNursery equivalent from the template
	 * @param genderEntryNo - femaleEntry/maleEntry equivalent from the template
	 * @return ListDataProject - We need the Desig, and female/male gids information that we can retrive using this data structure
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	protected ListDataProject getCrossingListProjectData(
			String studyName, Integer genderEntryNo, String programUUID)
			throws MiddlewareQueryException {
		// 1 get the particular study's list
		final Integer studyId = studyDataManager.getStudyIdByNameAndProgramUUID(studyName,programUUID);

		if (null == studyId) {
			throw new MiddlewareQueryException("no.such.study.exists",
					"No study with \"" + studyName + "\" exists.");
		}

		final StudyType studyType = studyDataManager.getStudyType(studyId);

		// 2. retrieve the list id of the particular study
		List<GermplasmList> germplasmList = fieldbookMiddlewareService
				.getGermplasmListsByProjectId(studyId, STUDY_TYPE_TO_LIST_TYPE_MAP.get(studyType));

		if (null == germplasmList || germplasmList.isEmpty()) {
			throw new MiddlewareQueryException("study.has.no.list",
					"Study with \"" + studyName + "\" has no list.");
		}

		return fieldbookMiddlewareService.getListDataProjectByListIdAndEntryNo(
				germplasmList.get(0).getId(), genderEntryNo);
	}
}
