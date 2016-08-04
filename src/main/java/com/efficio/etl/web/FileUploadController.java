
package com.efficio.etl.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.HTTPSessionUtil;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.operation.parser.WorkbookParser;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.validators.FileUploadFormValidator;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Controller
@RequestMapping({"/etl", FileUploadController.URL})
public class FileUploadController extends AbstractBaseETLController {

	public static final String URL = "/etl/fileUpload";

	private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

	private static final String STATUS_CODE = "statusCode";

	private static final String ERROR_TYPE = "errorType";

	private static final String STATUS_MESSAGE = "statusMessage";

	private static final String UPLOAD_FORM_FILE = "uploadForm.file";

	@Resource
	private ETLService etlService;

  	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Resource
	private HTTPSessionUtil httpSessionUtil;

	@Resource
	private DataImportService dataImportService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private final Map<String, String> returnMessage = new HashMap<String, String>();

	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("uploadForm") FileUploadForm uploadForm, Model model, HttpSession session) {
		this.httpSessionUtil.clearSessionData(session, new String[] {HTTPSessionUtil.USER_SELECTION_SESSION_NAME});

		return super.show(model);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/importFromExternalSystem")
	public String showImportFromExternalSystem() {
		return "etl/importFromExternalSystem";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String uploadFile(@ModelAttribute("uploadForm") FileUploadForm uploadForm, BindingResult result, Model model) {
		FileUploadFormValidator validator = new FileUploadFormValidator();
		validator.validate(uploadForm, result);

		if (result.hasErrors()) {
			/**
			 * Return the user back to form to show errors
			 */
			return this.getContentName();
		} else {

			try {
				String tempFileName = this.etlService.storeUserWorkbook(uploadForm.getFile().getInputStream());
				this.userSelection.setServerFileName(tempFileName);
				this.userSelection.setActualFileName(uploadForm.getFile().getOriginalFilename());
			} catch (IOException e) {
				FileUploadController.LOG.error(e.getMessage(), e);
				result.reject(FileUploadController.UPLOAD_FORM_FILE, "Error occurred while uploading file.");
			}

			if ("fieldbook".equalsIgnoreCase(uploadForm.getImportType())) {
				model.addAttribute("fileName", this.userSelection.getActualFileName());
				return "etl/fileUploadFieldbook";

			} else {
				try {
					this.etlService.retrieveCurrentWorkbookWithValidation(this.userSelection);
					return "redirect: workbook/step2";
				} catch (IOException e) {
					FileUploadController.LOG.error(e.getMessage(), e);
					result.reject(FileUploadController.UPLOAD_FORM_FILE, "Error occurred while reading Excel file");
				} catch (WorkbookParserException e) {
					FileUploadController.LOG.error(e.getMessage(), e);
					result.reject(FileUploadController.UPLOAD_FORM_FILE, this.etlService.convertMessageList(e.getErrorMessages()).get(0));
				}
			}

			// at this point, we can assume that program has reached an error condition. we return user to the form

			return this.getContentName();
		}
	}

	@ResponseBody
	@RequestMapping(value = "startProcess/{confirmDiscard}", method = RequestMethod.POST)
	public Map<String, String> startProcess(@PathVariable int confirmDiscard, final HttpSession session, HttpServletRequest request,
			HttpServletResponse response, Model model) {
		// HTTP 1.1
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		// HTTP 1.0
		response.setHeader("Pragma", "no-cache");
		// Proxies
		response.setDateHeader("Expires", 0);

		this.returnMessage.put(FileUploadController.STATUS_CODE, "0");
		this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "Import has started.");

		try {

			String programUUID = this.contextUtil.getCurrentProgramUUID();
			org.generationcp.middleware.domain.etl.Workbook wb;

			wb =
					this.dataImportService.parseWorkbook(this.etlService.retrieveCurrentWorkbookAsFile(this.userSelection), programUUID,
							confirmDiscard == 1 ? true : false, this.ontologyDataManager, new WorkbookParser());

			this.dataImportService.saveDataset(wb, programUUID);

			this.httpSessionUtil.clearSessionData(session, new String[] {HTTPSessionUtil.USER_SELECTION_SESSION_NAME});

			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, "1");
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "Import is done.");

		} catch (WorkbookParserException e) {

			FileUploadController.LOG.error(e.getMessage(), e);
			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, "-1");
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, e.getMessage());
			this.returnMessage.put(FileUploadController.ERROR_TYPE, e.getClass().getSimpleName());

		} catch (IOException e) {
			FileUploadController.LOG.error(e.getMessage(), e);
			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, "-1");
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "An error occurred while reading the file.");
			this.returnMessage.put(FileUploadController.ERROR_TYPE, e.getClass().getSimpleName());
		}

		return this.returnMessage;

	}

	@ResponseBody
	@RequestMapping(value = "validateAndParseWorkbook", method = RequestMethod.POST)
	public Map<String, String> validateAndParseWorkbook(final HttpSession session, HttpServletRequest request,
			HttpServletResponse response, Model model) {

		// HTTP 1.1
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		// HTTP 1.0
		response.setHeader("Pragma", "no-cache");
		// Proxies
		response.setDateHeader("Expires", 0);

		try {

			String programUUID = this.contextUtil.getCurrentProgramUUID();
			Workbook workbook =
					this.dataImportService.strictParseWorkbook(this.etlService.retrieveCurrentWorkbookAsFile(this.userSelection),
							programUUID);

			if (workbook.hasOutOfBoundsData()) {
				this.returnMessage.put(FileUploadController.STATUS_CODE, "2");
				this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "");
			} else {
				this.returnMessage.put(FileUploadController.STATUS_CODE, "1");
				this.returnMessage.put(FileUploadController.STATUS_MESSAGE, "");
			}

		} catch (IOException e) {

			FileUploadController.LOG.error(e.getMessage(), e);

			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, "-1");
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, e.getMessage());
			this.returnMessage.put(FileUploadController.ERROR_TYPE, "IOException");

		} catch (WorkbookParserException e) {

			FileUploadController.LOG.error(e.getMessage(), e);
			Boolean isMaxLimitException = false;
			StringBuilder builder = new StringBuilder();
			builder.append("The system detected format errors in the file:<br/><br/>");
			if (e.getErrorMessages() != null) {
				for (Message m : e.getErrorMessages()) {
					if (m != null) {
						try {
							builder.append(this.messageSource.getMessage(m.getMessageKey(), m.getMessageParams(), null) + "<br />");
							if ("error.observation.over.maximum.limit".equals(m.getMessageKey())
									|| "error.file.is.too.large".equals(m.getMessageKey())) {
								isMaxLimitException = true;
							}
						} catch (Exception ex) {
							FileUploadController.LOG.error(ex.getMessage(), ex);
						}
					}
				}
			} else {
				builder.append(e.getMessage());
			}

			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, "-1");
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, builder.toString());
			if (isMaxLimitException) {
				this.returnMessage.put(FileUploadController.ERROR_TYPE, "WorkbookParserException-OverMaxLimit");
			} else {
				this.returnMessage.put(FileUploadController.ERROR_TYPE, "WorkbookParserException");
			}

		} catch (Exception e) {
			FileUploadController.LOG.error(e.getMessage(), e);

			this.returnMessage.clear();
			this.returnMessage.put(FileUploadController.STATUS_CODE, "-1");
			this.returnMessage.put(FileUploadController.STATUS_MESSAGE, e.getMessage());
			this.returnMessage.put(FileUploadController.ERROR_TYPE, "Exception");

		}
		return this.returnMessage;

	}

	@Override
	public String getContentName() {
		return "etl/fileUpload";
	}

	@ModelAttribute("form")
	public FileUploadForm getForm() {
		return new FileUploadForm();
	}

	public void setEtlService(ETLService etlService) {
		this.etlService = etlService;
	}

	public void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}
}
