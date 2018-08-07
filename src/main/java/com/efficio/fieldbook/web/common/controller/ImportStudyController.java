
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.util.DataMapUtil;
import com.efficio.fieldbook.web.study.ImportStudyServiceFactory;
import com.efficio.fieldbook.web.study.ImportStudyType;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping(ImportStudyController.URL)
public class ImportStudyController extends AbstractBaseFieldbookController {

	public static final String SUCCESS = "success";
	private static final String ERROR = "error";
	private static final String IS_SUCCESS = "isSuccess";
	private static final Logger LOG = LoggerFactory.getLogger(ImportStudyController.class);
	public static final String URL = "/ImportManager";

	public static final int STATUS_ADD_NAME_TO_GID = 1;
	public static final int STATUS_ADD_GERMPLASM_AND_NAME = 2;
	public static final int STATUS_SELECT_GID = 3;
	public static final String CONTAINS_OUT_OF_SYNC_VALUES = "containsOutOfSyncValues";

	@Resource
	private UserSelection studySelection;

	@Resource
	private FileService fileService;

	@Resource
	private ImportStudyServiceFactory studyServiceFactory;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private OntologyService ontologyService;

	/** The message source. */
	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Resource
	private ObjectMapper objectMapper;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/import/{importType}", method = RequestMethod.POST)
	public String importFile(@ModelAttribute("createTrialForm") final CreateTrialForm form,
			@PathVariable final int importType, final BindingResult result,
			final Model model) {

		final ImportResult importResult;
		final UserSelection userSelection = this.getUserSelection();
		final ImportStudyType importStudyType = ImportStudyType.getImportType(importType);

		assert importStudyType != null;

		/**
		 * Should always revert the data first to the original data here we
		 * should move here that part the copies it to the original observation
		 */
		if (this.getUserSelection().getWorkbook().getObservations() != null) {
			this.getUserSelection().getWorkbook().getObservations().clear();
		}
		if (this.getUserSelection().getWorkbook().getOriginalObservations() != null) {
			this.getUserSelection().getWorkbook().getOriginalObservations().clear();
		}
		this.fieldbookMiddlewareService.loadAllObservations(userSelection.getWorkbook());
		WorkbookUtil.resetWorkbookObservations(userSelection.getWorkbook());

		importResult = this.importWorkbookByType(form.getFile(), result, userSelection.getWorkbook(), importStudyType);

		final Locale locale = LocaleContextHolder.getLocale();
		final Map<String, Object> resultsMap = new HashMap<>();
		resultsMap.put("hasDataOverwrite", userSelection.getWorkbook().hasExistingDataOverwrite() ? "1" : "0");
		if (!result.hasErrors()) {
			userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
			form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
			form.changePage(userSelection.getCurrentPage());
			userSelection.setCurrentPage(form.getCurrentPage());
			form.setImportVal(1);
			form.setNumberOfInstances(userSelection.getWorkbook().getTotalNumberOfInstances());
			form.setTrialEnvironmentValues(this.transformTrialObservations(
					userSelection.getWorkbook().getTrialObservations(), userSelection.getTrialLevelVariableList()));
			form.setTrialLevelVariables(userSelection.getTrialLevelVariableList());

			if (importResult.getErrorMessage() != null && !"".equalsIgnoreCase(importResult.getErrorMessage())) {
				resultsMap.put(ImportStudyController.IS_SUCCESS, 0);
				resultsMap.put(ImportStudyController.ERROR, importResult.getErrorMessage());
			} else {
				resultsMap.put(ImportStudyController.IS_SUCCESS, 1);
				resultsMap.put("modes", importResult.getModes());
				this.populateConfirmationMessages(importResult.getChangeDetails());
				resultsMap.put("changeDetails", importResult.getChangeDetails());
				resultsMap.put("errorMessage", importResult.getErrorMessage());
				final String reminderConfirmation = this.messageSource
						.getMessage("confirmation.import.text.modify.measurements", null, locale);
				String addedTraits = " ";
				String deletedTraits = " ";
				if (importResult.getModes() != null && !importResult.getModes().isEmpty()) {
					resultsMap.put("confirmMessageTrais",
							this.messageSource.getMessage("confirmation.import.add.or.delete.traits", null, locale));
					for (final ChangeType mode : importResult.getModes()) {
						if (mode == ChangeType.ADDED_TRAITS) {
							addedTraits = StringUtils.join(importResult.getVariablesAdded(), ", ");

						} else if (mode == ChangeType.DELETED_TRAITS) {
							deletedTraits = StringUtils.join(importResult.getVariablesRemoved(), ", ");
						}
					}
				}
				String plotsNotFoundMessage = " ";
				final Integer plotsNotFound = userSelection.getWorkbook().getPlotsIdNotfound();
				if (plotsNotFound != null && plotsNotFound != 0) {
					plotsNotFoundMessage = plotsNotFound + " "
							+ this.messageSource.getMessage("study.import.warning.plot.id.not.found", null, locale);
				}
				resultsMap.put("addedTraits", addedTraits);
				resultsMap.put("deletedTraits", deletedTraits);
				resultsMap.put("message", reminderConfirmation);
				resultsMap.put("plotsNotFound", plotsNotFoundMessage);
				resultsMap.put("confirmMessage",
						this.messageSource.getMessage("confirmation.import.text.to.proceed", null, locale));
				resultsMap.put("conditionConstantsImportErrorMessage",
						importResult.getConditionsAndConstantsErrorMessage());
			}

		} else {
			resultsMap.put(ImportStudyController.IS_SUCCESS, 0);
			final String errorCode = result.getFieldError("file").getCode();
			try {
				resultsMap.put(ImportStudyController.ERROR, this.messageSource.getMessage(errorCode, null, locale));
			} catch (final NoSuchMessageException e) {
				resultsMap.put(ImportStudyController.ERROR, errorCode);
				ImportStudyController.LOG.error(e.getMessage(), e);
			}
		}

		return super.convertObjectToJson(resultsMap);
	}

