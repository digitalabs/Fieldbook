
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
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

	@Override
	public String getContentName() {
		return null;
	}

	@RequestMapping(value = "/showDetails", method = RequestMethod.GET)
	public String showDetails(@ModelAttribute("reviewDetailsOutOfBoundsForm") ReviewDetailsOutOfBoundsForm form, Model model) {

		UserSelection userSelection = this.getUserSelection();
		List<MeasurementVariable> measurementVariables =
				this.getTraitsWithOutOfBoundsOnly(userSelection.getWorkbook().getMeasurementDatasetVariables());
		form.setMeasurementVariable(measurementVariables.get(0));
		form.setTraitSize(measurementVariables.size());
		form.setMeasurementVariables(this.filterColumnsForReviewDetailsTable(userSelection.getWorkbook().getAllVariables(), form
				.getMeasurementVariable().getTermId()));

		return super.showAjaxPage(model, REVIEW_DETAILS_OUT_OF_BOUNDS_PER_TRAIT_TEMPLATE_TRIAL);
	}

	@RequestMapping(value = "/showDetails/{action}", method = RequestMethod.POST)
	public String submitDetails(@PathVariable String action,
			@ModelAttribute("reviewDetailsOutOfBoundsForm") ReviewDetailsOutOfBoundsForm form, Model model) {

		UserSelection userSelection = this.getUserSelection();
		List<MeasurementVariable> measurementVariablesCategorical =
				this.getTraitsWithOutOfBoundsOnly(userSelection.getWorkbook().getMeasurementDatasetVariables());

		if ("next".equals(action)) {
			if (form.getTraitIndex() < measurementVariablesCategorical.size() - 1) {
				int nextIndex = form.getTraitIndex() + 1;
				form.setMeasurementVariable(measurementVariablesCategorical.get(nextIndex));
				form.setTraitIndex(nextIndex);
			}
		} else if ("previous".equals(action) && form.getTraitIndex() > 0) {

			int prevIndex = form.getTraitIndex() - 1;
			form.setMeasurementVariable(measurementVariablesCategorical.get(prevIndex));
			form.setTraitIndex(prevIndex);

		}
		form.setTraitSize(measurementVariablesCategorical.size());
		form.setMeasurementVariables(this.filterColumnsForReviewDetailsTable(userSelection.getWorkbook().getAllVariables(),
				form.getTraitTermId()));

		return super.showAjaxPage(model, REVIEW_DETAILS_OUT_OF_BOUNDS_PER_TRAIT_TEMPLATE_TRIAL);
	}

	@ResponseBody
	@RequestMapping(value = "/data/table/ajax", method = RequestMethod.POST)
	public List<Map<String, Object>> getPageDataTablesAjax(
			@ModelAttribute("reviewDetailsOutOfBoundsForm") ReviewDetailsOutOfBoundsForm form, Model model) {

		UserSelection userSelection = this.getUserSelection();
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		Map<String, String> trialInstanceLocationMap = this.getTrialInstanceLocationMap();
		tempList.addAll(userSelection.getMeasurementRowList());

		List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();

		int rowIndex = 0;
		for (MeasurementRow row : tempList) {

			Map<String, Object> dataMap = this.generateDatatableDataMap(rowIndex, row, form.getTraitTermId(), trialInstanceLocationMap);
			if (!dataMap.isEmpty()) {
				masterList.add(dataMap);
			}
			rowIndex++;

		}

		return masterList;
	}

	@RequestMapping(value = "/hasOutOfBoundValues", method = RequestMethod.GET)
	public ResponseEntity<Boolean> hasOutOfBoundValues() {
		List<MeasurementRow> measurementRowList = this.getUserSelection().getMeasurementRowList();
		if (null == measurementRowList) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		}

		for (MeasurementRow row : measurementRowList) {
			for (MeasurementData data : row.getDataList()) {
				if (isValueOutOfBound(data)) {
					return new ResponseEntity<>(true, HttpStatus.OK);
				}
			}
		}
		return new ResponseEntity<>(false, HttpStatus.OK);
	}

	private boolean isValueOutOfBound(final MeasurementData data) {
		return data != null
			&& !data.isAccepted()
			&& (
				data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())
				&& this.isNumericalValueOutOfBounds(data))
				|| (
				data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())
				&& this.isCategoricalValueOutOfBounds(data));
	}

	@ResponseBody
	@RequestMapping(value = "/submitDetails", method = RequestMethod.POST)
	public Map<String, String> processOutOfBoundsChanges(@RequestBody ReviewOutOfBoundsChanges changes) {

		Map<String, String> resultMap = new HashMap<String, String>();

		UserSelection userSelection = this.getUserSelection();
		List<MeasurementRow> measurementRows = userSelection.getMeasurementRowList();

		try {

			for (Datum datum : changes.getData()) {
				Integer termId = datum.getTermId();
				for (Value val : datum.getValues()) {
					MeasurementData measurementData = measurementRows.get(val.getRowIndex()).getMeasurementData(termId);
					this.updateMeasurementData(measurementData, val);
				}

			}

			resultMap.put(ReviewDetailsOutOfBoundsController.SUCCESS, "1");
		} catch (Exception e) {
			ReviewDetailsOutOfBoundsController.LOG.error(e.getMessage(), e);
			resultMap.put(ReviewDetailsOutOfBoundsController.SUCCESS, "-1");
			resultMap.put(ReviewDetailsOutOfBoundsController.ERROR_MESSAGE, e.getMessage());
		}

		return resultMap;
	}

	protected Map<String, String> getTrialInstanceLocationMap() {
		Map<String, String> map = new HashMap<>();
		UserSelection userSelection = this.getUserSelection();
		for (MeasurementRow row : userSelection.getWorkbook().getTrialObservations()) {
			String trialInstanceValue = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
			String locationValue = "";
			if (row.getMeasurementData(TermId.TRIAL_LOCATION.getId()) != null) {
				locationValue = row.getMeasurementData(TermId.TRIAL_LOCATION.getId()).getValue();
			}
			map.put(trialInstanceValue, locationValue);
		}
		return map;
	}

	protected void updateMeasurementData(MeasurementData measurementData, Value value) {

		String possibleValueId =
				this.getPossibleValueIDByValue(value.getNewValue(), measurementData.getMeasurementVariable().getPossibleValues());

		if (value.isSelected()) {
			if (value.getAction().equals(ReviewDetailsOutOfBoundsController.ACCEPT_VALUE_AS_IS)) {
				measurementData.setAccepted(true);
			} else if (value.getAction().equals(ReviewDetailsOutOfBoundsController.APPLY_NEW_VALUE_TO_SELECTED_VALUES)) {
				this.setMeasurementDataValue(possibleValueId, measurementData, value);
			} else if (value.getAction().equals(ReviewDetailsOutOfBoundsController.SET_SELECTED_VALUES_TO_MISSING)) {
				measurementData.setAccepted(true);
				measurementData.setValue(MeasurementData.MISSING_VALUE);
			}
		} else {
			this.setMeasurementDataValue(possibleValueId, measurementData, value);
		}
	}

	protected void setMeasurementDataValue(String possibleValueId, MeasurementData measurementData, Value value) {
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
		}
	}

	protected List<MeasurementVariable> getTraitsWithOutOfBoundsOnly(List<MeasurementVariable> measurementVariables) {
		List<MeasurementVariable> variables = new ArrayList<>();
		for (MeasurementVariable var : measurementVariables) {
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

	protected Boolean checkIfNumericalTraitHasOutOfBoundsData(Integer termId) {
		UserSelection userSelection = this.getUserSelection();
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		for (MeasurementRow row : tempList) {
			MeasurementData data = row.getMeasurementData(termId);
			if (data != null) {
				Boolean isNumericalValueOutOfBounds = this.isNumericalValueOutOfBounds(data);
				if (isNumericalValueOutOfBounds) {
					return isNumericalValueOutOfBounds;
				}
			}
		}

		return false;
	}

	protected Boolean checkIfCategoricalTraitHasOutOfBoundsData(Integer termId) {
		UserSelection userSelection = this.getUserSelection();
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		for (MeasurementRow row : tempList) {
			MeasurementData data = row.getMeasurementData(termId);
			Boolean isCategoricalValueOutOfBounds = this.isCategoricalValueOutOfBounds(data);
			if (isCategoricalValueOutOfBounds) {
				return isCategoricalValueOutOfBounds;
			}
		}

		return false;
	}

	protected List<MeasurementVariable> filterColumnsForReviewDetailsTable(List<MeasurementVariable> measurementVariables, int traitTermId) {
		List<MeasurementVariable> variables = new ArrayList<>();
		Boolean locationExists = false;
		for (MeasurementVariable var : measurementVariables) {
			if (var.getTermId() == TermId.ENTRY_NO.getId()) {
				variables.add(var);
			}
			if (var.getTermId() == TermId.PLOT_NO.getId()) {
				variables.add(var);
			}
			if (var.getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				variables.add(var);
			}
			if (var.getTermId() == TermId.TRIAL_LOCATION.getId()) {
				locationExists = true;
				variables.add(var);
			}
			if (var.getTermId() == traitTermId) {
				variables.add(var);
			}
		}

		if (locationExists) {
			Iterator<MeasurementVariable> iterator = variables.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					iterator.remove();
				}
			}
		}
		return variables;

	}

	protected Map<String, Object> generateDatatableDataMap(int rowIndex, MeasurementRow row, Integer targetTraitTermId,
			Map<String, String> trialInstanceLocationMap) {

		Map<String, Object> dataMap = new HashMap<String, Object>();
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
		for (MeasurementData data : row.getDataList()) {
			String displayVal = data.getDisplayValue();
			if (data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())
					&& data.getMeasurementVariable().getTermId() == targetTraitTermId) {

				isTraitCustomValue = this.isNumericalValueOutOfBounds(data);
				Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put("OLD VALUE", categArray);
			} else if (data.getMeasurementVariable().getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())
					&& data.getMeasurementVariable().getTermId() == targetTraitTermId) {

				isTraitCustomValue = this.isCategoricalValueOutOfBounds(data);
				Object[] categArray = new Object[] {displayVal, data.isAccepted()};
				dataMap.put("OLD VALUE", categArray);
			}
		}

		if (isTraitCustomValue) {
			return dataMap;
		} else {
			return new HashMap<String, Object>();
		}

	}

	protected UserSelection getUserSelection() {
		return this.studySelection;
	}

	protected void setStudySelection(UserSelection userSelection) {
		this.studySelection = userSelection;
	}

	protected boolean isCategoricalValueOutOfBounds(MeasurementData data) {

		String cValueId = data.getcValueId();
		String value = data.getValue();
		List<ValueReference> possibleValues = data.getMeasurementVariable().getPossibleValues();

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

		for (ValueReference ref : possibleValues) {
			if (ref.getKey().equals(val)) {
				return false;
			}
		}

		return true;
	}

	protected boolean isNumericalValueOutOfBounds(MeasurementData data) {
		String value = data.getValue();
		if (data.isAccepted()) {
			return false;
		}
		if (data.getMeasurementVariable().getMinRange() != null && data.getMeasurementVariable().getMaxRange() != null
				&& this.isValueOutOfRange(value, data)) {
			return true;
		}
		return false;
	}

	protected boolean isValueOutOfRange(String value, MeasurementData data) {
		if (MeasurementData.MISSING_VALUE.equalsIgnoreCase(value)) {
			return true;
		} else if (NumberUtils.isNumber(value)
				&& (Double.valueOf(value) < data.getMeasurementVariable().getMinRange() || Double.valueOf(value) > data
						.getMeasurementVariable().getMaxRange())) {
			return true;
		}
		return false;
	}

	protected String getPossibleValueIDByValue(String value, List<ValueReference> possibleValues) {
		for (ValueReference ref : possibleValues) {
			if (ref.getName().equalsIgnoreCase(value)) {
				return ref.getKey().toString();
			}
		}
		return value;
	}

}
