package com.efficio.fieldbook.web.nursery.service.impl;

import com.efficio.fieldbook.service.api.FileService;
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
import org.generationcp.middleware.util.PoiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Created by cyrus on 1/22/15.
 * This parses a Crossing Template Excel file
 * Note that this class is stateful, declare in spring app context as prototyped scope
 */
public class CrossingTemplateParser {

	/**
	 * The Constant FILE_INVALID.
	 */
	public final static String FILE_INVALID = "common.error.invalid.file";

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

	private final Map<String, Integer> observationColumnMap = new HashMap<>();
	private ImportedCrossesList importedCrossesList;
	private boolean importFileIsValid;
	private Workbook workbook;
	private int currentRow;
	private String originalFilename;
	/**
	 * Resources
	 */

	@Resource
	private StudyDataManager studyDataManager;
	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Resource
	private FileService fileService;

	public CrossingTemplateParser() {

	}

	public ImportedCrossesList parseFile(MultipartFile multipartFile) {
		try {
			// initial values
			importFileIsValid = true;
			currentRow = 0;

			this.workbook = storeImportGermplasmWorkbook(multipartFile);

			parseDescriptionSheet();

			parseObservationSheet();

		} catch (IOException | ParseException | MiddlewareQueryException e) {
			addParseErrorMsg(e.getMessage());
		}

		return importedCrossesList;

	}

	protected Workbook storeImportGermplasmWorkbook(MultipartFile multipartFile)
			throws IOException {
		String serverFilename = fileService.saveTemporaryFile(multipartFile.getInputStream());
		this.originalFilename = multipartFile.getOriginalFilename();

		return fileService.retrieveWorkbook(serverFilename);
	}

	public void parseDescriptionSheet() throws ParseException {
		parseCrossingListDetails();
		parseConditions();
		parseFactors();
		parseConstants();
		parseVariate();

	}

