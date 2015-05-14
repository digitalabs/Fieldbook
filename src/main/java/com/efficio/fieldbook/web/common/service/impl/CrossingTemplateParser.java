package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.util.AppConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.parsing.AbstractExcelFileParser;
import org.generationcp.commons.parsing.DescriptionSheetParser;
import org.generationcp.commons.parsing.FileParsingException;
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
import org.generationcp.middleware.pojos.ListDataProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Resource;
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
	public ImportedCrossesList parseWorkbook(Workbook workbook, Map<String,Object> additionalParams) throws
			FileParsingException {
		this.workbook = workbook;
		try {
			descriptionSheetParser = new DescriptionSheetParser<>(new ImportedCrossesList());

			this.importedCrossesList = descriptionSheetParser.parseWorkbook(this.workbook,additionalParams);

			parseObservationSheet(contextUtil.getCurrentProgramUUID());
		} catch (MiddlewareQueryException e) {
			LOG.debug(e.getMessage(), e);
			throw new FileParsingException(messageSource.getMessage(NO_REFERENCES_ERROR_DESC, new Object[]{}, Locale.getDefault()));
		}

		return importedCrossesList;
	}

	/**
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
			String femalePlotNo = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.FEMALE_PLOT.getString()));
			String maleNursery = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.MALE_NURSERY.getString()));
			String malePlotNo = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.MALE_PLOT.getString()));
			String breedingMethod = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.BREEDING_METHOD.getString()));
			String crossingDate = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.CROSSING_DATE.getString()));
			String seedsHarvested = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.SEEDS_HARVESTED.getString()));
			String notes = getCellStringValue(OBSERVATION_SHEET_NO, currentRow,
					observationColumnMap.get(AppConstants.NOTES.getString()));

			if (!isObservationRowValid(femaleNursery, femalePlotNo, maleNursery, malePlotNo,
					crossingDate,
					seedsHarvested)) {

				throw new FileParsingException("Invalid Observation on row: " + currentRow);
			}

			// process female + male parent entries, will throw middleware query exception if no study valid or null
			ListDataProject femaleListData = this
					.getCrossingListProjectData(femaleNursery, Integer.valueOf(femalePlotNo),programUUID);
			ListDataProject maleListData = this
					.getCrossingListProjectData(maleNursery, Integer.valueOf(malePlotNo),programUUID);

			ImportedCrosses importedCrosses = new ImportedCrosses(femaleListData, maleListData, 
					femaleNursery, maleNursery,femalePlotNo,malePlotNo,currentRow);

			importedCrosses.setOptionalFields(breedingMethod, crossingDate,seedsHarvested, notes);

			this.importedCrossesList.addImportedCrosses(importedCrosses);

			currentRow++;
		}
	}

	protected boolean isObservationRowValid(String femaleNursery, String femalePlot,
			String maleNursery, String malePlot, String crossingDate, String seedsHarvested) {
		return StringUtils.isNotBlank(femaleNursery) && StringUtils.isNotBlank(femalePlot)
				&& StringUtils.isNotBlank(maleNursery) && StringUtils
				.isNotBlank(malePlot) && StringUtils.isNumeric(femalePlot)
				&& StringUtils.isNumeric(malePlot) && (
				(!StringUtils.isNotBlank(seedsHarvested)) || StringUtils
						.isNumeric(seedsHarvested)) && (
				(!StringUtils.isNotBlank(crossingDate)) || DateUtil
						.isValidDate(crossingDate));
	}

	protected boolean isObservationsHeaderInvalid() {
		final List<ImportedFactor> importedFactors = new ArrayList<ImportedFactor>() {
			@Override
			public boolean contains(Object o) {
				boolean returnVal = false;
				for (ImportedFactor i : this) {
					if (i.getFactor().equalsIgnoreCase(o.toString())) {
						returnVal = true;
					}
				}
				return returnVal;
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
	 * Returns the ListProjectData given a female or male plot no using the current plot position on the template.
	 *
	 * @param studyName     - femaleNursery/maleNursery equivalent from the template
	 * @param genderedPlotNo - femalePlot/malePlot equivalent from the template
	 * @return ListDataProject - We need the Desig, and female/male gids information that we can retrive using this data structure
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	protected ListDataProject getCrossingListProjectData(
			String studyName, Integer genderedPlotNo, String programUUID)
			throws MiddlewareQueryException, FileParsingException {
		// 1 get the particular study's list
		final Integer studyId = studyDataManager.getStudyIdByNameAndProgramUUID(studyName,programUUID);

		if (null == studyId) {
			throw new FileParsingException(messageSource.getMessage("no.such.study.exists",new String[]{studyName},
					LocaleContextHolder.getLocale()));
		}

		final StudyType studyType = studyDataManager.getStudyType(studyId);

		// 2. retrieve the list id of the particular study
		ListDataProject listdataResult = fieldbookMiddlewareService.getListDataProjectByStudy(studyId,
				STUDY_TYPE_TO_LIST_TYPE_MAP.get(studyType),
				genderedPlotNo);

		if (null == listdataResult) {
			throw new FileParsingException(messageSource.getMessage("no.list.data.for.plot",new Object[]{studyName,genderedPlotNo},LocaleContextHolder.getLocale()));
		}

		return listdataResult;
	}
}
