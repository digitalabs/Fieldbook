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

package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.exception.FieldbookRequestValidationException;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.form.UpdateGermplasmCheckForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import com.hazelcast.util.StringUtil;

/**
 * This controller handles the 2nd step in the nursery manager process.
 *
 * @author Daniel Jao
 */
@Controller
@RequestMapping({ImportGermplasmListController.URL, ImportGermplasmListController.URL_2, ImportGermplasmListController.URL_3,
		ImportGermplasmListController.URL_4})
public class ImportGermplasmListController extends SettingsController {

	private static final String SUCCESS = "success";

	private static final String ERROR = "error";

	protected static final String TABLE_HEADER_LIST = "tableHeaderList";

	protected static final String TYPE2 = "type";

	protected static final String LIST_DATA_TABLE = "listDataTable";

	protected static final String HAS_EXPERIMENTAL_DESIGN = "hasExperimentalDesign";

	protected static final String CHECK_LISTS = "checkLists";

	protected static final String ENTRY_CODE = "entryCode";

	protected static final String SOURCE = "source";

	protected static final String CROSS = "cross";

	protected static final String CHECK = "check";

	protected static final String GID = "gid";

	protected static final String DESIG = "desig";

	protected static final String ENTRY = "entry";

	protected static final String CHECK_OPTIONS = "checkOptions";

	protected static final String POSITION = "position";

	protected static final String GROUP_ID = "groupId";

	protected static final Integer MAX_ENTRY_NO = 99999;

	protected static final Integer MAX_PLOT_NO = 99999999;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(ImportGermplasmListController.class);

	/** The Constant URL. */
	public static final String URL = "/NurseryManager/importGermplasmList";
	public static final String URL_2 = "/NurseryManager/GermplasmList";
	public static final String URL_3 = "/TrialManager/GermplasmList";
	public static final String URL_4 = "/ListManager/GermplasmList";

	/** The Constant PAGINATION_TEMPLATE. */
	public static final String PAGINATION_TEMPLATE = "/NurseryManager/showGermplasmPagination";
	public static final String EDIT_CHECK = "/Common/editCheck";

	/** The Constant CHECK_PAGINATION_TEMPLATE. */
	public static final String CHECK_PAGINATION_TEMPLATE = "/NurseryManager/showCheckGermplasmPagination";

	public static final int NO_ID = -1;

	static final String STARTING_PLOT_NO = "1";

	/** The germplasm list manager. */
	@Resource
	private GermplasmListManager germplasmListManager;

	/** The import germplasm file service. */
	@Resource
	private ImportGermplasmFileService importGermplasmFileService;

	/** The data import service. */
	@Resource
	private DataImportService dataImportService;

	/** The measurements generator service. */
	@Resource
	private MeasurementsGeneratorService measurementsGeneratorService;

	/** The ontology service. */
	@Resource
	private OntologyService ontologyService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	/** The merge check service. */
	@Resource
	private MergeCheckService mergeCheckService;

	/** The message source. */
	@Autowired
	public MessageSource messageSource;

	/** The Inventory list manager. */
	@Resource
	private InventoryDataManager inventoryDataManager;

