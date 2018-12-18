
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.util.DataMapUtil;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.trial.service.ValidationService;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.pojos.dms.ProjectProperty;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/trial/measurements")
public class TrialMeasurementsController extends AbstractBaseFieldbookController {

	private static final String PHENOTYPE_VALUE = "phenotypeValue";

	private static final String EXPERIMENT_ID = "experimentId";

	private static final String PHENOTYPE_ID = "phenotypeId";

	private static final String EDIT_EXPERIMENT_CELL_TEMPLATE = "/Common/updateExperimentCell";

	private static final String OBSERVATIONS_HTML = "TrialManager/observations";

	private static final Logger LOG = LoggerFactory.getLogger(TrialMeasurementsController.class);
	public static final String STATUS = "status";
	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String INDEX = "index";
	static final String SUCCESS = "success";
	private static final String TERM_ID = "termId";
	static final String DATA = "data";
	static final String MISSING_VALUE = "missing";

	@Resource
	private UserSelection userSelection;

	@Resource
	private ValidationService validationService;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private StudyService studyService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private PaginationListSelection paginationListSelection;

	@Resource
	private OntologyService ontologyService;

	private final DataMapUtil dataMapUtil = new DataMapUtil();

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/{studyType}/updateTraits", method = RequestMethod.POST)
	public Map<String, String> updateTraits(@ModelAttribute("createTrialForm") final CreateTrialForm form) {

		final Map<String, String> resultMap = new HashMap<>();

		final Workbook workbook = this.getUserSelection().getWorkbook();

		form.setMeasurementRowList(this.getUserSelection().getMeasurementRowList());
		form.setMeasurementVariables(this.getUserSelection().getWorkbook().getMeasurementDatasetVariables());
		form.setStudyName(workbook.getStudyDetails().getStudyName());

		workbook.setObservations(form.getMeasurementRowList());
		workbook.updateTrialObservationsWithReferenceList(form.getTrialEnvironmentValues());

		try {
			this.validationService.validateObservationValues(workbook);
			this.fieldbookMiddlewareService.saveMeasurementRows(workbook, this.contextUtil.getCurrentProgramUUID(),
				true);
			resultMap.put(TrialMeasurementsController.STATUS, "1");
		} catch (final WorkbookParserException e) {
			TrialMeasurementsController.LOG.error(e.getMessage(), e);
			resultMap.put(TrialMeasurementsController.STATUS, "-1");
			resultMap.put(TrialMeasurementsController.ERROR_MESSAGE, e.getMessage());
		}

		return resultMap;
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	/**
	 * POST call once value has been entered in the table cell and user has
	 * blurred out or hit enter.
	 */
	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/data", method = RequestMethod.POST)
	@Transactional
	public Map<String, Object> updateExperimentCellData(@RequestBody final Map<String, String> data,
		final HttpServletRequest req) {

		final Map<String, Object> map = new HashMap<>();
		Integer phenotypeId = null;
		final Phenotype.ValueStatus status;
		if (StringUtils.isNotBlank(data.get(TrialMeasurementsController.PHENOTYPE_ID))) {
			phenotypeId = Integer.valueOf(data.get(TrialMeasurementsController.PHENOTYPE_ID));
		}
		final int termId = Integer.parseInt(data.get(TrialMeasurementsController.TERM_ID));

		final String value = data.get("value");
		final boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard"));
		final boolean invalidButKeep = "1".equalsIgnoreCase(req.getParameter("invalidButKeep"));

		final int experimentId = Integer.parseInt(data.get(TrialMeasurementsController.EXPERIMENT_ID));
		map.put(TrialMeasurementsController.EXPERIMENT_ID, experimentId);
		map.put(TrialMeasurementsController.PHENOTYPE_ID, phenotypeId != null ? phenotypeId : "");

		if (!isDiscard) {
			Phenotype existingPhenotype = null;
			if (phenotypeId != null) {
				existingPhenotype = this.studyDataManager.getPhenotypeById(phenotypeId);
			}

			final Variable trait = this.ontologyVariableDataManager
					.getVariable(this.contextUtil.getCurrentProgramUUID(), termId, true, false);

			if (!invalidButKeep && !this.validationService.validateObservationValue(trait, value)) {
				map.put(TrialMeasurementsController.SUCCESS, "0");
				map.put(TrialMeasurementsController.ERROR_MESSAGE, "Invalid value.");
				return map;
			}
			final boolean isACalculatedValueBeingEdited = this.isBeingACalculatedValueEdited(trait, existingPhenotype, value);

			if (isACalculatedValueBeingEdited) {
				status = Phenotype.ValueStatus.MANUALLY_EDITED;
			}
			else if (existingPhenotype != null && existingPhenotype.getValueStatus() != null) {
				status = existingPhenotype.getValueStatus();
			}
			else {
				status = null;
			}
			this.studyDataManager.saveOrUpdatePhenotypeValue(experimentId, trait.getId(), value, existingPhenotype,
					trait.getScale().getDataType().getId(), status);
		}
		map.put(TrialMeasurementsController.SUCCESS, "1");

		Map<String, Object> dataMap = new HashMap<>();
		final List<ObservationDto> singleObservation = this.studyService
				.getSingleObservation(this.getUserSelection().getWorkbook().getStudyDetails().getId(), experimentId);
		if (!singleObservation.isEmpty()) {
			dataMap = this.dataMapUtil.generateDatatableDataMap(singleObservation.get(0), new HashMap<String, String>(), this.userSelection,
				this.ontologyVariableDataManager, this.contextUtil.getCurrentProgramUUID());
		}
		map.put(TrialMeasurementsController.DATA, dataMap);
		return map;
	}

	/**
	 * POST call once value has been entered in the table cell and user has
	 * blurred out or hit enter. The Update is made by Index of the record as
	 * stored temprorary in memory without persisting to the DB until User hits
	 * Save.
	 */
	@ResponseBody
	@RequestMapping(value = "/updateByIndex/experiment/cell/data", method = RequestMethod.POST)
	@Transactional
	public Map<String, Object> updateExperimentCellDataByIndex(@RequestBody final Map<String, String> data,
			final HttpServletRequest req) {

		final Map<String, Object> map = new HashMap<>();

		final int termId = Integer.parseInt(data.get(TrialMeasurementsController.TERM_ID));

		if (StringUtils.isNotBlank(data.get(TrialMeasurementsController.INDEX))) {
			final int index = Integer.parseInt(data.get(TrialMeasurementsController.INDEX));
			final String value = data.get("value");
			// for categorical
			final int isNew;
			if (data.get("isNew") != null) {
				isNew = Integer.valueOf(data.get("isNew"));
			} else {
				isNew = 1;
			}
			final boolean isDiscard = "1".equalsIgnoreCase(req.getParameter("isDiscard"));

			map.put(TrialMeasurementsController.INDEX, index);

			final MeasurementRow originalRow = this.userSelection.getMeasurementRowList().get(index);

			try {
				if (!isDiscard) {
					final MeasurementRow copyRow = originalRow.copy();
					final String oldValue = WorkbookUtil.getValueByIdInRow(originalRow.getMeasurementVariables(),termId,originalRow);
					this.copyMeasurementValue(copyRow, originalRow, isNew == 1);
					// we set the data to the copy row
					if (copyRow != null && copyRow.getMeasurementVariables() != null) {
						this.updatePhenotypeValues(copyRow.getDataList(), value, termId, isNew);
					}
					this.validationService.validateObservationValues(this.userSelection.getWorkbook(), copyRow);
					// if there are no error, meaning everything is good, thats
					// the time we copy it to the original
					this.copyMeasurementValue(originalRow, copyRow, isNew == 1);
					this.processVisualStatusForImportedTable(originalRow, oldValue, value, termId);
					this.updateDates(originalRow);
				}
				map.put(TrialMeasurementsController.SUCCESS, "1");
				final DataMapUtil dataMapUtil = new DataMapUtil();
				final Map<String, Object> dataMap = dataMapUtil.generateDatatableDataMap(originalRow, "",
					this.getUserSelection());
				map.put(TrialMeasurementsController.DATA, dataMap);
			} catch (final MiddlewareQueryException e) {
				TrialMeasurementsController.LOG.error(e.getMessage(), e);
				map.put(TrialMeasurementsController.SUCCESS, "0");
				map.put(TrialMeasurementsController.ERROR_MESSAGE, e.getMessage());
			}
		}
		return map;
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

	private void updatePhenotypeValues(final List<MeasurementData> measurementDataList, final String value,
		final int termId, final int isNew) {
		for (final MeasurementData var : measurementDataList) {
			if (var != null && var.getMeasurementVariable().getTermId() == termId) {
				if (this.isBeingACalculatedValueEdited(var.getMeasurementVariable(), var.getValue(), value)){
					var.setValueStatus(Phenotype.ValueStatus.MANUALLY_EDITED);
				}
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
	public Map<String, Object> markExperimentCellDataAsAccepted(@RequestBody final Map<String, String> data,
		final HttpServletRequest req) {

		final Map<String, Object> map = new HashMap<>();

		final int index = Integer.parseInt(data.get(TrialMeasurementsController.INDEX));
		final int termId = Integer.parseInt(data.get(TrialMeasurementsController.TERM_ID));

		map.put(TrialMeasurementsController.INDEX, index);

		final MeasurementRow originalRow = this.userSelection.getMeasurementRowList().get(index);

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

		map.put(TrialMeasurementsController.SUCCESS, "1");
		final DataMapUtil dataMapUtil = new DataMapUtil();
		final Map<String, Object> dataMap = dataMapUtil.generateDatatableDataMap(originalRow, "", this.getUserSelection());
		map.put(TrialMeasurementsController.DATA, dataMap);

		return map;
	}

	private void markNonEmptyVariateValuesAsMissing(final List<MeasurementData> measurementDataList) {
		for (final MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
				&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
					var.setValue(MeasurementData.MISSING_VALUE);
					if (var.getMeasurementVariable().getFormula() != null) {
						var.setValueStatus(Phenotype.ValueStatus.MANUALLY_EDITED);
					}
				}
			} else if (var != null && !StringUtils.isEmpty(var.getValue())
				&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
				|| !var.getMeasurementVariable().getPossibleValues().isEmpty())) {
				var.setAccepted(true);
				if (this.isCategoricalValueOutOfBounds(var.getcValueId(), var.getValue(),
					var.getMeasurementVariable().getPossibleValues())) {
					var.setValue(MeasurementData.MISSING_VALUE);
					var.setCustomCategoricalValue(true);
					if (var.getMeasurementVariable().getFormula() != null) {
						var.setValueStatus(Phenotype.ValueStatus.MANUALLY_EDITED);
					}
				} else {
					var.setCustomCategoricalValue(false);
				}
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/missing/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsMissing() {
		final Map<String, Object> map = new HashMap<>();
		for (final MeasurementRow row : this.userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsMissing(row.getDataList());
			}
		}
		map.put(TrialMeasurementsController.SUCCESS, "1");
		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/update/experiment/cell/accepted/all", method = RequestMethod.GET)
	public Map<String, Object> markAllExperimentDataAsAccepted() {

		final Map<String, Object> map = new HashMap<>();

		for (final MeasurementRow row : this.userSelection.getMeasurementRowList()) {
			if (row != null && row.getMeasurementVariables() != null) {
				this.markNonEmptyVariateValuesAsAccepted(row.getDataList());
			}
		}

		map.put(TrialMeasurementsController.SUCCESS, "1");

		return map;
	}

	private void markNonEmptyVariateValuesAsAccepted(final List<MeasurementData> measurementDataList) {
		for (final MeasurementData var : measurementDataList) {
			if (var != null && !StringUtils.isEmpty(var.getValue())
					&& var.getMeasurementVariable().getDataTypeId() == TermId.NUMERIC_VARIABLE.getId()) {
				if (this.isNumericalValueOutOfBounds(var.getValue(), var.getMeasurementVariable())) {
					var.setAccepted(true);
				}
			} else if (var != null && !StringUtils.isEmpty(var.getValue()) && var.getMeasurementVariable() != null
					&& (var.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()
							|| !(var.getMeasurementVariable().getPossibleValues() != null
									&& var.getMeasurementVariable().getPossibleValues().isEmpty()))) {
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

	/**
	 * GET call on clicking the cell in table for entering measurement value
	 * inline.
	 */
	@RequestMapping(value = "/edit/experiment/cell/{experimentId}/{termId}", method = RequestMethod.GET)
	public String editExperimentCells(@PathVariable final int experimentId, @PathVariable final int termId,
		@RequestParam(required = false) final Integer phenotypeId, final Model model) {

		if (phenotypeId != null) {
			final Phenotype phenotype = this.studyDataManager.getPhenotypeById(phenotypeId);
			model.addAttribute(TrialMeasurementsController.PHENOTYPE_ID, phenotype.getPhenotypeId());
			model.addAttribute(TrialMeasurementsController.PHENOTYPE_VALUE, phenotype.getValue());
		} else {
			model.addAttribute(TrialMeasurementsController.PHENOTYPE_ID, "");
			model.addAttribute(TrialMeasurementsController.PHENOTYPE_VALUE, "");
		}

		final Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
			termId, true, false);

		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());
		model.addAttribute("variable", variable);
		model.addAttribute(TrialMeasurementsController.EXPERIMENT_ID, experimentId);

		model.addAttribute(TrialMeasurementsController.TERM_ID, termId);
		model.addAttribute("possibleValues", this.fieldbookService.getAllPossibleValues(variable));

		return super.showAjaxPage(model, TrialMeasurementsController.EDIT_EXPERIMENT_CELL_TEMPLATE);
	}

	/**
	 * GET call on clicking the cell in table for bulk editing measurement value
	 * inline for import preview measurements table.
	 */
	@RequestMapping(value = "/update/experiment/cell/{index}/{termId}", method = RequestMethod.GET)
	public String editExperimentCells(@PathVariable final int index, @PathVariable final int termId,
			final Model model) {

		final List<MeasurementRow> tempList = new ArrayList<>();
		tempList.addAll(this.userSelection.getMeasurementRowList());

		final MeasurementRow row = tempList.get(index);
		final MeasurementRow copyRow = row.copy();
		this.copyMeasurementValue(copyRow, row);
		MeasurementData editData = null;
		if (copyRow != null && copyRow.getMeasurementVariables() != null) {
			for (final MeasurementData var : copyRow.getDataList()) {
				this.convertToUIDateIfDate(var);
				if (var != null && var.getMeasurementVariable().getTermId() == termId) {
					editData = var;
					break;
				}
			}
		}

		final Variable variable = this.ontologyVariableDataManager.getVariable(this.contextUtil.getCurrentProgramUUID(),
			termId, true, false);
		model.addAttribute("variable", variable);
		model.addAttribute(TrialMeasurementsController.PHENOTYPE_ID, editData.getPhenotypeId());
		model.addAttribute(TrialMeasurementsController.PHENOTYPE_VALUE, editData.getValue());
		model.addAttribute("possibleValues", this.fieldbookService.getAllPossibleValues(variable));
		model.addAttribute("categoricalVarId", TermId.CATEGORICAL_VARIABLE.getId());
		model.addAttribute("dateVarId", TermId.DATE_VARIABLE.getId());
		model.addAttribute("numericVarId", TermId.NUMERIC_VARIABLE.getId());

		this.updateModel(model, editData, index, termId);
		return super.showAjaxPage(model, TrialMeasurementsController.EDIT_EXPERIMENT_CELL_TEMPLATE);
	}

	private void convertToUIDateIfDate(final MeasurementData var) {
		if (var != null && var.getMeasurementVariable() != null && var.getMeasurementVariable().getDataTypeId() != null
			&& var.getMeasurementVariable().getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			var.setValue(DateUtil.convertToUIDateFormat(var.getMeasurementVariable().getDataTypeId(), var.getValue()));
		}
	}

	private void updateModel(final Model model, final MeasurementData measurementData, final int index, final int termId) {
		model.addAttribute("measurementData", measurementData);
		model.addAttribute(TrialMeasurementsController.INDEX, index);
		model.addAttribute(TrialMeasurementsController.TERM_ID, termId);
	}

	/**
	 * This the call to get data required for measurement table in JSON format.
	 * The url is
	 * /plotMeasurements/{studyid}/{instanceid}?pagenumber=1&pagesize=100
	 */
	@ResponseBody
	@RequestMapping(value = "/plotMeasurements/{studyId}/{instanceId}", method = RequestMethod.GET, produces = "application/json")
	@Transactional
	public Map<String, Object> getPlotMeasurementsPaginated(@PathVariable final int studyId,
		@PathVariable final int instanceId, @ModelAttribute("createTrialForm") final CreateTrialForm form,
		final Model model, final HttpServletRequest req) {

		final List<Map<String, Object>> masterDataList = new ArrayList<>();
		final Map<String, Object> masterMap = new HashMap<>();

		// number of records per page
		final Integer pageSize = Integer.parseInt(req.getParameter("pageSize"));
		final Integer pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
		final Integer sortedColumnTermId = Integer.parseInt(req.getParameter("sortBy"));
		final String sortBy = this.ontologyDataManager.getTermById(sortedColumnTermId).getName();
		final String sortOrder = req.getParameter("sortOrder");

		final List<ObservationDto> pageResults = this.studyService.getObservations(studyId, instanceId, pageNumber,
				pageSize, sortBy, sortOrder);
		final Map<String, String> nameToAliasMap = this.createNameToAliasMap(studyId);
		for (final ObservationDto row : pageResults) {
			final Map<String, Object> dataMap = this.dataMapUtil.generateDatatableDataMap(row, nameToAliasMap, this.userSelection,
				this.ontologyVariableDataManager, this.contextUtil.getCurrentProgramUUID());
			masterDataList.add(dataMap);
		}

		final int totalObservationUnits = this.studyService.countTotalObservationUnits(studyId, instanceId);

		// We need to pass back the draw number as an integer value to prevent
		// Cross Site Scripting attacks
		// The draw counter that this object is a response to, we echoing it
		// back for the frontend
		masterMap.put("draw", req.getParameter("draw"));
		masterMap.put("recordsTotal", totalObservationUnits);
		masterMap.put("recordsFiltered", totalObservationUnits);
		masterMap.put("data", masterDataList);

		return masterMap;
	}

	Map<String, String> createNameToAliasMap(final int studyId) {
		final Map<String, String> nameToAliasMap = new HashMap<>();

		final List<MeasurementVariable> measurementDatasetVariables = new ArrayList<>();
		measurementDatasetVariables.addAll(this.userSelection.getWorkbook().getMeasurementDatasetVariablesView());

		final int measurementDatasetId = this.fieldbookMiddlewareService.getMeasurementDatasetId(studyId,
				this.userSelection.getWorkbook().getStudyName());
		final List<ProjectProperty> projectProperties = this.ontologyDataManager
				.getProjectPropertiesByProjectId(measurementDatasetId);

		for (final ProjectProperty projectProperty : projectProperties) {
			final MeasurementVariable mvar = WorkbookUtil.getMeasurementVariable(measurementDatasetVariables,
					projectProperty.getVariableId());
			if (mvar != null) {
				nameToAliasMap.put(this.ontologyDataManager.getTermById(mvar.getTermId()).getName(),
						projectProperty.getAlias());
			}
		}
		return nameToAliasMap;
	}

	@ResponseBody
	@RequestMapping(value = "/plotMeasurements/preview", method = RequestMethod.GET, produces = "application/json")
	public List<Map<String, Object>> getPreviewPlotMeasurements() {

		final List<MeasurementRow> tempList = new ArrayList<>();

		if (this.getUserSelection().getTemporaryWorkbook() != null) {
			tempList.addAll(this.getUserSelection().getTemporaryWorkbook().getObservations());
		} else {
			tempList.addAll(this.getUserSelection().getWorkbook().getObservations());
		}

		final List<Map<String, Object>> masterList = new ArrayList<>();

		final DataMapUtil dataMapUtil = new DataMapUtil();
		for (final MeasurementRow row : tempList) {
			final Map<String, Object> dataMap = dataMapUtil.generateDatatableDataMap(row, "", this.getUserSelection());
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
	 * We maintain the state of categorical description view in session to
	 * support the ff scenario: 1. When user does a browser refresh, the state
	 * of measurements view is maintained 2. When user switches between studies
	 * (either nursery or trial) state is also maintained 3. Generating the
	 * modal for editing whole measurement row/entry is done in the backend (see
	 * updateExperimentModal.html) , this also helps us track which display
	 * values in the cateogrical dropdown is used
	 *
	 * @param showCategoricalDescriptionView
	 * @param session
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/setCategoricalDisplayType", method = RequestMethod.GET)
	public Boolean setCategoricalDisplayType(@RequestParam final Boolean showCategoricalDescriptionView,
		final HttpSession session) {
		Boolean isCategoricalDescriptionView = (Boolean) session.getAttribute("isCategoricalDescriptionView");

		if (null != showCategoricalDescriptionView) {
			isCategoricalDescriptionView = showCategoricalDescriptionView;
		} else {
			isCategoricalDescriptionView ^= Boolean.TRUE;
		}

		session.setAttribute("isCategoricalDescriptionView", isCategoricalDescriptionView);

		return isCategoricalDescriptionView;
	}

	@RequestMapping(value = "/pageView/{pageNum}", method = RequestMethod.GET)
	public String getPaginatedListViewOnly(@PathVariable final int pageNum,
			@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model,
			@RequestParam("listIdentifier") final String datasetId) {

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
		this.getUserSelection().setCurrentPage(form.getCurrentPage());
		return super.showAjaxPage(model, "/TrialManager/datasetSummaryView");
	}

	@RequestMapping(value = "/viewStudyAjax/{datasetId}/{studyId}", method = RequestMethod.GET)
	public String viewStudyAjax(@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model,
			@PathVariable final int datasetId, @PathVariable final int studyId) {
		Workbook workbook = null;
		try {
			workbook = this.fieldbookMiddlewareService.getCompleteDataset(datasetId);
			this.fieldbookService.setAllPossibleValuesInWorkbook(workbook);
			SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false,
				this.ontologyService, this.contextUtil.getCurrentProgramUUID());
		} catch (final MiddlewareException e) {
			TrialMeasurementsController.LOG.error(e.getMessage(), e);
		}
		this.getUserSelection()
				.setMeasurementRowList(workbook.arrangeMeasurementObservation(workbook.getObservations()));
		form.setMeasurementRowList(this.getUserSelection().getMeasurementRowList());
		form.setMeasurementVariables(workbook.getMeasurementDatasetVariables());
		this.changeLocationIdToName(form.getMeasurementRowList(), workbook.getMeasurementDatasetVariablesMap(),
				studyId);
		this.roundNumericValues(form.getMeasurementRowList());
		this.paginationListSelection.addReviewDetailsList(String.valueOf(datasetId), form.getMeasurementRowList());
		this.paginationListSelection.addReviewVariableList(String.valueOf(datasetId), form.getMeasurementVariables());
		form.changePage(1);
		this.getUserSelection().setCurrentPage(form.getCurrentPage());

		return super.showAjaxPage(model, TrialMeasurementsController.OBSERVATIONS_HTML);
	}

	void roundNumericValues(final List<MeasurementRow> measurementRowList) {
	  	for (final MeasurementRow row : measurementRowList) {
	  	  	for (final MeasurementData data : row.getDataList()) {
		  		if (data.getMeasurementVariable().getVariableType() != null && data.getMeasurementVariable().getVariableType().getId().equals(VariableType.TRAIT.getId()) && data.isNumeric() && !StringUtils.isEmpty(data.getValue()) && !TrialMeasurementsController.MISSING_VALUE.equals(data.getValue())) {
		    		final String value = StringUtils.stripEnd(String.format ("%.4f", Double.parseDouble(data.getValue())), "0");
					data.setValue(StringUtils.stripEnd(value, "."));
		  		}
			}
	  	}
	}

	void changeLocationIdToName(final List<MeasurementRow> measurementRowList,
			final Map<String, MeasurementVariable> measurementDatasetVariablesMap, final int studyId) {
		if (measurementDatasetVariablesMap.get(String.valueOf(TermId.LOCATION_ID.getId())) != null) {
			final Map<String, String> locationNameMap = this.studyDataManager
					.createInstanceLocationIdToNameMapFromStudy(studyId);
			for (final MeasurementRow row : measurementRowList) {
				for (final MeasurementData data : row.getDataList()) {
					if (TermId.LOCATION_ID.getId() == data.getMeasurementVariable().getTermId()) {
						data.setValue(locationNameMap.get(data.getValue()));
					}
				}
			}
		}

	}

	protected boolean isNumericalValueOutOfBounds(final String value, final MeasurementVariable var) {
		return var.getMinRange() != null && var.getMaxRange() != null && NumberUtils.isNumber(value)
			&& (Double.valueOf(value) < var.getMinRange() || Double.valueOf(value) > var.getMaxRange());
	}

	protected boolean isCategoricalValueOutOfBounds(final String cValueId, final String value,
		final List<ValueReference> possibleValues) {
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

	protected void copyMeasurementValue(final MeasurementRow origRow, final MeasurementRow valueRow) {
		this.copyMeasurementValue(origRow, valueRow, false);
	}

	protected void copyMeasurementValue(final MeasurementRow origRow, final MeasurementRow valueRow,
		final boolean isNew) {

		for (int index = 0; index < origRow.getDataList().size(); index++) {
			final MeasurementData data = origRow.getDataList().get(index);
			final MeasurementData valueRowData = valueRow.getDataList().get(index);
			// We only need to copy the measurement values of traits since we do
			// not allow
			// editing of factor columns.
			if (!data.getMeasurementVariable().isFactor()) {
				this.copyMeasurementDataValue(data, valueRowData, isNew);
			}
		}
	}

	private void copyMeasurementDataValue(final MeasurementData oldData, final MeasurementData newData,
		final boolean isNew) {
		if (oldData.getMeasurementVariable().getPossibleValues() != null
			&& !oldData.getMeasurementVariable().getPossibleValues().isEmpty()) {
			oldData.setValueStatus(newData.getValueStatus());
			oldData.setAccepted(newData.isAccepted());
			if (!StringUtils.isEmpty(oldData.getValue()) && oldData.isAccepted() && this.isCategoricalValueOutOfBounds(oldData.getcValueId(), oldData.getValue(), oldData.getMeasurementVariable().getPossibleValues())) {
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
			oldData.setValueStatus(newData.getValueStatus());
			oldData.setValue(newData.getValue());
			oldData.setAccepted(newData.isAccepted());
		}
	}

	public boolean isBeingACalculatedValueEdited(final Variable variable, final Phenotype oldPhenotype, final String newValue) {
		String value = null;
		if (oldPhenotype != null && variable.getFormula() != null) {
			if (TermId.CATEGORICAL_VARIABLE.getId() == variable.getScale().getDataType().getId() && oldPhenotype.getcValueId() != null) {
				value = oldPhenotype.getcValueId().toString();
			} else {
				value = oldPhenotype.getValue();
			}
			return !newValue.equals(value);
		}

		return oldPhenotype == null && variable.getFormula() != null;
	}

	public boolean isBeingACalculatedValueEdited(final MeasurementVariable variable, final String oldValue, final String newValue) {
		return ((oldValue == null || !oldValue.equals(newValue)) && variable.getFormula() != null) ? true : false;
	}

	private void processVisualStatusForImportedTable(final MeasurementRow row, final String oldValue, final String newValue, final Integer termId) {
		if (oldValue == null || !oldValue.equals(newValue)) {
			final Map<Integer, List<Integer>> usages = WorkbookUtil.getVariatesUsedInFormulas(this.getUserSelection().getWorkbook().getVariates());
			if (usages.containsKey(termId)) {
				for (final MeasurementData measurementData : row.getDataList()) {
					if (usages.get(termId).contains(measurementData.getMeasurementVariable().getTermId())) {
						measurementData.setValueStatus(Phenotype.ValueStatus.OUT_OF_SYNC);
					}
				}
			}
		}
	}

	void setValidationService(final ValidationService validationService) {
		this.validationService = validationService;
	}

	public void setStudyService(final StudyService studyService) {
		this.studyService = studyService;
	}

	void setOntologyVariableDataManager(final OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}

	void setStudyDataManager(final StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	void setFieldbookService(final com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		this.fieldbookService = fieldbookService;
	}

	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@Override
	public void setPaginationListSelection(final PaginationListSelection paginationListSelection) {
		this.paginationListSelection = paginationListSelection;
	}
}
