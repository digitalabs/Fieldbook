
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.efficio.fieldbook.web.trial.bean.Environment;
import com.efficio.fieldbook.web.trial.bean.EnvironmentData;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.mysql.jdbc.StringUtils;

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
	private OntologyScaleDataManager ontologyScaleDataManager;

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

		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		// row counter starts at index = 1 because zero index is the header
		int rowCounter = 1;

		while (rowCounter <= csvData.size() - 1) {
			final MeasurementRow measurementRow =
					this.createMeasurementRow(workbook, mappedHeaders, csvData.get(rowCounter), importedGermplasm,
							germplasmStandardVariables, generatedTrialInstancesFromUI, isPreview);
			if (measurementRow != null) {
				measurements.add(measurementRow);
			}
			rowCounter++;

		}

		// add factor data to the list of measurement row
		this.addFactorsToMeasurementRows(workbook, measurements);

		// add trait data to the list of measurement row
		this.addVariatesToMeasurementRows(workbook, measurements);

		return measurements;
	}

	@Override
	public void validateDesignData(final DesignImportData designImportData) throws DesignValidationException {

		final Map<Integer, List<String>> csvData = designImportData.getCsvData();
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		final DesignHeaderItem trialInstanceDesignHeaderItem =
				this.validateIfTrialFactorExists(mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem entryNoDesignHeaderItem = this.validateIfEntryNumberExists(mappedHeaders.get(PhenotypicType.GERMPLASM));
		this.validateIfPlotNumberExists(mappedHeaders.get(PhenotypicType.TRIAL_DESIGN));

		final Map<String, Map<Integer, List<String>>> csvMap = this.groupCsvRowsIntoTrialInstance(trialInstanceDesignHeaderItem, csvData);

		this.validateEntryNoMustBeUniquePerInstance(entryNoDesignHeaderItem, csvMap);
		this.validateIfPlotNumberIsUniquePerInstance(mappedHeaders.get(PhenotypicType.TRIAL_DESIGN), csvMap);
		this.validateColumnValues(designImportData.getCsvData(), mappedHeaders);
	}

	void validateColumnValues(final Map<Integer, List<String>> csvData, final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders)
			throws DesignValidationException {
		// validate values on columns with categorical variables
		final List<DesignHeaderItem> categoricalDesignHeaderItems =
				this.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.CATEGORICAL_VARIABLE.getId());
		this.validateValuesPerColumn(categoricalDesignHeaderItems, csvData, TermId.CATEGORICAL_VARIABLE.getId());

		// validate values on columns with numeric variables
		final List<DesignHeaderItem> numericDesignHeaderItems =
				this.retrieveDesignHeaderItemsBasedOnDataType(mappedHeaders, TermId.NUMERIC_VARIABLE.getId());
		this.validateValuesPerColumn(numericDesignHeaderItems, csvData, TermId.NUMERIC_VARIABLE.getId());
	}

	/****
	 * Retrieve the list of mappedHeaders according to their data type
	 * 
	 * @param mappedHeaders
	 * @param variableDataType (Numeric, Categorical, Character and Date)
	 * @return
	 */
	private List<DesignHeaderItem> retrieveDesignHeaderItemsBasedOnDataType(
			final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders, final int variableDataType) {

		final List<DesignHeaderItem> designHeaderItems = new ArrayList<DesignHeaderItem>();

		for (final List<DesignHeaderItem> mappedHeader : mappedHeaders.values()) {
			for (final DesignHeaderItem header : mappedHeader) {
				final StandardVariable standardVariable = header.getVariable();
				final Term dataType = standardVariable.getDataType();
				if (variableDataType == dataType.getId()) {
					designHeaderItems.add(header);
				}
			}
		}

		return designHeaderItems;
	}

	private void validateValuesPerColumn(final List<DesignHeaderItem> designHeaderItems, final Map<Integer, List<String>> csvData,
			final int variableDataType) throws NoSuchMessageException, DesignValidationException {

		// remove the header rows
		final Map<Integer, List<String>> csvRowData = new LinkedHashMap<Integer, List<String>>(csvData);
		csvRowData.remove(0);

		for (final DesignHeaderItem headerItem : designHeaderItems) {
			final StandardVariable standardVariable = headerItem.getVariable();
			final Integer columnIndex = headerItem.getColumnIndex();
			if (variableDataType == TermId.CATEGORICAL_VARIABLE.getId()) {
				this.validateValuesForCategoricalVariables(csvRowData, columnIndex, standardVariable);
			} else if (variableDataType == TermId.NUMERIC_VARIABLE.getId()) {
				this.validateValuesForNumericalVariables(csvRowData, columnIndex, standardVariable);
			}
		}
	}

	private void validateValuesForNumericalVariables(final Map<Integer, List<String>> csvRowData, final Integer columnIndex,
			final StandardVariable standardVariable) throws NoSuchMessageException, DesignValidationException {

		final Scale numericScale = this.ontologyScaleDataManager.getScaleById(standardVariable.getScale().getId(), false);

		for (final Map.Entry<Integer, List<String>> row : csvRowData.entrySet()) {
			final List<String> columnValues = row.getValue();
			final String valueToValidate = columnValues.get(columnIndex);
			if (!this.isValidNumericValueForNumericVariable(valueToValidate, standardVariable, numericScale)) {
				throw new DesignValidationException((this.messageSource.getMessage("design.import.error.invalid.value", null,
						Locale.ENGLISH)).replace("{0}", standardVariable.getName()));
			}
		}
	}

	private boolean isValidNumericValueForNumericVariable(final String valueToValidate, final StandardVariable variable,
			final Scale numericScale) {

		if (!org.generationcp.commons.util.StringUtil.isNumeric(valueToValidate)) {
			return false;
		}

		if (!this.isNumericValueWithinTheRange(valueToValidate, variable, numericScale)) {
			return false;
		}

		return true;
	}

	private boolean isNumericValueWithinTheRange(final String valueToValidate, final StandardVariable variable, final Scale numericScale) {
		if (numericScale != null && numericScale.getMinValue() != null && numericScale.getMaxValue() != null) {
			final Double minValue = Double.valueOf(numericScale.getMinValue());
			final Double maxValue = Double.valueOf(numericScale.getMaxValue());

			final Double currentValue = Double.valueOf(valueToValidate);
			if (!(currentValue >= minValue && currentValue <= maxValue)) {
				return false;
			}
		}

		return true;
	}

	private void validateValuesForCategoricalVariables(final Map<Integer, List<String>> csvRowData, final Integer columnIndex,
			final StandardVariable standardVariable) throws NoSuchMessageException, DesignValidationException {
		for (final Map.Entry<Integer, List<String>> row : csvRowData.entrySet()) {
			final List<String> columnValues = row.getValue();
			final String valueToValidate = columnValues.get(columnIndex);
			if (!this.isPartOfValidValuesForCategoricalVariable(valueToValidate, standardVariable)) {
				throw new DesignValidationException((this.messageSource.getMessage("design.import.error.invalid.value", null,
						Locale.ENGLISH)).replace("{0}", standardVariable.getName()));
			}
		}
	}

	private boolean isPartOfValidValuesForCategoricalVariable(final String categoricalValue, final StandardVariable categoricalVariable) {
		final List<Enumeration> possibleValues = categoricalVariable.getEnumerations();
		for (final Enumeration possibleValue : possibleValues) {
			if (categoricalValue.equalsIgnoreCase(possibleValue.getName())) {
				return true;
			}
		}
		return false;
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
	public boolean areTrialInstancesMatchTheSelectedEnvironments(final Integer noOfEnvironments, final DesignImportData designImportData) {

		final DesignHeaderItem trialInstanceDesignHeaderItem =
				this.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR,
						designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT));

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
	public Map<PhenotypicType, List<DesignHeaderItem>> categorizeHeadersByPhenotype(final List<DesignHeaderItem> designHeaders)
			throws MiddlewareException {
		final List<String> headers = new ArrayList<>();
		// get headers as string list
		for (final DesignHeaderItem item : designHeaders) {
			headers.add(item.getName());
		}

		// create groups for the Design Headers
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedDesignHeaders = new HashMap<>();

		// note: the null key here is for unmapped headers
		mappedDesignHeaders.put(null, new ArrayList<DesignHeaderItem>());
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
			final List<StandardVariable> match = variables.get(item.getName());

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
				mappedDesignHeaders.get(null).add(item);
			}

		}

		return mappedDesignHeaders;
	}

	protected DesignHeaderItem validateIfTrialFactorExists(final List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		final DesignHeaderItem headerItem = this.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, headerDesignItems);
		if (headerItem == null) {
			throw new DesignValidationException(
					this.messageSource.getMessage("design.import.error.trial.is.required", null, Locale.ENGLISH));
		} else {
			return headerItem;
		}
	}

	protected DesignHeaderItem validateIfEntryNumberExists(final List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		final DesignHeaderItem headerItem = this.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, headerDesignItems);
		if (headerItem == null) {
			throw new DesignValidationException(this.messageSource.getMessage("design.import.error.entry.no.is.required", null,
					Locale.ENGLISH));
		} else {
			return headerItem;
		}
	}

	protected void validateIfPlotNumberExists(final List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		for (final DesignHeaderItem headerDesignItem : headerDesignItems) {
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()) {
				return;
			}
		}
		throw new DesignValidationException(this.messageSource.getMessage("design.import.error.plot.no.is.required", null, Locale.ENGLISH));
	}

	protected void validateIfPlotNumberIsUniquePerInstance(final List<DesignHeaderItem> headerDesignItems,
			final Map<String, Map<Integer, List<String>>> csvMap) throws DesignValidationException {

		for (final DesignHeaderItem headerDesignItem : headerDesignItems) {
			if (headerDesignItem.getVariable().getId() == TermId.PLOT_NO.getId()) {
				for (final Entry<String, Map<Integer, List<String>>> entry : csvMap.entrySet()) {
					this.validatePlotNumberMustBeUnique(headerDesignItem, entry.getValue());
				}
			}
		}
	}

	protected void validateEntryNoMustBeUniquePerInstance(final DesignHeaderItem entryNoHeaderItem,
			final Map<String, Map<Integer, List<String>>> csvMapGrouped) throws DesignValidationException {

		for (final Entry<String, Map<Integer, List<String>>> entry : csvMapGrouped.entrySet()) {
			this.validateEntryNumberMustBeUnique(entryNoHeaderItem, entry.getValue());

		}

	}

	protected void validatePlotNumberMustBeUnique(final DesignHeaderItem plotNoHeaderItem, final Map<Integer, List<String>> csvMap)
			throws DesignValidationException {

		final Set<String> set = new HashSet<String>();

		final Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		while (iterator.hasNext()) {
			final String value = iterator.next().getValue().get(plotNoHeaderItem.getColumnIndex());

			if (!StringUtils.isNullOrEmpty(value)) {
				if (set.contains(value)) {
					throw new DesignValidationException(this.messageSource.getMessage("design.import.error.plot.number.must.be.unique",
							null, Locale.ENGLISH));
				} else {
					set.add(value);
				}
			}
		}

	}

	protected void validateEntryNumberMustBeUnique(final DesignHeaderItem entryNoHeaderItem, final Map<Integer, List<String>> csvMap)
			throws DesignValidationException {
		final Set<String> set = new HashSet<String>();

		final Iterator<Entry<Integer, List<String>>> iterator = csvMap.entrySet().iterator();
		while (iterator.hasNext()) {
			final String value = iterator.next().getValue().get(entryNoHeaderItem.getColumnIndex());
			if (StringUtils.isNullOrEmpty(value) && set.contains(value)) {
				throw new DesignValidationException(this.messageSource.getMessage("design.import.error.entry.number.unique.per.instance",
						null, Locale.ENGLISH));
			} else {
				set.add(value);
			}
		}
		this.validateGermplasmEntriesFromShouldMatchTheGermplasmList(set);
	}

	protected void validateGermplasmEntriesFromShouldMatchTheGermplasmList(final Set<String> entryNumbers) throws DesignValidationException {

		final List<ImportedGermplasm> importedGermplasmList =
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (!entryNumbers.contains(importedGermplasm.getEntryId().toString())) {
				throw new DesignValidationException(this.messageSource.getMessage(
						"design.import.error.mismatch.count.of.germplasm.entries", null, Locale.ENGLISH));
			}
		}
		if (importedGermplasmList.size() != entryNumbers.size()) {
			throw new DesignValidationException(this.messageSource.getMessage("design.import.error.mismatch.count.of.germplasm.entries",
					null, Locale.ENGLISH));
		}
	}

	protected Map<String, Map<Integer, List<String>>> groupCsvRowsIntoTrialInstance(final DesignHeaderItem trialInstanceHeaderItem,
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

	protected DesignHeaderItem filterDesignHeaderItemsByTermId(final TermId termId, final List<DesignHeaderItem> headerDesignItems) {
		for (final DesignHeaderItem headerDesignItem : headerDesignItems) {
			if (headerDesignItem.getVariable().getId() == termId.getId()) {
				return headerDesignItem;
			}
		}
		return null;
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

	protected MeasurementRow createMeasurementRow(final Workbook workbook, final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders,
			final List<String> rowValues, final List<ImportedGermplasm> importedGermplasm,
			final Map<Integer, StandardVariable> germplasmStandardVariables, final Set<String> trialInstancesFromUI, final boolean isPreview) {

		final MeasurementRow measurement = new MeasurementRow();

		final List<MeasurementData> dataList = new ArrayList<>();

		for (final Entry<PhenotypicType, List<DesignHeaderItem>> entry : mappedHeaders.entrySet()) {
			for (final DesignHeaderItem headerItem : entry.getValue()) {

				// do not add the trial instance record from file if it is not selected in environment tab
				if (headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
						&& !trialInstancesFromUI.contains(rowValues.get(headerItem.getColumnIndex()))) {
					return null;
				}

				if (headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
						&& workbook.getStudyDetails().getStudyType() == StudyType.N) {

					// do not add the trial instance to measurement data list if the workbook is Nursery
					continue;
				}

				if (headerItem.getVariable().getId() == TermId.ENTRY_NO.getId()) {

					final Integer entryNo = Integer.parseInt(rowValues.get(headerItem.getColumnIndex()));
					this.addGermplasmDetailsToDataList(importedGermplasm, germplasmStandardVariables, dataList, entryNo);
				}

				if (headerItem.getVariable().getPhenotypicType() == PhenotypicType.TRIAL_ENVIRONMENT && isPreview) {

					// only add the trial environment factors in measurement row ONLY in PREVIEW mode
					final String value = rowValues.get(headerItem.getColumnIndex());
					dataList.add(this.createMeasurementData(headerItem.getVariable(), value));
				}

				if (headerItem.getVariable().getPhenotypicType() == PhenotypicType.TRIAL_DESIGN
						|| headerItem.getVariable().getPhenotypicType() == PhenotypicType.VARIATE
						|| headerItem.getVariable().getId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {

					final String value = rowValues.get(headerItem.getColumnIndex());
					dataList.add(this.createMeasurementData(headerItem.getVariable(), value));

				}

			}
		}

		measurement.setDataList(dataList);
		return measurement;
	}

	protected MeasurementData createMeasurementData(final StandardVariable standardVariable, final String value) {
		final MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(this.createMeasurementVariable(standardVariable));
		data.setValue(value);
		data.setLabel(data.getMeasurementVariable().getName());
		data.setDataType(data.getMeasurementVariable().getDataType());
		return data;
	}

	protected MeasurementData createMeasurementData(final MeasurementVariable measurementVariable, final String value) {
		final MeasurementData data = new MeasurementData();
		data.setMeasurementVariable(measurementVariable);
		data.setValue(value);
		data.setLabel(data.getMeasurementVariable().getName());
		data.setDataType(data.getMeasurementVariable().getDataType());
		return data;
	}

	protected MeasurementVariable createMeasurementVariable(final StandardVariable standardVariable) {
		final MeasurementVariable variable =
				ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, this.fieldbookService);
		return variable;
	}

	protected Map<Integer, StandardVariable> convertToStandardVariables(final List<MeasurementVariable> list,
			final PhenotypicType phenotypicType) {

		final Map<Integer, StandardVariable> map = new HashMap<>();

		for (final MeasurementVariable measurementVariable : list) {
			try {
				final StandardVariable stdVar =
						this.ontologyService.getStandardVariable(measurementVariable.getTermId(), this.contextUtil.getCurrentProgramUUID());
				stdVar.setPhenotypicType(phenotypicType);
				map.put(measurementVariable.getTermId(), stdVar);
			} catch (final MiddlewareException e) {
				DesignImportServiceImpl.LOG.error(e.getMessage(), e);
			}
		}

		return map;
	}

	protected void addGermplasmDetailsToDataList(final List<ImportedGermplasm> importedGermplasm,
			final Map<Integer, StandardVariable> germplasmStandardVariables, final List<MeasurementData> dataList, final Integer entryNo) {

		final ImportedGermplasm germplasmEntry = importedGermplasm.get(entryNo - 1);

		if (germplasmStandardVariables.get(TermId.ENTRY_NO.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_NO.getId()), germplasmEntry.getEntryId()
					.toString()));
		}
		if (germplasmStandardVariables.get(TermId.GID.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.GID.getId()), germplasmEntry.getGid()));
		}
		if (germplasmStandardVariables.get(TermId.DESIG.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.DESIG.getId()), germplasmEntry.getDesig()));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_TYPE.getId()), germplasmEntry.getCheck()));
		}
		if (germplasmStandardVariables.get(TermId.CROSS.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.CROSS.getId()), germplasmEntry.getCross()));
		}
		if (germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.ENTRY_CODE.getId()),
					germplasmEntry.getEntryCode()));
		}
		if (germplasmStandardVariables.get(TermId.GERMPLASM_SOURCE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.GERMPLASM_SOURCE.getId()),
					germplasmEntry.getSource()));
		}
		if (germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()) != null) {
			dataList.add(this.createMeasurementData(germplasmStandardVariables.get(TermId.SEED_SOURCE.getId()), germplasmEntry.getSource()));
		}
	}

	private void addFactorsToMeasurementRows(final Workbook workbook, final List<MeasurementRow> measurements) {

		if (workbook.getStudyDetails().getStudyType() == StudyType.N) {
			for (final MeasurementVariable factor : workbook.getFactors()) {
				for (final MeasurementRow row : measurements) {
					this.addFactorToDataListIfNecessary(factor, row.getDataList());
				}

			}
		}
	}

	protected void addVariatesToMeasurementRows(final Workbook workbook, final List<MeasurementRow> measurements) {
		try {
			final Set<MeasurementVariable> temporaryList = new HashSet<>();
			for (final MeasurementVariable mvar : workbook.getVariates()) {
				if (mvar.getOperation() == Operation.ADD || mvar.getOperation() == Operation.UPDATE) {
					final MeasurementVariable copy = mvar.copy();
					temporaryList.add(copy);
				}
			}

			WorkbookUtil.addMeasurementDataToRowsIfNecessary(new ArrayList<MeasurementVariable>(temporaryList), measurements, true,
					this.userSelection, this.ontologyService, this.fieldbookService, this.contextUtil.getCurrentProgramUUID());
		} catch (final MiddlewareException e) {
			DesignImportServiceImpl.LOG.error(e.getMessage(), e);
		}
	}

	protected void addFactorToDataListIfNecessary(final MeasurementVariable factor, final List<MeasurementData> dataList) {
		for (final MeasurementData data : dataList) {
			if (data.getMeasurementVariable().equals(factor)) {
				return;
			}
		}
		dataList.add(this.createMeasurementData(factor, ""));
	}

	protected Set<String> extractTrialInstancesFromEnvironmentData(final EnvironmentData environmentData) {
		final Set<String> generatedTrialInstancesFromUI = new HashSet<>();
		for (final Environment env : environmentData.getEnvironments()) {
			generatedTrialInstancesFromUI.add(env.getManagementDetailValues().get(String.valueOf(TermId.TRIAL_INSTANCE_FACTOR.getId())));
		}
		return generatedTrialInstancesFromUI;
	}

	protected void populateEnvironmentDataWithValuesFromCsvFile(final EnvironmentData environmentData, final Workbook workbook,
			final DesignImportData designImportData) {

		final List<DesignHeaderItem> trialEnvironmentsDesignHeaderItems =
				designImportData.getMappedHeaders().get(PhenotypicType.TRIAL_ENVIRONMENT);
		final DesignHeaderItem trialInstanceHeaderItem =
				this.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, trialEnvironmentsDesignHeaderItems);
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
