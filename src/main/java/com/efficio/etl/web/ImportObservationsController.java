
package com.efficio.etl.web;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.PhenotypeException;
import org.generationcp.middleware.util.Message;
import org.generationcp.middleware.util.PoiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
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

	@RequestMapping(method = RequestMethod.GET)
	public String processImport(@ModelAttribute("uploadForm") FileUploadForm uploadForm, Model model, HttpSession session,
			HttpServletRequest request) {
		List<String> errors = new ArrayList<String>();
		org.generationcp.middleware.domain.etl.Workbook importData = null;
		String programUUID = null;
		try {
			programUUID = this.contextUtil.getCurrentProgramUUID();
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			boolean isMeansDataImport =
					this.userSelection.getDatasetType() != null
							&& this.userSelection.getDatasetType().intValue() == DataSetType.MEANS_DATA.getId();

			ImportObservationsController.LOG.debug("userSelection.getPhenotypicMap() = " + this.userSelection.getPhenotypicMap());
			// check if headers are not set (it means the user skipped the import project ontology)
			if (this.userSelection.getPhenotypicMap() == null || this.userSelection.getPhenotypicMap().isEmpty()) {
				// set variables and ids in workbook
				importData = this.etlService.retrieveAndSetProjectOntology(this.userSelection, isMeansDataImport);
			} else {
				// get workbook from user selection
				importData = this.etlService.convertToWorkbook(this.userSelection);
			}
			List<String> fileHeaders = this.etlService.retrieveColumnHeaders(workbook, this.userSelection);
			List<MeasurementVariable> studyHeaders = importData.getAllVariables();
			Map<String, List<Message>> mismatchErrors =
					this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, isMeansDataImport);

			Sheet sheet = workbook.getSheetAt(this.userSelection.getSelectedSheet());
			Integer maxLimit = 10000;
			Integer lastRowNum = PoiUtil.getLastRowNum(sheet);

			if (lastRowNum == 0) {
				List<Message> messages = new ArrayList<Message>();
				Message message = new Message("error.observation.no.records");
				messages.add(message);
				errors.addAll(this.etlService.convertMessageList(messages));
			} else if (lastRowNum > maxLimit) {
				List<Message> messages = new ArrayList<Message>();
				Message message = new Message("error.observation.over.maximum.limit", new DecimalFormat("###,###,###").format(maxLimit));
				messages.add(message);
				errors.addAll(this.etlService.convertMessageList(messages));
			}

			if (mismatchErrors != null && !mismatchErrors.isEmpty()) {
				for (Map.Entry<String, List<Message>> entry : mismatchErrors.entrySet()) {
					errors.addAll(this.etlService.convertMessageList(entry.getValue()));
				}
			} else if (lastRowNum <= maxLimit) {
				importData.setObservations(this.etlService.extractExcelFileData(workbook, this.userSelection, importData));

				// there is now an expectation after the validate project data step
				Map<String, List<Message>> projectDataErrors = this.etlService.validateProjectData(importData, programUUID);

				if (projectDataErrors != null) {
					for (Map.Entry<String, List<Message>> entry : projectDataErrors.entrySet()) {
						errors.addAll(this.etlService.convertMessageList(entry.getValue()));
					}
				}
			}

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
