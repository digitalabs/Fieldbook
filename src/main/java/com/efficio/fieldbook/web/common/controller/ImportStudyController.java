
package com.efficio.fieldbook.web.common.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Resource;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.ChangeType;
import com.efficio.fieldbook.web.common.bean.GermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.ImportResult;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;
import com.efficio.fieldbook.web.common.service.DataKaptureImportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.common.service.KsuCsvImportStudyService;
import com.efficio.fieldbook.web.common.service.KsuExcelImportStudyService;
import com.efficio.fieldbook.web.nursery.form.CreateNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Controller
@RequestMapping(ImportStudyController.URL)
public class ImportStudyController extends AbstractBaseFieldbookController {

	private static final String ERROR = "error";
	private static final String IS_SUCCESS = "isSuccess";
	private static final Logger LOG = LoggerFactory.getLogger(ImportStudyController.class);
	public static final String URL = "/ImportManager";
	private static final String ADD_OR_REMOVE_TRAITS_HTML = "/NurseryManager/addOrRemoveTraits";

	public static final int STATUS_ADD_NAME_TO_GID = 1;
	public static final int STATUS_ADD_GERMPLASM_AND_NAME = 2;
	public static final int STATUS_SELECT_GID = 3;

	@Resource
	private UserSelection studySelection;

	@Resource
	private FileService fileService;

	@Resource
	private FieldroidImportStudyService fieldroidImportStudyService;

	@Autowired
	@Qualifier("excelImportStudyService")
	private ExcelImportStudyService excelImportStudyService;

	@Resource
	private DataKaptureImportStudyService dataKaptureImportStudyService;

	@Autowired
	@Qualifier("ksuExcelImportStudyService")
	private KsuExcelImportStudyService ksuExcelImportStudyService;

