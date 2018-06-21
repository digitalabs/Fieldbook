package com.efficio.etl.web.controller.angular;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.AbstractBaseETLController;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.bean.VariableDTO;
import com.efficio.etl.web.validators.FileUploadFormValidator;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Controller
@RequestMapping(AngularMapOntologyController.URL)
public class AngularMapOntologyController extends AbstractBaseETLController {

	public static final String URL = "/etl/workbook/mapOntology";
	private static final Logger LOG = LoggerFactory.getLogger(AngularMapOntologyController.class);
	public static final String ERROR_HEADER_NO_MAPPING = "error.header.no.mapping";
	public static final String ERROR_DUPLICATE_LOCAL_VARIABLE = "error.duplicate.local.variable";
	public static final String ERROR_LOCATION_ID_DOESNT_EXISTS = "error.location.id.doesnt.exists";

	@Resource
	private FieldbookService fieldbookService;

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Resource
	private ETLService etlService;

	@Resource
	private DataImportService dataImportService;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public String getContentName() {
		return "etl/angular/angularMapHeaders";
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@Resource
	protected WorkbenchService workbenchService;

	@RequestMapping(method = RequestMethod.GET)
	public String show(final Model model, final HttpServletRequest request) {

		try {
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			final List<String> headers = this.etlService.retrieveColumnHeaders(workbook, this.userSelection, Boolean.FALSE);

			final Map<PhenotypicType, List<VariableDTO>> headerMap =
					this.etlService.prepareInitialCategorization(headers, this.userSelection);

			// null key is used to store variabledtos not initially mapped to
			// any standard variable (and thus, any phenotypic type)
			model.addAttribute("headerList", headerMap.get(null));
			model.addAttribute("trialEnvironmentList", headerMap.get(PhenotypicType.TRIAL_ENVIRONMENT));
			model.addAttribute("trialDesignList", headerMap.get(PhenotypicType.TRIAL_DESIGN));
			model.addAttribute("variateList", headerMap.get(PhenotypicType.VARIATE));
			model.addAttribute("germplasmList", headerMap.get(PhenotypicType.GERMPLASM));
			model.addAttribute("datasetType", this.userSelection.getDatasetType());

			model.addAttribute("fieldbookWebLink",
					WorkbenchAppPathResolver.getFullWebAddress(this.etlService.getFieldbookWebTool().getPath()));

			final Map<String, List<Integer>> tmp = new HashMap<>();
			for (final PhenotypicType type : PhenotypicType.values()) {
				tmp.put(type.toString(), type.getTypeStorages());
			}
			model.addAttribute("roleList", tmp);

			// total header count is populated as a function of get header list
			model.addAttribute("totalHeaderCount", headers.size());

			final int allocatedCount = this.getTotalAllocatedCount(headerMap);
			model.addAttribute("allocatedCount", allocatedCount);
		} catch (final IOException e) {
			AngularMapOntologyController.LOG.error(e.getMessage(), e);
		}

		return super.show(model, true, request);
	}

	public int getTotalAllocatedCount(final Map<PhenotypicType, List<VariableDTO>> headerMap) {
		int allocatedCount = 0;

		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.TRIAL_ENVIRONMENT);
		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.TRIAL_DESIGN);
		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.VARIATE);
		allocatedCount += this.getAllocatedCount(headerMap, PhenotypicType.GERMPLASM);

		return allocatedCount;
	}

	public int getAllocatedCount(final Map<PhenotypicType, List<VariableDTO>> headerMap, final PhenotypicType type) {
		final List<VariableDTO> variableDTOList = headerMap.get(type);

		if (variableDTOList == null) {
			return 0;
		} else {
			return variableDTOList.size();
		}
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, List<String>> processImport(@RequestBody final VariableDTO[] variables) {

		try {

			this.userSelection.clearMeasurementVariables();

			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			this.etlService.mergeVariableData(variables, workbook, this.userSelection);

			final org.generationcp.middleware.domain.etl.Workbook importData = this.etlService.convertToWorkbook(this.userSelection);

			final Map<String, List<Message>> messages = this.etlService.validateProjectOntology(importData);
			final Map<String, List<String>> proxy = new HashMap<>();

			final Set<String> vars = new HashSet<>();
			for (final VariableDTO variable : variables) {
				if (variable.getId() == null) {
					final Message message = new Message(ERROR_HEADER_NO_MAPPING);
					message.setMessageParams(new String[] {variable.getHeaderName()});
					final List<Message> messageList = new ArrayList<>();
					messageList.add(message);
					proxy.put(variable.getHeaderName(), this.etlService.convertMessageList(messageList));
				}
				if (!vars.add(variable.getHeaderName())) {// duplicate
					final Message message = new Message(ERROR_DUPLICATE_LOCAL_VARIABLE);
					message.setMessageParams(new String[] {variable.getHeaderName()});
					final List<Message> messageList = new ArrayList<>();
					messageList.add(message);
					proxy.put(variable.getHeaderName() + ":" + variable.getId(), this.etlService.convertMessageList(messageList));
				}
			}

			// If Location Name variable is present in the imported file, then the Location ID variable is required.
			if (this.checkIfLocationIdVariableExists(importData)) {
				final Message message = new Message(ERROR_LOCATION_ID_DOESNT_EXISTS);
				final List<Message> messageList = new ArrayList<>();
				messageList.add(message);
				proxy.put("", this.etlService.convertMessageList(messageList));
			}

			if (messages != null) {
				for (final Map.Entry<String, List<Message>> entry : messages.entrySet()) {
					proxy.put(entry.getKey(), this.etlService.convertMessageList(entry.getValue()));
				}
			}

			return proxy;

		} catch (final Exception e) {
			AngularMapOntologyController.LOG.error(e.getMessage(), e);
			final Map<String, List<String>> errorMap = new HashMap<>();
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			errorMap.put(Constants.GLOBAL, this.etlService.convertMessageList(error));
			return errorMap;
		}
	}

	protected boolean checkIfLocationIdVariableExists(final org.generationcp.middleware.domain.etl.Workbook importData)
			throws IOException, WorkbookParserException {

		final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook = this.dataImportService
				.parseWorkbookDescriptionSheet(this.etlService.retrieveCurrentWorkbook(this.userSelection),
						this.contextUtil.getCurrentIbdbUserId());
		importData.setConstants(referenceWorkbook.getConstants());
		importData.setConditions(referenceWorkbook.getConditions());

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();

		// The location name/id can be found on either conditions
		// or factors section of the fieldbook file if the study is Trial
		measurementVariables.addAll(importData.getConditions());
		measurementVariables.addAll(importData.getFactors());

		final boolean isLocationIDVariableExists =
				dataImportService.findMeasurementVariableByTermId(TermId.LOCATION_ID.getId(), measurementVariables).isPresent();
		final boolean isLocationNameVariableExists =
				dataImportService.findMeasurementVariableByTermId(TermId.TRIAL_LOCATION.getId(), measurementVariables).isPresent();

		// If Location Name variable is present in the imported file, then the Location ID variable is required.
		return isLocationNameVariableExists && !isLocationIDVariableExists;

	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String uploadFile(@ModelAttribute("uploadForm") final FileUploadForm uploadForm, final BindingResult result, final Model model) {
		final FileUploadFormValidator validator = new FileUploadFormValidator();
		validator.validate(uploadForm, result);

		if (result.hasErrors()) {
			return this.getContentName();
		} else {
			try {
				final String tempFileName = this.etlService.storeUserWorkbook(uploadForm.getFile().getInputStream());
				this.userSelection.setServerFileName(tempFileName);
				this.userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
			} catch (final IOException e) {
				AngularMapOntologyController.LOG.error(e.getMessage(), e);
				result.reject("uploadForm.file", "Error occurred while uploading file.");
			}
			return "redirect:" + AngularMapOntologyController.URL;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/confirm", method = RequestMethod.POST)
	public Map<String, Object> confirmImport(@RequestBody final VariableDTO[] variables, final HttpSession session,
			final HttpServletRequest request) {

		try {

			this.userSelection.clearMeasurementVariables();

			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			this.etlService.mergeVariableData(variables, workbook, this.userSelection);
			final org.generationcp.middleware.domain.etl.Workbook importData = this.etlService.convertToWorkbook(this.userSelection);

			final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook = this.dataImportService
					.parseWorkbookDescriptionSheet(this.etlService.retrieveCurrentWorkbook(this.userSelection),
							this.contextUtil.getCurrentIbdbUserId());
			importData.setConstants(referenceWorkbook.getConstants());
			importData.setConditions(referenceWorkbook.getConditions());

			this.dataImportService.addLocationIDVariableIfNotExists(importData, importData.getFactors(), this.contextUtil.getCurrentProgramUUID());
			this.dataImportService.assignLocationIdVariableToEnvironmentDetailSection(importData);
			this.dataImportService.removeLocationNameVariableIfExists(importData);
			this.fieldbookService.addStudyUUIDConditionAndPlotIDFactorToWorkbook(importData, false);

			this.etlService.saveProjectOntology(importData, this.contextUtil.getCurrentProgramUUID());
			this.userSelection.setStudyId(importData.getStudyDetails().getId());
			this.userSelection.setTrialDatasetId(importData.getTrialDatasetId());
			this.userSelection.setMeasurementDatasetId(importData.getMeasurementDatesetId());
			this.userSelection.setMeansDatasetId(importData.getMeansDatasetId());

			return this.wrapFormResult(AngularOpenSheetController.URL, request);

		} catch (final Exception e) {
			AngularMapOntologyController.LOG.error(e.getMessage(), e);
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			final List<String> errorMessages = this.etlService.convertMessageList(error);
			return this.wrapFormResult(errorMessages);
		}

	}

	@ModelAttribute("uploadForm")
	public FileUploadForm getForm() {
		return new FileUploadForm();
	}

}