	private static String DEFAULT_CHECK_VALUE = "C";
	private static String DEFAULT_TEST_VALUE = "T";

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName ()
	 */
	@Override
	public String getContentName() {
		return "NurseryManager/importGermplasmList";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController# getUserSelection ()
	 */
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
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String show(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {
		// this set the necessary info from the session variable

		form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
		if (this.getUserSelection().getImportedGermplasmMainInfo() != null
				&& this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null) {
			// this would be use to display the imported germplasm info
			form.setImportedGermplasm(
					this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
			form.setGermplasmListId(this.getUserSelection().getImportedGermplasmMainInfo().getListId());

			form.changePage(1);
			this.userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

		}
		return super.show(model);
	}

	/**
	 * Goes to the Next screen. Added validation if a germplasm list was properly uploaded
	 *
	 * @param form the form
	 * @param result the result
	 * @param model the model
	 * @return the string
	 * @throws MiddlewareQueryException the middleware query exception
	 */
	@ResponseBody
	@RequestMapping(value = {"/next", "/submitAll"}, method = RequestMethod.POST)
	@Transactional
	public String nextScreen(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final BindingResult result,
			final Model model, final HttpServletRequest req) throws BVDesignException {
		// start: section for taking note of the check germplasm
		boolean isDeleteObservations = false;

		final boolean isNursery = this.userSelection.getWorkbook().getStudyDetails().getStudyType() == StudyType.N;
		boolean hasTemporaryWorkbook = false;

		if (this.userSelection.getTemporaryWorkbook() != null) {

			WorkbookUtil.manageExpDesignVariablesAndObs(this.userSelection.getWorkbook(), this.userSelection.getTemporaryWorkbook());
			WorkbookUtil.addMeasurementDataToRowsExp(this.userSelection.getWorkbook().getFactors(),
					this.userSelection.getWorkbook().getObservations(), false, this.ontologyService, this.fieldbookService,
					this.contextUtil.getCurrentProgramUUID());
			WorkbookUtil.addMeasurementDataToRowsExp(this.userSelection.getWorkbook().getVariates(),
					this.userSelection.getWorkbook().getObservations(), true, this.ontologyService, this.fieldbookService,
					this.contextUtil.getCurrentProgramUUID());

			this.addVariablesFromTemporaryWorkbookToWorkbook(this.userSelection);

			this.updateObservationsFromTemporaryWorkbookToWorkbook(this.userSelection);

			this.userSelection.setTemporaryWorkbook(null);

			hasTemporaryWorkbook = true;
			isDeleteObservations = true;

		}

		// if we have no germplasm list available for the nursery, skip this
		// validation flow
		if (null != this.getUserSelection().getImportedGermplasmMainInfo()
				&& null != this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList()) {
			this.assignAndIncrementEntryNumberAndPlotNumber(form);

			// NOTE: clearing measurements if germplasm list is null
			if (this.userSelection.getImportedGermplasmMainInfo() == null && this.userSelection.getMeasurementRowList() != null) {
				this.userSelection.getMeasurementRowList().clear();
			}

			if (isNursery && !hasTemporaryWorkbook) {

				this.validateEntryAndPlotNo(form);

				this.processImportedGermplasmAndChecks(this.userSelection, form);

			} else if (!hasTemporaryWorkbook) {
				// this section of code is only called for existing trial
				// without temporary workbook. No need for reset of measurement
				// row
				// list here
				isDeleteObservations = true;
			}
		}

		this.userSelection.getWorkbook().setObservations(this.userSelection.getMeasurementRowList());

		this.fieldbookService.createIdCodeNameVariablePairs(this.userSelection.getWorkbook(),
				AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
		this.fieldbookService.createIdNameVariablePairs(this.userSelection.getWorkbook(), new ArrayList<SettingDetail>(),
				AppConstants.ID_NAME_COMBINATION.getString(), true);
		final int studyId = this.dataImportService.saveDataset(this.userSelection.getWorkbook(), true, isDeleteObservations,
				this.getCurrentProject().getUniqueID(), this.getCurrentProject().getCropType().getPlotCodePrefix());
		this.fieldbookService.saveStudyImportedCrosses(this.userSelection.getImportedCrossesId(), studyId);

		// for saving the list data project
		this.saveListDataProject(isNursery, studyId);

		this.fieldbookService.saveStudyColumnOrdering(studyId, this.userSelection.getWorkbook().getStudyName(), form.getColumnOrders(),
				this.userSelection.getWorkbook());

		return Integer.toString(studyId);
	}

	/**
	 * Setting Entry Number and plot number to user selection and increment user given entry number in germplasm list. Clearing Measurements
	 * list if germplasm sheet is not uploaded.
	 *
	 * @param form
	 */
	protected void validateEntryAndPlotNo(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form) {

		final Integer startingEntryNumber = org.generationcp.middleware.util.StringUtil.parseInt(form.getStartingEntryNo(), null);

		if (startingEntryNumber != null) {
			final Integer totalExpectedEntryNumber = startingEntryNumber
					+ this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().size() - 1;

			if (totalExpectedEntryNumber > ImportGermplasmListController.MAX_ENTRY_NO) {
				throw new FieldbookRequestValidationException("entry.number.should.not.exceed");
			}
		}

		final Integer totalExpectedNumber = this.computeTotalExpectedWithChecks(form);
		final Integer plotNo = org.generationcp.middleware.util.StringUtil.parseInt(form.getStartingPlotNo(), null);

		if (plotNo != null) {
			final Integer totalMeasurement = totalExpectedNumber + plotNo - 1;

			if (totalMeasurement > ImportGermplasmListController.MAX_PLOT_NO) {
				throw new FieldbookRequestValidationException("plot.number.should.not.exceed");
			}
		}
	}

	// NOTE: BMS-1929 Setting custom entry and plot number in user selection as
	// well as updating entry number in imported germplasm list
	void assignAndIncrementEntryNumberAndPlotNumber(final ImportGermplasmListForm form) {
		if (this.userSelection.getImportedGermplasmMainInfo() != null) {

			// Taking entryNumber as null if not supplied
			Integer entryNo = null;
			if (form.getStartingEntryNo() != null) {
				entryNo = org.generationcp.middleware.util.StringUtil.parseInt(form.getStartingEntryNo(), null);
				if (entryNo == null) {
					throw new FieldbookRequestValidationException("entry.number.should.be.in.range");

				}
			}

			Integer plotNo = null;
			if (form.getStartingPlotNo() != null) {
				plotNo = org.generationcp.middleware.util.StringUtil.parseInt(form.getStartingPlotNo(), null);
				if (plotNo == null) {
					throw new FieldbookRequestValidationException("plot.number.should.be.in.range");

				}
			}

			this.userSelection.setStartingEntryNo(entryNo);

			// Setting plot number in user selection as it will be used later
			this.userSelection.setStartingPlotNo(plotNo);

			// Skip applying entry number as it is null. So no change in list
			if (entryNo == null) {
				return;
			}

			final ImportedGermplasmMainInfo importedGermplasmMainInfo = this.userSelection.getImportedGermplasmMainInfo();

			// This will be used when updating the checks. Essentially the first
			// entry number. This can be done using a get(0) on the list
			// but this is safer.
			Integer minOriginalEntryNumber = null;
			if (importedGermplasmMainInfo != null && importedGermplasmMainInfo.getImportedGermplasmList() != null) {
				for (final ImportedGermplasm g : importedGermplasmMainInfo.getImportedGermplasmList().getImportedGermplasms()) {
					minOriginalEntryNumber = this.getMinimumEntryNumber(minOriginalEntryNumber, g);
					g.setEntryId(entryNo++);
				}
			}

			// This part of the code is only relevant when crating checks from
			// an existing list.

			// First calculate the number that needs to be added to existing
			// check numbers.
			final Integer differenceToAppendToChecks;
			if (minOriginalEntryNumber == null) {
				differenceToAppendToChecks = this.userSelection.getStartingEntryNo();
			} else {
				differenceToAppendToChecks = this.userSelection.getStartingEntryNo() - minOriginalEntryNumber;
			}

			final ImportedGermplasmMainInfo importedCheckGermplasmMainInfo = this.userSelection.getImportedCheckGermplasmMainInfo();
			final List<ImportedGermplasm> checkList;
			if (importedCheckGermplasmMainInfo != null && importedCheckGermplasmMainInfo.getImportedGermplasmList() != null
					&& (checkList = importedCheckGermplasmMainInfo.getImportedGermplasmList().getImportedGermplasms()) != null) {
				for (final ImportedGermplasm importedGermplasm : checkList) {
					importedGermplasm.setEntryId(importedGermplasm.getEntryId() + differenceToAppendToChecks);
				}
			}

		}
	}

	private Integer getMinimumEntryNumber(final Integer currentMinimumEntryNumber, final ImportedGermplasm currentGermplasm) {
		final Integer currentEntryId = currentGermplasm.getEntryId();
		if (currentMinimumEntryNumber == null || currentMinimumEntryNumber > currentGermplasm.getEntryId()) {
			return currentEntryId;
		}
		return currentMinimumEntryNumber;
	}

	private int getIntervalValue(final ImportGermplasmListForm form) {
		final String interval = SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_INTERVAL.getId());
		if (interval != null && !"".equals(interval)) {
			return Integer.parseInt(interval);
		}
		return 0;
	}

	/**
	 * List data project data is the germplasm list that is attached to a nursery or a trial This method is saving the germplasm for this
	 * nursery/trial
	 *
	 * @param isNursery
	 * @param studyId
	 * @throws MiddlewareQueryException
	 */
	private void saveListDataProject(final boolean isNursery, final int studyId) {

		final ImportedGermplasmMainInfo germplasmMainInfo = this.getUserSelection().getImportedGermplasmMainInfo();

		if (germplasmMainInfo != null && germplasmMainInfo.getListId() != null) {
			// we save the list
			// we need to create a new germplasm list
			final Integer listId = germplasmMainInfo.getListId();
			List<ImportedGermplasm> projectGermplasmList;

			final ImportedGermplasmList importedGermplasmList = germplasmMainInfo.getImportedGermplasmList();

			if (isNursery && importedGermplasmList.getOriginalImportedGermplasms() != null) {
				projectGermplasmList = importedGermplasmList.getOriginalImportedGermplasms();
			} else {
				projectGermplasmList = importedGermplasmList.getImportedGermplasms();
			}

			final List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(projectGermplasmList);
			this.fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId,
					isNursery ? GermplasmListType.NURSERY : GermplasmListType.TRIAL, listId, listDataProject, this.getCurrentIbdbUserId());
		} else {
			// we delete the record in the db
			this.fieldbookMiddlewareService.deleteListDataProjects(studyId,
					isNursery ? GermplasmListType.NURSERY : GermplasmListType.TRIAL);
		}

		if (this.getUserSelection().getImportedCheckGermplasmMainInfo() != null) {
			if (this.getUserSelection().getImportedCheckGermplasmMainInfo().getListId() != null) {
				// came from a list
				final Integer listId = this.getUserSelection().getImportedCheckGermplasmMainInfo().getListId();
				final List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(
						this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
				this.fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, GermplasmListType.CHECK, listId, listDataProject,
						this.getCurrentIbdbUserId());

			} else if (this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList() != null
					&& this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList()
							.getImportedGermplasms() != null
					&& !this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
							.isEmpty()) {
				final List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(
						this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
				this.fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, GermplasmListType.CHECK, null, listDataProject,
						this.getCurrentIbdbUserId());

			} else {
				// we delete it
				this.fieldbookMiddlewareService.deleteListDataProjects(studyId, GermplasmListType.CHECK);
			}
		} else {
			if (isNursery) {
				// we delete it
				this.fieldbookMiddlewareService.deleteListDataProjects(studyId, GermplasmListType.CHECK);
			}
		}
	}

	/**
	 * Displays the germplasm details of the list selected from the Browse List pop up in Germplasm and Checks tab.
	 *
	 * @param listId the id of the germplasm list to be displayed
	 * @param form - the form
	 * @param model - the model
	 * @return the string
	 */
	@RequestMapping(value = "/displayGermplasmDetails/{listId}/{type}", method = RequestMethod.GET)
	public String displayGermplasmDetailsOfSelectedList(@PathVariable final Integer listId, @PathVariable final String type,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {
		try {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			form.setImportedGermplasmMainInfo(mainInfo);
			mainInfo.setListId(listId);
			final List<GermplasmListData> data = new ArrayList<>();
			data.addAll(this.germplasmListManager.getGermplasmListDataByListId(listId));
			FieldbookListUtil.populateStockIdInGermplasmListData(data, this.inventoryDataManager);
			final List<ImportedGermplasm> list = this.transformGermplasmListDataToImportedGermplasm(data, null);
			final String defaultTestCheckId =
					this.getCheckId(ImportGermplasmListController.DEFAULT_TEST_VALUE, this.fieldbookService.getCheckTypeList());
			form.setImportedGermplasm(list);

			final boolean isNursery = type != null && type.equalsIgnoreCase(StudyType.N.getName()) ? true : false;
			final List<Map<String, Object>> dataTableDataList =
					this.generateGermplasmListDataTable(list, defaultTestCheckId, isNursery, true);
			this.initializeObjectsForGermplasmDetailsView(type, form, model, mainInfo, list, dataTableDataList);
		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	/**
	 * Displays the assigned Germplasm List of the study
	 *
	 * @param type - Study type (N/T)
	 * @param form - the form
	 * @param model - the model
	 * @return the string
	 */
	@RequestMapping(value = "/displaySelectedGermplasmDetails/{type}", method = RequestMethod.GET)
	public String displayGermplasmDetailsOfCurrentStudy(@PathVariable final String type,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {
		try {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			final Integer studyIdFromWorkbook = this.userSelection.getWorkbook().getStudyDetails().getId();
			final int studyId = studyIdFromWorkbook == null ? ImportGermplasmListController.NO_ID : studyIdFromWorkbook;

			List<ImportedGermplasm> list = new ArrayList<>();

			boolean isNursery = false;
			GermplasmListType germplasmListType = null;
			if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {
				isNursery = true;
				germplasmListType = GermplasmListType.NURSERY;
			} else if (type != null && type.equalsIgnoreCase(StudyType.T.getName())) {
				isNursery = false;
				germplasmListType = GermplasmListType.TRIAL;
			}

			final List<GermplasmList> germplasmLists =
					this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, germplasmListType);

			if (germplasmLists != null && !germplasmLists.isEmpty()) {
				final GermplasmList germplasmList = germplasmLists.get(0);

				if (germplasmList != null && germplasmList.getListRef() != null) {
					form.setLastDraggedPrimaryList(germplasmList.getListRef().toString());
					// BMS-1419, set the id to the original list's id
					mainInfo.setListId(germplasmList.getListRef());
				}
				final List<ListDataProject> data = this.fieldbookMiddlewareService.getListDataProject(germplasmList.getId());
				FieldbookListUtil.populateStockIdInListDataProject(data, this.inventoryDataManager);
				list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
			}

			final String defaultTestCheckId =
					this.getCheckId(ImportGermplasmListController.DEFAULT_TEST_VALUE, this.fieldbookService.getCheckTypeList());
			form.setImportedGermplasm(list);

			final List<Map<String, Object>> dataTableDataList =
					this.generateGermplasmListDataTable(list, defaultTestCheckId, isNursery, false);
			this.initializeObjectsForGermplasmDetailsView(type, form, model, mainInfo, list, dataTableDataList);

			// setting the form
			form.setImportedGermplasmMainInfo(mainInfo);
			form.setStudyId(studyId);
			form.setGermplasmListId(mainInfo.getListId() == null ? ImportGermplasmListController.NO_ID : mainInfo.getListId());
		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	List<Map<String, Object>> generateGermplasmListDataTable(final List<ImportedGermplasm> list, final String defaultTestCheckId,
			final boolean isNursery, final boolean isDefaultTestCheck) {
		final List<Map<String, Object>> dataTableDataList = new ArrayList<>();
		final List<Enumeration> checkList = this.fieldbookService.getCheckTypeList();

		for (final ImportedGermplasm germplasm : list) {
			final Map<String, Object> dataMap = new HashMap<>();

			dataMap.put(ImportGermplasmListController.POSITION, germplasm.getIndex().toString());
			dataMap.put(ImportGermplasmListController.CHECK_OPTIONS, checkList);
			dataMap.put(ImportGermplasmListController.ENTRY, germplasm.getEntryId().toString());
			dataMap.put(ImportGermplasmListController.DESIG, germplasm.getDesig());
			dataMap.put(ImportGermplasmListController.GID, germplasm.getGid());

			if (!isNursery) {
				if (isDefaultTestCheck || germplasm.getEntryTypeValue() == null || "0".equals(germplasm.getEntryTypeValue())) {
					germplasm.setEntryTypeValue(defaultTestCheckId);
					germplasm.setEntryTypeCategoricalID(Integer.valueOf(defaultTestCheckId));
					dataMap.put(ImportGermplasmListController.CHECK, defaultTestCheckId);
				} else {
					dataMap.put(ImportGermplasmListController.CHECK, germplasm.getEntryTypeCategoricalID());
				}

			} else {
				dataMap.put(ImportGermplasmListController.ENTRY_CODE, germplasm.getEntryCode());
				dataMap.put(ImportGermplasmListController.CHECK, "");
			}

			final List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
			if (factorsList != null) {
				// we iterate the map for dynamic header of nursery and trial
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

	void initializeObjectsForGermplasmDetailsView(final String type, final ImportGermplasmListForm form, final Model model,
			final ImportedGermplasmMainInfo mainInfo, final List<ImportedGermplasm> list,
			final List<Map<String, Object>> dataTableDataList) {
		// Set first entry number from the list
		if (!list.isEmpty()) {
			form.setStartingEntryNo(list.get(0).getEntryId().toString());
			final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);
		}

		if (this.userSelection.getMeasurementRowList() != null && !this.userSelection.getMeasurementRowList().isEmpty()) {
			form.setStartingPlotNo(this.userSelection.getMeasurementRowList().get(0).getMeasurementDataValue(TermId.PLOT_NO.getId()));
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
		model.addAttribute(ImportGermplasmListController.TYPE2, type);
		model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST,
				this.getGermplasmTableHeader(type, this.userSelection.getPlotsLevelList()));
	}

	@RequestMapping(value = "/displaySelectedCheckGermplasmDetails", method = RequestMethod.GET)
	public String displaySelectedCheckGermplasmDetails(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form,
			final Model model) {

		try {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			form.setImportedCheckGermplasmMainInfo(mainInfo);

			final List<Enumeration> checksList = this.fieldbookService.getCheckTypeList();

			final int studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
			List<ImportedGermplasm> list = new ArrayList<>();

			final List<GermplasmList> germplasmListCheck =
					this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.CHECK);

			if (germplasmListCheck != null && !germplasmListCheck.isEmpty()) {
				final GermplasmList checkList = germplasmListCheck.get(0);
				if (checkList != null && checkList.getListRef() != null && !checkList.getListRef().equals(0)) {
					form.setKeyForOverwrite(checkList.getListRef());
					form.setLastCheckSourcePrimary(0);
					form.setLastDraggedChecksList(checkList.getListRef().toString());
				} else {
					form.setLastCheckSourcePrimary(1);
				}

				final List<ListDataProject> data = this.fieldbookMiddlewareService.getListDataProject(checkList.getId());
				FieldbookListUtil.populateStockIdInListDataProject(data, this.inventoryDataManager);
				list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
			}

			this.generateCheckListModel(model, list, checksList);

			form.setImportedCheckGermplasm(list);

			final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			form.changeCheckPage(1);
			this.userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

			this.getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

			model.addAttribute(ImportGermplasmListController.HAS_EXPERIMENTAL_DESIGN,
					this.hasExperimentalDesign(this.userSelection.getWorkbook()));
			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST, this.getGermplasmCheckTableHeader());

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.CHECK_PAGINATION_TEMPLATE);
	}

	private List<TableHeader> getGermplasmTableHeader(final String type, final List<SettingDetail> factorsList) {
		final Locale locale = LocaleContextHolder.getLocale();
		final List<TableHeader> tableHeaderList = new ArrayList<>();
		if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {

			tableHeaderList.add(new TableHeader(this.messageSource.getMessage("nursery.import.header.position", null, locale),
					ImportGermplasmListController.POSITION));
			tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_CODE.getTermNameFromOntology(this.ontologyDataManager),
					ImportGermplasmListController.ENTRY_CODE));
		}

		if (factorsList != null) {
			// we iterate the map for dynamic header of nursery and trial
			for (final SettingDetail factorDetail : factorsList) {
				if (factorDetail != null && factorDetail.getVariable() != null
						&& !SettingsUtil.inHideVariableFields(factorDetail.getVariable().getCvTermId(),
								AppConstants.HIDE_GERMPLASM_DESCRIPTOR_HEADER_TABLE.getString())) {
					tableHeaderList.add(new TableHeader(factorDetail.getVariable().getName(),
							factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString()));
				}
			}
		}
		return tableHeaderList;
	}

	private List<TableHeader> getGermplasmCheckTableHeader() {
		final List<TableHeader> tableHeaderList = new ArrayList<>();
		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_TYPE.getTermNameFromOntology(this.ontologyDataManager),
				ImportGermplasmListController.CHECK));
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager),
				ImportGermplasmListController.DESIG));
		return tableHeaderList;
	}

