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
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.exception.FieldbookRequestValidationException;
import com.efficio.fieldbook.web.trial.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.trial.form.UpdateGermplasmCheckForm;
import org.generationcp.commons.constant.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.util.FieldbookListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This controller handles the 2nd step in the study manager process.
 *
 * @author Daniel Jao
 */
@Controller
@RequestMapping({ ImportGermplasmListController.URL,
		ImportGermplasmListController.URL_1, ImportGermplasmListController.URL_2 })
public class ImportGermplasmListController extends SettingsController {

	private static final String SUCCESS = "success";

	private static final String ERROR = "error";

	protected static final String TABLE_HEADER_LIST = "tableHeaderList";

	static final String TYPE2 = "type";

	static final String LIST_DATA_TABLE = "listDataTable";

	static final String CHECK_LISTS = "checkLists";

	protected static final String ENTRY_CODE = "entryCode";

	protected static final String SOURCE = "source";

	protected static final String CROSS = "cross";

	protected static final String CHECK = "check";

	protected static final String GID = "gid";

	protected static final String DESIG = "desig";

	protected static final String ENTRY = "entry";

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
		this.fieldbookService.saveStudyImportedCrosses(this.userSelection.getImportedCrossesId(), studyId);

		// for saving the list data project
		this.saveListDataProject(studyId);

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
	 * List data project data is the germplasm list that is attached to a study This method is saving the germplasm for this
	 * study
	 *
	 * @param studyId - the study id
	 */
	private void saveListDataProject(final int studyId) {

		final ImportedGermplasmMainInfo germplasmMainInfo = this.getUserSelection().getImportedGermplasmMainInfo();

		if (germplasmMainInfo != null && germplasmMainInfo.getListId() != null) {
			// we save the list
			// we need to create a new germplasm list
			final Integer listId = germplasmMainInfo.getListId();
			final List<ImportedGermplasm> projectGermplasmList;

			final ImportedGermplasmList importedGermplasmList = germplasmMainInfo.getImportedGermplasmList();

			projectGermplasmList = importedGermplasmList.getImportedGermplasms();

			final List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(projectGermplasmList);
			this.fieldbookMiddlewareService
				.saveOrUpdateListDataProject(studyId, GermplasmListType.STUDY, listId, listDataProject, this.getCurrentIbdbUserId());
		} else {
			// we delete the record in the db
			this.fieldbookMiddlewareService.deleteListDataProjects(studyId, GermplasmListType.STUDY);
		}

	}

