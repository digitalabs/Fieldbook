
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.CategoricalDisplayValue;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.nursery.service.ValidationService;

@Controller
@RequestMapping(ObservationMatrixController.URL)
public class ObservationMatrixController extends AbstractBaseFieldbookController {

	public static final String URL = "/Common/addOrRemoveTraits";
	public static final String PAGINATION_TEMPLATE = "/Common/showAddOrRemoveTraitsPagination";
	public static final String PAGINATION_TEMPLATE_VIEW_ONLY = "/NurseryManager/showAddOrRemoveTraitsPagination";
	public static final String EDIT_EXPERIMENT_TEMPLATE = "/Common/updateExperimentModal";
	public static final String EDIT_EXPERIMENT_CELL_TEMPLATE = "/Common/updateExperimentCell";
	private static final String TRIAL = "TRIAL";
	private static final Logger LOG = LoggerFactory.getLogger(ObservationMatrixController.class);
	private static final String STATUS = "status";
	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String INDEX = "index";
	private static final String SUCCESS = "success";
	private static final String TERM_ID = "termId";
	private static final String DATA = "data";

	@Resource
	private UserSelection studySelection;

	@Resource
	private ValidationService validationService;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private PaginationListSelection paginationListSelection;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private StudyService studyService;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Override
	public String getContentName() {
		return null;
	}

