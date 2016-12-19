
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
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
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
	private static final Logger LOG = LoggerFactory.getLogger(ObservationMatrixController.class);
	private static final String STATUS = "status";
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

		UserSelection userSelection = this.getUserSelection();
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

		UserSelection userSelection = this.getUserSelection();

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

		UserSelection userSelection = this.getUserSelection();

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

	private UserSelection getUserSelection() {
		return this.userSelection;
	}

	protected void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	/**
	 * POST call once value has been entered in the table cell and user has blurred out or hit enter.
	 */
	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/data", method = RequestMethod.POST)
	@Transactional
	public Map<String, Object> updateExperimentCellData(@RequestBody Map<String, String> data, HttpServletRequest req) {

		Map<String, Object> map = new HashMap<String, Object>();

		int experimentId = Integer.valueOf(data.get("experimentId"));

		Integer phenotypeId = null;
		if (StringUtils.isNotBlank(data.get("phenotypeId"))) {
			phenotypeId = Integer.valueOf(data.get("phenotypeId"));
		}
		int termId = Integer.valueOf(data.get(ObservationMatrixController.TERM_ID));
		String value = data.get("value");
		boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard")) ? true : false;
		map.put("experimentId", experimentId);
		map.put("phenotypeId", phenotypeId != null ? phenotypeId : "");

		if (!isDiscard) {
			Phenotype existingPhenotype = null;
			if (phenotypeId != null) {
				existingPhenotype = this.studyDataManager.getPhenotypeById(phenotypeId);
			}

			Variable trait = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, true, false);

			if (!this.validationService.validateObservationValue(trait, value)) {
				map.put(ObservationMatrixController.SUCCESS, "0");
				map.put(ObservationMatrixController.ERROR_MESSAGE, "Invalid value.");
				return map;
			}

			this.studyDataManager.saveOrUpdatePhenotypeValue(experimentId, trait.getId(), value, existingPhenotype,
					trait.getScale().getDataType().getId());
		}
		map.put(ObservationMatrixController.SUCCESS, "1");

		Map<String, Object> dataMap = new HashMap<String, Object>();
		List<ObservationDto> singleObservation =
				this.studyService.getSingleObservation(this.userSelection.getWorkbook().getStudyDetails().getId(), experimentId);
		if (!singleObservation.isEmpty()) {
			dataMap = generateDatatableDataMap(singleObservation.get(0), "");
		}
		map.put(ObservationMatrixController.DATA, dataMap);
		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/accepted", method = RequestMethod.POST)
	public Map<String, Object> markExperimentCellDataAsAccepted(@RequestBody Map<String, String> data, HttpServletRequest req) {

		Map<String, Object> map = new HashMap<String, Object>();

		int index = Integer.valueOf(data.get(ObservationMatrixController.INDEX));
		int termId = Integer.valueOf(data.get(ObservationMatrixController.TERM_ID));

		map.put(ObservationMatrixController.INDEX, index);

		UserSelection userSelection = this.getUserSelection();
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

		UserSelection userSelection = this.getUserSelection();
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
		UserSelection userSelection = this.getUserSelection();
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
	@RequestMapping(value = "/edit/experiment/cell/{experimentId}/{termId}", method = RequestMethod.GET)
	public String editExperimentCells(@PathVariable int experimentId, @PathVariable int termId,
			@RequestParam(required = false) Integer phenotypeId, Model model) throws MiddlewareQueryException {

		if (phenotypeId != null) {
			Phenotype phenotype = studyDataManager.getPhenotypeById(phenotypeId);
			model.addAttribute("phenotypeId", phenotype.getPhenotypeId());
			model.addAttribute("phenotypeValue", phenotype.getValue());
		} else {
			model.addAttribute("phenotypeId", "");
			model.addAttribute("phenotypeValue", "");
		}

		Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, true, false);

		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());
		model.addAttribute("isNursery", !this.getUserSelection().isTrial());
		model.addAttribute("variable", variable);
		model.addAttribute("experimentId", experimentId);

		model.addAttribute(ObservationMatrixController.TERM_ID, termId);
		model.addAttribute("possibleValues", this.fieldbookService.getAllPossibleValues(variable));

		return super.showAjaxPage(model, ObservationMatrixController.EDIT_EXPERIMENT_CELL_TEMPLATE);
	}

	/**
	 * This the call to get data required for measurement table in JSON format.
	 * The url is /plotMeasurements/{studyid}/{instanceid}?pagenumber=1&pagesize=100
	 */
	@ResponseBody
	@RequestMapping(value = "/plotMeasurements/{studyId}/{instanceId}", method = RequestMethod.GET, produces = "application/json")
	@Transactional
	public Map<String, Object> getPlotMeasurementsPaginated(@PathVariable final int studyId, @PathVariable final int instanceId,
			@ModelAttribute("createNurseryForm") final CreateNurseryForm form,
			final Model model, final HttpServletRequest req) {

		final List<Map<String, Object>> masterDataList = new ArrayList<Map<String, Object>>();
		final Map <String, Object> masterMap = new HashMap<>();

		// number of records per page
		final Integer pageSize = Integer.parseInt(req.getParameter("pageSize"));
		final Integer pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
		final List<ObservationDto> pageResults = this.studyService.getObservations(studyId, instanceId, pageNumber, pageSize);

		for (final ObservationDto row : pageResults) {
			final Map<String, Object> dataMap = this.generateDatatableDataMap(row, "");
			masterDataList.add(dataMap);
		}

		final int totalObservationUnits = this.studyService.countTotalObservationUnits(studyId, instanceId);

		//We need to pass back the draw number as an integer value to prevent Cross Site Scripting attacks
		//The draw counter that this object is a response to, we echoing it back for the frontend
		masterMap.put("draw", req.getParameter("draw"));
		masterMap.put("recordsTotal", totalObservationUnits);
		masterMap.put("recordsFiltered", totalObservationUnits);
		masterMap.put("data", masterDataList);

		return masterMap;
	}

	@ResponseBody
	@RequestMapping(value = "/plotMeasurements/preview", method = RequestMethod.GET, produces = "application/json")
	public List<Map<String, Object>> getPreviewPlotMeasurements() {
		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementRow> tempList = new ArrayList<MeasurementRow>();

		if (userSelection.getTemporaryWorkbook() != null && userSelection.getMeasurementRowList() == null) {
			tempList.addAll(userSelection.getTemporaryWorkbook().getObservations());
		} else {
			tempList.addAll(userSelection.getMeasurementRowList());
		}

		final List<Map<String, Object>> masterList = new ArrayList<Map<String, Object>>();

		for (final MeasurementRow row : tempList) {
			final Map<String, Object> dataMap = this.generateDatatableDataMap(row, "");
			masterList.add(dataMap);
		}

		return masterList;
	}

	@ResponseBody
	@RequestMapping(value = "/instanceMetadata/{studyId}", method = RequestMethod.GET)
	@Transactional
	public List<StudyInstance> getStudyInstanceMetaData(@PathVariable final int studyId) {
		return this.studyService.getStudyInstances(studyId);
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
		// the 3 attributes are needed always
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
		UserSelection userSelection = this.getUserSelection();
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

	private Map<String, Object> generateDatatableDataMap(final ObservationDto row, String suffix) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		// the 3 attributes are needed always
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

				if (StringUtils.isBlank(data.getTriatValue())) {
					dataMap.put(data.getTrait().getTraitName(),
							new Object[] {"", "", false, data.getPhenotypeId() != null ? data.getPhenotypeId() : ""});
				} else {
					String catName = "";
					String catDisplayValue = "";
					for (TermSummary category : measurementVariable.getScale().getCategories()) {
						if (category.getName().equals(data.getTriatValue())) {
							catName = category.getName();
							catDisplayValue = category.getDefinition();
							break;
						}
					}
					dataMap.put(data.getTrait().getTraitName(), new Object[] {catName + suffix, catDisplayValue + suffix, true,
							data.getPhenotypeId() != null ? data.getPhenotypeId() : ""});
				}
			} else if (measurementVariable.getScale().getDataType().equals(DataType.NUMERIC_VARIABLE)) {
				dataMap.put(data.getTrait().getTraitName(), new Object[] {data.getTriatValue() != null ? data.getTriatValue() : "", true,
						data.getPhenotypeId() != null ? data.getPhenotypeId() : ""});
			} else {
				dataMap.put(data.getTrait().getTraitName(), new Object[] {data.getTriatValue() != null ? data.getTriatValue() : "",
						data.getPhenotypeId() != null ? data.getPhenotypeId() : ""});
			}
		}

		dataMap.put(TermId.ENTRY_NO.name(), new Object[] {row.getEntryNo(), false});
		dataMap.put(TermId.ENTRY_TYPE.name(), new Object[] {row.getEntryType(), row.getEntryType(), false});
		dataMap.put(TermId.PLOT_NO.name(), new Object[] {row.getPlotNumber(), false});
		dataMap.put(TermId.REP_NO.name(), new Object[] {row.getRepitionNumber(), false});
		dataMap.put("TRIAL_INSTANCE", new Object[] {row.getTrialInstance(), false});

		// generate measurement row data from newly added traits (no data yet)
		final UserSelection userSelection = this.getUserSelection();
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

	void setValidationService(ValidationService validationService) {
		this.validationService = validationService;
	}

	void setStudyService(StudyService studyService) {
		this.studyService = studyService;
	}

	void setOntologyVariableDataManager(OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}

	void setStudyDataManager(StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	void setFieldbookService(com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}
}