	protected ImportResult importWorkbookByType(final MultipartFile file, final BindingResult result,
			final Workbook workbook, final ImportStudyType studyImportType) {
		ImportResult importResult = null;

		this.validateImportFile(file, result, studyImportType);

		if (!result.hasErrors()) {
			try {
				final String filename = this.fileService.saveTemporaryFile(file.getInputStream());
				importResult = this.studyServiceFactory.createStudyImporter(studyImportType, workbook,
						this.fileService.getFilePath(filename), file.getOriginalFilename()).importWorkbook();

			} catch (final WorkbookParserException e) {
				ImportStudyController.LOG.error(e.getMessage(), e);
				result.rejectValue("file", e.getMessage());
			} catch (final IOException e) {
				ImportStudyController.LOG.error(e.getMessage(), e);
			}
		}

		return importResult;
	}

	protected void validateImportFile(final MultipartFile file, final BindingResult result,
			final ImportStudyType importStudyType) {
		if (file == null) {
			result.rejectValue("file", AppConstants.FILE_NOT_FOUND_ERROR.getString());
		} else {
			if (ImportStudyType.IMPORT_KSU_CSV == importStudyType
					|| ImportStudyType.IMPORT_NURSERY_CSV == importStudyType) {
				final boolean isCSVFile = file.getOriginalFilename().contains(".csv");
				if (!isCSVFile) {
					result.rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
				}
			} else if (ImportStudyType.IMPORT_NURSERY_EXCEL == importStudyType
					|| ImportStudyType.IMPORT_KSU_EXCEL == importStudyType) {
				final boolean isExcelFile = file.getOriginalFilename().contains(".xls")
						|| file.getOriginalFilename().contains(".xlsx");
				if (!isExcelFile) {
					result.rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
				}
			}
		}
	}

	private List<List<ValueReference>> transformTrialObservations(final List<MeasurementRow> trialObservations,
			final List<SettingDetail> trialHeaders) {
		final List<List<ValueReference>> list = new ArrayList<>();
		if (trialHeaders != null && !trialHeaders.isEmpty() && trialObservations != null
				&& !trialObservations.isEmpty()) {
			for (final MeasurementRow row : trialObservations) {
				final List<ValueReference> refList = new ArrayList<>();
				for (final SettingDetail header : trialHeaders) {
					for (final MeasurementData data : row.getDataList()) {
						if (data.getMeasurementVariable() != null
								&& data.getMeasurementVariable().getTermId() == header.getVariable().getCvTermId()) {

							refList.add(new ValueReference(data.getMeasurementVariable().getTermId(), data.getValue()));
						}
					}
				}
				list.add(refList);
			}
		}
		return list;
	}

	private UserSelection getUserSelection() {
		return this.studySelection;
	}

