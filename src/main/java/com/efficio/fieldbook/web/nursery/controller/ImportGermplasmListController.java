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
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.nursery.form.UpdateGermplasmCheckForm;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

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

	private static final String TABLE_HEADER_LIST = "tableHeaderList";

	protected static final String TYPE2 = "type";

	protected static final String LIST_DATA_TABLE = "listDataTable";

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

	/** The user selection. */
	@Resource
	private UserSelection userSelection;

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

	/** The fieldbook middleware service. */
	@Resource
	private FieldbookService fieldbookMiddlewareService;
	@Resource
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

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

	private static String DEFAULT_CHECK_VALUE = "C";
	private static String DEFAULT_TEST_VALUE = "T";

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getContentName()
	 */
	@Override
	public String getContentName() {
		return "NurseryManager/importGermplasmList";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.efficio.fieldbook.web.AbstractBaseFieldbookController#getUserSelection()
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
	public String show(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {
		// this set the necessary info from the session variable

		form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
		if (this.getUserSelection().getImportedGermplasmMainInfo() != null
				&& this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null) {
			// this would be use to display the imported germplasm info
			form.setImportedGermplasm(this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList()
					.getImportedGermplasms());

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
	public String nextScreen(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, BindingResult result, Model model,
			HttpServletRequest req) throws MiddlewareQueryException {
		// start: section for taking note of the check germplasm
		boolean isDeleteObservations = false;
		String[] selectedCheck = form.getSelectedCheck();
		boolean isNursery = this.userSelection.getWorkbook().getStudyDetails().getStudyType() == StudyType.N;
		boolean hasTemporaryWorkbook = false;

	    if (userSelection.getTemporaryWorkbook() != null) {
            WorkbookUtil.manageExpDesignVariablesAndObs(this.userSelection.getWorkbook(), this.userSelection.getTemporaryWorkbook());
            WorkbookUtil.addMeasurementDataToRowsExp(this.userSelection.getWorkbook().getFactors(), this.userSelection.getWorkbook().getObservations(),
                    false, this.userSelection, ontologyService, fieldbookService);
            WorkbookUtil.addMeasurementDataToRowsExp(this.userSelection.getWorkbook().getVariates(), this.userSelection.getWorkbook().getObservations(),
                    true, this.userSelection, ontologyService, fieldbookService);


            if (this.userSelection.getExperimentalDesignVariables() != null){
            	Set<MeasurementVariable> unique = new HashSet<>(this.userSelection.getWorkbook().getFactors());
            	unique.addAll(this.userSelection.getTemporaryWorkbook().getFactors());
                unique.addAll(this.userSelection.getExperimentalDesignVariables());
                this.userSelection.getWorkbook().getFactors().clear();
				this.userSelection.getWorkbook().getFactors().addAll(unique);


                Set<MeasurementVariable> makeUniqueVariates = new HashSet<>(this.userSelection.getTemporaryWorkbook().getVariates());
                makeUniqueVariates.addAll(this.userSelection.getWorkbook().getVariates());
                this.userSelection.getWorkbook().getVariates().clear();
                this.userSelection.getWorkbook().getVariates().addAll(makeUniqueVariates);
			}

			Map<Integer, MeasurementVariable> observationVariables = WorkbookUtil.createVariableList(userSelection.getWorkbook().getFactors(), userSelection.getWorkbook().getVariates());

			WorkbookUtil.deleteDeletedVariablesInObservations(observationVariables, userSelection.getWorkbook().getObservations());
			userSelection.setMeasurementRowList(userSelection.getWorkbook().getObservations());
			WorkbookUtil.updateTrialObservations(userSelection.getWorkbook(),userSelection.getTemporaryWorkbook());
			userSelection.setTemporaryWorkbook(null);
			hasTemporaryWorkbook = true;
			isDeleteObservations = true;

		}

		if (isNursery && !hasTemporaryWorkbook){
			if (selectedCheck != null && selectedCheck.length != 0) {

				ImportedGermplasmMainInfo importedGermplasmMainInfoToUse = getUserSelection().getImportedCheckGermplasmMainInfo();
				if(importedGermplasmMainInfoToUse == null){
					//since for trial, we are using only the original info
					importedGermplasmMainInfoToUse = getUserSelection().getImportedGermplasmMainInfo();
				}
				if(importedGermplasmMainInfoToUse != null){
					for (int i = 0; i < selectedCheck.length; i++) {
						if (NumberUtils.isNumber(selectedCheck[i])) {
							importedGermplasmMainInfoToUse.getImportedGermplasmList().getImportedGermplasms().get(i).setCheck(selectedCheck[i]);
							importedGermplasmMainInfoToUse.getImportedGermplasmList().getImportedGermplasms().get(i).setCheckId(Integer.parseInt(selectedCheck[i]));
						}
					}
				}
			}else{
				//we set the check to null
				if(getUserSelection().getImportedGermplasmMainInfo() != null &&
						getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null
						&& getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null){
					//this is to keep track of the original list before merging with the checks
					for(ImportedGermplasm germplasm : getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()){
						germplasm.setCheckId(null);
						germplasm.setCheck("");
					}
				}
			}

			//end: section for taking note of the check germplasm
			if (getUserSelection().getImportedGermplasmMainInfo() != null) {
				form.setImportedGermplasmMainInfo(getUserSelection().getImportedGermplasmMainInfo());
				form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
				form.setImportedCheckGermplasmMainInfo(getUserSelection().getImportedCheckGermplasmMainInfo());
				if (getUserSelection().getImportedCheckGermplasmMainInfo() != null) {
					form.setImportedCheckGermplasm(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
				}
				if(getUserSelection().getImportedGermplasmMainInfo() != null &&
						getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList() != null){
					//this is to keep track of the original list before merging with the checks
					getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().copyImportedGermplasms();
				}
				//merge primary and check germplasm list
				if (getUserSelection().getImportedCheckGermplasmMainInfo() != null && form.getImportedCheckGermplasm() != null
						&& SettingsUtil.checkVariablesHaveValues(form.getCheckVariables())) {
					String lastDragCheckList = form.getLastDraggedChecksList();
					if("0".equalsIgnoreCase(lastDragCheckList)){
						//we do the cleaning here
						List<ImportedGermplasm> newNurseryGermplasm = cleanGermplasmList(form.getImportedGermplasm(),
								form.getImportedCheckGermplasm());
						form.setImportedGermplasm(newNurseryGermplasm);
					}

					int interval = getIntervalValue(form);

					String defaultTestCheckId = getCheckId(DEFAULT_TEST_VALUE, fieldbookService.getCheckList());

					List<ImportedGermplasm> newImportedGermplasm = mergeCheckService.mergeGermplasmList(form.getImportedGermplasm(),
							form.getImportedCheckGermplasm(),
							Integer.parseInt(SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_START.getId())),
							interval,
							SettingsUtil.getCodeInPossibleValues(SettingsUtil.getFieldPossibleVales(fieldbookService, TermId.CHECK_PLAN.getId()), SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_PLAN.getId())),
							defaultTestCheckId);

					getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(newImportedGermplasm);
					form.setImportedGermplasm(getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
				}

				//this would validate and add CHECK factor if necessary
				importGermplasmFileService.validataAndAddCheckFactor(form.getImportedGermplasm(), getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms(), userSelection);
				userSelection.setMeasurementRowList(measurementsGeneratorService.generateRealMeasurementRows(userSelection));

				//add or remove check variables if needed
				fieldbookService.manageCheckVariables(userSelection, form);
			}
		} else if (!hasTemporaryWorkbook) {
			isDeleteObservations = true;
			userSelection.setMeasurementRowList(null);
		}

		userSelection.getWorkbook().setObservations(userSelection.getMeasurementRowList());

		fieldbookService.createIdCodeNameVariablePairs(userSelection.getWorkbook(), AppConstants.ID_CODE_NAME_COMBINATION_STUDY.getString());
		fieldbookService.createIdNameVariablePairs(userSelection.getWorkbook(), new ArrayList<SettingDetail>(), AppConstants.ID_NAME_COMBINATION.getString(), true);
		int studyId = dataImportService.saveDataset(userSelection.getWorkbook(), true, isDeleteObservations, getCurrentProject().getUniqueID());
		fieldbookService.saveStudyImportedCrosses(userSelection.getImportedCrossesId(), studyId);
		//for saving the list data project
		saveListDataProject(isNursery, studyId);


		fieldbookService.saveStudyColumnOrdering(studyId, userSelection.getWorkbook().getStudyName(), form.getColumnOrders(), userSelection.getWorkbook());

		return Integer.toString(studyId);
	}

	private int getIntervalValue(ImportGermplasmListForm form) {
		String interval = SettingsUtil.getSettingDetailValue(form.getCheckVariables(), TermId.CHECK_INTERVAL.getId());
		if (interval != null && !("").equals(interval)) {
			return Integer.parseInt(interval);
		}
		return 0;
	}

	private void saveListDataProject(boolean isNursery, int studyId) throws MiddlewareQueryException{
		//we call here to have

		if(getUserSelection().getImportedGermplasmMainInfo() != null && getUserSelection().getImportedGermplasmMainInfo().getListId() != null){
			//we save the list
			//we need to create a new germplasm list
			Integer listId = getUserSelection().getImportedGermplasmMainInfo().getListId();
			List<ImportedGermplasm> importedGermplasmList;

			if (isNursery){
				if (getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getOriginalImportedGermplasms() != null){
					importedGermplasmList = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getOriginalImportedGermplasms();
				}else{
					importedGermplasmList = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
				}
			}else{
				importedGermplasmList = getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
			}

			List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(importedGermplasmList);
			fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, isNursery ? GermplasmListType.NURSERY : GermplasmListType.TRIAL, listId, listDataProject, getCurrentIbdbUserId());
		}else{
			//we delete the record in the db
			fieldbookMiddlewareService.deleteListDataProjects(studyId, isNursery ? GermplasmListType.NURSERY : GermplasmListType.TRIAL);
		}
		if(getUserSelection().getImportedCheckGermplasmMainInfo() != null){
			if(getUserSelection().getImportedCheckGermplasmMainInfo().getListId() != null){
				//came from a list
				Integer listId = getUserSelection().getImportedCheckGermplasmMainInfo().getListId();
				List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
				fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, GermplasmListType.CHECK, listId, listDataProject,getCurrentIbdbUserId());

			}else if(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList() != null &&
					getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms() != null
					&& !getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().isEmpty()){
				List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProject(getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
				fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, GermplasmListType.CHECK, null, listDataProject, getCurrentIbdbUserId());

			}else{
				//we delete it
				fieldbookMiddlewareService.deleteListDataProjects(studyId, GermplasmListType.CHECK);
			}
		}else{
			if(isNursery){
				//we delete it
				fieldbookMiddlewareService.deleteListDataProjects(studyId, GermplasmListType.CHECK);
			}
		}
	}

	private List<ImportedGermplasm> cleanGermplasmList(List<ImportedGermplasm> primaryList,
			List<ImportedGermplasm> checkList){
		if (checkList == null || checkList.isEmpty()) {
			return primaryList;
		}

		List<ImportedGermplasm> newPrimaryList = new ArrayList<>();
		Map<Integer, ImportedGermplasm> checkGermplasmMap = new HashMap<>();
		for (ImportedGermplasm checkGermplasm : checkList) {
			checkGermplasmMap.put(checkGermplasm.getIndex(), checkGermplasm);
		}

		for (ImportedGermplasm primaryGermplasm : primaryList) {
			if (checkGermplasmMap.get(primaryGermplasm.getIndex()) == null) {
				newPrimaryList.add(primaryGermplasm);
			}
		}
		return newPrimaryList;
	}

	/**
	 * Display germplasm details.
	 *
	 * @param listId the list id
	 * @param form the form
	 * @param model the model
	 * @return the string
	 */
	@RequestMapping(value = "/displayGermplasmDetails/{listId}/{type}", method = RequestMethod.GET)
	public String displayGermplasmDetails(@PathVariable Integer listId, @PathVariable String type,
			@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {

		try {
			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			form.setImportedGermplasmMainInfo(mainInfo);
			int count = (int) this.germplasmListManager.countGermplasmListDataByListId(listId);
			mainInfo.setListId(listId);
			List<GermplasmListData> data = new ArrayList<>();
			data.addAll(this.germplasmListManager.getGermplasmListDataByListId(listId, 0, count));
			List<ImportedGermplasm> list = this.transformGermplasmListDataToImportedGermplasm(data, null);
			String defaultTestCheckId =
					this.getCheckId(ImportGermplasmListController.DEFAULT_TEST_VALUE, this.fieldbookService.getCheckList());
			form.setImportedGermplasm(list);
			List<Map<String, Object>> dataTableDataList = new ArrayList<>();
			List<Enumeration> checkList = this.fieldbookService.getCheckList();
			boolean isNursery = false;
			if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {
				isNursery = true;
			} else if (type != null && type.equalsIgnoreCase(StudyType.T.getName())) {
				isNursery = false;
			}
			for (ImportedGermplasm germplasm : list) {
				Map<String, Object> dataMap = new HashMap<>();

				dataMap.put(ImportGermplasmListController.POSITION, germplasm.getIndex().toString());
				dataMap.put(ImportGermplasmListController.CHECK_OPTIONS, checkList);
				dataMap.put(ImportGermplasmListController.ENTRY, germplasm.getEntryId().toString());
				dataMap.put(ImportGermplasmListController.DESIG, germplasm.getDesig());
				dataMap.put(ImportGermplasmListController.GID, germplasm.getGid());

				if (!isNursery) {
					germplasm.setCheck(defaultTestCheckId);
					germplasm.setCheckId(Integer.valueOf(defaultTestCheckId));
					dataMap.put(ImportGermplasmListController.CHECK, defaultTestCheckId);

					List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
					if (factorsList != null) {
						// we iterate the map for dynamic header of trial
						for (SettingDetail factorDetail : factorsList) {
							if (factorDetail != null && factorDetail.getVariable() != null) {
								dataMap.put(factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString(),
										this.getGermplasmData(factorDetail.getVariable().getCvTermId().toString(), germplasm));
							}
						}
					}
				} else {
					dataMap.put(ImportGermplasmListController.CROSS, germplasm.getCross());
					dataMap.put(ImportGermplasmListController.SOURCE, germplasm.getSource());
					dataMap.put(ImportGermplasmListController.ENTRY_CODE, germplasm.getEntryCode());
					dataMap.put(ImportGermplasmListController.CHECK, "");
				}

				dataTableDataList.add(dataMap);
			}
			ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			form.changePage(1);
			this.userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

			this.getUserSelection().setImportedGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

			model.addAttribute(ImportGermplasmListController.CHECK_LISTS, this.fieldbookService.getCheckList());
			model.addAttribute(ImportGermplasmListController.LIST_DATA_TABLE, dataTableDataList);
			model.addAttribute(ImportGermplasmListController.TYPE2, type);
			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST,
					this.getGermplasmTableHeader(type, this.userSelection.getPlotsLevelList()));
		} catch (Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	@RequestMapping(value = "/displaySelectedGermplasmDetails/{type}", method = RequestMethod.GET)
	public String displaySelectedGermplasmDetails(@PathVariable String type,
			@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {

		try {
			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();

			mainInfo.setAdvanceImportType(true);
			form.setImportedGermplasmMainInfo(mainInfo);

			int studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
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

			List<GermplasmList> germplasmLists =
					this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, germplasmListType);

			if (germplasmLists != null && !germplasmLists.isEmpty()) {
				GermplasmList germplasmList = germplasmLists.get(0);

				if (germplasmList != null && germplasmList.getListRef() != null) {
					form.setLastDraggedPrimaryList(germplasmList.getListRef().toString());
					mainInfo.setListId(germplasmList.getId());
				}
				List<ListDataProject> data = this.fieldbookMiddlewareService.getListDataProject(germplasmList.getId());
				list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
			}

			String defaultTestCheckId =
					this.getCheckId(ImportGermplasmListController.DEFAULT_TEST_VALUE, this.fieldbookService.getCheckList());
			form.setImportedGermplasm(list);
			List<Map<String, Object>> dataTableDataList = new ArrayList<>();
			List<Enumeration> checkList = this.fieldbookService.getCheckList();

			for (ImportedGermplasm germplasm : list) {
				Map<String, Object> dataMap = new HashMap<>();

				dataMap.put(ImportGermplasmListController.POSITION, germplasm.getIndex().toString());
				dataMap.put(ImportGermplasmListController.CHECK_OPTIONS, checkList);
				dataMap.put(ImportGermplasmListController.ENTRY, germplasm.getEntryId().toString());
				dataMap.put(ImportGermplasmListController.DESIG, germplasm.getDesig());
				dataMap.put(ImportGermplasmListController.GID, germplasm.getGid());

				if (!isNursery) {
					if (germplasm.getCheck() == null || "0".equals(germplasm.getCheck())) {
						germplasm.setCheck(defaultTestCheckId);
						germplasm.setCheckId(Integer.valueOf(defaultTestCheckId));
						dataMap.put(ImportGermplasmListController.CHECK, defaultTestCheckId);
					} else {
						dataMap.put(ImportGermplasmListController.CHECK, germplasm.getCheckId());
					}

					List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
					if (factorsList != null) {
						// we iterate the map for dynamic header of trial
						for (SettingDetail factorDetail : factorsList) {
							if (factorDetail != null && factorDetail.getVariable() != null) {
								dataMap.put(factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString(),
										this.getGermplasmData(factorDetail.getVariable().getCvTermId().toString(), germplasm));
							}
						}
					}
				} else {
					dataMap.put(ImportGermplasmListController.CROSS, germplasm.getCross());
					dataMap.put(ImportGermplasmListController.SOURCE, germplasm.getSource());
					dataMap.put(ImportGermplasmListController.ENTRY_CODE, germplasm.getEntryCode());
					dataMap.put(ImportGermplasmListController.CHECK, "");
				}

				dataTableDataList.add(dataMap);
			}
			ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			form.changePage(1);
			this.userSelection.setCurrentPageGermplasmList(form.getCurrentPage());

			this.getUserSelection().setImportedGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

			model.addAttribute(ImportGermplasmListController.CHECK_LISTS, this.fieldbookService.getCheckList());
			model.addAttribute(ImportGermplasmListController.LIST_DATA_TABLE, dataTableDataList);
			model.addAttribute(ImportGermplasmListController.TYPE2, type);
			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST,
					this.getGermplasmTableHeader(type, this.userSelection.getPlotsLevelList()));
		} catch (Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	@RequestMapping(value = "/displaySelectedCheckGermplasmDetails", method = RequestMethod.GET)
	public String displaySelectedCheckGermplasmDetails(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {

		try {
			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			form.setImportedCheckGermplasmMainInfo(mainInfo);

			List<Enumeration> checksList = this.fieldbookService.getCheckList();

			int studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
			List<ImportedGermplasm> list = new ArrayList<>();

			List<GermplasmList> germplasmListCheck =
					this.fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, GermplasmListType.CHECK);

			if (germplasmListCheck != null && !germplasmListCheck.isEmpty()) {
				GermplasmList checkList = germplasmListCheck.get(0);
				if (checkList != null & checkList.getListRef() != null && !checkList.getListRef().equals(0)) {
					form.setKeyForOverwrite(checkList.getListRef());
					form.setLastCheckSourcePrimary(0);
					form.setLastDraggedChecksList(checkList.getListRef().toString());
				} else {
					form.setLastCheckSourcePrimary(1);
				}

				List<ListDataProject> data = this.fieldbookMiddlewareService.getListDataProject(checkList.getId());
				list = ListDataProjectUtil.transformListDataProjectToImportedGermplasm(data);
			}

			this.generateCheckListModel(model, list, checksList);

			form.setImportedCheckGermplasm(list);

			ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			form.changeCheckPage(1);
			this.userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

			this.getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST, this.getGermplasmCheckTableHeader());

		} catch (Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.CHECK_PAGINATION_TEMPLATE);
	}

	private List<TableHeader> getGermplasmTableHeader(String type, List<SettingDetail> factorsList) {
		Locale locale = LocaleContextHolder.getLocale();
		List<TableHeader> tableHeaderList = new ArrayList<>();
		if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {

			tableHeaderList.add(new TableHeader(this.messageSource.getMessage("nursery.import.header.position", null, locale),
					ImportGermplasmListController.POSITION));
			tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_ID.getTermNameFromOntology(this.ontologyDataManager),
					ImportGermplasmListController.ENTRY));
			tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager),
					ImportGermplasmListController.DESIG));
			tableHeaderList.add(new TableHeader(ColumnLabels.GID.getTermNameFromOntology(this.ontologyDataManager),
					ImportGermplasmListController.GID));
			tableHeaderList.add(new TableHeader(ColumnLabels.PARENTAGE.getTermNameFromOntology(this.ontologyDataManager),
					ImportGermplasmListController.CROSS));
			tableHeaderList.add(new TableHeader(ColumnLabels.SEED_SOURCE.getTermNameFromOntology(this.ontologyDataManager),
					ImportGermplasmListController.SOURCE));
			tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_CODE.getTermNameFromOntology(this.ontologyDataManager),
					ImportGermplasmListController.ENTRY_CODE));

		} else if (type != null && type.equalsIgnoreCase(StudyType.T.getName()) && factorsList != null) {
			// we iterate the map for dynamic header of trial
			for (SettingDetail factorDetail : factorsList) {
				if (factorDetail != null && factorDetail.getVariable() != null && !SettingsUtil
						.inHideVariableFields(factorDetail.getVariable().getCvTermId(),
								AppConstants.HIDE_GERMPLASM_DESCRIPTOR_HEADER_TABLE.getString())) {
					tableHeaderList.add(new TableHeader(factorDetail.getVariable().getName(),
							factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString()));
				}

			}
		}
		return tableHeaderList;
	}

	private List<TableHeader> getGermplasmCheckTableHeader() {
		List<TableHeader> tableHeaderList = new ArrayList<>();
		tableHeaderList.add(new TableHeader(ColumnLabels.ENTRY_TYPE.getTermNameFromOntology(this.ontologyDataManager),
				ImportGermplasmListController.CHECK));
		tableHeaderList.add(new TableHeader(ColumnLabels.DESIGNATION.getTermNameFromOntology(this.ontologyDataManager),
				ImportGermplasmListController.DESIG));
		return tableHeaderList;
	}

	private String getGermplasmData(String termId, ImportedGermplasm germplasm) {
		String val = "";
		if (termId != null && NumberUtils.isNumber(termId)) {
			Integer term = Integer.valueOf(termId);
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
				val = germplasm.getCheck();
			}
		}
		return val;
	}

	@RequestMapping(value = "/refreshListDetails", method = RequestMethod.GET)
	public String refereshListDetails(Model model, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form) {

		try {
			String type = "T";
			List<Map<String, Object>> dataTableDataList = new ArrayList<>();
			List<Enumeration> checkList = this.fieldbookService.getCheckList();
			List<ImportedGermplasm> list =
					this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();

			// we need to take note of the check here

			for (ImportedGermplasm germplasm : list) {
				Map<String, Object> dataMap = new HashMap<>();
				dataMap.put(ImportGermplasmListController.POSITION, germplasm.getIndex().toString());
				dataMap.put(ImportGermplasmListController.CHECK_OPTIONS, checkList);
				dataMap.put(ImportGermplasmListController.ENTRY, germplasm.getEntryId().toString());
				dataMap.put(ImportGermplasmListController.DESIG, germplasm.getDesig());
				dataMap.put(ImportGermplasmListController.GID, germplasm.getGid());
				List<SettingDetail> factorsList = this.userSelection.getPlotsLevelList();
				if (factorsList != null) {
					// we iterate the map for dynamic header of trial
					for (SettingDetail factorDetail : factorsList) {
						if (factorDetail != null && factorDetail.getVariable() != null) {
							dataMap.put(factorDetail.getVariable().getCvTermId() + AppConstants.TABLE_HEADER_KEY_SUFFIX.getString(),
									this.getGermplasmData(factorDetail.getVariable().getCvTermId().toString(), germplasm));
						}
					}
				}
				dataMap.put(ImportGermplasmListController.CHECK, germplasm.getCheck() != null ? germplasm.getCheck() : "");

				dataTableDataList.add(dataMap);
			}

			model.addAttribute(ImportGermplasmListController.CHECK_LISTS, this.fieldbookService.getCheckList());
			model.addAttribute(ImportGermplasmListController.LIST_DATA_TABLE, dataTableDataList);
			model.addAttribute(ImportGermplasmListController.TYPE2, type);
			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST,
					this.getGermplasmTableHeader(type, this.userSelection.getPlotsLevelList()));
			model.addAttribute("hasMeasurement", this.hasMeasurement());

			form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
			form.setImportedGermplasm(list);

		} catch (Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.PAGINATION_TEMPLATE);
	}

	protected Boolean hasMeasurement() {
		return this.userSelection.getMeasurementRowList() != null && !this.userSelection.getMeasurementRowList().isEmpty();
	}

	protected String getCheckId(String checkCode, List<Enumeration> checksList) throws MiddlewareQueryException {
		String checkId = "";

		for (Enumeration enumVar : checksList) {
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
	public String displayCheckGermplasmDetails(@PathVariable Integer listId,
			@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {

		try {
			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			form.setImportedCheckGermplasmMainInfo(mainInfo);
			int count = (int) this.germplasmListManager.countGermplasmListDataByListId(listId);
			mainInfo.setListId(listId);

			List<Enumeration> checksList = this.fieldbookService.getCheckList();
			String checkId = this.getCheckId(ImportGermplasmListController.DEFAULT_CHECK_VALUE, checksList);

			List<GermplasmListData> data = new ArrayList<>();
			data.addAll(this.germplasmListManager.getGermplasmListDataByListId(listId, 0, count));
			List<ImportedGermplasm> list = this.transformGermplasmListDataToImportedGermplasm(data, checkId);

			this.generateCheckListModel(model, list, checksList);

			form.setImportedCheckGermplasm(list);

			ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			form.changeCheckPage(1);
			this.userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

			this.getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST, this.getGermplasmCheckTableHeader());

		} catch (Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.CHECK_PAGINATION_TEMPLATE);
	}

	/**
	 * Display check germplasm details.
	 * @param type
	 * @param form
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/reload/check/list/{type}", method = RequestMethod.GET)
	public String reloadCheckList(@PathVariable String type, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form,
			Model model) {
		boolean isNursery = false;
		if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {
			isNursery = true;
		} else if (type != null && type.equalsIgnoreCase(StudyType.T.getName())) {
			isNursery = false;
		}
		try {

			List<Enumeration> checksList = this.fieldbookService.getCheckList();
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

		} catch (Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.CHECK_PAGINATION_TEMPLATE);
	}

	private void generateCheckListModel(Model model, List<ImportedGermplasm> list, List<Enumeration> checksList) {
		List<Map<String, Object>> dataTableDataList = new ArrayList<>();
		if (list != null) {
			for (ImportedGermplasm germplasm : list) {
				Map<String, Object> dataMap = new HashMap<>();
				dataMap.put(ImportGermplasmListController.DESIG, germplasm.getDesig());
				dataMap.put(ImportGermplasmListController.GID, germplasm.getGid());
				dataMap.put(ImportGermplasmListController.CHECK, germplasm.getCheck());
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
	public String deleteCheckGermplasmDetails(@PathVariable Integer gid, Model model) {
		try {

			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			if (this.userSelection.getImportedCheckGermplasmMainInfo() != null) {
				mainInfo = this.userSelection.getImportedCheckGermplasmMainInfo();
			}
			mainInfo.setAdvanceImportType(true);

			List<ImportedGermplasm> checkList = mainInfo.getImportedGermplasmList().getImportedGermplasms();
			Iterator<ImportedGermplasm> iter = checkList.iterator();
			while (iter.hasNext()) {
				if (iter.next().getGid().equalsIgnoreCase(gid.toString())) {
					iter.remove();
					break;
				}
			}

			this.userSelection.getImportedCheckGermplasmMainInfo().getImportedGermplasmList().setImportedGermplasms(checkList);
		} catch (Exception e) {
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
	public String addCheckGermplasmDetails(@PathVariable Integer entryId,
			@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form,
			@RequestParam("selectedCheckVal") String selectedCheckVal, Model model) {

		try {
			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			if (this.userSelection.getImportedCheckGermplasmMainInfo() != null) {
				mainInfo = this.userSelection.getImportedCheckGermplasmMainInfo();
			}
			mainInfo.setAdvanceImportType(true);
			form.setImportedCheckGermplasmMainInfo(mainInfo);

			List<Enumeration> checksList = this.fieldbookService.getCheckList();
			String checkId = this.getCheckId(ImportGermplasmListController.DEFAULT_CHECK_VALUE, checksList);

			List<ImportedGermplasm> primaryList =
					this.userSelection.getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms();
			ImportedGermplasm importedGermplasm = null;
			for (ImportedGermplasm impGerm : primaryList) {
				if (impGerm.getEntryId().intValue() == entryId.intValue()) {
					importedGermplasm = impGerm.copy();
					break;
				}

			}

			importedGermplasm.setCheck(checkId);

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

			ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			form.changeCheckPage(1);
			this.userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());

			this.getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

			model.addAttribute(ImportGermplasmListController.TABLE_HEADER_LIST, this.getGermplasmCheckTableHeader());

		} catch (Exception e) {
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
	public String resetCheckGermplasmDetails(Model model) {

		try {
			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);

			List<ImportedGermplasm> list = new ArrayList<>();

			ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			this.userSelection.setCurrentPageCheckGermplasmList(1);

			this.getUserSelection().setImportedCheckGermplasmMainInfo(mainInfo);
			this.getUserSelection().setImportValid(true);

		} catch (Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return ImportGermplasmListController.SUCCESS;
	}

	@RequestMapping(value = "/edit/check/{index}/{dataTableIndex}/{type}", method = RequestMethod.GET)
	public String editCheck(@ModelAttribute("updatedGermplasmCheckForm") UpdateGermplasmCheckForm form, Model model,
			@PathVariable int index, @PathVariable int dataTableIndex, @PathVariable String type,
			@RequestParam(value = "currentVal") String currentVal) {

		try {
			ImportedGermplasm importedCheckGermplasm = null;
			if (type != null && type.equalsIgnoreCase(StudyType.T.getName())) {
				importedCheckGermplasm =
						this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
								.get(dataTableIndex);
			} else if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {
				importedCheckGermplasm =
						this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
								.get(dataTableIndex);
			}
			importedCheckGermplasm.setCheck(currentVal);
			List<Enumeration> allEnumerations = this.fieldbookService.getCheckList();

			model.addAttribute("allCheckTypes", allEnumerations);
			form.setCheckVal(currentVal);
			form.setIndex(index);
			form.setDataTableIndex(dataTableIndex);
		} catch (Exception e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, ImportGermplasmListController.EDIT_CHECK);
	}

	@ResponseBody
	@RequestMapping(value = "/update/check/{type}", method = RequestMethod.POST)
	public String updateCheck(@ModelAttribute("updatedGermplasmCheckForm") UpdateGermplasmCheckForm form, @PathVariable String type,
			Model model) {

		try {
			ImportedGermplasm importedCheckGermplasm = null;
			if (type != null && type.equalsIgnoreCase(StudyType.T.getName())) {
				importedCheckGermplasm =
						this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
								.get(form.getIndex());
			} else if (type != null && type.equalsIgnoreCase(StudyType.N.getName())) {
				importedCheckGermplasm =
						this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms()
								.get(form.getIndex());
			}
			importedCheckGermplasm.setCheck(form.getCheckVal());
			importedCheckGermplasm.setCheckId(Integer.valueOf(form.getCheckVal()));

		} catch (Exception e) {
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
	public String resetNurseryGermplasmDetails(Model model) {

		try {
			ImportedGermplasmMainInfo mainInfo = new ImportedGermplasmMainInfo();
			mainInfo.setAdvanceImportType(true);
			List<ImportedGermplasm> list = new ArrayList<>();

			ImportedGermplasmList importedGermplasmList = new ImportedGermplasmList();
			importedGermplasmList.setImportedGermplasms(list);
			mainInfo.setImportedGermplasmList(importedGermplasmList);

			this.getUserSelection().setImportedGermplasmMainInfo(null);
			this.getUserSelection().setImportValid(true);

		} catch (Exception e) {
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
	public String getPaginatedList(@PathVariable int pageNum, @ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form,
			Model model) {
		// this set the necessary info from the session variable

		form.setImportedGermplasmMainInfo(this.getUserSelection().getImportedGermplasmMainInfo());
		form.setImportedGermplasm(this.getUserSelection().getImportedGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms());
		form.changePage(pageNum);
		this.userSelection.setCurrentPageGermplasmList(form.getCurrentPage());
		try {
			model.addAttribute(ImportGermplasmListController.CHECK_LISTS, this.fieldbookService.getCheckList());
		} catch (MiddlewareQueryException e) {
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
	public String getCheckPaginatedList(@PathVariable int pageNum, @PathVariable int previewPageNum,
			@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Model model) {
		// this set the necessary info from the session variable
		// we need to set the data in the measurementList
		for (int i = 0; i < form.getPaginatedImportedCheckGermplasm().size(); i++) {
			ImportedGermplasm importedGermplasm = form.getPaginatedImportedCheckGermplasm().get(i);
			int realIndex = (previewPageNum - 1) * form.getResultPerPage() + i;
			this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList().getImportedGermplasms().get(realIndex)
					.setCheck(importedGermplasm.getCheck());
		}

		form.setImportedCheckGermplasmMainInfo(this.getUserSelection().getImportedCheckGermplasmMainInfo());
		form.setImportedCheckGermplasm(this.getUserSelection().getImportedCheckGermplasmMainInfo().getImportedGermplasmList()
				.getImportedGermplasms());
		form.changeCheckPage(pageNum);
		this.userSelection.setCurrentPageCheckGermplasmList(form.getCurrentCheckPage());
		try {
			model.addAttribute(ImportGermplasmListController.CHECK_LISTS, this.fieldbookService.getCheckList());
		} catch (MiddlewareQueryException e) {
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
	private List<ImportedGermplasm> transformGermplasmListDataToImportedGermplasm(List<GermplasmListData> data, String defaultCheckId) {
		List<ImportedGermplasm> list = new ArrayList<>();
		int index = 1;
		if (data != null && !data.isEmpty()) {
			for (GermplasmListData aData : data) {
				ImportedGermplasm germplasm = new ImportedGermplasm();
				germplasm.setCheck(defaultCheckId);
				germplasm.setCross(aData.getGroupName());
				germplasm.setDesig(aData.getDesignation());
				germplasm.setEntryCode(aData.getEntryCode());
				germplasm.setEntryId(aData.getEntryId());
				germplasm.setGid(aData.getGid().toString());
				germplasm.setSource(aData.getSeedSource());
				germplasm.setGroupName(aData.getGroupName());
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
		Map<String, String> result = new HashMap<>();

		try {
			List<Enumeration> allEnumerations = this.fieldbookService.getCheckList();
			result.put(ImportGermplasmListController.SUCCESS, "1");
			result.put("allCheckTypes", this.convertObjectToJson(allEnumerations));

		} catch (MiddlewareQueryException e) {
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
	public Map<String, String> addUpdateCheckType(@PathVariable int operation,
			@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Locale local) {
		Map<String, String> result = new HashMap<>();

		try {
			StandardVariable stdVar = this.ontologyService.getStandardVariable(TermId.CHECK.getId());
			Enumeration enumeration;
			String message;
			if (operation == 1) {
				enumeration = new Enumeration(null, form.getManageCheckCode(), form.getManageCheckValue(), 0);
				message =
						this.messageSource.getMessage("nursery.manage.check.types.add.success", new Object[] {form.getManageCheckValue()},
								local);
			} else {
				enumeration = stdVar.getEnumeration(Integer.parseInt(form.getManageCheckCode()));
				enumeration.setDescription(form.getManageCheckValue());
				message =
						this.messageSource.getMessage("nursery.manage.check.types.edit.success", new Object[] {enumeration.getName()},
								local);
			}
			if (!this.validateEnumerationDescription(stdVar.getEnumerations(), enumeration)) {
				result.put(ImportGermplasmListController.SUCCESS, "-1");
				result.put(ImportGermplasmListController.ERROR,
						this.messageSource.getMessage("error.add.check.duplicate.description", null, local));
			} else {
				this.ontologyService.saveOrUpdateStandardVariableEnumeration(stdVar, enumeration);
				List<Enumeration> allEnumerations = this.ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
				result.put("checkTypes", this.convertObjectToJson(allEnumerations));

				result.put(ImportGermplasmListController.SUCCESS, "1");
				result.put("successMessage", message);
			}

		} catch (MiddlewareQueryException e) {
			ImportGermplasmListController.LOG.debug(e.getMessage(), e);
			result.put(ImportGermplasmListController.SUCCESS, "-1");
			result.put(ImportGermplasmListController.ERROR, e.getMessage());
		} catch (MiddlewareException e) {
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
	public Map<String, String> deleteCheckType(@ModelAttribute("importGermplasmListForm") ImportGermplasmListForm form, Locale local) {
		Map<String, String> result = new HashMap<>();

		try {
			String name =
					this.ontologyService.getStandardVariable(TermId.CHECK.getId())
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
				List<Enumeration> allEnumerations = this.ontologyService.getStandardVariable(TermId.CHECK.getId()).getEnumerations();
				result.put("checkTypes", this.convertObjectToJson(allEnumerations));
			}

		} catch (MiddlewareQueryException e) {
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
		try {
			return this.fieldbookService.getCheckList();
		} catch (MiddlewareQueryException e) {
			ImportGermplasmListController.LOG.error(e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	private boolean validateEnumerationDescription(List<Enumeration> enumerations, Enumeration newEnumeration) {
		if (enumerations != null && !enumerations.isEmpty() && newEnumeration != null && newEnumeration.getDescription() != null) {
			for (Enumeration enumeration : enumerations) {
				if (enumeration.getDescription() != null
						&& newEnumeration.getDescription().trim().equalsIgnoreCase(enumeration.getDescription().trim())
						&& !enumeration.getId().equals(newEnumeration.getId())) {
					return false;
				}
			}
		}
		return true;
	}
}
