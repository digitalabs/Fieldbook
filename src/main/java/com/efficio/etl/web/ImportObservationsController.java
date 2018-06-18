
package com.efficio.etl.web;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.controller.angular.AngularOpenSheetController;
import com.efficio.etl.web.validators.FileUploadFormValidator;
import com.efficio.fieldbook.service.api.WorkbenchService;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.PhenotypeException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(ImportObservationsController.URL)
public class ImportObservationsController extends AbstractBaseETLController {

	public static final String URL = "/etl/workbook/importObservations";
	private static final Logger LOG = LoggerFactory.getLogger(ImportObservationsController.class);

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Resource
	private ETLService etlService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private DataImportService dataImportService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	private boolean hasErrors;

	@Resource
	protected WorkbenchService workbenchService;

	@Override
	public String getContentName() {
		if (this.hasErrors) {
			return "etl/validateProjectData";
		}
		return "etl/importObservations";
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@RequestMapping(value = "/{confirmDiscard}", method = RequestMethod.GET)
	public String processImport(@ModelAttribute("uploadForm") final FileUploadForm uploadForm,
			@PathVariable final int confirmDiscard, final Model model, final HttpSession session,
			final HttpServletRequest request) {
		final List<String> errors = new ArrayList<>();

		org.generationcp.middleware.domain.etl.Workbook importData = null;

		String programUUID = null;
		try {

			programUUID = this.contextUtil.getCurrentProgramUUID();
			final Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			final boolean isMeansDataImport = this.userSelection.getDatasetType() != null
					&& this.userSelection.getDatasetType().intValue() == DataSetType.MEANS_DATA.getId();

			importData = this.etlService.createWorkbookFromUserSelection(this.userSelection, isMeansDataImport);

			this.dataImportService.populatePossibleValuesForCategoricalVariates(importData.getVariates(), programUUID);

			final List<MeasurementVariable> studyHeaders = importData.getFactors();

			final List<String> fileHeaders = this.etlService.retrieveColumnHeaders(workbook, this.userSelection,
					this.etlService.headersContainsPlotId(importData));

			final Map<String, List<Message>> mismatchErrors = this.etlService.checkForMismatchedHeaders(fileHeaders,
					studyHeaders, isMeansDataImport);

			final boolean isWorkbookHasObservationRecords = this.etlService
					.isWorkbookHasObservationRecords(this.userSelection, errors, workbook);
			final boolean isObservationOverMaxLimit = this.etlService.isObservationOverMaximumLimit(this.userSelection,
					errors, workbook);

			if (mismatchErrors != null && !mismatchErrors.isEmpty()) {
				for (final Map.Entry<String, List<Message>> entry : mismatchErrors.entrySet()) {
					errors.addAll(this.etlService.convertMessageList(entry.getValue()));
				}
			} else if (isWorkbookHasObservationRecords && !isObservationOverMaxLimit) {

				this.dataImportService.removeLocationNameVariableIfExists(importData);
				this.dataImportService.assignLocationIdVariableToEnvironmentDetailSection(importData);
				importData.setObservations(this.etlService.extractExcelFileData(workbook, this.userSelection,
						importData, confirmDiscard == 1 ? true : false));

				// there is now an expectation after the validate project data
				// step
				final Map<String, List<Message>> projectDataErrors = this.etlService.validateProjectData(importData,
						programUUID);

				if (projectDataErrors != null) {
					for (final Map.Entry<String, List<Message>> entry : projectDataErrors.entrySet()) {
						errors.addAll(this.etlService.convertMessageList(entry.getValue()));
					}
				}
			}

			final List<Message> messages = new ArrayList<>();
			this.dataImportService.checkForInvalidGids(importData, messages);
			errors.addAll(this.etlService.convertMessageList(messages));

		} catch (final Exception e) {
			ImportObservationsController.LOG.error(e.getMessage(), e);
			final List<Message> error = new ArrayList<>();
			error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
			errors.addAll(this.etlService.convertMessageList(error));

		}

		model.addAttribute("errors", errors);
		this.hasErrors = !errors.isEmpty();
		model.addAttribute("hasErrors", this.hasErrors);

		if (this.hasErrors) {
			return this.getContentName();
		} else {
			return this.confirmImport(model, importData, programUUID);
		}
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String uploadFile(@ModelAttribute("uploadForm") final FileUploadForm uploadForm, final BindingResult result,
			final Model model) {
		final FileUploadFormValidator validator = new FileUploadFormValidator();
		validator.validate(uploadForm, result);
		this.hasErrors = result.hasErrors();
		if (this.hasErrors) {
			return this.getContentName();
		} else {
			try {
				final String tempFileName = this.etlService.storeUserWorkbook(uploadForm.getFile().getInputStream());
				this.userSelection.setServerFileName(tempFileName);
				this.userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
			} catch (final IOException e) {
				ImportObservationsController.LOG.error(e.getMessage(), e);
				result.reject("uploadForm.file", "Error occurred while uploading file.");
			}
			return "redirect:" + AngularOpenSheetController.URL;
		}
	}

	public String confirmImport(final Model model, final org.generationcp.middleware.domain.etl.Workbook importData,
			final String programUUID) {
		final List<String> errors = new ArrayList<>();
		try {
			final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook = this.dataImportService
				.parseWorkbookDescriptionSheet(this.etlService.retrieveCurrentWorkbook(this.userSelection),
					this.contextUtil.getCurrentIbdbUserId());
			importData.setConstants(referenceWorkbook.getConstants());
			importData.setConditions(referenceWorkbook.getConditions());
			this.dataImportService.addLocationIDVariableInFactorsIfNotExists(importData, programUUID);
			this.dataImportService.assignLocationIdVariableToEnvironmentDetailSection(importData);
			this.dataImportService.removeLocationNameVariableIfExists(importData);
			this.dataImportService.populatePossibleValuesForCategoricalVariates(importData.getConditions(), programUUID);
			this.etlService.saveProjectData(importData, programUUID);

		} catch (final PhenotypeException e) {
			ImportObservationsController.LOG.error(e.getMessage(), e);
			errors.add(e.getMessage().replaceAll("\n", "<br>"));
		} catch (final Exception e) {
			ImportObservationsController.LOG.error(e.getMessage(), e);
			errors.add(e.getMessage());
		} finally {
			model.addAttribute("errors", errors);
			this.hasErrors = !errors.isEmpty();
			model.addAttribute("hasErrors", this.hasErrors);
		}

		return super.show(model);

	}
}