	/**
	 * Displays the germplasm details of the list selected from the Browse List
	 * pop up in Germplasm and Checks tab.
	 *
	 * @param listId
	 *            the id of the germplasm list to be displayed
	 * @param form
	 *            - the form
	 * @param model
	 *            - the model
	 * @return the string
	 */
	@RequestMapping(value = "/displayGermplasmDetails/{listId}", method = RequestMethod.GET)
	public String displayGermplasmDetailsOfSelectedList(@PathVariable final Integer listId,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {
		try {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			form.setImportedGermplasmMainInfo(mainInfo);
			mainInfo.setListId(listId);
			final List<GermplasmListData> data = new ArrayList<>(this.germplasmListManager.getGermplasmListDataByListId(listId));
			FieldbookListUtil.populateStockIdInGermplasmListData(data, this.inventoryDataManager);
			final List<ImportedGermplasm> list = this.transformGermplasmListDataToImportedGermplasm(data, null);
			final String defaultTestCheckId = this.getCheckId(ImportGermplasmListController.DEFAULT_TEST_VALUE,
					this.fieldbookService.getCheckTypeList());
			form.setImportedGermplasm(list);

			final List<Map<String, Object>> dataTableDataList = this.generateGermplasmListDataTable(list, defaultTestCheckId, true);
			this.initializeObjectsForGermplasmDetailsView(form, model, mainInfo, list, dataTableDataList);
		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	/**
	 * Displays the assigned Germplasm List of the study
	 *
	 * @param form - the form
	 * @param model - the model
	 * @return the string
	 */
	@RequestMapping(value = "/displaySelectedGermplasmDetails", method = RequestMethod.GET)
	public String displayGermplasmDetailsOfCurrentStudy(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form,
		final Model model) {
		try {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			final Integer studyIdFromWorkbook = this.userSelection.getWorkbook().getStudyDetails().getId();
			final int studyId = studyIdFromWorkbook == null ? ImportGermplasmListController.NO_ID : studyIdFromWorkbook;

			List<ImportedGermplasm> list = new ArrayList<>();

			final GermplasmListType germplasmListType = GermplasmListType.STUDY;

			final List<GermplasmList> germplasmLists =
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, germplasmListType);

			if (germplasmLists != null && !germplasmLists.isEmpty()) {
				final GermplasmList germplasmList = germplasmLists.get(0);

				if (germplasmList != null && germplasmList.getListRef() != null) {
					form.setLastDraggedPrimaryList(germplasmList.getListRef().toString());
					// BMS-1419, set the id to the original list's id
					mainInfo.setListId(germplasmList.getListRef());
				}
				final List<ListDataProject> data =
					this.fieldbookMiddlewareService.getListDataProject(germplasmList != null ? germplasmList.getId() : null);
				FieldbookListUtil.populateStockIdInListDataProject(data, this.inventoryDataManager);
				list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
			}

			final String defaultTestCheckId =
				this.getCheckId(ImportGermplasmListController.DEFAULT_TEST_VALUE, this.fieldbookService.getCheckTypeList());
			form.setImportedGermplasm(list);

			final List<Map<String, Object>> dataTableDataList = this.generateGermplasmListDataTable(list, defaultTestCheckId, false);//
			this.initializeObjectsForGermplasmDetailsView(form, model, mainInfo, list, dataTableDataList);

			// setting the form
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setStudyId(studyId);
			form.setGermplasmListId(
					mainInfo.getListId() == null ? ImportGermplasmListController.NO_ID : mainInfo.getListId());
		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	List<Map<String, Object>> generateGermplasmListDataTable(final List<ImportedGermplasm> list, final String defaultTestCheckId,
		final boolean isDefaultTestCheck) {
		final List<Map<String, Object>> dataTableDataList = new ArrayList<>();
		final List<Enumeration> checkList = this.fieldbookService.getCheckTypeList();

		for (final ImportedGermplasm germplasm : list) {
			final Map<String, Object> dataMap = new HashMap<>();

			dataMap.put(ImportGermplasmListController.POSITION, germplasm.getIndex().toString());
			dataMap.put(ImportGermplasmListController.CHECK_OPTIONS, checkList);
			dataMap.put(ImportGermplasmListController.ENTRY, germplasm.getEntryId().toString());
			dataMap.put(ImportGermplasmListController.DESIG, germplasm.getDesig());
			dataMap.put(ImportGermplasmListController.GID, germplasm.getGid());
			dataMap.put(ImportGermplasmListController.ENTRY_CODE, germplasm.getEntryCode());

			if (isDefaultTestCheck || germplasm.getEntryTypeValue() == null || "0".equals(germplasm.getEntryTypeValue())) {
				germplasm.setEntryTypeValue(defaultTestCheckId);
				germplasm.setEntryTypeCategoricalID(Integer.valueOf(defaultTestCheckId));
				dataMap.put(ImportGermplasmListController.CHECK, defaultTestCheckId);
			} else {
				dataMap.put(ImportGermplasmListController.CHECK, germplasm.getEntryTypeCategoricalID());
			}

			final List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
			if (factorsList != null) {
				// we iterate the map for dynamic header of study
				for (final SettingDetail factorDetail : factorsList) {
					if (factorDetail != null && factorDetail.getVariable() != null) {
						dataMap.put(factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString(),
							this.getGermplasmData(factorDetail.getVariable().getCvTermId().toString(), germplasm));
					}
				}
			}
			dataTableDataList.add(dataMap);
		}

		return dataTableDataList;
	}

	void initializeObjectsForGermplasmDetailsView(final ImportGermplasmListForm form,
		final Model model, final ImportedGermplasmMainInfo mainInfo, final List<ImportedGermplasm> list,
		final List<Map<String, Object>> dataTableDataList) {
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

	private String getGermplasmData(final String termId, final ImportedGermplasm germplasm) {
		String val = "";
		if (NumberUtils.isNumber(termId)) {
			final int term = Integer.parseInt(termId);
			if (term == TermId.GID.getId()) {
				val = germplasm.getGid();
			} else if (term == TermId.ENTRY_CODE.getId()) {
				val = germplasm.getEntryCode();
			} else if (term == TermId.ENTRY_NO.getId()) {
				val = germplasm.getEntryId().toString();
			} else if (term == TermId.SOURCE.getId() || term == TermId.GERMPLASM_SOURCE.getId()) {
				val = germplasm.getSource();
			} else if (term == TermId.CROSS.getId()) {
				val = germplasm.getCross();
			} else if (term == TermId.DESIG.getId()) {
				val = germplasm.getDesig();
			} else if (term == TermId.CHECK.getId()) {
				val = germplasm.getEntryTypeValue();
			} else if (term == TermId.GROUPGID.getId()) {
				val = germplasm.getMgid().toString();
			} else if (term == TermId.STOCKID.getId()) {
				val = germplasm.getStockIDs();
			}
		}
		return val;
	}

	@RequestMapping(value = "/refreshListDetails", method = RequestMethod.GET)
	public String refreshListDetails(final Model model,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form) {

		try {
			if (this.getUserSelection().getImportedGermplasmMainInfo() != null) {
				final String type = GermplasmListType.STUDY.toString();
				final List<Map<String, Object>> dataTableDataList = new ArrayList<>();
				final List<Enumeration> checkList = this.fieldbookService.getCheckTypeList();
				final List<ImportedGermplasm> list =
						this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

				// we need to take note of the check here

				for (final ImportedGermplasm germplasm : list) {
					final Map<String, Object> dataMap = new HashMap<>();
					dataMap.put(ImportGermplasmListController.POSITION, germplasm.getIndex().toString());
					dataMap.put(ImportGermplasmListController.CHECK_OPTIONS, checkList);
					dataMap.put(ImportGermplasmListController.ENTRY, germplasm.getEntryId().toString());
					dataMap.put(ImportGermplasmListController.DESIG, germplasm.getDesig());
					dataMap.put(ImportGermplasmListController.GID, germplasm.getGid());
					dataMap.put(ImportGermplasmListController.GROUP_ID, germplasm.getMgid());

					final List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
					if (factorsList != null) {
						// we iterate the map for dynamic header of trial
						for (final SettingDetail factorDetail : factorsList) {
							if (factorDetail != null && factorDetail.getVariable() != null) {
								dataMap.put(factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString(),
										this.getGermplasmData(factorDetail.getVariable().getCvTermId().toString(), germplasm));
							}
						}
					}
					dataMap.put(ImportGermplasmListController.CHECK,
							germplasm.getEntryTypeValue() != null ? germplasm.getEntryTypeValue() : "");

					dataTableDataList.add(dataMap);
				}

				model.addAttribute(ImportGermplasmListController.CHECK_LISTS, checkList);
				model.addAttribute(ImportGermplasmListController.LIST_DATA_TABLE, dataTableDataList);
				model.addAttribute(ImportGermplasmListController.TYPE2, type);
				model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST, this.getGermplasmTableHeader(this.userSelection.getPlotsLevelList()));
				model.addAttribute("hasMeasurement", this.hasMeasurement());

				form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
				form.setImportedGermplasm(list);
			}

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	protected Boolean hasMeasurement() {
		return this.userSelection.getMeasurementRowList() != null
				&& !this.userSelection.getMeasurementRowList().isEmpty();
	}

	private String getCheckId(final String checkCode, final List<Enumeration> checksList) {

		String checkId = "";

		for (final Enumeration enumVar : checksList) {
			if (enumVar.getName().equalsIgnoreCase(checkCode)) {
				checkId = enumVar.getId().toString();
				break;
			}
		}
		return checkId;
	}

	@RequestMapping(value = "/edit/check/{index}/{dataTableIndex}", method = RequestMethod.GET)
	public String editCheck(@ModelAttribute("updatedGermplasmCheckForm") final UpdateGermplasmCheckForm form, final Model model,
			@PathVariable final int index, @PathVariable final int dataTableIndex,
			@RequestParam(value = "currentVal") final String currentVal) {

		try {
			final ImportedGermplasm importedCheckGermplasm =
					this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
							.get(dataTableIndex);
			importedCheckGermplasm.setEntryTypeValue(currentVal);
			final List<Enumeration> allEnumerations = this.fieldbookService.getCheckTypeList();

			model.addAttribute("allCheckTypes", allEnumerations);
			form.setCheckVal(currentVal);
			form.setIndex(index);
			form.setDataTableIndex(dataTableIndex);
		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.EDIT_CHECK);
	}

	@ResponseBody
	@RequestMapping(value = "/update/check", method = RequestMethod.POST)
	public String updateCheck(@ModelAttribute("updatedGermplasmCheckForm") final UpdateGermplasmCheckForm form,
			final Model model) {

		try {
			final ImportedGermplasm importedCheckGermplasm =
					this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
							.get(form.getIndex());
			importedCheckGermplasm.setEntryTypeValue(form.getCheckVal());
			importedCheckGermplasm.setEntryTypeCategoricalID(Integer.valueOf(form.getCheckVal()));

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return ImportGermplasmListController.SUCCESS;
	}

	/**
	 * Reset check germplasm details.
	 *
	 * @param model
	 *            the model
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/resetStudyGermplasmDetails", method = RequestMethod.GET)
	public String resetStudyGermplasmDetails(final Model model) {

		try {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			final List<ImportedGermplasm> list = new ArrayList<>();

			final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			this.getUserSelection().setImportedGermplasmMainInfo(null);
			this.getUserSelection().setImportValid(true);

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return ImportGermplasmListController.SUCCESS;
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


	/**
	 * Transform germplasm list data to imported germplasm.
	 *
	 * @param data
	 *            the data
	 * @param defaultCheckId
	 *            the default check id
	 * @return the list
	 */
	private List<ImportedGermplasm> transformGermplasmListDataToImportedGermplasm(final List<GermplasmListData> data,
			final String defaultCheckId) {
		final List<ImportedGermplasm> list = new ArrayList<>();
		int index = 1;
		if (data != null && !data.isEmpty()) {
			for (final GermplasmListData aData : data) {
				final ImportedGermplasm germplasm = new ImportedGermplasm();
				germplasm.setEntryTypeValue(defaultCheckId);
				germplasm.setCross(aData.getGroupName());
				germplasm.setDesig(aData.getDesignation());
				germplasm.setEntryCode(aData.getEntryCode());
				germplasm.setEntryId(aData.getEntryId());
				germplasm.setGid(aData.getGid().toString());
				germplasm.setMgid(aData.getGroupId()); // set Group_Id from
														// germplasm
				germplasm.setSource(aData.getSeedSource());
				germplasm.setGroupName(aData.getGroupName());
				germplasm.setGroupId(aData.getGroupId());
				germplasm.setStockIDs(aData.getStockIDs());
				germplasm.setIndex(index++);

				list.add(germplasm);
			}
		}
		return list;
	}

	/**
	 * Gets the all check types.
	 *
	 * @return the all check types
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllCheckTypes", method = RequestMethod.GET)
	public Map<String, String> getAllCheckTypes() {
		final Map<String, String> result = new HashMap<>();

		try {
			final List<Enumeration> allEnumerations = this.fieldbookService.getCheckTypeList();
			result.put(ImportGermplasmListController.SUCCESS, "1");
			result.put("allCheckTypes", this.convertObjectToJson(allEnumerations));

		} catch (final MiddlewareException e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
			result.put(ImportGermplasmListController.SUCCESS, "-1");
		}

		return result;
	}

	/**
	 * Adds the update check type.
	 *
	 * @param operation
	 *            the operation
	 * @param form
	 *            the form
	 * @param local
	 *            the local
	 * @return the map
	 */
	@ResponseBody
	@RequestMapping(value = "/addUpdateCheckType/{operation}", method = RequestMethod.POST)
	public Map<String, String> addUpdateCheckType(@PathVariable final int operation,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Locale local) {
		final Map<String, String> result = new HashMap<>();

		try {
			final StandardVariable stdVar =
					this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID());
			final Enumeration enumeration;
			final String message;
			if (operation == 1) {
				enumeration = new Enumeration(null, form.getManageCheckCode(), form.getManageCheckValue(), 0);
				message = this.messageSource.getMessage("study.manage.check.types.add.success", new Object[] {form.getManageCheckValue()},
						local);
			} else {
				enumeration = stdVar.getEnumeration(Integer.parseInt(form.getManageCheckCode()));
				enumeration.setDescription(form.getManageCheckValue());
				message = this.messageSource.getMessage("study.manage.check.types.edit.success", new Object[] {enumeration.getName()},
						local);
			}
			if (!this.validateEnumerationDescription(stdVar.getEnumerations(), enumeration)) {
				result.put(ImportGermplasmListController.SUCCESS, "-1");
				result.put(ImportGermplasmListController.ERROR,
						this.messageSource.getMessage("error.add.check.duplicate.description", null, local));
			} else {
				this.ontologyService.saveOrUpdateStandardVariableEnumeration(stdVar, enumeration);
				final List<Enumeration> allEnumerations = this.ontologyService
						.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID()).getEnumerations();
				this.updateUserSelection();

				result.put("checkTypes", this.convertObjectToJson(allEnumerations));

				result.put(ImportGermplasmListController.SUCCESS, "1");
				result.put("successMessage", message);
			}

		} catch (final MiddlewareQueryException e) {
			ImportGermplasmListController.LOG.debug(e.getMessage(), e);
			result.put(ImportGermplasmListController.SUCCESS, "-1");
			result.put(ImportGermplasmListController.ERROR, e.getMessage());
		} catch (final MiddlewareException e) {
			ImportGermplasmListController.LOG.debug(e.getMessage(), e);
			result.put(ImportGermplasmListController.SUCCESS, "-1");
		}

		return result;
	}

	private void updateUserSelection() {
		for (final SettingDetail settingDetail: this.userSelection.getPlotsLevelList()) {
			if (settingDetail.getVariable().getCvTermId().equals(TermId.ENTRY_TYPE.getId())) {
				final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(TermId.ENTRY_TYPE.getId(), true);
				settingDetail.setPossibleValues(possibleValues);

				settingDetail.setPossibleValuesToJson(possibleValues);
				final List<ValueReference> possibleValuesFavorite =
						this.fieldbookService.getAllPossibleValuesFavorite(settingDetail.getVariable().getCvTermId(), this.getCurrentProject().getUniqueID(), true);
				settingDetail.setPossibleValuesFavorite(possibleValuesFavorite);
				settingDetail.setPossibleValuesFavoriteToJson(possibleValuesFavorite);

				final List<ValueReference> allValues = this.fieldbookService.getAllPossibleValuesWithFilter(settingDetail.getVariable().getCvTermId(), false);
				settingDetail.setAllValues(allValues);
				settingDetail.setAllValuesToJson(allValues);

				final List<ValueReference> allFavoriteValues =
						this.fieldbookService.getAllPossibleValuesFavorite(settingDetail.getVariable().getCvTermId(),
								this.getCurrentProject().getUniqueID(), null);

				final List<ValueReference> intersection = SettingsUtil.intersection(allValues, allFavoriteValues);

				settingDetail.setAllFavoriteValues(intersection);
				settingDetail.setAllFavoriteValuesToJson(intersection);

				break;
			}
		}
	}

	/**
	 * Delete check type.
	 *
	 * @param form
	 *            the form
	 * @param local
	 *            the local
	 * @return the map
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteCheckType", method = RequestMethod.POST)
	public Map<String, String> deleteCheckType(
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Locale local) {
		final Map<String, String> result = new HashMap<>();

		try {
			final String name = this.ontologyService
					.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID())
					.getEnumeration(Integer.parseInt(form.getManageCheckCode())).getName();

			if (!this.ontologyService.validateDeleteStandardVariableEnumeration(TermId.CHECK.getId(),
					Integer.parseInt(form.getManageCheckCode()))) {
				result.put(ImportGermplasmListController.SUCCESS, "-1");
				result.put(ImportGermplasmListController.ERROR,
						this.messageSource.getMessage("study.manage.check.types.delete.error", new Object[] {name}, local));
			} else {
				this.ontologyService.deleteStandardVariableValidValue(TermId.CHECK.getId(),
						Integer.parseInt(form.getManageCheckCode()));
				result.put(ImportGermplasmListController.SUCCESS, "1");
				result.put("successMessage",
						this.messageSource.getMessage("study.manage.check.types.delete.success", new Object[] {name}, local));
				final List<Enumeration> allEnumerations = this.ontologyService
						.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID())
						.getEnumerations();
				result.put("checkTypes", this.convertObjectToJson(allEnumerations));
			}

		} catch (final MiddlewareException e) {
			ImportGermplasmListController.LOG.debug(e.getMessage(), e);
			result.put(ImportGermplasmListController.SUCCESS, "-1");
			result.put(ImportGermplasmListController.ERROR, e.getMessage());
		}

		return result;
	}

	/**
	 * Gets the check type list.
	 *
	 * @return the check type list
	 */
	@ModelAttribute("checkTypes")
	public List<Enumeration> getCheckTypes() {
		return this.fieldbookService.getCheckTypeList();
	}

	@ModelAttribute("contextInfo")
	public ContextInfo getContextInfo() {
		return this.contextUtil.getContextInfoFromSession();
	}

	private boolean validateEnumerationDescription(final List<Enumeration> enumerations,
			final Enumeration newEnumeration) {
		if (enumerations != null && !enumerations.isEmpty() && newEnumeration != null
				&& newEnumeration.getDescription() != null) {
			for (final Enumeration enumeration : enumerations) {
				if (enumeration.getDescription() != null
						&& newEnumeration.getDescription().trim().equalsIgnoreCase(enumeration.getDescription().trim())
						&& !enumeration.getId().equals(newEnumeration.getId())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Updates the Check value and Check ID of Imported Germplasm based on the
	 * Selected Checks from ImportGermplasmListForm
	 *
	 * @param userSelection
	 * @param form
	 */
	void processChecks(final UserSelection userSelection, final ImportGermplasmListForm form) {

		final String[] selectedCheck = form.getSelectedCheck();

		if (selectedCheck != null && selectedCheck.length != 0) {

			ImportedGermplasmMainInfo importedGermplasmMainInfoToUse = userSelection
					.getImportedCheckGermplasmMainInfo();
			if (importedGermplasmMainInfoToUse == null) {

				// since for trial, we are using only the original info
				importedGermplasmMainInfoToUse = userSelection.getImportedGermplasmMainInfo();
			}
			if (importedGermplasmMainInfoToUse != null) {
				for (int i = 0; i < selectedCheck.length; i++) {
					if (NumberUtils.isNumber(selectedCheck[i])) {
						importedGermplasmMainInfoToUse.getImportedGermplasmList().getImportedGermplasms().get(i)
								.setEntryTypeValue(selectedCheck[i]);
						importedGermplasmMainInfoToUse.getImportedGermplasmList().getImportedGermplasms().get(i)
								.setEntryTypeCategoricalID(Integer.parseInt(selectedCheck[i]));
					}
				}
			}
		} else {
			// we set the check to null
			if (userSelection.getImportedGermplasmMainInfo() != null
					&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList() != null
					&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList()
							.getImportedGermplasms() != null) {

				// this is to keep track of the original list before merging
				// with the checks
				for (final ImportedGermplasm germplasm : userSelection.getImportedGermplasmMainInfo()
						.getImportedGermplasmList().getImportedGermplasms()) {
					germplasm.setEntryTypeCategoricalID(null);
					germplasm.setEntryTypeValue("");
				}
			}
		}

		// end: section for taking note of the check germplasm
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

	Integer computeTotalExpectedWithChecks(final ImportGermplasmListForm form) {

		final int totalGermplasmCount = this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList()
				.getImportedGermplasms().size();
		Integer checkInterval = null;
		Integer startCheckFrom = null;

		if (form.getCheckVariables() != null) {
			for (final SettingDetail settingDetail : form.getCheckVariables()) {
				if (Objects.equals(settingDetail.getVariable().getCvTermId(), TermId.CHECK_START.getId())) {
					startCheckFrom = org.generationcp.middleware.util.StringUtil.parseInt(settingDetail.getValue(),
							null);
				}
				if (Objects.equals(settingDetail.getVariable().getCvTermId(), TermId.CHECK_INTERVAL.getId())) {
					checkInterval = org.generationcp.middleware.util.StringUtil.parseInt(settingDetail.getValue(),
							null);
				}
			}

			if (checkInterval != null && startCheckFrom != null) {
				final int totalCount = (totalGermplasmCount - startCheckFrom) / checkInterval;
				return totalCount + totalGermplasmCount + 1;
			}
		}

		return totalGermplasmCount;

	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}
}
