
package com.efficio.fieldbook.web.nursery.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.web.common.bean.DesignHeaderItem;
import com.efficio.fieldbook.web.common.bean.DesignImportData;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.DesignValidationException;
import com.efficio.fieldbook.web.common.service.DesignImportService;
import com.mysql.jdbc.StringUtils;

public class DesignImportValidator {

	@Resource
	private UserSelection userSelection;

	@Resource
	private MessageSource messageSource;

	@Resource
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Resource
	private DesignImportService designImportService;

	public void validateDesignData(final DesignImportData designImportData) throws DesignValidationException {

		final Map<Integer, List<String>> csvData = designImportData.getCsvData();
		final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders = designImportData.getMappedHeaders();

		final DesignHeaderItem trialInstanceDesignHeaderItem =
				this.validateIfTrialFactorExists(mappedHeaders.get(PhenotypicType.TRIAL_ENVIRONMENT));
		final DesignHeaderItem entryNoDesignHeaderItem = this.validateIfEntryNumberExists(mappedHeaders.get(PhenotypicType.GERMPLASM));
		this.validateIfPlotNumberExists(mappedHeaders.get(PhenotypicType.TRIAL_DESIGN));

		final Map<String, Map<Integer, List<String>>> csvMap =
				this.designImportService.groupCsvRowsIntoTrialInstance(trialInstanceDesignHeaderItem, csvData);

		this.validateEntryNoMustBeUniquePerInstance(entryNoDesignHeaderItem, csvMap);
		this.validateIfPlotNumberIsUniquePerInstance(mappedHeaders.get(PhenotypicType.TRIAL_DESIGN), csvMap);
		this.validateColumnValues(designImportData.getCsvData(), mappedHeaders);
	}

	protected DesignHeaderItem validateIfTrialFactorExists(final List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		final DesignHeaderItem headerItem =
				this.designImportService.filterDesignHeaderItemsByTermId(TermId.TRIAL_INSTANCE_FACTOR, headerDesignItems);
		if (headerItem == null) {
			throw new DesignValidationException(
					this.messageSource.getMessage("design.import.error.trial.is.required", null, Locale.ENGLISH));
		} else {
			return headerItem;
		}
	}

	protected DesignHeaderItem validateIfEntryNumberExists(final List<DesignHeaderItem> headerDesignItems) throws DesignValidationException {
		final DesignHeaderItem headerItem = this.designImportService.filterDesignHeaderItemsByTermId(TermId.ENTRY_NO, headerDesignItems);
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

	protected void validateEntryNoMustBeUniquePerInstance(final DesignHeaderItem entryNoHeaderItem,
			final Map<String, Map<Integer, List<String>>> csvMapGrouped) throws DesignValidationException {

		for (final Entry<String, Map<Integer, List<String>>> entry : csvMapGrouped.entrySet()) {
			this.validateEntryNumberMustBeUnique(entryNoHeaderItem, entry.getValue());

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
	List<DesignHeaderItem> retrieveDesignHeaderItemsBasedOnDataType(final Map<PhenotypicType, List<DesignHeaderItem>> mappedHeaders,
			final int variableDataType) {

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
			final int variableDataType) throws DesignValidationException {

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
			final StandardVariable standardVariable) throws DesignValidationException {

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

	/**
	 * Returns true if the input is an valid number and within the specified range of the numeric variable.
	 * 
	 * @param valueToValidate
	 * @param variable
	 * @param numericScale
	 * @return
	 */
	boolean isValidNumericValueForNumericVariable(final String valueToValidate, final StandardVariable variable, final Scale numericScale) {

		if (!org.generationcp.commons.util.StringUtil.isNumeric(valueToValidate)) {
			return false;
		}

		if (!this.isNumericValueWithinTheRange(valueToValidate, variable, numericScale)) {
			return false;
		}

		return true;
	}

	boolean isNumericValueWithinTheRange(final String valueToValidate, final StandardVariable variable, final Scale numericScale) {
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

	void validateValuesForCategoricalVariables(final Map<Integer, List<String>> csvRowData, final Integer columnIndex,
			final StandardVariable standardVariable) throws DesignValidationException {
		for (final Map.Entry<Integer, List<String>> row : csvRowData.entrySet()) {
			final List<String> columnValues = row.getValue();
			final String valueToValidate = columnValues.get(columnIndex);

			if (!this.isPartOfValidValuesForCategoricalVariable(valueToValidate, standardVariable)) {
				throw new DesignValidationException((this.messageSource.getMessage("design.import.error.invalid.value", null,
						Locale.ENGLISH)).replace("{0}", standardVariable.getName()));
			}
		}
	}

	boolean isPartOfValidValuesForCategoricalVariable(final String categoricalValue, final StandardVariable categoricalVariable)
			throws DesignValidationException {
		final List<Enumeration> possibleValues = categoricalVariable.getEnumerations();

		// categorical variables are expected to have possible values, otherwise this will cause data error
		if (possibleValues == null || possibleValues.isEmpty()) {
			throw new DesignValidationException(
					(this.messageSource.getMessage("design.import.error.no.valid.values", null, Locale.ENGLISH)).replace("{0}",
							categoricalVariable.getName()));
		}

		for (final Enumeration possibleValue : possibleValues) {
			if (categoricalValue.equalsIgnoreCase(possibleValue.getName())) {
				return true;
			}
		}
		return false;
	}

}