	/**
	 * Get for the pagination of the list
	 * 
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = "/reload/{studyType}/{pageNum}/{previewPageNum}", method = RequestMethod.GET)
	public String getPaginatedListAfterImport(@PathVariable String studyType, @PathVariable int pageNum, @PathVariable int previewPageNum,
			@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model) {

		boolean isTrial = studyType.equalsIgnoreCase(ObservationMatrixController.TRIAL);
		UserSelection userSelection = this.getUserSelection(isTrial);
		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
		form.setMeasurementRowList(userSelection.getWorkbook().getObservations());
		form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
		form.setStudyName(userSelection.getWorkbook().getStudyDetails().getStudyName());
		form.changePage(pageNum);
		userSelection.setCurrentPage(form.getCurrentPage());
		return super.showAjaxPage(model, ObservationMatrixController.PAGINATION_TEMPLATE);
	}

	@RequestMapping(value = "/pageView/{studyType}/{pageNum}", method = RequestMethod.GET)
	public String getPaginatedListViewOnly(@PathVariable String studyType, @PathVariable int pageNum,
			@ModelAttribute("createNurseryForm") CreateNurseryForm form, Model model, @RequestParam("listIdentifier") String datasetId) {

		boolean isTrial = studyType.equalsIgnoreCase(ObservationMatrixController.TRIAL);
		UserSelection userSelection = this.getUserSelection(isTrial);

		List<MeasurementRow> rows = this.paginationListSelection.getReviewDetailsList(datasetId);
		if (rows != null) {
			form.setMeasurementRowList(rows);
			form.changePage(pageNum);
		}
		List<MeasurementVariable> variables = this.paginationListSelection.getReviewVariableList(datasetId);
		if (variables != null) {
			form.setMeasurementVariables(variables);
		}
		form.changePage(pageNum);
		userSelection.setCurrentPage(form.getCurrentPage());
		return super.showAjaxPage(model, ObservationMatrixController.PAGINATION_TEMPLATE_VIEW_ONLY);
	}

	@ResponseBody
	@RequestMapping(value = "/{studyType}/updateTraits", method = RequestMethod.POST)
	public Map<String, String> updateTraits(@ModelAttribute("createNurseryForm") CreateNurseryForm form, @PathVariable String studyType,
			BindingResult result, Model model) {

		boolean isTrial = studyType.equalsIgnoreCase(ObservationMatrixController.TRIAL);
		UserSelection userSelection = this.getUserSelection(isTrial);

		Map<String, String> resultMap = new HashMap<String, String>();

		Workbook workbook = userSelection.getWorkbook();

		form.setMeasurementRowList(userSelection.getMeasurementRowList());
		form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
		form.setStudyName(workbook.getStudyDetails().getStudyName());

		workbook.setObservations(form.getMeasurementRowList());
		workbook.updateTrialObservationsWithReferenceList(form.getTrialEnvironmentValues());

		try {
			this.validationService.validateObservationValues(workbook, "");
			this.fieldbookMiddlewareService.saveMeasurementRows(workbook, this.contextUtil.getCurrentProgramUUID());
			resultMap.put(ObservationMatrixController.STATUS, "1");
		} catch (WorkbookParserException e) {
			ObservationMatrixController.LOG.error(e.getMessage(), e);
			resultMap.put(ObservationMatrixController.STATUS, "-1");
			resultMap.put(ObservationMatrixController.ERROR_MESSAGE, e.getMessage());
		}

		return resultMap;
	}

	private UserSelection getUserSelection(boolean isTrial) {
		return this.studySelection;
	}

	protected void setStudySelection(UserSelection userSelection) {
		this.studySelection = userSelection;
	}

	/**
	 * This is the GET call to open the action dialog to edit one row.
	 */
	@RequestMapping(value = "/update/experiment/{index}", method = RequestMethod.GET)
	public String editExperimentModal(@PathVariable int index, @ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form,
			Model model) throws MiddlewareQueryException {

		UserSelection userSelection = this.getUserSelection(false);
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		MeasurementRow row = tempList.get(index);
		MeasurementRow copyRow = row.copy();
		this.copyMeasurementValue(copyRow, row);
		if (copyRow != null && copyRow.getMeasurementVariables() != null) {
			for (MeasurementData var : copyRow.getDataList()) {
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
		model.addAttribute("isNursery", userSelection.getWorkbook().isNursery());
		return super.showAjaxPage(model, ObservationMatrixController.EDIT_EXPERIMENT_TEMPLATE);
	}

	/**
	 * POST call once value has been entered in the table cell and user has blurred out or hit enter.
	 */
	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/data", method = RequestMethod.POST)
	public Map<String, Object> updateExperimentCellData(@RequestBody Map<String, String> data, HttpServletRequest req) {

		Map<String, Object> map = new HashMap<String, Object>();

		int index = Integer.valueOf(data.get(ObservationMatrixController.INDEX));
		int termId = Integer.valueOf(data.get(ObservationMatrixController.TERM_ID));
		String value = data.get("value");
		// for categorical
		int isNew = Integer.valueOf(data.get("isNew"));
		boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard")) ? true : false;

		map.put(ObservationMatrixController.INDEX, index);

		UserSelection userSelection = this.getUserSelection(false);
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

		try {
			if (!isDiscard) {
				MeasurementRow copyRow = originalRow.copy();
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
			map.put(ObservationMatrixController.SUCCESS, "1");
			Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
			map.put(ObservationMatrixController.DATA, dataMap);
		} catch (MiddlewareQueryException e) {
			ObservationMatrixController.LOG.error(e.getMessage(), e);
			map.put(ObservationMatrixController.SUCCESS, "0");
			map.put(ObservationMatrixController.ERROR_MESSAGE, e.getMessage());
		}

		return map;
	}

	private void updateDates(MeasurementRow originalRow) {
		if (originalRow != null && originalRow.getMeasurementVariables() != null) {
			for (MeasurementData var : originalRow.getDataList()) {
				this.convertToDBDateIfDate(var);
			}
		}
	}

	private void convertToUIDateIfDate(MeasurementData var) {
		if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
				&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}

	private void convertToDBDateIfDate(MeasurementData var) {
		if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
				&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			var.setValue(DateUtil.convertToDBDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}

	private void updatePhenotypeValues(List<MeasurementData> measurementDataList, String value, int termId, int isNew) {
		for (MeasurementData var : measurementDataList) {
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

	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/accepted", method = RequestMethod.POST)
	public Map<String, Object> markExperimentCellDataAsAccepted(@RequestBody Map<String, String> data, HttpServletRequest req) {

		Map<String, Object> map = new HashMap<String, Object>();

		int index = Integer.valueOf(data.get(ObservationMatrixController.INDEX));
		int termId = Integer.valueOf(data.get(ObservationMatrixController.TERM_ID));

		map.put(ObservationMatrixController.INDEX, index);

		UserSelection userSelection = this.getUserSelection(false);
		MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);

		if (originalRow != null && originalRow.getMeasurementVariables() != null) {
			for (MeasurementData var : originalRow.getDataList()) {
				if (var != null
						&& var.getMeasurementVariable().getTermId() == termId
						&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
								.getMeasurementVariable().getPossibleValues().isEmpty())) {
					var.setAccepted(true);
					if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var.getMeasurementVariable()
							.getPossibleValues())) {
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

		map.put(ObservationMatrixController.SUCCESS, "1");
		Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
		map.put(ObservationMatrixController.DATA, dataMap);

		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/accepted/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsAccepted() {

		Map<String, Object> map = new HashMap<String, Object>();

		UserSelection userSelection = this.getUserSelection(false);
		for (MeasurementRow row : userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsAccepted(row.getDataList());
			}
		}

		map.put(ObservationMatrixController.SUCCESS, "1");

		return map;
	}

	private void markNonEmptyVariateValuesAsAccepted(List<MeasurementData> measurementDataList) {
		for (MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
					&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
				}
			} else if (var != null
					&& !StringUtils.isEmpty(var.getValue())
					&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
							.getMeasurementVariable().getPossibleValues().isEmpty())) {
				var.setAccepted(true);
				if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var.getMeasurementVariable().getPossibleValues())) {
					var.setCustomCategoricalValue(true);
				} else {
					var.setCustomCategoricalValue(false);
				}

			}
		}
	}

	private void markNonEmptyVariateValuesAsMissing(List<MeasurementData> measurementDataList) {
		for (MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
					&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
					var.setValue(MeasurementData.MISSING_VALUE);
				}
			} else if (var != null
					&& !StringUtils.isEmpty(var.getValue())
					&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
							.getMeasurementVariable().getPossibleValues().isEmpty())) {
				var.setAccepted(true);
				if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(), var.getMeasurementVariable().getPossibleValues())) {
					var.setValue(MeasurementData.MISSING_VALUE);
					var.setCustomCategoricalValue(true);
				} else {
					var.setCustomCategoricalValue(false);
				}
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/missing/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsMissing() {
		Map<String, Object> map = new HashMap<String, Object>();
		UserSelection userSelection = this.getUserSelection(false);
		for (MeasurementRow row : userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsMissing(row.getDataList());
			}
		}
		map.put(ObservationMatrixController.SUCCESS, "1");
		return map;
	}

	/**
	 * GET call on clicking the cell in table for entering measurement value inline.
	 */
	@RequestMapping(value = "/update/experiment/cell/{index}/{termId}", method = RequestMethod.GET)
	public String editExperimentCells(@PathVariable int index, @PathVariable int termId, Model model) throws MiddlewareQueryException {

		UserSelection userSelection = this.getUserSelection(false);
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		MeasurementRow row = tempList.get(index);
		MeasurementRow copyRow = row.copy();
		this.copyMeasurementValue(copyRow, row);
		MeasurementData editData = null;
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		if (copyRow != null && copyRow.getMeasurementVariables() != null) {
			for (MeasurementData var : copyRow.getDataList()) {
				this.convertToUIDateIfDate(var);
				if (var != null
						&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() || !var
								.getMeasurementVariable().getPossibleValues().isEmpty())) {
					possibleValues = var.getMeasurementVariable().getPossibleValues();
				}
				if (var != null && var.getMeasurementVariable().getTermId() == termId) {
					editData = var;
					break;
				}
			}
		}
		this.updateModel(model, userSelection.getWorkbook().isNursery(), editData, index, termId, possibleValues);
		return super.showAjaxPage(model, ObservationMatrixController.EDIT_EXPERIMENT_CELL_TEMPLATE);
	}

	private void updateModel(Model model, boolean isNursery, MeasurementData measurementData, int index, int termId,
			List<ValueReference> possibleValues) {
		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());
		model.addAttribute("isNursery", isNursery);
		model.addAttribute("measurementData", measurementData);
		model.addAttribute(ObservationMatrixController.INDEX, index);
		model.addAttribute(ObservationMatrixController.TERM_ID, termId);
		model.addAttribute("possibleValues", possibleValues);
	}

	/**
	 * This the call to get data required for measurement table in JSON format.
	 * The url is /plotMeasurements/{studyid}/{instanceid}?pagenumber=1&pagesize=100
	 */
	@ResponseBody
	@RequestMapping(value = "/plotMeasurements/{studyId}/{instanceId}", method = RequestMethod.GET, produces = "application/json")
	@Transactional
	public Map<String, Object> getPageDataTablesAjax(@PathVariable final int studyId, @PathVariable final int instanceId,
			@ModelAttribute("createNurseryForm") final CreateNurseryForm form,
			final Model model, final HttpServletRequest req) {

		final List<Map<String, Object>> masterDataList = new ArrayList<Map<String, Object>>();
		final Map <String, Object> masterMap = new HashMap<>();

		// number of records per page
		final Integer pageSize = Integer.parseInt(req.getParameter("pageSize"));
		final Integer pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
		final List<ObservationDto> allObservations = this.studyService.getObservations(studyId, instanceId, pageNumber, pageSize);

		for (final ObservationDto row : allObservations) {
			final Map<String, Object> dataMap = this.generateDatatableDataMap(row, "");
			masterDataList.add(dataMap);
		}

		//We need to pass back the draw number as an integer value to prevent Cross Site Scripting attacks
		//The draw counter that this object is a response to, we echoing it back for the frontend
		masterMap.put("draw", req.getParameter("draw"));
		masterMap.put("recordsTotal", allObservations.size());
		masterMap.put("recordsFiltered", allObservations.size());
		masterMap.put("data", masterDataList);

		return masterMap;
	}

	/**
	 * We maintain the state of categorical description view in session to support the ff scenario: 1. When user does a browser refresh, the
	 * state of measurements view is maintained 2. When user switches between studies (either nursery or trial) state is also maintained 3.
	 * Generating the modal for editing whole measurement row/entry is done in the backend (see updateExperimentModal.html) , this also
	 * helps us track which display values in the cateogrical dropdown is used
	 * 
	 * @param showCategoricalDescriptionView
	 * @param session
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/setCategoricalDisplayType", method = RequestMethod.GET)
	public Boolean setCategoricalDisplayType(@RequestParam Boolean showCategoricalDescriptionView, HttpSession session) {
		Boolean isCategoricalDescriptionView = (Boolean) session.getAttribute("isCategoricalDescriptionView");

		if (null != showCategoricalDescriptionView) {
			isCategoricalDescriptionView = showCategoricalDescriptionView;
		} else {
			isCategoricalDescriptionView ^= Boolean.TRUE;
		}

		session.setAttribute("isCategoricalDescriptionView", isCategoricalDescriptionView);

		return isCategoricalDescriptionView;
	}

	/**
	 * This is the POST call to save (in session) the data submitted from the action dialog to edit one row.
	 */
	@ResponseBody
	@RequestMapping(value = "/data/table/ajax/submit/{index}", method = RequestMethod.POST)
	public Map<String, Object> dataTablesAjaxSubmit(@PathVariable int index,
			@ModelAttribute("addOrRemoveTraitsForm") AddOrRemoveTraitsForm form) {
		Map<String, Object> map = new HashMap<String, Object>();
		UserSelection userSelection = this.getUserSelection(false);
		List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();
		tempList.addAll(userSelection.getMeasurementRowList());

		MeasurementRow row = form.getUpdateObservation();
		MeasurementRow originalRow = userSelection.getMeasurementRowList().get(index);
		MeasurementRow copyRow = originalRow.copy();
		this.copyMeasurementValue(copyRow, row);

		try {
			this.validationService.validateObservationValues(userSelection.getWorkbook(), copyRow);
			// if there are no error, meaning everything is good, thats the time we copy it to the original
			this.copyMeasurementValue(originalRow, row);
			this.updateDates(originalRow);
			map.put(ObservationMatrixController.SUCCESS, "1");
			for (MeasurementData data : originalRow.getDataList()) {
				// we set the data accepted automatically to true, if value is out out limit
				if (data.getMeasurementVariable().getDataTypeId().equals(TermId.NUMERIC_VARIABLE.getId())) {
					Double minRange = data.getMeasurementVariable().getMinRange();
					Double maxRange = data.getMeasurementVariable().getMaxRange();
					if (minRange != null && maxRange != null && NumberUtils.isNumber(data.getValue())
							&& (Double.parseDouble(data.getValue()) < minRange || Double.parseDouble(data.getValue()) > maxRange)) {
						data.setAccepted(true);
					}
				}
			}

			Map<String, Object> dataMap = this.generateDatatableDataMap(originalRow, "");
			map.put(ObservationMatrixController.DATA, dataMap);
		} catch (MiddlewareQueryException e) {
			ObservationMatrixController.LOG.error(e.getMessage(), e);
			map.put(ObservationMatrixController.SUCCESS, "0");
			map.put(ObservationMatrixController.ERROR_MESSAGE, e.getMessage());
		}

		return map;
	}

	protected boolean isNumericalValueOutOfBounds(String value, MeasurementVariable var) {
		return var.getMinRange() != null && var.getMaxRange() != null && NumberUtils.isNumber(value)
				&& (Double.valueOf(value) < var.getMinRange() || Double.valueOf(value) > var.getMaxRange());
	}

	protected boolean isCategoricalValueOutOfBounds(String cValueId, String value, List<ValueReference> possibleValues) {
		String val = cValueId;
		if (val == null) {
			val = value;
		}
		for (ValueReference ref : possibleValues) {
			if (ref.getKey().equals(val)) {
				return false;
			}
		}
		return true;
	}

	protected void copyMeasurementValue(MeasurementRow origRow, MeasurementRow valueRow) {
		this.copyMeasurementValue(origRow, valueRow, false);
	}

	protected void copyMeasurementValue(MeasurementRow origRow, MeasurementRow valueRow, boolean isNew) {

		for (int index = 0; index < origRow.getDataList().size(); index++) {
			MeasurementData data = origRow.getDataList().get(index);
			MeasurementData valueRowData = valueRow.getDataList().get(index);
			this.copyMeasurementDataValue(data, valueRowData, isNew);
		}
	}

	private void copyMeasurementDataValue(MeasurementData oldData, MeasurementData newData, boolean isNew) {
		if (oldData.getMeasurementVariable().getPossibleValues() != null && !oldData.getMeasurementVariable().getPossibleValues().isEmpty()) {
			oldData.setAccepted(newData.isAccepted());
			if (!StringUtils.isEmpty(oldData.getValue())
					&& oldData.isAccepted()
					&& this.isCategoricalValueOutOfBounds(oldData.getcValueId(), oldData.getValue(), oldData.getMeasurementVariable()
							.getPossibleValues())) {
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

	private Map<String, Object> generateDatatableDataMap(MeasurementRow row, String suffix) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 4 attributes are needed always
		dataMap.put("Action", Integer.toString(row.getExperimentId()));
		dataMap.put("experimentId", Integer.toString(row.getExperimentId()));
		dataMap.put("GID", row.getMeasurementDataValue(TermId.GID.getId()));
		dataMap.put("DESIGNATION", row.getMeasurementDataValue(TermId.DESIG.getId()));

		// initialize suffix as empty string if its null
		suffix = null == suffix ? "" : suffix;

		// generate measurement row data from dataList (existing / generated data)
		for (MeasurementData data : row.getDataList()) {
			if (data.isCategorical()) {
				CategoricalDisplayValue categoricalDisplayValue = data.getDisplayValueForCategoricalData();

				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {categoricalDisplayValue.getName() + suffix,
						categoricalDisplayValue.getDescription() + suffix, data.isAccepted()});

			} else if (data.isNumeric()) {
				dataMap.put(data.getMeasurementVariable().getName(), new Object[] {data.getDisplayValue() + suffix, data.isAccepted()});
			} else {
				dataMap.put(data.getMeasurementVariable().getName(), data.getDisplayValue());
			}
		}

		// generate measurement row data from newly added traits (no data yet)
		UserSelection userSelection = this.getUserSelection(false);
		if (userSelection != null && userSelection.getMeasurementDatasetVariable() != null
				&& !userSelection.getMeasurementDatasetVariable().isEmpty()) {
			for (MeasurementVariable var : userSelection.getMeasurementDatasetVariable()) {
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

	private Map<String, Object> generateDatatableDataMap(ObservationDto row, String suffix) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 4 attributes are needed always
		dataMap.put("Action", Integer.toString(row.getMeasurementId()));
		dataMap.put("experimentId", Integer.toString(row.getMeasurementId()));
		dataMap.put("GID", row.getGid());
		dataMap.put("DESIGNATION", row.getDesignation());

		// initialize suffix as empty string if its null
		suffix = null == suffix ? "" : suffix;

		// generate measurement row data from dataList (existing / generated data)
		for (MeasurementDto data : row.getTraitMeasurements()) {

			Variable measurementVariable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
					data.getTrait().getTraitId(), true, false);

			if (measurementVariable.getScale().getDataType().equals(DataType.CATEGORICAL_VARIABLE)) {
				for (TermSummary category : measurementVariable.getScale().getCategories()) {
					if (category.getName().equals(data.getTriatValue())) {
						dataMap.put(data.getTrait().getTraitName(),
								new Object[] {category.getName() + suffix, category.getDefinition() + suffix, true});
						break;
					}
				}
			} else if (measurementVariable.getScale().getDataType().equals(DataType.NUMERIC_VARIABLE)) {
				dataMap.put(data.getTrait().getTraitName(), new Object[] {data.getTriatValue() != null ? data.getTriatValue() : "", true});
			} else {
				dataMap.put(data.getTrait().getTraitName(), data.getTriatValue() != null ? data.getTriatValue() : "");
			}
		}

		dataMap.put(TermId.ENTRY_NO.name(), new Object[] {row.getEntryNo(), false});
		dataMap.put(TermId.ENTRY_TYPE.name(), new Object[] {row.getEntryType(), row.getEntryType(), false});
		dataMap.put(TermId.PLOT_NO.name(), new Object[] {row.getPlotNumber(), false});
		dataMap.put(TermId.REP_NO.name(), new Object[] {row.getRepitionNumber(), false});
		dataMap.put("TRIAL_INSTANCE", new Object[] {row.getTrialInstance(), false});

		// generate measurement row data from newly added traits (no data yet)
		UserSelection userSelection = this.getUserSelection(false);
		if (userSelection != null && userSelection.getMeasurementDatasetVariable() != null
				&& !userSelection.getMeasurementDatasetVariable().isEmpty()) {
			for (MeasurementVariable var : userSelection.getMeasurementDatasetVariable()) {
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

	public void setValidationService(ValidationService validationService) {
		this.validationService = validationService;
	}

}