	@ResponseBody
	@RequestMapping(value = "/revert/data", method = RequestMethod.GET)
	public Map<String, Object> revertData(@ModelAttribute("createTrialForm") final CreateTrialForm form,
			final Model model) {

		this.doRevertData(form);

		final Map<String, Object> result = new HashMap<>();
		result.put(ImportStudyController.SUCCESS, "1");
		return result;
	}

	private void doRevertData(final CreateTrialForm form) {
		final UserSelection userSelection = this.getUserSelection();
		// we should remove here the newly added traits
		final List<MeasurementVariable> newVariableList = new ArrayList<>();

		newVariableList.addAll(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
		form.setMeasurementVariables(newVariableList);
		final List<MeasurementRow> list = new ArrayList<>();
		if (userSelection.getWorkbook().getOriginalObservations() != null) {
			for (final MeasurementRow row : userSelection.getWorkbook().getOriginalObservations()) {
				list.add(row.copy());
			}
		}
		userSelection.getWorkbook().setObservations(list);
		userSelection.setMeasurementRowList(list);

		WorkbookUtil.revertImportedConditionAndConstantsData(userSelection.getWorkbook());
	}

	// TODO finish review of BMS-4886
	@ResponseBody
	@RequestMapping(value = "/apply/change/details", method = RequestMethod.POST)
	public String applyChangeDetails(@RequestParam(value = "data") final String userResponses) throws FieldbookException {
		final UserSelection userSelection = this.getUserSelection();
		final GermplasmChangeDetail[] responseDetails = this.getResponseDetails(userResponses);
		final List<MeasurementRow> observations = userSelection.getWorkbook().getObservations();
		final Map<String, Map<String, String>> changeMap = new HashMap<>();

		// create data structures that will be used to store values that will
		// eventually be stored into the database
		final List<Name> namesForAdding = new ArrayList<>();
		final List<Pair<Germplasm, Name>> germplasmPairs = new ArrayList<>();
		final Map<Integer, MeasurementRow> entryNumberIndexMap = new HashMap<>();
		int germplasmPairIndex = 0;
		for (final GermplasmChangeDetail responseDetail : responseDetails) {
			// reduce the nesting of the loop by continuing the loop in case
			// expected condition is not satisfied
			if (responseDetail.getIndex() >= observations.size()) {
				continue;
			}

			final MeasurementRow row = observations.get(responseDetail.getIndex());
			final int userId = this.getUserId();
			final MeasurementData desigData = row.getMeasurementData(TermId.DESIG.getId());
			final MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
			final MeasurementData entryNumData = row.getMeasurementData(TermId.ENTRY_NO.getId());

			if (responseDetail.getStatus() == ImportStudyController.STATUS_ADD_NAME_TO_GID) {
				// add germplasm name to gid
				final String gDate = DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(), responseDetail.getImportDate());
				final Integer dateInteger = Integer.valueOf(gDate);
				// instead of directly saving the new name value, store the name
				// into the prepared list
				namesForAdding.add(new Name(null, Integer.valueOf(responseDetail.getOriginalGid()), responseDetail.getNameType(), 0, userId,
					responseDetail.getNewDesig(), responseDetail.getImportLocationId(), dateInteger, 0));
				desigData.setValue(responseDetail.getNewDesig());
				gidData.setValue(responseDetail.getOriginalGid());
			} else if (responseDetail.getStatus() == ImportStudyController.STATUS_ADD_GERMPLASM_AND_NAME) {
				// create new germlasm
				final String gDate = DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(), responseDetail.getImportDate());
				final Integer dateInteger = Integer.valueOf(gDate);
				final Name name = new Name(null, null, responseDetail.getNameType(), 1, userId, responseDetail.getNewDesig(),
					responseDetail.getImportLocationId(), dateInteger, 0);
				final Germplasm germplasm =
					new Germplasm(null, responseDetail.getImportMethodId(), 0, 0, 0, userId, 0, responseDetail.getImportLocationId(),
						dateInteger, name);

				// instead of directly saving into the database, store the
				// germplasm - name pair into a list
				germplasmPairs.add(new ImmutablePair<>(germplasm, name));

				// store the measurement row and the associated index of the
				// entry so that the GID resulting in the database save later on
				// can still be used to properly update the required data
				// structures
				entryNumberIndexMap.put(germplasmPairIndex++, row);

				// update the value of the DESIG measurementdata with the new
				// value
				desigData.setValue(responseDetail.getNewDesig());
			} else if (responseDetail.getStatus() == ImportStudyController.STATUS_SELECT_GID) {
				// choose gids
				desigData.setValue(responseDetail.getNewDesig());
				gidData.setValue(String.valueOf(responseDetail.getSelectedGid()));

			}

			if (responseDetail.getStatus() > 0 && entryNumData != null) {
				final Map<String, String> tempMap = new HashMap<>();
				tempMap.put(Integer.toString(TermId.GID.getId()), gidData.getValue());
				tempMap.put(Integer.toString(TermId.DESIG.getId()), desigData.getValue());
				changeMap.put(entryNumData.getValue(), tempMap);
			}

		}

