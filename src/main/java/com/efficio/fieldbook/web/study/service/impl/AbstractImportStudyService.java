
package com.efficio.fieldbook.web.study.service.impl;

import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.trial.service.ValidationService;
import com.efficio.fieldbook.web.study.service.ImportStudyService;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
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
			this.copyConditionsAndConstants(this.workbook);
			this.parseAndLoadObservationData();
			this.validateObservationColumns();
			this.validateImportMetadata();

			final Set<ChangeType> modes = new HashSet<>();

			final List<String> addedTraits = new ArrayList<String>();
			final List<String> removedTraits = new ArrayList<String>();
			
			this.detectAddedTraitsAndPerformRename(modes,addedTraits,removedTraits);

			final List<GermplasmChangeDetail> changeDetailsList = new ArrayList<>();

			this.performWorkbookMetadataUpdate();
			final Map<String, MeasurementRow> measurementRowsMap =
				this.createMeasurementRowsMap(this.workbook.getObservations());
			this.performStudyDataImport(modes, this.parsedData, measurementRowsMap, changeDetailsList, this.workbook);
			SettingsUtil.resetBreedingMethodValueToId(
				this.fieldbookMiddlewareService, this.workbook.getObservations(), true, this.ontologyService,
				this.workbook.getStudyDetails().getProgramUUID());

			try {
				this.validationService.validateObservationValues(this.workbook);
			} catch (final MiddlewareQueryException e) {
				AbstractImportStudyService.LOG.error(e.getMessage(), e);
				WorkbookUtil.resetWorkbookObservations(this.workbook);
				return new ImportResult(e.getMessage());
			}

			String conditionsAndConstantsErrorMessage = "";

			try {
				this.validationService.validateConditionAndConstantValues(this.workbook);
			} catch (final MiddlewareQueryException e) {
				conditionsAndConstantsErrorMessage = e.getMessage();
				WorkbookUtil.revertImportedConditionAndConstantsData(this.workbook);
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
	protected abstract void detectAddedTraitsAndPerformRename(final Set<ChangeType> modes, final List<String> addedVariates, final List<String> removedVariates) throws IOException, WorkbookParserException;

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
	 * @param changeDetailsList
	 * @param workbook
	 */
	protected abstract void performStudyDataImport(final Set<ChangeType> modes, final T parsedData,
			final Map<String, MeasurementRow> rowsMap,
			final List<GermplasmChangeDetail> changeDetailsList, final Workbook workbook) throws WorkbookParserException;

	protected void setNewDesignation(final MeasurementRow measurementRow, final String newDesig) {
		final String originalDesig = measurementRow.getMeasurementDataValue(TermId.DESIG.getId());
		final String originalGid = measurementRow.getMeasurementDataValue(TermId.GID.getId());

		if (originalDesig != null && !originalDesig.equalsIgnoreCase(newDesig)) {
			final List<Integer> newGids = this.getGermplasmIdsByName(newDesig);
			if (originalGid != null && newGids.contains(Integer.valueOf(originalGid))) {
				final MeasurementData wData = measurementRow.getMeasurementData(TermId.DESIG.getId());
				wData.setValue(newDesig);
			}
		}
	}

	protected List<Integer> getGermplasmIdsByName(final String newDesig) {
		return this.fieldbookMiddlewareService.getGermplasmIdsByName(newDesig);
	}

	public Map<String, MeasurementRow> createMeasurementRowsMap(final List<MeasurementRow> observations) {
		final Map<String, MeasurementRow> map = new HashMap<>();
		if (observations != null && !observations.isEmpty()) {
			for (final MeasurementRow row : observations) {
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

    void setOntologyService(final OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    void setValidationService(final ValidationService validationService) {
        this.validationService = validationService;
    }

    void setParsedData(final T parsedData) {
        this.parsedData = parsedData;
    }
}
