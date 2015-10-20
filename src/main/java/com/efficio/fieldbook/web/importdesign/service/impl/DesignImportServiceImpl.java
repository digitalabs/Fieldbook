
package com.efficio.fieldbook.web.importdesign.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.importdesign.generator.DesignImportMeasurementRowGenerator;
import com.efficio.fieldbook.web.importdesign.service.DesignImportService;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.ExpDesignUtil;

public class DesignImportServiceImpl implements DesignImportService {

	private static final Logger LOG = LoggerFactory.getLogger(DesignImportServiceImpl.class);

	@Resource
	private UserSelection userSelection;

	@Resource
	private FieldbookService fieldbookService;

	@Resource
	private OntologyService ontologyService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private MessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public List<MeasurementRow> generateDesign(final Workbook workbook, final DesignImportData designImportData,
			final EnvironmentData environmentData, final boolean isPreview) throws DesignValidationException {

		final Set<String> generatedTrialInstancesFromUI = this.extractTrialInstancesFromEnvironmentData(environmentData);

		/**
		 * this will add the trial environment factors and their values to ManagementDetailValues so we can pass them to the UI and reflect
		 * the values in the Environments Tab
		 **/
		this.populateEnvironmentDataWithValuesFromCsvFile(environmentData, workbook, designImportData);

		final List<ImportedGermplasm> importedGermplasm =
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

		final Map<Integer, List<String>> csvData = designImportData.getCsvData();
		final Map<Integer, StandardVariable> germplasmStandardVariables =
				this.convertToStandardVariables(workbook.getGermplasmFactors(), PhenotypicType.GERMPLASM);

		final List<MeasurementRow> measurements = new ArrayList<>();
		final Map<PhenotypicType, Map<Integer, DesignHeaderItem>> mappedHeadersWithStdVarId =
				designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId();

		// row counter starts at index = 1 because zero index is the header
		int rowCounter = 1;

		final Map<String, Integer> availableCheckTypes = this.retrieveAvailableCheckTypes();
		final DesignImportMeasurementRowGenerator measurementRowGenerator =
				new DesignImportMeasurementRowGenerator(workbook, mappedHeadersWithStdVarId, importedGermplasm, germplasmStandardVariables,
						generatedTrialInstancesFromUI, isPreview, availableCheckTypes);

		while (rowCounter <= csvData.size() - 1) {
			final MeasurementRow measurementRow =
					measurementRowGenerator.createMeasurementRow(csvData.get(rowCounter), this.fieldbookService);
			if (measurementRow != null) {
				measurements.add(measurementRow);
			}
			rowCounter++;
		}

		// add factor data to the list of measurement row
		measurementRowGenerator.addFactorsToMeasurementRows(measurements);

		// add trait data to the list of measurement row
		measurementRowGenerator.addVariatesToMeasurementRows(measurements, this.userSelection, this.fieldbookService, this.ontologyService,
				this.contextUtil);

		return measurements;
	}

	/**
	 * Returns all available check types at the moment in the form of a map <Name, CVTermId> i.e <C,10170>
	 * 
	 * @return map <Name, CVTermId>
	 */
	private Map<String, Integer> retrieveAvailableCheckTypes() {
		final Map<String, Integer> checkTypeMap = new HashMap<String, Integer>();
		final List<Enumeration> checkTypes = this.fieldbookService.getCheckTypeList();

		for (final Enumeration checkType : checkTypes) {
			checkTypeMap.put(checkType.getName(), checkType.getId());
		}

		return checkTypeMap;
	}