		// perform the database / transaction managed operations outside of the
		// loop for better performance
		try {
			if (!namesForAdding.isEmpty()) {
				this.fieldbookMiddlewareService.addGermplasmNames(namesForAdding);
			}

			if (!germplasmPairs.isEmpty()) {
				final List<Integer> newGids = this.fieldbookMiddlewareService.addGermplasm(germplasmPairs);

				// update both the maintained change map as well as the GID
				// measurement data with the new GID created from saving to the
				// database

				for (int i = 0; i < newGids.size(); i++) {
					final Integer newGid = newGids.get(i);
					final MeasurementRow row = entryNumberIndexMap.get(i);

					final MeasurementData entryNumData = row.getMeasurementData(TermId.ENTRY_NO.getId());
					final MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());

					gidData.setValue(newGid.toString());
					changeMap.get(entryNumData.getValue()).put(Integer.toString(TermId.GID.getId()), String.valueOf(newGid));

				}
			}
		} catch (final MiddlewareQueryException e) {
			ImportStudyController.LOG.error(e.getMessage(), e);
			throw new FieldbookException(e.getMessage());
		}

		// we need to set the gid and desig for the trial with the same entry
		// number
		for (final MeasurementRow row : observations) {
			final MeasurementData entryNumData = row.getMeasurementData(TermId.ENTRY_NO.getId());
			if (entryNumData != null && changeMap.containsKey(entryNumData.getValue())) {
				final Map<String, String> tempMap = changeMap.get(entryNumData.getValue());
				final MeasurementData desigData = row.getMeasurementData(TermId.DESIG.getId());
				final MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
				desigData.setValue(tempMap.get(Integer.toString(TermId.DESIG.getId())));
				gidData.setValue(tempMap.get(Integer.toString(TermId.GID.getId())));
			}
		}

		return ImportStudyController.SUCCESS;
	}

	private int getUserId() throws FieldbookException {
		try {
			return this.getCurrentIbdbUserId();
		} catch (final MiddlewareQueryException e) {
			ImportStudyController.LOG.error(e.getMessage(), e);
			throw new FieldbookException(e.getMessage());
		}
	}

	private GermplasmChangeDetail[] getResponseDetails(final String userResponses) throws FieldbookException {

		try {
			return this.objectMapper.readValue(userResponses, GermplasmChangeDetail[].class);
		} catch (final IOException e) {
			ImportStudyController.LOG.error(e.getMessage(), e);
			throw new FieldbookException(e.getMessage());
		}
	}

	private void populateConfirmationMessages(final List<GermplasmChangeDetail> details) {
		if (details != null && !details.isEmpty()) {
			for (int index = 0; index < details.size(); index++) {
				final String[] args = new String[] { String.valueOf(index + 1), String.valueOf(details.size()),
						details.get(index).getOriginalDesig(), details.get(index).getTrialInstanceNumber(),
						details.get(index).getEntryNumber(), details.get(index).getPlotNumber() };
				final String message = this.messageSource.getMessage("import.change.desig.confirmation", args,
						LocaleContextHolder.getLocale());
				details.get(index).setMessage(message);
			}
		}
	}

	@ResponseBody
	@RequestMapping(value = "/import/save", method = RequestMethod.POST)
	public Map<String, Object> saveImportedFiles(@ModelAttribute("createTrialForm") final CreateTrialForm form,
			final Model model) throws MiddlewareException {
		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementVariable> traits = WorkbookUtil.getAddedTraitVariables(
				userSelection.getWorkbook().getVariates(), userSelection.getWorkbook().getObservations());
		final Workbook workbook = userSelection.getWorkbook();
		userSelection.getWorkbook().getVariates().addAll(traits);

		this.fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<SettingDetail>(),
				AppConstants.ID_NAME_COMBINATION.getString(), true);

		// will do the cleanup for BM_CODE_VTE here
		SettingsUtil.resetBreedingMethodValueToCode(this.fieldbookMiddlewareService, workbook.getObservations(), false,
				this.ontologyService, this.contextUtil.getCurrentProgramUUID());
		this.fieldbookMiddlewareService.saveMeasurementRows(userSelection.getWorkbook(),
				this.contextUtil.getCurrentProgramUUID(), true);
		SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false,
				this.ontologyService, this.contextUtil.getCurrentProgramUUID());
		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());

		userSelection.getWorkbook().setOriginalObservations(userSelection.getWorkbook().getObservations());
		final List<SettingDetail> newTraits = new ArrayList<>();
		final List<SettingDetail> selectedVariates = new ArrayList<>();
		SettingsUtil.convertWorkbookVariatesToSettingDetails(traits, this.fieldbookMiddlewareService,
				this.fieldbookService, newTraits, selectedVariates);

		form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
		userSelection.getBaselineTraitsList().addAll(newTraits);
		userSelection.setNewTraits(newTraits);

		for (final SettingDetail detail : newTraits) {
			detail.getVariable().setOperation(Operation.UPDATE);
		}
		for (final SettingDetail detail : selectedVariates) {
			detail.getVariable().setOperation(Operation.UPDATE);
		}
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(
				userSelection.getWorkbook().getMeasurementDatesetId(),
				SettingsUtil.buildVariates(userSelection.getWorkbook().getVariates())));

		this.fieldbookService.saveStudyColumnOrdering(userSelection.getWorkbook().getStudyDetails().getId(),
				userSelection.getWorkbook().getStudyDetails().getStudyName(), form.getColumnOrders(),
				userSelection.getWorkbook());
		final Boolean hasOutOfSyncObservations =
			this.fieldbookMiddlewareService.hasOutOfSyncObservations(workbook.getMeasurementDatesetId());

		final Map<String, Object> result = new HashMap<>();
		result.put(ImportStudyController.SUCCESS, "1");
		result.put(ImportStudyController.CONTAINS_OUT_OF_SYNC_VALUES, hasOutOfSyncObservations);
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/import/preview", method = RequestMethod.POST)
	public List<Map<String, Object>> previewImportedFiles(
			@ModelAttribute("createTrialForm") final CreateTrialForm form, final Model model) {
		final UserSelection userSelection = this.getUserSelection();
		final List<MeasurementVariable> traits = WorkbookUtil.getAddedTraitVariables(
				userSelection.getWorkbook().getVariates(), userSelection.getWorkbook().getObservations());

		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
		final List<MeasurementVariable> newVariableList = new ArrayList<>();

		form.setMeasurementVariables(newVariableList);

		newVariableList.addAll(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
		newVariableList.addAll(traits);

		final List<MeasurementRow> tempList = new ArrayList<>();

		if (userSelection.getTemporaryWorkbook() != null && userSelection.getMeasurementRowList() == null) {
			tempList.addAll(userSelection.getTemporaryWorkbook().getObservations());
		} else {
			tempList.addAll(userSelection.getMeasurementRowList());
		}

		form.setMeasurementRowList(tempList);

		final List<Map<String, Object>> masterList = new ArrayList<>();

		final DataMapUtil dataMapUtil = new DataMapUtil();
		for (final MeasurementRow row : tempList) {
			final Map<String, Object> dataMap = dataMapUtil.generateDatatableDataMap(row, "", this.getUserSelection());
			masterList.add(dataMap);
		}

		return masterList;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieve/new/import/variables", method = RequestMethod.GET)
	public Map<String, String> getNewlyImportedTraits() throws IOException {
		final UserSelection userSelection = this.getUserSelection();
		final Map<String, String> map = new HashMap<>();
		final List<SettingDetail> newTraits = userSelection.getNewTraits();
		final List<SettingDetail> selectedVariates = userSelection.getNewSelectionVariates();
		map.put("newTraits", this.objectMapper.writeValueAsString(newTraits));
		map.put("newSelectionVariates", this.objectMapper.writeValueAsString(selectedVariates));
		return map;
	}

	public void setFileService(final FileService fileService) {
		this.fileService = fileService;
	}

	public void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	public void setOntologyService(final OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

}