	private String getGermplasmData(final String termId, final ImportedGermplasm germplasm) {
		String val = "";
		if (termId != null && NumberUtils.isNumber(termId)) {
			final Integer term = Integer.valueOf(termId);
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
	public String refreshListDetails(final Model model, @ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form) {

		try {
			final String type = StudyType.T.getName();
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
			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST,
					this.getGermplasmTableHeader(type, this.userSelection.getPlotsLevelList()));
			model.addAttribute("hasMeasurement", this.hasMeasurement());

			final Integer startingEntryNo = this.getUserSelection().getStartingEntryNo();

			if (!list.isEmpty()) {
				form.setStartingEntryNo(list.get(0).getEntryId().toString());
			} else if (startingEntryNo != null) {
				form.setStartingEntryNo(Integer.toString(startingEntryNo));
			}

			form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
			form.setImportedGermplasm(list);

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	protected Boolean hasMeasurement() {
		return this.userSelection.getMeasurementRowList() != null && !this.userSelection.getMeasurementRowList().isEmpty();
	}

	protected String getCheckId(final String checkCode, final List<Enumeration> checksList) {

		String checkId = "";

		for (final Enumeration enumVar : checksList) {
			if (enumVar.getName().equalsIgnoreCase(checkCode)) {
				checkId = enumVar.getId().toString();
				break;
			}
		}
		return checkId;
	}

	/**
	 * Display check germplasm details.
	 *
	 * @param listId the list id
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = "/displayCheckGermplasmDetails/{listId}", method = RequestMethod.GET)
	public String displayCheckGermplasmDetails(@PathVariable final Integer listId,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {

		try {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			form.setImportedCheckGermplasmMainInfo(mainInfo);
			this.germplasmListManager.countGermplasmListDataByListId(listId);
			mainInfo.setListId(listId);

			final List<Enumeration> checksList = this.fieldbookService.getCheckTypeList();
			final String checkId = this.getCheckId(ImportGermplasmListController.DEFAULT_CHECK_VALUE, checksList);
			final List<GermplasmListData> data = new ArrayList<>();
			data.addAll(this.germplasmListManager.getGermplasmListDataByListId(listId));
			FieldbookListUtil.populateStockIdInGermplasmListData(data, this.inventoryDataManager);
			final List<ImportedGermplasm> list = this.transformGermplasmListDataToImportedGermplasm(data, checkId);
			this.generateCheckListModel(model, list, checksList);

			form.setImportedCheckGermplasm(list);

			final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			form.changeCheckPage(1);
			this.userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

			this.getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST, this.getGermplasmCheckTableHeader());

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.CHECK_PAGINATION_TEMPLATE);
	}

	/**
	 * Display check germplasm details.
	 *
	 * @param type
	 * @param form
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/reload/check/list/{type}", method = RequestMethod.GET)
	public String reloadCheckList(@PathVariable final String type,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {
		boolean isNursery = false;
		if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {
			isNursery = true;
		} else if (type != null && type.equalsIgnoreCase(StudyType.T.getName())) {
			isNursery = false;
		}
		try {

			final List<Enumeration> checksList = this.fieldbookService.getCheckTypeList();
			List<ImportedGermplasm> list = new ArrayList<>();
			if (isNursery && this.userSelection.getImportedCheckGermplasmMainInfo() != null
					&& this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList() != null
					&& this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null) {
				// we set it here
				list = this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
				form.setImportedCheckGermplasm(list);
			}
			this.generateCheckListModel(model, list, checksList);

			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST, this.getGermplasmCheckTableHeader());

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.CHECK_PAGINATION_TEMPLATE);
	}

	private void generateCheckListModel(final Model model, final List<ImportedGermplasm> list, final List<Enumeration> checksList) {
		final List<Map<String, Object>> dataTableDataList = new ArrayList<>();
		if (list != null) {
			for (final ImportedGermplasm germplasm : list) {
				final Map<String, Object> dataMap = new HashMap<>();
				dataMap.put(ImportGermplasmListController.DESIG, germplasm.getDesig());
				dataMap.put(ImportGermplasmListController.GID, germplasm.getGid());
				dataMap.put(ImportGermplasmListController.CHECK, germplasm.getEntryTypeValue());
				dataMap.put(ImportGermplasmListController.ENTRY, germplasm.getEntryId());
				dataMap.put("index", germplasm.getIndex());
				dataMap.put(ImportGermplasmListController.CHECK_OPTIONS, checksList);
				dataTableDataList.add(dataMap);
			}
		}
		model.addAttribute(ImportGermplasmListController.CHECK_LISTS, checksList);
		model.addAttribute("checkListDataTable", dataTableDataList);

	}

	/**
	 * Delete check germplasm details.
	 *
	 * @param gid the gid
	 * @param model the model
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteCheckGermplasmDetails/{gid}", method = RequestMethod.GET)
	public String deleteCheckGermplasmDetails(@PathVariable final Integer gid, final Model model) {
		try {

			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			if (this.userSelection.getImportedCheckGermplasmMainInfo() != null) {
				mainInfo = this.userSelection.getImportedCheckGermplasmMainInfo();
			}
			mainInfo.setAdvanceImportType(true);

			final List<ImportedGermplasm> checkList = mainInfo.getImportedGermplasmList().getImportedGermplasms();
			final Iterator<ImportedGermplasm> iter = checkList.iterator();
			while (iter.hasNext()) {
				if (iter.next().getGid().equalsIgnoreCase(gid.toString())) {
					iter.remove();
					break;
				}
			}

			this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(checkList);
		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return ImportGermplasmListController.SUCCESS;
	}

	/**
	 * Adds the check germplasm details.
	 *
	 * @param entryId the entry id
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = "/addCheckGermplasmDetails/{entryId}", method = RequestMethod.GET)
	public String addCheckGermplasmDetails(@PathVariable final Integer entryId,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form,
			@RequestParam("selectedCheckVal") final String selectedCheckVal, final Model model) {

		try {
			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			if (this.userSelection.getImportedCheckGermplasmMainInfo() != null) {
				mainInfo = this.userSelection.getImportedCheckGermplasmMainInfo();
			}
			mainInfo.setAdvanceImportType(true);
			form.setImportedCheckGermplasmMainInfo(mainInfo);

			final List<Enumeration> checksList = this.fieldbookService.getCheckTypeList();
			final String checkId = this.getCheckId(ImportGermplasmListController.DEFAULT_CHECK_VALUE, checksList);

			final List<ImportedGermplasm> primaryList =
					this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
			ImportedGermplasm importedGermplasm = null;
			for (final ImportedGermplasm impGerm : primaryList) {
				if (impGerm.getEntryId().intValue() == entryId.intValue()) {
					importedGermplasm = impGerm.copy();
					break;
				}

			}

			importedGermplasm.setEntryTypeValue(checkId);

			List<ImportedGermplasm> list = new ArrayList<>();
			if (this.userSelection.getImportedCheckGermplasmMainInfo() != null
					&& this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList() != null
					&& this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null) {
				// we set it here
				list = this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

			}
			list.add(importedGermplasm);

			form.setImportedCheckGermplasm(list);

			this.generateCheckListModel(model, list, checksList);

			final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			form.changeCheckPage(1);
			this.userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

			this.getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST, this.getGermplasmCheckTableHeader());

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.CHECK_PAGINATION_TEMPLATE);
	}

	/**
	 * Reset check germplasm details.
	 *
	 * @param model the model
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/resetCheckGermplasmDetails", method = RequestMethod.GET)
	public String resetCheckGermplasmDetails(final Model model) {

		try {
			final ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);

			final List<ImportedGermplasm> list = new ArrayList<>();

			final ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			this.userSelection.setCurrentPageCheckGermplasmList(1);

			this.getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

		} catch (final Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return ImportGermplasmListController.SUCCESS;
	}

	@RequestMapping(value = "/edit/check/{index}/{dataTableIndex}/{type}", method = RequestMethod.GET)
	public String editCheck(@ModelAttribute("updatedGermplasmCheckForm") final UpdateGermplasmCheckForm form, final Model model,
			@PathVariable final int index, @PathVariable final int dataTableIndex, @PathVariable final String type,
			@RequestParam(value = "currentVal") final String currentVal) {

		try {
			ImportedGermplasm importedCheckGermplasm = null;
			if (type != null && type.equalsIgnoreCase(StudyType.T.getName())) {
				importedCheckGermplasm = this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList()
						.getImportedGermplasms().get(dataTableIndex);
			} else if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {
				importedCheckGermplasm = this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList()
						.getImportedGermplasms().get(dataTableIndex);
			}
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
	@RequestMapping(value = "/update/check/{type}", method = RequestMethod.POST)
	public String updateCheck(@ModelAttribute("updatedGermplasmCheckForm") final UpdateGermplasmCheckForm form,
			@PathVariable final String type, final Model model) {

		try {
			ImportedGermplasm importedCheckGermplasm = null;
			if (type != null && type.equalsIgnoreCase(StudyType.T.getName())) {
				importedCheckGermplasm = this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList()
						.getImportedGermplasms().get(form.getIndex());
			} else if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {
				importedCheckGermplasm = this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList()
						.getImportedGermplasms().get(form.getIndex());
			}
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
	 * @param model the model
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/resetNurseryGermplasmDetails", method = RequestMethod.GET)
	public String resetNurseryGermplasmDetails(final Model model) {

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
	 * @param pageNum the page num
	 * @param form the form
	 * @param model the model
	 * @return the paginated list
	 */
	@RequestMapping(value = "/page/{pageNum}", method = RequestMethod.GET)
	public String getPaginatedList(@PathVariable final int pageNum,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {
		// this set the necessary info from the session variable

		form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
		form.setImportedGermplasm(
				this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
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
	 * Gets the check paginated list.
	 *
	 * @param pageNum the page num
	 * @param previewPageNum the preview page num
	 * @param form the form
	 * @param model the model
	 * @return the check paginated list
	 */
	@RequestMapping(value = "/checkPage/{pageNum}/{previewPageNum}", method = RequestMethod.POST)
	public String getCheckPaginatedList(@PathVariable final int pageNum, @PathVariable final int previewPageNum,
			@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form, final Model model) {
		// this set the necessary info from the session variable
		// we need to set the data in the measurementList
		for (int i = 0; i < form.getPaginatedImportedCheckGermplasm().size(); i++) {
			final ImportedGermplasm importedGermplasm = form.getPaginatedImportedCheckGermplasm().get(i);
			final int realIndex = (previewPageNum - 1) * form.getResultPerPage() + i;
			this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(realIndex)
					.setEntryTypeValue(importedGermplasm.getEntryTypeValue());
		}

		form.setImportedCheckGermplasmMainInfo(this.getUserSelection().getImportedCheckGermplasmMainInfo());
		form.setImportedCheckGermplasm(
				this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
		form.changeCheckPage(pageNum);
		this.userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());
		try {
			model.addAttribute(ImportGermplasmListController.CHECK_LISTS, this.fieldbookService.getCheckTypeList());
		} catch (final MiddlewareException e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.CHECK_PAGINATION_TEMPLATE);
	}

	/**
	 * Transform germplasm list data to imported germplasm.
	 *
	 * @param data the data
	 * @param defaultCheckId the default check id
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
	 * @param operation the operation
	 * @param form the form
	 * @param local the local
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
			Enumeration enumeration;
			String message;
			if (operation == 1) {
				enumeration = new Enumeration(null, form.getManageCheckCode(), form.getManageCheckValue(), 0);
				message = this.messageSource.getMessage("nursery.manage.check.types.add.success", new Object[] {form.getManageCheckValue()},
						local);
			} else {
				enumeration = stdVar.getEnumeration(Integer.parseInt(form.getManageCheckCode()));
				enumeration.setDescription(form.getManageCheckValue());
				message = this.messageSource.getMessage("nursery.manage.check.types.edit.success", new Object[] {enumeration.getName()},
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

	/**
	 * Delete check type.
	 *
	 * @param form the form
	 * @param local the local
	 * @return the map
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteCheckType", method = RequestMethod.POST)
	public Map<String, String> deleteCheckType(@ModelAttribute("importGermplasmListForm") final ImportGermplasmListForm form,
			final Locale local) {
		final Map<String, String> result = new HashMap<>();

		try {
			final String name = this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID())
					.getEnumeration(Integer.parseInt(form.getManageCheckCode())).getName();

			if (!this.ontologyService.validateDeleteStandardVariableEnumeration(TermId.CHECK.getId(),
					Integer.parseInt(form.getManageCheckCode()))) {
				result.put(ImportGermplasmListController.SUCCESS, "-1");
				result.put(ImportGermplasmListController.ERROR,
						this.messageSource.getMessage("nursery.manage.check.types.delete.error", new Object[] {name}, local));
			} else {
				this.ontologyService.deleteStandardVariableValidValue(TermId.CHECK.getId(), Integer.parseInt(form.getManageCheckCode()));
				result.put(ImportGermplasmListController.SUCCESS, "1");
				result.put("successMessage",
						this.messageSource.getMessage("nursery.manage.check.types.delete.success", new Object[] {name}, local));
				final List<Enumeration> allEnumerations = this.ontologyService
						.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID()).getEnumerations();
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

	private boolean validateEnumerationDescription(final List<Enumeration> enumerations, final Enumeration newEnumeration) {
		if (enumerations != null && !enumerations.isEmpty() && newEnumeration != null && newEnumeration.getDescription() != null) {
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
	 *
	 * @param userSelection
	 */
	protected void updateObservationsFromTemporaryWorkbookToWorkbook(final UserSelection userSelection) {

		final Map<Integer, MeasurementVariable> observationVariables =
				WorkbookUtil.createVariableList(userSelection.getWorkbook().getFactors(), userSelection.getWorkbook().getVariates());

		WorkbookUtil.deleteDeletedVariablesInObservations(observationVariables, userSelection.getWorkbook().getObservations());

		userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());

		WorkbookUtil.updateTrialObservations(userSelection.getWorkbook(), userSelection.getTemporaryWorkbook());

	}

	/**
	 * This will copy the factors, variates and experimental design variable generated from importing a Custom Design to the Workbook that
	 * will be saved.
	 *
	 * @param userSelection
	 */
	protected void addVariablesFromTemporaryWorkbookToWorkbook(final UserSelection userSelection) {

		if (userSelection.getExperimentalDesignVariables() != null) {

			// Make sure that measurement variables are unique.
			final Set<MeasurementVariable> unique = new HashSet<>(userSelection.getWorkbook().getFactors());
			unique.addAll(userSelection.getTemporaryWorkbook().getFactors());
			unique.addAll(userSelection.getExperimentalDesignVariables());
			userSelection.getWorkbook().getFactors().clear();
			userSelection.getWorkbook().getFactors().addAll(unique);

			final Set<MeasurementVariable> makeUniqueVariates = new HashSet<>(userSelection.getTemporaryWorkbook().getVariates());
			makeUniqueVariates.addAll(userSelection.getWorkbook().getVariates());
			userSelection.getWorkbook().getVariates().clear();
			userSelection.getWorkbook().getVariates().addAll(makeUniqueVariates);

		}
	}

	/**
	 * Updates the Check value and Check ID of Imported Germplasm based on the Selected Checks from ImportGermplasmListForm
	 *
	 * @param userSelection
	 * @param form
	 */
	protected void processChecks(final UserSelection userSelection, final ImportGermplasmListForm form) {

		final String[] selectedCheck = form.getSelectedCheck();

		if (selectedCheck != null && selectedCheck.length != 0) {

			ImportedGermplasmMainInfo importedGermplasmMainInfoToUse = userSelection.getImportedCheckGermplasmMainInfo();
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
					&& userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null) {

				// this is to keep track of the original list before merging
				// with the checks
				for (final ImportedGermplasm germplasm : userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList()
						.getImportedGermplasms()) {
					germplasm.setEntryTypeCategoricalID(null);
					germplasm.setEntryTypeValue("");
				}
			}
		}

		// end: section for taking note of the check germplasm
	}

	protected void processImportedGermplasmAndChecks(final UserSelection userSelection, final ImportGermplasmListForm form) {

		this.processChecks(userSelection, form);

		if (userSelection.getImportedGermplasmMainInfo() != null) {

			this.copyImportedGermplasmFromUserSelectionToForm(userSelection, form);

			this.mergePrimaryAndCheckGermplasmList(userSelection, form);

			// This would validate and add CHECK factor if necessary
			this.importGermplasmFileService.validataAndAddCheckFactor(form.getImportedGermplasm(),
					userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(), userSelection);

			if (userSelection.getStartingEntryNo() == null) {
				userSelection.setStartingEntryNo(org.generationcp.middleware.util.StringUtil.parseInt(form.getStartingEntryNo(), 1));
			}

			if (userSelection.getStartingPlotNo() == null) {
				userSelection.setStartingPlotNo(org.generationcp.middleware.util.StringUtil.parseInt(form.getStartingPlotNo(), 1));
			}

			userSelection.setMeasurementRowList(this.measurementsGeneratorService.generateRealMeasurementRows(userSelection));

			// add or remove check variables if needed
			this.fieldbookService.manageCheckVariables(userSelection, form);

			// remove the experimental design variable if the user changed or
			// updated the germplasm/check list
			this.addExperimentFactorToBeDeleted(userSelection.getWorkbook().getConditions());
		}

	}

	/**
	 * Copies the Germplasm List and Check list from userSelection to ImportGermplasmListForm
	 *
	 * @param userSelection
	 * @param form
	 */
	protected void copyImportedGermplasmFromUserSelectionToForm(final UserSelection userSelection, final ImportGermplasmListForm form) {

		if (userSelection.getImportedGermplasmMainInfo() != null) {

			form.setImportedGermplasmMainInfo(userSelection.getImportedGermplasmMainInfo());
			form.setImportedGermplasm(userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());

			// this is to keep track of the original list before merging
			// with the checks
			if (userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList() != null) {
				userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().copyImportedGermplasms();
			}

		}

		if (userSelection.getImportedCheckGermplasmMainInfo() != null) {

			form.setImportedCheckGermplasmMainInfo(userSelection.getImportedCheckGermplasmMainInfo());
			form.setImportedCheckGermplasm(
					userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
		}

	}

	/**
	 * This will merge the selected Germplasm List and Check list into one Imported Germplasm list. This is necessary for generation of
	 * Observation with checks.
	 *
	 * @param userSelection
	 * @param form
	 */
	protected void mergePrimaryAndCheckGermplasmList(final UserSelection userSelection, final ImportGermplasmListForm form) {

		// merge primary and check germplasm list
		if (userSelection.getImportedCheckGermplasmMainInfo() != null && form.getImportedCheckGermplasm() != null
				&& SettingsUtil.checkVariablesHaveValues(form.getCheckVariables())) {

			this.mergeCheckService.updatePrimaryListAndChecksBeforeMerge(form);

			final int interval = this.getIntervalValue(form);

			final List<ImportedGermplasm> newImportedGermplasm = this.mergeCheckService.mergeGermplasmList(form.getImportedGermplasm(),
					form.getImportedCheckGermplasm(),
					Integer.parseInt(SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_START.getId())), interval,
					SettingsUtil.getCodeInPossibleValues(
							SettingsUtil.getFieldPossibleVales(this.fieldbookService, TermId.CHECK_PLAN.getId()), SettingsUtil
									.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_PLAN.getId())));

			userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(newImportedGermplasm);
			form.setImportedGermplasm(userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
		}

	}

	/**
	 * This will remove the Experimental Design Factor in workbook.
	 *
	 * @param conditions
	 */
	protected void addExperimentFactorToBeDeleted(final List<MeasurementVariable> conditions) {
		conditions.add(this.createMeasurementVariable(String.valueOf(TermId.EXPERIMENT_DESIGN_FACTOR.getId()), "", Operation.DELETE,
				PhenotypicType.TRIAL_ENVIRONMENT));
	}

	protected MeasurementVariable createMeasurementVariable(final String idToCreate, final String value, final Operation operation,
			final PhenotypicType role) {
		final StandardVariable stdvar =
				this.fieldbookMiddlewareService.getStandardVariable(Integer.valueOf(idToCreate), this.contextUtil.getCurrentProgramUUID());
		stdvar.setPhenotypicType(role);
		final MeasurementVariable var = new MeasurementVariable(Integer.valueOf(idToCreate), stdvar.getName(), stdvar.getDescription(),
				stdvar.getScale().getName(), stdvar.getMethod().getName(), stdvar.getProperty().getName(), stdvar.getDataType().getName(),
				value, stdvar.getPhenotypicType().getLabelList().get(0));
		var.setRole(role);
		var.setDataTypeId(stdvar.getDataType().getId());
		var.setFactor(false);
		var.setOperation(operation);
		return var;

	}

	protected boolean hasExperimentalDesign(final Workbook workbook) {
		final ExperimentalDesignVariable expDesignVar = workbook.getExperimentalDesignVariables();
		return expDesignVar != null && expDesignVar.getExperimentalDesign() != null
				&& !StringUtil.isNullOrEmpty(expDesignVar.getExperimentalDesign().getValue());

	}

	protected Integer computeTotalExpectedWithChecks(final ImportGermplasmListForm form) {

		final int totalGermplasmCount =
				this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().size();
		Integer checkInterval = null;
		Integer startCheckFrom = null;

		if (form.getCheckVariables() != null) {
			for (final SettingDetail settingDetail : form.getCheckVariables()) {
				if (Objects.equals(settingDetail.getVariable().getCvTermId(), TermId.CHECK_START.getId())) {
					startCheckFrom = org.generationcp.middleware.util.StringUtil.parseInt(settingDetail.getValue(), null);
				}
				if (Objects.equals(settingDetail.getVariable().getCvTermId(), TermId.CHECK_INTERVAL.getId())) {
					checkInterval = org.generationcp.middleware.util.StringUtil.parseInt(settingDetail.getValue(), null);
				}
			}

			if (checkInterval != null && startCheckFrom != null) {
				final Integer totalCount = (totalGermplasmCount - startCheckFrom) / checkInterval;
				return totalCount + totalGermplasmCount + 1;
			}
		}

		return totalGermplasmCount;

	}

	@ResponseBody
	@RequestMapping(value = "/startingEntryNo", method = RequestMethod.POST)
	public void updateEntryNumbersOfGermplasmList(@RequestBody final Integer startingEntryNo) {
		final List<ImportedGermplasm> list =
				this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
		final Integer lowestEntryNo = this.getLowestEntryNo(list);
		if (lowestEntryNo == null) {
			return;
		}
		final Integer numToAddToEntryNo = startingEntryNo - lowestEntryNo;
		for (final ImportedGermplasm germplasm : list) {
			final Integer currentEntryNo = germplasm.getEntryId();
			if (currentEntryNo != null) {
				germplasm.setEntryId(currentEntryNo + numToAddToEntryNo);
			}
		}
		this.getUserSelection().setStartingEntryNo(startingEntryNo);
	}

	private Integer getLowestEntryNo(final List<ImportedGermplasm> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		Integer lowestEntryNo = list.get(0).getEntryId();
		if (list.size() == 1) {
			return lowestEntryNo;
		}
		for (int i = 1; i < list.size(); i++) {
			final ImportedGermplasm germplasm = list.get(i);
			if (germplasm.getEntryId() != null && germplasm.getEntryId() < lowestEntryNo) {
				lowestEntryNo = germplasm.getEntryId();
			}
		}
		return lowestEntryNo;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}
}
