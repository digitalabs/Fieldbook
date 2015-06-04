
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

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
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.ListDataProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.util.AppConstants;

/**
 * Created by cyrus on 1/22/15. This parses a Crossing Template Excel file Note that this class is stateful, declare in spring app context
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
	private final boolean importFileIsValid = true;
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
	@Resource
	private CrossingService crossingService;

	private DescriptionSheetParser<ImportedCrossesList> descriptionSheetParser;

	public CrossingTemplateParser() {

	}

	@Override
	public ImportedCrossesList parseWorkbook(Workbook workbook, Map<String, Object> additionalParams) throws FileParsingException {
		this.workbook = workbook;
		try {
			this.descriptionSheetParser = new DescriptionSheetParser<>(new ImportedCrossesList());

			this.importedCrossesList = this.descriptionSheetParser.parseWorkbook(this.workbook, additionalParams);

			this.parseObservationSheet(this.contextUtil.getCurrentProgramUUID());
		} catch (MiddlewareQueryException e) {
			CrossingTemplateParser.LOG.debug(e.getMessage(), e);
			throw new FileParsingException(this.messageSource.getMessage(CrossingTemplateParser.NO_REFERENCES_ERROR_DESC, new Object[] {},
					Locale.getDefault()));
		}

		return this.importedCrossesList;
	}

	/**
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	protected void parseObservationSheet(String programUUID) throws FileParsingException, MiddlewareQueryException {
		if (this.isObservationsHeaderInvalid()) {
			throw new FileParsingException("Invalid Observation headers");
		}

		this.currentRow = 1;
		while (this.importFileIsValid
				&& !this.isRowEmpty(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
						this.importedCrossesList.sizeOfObservationHeader())) {

			String femaleNursery =
					this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
							this.observationColumnMap.get(AppConstants.FEMALE_NURSERY.getString()));
			String femalePlotNo =
					this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
							this.observationColumnMap.get(AppConstants.FEMALE_PLOT.getString()));
			String maleNursery =
					this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
							this.observationColumnMap.get(AppConstants.MALE_NURSERY.getString()));
			String malePlotNo =
					this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
							this.observationColumnMap.get(AppConstants.MALE_PLOT.getString()));
			String breedingMethod =
					this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
							this.observationColumnMap.get(AppConstants.BREEDING_METHOD.getString()));
			String crossingDate =
					this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
							this.observationColumnMap.get(AppConstants.CROSSING_DATE.getString()));
			String seedsHarvested =
					this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
							this.observationColumnMap.get(AppConstants.SEEDS_HARVESTED.getString()));
			String notes =
					this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, this.currentRow,
							this.observationColumnMap.get(AppConstants.NOTES.getString()));

			if (!this.isObservationRowValid(femaleNursery, femalePlotNo, maleNursery, malePlotNo, crossingDate, seedsHarvested)) {

				throw new FileParsingException("Invalid Observation on row: " + this.currentRow);
			}

			// process female + male parent entries, will throw middleware query exception if no study valid or null
			ListDataProject femaleListData = this.getCrossingListProjectData(femaleNursery, Integer.valueOf(femalePlotNo), programUUID);
			ListDataProject maleListData = this.getCrossingListProjectData(maleNursery, Integer.valueOf(malePlotNo), programUUID);

			ImportedCrosses importedCrosses =
					new ImportedCrosses(femaleListData, maleListData, femaleNursery, maleNursery, femalePlotNo, malePlotNo, this.currentRow);
			importedCrosses.setOptionalFields(breedingMethod, crossingDate, seedsHarvested, notes);
			// this would set the correct cross string depending if the use is cimmyt wheat
			Germplasm germplasm = new Germplasm();
			germplasm.setGnpgs(2);
			germplasm.setGid(Integer.MAX_VALUE);
			germplasm.setGpid1(femaleListData.getGermplasmId());
			germplasm.setGpid2(maleListData.getGermplasmId());
			String crossString = this.crossingService.getCross(germplasm, importedCrosses, "/");
			importedCrosses.setCross(crossString);

			this.importedCrossesList.addImportedCrosses(importedCrosses);

			this.currentRow++;
		}
	}

	protected boolean isObservationRowValid(String femaleNursery, String femalePlot, String maleNursery, String malePlot,
			String crossingDate, String seedsHarvested) {
		return StringUtils.isNotBlank(femaleNursery) && StringUtils.isNotBlank(femalePlot) && StringUtils.isNotBlank(maleNursery)
				&& StringUtils.isNotBlank(malePlot) && StringUtils.isNumeric(femalePlot) && StringUtils.isNumeric(malePlot)
				&& (!StringUtils.isNotBlank(seedsHarvested) || StringUtils.isNumeric(seedsHarvested))
				&& (!StringUtils.isNotBlank(crossingDate) || DateUtil.isValidDate(crossingDate));
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

		importedFactors.addAll(this.importedCrossesList.getImportedFactors());

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

		importedVariates.addAll(this.importedCrossesList.getImportedVariates());

		final int headerSize = this.importedCrossesList.sizeOfObservationHeader();

		for (int i = 0; i < headerSize; i++) {
			// search the current header
			String obsHeader = this.getCellStringValue(CrossingTemplateParser.OBSERVATION_SHEET_NO, 0, i);

			boolean inFactors = importedFactors.contains(obsHeader);
			boolean inVariates = importedVariates.contains(obsHeader);

			if (!inFactors && !inVariates) {
				return true;
			} else {
				this.observationColumnMap.put(obsHeader, i);
			}
		}

		return false;
	}

	/**
	 * Returns the ListProjectData given a female or male plot no using the current plot position on the template.
	 *
	 * @param studyName - femaleNursery/maleNursery equivalent from the template
	 * @param genderedPlotNo - femalePlot/malePlot equivalent from the template
	 * @return ListDataProject - We need the Desig, and female/male gids information that we can retrive using this data structure
	 * @throws org.generationcp.middleware.exceptions.MiddlewareQueryException
	 */
	protected ListDataProject getCrossingListProjectData(String studyName, Integer genderedPlotNo, String programUUID)
					throws MiddlewareQueryException, FileParsingException {
		// 1 get the particular study's list
		final Integer studyId = this.studyDataManager.getStudyIdByNameAndProgramUUID(studyName, programUUID);

		if (null == studyId) {
			throw new FileParsingException(this.messageSource.getMessage("no.such.study.exists", new String[] {studyName},
					LocaleContextHolder.getLocale()));
		}

		final StudyType studyType = this.studyDataManager.getStudyType(studyId);

		// 2. retrieve the list id of the particular study
		ListDataProject listdataResult =
				this.fieldbookMiddlewareService.getListDataProjectByStudy(studyId,
						CrossingTemplateParser.STUDY_TYPE_TO_LIST_TYPE_MAP.get(studyType), genderedPlotNo);

		if (null == listdataResult) {
			throw new FileParsingException(this.messageSource.getMessage("no.list.data.for.plot", new Object[] {studyName, genderedPlotNo},
					LocaleContextHolder.getLocale()));
		}

		return listdataResult;
	}
}
