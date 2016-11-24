
package com.efficio.fieldbook.web.study.service.impl;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @param <T>
 */
public abstract class AbstractImportStudyService<T> implements ImportStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractImportStudyService.class);

	@Resource
	protected ValidationService validationService;

	@Resource
	protected FieldbookService fieldbookMiddlewareService;

	@Resource
	protected OntologyService ontologyService;

	protected Workbook workbook;
	protected String currentFile;
	protected String originalFileName;

    protected T parsedData;

	public AbstractImportStudyService(final Workbook workbook, final String currentFile, final String originalFileName) {
		this.workbook = workbook;
		this.currentFile = currentFile;
		this.originalFileName = originalFileName;
	}

    /**
     * Abstracts the details for importing a study into the system, highlighting the expected flow common to all import operation
     * @return
     * @throws WorkbookParserException
     */
	@Override
	public ImportResult importWorkbook() throws WorkbookParserException {

		try {
			// prior to continuing import, we create a copy of the current conditions and constants of the workbook, to simplify reset
			// operation later on
			this.copyConditionsAndConstants(workbook);
            parseAndLoadObservationData();
			this.validateObservationColumns();
			this.validateImportMetadata();

			final Set<ChangeType> modes = new HashSet<>();
			//this.detectAddedTraitsAndPerformRename(modes);

			final List<String> addedTraits = new ArrayList();
			final List<String> removedTraits = new ArrayList();
			
			this.detectAddedTraitsAndPerformRename(modes,addedTraits,removedTraits);
			
			final String trialInstanceNo = this.retrieveTrialInstanceNumber();
			final List<GermplasmChangeDetail> changeDetailsList = new ArrayList<>();

			this.performWorkbookMetadataUpdate();
			final Map<String, MeasurementRow> rowsMap =
					this.createMeasurementRowsMap(workbook.getObservations(), trialInstanceNo, workbook.isNursery());
			this.performStudyDataImport(modes, parsedData, rowsMap, trialInstanceNo, changeDetailsList, workbook);

			SettingsUtil.resetBreedingMethodValueToId(fieldbookMiddlewareService, workbook.getObservations(), true, ontologyService, 
					workbook.getStudyDetails().getProgramUUID());

			try {
				this.validationService.validateObservationValues(workbook, trialInstanceNo);
			} catch (final MiddlewareQueryException e) {
				AbstractImportStudyService.LOG.error(e.getMessage(), e);
				WorkbookUtil.resetWorkbookObservations(workbook);
				return new ImportResult(e.getMessage());
			}

			String conditionsAndConstantsErrorMessage = "";

			try {
				this.validationService.validateConditionAndConstantValues(workbook, trialInstanceNo);
			} catch (final MiddlewareQueryException e) {
				conditionsAndConstantsErrorMessage = e.getMessage();
				WorkbookUtil.revertImportedConditionAndConstantsData(workbook);
				AbstractImportStudyService.LOG.error(e.getMessage(), e);
			}

			final ImportResult res = new ImportResult(modes, changeDetailsList);
			res.setConditionsAndConstantsErrorMessage(conditionsAndConstantsErrorMessage);
			res.setVariablesAdded(addedTraits);
			res.setVariablesRemoved(removedTraits);
			return res;
		} catch (final Exception e) {
			throw new WorkbookParserException(e.getMessage(), e);
		}
	}

    protected void parseAndLoadObservationData() throws IOException {
        this.parsedData = this.parseObservationData();
    }

	abstract void validateObservationColumns() throws WorkbookParserException;

	/**
	 * This method is used to validate the internal structure of the import file. It shouldn't validate the actual measurement content of
	 * the file, rather the focus of this method is to validate any descriptive elements that are within the file e.g., if description sheet
	 * is present, validate correctness of description sheet content;
	 */
	abstract void validateImportMetadata() throws WorkbookParserException;

	/**
	 * This method abstracts the implementation for cases where the import file contains variable definition information, aside from just
	 * study data
	 */
	protected void performWorkbookMetadataUpdate() throws WorkbookParserException {
		// this method left intentionally blank as most import formats do not have variable definition information. to be implemented via
		// overriding by concerned sub classes
	}

	/**
     *
     */
	protected abstract void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes) throws IOException, WorkbookParserException;

	/**
    *
    */
	protected abstract void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes, final List<String> addedVariates, final List<String> removedVariates);

	
	/**
	 * Provides an implementation of retrieving the trial instance number by calling another method. Can be overridden in cases where study
	 * import service needs a different logic with a different set of working parameters for calculating the trial instance number
	 *
	 * @return
	 * @throws WorkbookParserException
	 */
	protected String retrieveTrialInstanceNumber() throws WorkbookParserException {
		return this.getTrialInstanceNo(workbook, originalFileName);
	}

	/**
	 * The following method abstracts the implementation and even the return type of the result of parsing the observation data contained
	 * within the target file This is to accommodate differences in parsing output regarding CSV based and Excel based file formats
	 *
	 * @return
	 */
	protected abstract T parseObservationData() throws IOException;

	/**
	 * The following method abstracts the actual implementation of how parsed study data is imported back into the current workbook.
	 * 
	 * @param modes
	 * @param parsedData
	 * @param rowsMap
	 * @param trialInstanceNumber
	 * @param changeDetailsList
	 * @param workbook
	 */
	protected abstract void performStudyDataImport(final Set<ChangeType> modes, final T parsedData,
			final Map<String, MeasurementRow> rowsMap, final String trialInstanceNumber,
			final List<GermplasmChangeDetail> changeDetailsList, final Workbook workbook) throws WorkbookParserException;

	public String getTrialInstanceNo(final Workbook workbook, final String filename) throws WorkbookParserException {
		final String trialInstanceNumber = workbook != null && workbook.isNursery() ? "1" : this.getTrialInstanceNoFromFileName(filename);
		if (StringUtils.isEmpty(trialInstanceNumber)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}
		return trialInstanceNumber;
	}

	public String getTrialInstanceNoFromFileName(final String filename) throws WorkbookParserException {
		String trialInstanceNumber = "";

		final String pattern = "(.+)[-](\\d+)";
		final Pattern r = Pattern.compile(pattern);
		final Matcher m = r.matcher(filename);

		if (m.find()) {
			trialInstanceNumber = m.group(m.groupCount());
		}

		if (!NumberUtils.isNumber(trialInstanceNumber)) {
			throw new WorkbookParserException("error.workbook.import.missing.trial.instance");
		}

		return trialInstanceNumber;
	}

	public Map<String, MeasurementRow> createMeasurementRowsMap(final List<MeasurementRow> observations, final String instanceNumber,
			final boolean isNursery) {
		final Map<String, MeasurementRow> map = new HashMap<>();
		final List<MeasurementRow> newObservations;
		if (!isNursery) {

			newObservations = WorkbookUtil.filterObservationsByTrialInstance(observations, instanceNumber);

		} else {
			newObservations = observations;
		}

		if (newObservations != null && !newObservations.isEmpty()) {
			for (final MeasurementRow row : newObservations) {
				map.put(row.getKeyIdentifier(), row);
			}
		}
		return map;
	}

	protected void copyConditionsAndConstants(final Workbook workbook) {

		if (workbook != null) {
			final List<MeasurementVariable> newVarList = new ArrayList<>();
			if (workbook.getConditions() != null) {
				final List<MeasurementVariable> conditionsCopy = new ArrayList<>();
				for (final MeasurementVariable var : workbook.getConditions()) {
					conditionsCopy.add(var.copy());
				}
				workbook.setImportConditionsCopy(conditionsCopy);
				newVarList.addAll(conditionsCopy);
			}
			if (workbook.getConstants() != null) {
				final List<MeasurementVariable> constantsCopy = new ArrayList<>();
				for (final MeasurementVariable var : workbook.getConstants()) {
					constantsCopy.add(var.copy());
				}
				workbook.setImportConstantsCopy(constantsCopy);
				newVarList.addAll(constantsCopy);
			}
			if (workbook.getTrialObservations() != null) {
				final List<MeasurementRow> trialObservationsCopy = new ArrayList<>();
				for (final MeasurementRow row : workbook.getTrialObservations()) {
					trialObservationsCopy.add(row.copy(newVarList));
				}
				workbook.setImportTrialObservationsCopy(trialObservationsCopy);
			}
		}
	}

    List<String> getMeasurementHeaders(final Workbook workbook) {
        final List<String> headers = new ArrayList<>();

        final List<MeasurementVariable> measurementDatasetVariablesView = workbook.getMeasurementDatasetVariablesView();
        for (final MeasurementVariable mvar : measurementDatasetVariablesView) {
            headers.add(mvar.getName());
        }
        return headers;
    }

    void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
        this.fieldbookMiddlewareService = fieldbookMiddlewareService;
    }

    void setOntologyService(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    void setParsedData(T parsedData) {
        this.parsedData = parsedData;
    }
}
