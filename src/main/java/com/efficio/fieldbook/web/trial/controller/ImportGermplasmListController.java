/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.controller;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.exception.FieldbookRequestValidationException;
import com.efficio.fieldbook.web.study.germplasm.StudyGermplasmTransformer;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.apache.commons.lang.StringUtils;
import org.fest.util.Collections;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This controller handles the 2nd step in the study manager process.
 *
 * @author Daniel Jao
 */
@Controller
@RequestMapping({ ImportGermplasmListController.URL,
		ImportGermplasmListController.URL_1, ImportGermplasmListController.URL_2 })
@Transactional
public class ImportGermplasmListController extends SettingsController {

	private static final String SUCCESS = "success";

	private static final String ERROR = "error";

	protected static final String TABLE_HEADER_LIST = "tableHeaderList";

	static final String TYPE2 = "type";

	static final String LIST_DATA_TABLE = "listDataTable";

	static final String CHECK_LISTS = "checkLists";

	protected static final String ENTRY_CODE = "entryCode";

	protected static final String ENTRY_ID = "entryId";

	protected static final String SOURCE = "source";

	protected static final String CROSS = "cross";

	protected static final String CHECK = "check";

	protected static final String GID = "gid";

	protected static final String DESIG = "desig";

	protected static final String ENTRY_NUMBER = "entryNumber";

	static final String CHECK_OPTIONS = "checkOptions";

	static final String POSITION = "position";

