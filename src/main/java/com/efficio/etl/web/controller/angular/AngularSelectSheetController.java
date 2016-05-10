
package com.efficio.etl.web.controller.angular;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.etl.Constants;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.service.impl.ETLServiceImpl;
import com.efficio.etl.web.AbstractBaseETLController;
import com.efficio.etl.web.bean.ConsolidatedStepForm;
import com.efficio.etl.web.bean.RowDTO;
import com.efficio.etl.web.bean.SheetDTO;
import com.efficio.etl.web.bean.StudyDetailsForm;
import com.efficio.etl.web.bean.UserSelection;

@Controller
@RequestMapping(AngularSelectSheetController.URL)
public class AngularSelectSheetController extends AbstractBaseETLController {

	private static final Logger LOG = LoggerFactory.getLogger(AngularSelectSheetController.class);
	public static final String URL = "/workbook/step2";

	public static final int ROW_COUNT_PER_SCREEN = 10;
	public static final int MAX_DISPLAY_CHARACTER_PER_ROW = 60;
	public static final int FIELDBOOK_DEFAULT_STUDY_ID = 1;

	protected static final SimpleDateFormat DATE_PICKER_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_2);
	protected static final SimpleDateFormat DB_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.DATE_AS_NUMBER_FORMAT);
	private static final String ADD_TO_NEW_STUDY = "add.to.new.study";

	@Resource
	private ETLService etlService;

	@Resource(name = "etlUserSelection")
	private UserSelection userSelection;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public String getContentName() {
		return "angular/angularSelectSheet";
	}

	@Override
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String show(Model model, HttpServletRequest request) {

		// removed code for initial retrieval of rows since at this point, user has not yet selected a sheet

		model.addAttribute("displayedRows", AngularSelectSheetController.ROW_COUNT_PER_SCREEN);
		List<StudyDetails> previousStudies = this.getPreviousStudies();

		for (StudyDetails previousStudy : previousStudies) {
			if (!StringUtils.isEmpty(previousStudy.getStartDate())) {
				try {
					Date date = AngularSelectSheetController.DB_FORMAT.parse(previousStudy.getStartDate());
					previousStudy.setStartDate(AngularSelectSheetController.DATE_PICKER_FORMAT.format(date));
				} catch (ParseException e) {
					AngularSelectSheetController.LOG.error(e.getMessage(), e);
				}
			}

			if (!StringUtils.isEmpty(previousStudy.getEndDate())) {
				try {
					Date date = AngularSelectSheetController.DB_FORMAT.parse(previousStudy.getEndDate());
					previousStudy.setEndDate(AngularSelectSheetController.DATE_PICKER_FORMAT.format(date));
				} catch (ParseException e) {
					AngularSelectSheetController.LOG.error(e.getMessage(), e);
				}
			}
		}

		if ((this.userSelection.getStudyId() == null || this.userSelection.getStudyId() == 0 || this.userSelection.getStudyId() == AngularSelectSheetController.FIELDBOOK_DEFAULT_STUDY_ID)
				&& !StringUtils.isEmpty(this.userSelection.getStudyName())) {
			StudyDetails details = new StudyDetails();
			details.setId(this.userSelection.getStudyId() != null ? this.userSelection.getStudyId() : 0);
			details.setStudyName(this.userSelection.getStudyName());
			details.setTitle(this.userSelection.getStudyTitle());
			details.setObjective(this.userSelection.getStudyObjective());
			details.setEndDate(this.userSelection.getStudyEndDate());
			details.setStartDate(this.userSelection.getStudyStartDate());
			details.setStudyType(StudyType.getStudyType(this.userSelection.getStudyType()));
			details.setLabel(this.etlService.convertMessage(new Message(AngularSelectSheetController.ADD_TO_NEW_STUDY)));
			previousStudies.add(details);
		} else {
			try {
				if (!this.populateStudyDetailsIfFieldbookFormat(previousStudies, model)) {
					StudyDetails newStudy = new StudyDetails();
					newStudy.setId(0);
					newStudy.setLabel(this.etlService.convertMessage(new Message(AngularSelectSheetController.ADD_TO_NEW_STUDY)));
					previousStudies.add(newStudy);
				}
			} catch (IOException e) {
				AngularSelectSheetController.LOG.error(e.getMessage(), e);
			}
		}

		model.addAttribute("previousStudies", previousStudies);

		// reset mapped headers
		this.userSelection.clearMeasurementVariables();

		return super.show(model);
	}

	private boolean populateStudyDetailsIfFieldbookFormat(List<StudyDetails> previousStudies, Model model) throws IOException {
		boolean addedNewStudy = false;
		// check if fieldbook format by checking 1st sheet
		boolean inFieldbookFormat = false;
		Workbook wb = this.etlService.retrieveCurrentWorkbook(this.userSelection);
		if (wb.getNumberOfSheets() > 1) {
			Sheet sheet1 = wb.getSheetAt(ETLServiceImpl.DESCRIPTION_SHEET);
			Sheet sheet2 = wb.getSheetAt(ETLServiceImpl.OBSERVATION_SHEET);
			if (sheet1 != null && "Description".equalsIgnoreCase(sheet1.getSheetName()) && sheet2 != null
					&& "Observation".equalsIgnoreCase(sheet2.getSheetName())) {
				inFieldbookFormat = true;
			}
			if (inFieldbookFormat) {
				this.userSelection.setSelectedSheet(ETLServiceImpl.OBSERVATION_SHEET);
				this.userSelection.setHeaderRowIndex(0);
				List<String> fileHeaders = this.etlService.retrieveColumnHeaders(wb, this.userSelection);
				if (fileHeaders != null) {
					this.userSelection.setHeaderRowDisplayText(StringUtils.join(fileHeaders, ','));
				}
				StudyDetails studyDetails = this.etlService.readStudyDetails(sheet1);
				if (studyDetails != null) {
					String studyName = studyDetails.getStudyName();
					StudyDetails previousStudy = null;
					// check if study name already exists
					if (studyName != null) {
						for (StudyDetails s : previousStudies) {
							if (studyName.equals(s.getStudyName())) {
								previousStudy = s;
							}
						}
					}
					if (previousStudy == null) {
						// set a temporary study id - 0 must not be used as it is used for user-defined study name
						studyDetails.setId(AngularSelectSheetController.FIELDBOOK_DEFAULT_STUDY_ID);
						studyDetails.setLabel(this.etlService.convertMessage(new Message(AngularSelectSheetController.ADD_TO_NEW_STUDY)));
						// format dates
						final String oldFormat = "yyyyMMdd";
						final String newFormat = "MM/dd/yyyy";
						studyDetails.setStartDate(ETLServiceImpl.formatDate(studyDetails.getStartDate(), oldFormat, newFormat));
						studyDetails.setEndDate(ETLServiceImpl.formatDate(studyDetails.getEndDate(), oldFormat, newFormat));
						// add study to list
						previousStudies.add(studyDetails);
						addedNewStudy = true;
					} else {
						studyDetails = previousStudy;
					}
					if (this.userSelection.getStudyName() == null) {
						// update user selection
						this.userSelection.setStudyId(studyDetails.getId());
						this.userSelection.setStudyName(studyName);
						this.userSelection.setStudyTitle(studyDetails.getTitle());
						this.userSelection.setStudyObjective(studyDetails.getObjective());
						this.userSelection.setStudyStartDate(studyDetails.getStartDate());
						this.userSelection.setStudyEndDate(studyDetails.getEndDate());
						this.userSelection.setStudyType(studyDetails.getStudyType() == null ? "" : studyDetails.getStudyType().getName());
						// update form
						ConsolidatedStepForm form = this.getSelectRowsForm();
						model.addAttribute("form", form);
					}
				}
			}
		} else {
			this.userSelection.setSelectedSheet(0);
			// update form
			ConsolidatedStepForm form = this.getSelectRowsForm();
			model.addAttribute("form", form);
		}

		return addedNewStudy;
	}

	// added support for parameterized sheet index
	@ResponseBody
	@RequestMapping(value = "/displayRow", params = "list=true")
	public List<RowDTO> getUpdatedRowDisplayHTML(@RequestParam(value = "lastRowIndex") Integer lastRowIndex, @RequestParam(
			value = "startRowIndex", required = false) Integer startRow,
			@RequestParam(value = "selectedSheetIndex") Integer selectedSheetIndex) {

		try {
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			Integer finalStartRow = startRow;
			if (startRow == null) {
				finalStartRow = 0;
			}

			int count = this.etlService.getAvailableRowsForDisplay(workbook, selectedSheetIndex);

			Integer finalLastRowIndex = lastRowIndex;
			if (lastRowIndex > count) {
				finalLastRowIndex = count;
			}

			return this.etlService.retrieveRowInformation(workbook, selectedSheetIndex, finalStartRow, finalLastRowIndex,
					AngularSelectSheetController.MAX_DISPLAY_CHARACTER_PER_ROW);

		} catch (IOException e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
			return new ArrayList<RowDTO>();
		}
	}

	// changed row count implem to have parameterized selected sheet index
	@ResponseBody
	@RequestMapping(value = "/displayRow", params = "count=true")
	public Map<String, Object> getMaximumRowDisplayCount(@RequestParam(value = "selectedSheetIndex") Integer selectedSheetIndex) {
		Map<String, Object> returnValue = new HashMap<String, Object>();
		try {
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
			Integer count = this.etlService.getAvailableRowsForDisplay(workbook, selectedSheetIndex);
			returnValue.put("value", count);
			returnValue.put("status", "ok");
		} catch (IOException e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
			returnValue.put("status", "error");
		}

		return returnValue;
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> processForm(@RequestBody ConsolidatedStepForm form, HttpServletRequest request) {

		// validation routine
		String startDateString = form.getStudyDetails().getStartDate();
		String endDateString = form.getStudyDetails().getEndDate();
		Date startDate = null;
		Date endDate = null;
		List<Message> messageList = new ArrayList<Message>();

		if (!StringUtils.isEmpty(startDateString)) {
			// check if date is later than current date
			try {
				startDate = AngularSelectSheetController.DATE_PICKER_FORMAT.parse(startDateString);
				if (startDate.after(new Date())) {
					messageList.add(new Message("error.start.is.after.current.date"));
				}
			} catch (ParseException e) {
				AngularSelectSheetController.LOG.error(e.getMessage(), e);
			}
		}

		if (!StringUtils.isEmpty(endDateString)) {
			try {
				endDate = AngularSelectSheetController.DATE_PICKER_FORMAT.parse(endDateString);

				if (startDate == null) {
					messageList.add(new Message("error.date.startdate.required"));
				} else if (endDate.before(startDate)) {
					messageList.add(new Message("error.date.enddate.invalid"));
				}
			} catch (ParseException e) {
				AngularSelectSheetController.LOG.error(e.getMessage(), e);
			}
		}

		if (!messageList.isEmpty()) {
			return this.wrapFormResult(this.etlService.convertMessageList(messageList));
		}

		// transfer form data to user selection object
		this.userSelection.setSelectedSheet(form.getSelectedSheetIndex());
		this.userSelection.setHeaderRowIndex(form.getHeaderRowIndex());
		this.userSelection.setHeaderRowDisplayText(form.getHeaderRowDisplayText());
		this.userSelection.setStudyName(form.getStudyDetails().getStudyName());
		this.userSelection.setStudyTitle(form.getStudyDetails().getStudyTitle());
		this.userSelection.setStudyObjective(form.getStudyDetails().getObjective());
		this.userSelection.setStudyStartDate(form.getStudyDetails().getStartDate());
		this.userSelection.setStudyEndDate(form.getStudyDetails().getEndDate());
		this.userSelection.setStudyType(form.getStudyDetails().getStudyType());
		this.userSelection.setDatasetType(form.getDatasetType());
		// routing logic for existing study vs new study details
		Integer studyId = form.getStudyDetails().getStudyId();
		this.userSelection.setStudyId(studyId);
		if (studyId != null && studyId != 0) {
			List<String> errors = new ArrayList<String>();
			Map<String, List<Message>> mismatchErrors = null;
			boolean isMeansDataImport =
					this.userSelection.getDatasetType() != null
							&& this.userSelection.getDatasetType().intValue() == DataSetType.MEANS_DATA.getId();

			try {
				// check if the selected dataset still has no mapped headers
				if (isMeansDataImport) {
					if (!this.etlService.hasMeansDataset(studyId)) {
						return this.wrapFormResult(AngularMapOntologyController.URL, request);
					}
				} else {
					if (!this.etlService.hasMeasurementEffectDataset(studyId)) {
						return this.wrapFormResult(AngularMapOntologyController.URL, request);
					}
				}

				try {
					// TODO : refactor validation logic to avoid duplication with ImportObservationsController
					Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);
					org.generationcp.middleware.domain.etl.Workbook importData =
							this.etlService.retrieveAndSetProjectOntology(this.userSelection, isMeansDataImport);

					List<String> fileHeaders = this.etlService.retrieveColumnHeaders(workbook, this.userSelection);
					List<MeasurementVariable> studyHeaders = importData.getAllVariables();
					mismatchErrors = this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, isMeansDataImport);
				} catch (Exception e) {
					AngularSelectSheetController.LOG.error(e.getMessage(), e);
					List<Message> error = new ArrayList<Message>();
					error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
					errors.addAll(this.etlService.convertMessageList(error));
				}

				if (mismatchErrors != null && !mismatchErrors.isEmpty()) {
					for (Map.Entry<String, List<Message>> entry : mismatchErrors.entrySet()) {
						errors.addAll(this.etlService.convertMessageList(entry.getValue()));
					}
					return this.wrapFormResult(errors);
				} else {
					return this.wrapFormResult(AngularOpenSheetController.URL, request);
				}

			} catch (Exception e) {
				AngularSelectSheetController.LOG.error(e.getMessage(), e);
				List<Message> error = new ArrayList<Message>();
				error.add(new Message(Constants.MESSAGE_KEY_GENERIC_ERROR));
				errors.addAll(this.etlService.convertMessageList(error));
				return this.wrapFormResult(errors);
			}
		} else {
			return this.wrapFormResult(AngularMapOntologyController.URL, request);
		}

	}

	@ModelAttribute("form")
	public ConsolidatedStepForm getSelectRowsForm() {
		ConsolidatedStepForm consolidatedForm = new ConsolidatedStepForm();
		consolidatedForm.setSelectedSheetIndex(this.userSelection.getSelectedSheet());

		consolidatedForm.setHeaderRowIndex(this.userSelection.getHeaderRowIndex());
		consolidatedForm.setHeaderRowDisplayText(this.userSelection.getHeaderRowDisplayText());
		consolidatedForm.setDatasetType(this.userSelection.getDatasetType() != null ? this.userSelection.getDatasetType()
				: DataSetType.PLOT_DATA.getId());

		StudyDetailsForm studyDetailsForm = new StudyDetailsForm();
		studyDetailsForm.setStudyName(this.userSelection.getStudyName());
		studyDetailsForm.setStudyTitle(this.userSelection.getStudyTitle());
		studyDetailsForm.setObjective(this.userSelection.getStudyObjective());
		studyDetailsForm.setStartDate(this.userSelection.getStudyStartDate());
		studyDetailsForm.setEndDate(this.userSelection.getStudyEndDate());
		studyDetailsForm.setStudyType(this.userSelection.getStudyType());
		studyDetailsForm.setStudyId(this.userSelection.getStudyId());
		studyDetailsForm.setStudyType(this.userSelection.getStudyType() == null ? "" : this.userSelection.getStudyType());

		consolidatedForm.setStudyDetails(studyDetailsForm);

		return consolidatedForm;
	}

	@ModelAttribute("sheetList")
	public List<SheetDTO> getSheets() {
		try {
			Workbook workbook = this.etlService.retrieveCurrentWorkbook(this.userSelection);

			return this.etlService.retrieveSheetInformation(workbook);

		} catch (IOException e) {
			AngularSelectSheetController.LOG.error(e.getMessage(), e);
		}

		return new ArrayList<SheetDTO>();
	}

	@ModelAttribute("studyTypeList")
	public Map<String, String> getStudyTypes() {

		Map<String, String> studyTypes = new HashMap<String, String>();

		for (StudyType type : StudyType.values()) {
			String studyType = this.etlService.getCVDefinitionById(type.getId());
			if (studyType != null && !"".equals(studyType.trim())) {
				studyTypes.put(type.getName(), studyType);
			}
		}

		return studyTypes;
	}

	@ModelAttribute("datasetTypeList")
	public Map<Integer, String> getDatasetTypes() {

		Map<Integer, String> datasetTypes = new HashMap<Integer, String>();
		datasetTypes.put(DataSetType.PLOT_DATA.getId(), this.etlService.getCVDefinitionById(DataSetType.PLOT_DATA.getId()));
		datasetTypes.put(DataSetType.MEANS_DATA.getId(), this.etlService.getCVDefinitionById(DataSetType.MEANS_DATA.getId()));
		return datasetTypes;
	}

	public List<StudyDetails> getPreviousStudies() {
		return this.etlService.retrieveExistingStudyDetails(this.contextUtil.getCurrentProgramUUID());
	}
}
