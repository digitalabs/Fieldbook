
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.Datum;
import com.efficio.fieldbook.web.common.bean.ReviewOutOfBoundsChanges;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.bean.Value;
import com.efficio.fieldbook.web.common.form.ReviewDetailsOutOfBoundsForm;

@Controller
@RequestMapping(ReviewDetailsOutOfBoundsController.URL)
// TODO: MARK FOR DELETE IBP-2789
public class ReviewDetailsOutOfBoundsController extends AbstractBaseFieldbookController {

	public static final String URL = "/Common/ReviewDetailsOutOfBounds";
	private static final String REVIEW_DETAILS_OUT_OF_BOUNDS_PER_TRAIT_TEMPLATE_TRIAL = "/Common/reviewDetailsOutOfBoundsPerTraitTrial";
	private static final Logger LOG = LoggerFactory.getLogger(ReviewDetailsOutOfBoundsController.class);

	private static final String SUCCESS = "success";
	private static final String ERROR_MESSAGE = "errorMessage";

	private static final String ACCEPT_VALUE_AS_IS = "1";
	private static final String APPLY_NEW_VALUE_TO_SELECTED_VALUES = "2";
	private static final String SET_SELECTED_VALUES_TO_MISSING = "3";

	@Resource
	private UserSelection studySelection;

	@Resource
	private StudyDataManager studyDataManager;

	@Override
	public String getContentName() {
		return null;
	}

	@RequestMapping(value = "/showDetails", method = RequestMethod.GET)
	public String showDetails(@ModelAttribute("reviewDetailsOutOfBoundsForm") final ReviewDetailsOutOfBoundsForm form, final Model model) {

		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementVariable> measurementVariables =
				this.getTraitsWithOutOfBoundsOnly(userSelection.getWorkbook().getMeasurementDatasetVariables());
		form.setMeasurementVariable(measurementVariables.get(0));
		form.setTraitSize(measurementVariables.size());
		form.setMeasurementVariables(this.filterColumnsForReviewDetailsTable(userSelection.getWorkbook().getAllVariables(),
				form.getMeasurementVariable().getTermId()));

		return super.showAjaxPage(model, ReviewDetailsOutOfBoundsController.REVIEW_DETAILS_OUT_OF_BOUNDS_PER_TRAIT_TEMPLATE_TRIAL);
	}

	@RequestMapping(value = "/showDetails/{action}", method = RequestMethod.POST)
	public String submitDetails(@PathVariable final String action,
			@ModelAttribute("reviewDetailsOutOfBoundsForm") final ReviewDetailsOutOfBoundsForm form, final Model model) {

		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementVariable> measurementVariablesCategorical =
				this.getTraitsWithOutOfBoundsOnly(userSelection.getWorkbook().getMeasurementDatasetVariables());

		if ("next".equals(action)) {
			if (form.getTraitIndex() < measurementVariablesCategorical.size() - 1) {
				final int nextIndex = form.getTraitIndex() + 1;
				form.setMeasurementVariable(measurementVariablesCategorical.get(nextIndex));
				form.setTraitIndex(nextIndex);
			}
		} else if ("previous".equals(action) && form.getTraitIndex() > 0) {

			final int prevIndex = form.getTraitIndex() - 1;
			form.setMeasurementVariable(measurementVariablesCategorical.get(prevIndex));
			form.setTraitIndex(prevIndex);

		}
		form.setTraitSize(measurementVariablesCategorical.size());
		form.setMeasurementVariables(
				this.filterColumnsForReviewDetailsTable(userSelection.getWorkbook().getAllVariables(), form.getTraitTermId()));

		return super.showAjaxPage(model, ReviewDetailsOutOfBoundsController.REVIEW_DETAILS_OUT_OF_BOUNDS_PER_TRAIT_TEMPLATE_TRIAL);
	}