	/**
	 * FIXME: For now, the headers are referenced to a static (APP_CONSTANTS) lookup
	 *
	 * @throws ParseException
	 */
	public void parseObservationSheet()
			throws ParseException, MiddlewareQueryException {
		if (isObservationsHeaderInvalid()) {
			addParseErrorMsg(FILE_INVALID);
			LOG.debug("Invalid Observation headers");

			return;
		}

		currentRow = 1;
		final int headerSize = importedCrossesList.getImportedFactors().size() + importedCrossesList
				.getImportedVariates().size();
		while (importFileIsValid &&
				!PoiUtil.rowIsEmpty(this.workbook.getSheetAt(OBSERVATION_SHEET_NO), currentRow, 0,
						headerSize - 1)
				) {

			String femaleNursery = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, currentRow,
							observationColumnMap.get(AppConstants.FEMALE_NURSERY.getString()));
			String femaleEntry = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, currentRow,
							observationColumnMap.get(AppConstants.FEMALE_ENTRY.getString()));
			String maleNursery = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, currentRow,
							observationColumnMap.get(AppConstants.MALE_NURSERY.getString()));
			String maleEntry = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, currentRow,
							observationColumnMap.get(AppConstants.MALE_ENTRY.getString()));
			String breedingMethod = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, currentRow,
							observationColumnMap.get(AppConstants.BREEDING_METHOD.getString()));
			String crossingDate = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, currentRow,
							observationColumnMap.get(AppConstants.CROSSING_DATE.getString()));
			String seedsHarvested = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, currentRow,
							observationColumnMap.get(AppConstants.SEEDS_HARVESTED.getString()));
			String notes = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, currentRow,
							observationColumnMap.get(AppConstants.NOTES.getString()));

			if (!isObservationRowValid(femaleNursery, femaleEntry, maleNursery, maleEntry,
					crossingDate,
					seedsHarvested)) {
				addParseErrorMsg(FILE_INVALID);
				LOG.debug("Invalid Observation on row: " + currentRow);

				return;
			}

			// proceess female + male parent entries, will throw middleware query exception if no study valid or null
			ListDataProject femaleListData = this
					.getCrossingListProjectData(femaleNursery, Integer.valueOf(femaleEntry));
			ListDataProject maleListData = this
					.getCrossingListProjectData(maleNursery, Integer.valueOf(maleEntry));

			this.importedCrossesList.addImportedCrosses(
					new ImportedCrosses(femaleListData, maleListData, femaleNursery, maleNursery, breedingMethod,
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

	public void parseCrossingListDetails() throws ParseException {
		String listName = PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, 0, 1);
		String listTitle = PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, 1, 1);

		String labelId = PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, 2, 0);

		int listDateColNo = AppConstants.LIST_DATE.getString().equalsIgnoreCase(labelId) ? 2 : 3;
		int listTypeColNo = AppConstants.LIST_TYPE.getString().equalsIgnoreCase(labelId) ? 2 : 3;

		Date listDate = DateUtil.parseDate(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, listDateColNo, 1));
		String listType = PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, listTypeColNo, 1);

		this.importedCrossesList = new ImportedCrossesList(this.originalFilename, listName,
				listTitle, listType, listDate);
	}

	public void parseConditions() {
		// condition headers start at row = 5 (+ 1 : count starts from 0 )
		currentRow = CONDITION_ROW_NO;

		if (!isConditionHeadersInvalid(CONDITION_ROW_NO) && importFileIsValid) {
			currentRow++;

			while (!PoiUtil
					.rowIsEmpty(workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0, 7)) {
				this.importedCrossesList.addImportedCondition(
						new ImportedCondition(
								PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
										currentRow, 0)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 1)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 2)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 3)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 4)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 5)
								, PoiUtil.getCellStringValue(workbook, DESCRIPTION_SHEET_NO,
								currentRow, 6),
								""
						)
				);

				currentRow++;
			}
		}

		while (PoiUtil.rowIsEmpty(workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0, 7)) {
			currentRow++;
		}
	}

	public void parseFactors() {

		if (!isFactorHeadersInvalid(currentRow) && importFileIsValid) {
			currentRow++;

			while ((!PoiUtil
					.rowIsEmpty(this.workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0,
							7))) {
				final ImportedFactor factor = new ImportedFactor(
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								0)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								1)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								2)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								3)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								4)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								5)
						, "");

				importedCrossesList.addImportedFactor(factor);

				currentRow++;
			}

			currentRow++;

		} else {
			// Incorrect headers for factors.
			addParseErrorMsg(FILE_INVALID);
			LOG.debug("Error parsing on factors header: Incorrect headers for factors.");
		}

		while (PoiUtil.rowIsEmpty(workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0, 7)) {
			currentRow++;
		}
	}

	public void parseConstants() {
		if (!isConstantsHeaderInvalid(currentRow) && importFileIsValid) {
			currentRow++;
			while ((!PoiUtil
					.rowIsEmpty(this.workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0,
							7))) {
				importedCrossesList.addImportedConstant(new ImportedConstant(
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								0)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								1)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								2)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								3)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								4)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								5)
						,
						PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
								6)));

				currentRow++;
			}
			currentRow++;

		} else {
			// Incorrect headers for factors.
			addParseErrorMsg(FILE_INVALID);
			LOG.debug("Error parsing on constants header: Incorrect headers for constants.");
		}

		while (PoiUtil.rowIsEmpty(workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0, 7)) {
			currentRow++;
		}
	}

	public void parseVariate() {
		if (!isVariateHeaderInvalid(currentRow) && importFileIsValid) {
			currentRow++;
			while (!PoiUtil
					.rowIsEmpty(this.workbook.getSheetAt(DESCRIPTION_SHEET_NO), currentRow, 0,
							7)) {
				importedCrossesList.addImportedVariate(
						new ImportedVariate(
								PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
										currentRow, 0)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 1)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 2)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 3)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 4)
								, PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								currentRow, 5)));
				currentRow++;
			}

		} else {
			addParseErrorMsg(FILE_INVALID);
			LOG.debug("Error parsing on variates header: Incorrect headers for variates.");
		}
	}

	private boolean isConditionHeadersInvalid(int conditionHeaderRowNo) {
		return !AppConstants.CONDITION.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 0))
				|| !AppConstants.DESCRIPTION.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 1))
				|| !AppConstants.PROPERTY.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 2))
				|| !AppConstants.SCALE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 3))
				|| !AppConstants.METHOD.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 4))
				|| !AppConstants.DATA_TYPE.getString()
				.equalsIgnoreCase(PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 5))
				|| !AppConstants.VALUE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						conditionHeaderRowNo, 6));
	}

	private boolean isFactorHeadersInvalid(int factorHeaderRowNo) {
		return !AppConstants.FACTOR.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 0))
				|| !AppConstants.DESCRIPTION.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 1))
				|| !AppConstants.PROPERTY.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 2))
				|| !AppConstants.SCALE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 3))
				|| !AppConstants.METHOD.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 4))
				|| !AppConstants.DATA_TYPE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						factorHeaderRowNo, 5));
	}

	private boolean isConstantsHeaderInvalid(int constantHeaderRowNo) {
		return !
				AppConstants.CONSTANT.getString().equalsIgnoreCase(PoiUtil
						.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
								constantHeaderRowNo, 0))
				|| !AppConstants.DESCRIPTION.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 1))
				|| !AppConstants.PROPERTY.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 2))
				|| !AppConstants.SCALE.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 3))
				|| !AppConstants.METHOD.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 4))
				|| !AppConstants.DATA_TYPE.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 5))
				|| !AppConstants.VALUE.getString().equalsIgnoreCase(PoiUtil
				.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO,
						constantHeaderRowNo, 6));
	}

	private boolean isVariateHeaderInvalid(int variateHeaderRowNo) {
		return !AppConstants.VARIATE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						0))
				|| !AppConstants.DESCRIPTION.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						1))
				|| !AppConstants.PROPERTY.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						2))
				|| !AppConstants.SCALE.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						3))
				|| !AppConstants.METHOD.getString().equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						4))
				|| !AppConstants.DATA_TYPE.getString().replace("_", " ").equalsIgnoreCase(
				PoiUtil.getCellStringValue(this.workbook, DESCRIPTION_SHEET_NO, currentRow,
						5));
	}

	private void addParseErrorMsg(String message) {
		if (importFileIsValid) {
			importedCrossesList.addErrorMessages(message);
			importFileIsValid = false;
		}
	}

	protected boolean isObservationsHeaderInvalid() {
		final ArrayList<ImportedFactor> importedFactors = new ArrayList<ImportedFactor>() {
			@Override
			public boolean contains(Object o) {
				for (ImportedFactor i : this) {
					if (i.getFactor().equalsIgnoreCase(o.toString()))
						return true;
				}
				return false;
			}
		};

		importedFactors.addAll(importedCrossesList.getImportedFactors());

		final ArrayList<ImportedVariate> importedVariates = new ArrayList<ImportedVariate>() {
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

		final int headerSize = importedCrossesList.getImportedFactors().size() + importedCrossesList
				.getImportedVariates().size();

		for (int i = 0; i < headerSize; i++) {
			// search the current header
			String obsHeader = PoiUtil
					.getCellStringValue(this.workbook, OBSERVATION_SHEET_NO, 0, i);

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
	 * @throws MiddlewareQueryException
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

		if (null == germplasmList && germplasmList.isEmpty()) {
			throw new MiddlewareQueryException("study.has.no.list",
					"Study with \"" + studyName + "\" has no list.");
		}

		return fieldbookMiddlewareService.getListDataProjectByListIdAndEntryNo(
				germplasmList.get(0).getId(), genderEntryNo);
	}
}
