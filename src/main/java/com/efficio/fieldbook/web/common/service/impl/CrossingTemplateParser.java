package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.util.parsing.AbstractExcelFileParser;
import com.efficio.fieldbook.web.nursery.bean.*;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
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

	/**
	 * The Constant FILE_INVALID.
	 */
	public static final String FILE_INVALID = "common.error.invalid.file";
	public static final String NO_REFERENCES_ERROR_DESC = "study.import.crosses.error.no.references";
	public static final String TEMPLATE_LIST_TYPE = "LST";
	public static final int DESCRIPTION_SHEET_COL_SIZE = 8;
	protected static final int DESCRIPTION_SHEET_NO = 0;
	protected static final int OBSERVATION_SHEET_NO = 1;
	protected static final int CONDITION_ROW_NO = 5;

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

	public CrossingTemplateParser() {

	}

	@Override public ImportedCrossesList parseWorkbook(Workbook workbook) throws
			FileParsingException{
		this.workbook = workbook;

		try {
			parseDescriptionSheet();
			parseObservationSheet();
		} catch (ParseException e) {
			LOG.debug(e.getMessage(), e);
			throw new FileParsingException(messageSource.getMessage(FILE_INVALID, new Object[]{}, Locale.getDefault()));
		} catch (MiddlewareQueryException e) {
			LOG.debug(e.getMessage(), e);
			throw new FileParsingException(messageSource.getMessage(NO_REFERENCES_ERROR_DESC, new Object[]{}, Locale.getDefault()));
		}

		return importedCrossesList;
	}

	protected void parseDescriptionSheet() throws FileParsingException, ParseException {
		parseCrossingListDetails();
		parseConditions();
		parseFactors();
		parseConstants();
		parseVariate();
	}

	/**
	 * FIXME: For now, the headers are referenced to a static (APP_CONSTANTS) lookup
	 *
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	protected void parseObservationSheet()
			throws FileParsingException, MiddlewareQueryException {
		if (isObservationsHeaderInvalid()) {
			throw new FileParsingException("Invalid Observation headers");
		}

		currentRow = 1;
		while (importFileIsValid && !isRowEmpty(OBSERVATION_SHEET_NO, currentRow,
				sizeOfObservationHeader())) {

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
					.getCrossingListProjectData(femaleNursery, Integer.valueOf(femaleEntry));
			ListDataProject maleListData = this
					.getCrossingListProjectData(maleNursery, Integer.valueOf(maleEntry));

			this.importedCrossesList.addImportedCrosses(
					new ImportedCrosses(femaleListData, maleListData, femaleNursery, maleNursery,
							breedingMethod,
							crossingDate, seedsHarvested, notes, currentRow));

			currentRow++;
		}
	}

	protected int sizeOfObservationHeader() {
		return importedCrossesList.getImportedFactors().size() + importedCrossesList
				.getImportedVariates().size();
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

	protected void parseCrossingListDetails() throws FileParsingException, ParseException {
		String listName = getCellStringValue(DESCRIPTION_SHEET_NO, 0, 1);
		String listTitle = getCellStringValue(DESCRIPTION_SHEET_NO, 1, 1);

		String labelId = getCellStringValue(DESCRIPTION_SHEET_NO, 2, 0);

		int listDateColNo = AppConstants.LIST_DATE.getString().equalsIgnoreCase(labelId) ? 2 : 3;
		int listTypeColNo = AppConstants.LIST_TYPE.getString().equalsIgnoreCase(labelId) ? 2 : 3;

		Date listDate = DateUtil.parseDate(
				getCellStringValue(DESCRIPTION_SHEET_NO, listDateColNo, 1));
		String listType = getCellStringValue(DESCRIPTION_SHEET_NO, listTypeColNo, 1);

		this.importedCrossesList = new ImportedCrossesList(originalFilename, listName,
				listTitle, listType, listDate);

		if (!TEMPLATE_LIST_TYPE.equalsIgnoreCase(listType)) {
			throw new FileParsingException("Error parsing details : List type is invalid");
		}
	}

	protected void parseConditions() {
		// condition headers start at row = 5 (+ 1 : count starts from 0 )
		currentRow = CONDITION_ROW_NO;

		if (!isConditionHeadersInvalid(CONDITION_ROW_NO) && importFileIsValid) {
			currentRow++;

			while (!isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
				this.importedCrossesList.addImportedCondition(
						new ImportedCondition(
								getCellStringValue(DESCRIPTION_SHEET_NO,
										currentRow, 0)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 1)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 2)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 3)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 4)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 5)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 6),
								""
						)
				);

				currentRow++;
			}
		}

		while (isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
			currentRow++;
		}
	}

	protected void parseFactors() throws FileParsingException{

		if (!isFactorHeadersInvalid(currentRow) && importFileIsValid) {
			currentRow++;

			while (!isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
				final ImportedFactor factor = new ImportedFactor(
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								0)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								1)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								2)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								3)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								4)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								5)
						, "");

				importedCrossesList.addImportedFactor(factor);

				currentRow++;
			}

			currentRow++;

		} else {
			throw new FileParsingException("Error parsing on factors header: Incorrect headers for factors.");
		}

		while (isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
			currentRow++;
		}
	}

	protected void parseConstants() throws FileParsingException{
		if (!isConstantsHeaderInvalid(currentRow) && importFileIsValid) {
			currentRow++;
			while (!isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
				importedCrossesList.addImportedConstant(new ImportedConstant(
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								0)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								1)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								2)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								3)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								4)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								5)
						,
						getCellStringValue(DESCRIPTION_SHEET_NO, currentRow,
								6)));

				currentRow++;
			}
			currentRow++;

		} else {
			// Incorrect headers for factors.
			throw new FileParsingException("Error parsing on constants header: Incorrect headers for constants.");
		}

		while (isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
			currentRow++;
		}
	}

	protected void parseVariate() throws FileParsingException{
		if (!isVariateHeaderInvalid(currentRow) && importFileIsValid) {
			currentRow++;
			while (!isRowEmpty(DESCRIPTION_SHEET_NO, currentRow, DESCRIPTION_SHEET_COL_SIZE)) {
				importedCrossesList.addImportedVariate(
						new ImportedVariate(
								getCellStringValue(DESCRIPTION_SHEET_NO,
										currentRow, 0)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 1)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 2)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 3)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 4)
								, getCellStringValue(DESCRIPTION_SHEET_NO,
								currentRow, 5)));
				currentRow++;
			}

		} else {
			throw new FileParsingException("Error parsing on variates header: Incorrect headers for variates.");
		}
	}

	protected boolean isConditionHeadersInvalid(int conditionHeaderRowNo) {
		String[] headers = {
				AppConstants.CONDITION.getString(),
				AppConstants.DESCRIPTION.getString(),
				AppConstants.PROPERTY.getString(),
				AppConstants.SCALE.getString(),
				AppConstants.METHOD.getString(),
				AppConstants.DATA_TYPE.getString(),
				AppConstants.VALUE.getString()
		};

		return isHeaderInvalid(conditionHeaderRowNo, DESCRIPTION_SHEET_NO, headers);
	}



	protected boolean isFactorHeadersInvalid(int factorHeaderRowNo) {
		String[] headers = {
				AppConstants.FACTOR.getString(),
				AppConstants.DESCRIPTION.getString(),
				AppConstants.PROPERTY.getString(),
				AppConstants.SCALE.getString(),
				AppConstants.METHOD.getString(),
				AppConstants.DATA_TYPE.getString()
		};

		return isHeaderInvalid(factorHeaderRowNo,DESCRIPTION_SHEET_NO, headers);
	}

	protected boolean isConstantsHeaderInvalid(int constantHeaderRowNo) {
		String[] headers = {
				AppConstants.CONSTANT.getString(),
				AppConstants.DESCRIPTION.getString(),
				AppConstants.PROPERTY.getString(),
				AppConstants.SCALE.getString(),
				AppConstants.METHOD.getString(),
				AppConstants.DATA_TYPE.getString(),
				AppConstants.VALUE.getString()
		};

		return isHeaderInvalid(constantHeaderRowNo, DESCRIPTION_SHEET_NO,headers);
	}

	protected boolean isVariateHeaderInvalid(int variateHeaderRowNo) {
		String headers[] = {
			AppConstants.VARIATE.getString(),
				AppConstants.DESCRIPTION.getString(),
				AppConstants.PROPERTY.getString(),
				AppConstants.SCALE.getString(),
				AppConstants.METHOD.getString(),
				AppConstants.DATA_TYPE.getString()
		};

		return isHeaderInvalid(variateHeaderRowNo, DESCRIPTION_SHEET_NO, headers);
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

		final int headerSize = sizeOfObservationHeader();

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
	protected ListDataProject getCrossingListProjectData(String studyName, Integer genderEntryNo)
			throws MiddlewareQueryException {
		// 1 get the particular study's list
		final Integer studyId = studyDataManager.getStudyIdByName(studyName);

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