	@ResponseBody
	@RequestMapping(value = "/data/table/ajax", method = RequestMethod.POST)
	public List<Map<String, Object>> getPageDataTablesAjax(
			@ModelAttribute("reviewDetailsOutOfBoundsForm") final ReviewDetailsOutOfBoundsForm form, final Model model) {

		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementRow> tempList = new ArrayList<>();
		final Map<String, String> trialInstanceLocationMap = this.getTrialInstanceLocationMap();
		tempList.addAll(userSelection.getMeasurementRowList());

		final List<Map<String, Object>> masterList = new ArrayList<>();

		int rowIndex = 0;
		for (final MeasurementRow row : tempList) {

			final Map<String, Object> dataMap =
					this.generateDatatableDataMap(rowIndex, row, form.getTraitTermId(), trialInstanceLocationMap);
			if (!dataMap.isEmpty()) {
				masterList.add(dataMap);
			}
			rowIndex++;

		}

		return masterList;
	}

	@RequestMapping(value = "/hasOutOfBoundValues", method = RequestMethod.GET)
	public ResponseEntity<Boolean> hasOutOfBoundValues() {
		final List<MeasurementRow> measurementRowList = this.getUserSelection().getMeasurementRowList();
		if (null == measurementRowList) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		}

		for (final MeasurementRow row : measurementRowList) {
			for (final MeasurementData data : row.getDataList()) {
				if (this.isValueOutOfBound(data)) {
					return new ResponseEntity<>(true, HttpStatus.OK);
				}
			}
		}
		return new ResponseEntity<>(false, HttpStatus.OK);
	}

	private boolean isValueOutOfBound(final MeasurementData data) {
		return data != null && !data.isAccepted() && data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())
				&& this.isNumericalValueOutOfBounds(data)
				|| data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())
						&& this.isCategoricalValueOutOfBounds(data);
	}

	@ResponseBody
	@RequestMapping(value = "/submitDetails", method = RequestMethod.POST)
	public Map<String, String> processOutOfBoundsChanges(@RequestBody final ReviewOutOfBoundsChanges changes) {

		final Map<String, String> resultMap = new HashMap<>();

		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementRow> measurementRows = userSelection.getMeasurementRowList();

		try {

			for (final Datum datum : changes.getData()) {
				final Integer termId = datum.getTermId();
				for (final Value val : datum.getValues()) {
					final MeasurementData measurementData = measurementRows.get(val.getRowIndex()).getMeasurementData(termId);
					this.updateMeasurementData(measurementData, val);
				}

			}

			resultMap.put(ReviewDetailsOutOfBoundsController.SUCCESS, "1");
		} catch (final Exception e) {
			ReviewDetailsOutOfBoundsController.LOG.error(e.getMessage(), e);
			resultMap.put(ReviewDetailsOutOfBoundsController.SUCCESS, "-1");
			resultMap.put(ReviewDetailsOutOfBoundsController.ERROR_MESSAGE, e.getMessage());
		}

		return resultMap;
	}

	protected Map<String, String> getTrialInstanceLocationMap() {
		final Map<String, String> map = new HashMap<>();
		final UserSelection userSelection = this.getUserSelection();
		final Integer studyId = this.studySelection.getWorkbook().getStudyDetails().getId();
		final Map<String, String> locationNameMap = this.studyDataManager.createInstanceLocationIdToNameMapFromStudy(studyId);
		for (final MeasurementRow row : userSelection.getWorkbook().getTrialObservations()) {
			final String trialInstanceValue = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
			String locationValue = "";
			if (row.getMeasurementData(TermId.LOCATION_ID.getId()) != null) {
				final String locationId = row.getMeasurementData(TermId.LOCATION_ID.getId()).getValue();
				locationValue = locationNameMap.get(locationId);
			}
			map.put(trialInstanceValue, locationValue);
		}
		return map;
	}

	protected void updateMeasurementData(final MeasurementData measurementData, final Value value) {

		final String possibleValueId =
				this.getPossibleValueIDByValue(value.getNewValue(), measurementData.getMeasurementVariable().getPossibleValues());

		if (value.isSelected()) {
			if (value.getAction().equals(ReviewDetailsOutOfBoundsController.ACCEPT_VALUE_AS_IS)) {
				measurementData.setAccepted(true);
			} else if (value.getAction().equals(ReviewDetailsOutOfBoundsController.APPLY_NEW_VALUE_TO_SELECTED_VALUES)) {
				this.setMeasurementDataValue(possibleValueId, measurementData, value);
			} else if (value.getAction().equals(ReviewDetailsOutOfBoundsController.SET_SELECTED_VALUES_TO_MISSING)) {
				measurementData.setAccepted(true);
				measurementData.setValue(MeasurementData.MISSING_VALUE);
				if (measurementData.getMeasurementVariable().getFormula() != null) {
					measurementData.setValueStatus(Phenotype.ValueStatus.MANUALLY_EDITED);
				}
			}
		} else {
			this.setMeasurementDataValue(possibleValueId, measurementData, value);
		}
	}

	protected void setMeasurementDataValue(final String possibleValueId, final MeasurementData measurementData, final Value value) {
		if (!value.getNewValue().isEmpty()) {

			if (measurementData.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				measurementData.setAccepted(true);
				measurementData.setValue(value.getNewValue());

			} else if (measurementData.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
				if (possibleValueId.equalsIgnoreCase(value.getNewValue())) {
					measurementData.setAccepted(true);
				}
				measurementData.setValue(possibleValueId);
			}
			if (measurementData.getMeasurementVariable().getFormula() != null) {
				measurementData.setValueStatus(Phenotype.ValueStatus.MANUALLY_EDITED);
			}
		}
	}

	protected List<MeasurementVariable> getTraitsWithOutOfBoundsOnly(final List<MeasurementVariable> measurementVariables) {
		final List<MeasurementVariable> variables = new ArrayList<>();
		for (final MeasurementVariable var : measurementVariables) {
			if (var.getDataTypeId() != null && var.getDataTypeId() == TermId.NUMERIC_VARIABLE.getId() && !var.isFactor()
					&& this.checkIfNumericalTraitHasOutOfBoundsData(var.getTermId())) {
				variables.add(var);
			} else if (var.getPossibleValues() != null && !var.getPossibleValues().isEmpty() && !var.isFactor()
					&& this.checkIfCategoricalTraitHasOutOfBoundsData(var.getTermId())) {

				variables.add(var);
			}
		}
		return variables;

	}

	protected Boolean checkIfNumericalTraitHasOutOfBoundsData(final Integer termId) {
		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementRow> tempList = new ArrayList<>();
		tempList.addAll(userSelection.getMeasurementRowList());

		for (final MeasurementRow row : tempList) {
			final MeasurementData data = row.getMeasurementData(termId);
			if (data != null) {
				final Boolean isNumericalValueOutOfBounds = this.isNumericalValueOutOfBounds(data);
				if (isNumericalValueOutOfBounds) {
					return isNumericalValueOutOfBounds;
				}
			}
		}

		return false;
	}

	protected Boolean checkIfCategoricalTraitHasOutOfBoundsData(final Integer termId) {
		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementRow> tempList = new ArrayList<>();
		tempList.addAll(userSelection.getMeasurementRowList());

		for (final MeasurementRow row : tempList) {
			final MeasurementData data = row.getMeasurementData(termId);
			final Boolean isCategoricalValueOutOfBounds = this.isCategoricalValueOutOfBounds(data);
			if (isCategoricalValueOutOfBounds) {
				return isCategoricalValueOutOfBounds;
			}
		}

		return false;
	}

	protected List<MeasurementVariable> filterColumnsForReviewDetailsTable(final List<MeasurementVariable> measurementVariables,
			final int traitTermId) {
		final List<MeasurementVariable> variables = new ArrayList<>();
		Boolean locationExists = false;
		for (final MeasurementVariable var : measurementVariables) {
			if (Arrays.asList(TermId.ENTRY_NO.getId(), TermId.PLOT_NO.getId(), TermId.TRIAL_INSTANCE_FACTOR.getId(), traitTermId)
					.contains(var.getTermId())) {
				variables.add(var);

			} else if (var.getTermId() == TermId.LOCATION_ID.getId()) {
				locationExists = true;
				variables.add(var);
			}
		}

		if (locationExists) {
			final Iterator<MeasurementVariable> iterator = variables.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					iterator.remove();
				}
			}
		}
		return variables;

	}

	protected Map<String, Object> generateDatatableDataMap(final int rowIndex, final MeasurementRow row, final Integer targetTraitTermId,
			final Map<String, String> trialInstanceLocationMap) {

		final Map<String, Object> dataMap = new HashMap<>();
		// the 4 attributes are needed always
		String trialInstanceValue = row.getMeasurementDataValue(TermId.TRIAL_INSTANCE_FACTOR.getId());

		if (trialInstanceValue == null) {
			trialInstanceValue = "1";
		}
		dataMap.put("TRIAL_INSTANCE", trialInstanceValue);
		dataMap.put("LOCATION_NAME", trialInstanceLocationMap.get(trialInstanceValue));
		dataMap.put("ENTRY_NO", row.getMeasurementDataValue(TermId.ENTRY_NO.getId()));
		dataMap.put("PLOT_NO", row.getMeasurementDataValue(TermId.PLOT_NO.getId()));
		dataMap.put("MEASUREMENT_ROW_INDEX", rowIndex);

		boolean isTraitCustomValue = false;
		for (final MeasurementData data : row.getDataList()) {
			final String displayVal = data.getDisplayValue();
			if (data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())
					&& data.getMeasurementVariable().getTermId() == targetTraitTermId) {

				isTraitCustomValue = this.isNumericalValueOutOfBounds(data);
				final Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put("OLD VALUE", categArray);
			} else if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())
					&& data.getMeasurementVariable().getTermId() == targetTraitTermId) {

				isTraitCustomValue = this.isCategoricalValueOutOfBounds(data);
				final Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put("OLD VALUE", categArray);
			}
		}

		if (isTraitCustomValue) {
			return dataMap;
		} else {
			return new HashMap<>();
		}

	}

	protected UserSelection getUserSelection() {
		return this.studySelection;
	}

	protected void setStudySelection(final UserSelection userSelection) {
		this.studySelection = userSelection;
	}

	public void setStudyDataManager(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	protected boolean isCategoricalValueOutOfBounds(final MeasurementData data) {

		final String cValueId = data.getcValueId();
		final String value = data.getValue();
		final List<ValueReference> possibleValues = data.getMeasurementVariable().getPossibleValues();

		if (data.isAccepted()) {
			return false;
		}

		if (StringUtil.isEmpty(cValueId) && StringUtil.isEmpty(value)) {
			return false;
		}

		String val = cValueId;
		if (val == null || StringUtil.isEmpty(cValueId)) {
			val = value;
		}

		for (final ValueReference ref : possibleValues) {
			if (ref.getKey().equals(val)) {
				return false;
			}
		}

		return true;
	}

	protected boolean isNumericalValueOutOfBounds(final MeasurementData data) {
		final String value = data.getValue();
		if (data.isAccepted()) {
			return false;
		}
		return data.getMeasurementVariable().getMinRange() != null && data.getMeasurementVariable().getMaxRange() != null
				&& this.isValueOutOfRange(value, data);
	}

	protected boolean isValueOutOfRange(final String value, final MeasurementData data) {
		if (MeasurementData.MISSING_VALUE.equalsIgnoreCase(value)) {
			return true;
		}
		return NumberUtils.isNumber(value) && (Double.valueOf(value) < data.getMeasurementVariable().getMinRange()
				|| Double.valueOf(value) > data.getMeasurementVariable().getMaxRange());
	}

	protected String getPossibleValueIDByValue(final String value, final List<ValueReference> possibleValues) {
		for (final ValueReference ref : possibleValues) {
			if (ref.getName().equalsIgnoreCase(value)) {
				return ref.getKey();
			}
		}
		return value;
	}

}
