
package com.efficio.etl.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.controller.angular.AngularOpenSheetController;
import com.efficio.etl.web.validators.FileUploadFormValidator;

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
	public String processImport(@ModelAttribute("uploadForm") FileUploadForm uploadForm, @PathVariable int confirmDiscard, Model model,
			HttpSession session, HttpServletRequest request) {
		List<String> errors = new ArrayList<String>();

		org.generationcp.middleware.domain.etl.Workbook importData = null;

		String programUUID = null;
		try {

			programUUID = this.contextUtil.getCurrentProgramUUID();
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			boolean isMeansDataImport =
					this.userSelection.getDatasetType() != null
							&& this.userSelection.getDatasetType().intValue() == DataSetType.MEANS_DATA.getId();

			importData = this.etlService.createWorkbookFromUserSelection(this.userSelection, isMeansDataImport);

			this.dataImportService.populatePossibleValuesForCategoricalVariates(importData.getVariates(), programUUID);

			List<MeasurementVariable> studyHeaders = importData.getAllVariables();
			boolean hasPlotId = false;
			for (MeasurementVariable mv: studyHeaders) {
				if (mv.getTermId() == 8201){
					hasPlotId = true;
					break;
				}
			}

			List<String> fileHeaders = this.etlService.retrieveColumnHeaders(workbook, this.userSelection, hasPlotId);

			Map<String, List<Message>> mismatchErrors =
					this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, isMeansDataImport);

			boolean isWorkbookHasObservationRecords = this.etlService.isWorkbookHasObservationRecords(this.userSelection, errors, workbook);
			boolean isObservationOverMaxLimit = this.etlService.isObservationOverMaximumLimit(this.userSelection, errors, workbook);

			if (mismatchErrors != null && !mismatchErrors.isEmpty()) {
				for (Map.Entry<String, List<Message>> entry : mismatchErrors.entrySet()) {
					errors.addAll(this.etlService.convertMessageList(entry.getValue()));
				}
			} else if (isWorkbookHasObservationRecords && !isObservationOverMaxLimit) {

				importData.setObservations(this.etlService.extractExcelFileData(workbook, this.userSelection, importData,
						confirmDiscard == 1 ? true : false));

				// there is now an expectation after the validate project data step
				Map<String, List<Message>> projectDataErrors = this.etlService.validateProjectData(importData, programUUID);

				if (projectDataErrors != null) {
					for (Map.Entry<String, List<Message>> entry : projectDataErrors.entrySet()) {
						errors.addAll(this.etlService.convertMessageList(entry.getValue()));
					}
				}
			}

			List<Message> messages = new ArrayList<Message>();
			this.dataImportService.checkForInvalidGids(importData, messages);
			errors.addAll(this.etlService.convertMessageList(messages));


		} catch (Exception e) {
			ImportObservationsController.LOG.error(e.getMessage(), e);
			List<Message> error = new ArrayList<Message>();
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
	public String uploadFile(@ModelAttribute("uploadForm") FileUploadForm uploadForm, BindingResult result, Model model) {
		FileUploadFormValidator validator = new FileUploadFormValidator();
		validator.validate(uploadForm, result);
		this.hasErrors = result.hasErrors();
		if (this.hasErrors) {
			return this.getContentName();
		} else {
			try {
				String tempFileName = this.etlService.storeUserWorkbook(uploadForm.getFile().getInputStream());
				this.userSelection.setServerFileName(tempFileName);
				this.userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
			} catch (IOException e) {
				ImportObservationsController.LOG.error(e.getMessage(), e);
				result.reject("uploadForm.file", "Error occurred while uploading file.");
			}
			return "redirect:" + AngularOpenSheetController.URL;
		}
	}

	public String confirmImport(Model model, org.generationcp.middleware.domain.etl.Workbook importData, String programUUID) {
		List<String> errors = new ArrayList<String>();
		try {

			this.etlService.saveProjectData(importData, programUUID);

		} catch (PhenotypeException e) {
			ImportObservationsController.LOG.error(e.getMessage(), e);
			errors.add(e.getMessage().replaceAll("\n", "<br>"));
		} catch (Exception e) {
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
