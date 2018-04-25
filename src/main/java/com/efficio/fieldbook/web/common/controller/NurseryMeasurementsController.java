
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.CategoricalDisplayValue;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/nursery/measurements")
public class NurseryMeasurementsController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(NurseryMeasurementsController.class);
	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String INDEX = "index";
	static final String SUCCESS = "success";
	private static final String TERM_ID = "termId";
	static final String DATA = "data";

	@Resource
	private UserSelection userSelection;

	@Resource
	private ValidationService validationService;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private PaginationListSelection paginationListSelection;

	@Resource
	private StudyService studyService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Override
	public String getContentName() {
		return null;
	}

	@Deprecated
	@RequestMapping(value = "/inlineinput/single/{index}/{termId}", method = RequestMethod.GET)
	public String inlineInputNurseryGet(@PathVariable final int index, @PathVariable final int termId, final Model model) throws MiddlewareQueryException {

		final List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();

		final List<MeasurementRow> measurementRowList = this.userSelection.getMeasurementRowList();

		tempList.addAll(measurementRowList);

		final MeasurementRow row = tempList.get(index);
		final MeasurementRow copyRow = row.copy();
		this.copyMeasurementValue(copyRow, row);
		MeasurementData editData = null;
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		if (copyRow != null && copyRow.getMeasurementVariables() != null) {
			for (final MeasurementData var : copyRow.getDataList()) {
				this.convertToUIDateIfDate(var);
				final MeasurementVariable variable = var.getMeasurementVariable();
				if (var != null && (variable.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
						|| (variable.getPossibleValues() != null && !variable.getPossibleValues().isEmpty()))) {
					possibleValues = variable.getPossibleValues();
					if (possibleValues.isEmpty()) {
						variable.setPossibleValues(this.fieldbookService.getAllPossibleValues(variable.getTermId()));
						possibleValues = variable.getPossibleValues();
					}
				}
				if (var != null && variable.getTermId() == termId) {
					editData = var;
					break;
				}
			}
		}
		this.updateModel(model, editData, index, termId, possibleValues);
		return super.showAjaxPage(model, "/NurseryManager/inlineInputMeasurement");
	}

	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/inlineinput/single", method = RequestMethod.POST)
	public Map<String, Object> inlineInputNurseryPost(@RequestBody final Map<String, String> data, final HttpServletRequest req) {

		final Map<String, Object> map = new HashMap<String, Object>();

		final int index = Integer.valueOf(data.get(NurseryMeasurementsController.INDEX));
		final int termId = Integer.valueOf(data.get(NurseryMeasurementsController.TERM_ID));
		final String value = data.get("value");
		// for categorical
		final int isNew = Integer.valueOf(data.get("isNew"));
		final boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard")) ? true : false;

		map.put(NurseryMeasurementsController.INDEX, index);

		final List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		final MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

		try {
			if (!isDiscard) {
				final MeasurementRow copyRow = originalRow.copy();
				this.copyMeasurementValue(copyRow, originalRow, isNew == 1 ? true : false);
				// we set the data to the copy row
				if (copyRow != null && copyRow.getMeasurementVariables() != null) {
					this.updatePhenotypeValues(copyRow.getDataList(), value, termId, isNew);
				}
				this.validationService.validateObservationValues(userSelection.getWorkbook(), copyRow);
				// if there are no error, meaning everything is good, thats the time we copy it to the original
				this.copyMeasurementValue(originalRow, copyRow, isNew == 1 ? true : false);
				this.updateDates(originalRow);
			}
			map.put(NurseryMeasurementsController.SUCCESS, "1");
			final Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
			map.put(NurseryMeasurementsController.DATA, dataMap);
		} catch (final MiddlewareQueryException e) {
			NurseryMeasurementsController.LOG.error(e.getMessage(), e);
			map.put(NurseryMeasurementsController.SUCCESS, "0");
			map.put(NurseryMeasurementsController.ERROR_MESSAGE, e.getMessage());
		}

		return map;
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public List<Map<String, Object>> nurseryMeasurementsGet(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model) {
		final List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();

		if (this.userSelection.getTemporaryWorkbook() != null && this.userSelection.getMeasurementRowList() == null) {
			tempList.addAll(this.userSelection.getTemporaryWorkbook().getObservations());
		} else {
			tempList.addAll(this.userSelection.getMeasurementRowList());
		}

		form.setMeasurementRowList(tempList);

		final List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();

		for (final MeasurementRow row : tempList) {

			final Map<String, Object> dataMap = this.generateDatatableDataMap(row, "");

			masterList.add(dataMap);
		}

		return masterList;
	}

	/**
	 * This is the GET call to open the action dialog to edit one row.
	 */
	@RequestMapping(value = "/inlineinput/multiple/{index}", method = RequestMethod.GET)
	public String inlineInputNurseryMultipleGet(@PathVariable final int index,
			@ModelAttribute("addOrRemoveTraitsForm") final AddOrRemoveTraitsForm form, final Model model) throws MiddlewareQueryException {

		final List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		final MeasurementRow row = tempList.get(index);
		final MeasurementRow copyRow = row.copy();
		this.copyMeasurementValue(copyRow, row);
		if (copyRow != null && copyRow.getMeasurementVariables() != null) {
			for (final MeasurementData var : copyRow.getDataList()) {
				if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
						&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
					// we change the date to the UI format
					var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
				}
			}
		}
		form.setUpdateObservation(copyRow);
		form.setExperimentIndex(index);
		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());
		return super.showAjaxPage(model, "/Common/updateExperimentModal");
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/multiple/{index}", method = RequestMethod.POST)
	public Map<String, Object> inlineInputNurseryMultiplePost(@PathVariable final int index,
			@ModelAttribute("addOrRemoveTraitsForm") final AddOrRemoveTraitsForm form) {
		final Map<String, Object> map = new HashMap<String, Object>();
		final List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		final MeasurementRow row = form.getUpdateObservation();
		final MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);
		final MeasurementRow copyRow = originalRow.copy();
		this.copyMeasurementValue(copyRow, row);

		try {
			this.validationService.validateObservationValues(userSelection.getWorkbook(), copyRow);
			// if there are no error, meaning everything is good, thats the time we copy it to the original
			this.copyMeasurementValue(originalRow, row);
			this.updateDates(originalRow);
			map.put(NurseryMeasurementsController.SUCCESS, "1");
			for (final MeasurementData data : originalRow.getDataList()) {
				// we set the data accepted automatically to true, if value is out out limit
				if (data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
					final Double minRange = data.getMeasurementVariable().getMinRange();
					final Double maxRange = data.getMeasurementVariable().getMaxRange();
					if (minRange != null && maxRange != null && NumberUtils.isNumber(data.getValue())
							&& (Double.parseDouble(data.getValue()) < minRange || Double.parseDouble(data.getValue()) > maxRange)) {
						data.setAccepted(true);
					}
				}
			}

			final Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
			map.put(NurseryMeasurementsController.DATA, dataMap);
		} catch (final MiddlewareQueryException e) {
			NurseryMeasurementsController.LOG.error(e.getMessage(), e);
			map.put(NurseryMeasurementsController.SUCCESS, "0");
			map.put(NurseryMeasurementsController.ERROR_MESSAGE, e.getMessage());
		}

		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/accepted", method = RequestMethod.POST)
	public Map<String, Object> markExperimentCellDataAsAccepted(@RequestBody final Map<String, String> data, final HttpServletRequest req) {

		final Map<String, Object> map = new HashMap<String, Object>();

		final int index = Integer.valueOf(data.get(NurseryMeasurementsController.INDEX));
		final int termId = Integer.valueOf(data.get(NurseryMeasurementsController.TERM_ID));

		map.put(NurseryMeasurementsController.INDEX, index);

		final MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

		if (originalRow != null && originalRow.getMeasurementVariables() != null) {
			for (final MeasurementData var : originalRow.getDataList()) {
				if (var != null && var.getMeasurementVariable().getTermId() == termId
						&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
								|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
					var.setAccepted(true);
					if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(),
							var.getMeasurementVariable().getPossibleValues())) {
						var.setCustomCategoricalValue(true);
					} else {
						var.setCustomCategoricalValue(false);
					}
					break;
				} else if (var != null && var.getMeasurementVariable().getTermId() == termId
						&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
					var.setAccepted(true);
					break;
				}
			}
		}

		map.put(NurseryMeasurementsController.SUCCESS, "1");
		final Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
		map.put(NurseryMeasurementsController.DATA, dataMap);

		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/accepted/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsAccepted() {

		final Map<String, Object> map = new HashMap<String, Object>();

		for (final MeasurementRow row : userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsAccepted(row.getDataList());
			}
		}

		map.put(NurseryMeasurementsController.SUCCESS, "1");

		return map;
	}

	@RequestMapping(value = "/pageView/{studyType}/{pageNum}", method = RequestMethod.GET)
	public String getPaginatedListViewOnly(@PathVariable final String studyType, @PathVariable final int pageNum,
			@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model, @RequestParam("listIdentifier")
	final String datasetId) {

		final List<MeasurementRow> rows = this.paginationListSelection.getReviewDetailsList(datasetId);
		if (rows != null) {
			form.setMeasurementRowList(rows);
			form.changePage(pageNum);
		}
		final List<MeasurementVariable> variables = this.paginationListSelection.getReviewVariableList(datasetId);
		if (variables != null) {
			form.setMeasurementVariables(variables);
		}
		form.changePage(pageNum);
		userSelection.setCurrentPage(form.getCurrentPage());
		return super.showAjaxPage(model, "/NurseryManager/datasetSummaryView");
	}

	private void markNonEmptyVariateValuesAsAccepted(final List<MeasurementData> measurementDataList) {
		for (final MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
					&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
				}
			} else if (var != null && !StringUtils.isEmpty(var.getValue())
					&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
							|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
				var.setAccepted(true);
				if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(),
						var.getMeasurementVariable().getPossibleValues())) {
					var.setCustomCategoricalValue(true);
				} else {
					var.setCustomCategoricalValue(false);
				}

			}
		}
	}

	private void markNonEmptyVariateValuesAsMissing(final List<MeasurementData> measurementDataList) {
		for (final MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
					&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
					var.setValue(MeasurementData.MISSING_VALUE);
				}
			} else if (var != null && !StringUtils.isEmpty(var.getValue())
					&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
							|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
				var.setAccepted(true);
				if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(),
						var.getMeasurementVariable().getPossibleValues())) {
					var.setValue(MeasurementData.MISSING_VALUE);
					var.setCustomCategoricalValue(true);
				} else {
					var.setCustomCategoricalValue(false);
				}
			}
		}
	}

	protected boolean isNumericalValueOutOfBounds(final String value, final MeasurementVariable var) {
		return var.getMinRange() != null && var.getMaxRange() != null && NumberUtils.isNumber(value)
				&& (Double.valueOf(value) < var.getMinRange() || Double.valueOf(value) > var.getMaxRange());
	}

	@ResponseBody
	@RequestMapping(value = "/inlineinput/missing/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsMissing() {
		final Map<String, Object> map = new HashMap<String, Object>();
		for (final MeasurementRow row : userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsMissing(row.getDataList());
			}
		}
		map.put(TrialMeasurementsController.SUCCESS, "1");
		return map;
	}

	private Map<String, Object> generateDatatableDataMap(final MeasurementRow row, String suffix) {
		final Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 4 attributes are needed always
		dataMap.put("Action", Integer.toString(row.getExperimentId()));
		dataMap.put("experimentId", Integer.toString(row.getExperimentId()));
		dataMap.put("GID", row.getMeasurementDataValue(TermId.GID.getId()));
		dataMap.put("DESIGNATION", row.getMeasurementDataValue(TermId.DESIG.getId()));

		// initialize suffix as empty string if its null
		suffix = null == suffix ? "" : suffix;

		// generate measurement row data from dataList (existing / generated data)
		for (final MeasurementData data : row.getDataList()) {
			if (data.isCategorical()) {
				final CategoricalDisplayValue categoricalDisplayValue = data.getDisplayValueForCategoricalData();

				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {categoricalDisplayValue.getName() + suffix,
						categoricalDisplayValue.getDescription() + suffix, data.isAccepted()});

			} else if (data.isNumeric()) {
				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {data.getDisplayValue() + suffix, data.isAccepted()});
			} else {
				dataMap.put(data.getMeasurementVariable().getName(), data.getDisplayValue());
			}
		}

		// generate measurement row data from newly added traits (no data yet)
		if (this.userSelection != null && this.userSelection.getMeasurementDatasetVariable() != null
				&& !this.userSelection.getMeasurementDatasetVariable().isEmpty()) {
			for (final MeasurementVariable var : this.userSelection.getMeasurementDatasetVariable()) {
				if (!dataMap.containsKey(var.getName())) {
					if (var.getDataTypeId().equals(TermId.CATEGORICAL_VARIABLE.getId())) {
						dataMap.put(var.getName(), new Object[] {"", "", true});
					} else {
						dataMap.put(var.getName(), "");
					}
				}
			}
		}
		return dataMap;
	}

	private void updateModel(final Model model, final MeasurementData measurementData, final int index, final int termId,
			final List<ValueReference> possibleValues) {
		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());
		model.addAttribute("measurementData", measurementData);
		model.addAttribute(NurseryMeasurementsController.INDEX, index);
		model.addAttribute(NurseryMeasurementsController.TERM_ID, termId);
		model.addAttribute("possibleValues", possibleValues);
	}

	protected void copyMeasurementValue(final MeasurementRow origRow, final MeasurementRow valueRow) {
		this.copyMeasurementValue(origRow, valueRow, false);
	}

	protected void copyMeasurementValue(final MeasurementRow origRow, final MeasurementRow valueRow, final boolean isNew) {

		for (int index = 0; index < origRow.getDataList().size(); index++) {
			final MeasurementData data = origRow.getDataList().get(index);
			final MeasurementData valueRowData = valueRow.getDataList().get(index);
			this.copyMeasurementDataValue(data, valueRowData, isNew);
		}
	}

	private void copyMeasurementDataValue(final MeasurementData oldData, final MeasurementData newData, final boolean isNew) {
		if (oldData.getMeasurementVariable().getPossibleValues() != null
				&& !oldData.getMeasurementVariable().getPossibleValues().isEmpty()) {
			oldData.setAccepted(newData.isAccepted());
			if (!StringUtils.isEmpty(oldData.getValue()) && oldData.isAccepted() && this.isCategoricalValueOutOfBounds(
					oldData.getcValueId(), oldData.getValue(), oldData.getMeasurementVariable().getPossibleValues())) {
				oldData.setCustomCategoricalValue(true);
			} else {
				oldData.setCustomCategoricalValue(false);
			}
			if (newData.getcValueId() != null) {
				if (isNew) {
					oldData.setCustomCategoricalValue(true);
					oldData.setcValueId(null);
				} else {
					oldData.setcValueId(newData.getcValueId());
					oldData.setCustomCategoricalValue(false);
				}
				oldData.setValue(newData.getcValueId());
			} else if (newData.getValue() != null) {
				if (isNew) {
					oldData.setCustomCategoricalValue(true);
					oldData.setcValueId(null);
				} else {
					oldData.setcValueId(newData.getValue());
					oldData.setCustomCategoricalValue(false);
				}
				oldData.setValue(newData.getValue());
			}
		} else {
			oldData.setValue(newData.getValue());
			oldData.setAccepted(newData.isAccepted());
		}
	}

	private boolean isCategoricalValueOutOfBounds(final String cValueId, final String value, final List<ValueReference> possibleValues) {
		String val = cValueId;
		if (val == null) {
			val = value;
		}
		for (final ValueReference ref : possibleValues) {
			if (ref.getKey().equals(val)) {
				return false;
			}
		}
		return true;
	}

	private void updatePhenotypeValues(final List<MeasurementData> measurementDataList, final String value, final int termId,
			final int isNew) {
		for (final MeasurementData var : measurementDataList) {
			if (var != null && var.getMeasurementVariable().getTermId() == termId) {
				if (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
						|| !var.getMeasurementVariable().getPossibleValues().isEmpty()) {
					if (isNew == 1) {
						var.setcValueId(null);
						var.setCustomCategoricalValue(true);
					} else {
						var.setcValueId(value);
						var.setCustomCategoricalValue(false);
					}
					var.setValue(value);
					var.setAccepted(true);
				} else {
					var.setAccepted(true);
					var.setValue(value);
				}
				break;
			}
		}
	}

	private void convertToUIDateIfDate(final MeasurementData var) {
		if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
				&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}

	private void updateDates(final MeasurementRow originalRow) {
		if (originalRow != null && originalRow.getMeasurementVariables() != null) {
			for (final MeasurementData var : originalRow.getDataList()) {
				this.convertToDBDateIfDate(var);
			}
		}
	}

	private void convertToDBDateIfDate(final MeasurementData var) {
		if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
				&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			var.setValue(DateUtil.convertToDBDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}

}