	@Resource
	private KsuCsvImportStudyService ksuCsvImportStudyService;

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
	@RequestMapping(value = "/import/{studyType}/{importType}", method = RequestMethod.POST)
	public String importFile(@ModelAttribute("addOrRemoveTraitsForm") final AddOrRemoveTraitsForm form,
			@PathVariable final String studyType, @PathVariable final int importType, final BindingResult result, final Model model) {

		final boolean isTrial = "TRIAL".equalsIgnoreCase(studyType);
		ImportResult importResult = null;
		final UserSelection userSelection = this.getUserSelection(isTrial);

		/**
		 * Should always revert the data first to the original data here we should move here that part the copies it to the original
		 * observation
		 */
		WorkbookUtil.resetWorkbookObservations(userSelection.getWorkbook());

		importResult = this.importWorkbookByType(form.getFile(), result, userSelection.getWorkbook(), importType);

		final Locale locale = LocaleContextHolder.getLocale();
		final Map<String, Object> resultsMap = new HashMap<String, Object>();
		resultsMap.put("hasDataOverwrite", userSelection.getWorkbook().hasExistingDataOverwrite() ? "1" : "0");
		if (!result.hasErrors()) {
			userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
			form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
			form.changePage(userSelection.getCurrentPage());
			userSelection.setCurrentPage(form.getCurrentPage());
			form.setImportVal(1);
			form.setNumberOfInstances(userSelection.getWorkbook().getTotalNumberOfInstances());
			form.setTrialEnvironmentValues(this.transformTrialObservations(userSelection.getWorkbook().getTrialObservations(),
					userSelection.getTrialLevelVariableList()));
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
				final List<String> detailErrorMessage = new ArrayList<String>();
				String reminderConfirmation = "";
				if (importResult.getModes() != null && !importResult.getModes().isEmpty()) {
					for (final ChangeType mode : importResult.getModes()) {
						String message = this.messageSource.getMessage(mode.getMessageCode(), null, locale);
						if (mode == ChangeType.ADDED_TRAITS) {
							message +=
									StringUtils.join(WorkbookUtil.getAddedTraits(userSelection.getWorkbook().getVariates(), userSelection
											.getWorkbook().getObservations()), ", ");
						}
						detailErrorMessage.add(message);
					}
					reminderConfirmation = this.messageSource.getMessage("confirmation.import.text", null, locale);
				}
				resultsMap.put("message", reminderConfirmation);
				resultsMap.put("confirmMessage", this.messageSource.getMessage("confirmation.import.text.to.proceed", null, locale));
				resultsMap.put("detailErrorMessage", detailErrorMessage);
				resultsMap.put("conditionConstantsImportErrorMessage", importResult.getConditionsAndConstantsErrorMessage());
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

	protected ImportResult importWorkbookByType(final MultipartFile file, final BindingResult result, final Workbook workbook,
			final Integer importType) {
		ImportResult importResult = null;

		this.validateImportFile(file, result, importType);

		if (!result.hasErrors()) {
			try {
				final String filename = this.fileService.saveTemporaryFile(file.getInputStream());
				if (AppConstants.IMPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == importType) {
					importResult =
							this.fieldroidImportStudyService.importWorkbook(workbook, this.fileService.getFilePath(filename),
									this.ontologyService, this.fieldbookMiddlewareService);
				} else if (AppConstants.IMPORT_NURSERY_EXCEL.getInt() == importType) {
					importResult =
							this.excelImportStudyService.importWorkbook(workbook, this.fileService.getFilePath(filename),
									this.ontologyService, this.fieldbookMiddlewareService);
				} else if (AppConstants.IMPORT_DATAKAPTURE.getInt() == importType) {
					importResult =
							this.dataKaptureImportStudyService.importWorkbook(workbook, this.fileService.getFilePath(filename),
									this.ontologyService, this.fieldbookMiddlewareService);
				} else if (AppConstants.IMPORT_KSU_EXCEL.getInt() == importType) {
					importResult =
							this.ksuExcelImportStudyService.importWorkbook(workbook, this.fileService.getFilePath(filename),
									this.ontologyService, this.fieldbookMiddlewareService);
				} else if (AppConstants.IMPORT_KSU_CSV.getInt() == importType) {
					importResult =
							this.ksuCsvImportStudyService.importWorkbook(workbook, this.fileService.getFilePath(filename),
									file.getOriginalFilename());
				}

			} catch (final WorkbookParserException e) {
				ImportStudyController.LOG.error(e.getMessage(), e);
				result.rejectValue("file", e.getMessage());
			} catch (final IOException e) {
				ImportStudyController.LOG.error(e.getMessage(), e);
			}
		}

		return importResult;
	}

	protected void validateImportFile(final MultipartFile file, final BindingResult result, final Integer importType) {
		if (file == null) {
			result.rejectValue("file", AppConstants.FILE_NOT_FOUND_ERROR.getString());
		} else {
			if (AppConstants.IMPORT_NURSERY_FIELDLOG_FIELDROID.getInt() == importType
					|| AppConstants.IMPORT_DATAKAPTURE.getInt() == importType || AppConstants.IMPORT_KSU_CSV.getInt() == importType) {
				final boolean isCSVFile = file.getOriginalFilename().contains(".csv");
				if (!isCSVFile) {
					result.rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
				}
			} else if (AppConstants.IMPORT_NURSERY_EXCEL.getInt() == importType || AppConstants.IMPORT_KSU_EXCEL.getInt() == importType) {
				final boolean isExcelFile = file.getOriginalFilename().contains(".xls") || file.getOriginalFilename().contains(".xlsx");
				if (!isExcelFile) {
					result.rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
				}
			}
		}
	}

	private List<List<ValueReference>> transformTrialObservations(final List<MeasurementRow> trialObservations,
			final List<SettingDetail> trialHeaders) {
		final List<List<ValueReference>> list = new ArrayList<List<ValueReference>>();
		if (trialHeaders != null && !trialHeaders.isEmpty() && trialObservations != null && !trialObservations.isEmpty()) {
			for (final MeasurementRow row : trialObservations) {
				final List<ValueReference> refList = new ArrayList<ValueReference>();
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

	private UserSelection getUserSelection(final boolean isTrial) {
		return this.studySelection;
	}

	public String show(final Model model, final boolean isTrial) {
		this.setupModelInfo(model);
		model.addAttribute(AbstractBaseFieldbookController.TEMPLATE_NAME_ATTRIBUTE, this.getContentName(isTrial));
		return AbstractBaseFieldbookController.BASE_TEMPLATE_NAME;
	}

	private String getContentName(final boolean isTrial) {
		return isTrial ? "TrialManager/openTrial" : "NurseryManager/addOrRemoveTraits";
	}

	@RequestMapping(value = "/revert/data", method = RequestMethod.GET)
	public String revertData(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model) {

		this.doRevertData(form);

		return super.showAjaxPage(model, ImportStudyController.ADD_OR_REMOVE_TRAITS_HTML);
	}

	private void doRevertData(final CreateNurseryForm form) {
		final UserSelection userSelection = this.getUserSelection(false);
		// we should remove here the newly added traits
		final List<MeasurementVariable> newVariableList = new ArrayList<MeasurementVariable>();

		newVariableList.addAll(userSelection.getWorkbook().isNursery() ? userSelection.getWorkbook().getMeasurementDatasetVariables()
				: userSelection.getWorkbook().getMeasurementDatasetVariablesView());
		form.setMeasurementVariables(newVariableList);
		final List<MeasurementRow> list = new ArrayList<MeasurementRow>();
		if (userSelection.getWorkbook().getOriginalObservations() != null) {
			for (final MeasurementRow row : userSelection.getWorkbook().getOriginalObservations()) {
				list.add(row.copy());
			}
		}
		userSelection.getWorkbook().setObservations(list);
		userSelection.setMeasurementRowList(list);

		WorkbookUtil.revertImportedConditionAndConstantsData(userSelection.getWorkbook());
	}

	@ResponseBody
	@RequestMapping(value = "/apply/change/details", method = RequestMethod.POST)
	public String applyChangeDetails(@RequestParam(value = "data") final String userResponses) throws FieldbookException {
		final UserSelection userSelection = this.getUserSelection(false);
		final GermplasmChangeDetail[] responseDetails = this.getResponseDetails(userResponses);
		final List<MeasurementRow> observations = userSelection.getWorkbook().getObservations();
		final Map<String, Map<String, String>> changeMap = new HashMap<>();

		// create data structures that will be used to store values that will eventually be stored into the database
		final List<Name> namesForAdding = new ArrayList<>();
		final List<Pair<Germplasm, Name>> germplasmPairs = new ArrayList<>();
		final Map<Integer, MeasurementRow> entryNumberIndexMap = new HashMap<>();
		int germplasmPairIndex = 0;
		for (final GermplasmChangeDetail responseDetail : responseDetails) {
			// reduce the nesting of the loop by continuing the loop in case expected condition is not satisfied
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
				// instead of directly saving the new name value, store the name into the prepared list
				namesForAdding.add(new Name(null, Integer.valueOf(responseDetail.getOriginalGid()), responseDetail.getNameType(), 0,
						userId, responseDetail.getNewDesig(), responseDetail.getImportLocationId(), dateInteger, 0));
				desigData.setValue(responseDetail.getNewDesig());
				gidData.setValue(responseDetail.getOriginalGid());
			} else if (responseDetail.getStatus() == ImportStudyController.STATUS_ADD_GERMPLASM_AND_NAME) {
				// create new germlasm
				final String gDate = DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(), responseDetail.getImportDate());
				final Integer dateInteger = Integer.valueOf(gDate);
				final Name name =
						new Name(null, null, responseDetail.getNameType(), 1, userId, responseDetail.getNewDesig(),
								responseDetail.getImportLocationId(), dateInteger, 0);
				final Germplasm germplasm =
						new Germplasm(null, responseDetail.getImportMethodId(), 0, 0, 0, userId, 0, responseDetail.getImportLocationId(),
								dateInteger, name);

				// instead of directly saving into the database, store the germplasm - name pair into a list
				germplasmPairs.add(new ImmutablePair<Germplasm, Name>(germplasm, name));

				// store the measurement row and the associated index of the entry so that the GID resulting in the database save later on
				// can still be used to properly update the required data structures
				entryNumberIndexMap.put(germplasmPairIndex++, row);

				// update the value of the DESIG measurementdata with the new value
				desigData.setValue(responseDetail.getNewDesig());
			} else if (responseDetail.getStatus() == ImportStudyController.STATUS_SELECT_GID) {
				// choose gids
				desigData.setValue(responseDetail.getNewDesig());
				gidData.setValue(String.valueOf(responseDetail.getSelectedGid()));

			}

			if (responseDetail.getStatus() > 0 && entryNumData != null && entryNumData.getValue() != null) {
				final Map<String, String> tempMap = new HashMap<>();
				tempMap.put(Integer.toString(TermId.GID.getId()), gidData.getValue());
				tempMap.put(Integer.toString(TermId.DESIG.getId()), desigData.getValue());
				changeMap.put(entryNumData.getValue(), tempMap);
			}

		}

		// perform the database / transaction managed operations outside of the loop for better performance
		try {
			if (!namesForAdding.isEmpty()) {
				this.fieldbookMiddlewareService.addGermplasmNames(namesForAdding);
			}

			if (!germplasmPairs.isEmpty()) {
				final List<Integer> newGids = this.fieldbookMiddlewareService.addGermplasm(germplasmPairs);

				// update both the maintained change map as well as the GID measurement data with the new GID created from saving to the
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

		// we need to set the gid and desig for the trial with the same entry number
		if (!userSelection.getWorkbook().isNursery()) {
			for (final MeasurementRow row : observations) {
				final MeasurementData entryNumData = row.getMeasurementData(TermId.ENTRY_NO.getId());
				if (entryNumData != null && entryNumData.getValue() != null && changeMap.containsKey(entryNumData.getValue())) {
					final Map<String, String> tempMap = changeMap.get(entryNumData.getValue());
					final MeasurementData desigData = row.getMeasurementData(TermId.DESIG.getId());
					final MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
					desigData.setValue(tempMap.get(Integer.toString(TermId.DESIG.getId())));
					gidData.setValue(tempMap.get(Integer.toString(TermId.GID.getId())));
				}
			}
		}

		return "success";
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
		} catch (final JsonParseException e) {
			ImportStudyController.LOG.error(e.getMessage(), e);
			throw new FieldbookException(e.getMessage());
		} catch (final JsonMappingException e) {
			ImportStudyController.LOG.error(e.getMessage(), e);
			throw new FieldbookException(e.getMessage());
		} catch (final IOException e) {
			ImportStudyController.LOG.error(e.getMessage(), e);
			throw new FieldbookException(e.getMessage());
		}
	}

	private void populateConfirmationMessages(final List<GermplasmChangeDetail> details) {
		if (details != null && !details.isEmpty()) {
			for (int index = 0; index < details.size(); index++) {
				final String[] args =
						new String[] {String.valueOf(index + 1), String.valueOf(details.size()), details.get(index).getOriginalDesig(),
								details.get(index).getTrialInstanceNumber(), details.get(index).getEntryNumber(),
								details.get(index).getPlotNumber()};
				final String message =
						this.messageSource.getMessage("import.change.desig.confirmation", args, LocaleContextHolder.getLocale());
				details.get(index).setMessage(message);
			}
		}
	}

	@RequestMapping(value = "/import/save", method = RequestMethod.POST)
	public String saveImportedFiles(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model)
			throws MiddlewareException {
		final UserSelection userSelection = this.getUserSelection(false);
		final List<MeasurementVariable> traits =
				WorkbookUtil.getAddedTraitVariables(userSelection.getWorkbook().getVariates(), userSelection.getWorkbook()
						.getObservations());
		final Workbook workbook = userSelection.getWorkbook();
		userSelection.getWorkbook().getVariates().addAll(traits);

		this.fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<SettingDetail>(),
				AppConstants.ID_NAME_COMBINATION.getString(), true);

		// will do the cleanup for BM_CODE_VTE here
		SettingsUtil.resetBreedingMethodValueToCode(this.fieldbookMiddlewareService, workbook.getObservations(), false,
				this.ontologyService, contextUtil.getCurrentProgramUUID());
		this.fieldbookMiddlewareService.saveMeasurementRows(userSelection.getWorkbook(), this.contextUtil.getCurrentProgramUUID());
		SettingsUtil.resetBreedingMethodValueToId(this.fieldbookMiddlewareService, workbook.getObservations(), false, this.ontologyService, contextUtil.getCurrentProgramUUID());
		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());

		userSelection.getWorkbook().setOriginalObservations(userSelection.getWorkbook().getObservations());
		final List<SettingDetail> newTraits = new ArrayList<SettingDetail>();
		final List<SettingDetail> selectedVariates = new ArrayList<SettingDetail>();
		SettingsUtil.convertWorkbookVariatesToSettingDetails(traits, this.fieldbookMiddlewareService, this.fieldbookService, newTraits,
				selectedVariates);

		if (workbook.isNursery()) {
			userSelection.getSelectionVariates().addAll(selectedVariates);
			userSelection.setNewSelectionVariates(selectedVariates);
			form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariables());
		} else {
			form.setMeasurementVariables(userSelection.getWorkbook().getMeasurementDatasetVariablesView());
		}
		userSelection.getBaselineTraitsList().addAll(newTraits);
		userSelection.setNewTraits(newTraits);

		for (final SettingDetail detail : newTraits) {
			detail.getVariable().setOperation(Operation.UPDATE);
		}
		for (final SettingDetail detail : selectedVariates) {
			detail.getVariable().setOperation(Operation.UPDATE);
		}
		form.setMeasurementDataExisting(this.fieldbookMiddlewareService.checkIfStudyHasMeasurementData(userSelection.getWorkbook()
				.getMeasurementDatesetId(), SettingsUtil.buildVariates(userSelection.getWorkbook().getVariates())));

		this.fieldbookService.saveStudyColumnOrdering(userSelection.getWorkbook().getStudyDetails().getId(), userSelection.getWorkbook()
				.getStudyDetails().getStudyName(), form.getColumnOrders(), userSelection.getWorkbook());

		return super.showAjaxPage(model, ImportStudyController.ADD_OR_REMOVE_TRAITS_HTML);
	}

	@RequestMapping(value = "/import/preview", method = RequestMethod.POST)
	public String previewImportedFiles(@ModelAttribute("createNurseryForm") final CreateNurseryForm form, final Model model) {
		final UserSelection userSelection = this.getUserSelection(false);
		final List<MeasurementVariable> traits =
				WorkbookUtil.getAddedTraitVariables(userSelection.getWorkbook().getVariates(), userSelection.getWorkbook()
						.getObservations());

		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
		final List<MeasurementVariable> newVariableList = new ArrayList<MeasurementVariable>();

		form.setMeasurementVariables(newVariableList);

		newVariableList.addAll(userSelection.getWorkbook().isNursery() ? userSelection.getWorkbook().getMeasurementDatasetVariables()
				: userSelection.getWorkbook().getMeasurementDatasetVariablesView());
		newVariableList.addAll(traits);
		return super.showAjaxPage(model, ImportStudyController.ADD_OR_REMOVE_TRAITS_HTML);
	}

	@ResponseBody
	@RequestMapping(value = "/retrieve/new/import/variables", method = RequestMethod.GET)
	public Map<String, String> getNewlyImportedTraits() throws IOException {
		final UserSelection userSelection = this.getUserSelection(false);
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

	public void setFieldroidImportStudyService(final FieldroidImportStudyService fieldroidImportStudyService) {
		this.fieldroidImportStudyService = fieldroidImportStudyService;
	}

	public void setExcelImportStudyService(final ExcelImportStudyService excelImportStudyService) {
		this.excelImportStudyService = excelImportStudyService;
	}

	public void setDataKaptureImportStudyService(final DataKaptureImportStudyService dataKaptureImportStudyService) {
		this.dataKaptureImportStudyService = dataKaptureImportStudyService;
	}

	public void setKsuExcelImportStudyService(final KsuExcelImportStudyService ksuExcelImportStudyService) {
		this.ksuExcelImportStudyService = ksuExcelImportStudyService;
	}

	public void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	public void setOntologyService(final OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

}