	private static final String GROUP_ID = "groupId";

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmListController.class);

	/** The Constant URL. */
	public static final String URL = "/StudyManager/importGermplasmList";
	static final String URL_1 = "/TrialManager/GermplasmList";
	static final String URL_2 = "/ListManager/GermplasmList";

	/** The Constant PAGINATION_TEMPLATE. */
	private static final String PAGINATION_TEMPLATE = "/StudyManager/showGermplasmPagination";
	private static final String EDIT_CHECK = "/Common/editCheck";

	private static final int NO_ID = -1;

	static final String STARTING_PLOT_NO = "1";
	static final String HAS_SAVED_GERMPLASM = "hasSavedGermplasm";

	/** The germplasm list manager. */
	@Resource
	private GermplasmListManager germplasmListManager;

	/** The data import service. */
	@Resource
	private DataImportService dataImportService;

	/** The ontology service. */
	@Resource
	private OntologyService ontologyService;

	/** The message source. */
	@Autowired
	public MessageSource messageSource;

	/** The Inventory list manager. */
	@Resource
	private InventoryDataManager inventoryDataManager;

	@Resource
	private StudyEntryService studyEntryService;

	@Resource
	private StudyGermplasmTransformer studyGermplasmTransformer;

	private static final String DEFAULT_TEST_VALUE = "T";

	@Override
	public String getContentName() {
		return "StudyManager/importGermplasmList";
	}

	/**
	 * Gets the user selection.
	 *
	 * @return the user selection
	 */
	public UserSelection getUserSelection() {
		return this.userSelection;
	}

	/**
	 * Show the main import page.
	 *
	 * @param form
	 *            the form
	 * @param model
	 *            the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form,
			final Model model) {
		// this set the necessary info from the session variable

		form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
		if (this.getUserSelection().getImportedGermplasmMainInfo() != null
				&& this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null) {
			// this would be use to display the imported germplasm info
			form.setImportedGermplasm(this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList()
					.getImportedGermplasms());
			form.setGermplasmListId(this.getUserSelection().getImportedGermplasmMainInfo().getListId());

			form.changePage(1);
			this.userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

		}
		return super.show(model);
	}

	/**
	 * Goes to the Next screen. Added validation if a germplasm list was
	 * properly uploaded
	 *
	 * @param form
	 *            the form
	 * @param result
	 *            the result
	 * @param model
	 *            the model
	 * @return the string
	 * @throws MiddlewareQueryException
	 *             the middleware query exception
	 */
	@ResponseBody
	@RequestMapping(value = { "/next", "/submitAll" }, method = RequestMethod.POST)
	@Transactional
	public String nextScreen(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form,
			final BindingResult result, final Model model, final HttpServletRequest req) {
		// start: section for taking note of the check germplasm
		boolean isDeleteObservations = false;

		// if we have no germplasm list available for the study, skip this
		// validation flow
		if (null != this.userSelection.getImportedGermplasmMainInfo()
				&& null != this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList()) {
			this.assignPlotNumber(form);
			isDeleteObservations = true;
		}

		this.userSelection.setMeasurementRowList(null);
		this.userSelection.getWorkbook().setOriginalObservations(null);
		this.userSelection.getWorkbook().setObservations(new ArrayList<>());
		this.fieldbookService.createIdCodeNameVariablePairs(this.userSelection.getWorkbook(),
				AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
		this.fieldbookService.createIdNameVariablePairs(this.userSelection.getWorkbook(),
				new ArrayList<SettingDetail>(), AppConstants.ID_NAME_COMBINATION.getString(), true);
		final int studyId = this.dataImportService.saveDataset(this.userSelection.getWorkbook(), true,
				isDeleteObservations, this.getCurrentProject().getUniqueID(),
				this.getCurrentProject().getCropType());

		// for saving the stocks
		this.saveStudyGermplasm(studyId);

		this.fieldbookService.saveStudyColumnOrdering(studyId,
			form.getColumnOrders(), this.userSelection.getWorkbook());

		return Integer.toString(studyId);
	}

	void assignPlotNumber(final ImportGermplasmListForm form) {
		if (this.userSelection.getImportedGermplasmMainInfo() != null) {


			Integer plotNo = null;
			if (form.getStartingPlotNo() != null) {
				plotNo = org.generationcp.middleware.util.StringUtil.parseInt(form.getStartingPlotNo(), null);
				if (plotNo == null) {
					throw new FieldbookRequestValidationException("plot.number.should.be.in.range");

				}
			}


			// Setting plot number in user selection as it will be used later
			this.userSelection.setStartingPlotNo(plotNo);

		}
	}

	/**
	 * Saves the study germplasm associated to the study.
	 * @param studyId
	 */
	private void saveStudyGermplasm(final int studyId) {

		final ImportedGermplasmMainInfo germplasmMainInfo = this.getUserSelection().getImportedGermplasmMainInfo();
		final ImportedGermplasmList importedGermplasmList = germplasmMainInfo != null ? germplasmMainInfo.getImportedGermplasmList() : null;

		if (importedGermplasmList != null && !Collections.isEmpty(importedGermplasmList.getImportedGermplasms())) {
			final List<ImportedGermplasm> importedGermplasm = importedGermplasmList.getImportedGermplasms();
			final List<StudyEntryDto> studyEntryDtoList = this.studyGermplasmTransformer.transformToStudyEntryDto(importedGermplasm);
			// Delete the existing stocks so that we can replace it with the current list.
			this.studyEntryService.deleteStudyEntries(studyId);
			this.studyEntryService.saveStudyEntries(studyId, studyEntryDtoList);
		} else {
			// we delete the record in the db
			this.studyEntryService.deleteStudyEntries(studyId);
		}

	}

	void initializeObjectsForGermplasmDetailsView(final ImportGermplasmListForm form,
		final Model model, final ImportedGermplasmMainInfo mainInfo, final List<ImportedGermplasm> list,
		final List<Map<String, Object>> dataTableDataList, final Boolean isNewList) {
		// Set first entry number from the list
		if (!list.isEmpty()) {
			final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);
		}

		if (this.userSelection.getMeasurementRowList() != null
				&& !this.userSelection.getMeasurementRowList().isEmpty()) {
			form.setStartingPlotNo(
					this.userSelection.getMeasurementRowList().get(0).getMeasurementDataValue(TermId.PLOT_NO.getId()));
		}

		// Set the default value of starting plot number to 1
		if (StringUtils.isEmpty(form.getStartingPlotNo())) {
			form.setStartingPlotNo(ImportGermplasmListController.STARTING_PLOT_NO);
		}

		form.changePage(1);
		this.userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

		this.userSelection.setImportedGermplasmMainInfo(mainInfo);
		this.userSelection.setImportValid(true);

		model.addAttribute(ImportGermplasmListController.CHECK_LISTS, this.fieldbookService.getCheckTypeList());
		model.addAttribute(ImportGermplasmListController.LIST_DATA_TABLE, dataTableDataList);
		model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST,
				this.getGermplasmTableHeader(this.userSelection.getPlotsLevelList()));
		model.addAttribute(ImportGermplasmListController.HAS_SAVED_GERMPLASM, this.hasSavedGermplasm(isNewList));
	}


	private List<TableHeader> getGermplasmTableHeader(final List<SettingDetail> factorsList) {
		final List<TableHeader> tableHeaderList = new ArrayList<>();

		if (factorsList != null) {
			// we iterate the map for dynamic header of studies
			for (final SettingDetail factorDetail : factorsList) {
				if (factorDetail != null && factorDetail.getVariable() != null
						&& !SettingsUtil.inHideVariableFields(factorDetail.getVariable().getCvTermId(),
								AppConstants.HIDE_GERMPLASM_DESCRIPTOR_HEADER_TABLE.getString()) && !factorDetail.isHidden()) {
					tableHeaderList.add(new TableHeader(factorDetail.getVariable().getName(),
							factorDetail.getVariable().getCvTermId()
									+ AppConstants.TABLE_HEADER_KEY_SUFFIX.getString()));
				}
			}
		}
		return tableHeaderList;
	}

	protected Boolean hasSavedGermplasm(final Boolean isNewList) {
		if (!isNewList) {
			final Integer studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
			if (studyId != null) {
				return this.studyEntryService.countStudyEntries(studyId) > 0;
			}
		}

		return false;
	}

	/**
	 * Gets the paginated list.
	 *
	 * @param pageNum
	 *            the page num
	 * @param form
	 *            the form
	 * @param model
	 *            the model
	 * @return the paginated list
	 */
	@RequestMapping(value = "/page/{pageNum}", method = RequestMethod.GET)
	public String getPaginatedList(@PathVariable final int pageNum,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {
		// this set the necessary info from the session variable

		form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
		form.setImportedGermplasm(this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList()
				.getImportedGermplasms());
		form.changePage(pageNum);
		this.userSelection.setCurrentPageGermplasmList(form.getCurrentPage());
		try {
			model.addAttribute(ImportGermplasmListController.CHECK_LISTS, this.fieldbookService.getCheckTypeList());
		} catch (final MiddlewareQueryException e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	@ModelAttribute("contextInfo")
	public ContextInfo getContextInfo() {
		return this.contextUtil.getContextInfoFromSession();
	}

	/**
	 * Copies the Germplasm List and Check list from userSelection to
	 * ImportGermplasmListForm
	 *
	 * @param userSelection
	 * @param form
	 */
	void copyImportedGermplasmFromUserSelectionToForm(final UserSelection userSelection,
			final ImportGermplasmListForm form) {

		if (userSelection.getImportedGermplasmMainInfo() != null) {

			form.setImportedGermplasmMainInfo(userSelection.getImportedGermplasmMainInfo());
			form.setImportedGermplasm(
					userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());

			// this is to keep track of the original list before merging
			// with the checks
			if (userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList() != null) {
				userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().copyImportedGermplasms();
			}

		}

		if (userSelection.getImportedCheckGermplasmMainInfo() != null) {

			form.setImportedCheckGermplasmMainInfo(userSelection.getImportedCheckGermplasmMainInfo());
			form.setImportedCheckGermplasm(userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList()
					.getImportedGermplasms());
		}

	}

	/**
	 * This will remove the Experimental Design Factor in workbook.
	 *
	 * @param conditions
	 */
	void addExperimentFactorToBeDeleted(final List<MeasurementVariable> conditions) {
		conditions.add(this.createMeasurementVariable(String.valueOf(TermId.EXPERIMENT_DESIGN_FACTOR.getId()), "",
				Operation.DELETE, PhenotypicType.TRIAL_ENVIRONMENT));
	}

	protected MeasurementVariable createMeasurementVariable(final String idToCreate, final String value,
			final Operation operation, final PhenotypicType role) {
		final StandardVariable stdvar = this.fieldbookMiddlewareService.getStandardVariable(Integer.valueOf(idToCreate),
				this.contextUtil.getCurrentProgramUUID());
		stdvar.setPhenotypicType(role);
		final MeasurementVariable var = new MeasurementVariable(Integer.valueOf(idToCreate), stdvar.getName(),
				stdvar.getDescription(), stdvar.getScale().getName(), stdvar.getMethod().getName(),
				stdvar.getProperty().getName(), stdvar.getDataType().getName(), value,
				stdvar.getPhenotypicType().getLabelList().get(0));
		var.setRole(role);
		var.setDataTypeId(stdvar.getDataType().getId());
		var.setFactor(false);
		var.setOperation(operation);
		return var;

	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}
}