	@Override
	public Set<MeasurementVariable> getDesignMeasurementVariables(final Workbook workbook, final DesignImportData designImportData,
			final boolean isPreview) {

		final Set<MeasurementVariable> measurementVariables = new LinkedHashSet<>();
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		// Add the trial environments first
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT, mappedHeaders));

		// remove the trial environment factors if NOT in PREVIEW mode except for TRIAL INSTANCE
		if (!isPreview) {
			final Iterator<MeasurementVariable> iterator = measurementVariables.iterator();
			while (iterator.hasNext()) {
				final MeasurementVariable temp = iterator.next();
				if (temp.getTermId() != TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					iterator.remove();
				}
			}
		}

		// Add the germplasm factors that exist from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.GERMPLASM, mappedHeaders));

		// Add the germplasm factors from the selected germplasm in workbook
		measurementVariables.addAll(workbook.getGermplasmFactors());

		// Add the design factors that exists from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.TRIAL_DESIGN, mappedHeaders));

		// Add the variates that exist from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.VARIATE, mappedHeaders));

		// Add the variates from the added traits in workbook
		measurementVariables.addAll(workbook.getVariates());

		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {

			measurementVariables.addAll(workbook.getFactors());

			// remove the trial instance factor if the Study is Nursery because it only has 1 trial instance by default
			final Iterator<MeasurementVariable> iterator = measurementVariables.iterator();
			while (iterator.hasNext()) {
				final MeasurementVariable temp = iterator.next();
				if (temp.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					iterator.remove();
					break;
				}
			}
		}

		return measurementVariables;
	}

	@Override
	public Set<StandardVariable> getDesignRequiredStandardVariables(final Workbook workbook, final DesignImportData designImportData) {

		final Set<StandardVariable> standardVariables = new LinkedHashSet<>();
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		for (final DesignHeaderItem designHeaderItem : mappedHeaders.get(PhenotypicType.TRIAL_DESIGN)) {
			standardVariables.add(designHeaderItem.getVariable());
		}

		return standardVariables;
	}

	@Override
	public Set<MeasurementVariable> getDesignRequiredMeasurementVariable(final Workbook workbook, final DesignImportData designImportData) {

		final Set<MeasurementVariable> measurementVariables = new LinkedHashSet<>();
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.TRIAL_DESIGN, mappedHeaders));

		return measurementVariables;

	}

	@Override
	public Set<MeasurementVariable> getMeasurementVariablesFromDataFile(final Workbook workbook, final DesignImportData designImportData) {

		final Set<MeasurementVariable> measurementVariables = new LinkedHashSet<>();
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		// Add the trial environments first
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.TRIAL_ENVIRONMENT, mappedHeaders));

		// Add the germplasm factors that exist from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.GERMPLASM, mappedHeaders));

		// Add the design factors that exists from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.TRIAL_DESIGN, mappedHeaders));

		// Add the variates that exist from csv file header
		measurementVariables.addAll(this.extractMeasurementVariable(PhenotypicType.VARIATE, mappedHeaders));

		return measurementVariables;
	}

	@Override
	public boolean areTrialInstancesMatchTheSelectedEnvironments(final Integer noOfEnvironments, final DesignImportData designImportData)
			throws DesignValidationException {

		final DesignHeaderItem trialInstanceDesignHeaderItem =
				this.validateIfStandardVariableExists(
						designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
						"design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);

		if (trialInstanceDesignHeaderItem != null) {
			final Map<String, Map<Integer, List<String>>> csvMap =
					this.groupCsvRowsIntoTrialInstance(trialInstanceDesignHeaderItem, designImportData.getCsvData());
			if (noOfEnvironments == csvMap.size()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Map<PhenotypicType, List<DesignHeaderItem>> categorizeHeadersByPhenotype(final List<DesignHeaderItem> designHeaders) {
		final List<String> headers = new ArrayList<>();
		// get headers as string list
		for (final DesignHeaderItem item : designHeaders) {
			headers.add(item.getName());
		}

		// create groups for the Design Headers
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedDesignHeaders = new HashMap<>();

		// note: the UNASSIGNED key here is for unmapped headers
		mappedDesignHeaders.put(PhenotypicType.UNASSIGNED, new ArrayList<DesignHeaderItem>());
		mappedDesignHeaders.put(PhenotypicType.TRIAL_ENVIRONMENT, new ArrayList<DesignHeaderItem>());
		mappedDesignHeaders.put(PhenotypicType.TRIAL_DESIGN, new ArrayList<DesignHeaderItem>());
		mappedDesignHeaders.put(PhenotypicType.GERMPLASM, new ArrayList<DesignHeaderItem>());
		mappedDesignHeaders.put(PhenotypicType.VARIATE, new ArrayList<DesignHeaderItem>());

		final Map<String, List<StandardVariable>> variables =
				this.ontologyDataManager.getStandardVariablesInProjects(headers, this.contextUtil.getCurrentProgramUUID());

		// ok, so these variables dont have Phenotypic information, we need to
		// assign it via proj-prop or cvtermid
		final Set<PhenotypicType> designImportRoles =
				new HashSet<>(Arrays.asList(new PhenotypicType[] {PhenotypicType.TRIAL_ENVIRONMENT, PhenotypicType.TRIAL_DESIGN,
						PhenotypicType.GERMPLASM, PhenotypicType.VARIATE}));
		for (final Entry<String, List<StandardVariable>> entryVar : variables.entrySet()) {
			for (final StandardVariable sv : entryVar.getValue()) {
				for (final VariableType variableType : sv.getVariableTypes()) {
					if (designImportRoles.contains(variableType.getRole())) {
						sv.setPhenotypicType(variableType.getRole());
						break;
					}
				}
			}
		}

		for (final DesignHeaderItem item : designHeaders) {
			final List<StandardVariable> match = variables.get(item.getName().toUpperCase());

			if (null != match && !match.isEmpty() && null != mappedDesignHeaders.get(match.get(0).getPhenotypicType())) {
				final StandardVariable standardVariable = match.get(0);
				item.setVariable(standardVariable);

				// let set required if ff condition is true
				if (TermId.PLOT_NO.getId() == standardVariable.getId() || TermId.ENTRY_NO.getId() == standardVariable.getId()
						|| TermId.TRIAL_INSTANCE_FACTOR.getId() == standardVariable.getId()) {
					item.setRequired(true);
				}

				mappedDesignHeaders.get(standardVariable.getPhenotypicType()).add(item);
			} else {
				mappedDesignHeaders.get(PhenotypicType.UNASSIGNED).add(item);
			}

		}

		return mappedDesignHeaders;
	}

	@Override
	public Map<String, Map<Integer, List<String>>> groupCsvRowsIntoTrialInstance(final DesignHeaderItem trialInstanceHeaderItem,
			final Map<Integer, List<String>> csvMap) {

		final Map<String, Map<Integer, List<String>>> csvMapGrouped = new HashMap<>();

		final Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		// skip the header row
		iterator.next();
		while (iterator.hasNext()) {
			final Entry<Integer, List<String>> entry = iterator.next();
			final String trialInstance = entry.getValue().get(trialInstanceHeaderItem.getColumnIndex());
			if (!csvMapGrouped.containsKey(trialInstance)) {
				csvMapGrouped.put(trialInstance, new HashMap<Integer, List<String>>());
			}
			csvMapGrouped.get(trialInstance).put(entry.getKey(), entry.getValue());
		}
		return csvMapGrouped;

	}

	@Override
	public DesignHeaderItem validateIfStandardVariableExists(final Map<Integer, DesignHeaderItem> map, final String messageCodeId,
			final TermId termId) throws DesignValidationException {

		final DesignHeaderItem headerItem = map.get(termId.getId());
		if (headerItem == null) {
			throw new DesignValidationException(this.messageSource.getMessage(messageCodeId, null, Locale.ENGLISH));
		} else {
			return headerItem;
		}
	}

	@Override
	public Set<MeasurementVariable> extractMeasurementVariable(final PhenotypicType phenotypicType,
			final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders) {

		final Set<MeasurementVariable> measurementVariables = new HashSet<>();

		for (final DesignHeaderItem designHeaderItem : mappedHeaders.get(phenotypicType)) {
			final MeasurementVariable measurementVariable = this.createMeasurementVariable(designHeaderItem.getVariable());
			measurementVariables.add(measurementVariable);
		}

		return measurementVariables;
	}

	protected MeasurementVariable createMeasurementVariable(final StandardVariable standardVariable) {
		return ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, this.fieldbookService);
	}

	protected Map<Integer, StandardVariable> convertToStandardVariables(final List<MeasurementVariable> list,
			final PhenotypicType phenotypicType) {

		final Map<Integer, StandardVariable> map = new HashMap<>();

		for (final MeasurementVariable measurementVariable : list) {
			final StandardVariable stdVar =
					this.ontologyService.getStandardVariable(measurementVariable.getTermId(), this.contextUtil.getCurrentProgramUUID());
			stdVar.setPhenotypicType(phenotypicType);
			map.put(measurementVariable.getTermId(), stdVar);
		}

		return map;
	}

	protected Set<String> extractTrialInstancesFromEnvironmentData(final EnvironmentData environmentData) {
		final Set<String> generatedTrialInstancesFromUI = new HashSet<>();
		for (final Environment env : environmentData.getEnvironments()) {
			generatedTrialInstancesFromUI.add(env.getManagementDetailValues().get(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId())));
		}
		return generatedTrialInstancesFromUI;
	}

	protected void populateEnvironmentDataWithValuesFromCsvFile(final EnvironmentData environmentData, final Workbook workbook,
			final DesignImportData designImportData) throws DesignValidationException {

		final List<DesignHeaderItem> trialEnvironmentsDesignHeaderItems =
				designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT);
		final DesignHeaderItem trialInstanceHeaderItem =
				this.validateIfStandardVariableExists(
						designImportData.getMappedHeadersWithDesignHeaderItemsMappedToStdVarId().get(PhenotypicType.TRIAL_ENVIRONMENT),
						"design.import.error.trial.is.required", TermId.TRIAL_INSTANCE_FACTOR);
		final Map<String, Map<Integer, List<String>>> groupedCsvRows =
				this.groupCsvRowsIntoTrialInstance(trialInstanceHeaderItem, designImportData.getCsvData());

		final Iterator<Environment> iteratorEnvironment = environmentData.getEnvironments().iterator();
		while (iteratorEnvironment.hasNext()) {
			final Environment environment = iteratorEnvironment.next();
			final String trialInstanceNo =
					environment.getManagementDetailValues().get(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId()));
			final Map<Integer, List<String>> csvData = groupedCsvRows.get(trialInstanceNo);
			if (csvData != null) {
				for (final DesignHeaderItem item : trialEnvironmentsDesignHeaderItems) {
					final String value = this.getTheFirstValueFromCsv(item, csvData);
					environment.getManagementDetailValues().put(String.valueOf(item.getId()), value);
				}
			} else {
				iteratorEnvironment.remove();
			}

		}

	}

	protected String getTheFirstValueFromCsv(final DesignHeaderItem item, final Map<Integer, List<String>> map) {
		return map.entrySet().iterator().next().getValue().get(item.getColumnIndex());
	}

}
