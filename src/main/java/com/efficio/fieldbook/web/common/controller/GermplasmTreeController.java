/*
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 * *****************************************************************************
 */

package com.efficio.fieldbook.web.common.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RulesNotConfiguredException;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmFolderMetadata;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.GermplasmListMetadata;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.util.FieldbookUtil;
import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.SaveListForm;
import com.efficio.fieldbook.web.common.service.CrossingService;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;
import com.google.common.collect.Lists;

/**
 * The Class GermplasmTreeController.
 */
@Controller
@RequestMapping(value = "/ListTreeManager")
@Transactional
public class GermplasmTreeController extends AbstractBaseFieldbookController {

	/**
	 * The default folder open state stored when closing the germplasm lists browser. 
	 */
	static final String DEFAULT_STATE_SAVED_FOR_GERMPLASM_LIST = "Lists";

	private static final String COMMON_SAVE_GERMPLASM_LIST = "Common/saveGermplasmList";

	private static final String GERMPLASM_LIST_TYPES = "germplasmListTypes";

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(GermplasmTreeController.class);

	private static final String GERMPLASM_LIST_TABLE_PAGE = "Common/includes/germplasmListTable";
	public static final String GERMPLASM_LIST_ROOT_NODES = "germplasmListRootNodes";
	private static final String GERMPLASM_LIST_TABLE_ROWS_PAGE = "Common/includes/germplasmListTableRows";
	public static final String GERMPLASM_LIST_CHILD_NODES = "germplasmListChildNodes";
	private static final String LISTS = "LISTS";

	public static final String GERMPLASM_LIST_TYPE_ADVANCE = "advance";
	public static final String GERMPLASM_LIST_TYPE_CROSS = "cross";
	public static final String NODE_NONE = "None";
	/**
	 * The Constant BATCH_SIZE.
	 */
	public static final int BATCH_SIZE = 500;

	/**
	 * The germplasm list manager.
	 */
	@Resource
	private GermplasmListManager germplasmListManager;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private UserDataManager userDataManager;

	@Resource
	private CrossingService crossingService;

	@Resource
	private NamingConventionService namingConventionService;

	private static final String NAME_NOT_UNIQUE = "Name not unique";
	private static final String HAS_CHILDREN = "Folder has children";
	private static final String FOLDER = "FOLDER";

	static final String IS_SUCCESS = "isSuccess";

	private static final String MESSAGE = "message";

	static final String DATE_FORMAT = "yyyyMMdd";

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private UserSelection userSelection;

	@Resource
	private UserTreeStateService userTreeStateService;

	@Resource
	private GermplasmDataManager germplasmDataManager;

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/saveList/{listIdentifier}", method = RequestMethod.GET)
	public String saveList(@ModelAttribute("saveListForm") final SaveListForm form, @PathVariable final String listIdentifier,
			final Model model) {

		try {
			form.setListDate(DateUtil.getCurrentDateInUIFormat());
			form.setListIdentifier(listIdentifier);
			final List<UserDefinedField> germplasmListTypes = this.germplasmListManager.getGermplasmListTypes();
			form.setListType(AppConstants.GERMPLASM_LIST_TYPE_HARVEST.getString());
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_TYPES, germplasmListTypes);

		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, GermplasmTreeController.COMMON_SAVE_GERMPLASM_LIST);
	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/saveList", method = RequestMethod.POST)
	public Map<String, Object> savePost(@ModelAttribute("saveListForm") final SaveListForm form, final Model model) {
		final Map<String, Object> results = new HashMap<>();

		try {
			final GermplasmList germplasmListIsNew =
					this.fieldbookMiddlewareService.getGermplasmListByName(form.getListName(), this.getCurrentProgramUUID());
			if (germplasmListIsNew == null && !this.isSimilarToRootFolderName(form.getListName())) {
				final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
				final Integer germplasmListId = this.saveGermplasmList(form, listDataItems);

				final List<GermplasmListData> data = new ArrayList<>();
				data.addAll(this.germplasmListManager.getGermplasmListDataByListId(germplasmListId));
				final List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProjectFromGermplasmListData(data);

				final Integer listDataProjectListId =
						this.saveListDataProjectList(form.getGermplasmListType(), germplasmListId, listDataProject);
				results.put(GermplasmTreeController.IS_SUCCESS, 1);
				results.put("germplasmListId", germplasmListId);
				results.put("uniqueId", form.getListIdentifier());
				results.put("listName", form.getListName());

				if (GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE.equals(form.getGermplasmListType())) {
					results.put("advancedGermplasmListId", listDataProjectListId);
				} else if (GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS.equals(form.getGermplasmListType())) {
					results.put("crossesListId", listDataProjectListId);
				}
			} else {
				results.put(GermplasmTreeController.IS_SUCCESS, 0);
				results.put(GermplasmTreeController.MESSAGE,
						this.messageSource.getMessage("germplasm.save.list.name.unique.error", null, LocaleContextHolder.getLocale()));
			}
		} catch (final RulesNotConfiguredException rnce) {
			GermplasmTreeController.LOG.error(rnce.getMessage(), rnce);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE, rnce.getMessage());
		} catch (final RuleException re) {
			GermplasmTreeController.LOG.error(re.getMessage(), re);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE,
					this.messageSource.getMessage("germplasm.naming.failed", null, LocaleContextHolder.getLocale()));
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}

		return results;
	}

	@ResponseBody
	@RequestMapping(value = "/updateCrossesList", method = RequestMethod.POST)
	public Map<String, Object> updateCrossesList(final Model model, final HttpSession session) {
		final Map<String, Object> results = new HashMap<>();

		try {
			final List<Pair<Germplasm, GermplasmListData>> listDataItems = new ArrayList<>();
			final String crossesListId = (String) session.getAttribute("createdCrossesListId");

			if (crossesListId != null) {
				final Integer germplasmListId = Integer.parseInt(crossesListId);
				final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(germplasmListId);
				this.updateGermplasmList(germplasmListId, listDataItems);
				session.removeAttribute("createdCrossesListId");

				final List<GermplasmListData> data = new ArrayList<>();
				data.addAll(this.germplasmListManager.getGermplasmListDataByListId(germplasmListId));
				final List<ListDataProject> listDataProject = ListDataProjectUtil.createListDataProjectFromGermplasmListData(data);

				final Integer listDataProjectListId =
						this.saveListDataProjectList(GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS, germplasmListId, listDataProject);
				results.put(GermplasmTreeController.IS_SUCCESS, 1);
				results.put("germplasmListId", germplasmListId);
				results.put("listName", germplasmList.getName());
				results.put("crossesListId", listDataProjectListId);
			} else {
				results.put(GermplasmTreeController.IS_SUCCESS, 0);
				results.put(GermplasmTreeController.MESSAGE,
						this.messageSource.getMessage("crossing.no.crossing.list", null, LocaleContextHolder.getLocale()));
			}
		} catch (final RulesNotConfiguredException rnce) {
			GermplasmTreeController.LOG.error(rnce.getMessage(), rnce);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE, rnce.getMessage());
		} catch (final RuleException re) {
			GermplasmTreeController.LOG.error(re.getMessage(), re);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE,
					this.messageSource.getMessage("germplasm.naming.failed", null, LocaleContextHolder.getLocale()));
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}

		return results;
	}

	private Integer updateGermplasmList(final Integer germplasmListId, final List<Pair<Germplasm, GermplasmListData>> listDataItems)
			throws RuleException {
		final GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(germplasmListId);
		final CrossSetting crossSetting = this.userSelection.getCrossSettings();
		final ImportedCrossesList importedCrossesList = this.userSelection.getImportedCrossesList();

		this.applyNamingSettingToCrosses(listDataItems, germplasmList, crossSetting, importedCrossesList);
		return this.fieldbookMiddlewareService.updateGermplasmList(listDataItems, germplasmList);
	}

	protected Integer saveGermplasmList(final SaveListForm form, final List<Pair<Germplasm, GermplasmListData>> listDataItems)
			throws RuleException {
		final Integer currentUserId = this.getCurrentIbdbUserId();
		final GermplasmList germplasmList = this.createGermplasmList(form, currentUserId);
		if (GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE.equals(form.getGermplasmListType())) {
			final AdvancingNurseryForm advancingNurseryForm = this.getPaginationListSelection().getAdvanceDetails(form.getListIdentifier());
			final List<Pair<Germplasm, List<Name>>> germplasms = new ArrayList<>();
			final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes = new ArrayList<>();

			this.populateGermplasmListDataFromAdvanced(germplasmList, advancingNurseryForm, germplasms, listDataItems, currentUserId,
					germplasmAttributes);
			return this.fieldbookMiddlewareService.saveNurseryAdvanceGermplasmList(germplasms, listDataItems, germplasmList,
					germplasmAttributes);
		} else if (GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS.equals(form.getGermplasmListType())) {
			final CrossSetting crossSetting = this.userSelection.getCrossSettings();
			final ImportedCrossesList importedCrossesList = this.userSelection.getImportedCrossesList();

			this.applyNamingSettingToCrosses(listDataItems, germplasmList, crossSetting, importedCrossesList);
			// Set imported user as owner of the list
			germplasmList.setUserId(importedCrossesList.getUserId());
			return this.fieldbookMiddlewareService.saveGermplasmList(listDataItems, germplasmList);
		} else {
			throw new IllegalArgumentException("Unknown germplasm list type supplied when saving germplasm list");
		}
	}

	private void checkForEmptyDesigNames(final List<ImportedCrosses> importedCrosses) throws RulesNotConfiguredException {
		boolean valid = true;
		for (final ImportedCrosses importedCross : importedCrosses) {
			if (StringUtils.isEmpty(importedCross.getDesig())) {
				valid = false;
			}
		}
		if (!valid) {
			throw new RulesNotConfiguredException(this.messageSource.getMessage("error.save.cross.rules.not.configured", null,
					"The rules" + " were not configured", LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Apply the naming setting to the crosses depending whether manual setting or rules based on the breeding method were selected
	 *
	 * @param listDataItems
	 * @param germplasmList
	 * @param crossSetting
	 * @param importedCrossesList
	 * @throws RuleException
	 */
	private void applyNamingSettingToCrosses(final List<Pair<Germplasm, GermplasmListData>> listDataItems,
			final GermplasmList germplasmList, final CrossSetting crossSetting, final ImportedCrossesList importedCrossesList)
			throws RuleException {

		// before continuing processing of the crosses, we process the breeding
		// method to be used for each imported cross
		// so that the correct information is available for further operations
		this.crossingService.processCrossBreedingMethod(crossSetting, importedCrossesList);

		if (crossSetting.isUseManualSettingsForNaming()) {
			// this line of code is where the creation of new germplasm takes
			// place
			this.crossingService.applyCrossSetting(crossSetting, importedCrossesList, this.getCurrentIbdbUserId(),
					this.userSelection.getWorkbook());
			this.populateGermplasmListData(germplasmList, listDataItems, importedCrossesList.getImportedCrosses());
		} else {
			final ImportedCrossesList importedCrossesListWithNamingSettings = this.applyNamingRules(crossSetting, importedCrossesList);
			// this line of code is where the creation of new germplasm takes
			// place
			this.crossingService.applyCrossSettingWithNamingRules(crossSetting, importedCrossesListWithNamingSettings,
					this.getCurrentIbdbUserId(), this.userSelection.getWorkbook());
			this.populateGermplasmListData(germplasmList, listDataItems, importedCrossesListWithNamingSettings.getImportedCrosses());
		}
		this.checkForEmptyDesigNames(importedCrossesList.getImportedCrosses());
	}

	private ImportedCrossesList applyNamingRules(final CrossSetting setting, final ImportedCrossesList importedCrossesList)
			throws RuleException {

		// TODO REFACTOR THIS

		final AdvancingSourceList list = new AdvancingSourceList();
		final List<AdvancingSource> rows = new ArrayList<>();

		final List<Integer> gids = new ArrayList<>();
		final List<ImportedCrosses> importedCrosses = importedCrossesList.getImportedCrosses();
		for (final ImportedCrosses cross : importedCrosses) {
			final Name name = new Name();
			name.setNstat(1);
			name.setNval(cross.getCross());
			final List<Name> names = new ArrayList<>();
			names.add(name);
			cross.setNames(names);
			final AdvancingSource advancingSource = new AdvancingSource(cross);
			// TODO add trail instance number
			advancingSource.setConditions(this.userSelection.getWorkbook().getConditions());
			advancingSource.setStudyType(this.userSelection.getWorkbook().getStudyDetails().getStudyType());
			advancingSource.setBreedingMethodId(cross.getBreedingMethodId());
			rows.add(advancingSource);
			if (cross.getGid() != null && NumberUtils.isNumber(cross.getGid())) {
				gids.add(Integer.valueOf(cross.getGid()));
			}
		}

		list.setRows(rows);

		final AdvancingNursery advancingParameters = new AdvancingNursery();

		advancingParameters.setCheckAdvanceLinesUnique(true);
		final List<ImportedCrosses> crosses = this.namingConventionService.generateCrossesList(importedCrosses, list, advancingParameters,
				this.userSelection.getWorkbook(), gids);

		importedCrossesList.setImportedGermplasms(crosses);
		this.userSelection.setImportedCrossesList(importedCrossesList);

		return importedCrossesList;
	}

	protected Integer saveListDataProjectList(final String germplasmListType, final Integer germplasmListId,
			final List<ListDataProject> dataProjectList) {
		final GermplasmListType type;
		final Integer currentUserID = this.getCurrentIbdbUserId();
		if (GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE.equals(germplasmListType)) {
			type = GermplasmListType.ADVANCED;

		} else if (GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS.equalsIgnoreCase(germplasmListType)) {
			type = GermplasmListType.CROSSES;
			// need to add the copying of the duplicate entry here
			FieldbookUtil.copyDupeNotesToListDataProject(dataProjectList, this.userSelection.getImportedCrossesList().getImportedCrosses());
		} else {
			throw new IllegalArgumentException("Unknown germplasm list type supplied when saving germplasm list");
		}

		int studyId = 0;
		if (this.userSelection.getWorkbook() != null && this.userSelection.getWorkbook().getStudyDetails() != null
				&& this.userSelection.getWorkbook().getStudyDetails().getId() != null) {
			studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
		}

		return this.fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, type, germplasmListId, dataProjectList, currentUserID);

	}

	/**
	 * Load initial germplasm tree for crosses.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/saveCrossesList", method = RequestMethod.GET)
	public String saveList(@ModelAttribute("saveListForm") final SaveListForm form, final Model model) {

		try {
			String listName = "";
			String listDescription = "";
			String listType = AppConstants.GERMPLASM_LIST_TYPE_GENERIC_LIST.getString();
			String listDate = DateUtil.getCurrentDateInUIFormat();

			if (this.userSelection.getImportedCrossesList() != null) {
				listName = this.userSelection.getImportedCrossesList().getName();
				listDescription = this.userSelection.getImportedCrossesList().getTitle();
				listType = this.userSelection.getImportedCrossesList().getType();
				listDate = DateUtil.getDateInUIFormat(this.userSelection.getImportedCrossesList().getDate());
			}

			form.setListName(listName);
			form.setListDescription(listDescription);
			form.setListType(listType);
			form.setListDate(listDate);

			final List<UserDefinedField> germplasmListTypes = this.germplasmListManager.getGermplasmListTypes();
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_TYPES, germplasmListTypes);

		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, GermplasmTreeController.COMMON_SAVE_GERMPLASM_LIST);
	}

	protected int saveCrossesList(final Integer germplasmListId, final List<ListDataProject> listDataProject, final Integer userId) {
		int studyId = 0;

		if (this.userSelection.getWorkbook() != null && this.userSelection.getWorkbook().getStudyDetails() != null
				&& this.userSelection.getWorkbook().getStudyDetails().getId() != null) {
			studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
		}

		final int crossesId = this.fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, GermplasmListType.CROSSES,
				germplasmListId, listDataProject, userId);
		this.userSelection.addImportedCrossesId(crossesId);
		return crossesId;
	}

	private GermplasmList createGermplasmList(final SaveListForm saveListForm, final Integer currentUserId) {

		// Create germplasm list
		final String listName = saveListForm.getListName();
		final String listType = saveListForm.getListType();

		final String description = saveListForm.getListDescription();
		GermplasmList parent = null;
		Integer parentId = null;
		GermplasmList gpList = null;
		if (saveListForm.getParentId() != null && !GermplasmTreeController.LISTS.equals(saveListForm.getParentId())) {
			parentId = Integer.valueOf(saveListForm.getParentId());
			try {
				gpList = this.germplasmListManager.getGermplasmListById(parentId);
			} catch (final MiddlewareQueryException e) {
				GermplasmTreeController.LOG.error(e.getMessage(), e);
			}
		}

		if (gpList != null && gpList.isFolder()) {

			parent = gpList;

		}

		final Integer status = 1;
		final Long dateLong = Long.valueOf(DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(), saveListForm.getListDate()));

		final GermplasmList germplasmList = new GermplasmList(null, listName, dateLong, listType, currentUserId, description, parent,
				status, saveListForm.getListNotes());
		germplasmList.setProgramUUID(this.getCurrentProgramUUID());
		return germplasmList;

	}

	private void populateGermplasmListData(final GermplasmList germplasmList, final List<Pair<Germplasm, GermplasmListData>> listDataItems,
			final List<ImportedCrosses> importedGermplasmList) {

		// Create germplasms to save - Map<Germplasm, List<Name>>
		for (final ImportedCrosses importedCrosses : importedGermplasmList) {
			final Integer gid = importedCrosses.getGid() != null ? Integer.valueOf(importedCrosses.getGid()) : null;

			final Germplasm germplasm = new Germplasm();
			germplasm.setGid(gid);

			// Create list data items to save - Map<Germplasm,
			// GermplasmListData>
			final Integer entryId = importedCrosses.getEntryId();
			final String entryCode = importedCrosses.getEntryCode();
			final String seedSource = importedCrosses.getSource();
			final String designation = importedCrosses.getDesig();
			final String notes = importedCrosses.getNotes();
			final Integer crossingDate = importedCrosses.getCrossingDate();
			String groupName = importedCrosses.getCross();

			// Common germplasm list data fields
			final Integer listDataId = importedCrosses.getId(); // null will be
																// set for new
																// records
			final Integer listDataStatus = 0;
			final Integer localRecordId = 0;

			if (groupName == null) {
				// Default value if null
				groupName = "-";
			}

			final GermplasmListData listData = new GermplasmListData(listDataId, germplasmList, gid, entryId, entryCode, seedSource,
					designation, groupName, listDataStatus, localRecordId, notes, crossingDate);

			listDataItems.add(new ImmutablePair<>(germplasm, listData));
		}
	}

	/**
	 * Creates the nursery advance germplasm list.
	 *
	 * @param form the form
	 * @param germplasms the germplasms
	 * @param listDataItems the list data items
	 * @return the germplasm list
	 */

	void populateGermplasmListDataFromAdvanced(final GermplasmList germplasmList, final AdvancingNurseryForm form,
			final List<Pair<Germplasm, List<Name>>> germplasms, final List<Pair<Germplasm, GermplasmListData>> listDataItems,
			final Integer currentUserID, final List<Pair<Germplasm, List<Attribute>>> germplasmAttributes) {

		final String harvestDate = form.getHarvestYear() + form.getHarvestMonth() + "00";

		// Common germplasm fields
		final Integer lgid = 0;
		Integer locationId = 0;
		final String harvestLocationId = form.getHarvestLocationId();
		if (harvestLocationId != null && !"".equals(harvestLocationId)) {
			locationId = Integer.valueOf(harvestLocationId);
		}
		final Integer gDate = DateUtil.getCurrentDateAsIntegerValue();

		// Common germplasm list data fields
		final Integer listDataId = null;
		final Integer listDataStatus = 0;
		final Integer localRecordId = 0;

		// Common name fields
		final Integer nRef = 0;
	
		Integer plotCodeFldNo = this.germplasmDataManager.getPlotCodeField().getFldno();
		Integer plotFldNo = 0;
		Integer trialInstanceFldNo = 0;
		Integer repFldNo = 0;
		// get FLDNOs for Attribute Objects to be created
		if (userSelection.isTrial()){
			plotFldNo = this.getPassportAttributeForCode("PLOT_NUMBER");
			repFldNo = this.getPassportAttributeForCode("REP_NUMBER");
			trialInstanceFldNo = this.getPassportAttributeForCode("INSTANCE_NUMBER");
		}

		// Create germplasms to save - Map<Germplasm, List<Name>>
		for (final ImportedGermplasm importedGermplasm : form.getGermplasmList()) {
			Integer gid = null;

			if (importedGermplasm.getGid() != null) {
				gid = Integer.valueOf(importedGermplasm.getGid());
			}

			final Integer methodId = importedGermplasm.getBreedingMethodId();
			final Integer gnpgs = importedGermplasm.getGnpgs();
			final Integer gpid1 = importedGermplasm.getGpid1();
			final Integer gpid2 = importedGermplasm.getGpid2();
			final Integer mgid = importedGermplasm.getMgid() == null ? 0 : importedGermplasm.getMgid();

			final List<Name> names = importedGermplasm.getNames();
			Name preferredName = names.get(0);

			for (final Name name : names) {

				name.setLocationId(locationId);
				name.setNdate(gDate);
				name.setUserId(currentUserID);
				name.setReferenceId(nRef);

				// If crop == CIMMYT WHEAT (crop with more than one name saved)
				// Germplasm name is the Names entry with NType = 1027, NVal =
				// table.desig, NStat = 0
				if (name.getNstat() == 0 && name.getTypeId() == GermplasmNameType.UNRESOLVED_NAME.getUserDefinedFieldID()) {
					preferredName = name;
				}
			}

			final Integer trueGdate = !"".equals(harvestDate.trim()) ? Integer.valueOf(harvestDate) : gDate;
			final Germplasm germplasm;
			germplasm = new Germplasm(gid, methodId, gnpgs, gpid1, gpid2, currentUserID, lgid, locationId, trueGdate, preferredName);

			germplasm.setMgid(mgid);

			germplasms.add(new ImmutablePair<>(germplasm, names));

			// Create list data items to save - Map<Germplasm,
			// GermplasmListData>
			final Integer entryId = importedGermplasm.getEntryId();
			final String entryCode = importedGermplasm.getEntryCode();
			final String seedSource = importedGermplasm.getSource();
			final String designation = importedGermplasm.getDesig();
			String groupName = importedGermplasm.getCross();

			if (groupName == null) {
				// Default value if null
				groupName = "-";
			}

			final GermplasmListData listData = new GermplasmListData(listDataId, germplasmList, gid, entryId, entryCode, seedSource,
					designation, groupName, listDataStatus, localRecordId);

			listDataItems.add(new ImmutablePair<>(germplasm, listData));

			final List<Attribute> attributesPerGermplasm = Lists.newArrayList();
			// Add the seed source/origin attribute (which is generated based on
			// format strings configured in crossing.properties) to the
			// germplasm in FieldbookServiceImpl.advanceNursery().
			// originAttribute gid will be set when saving once gid is known
			final Attribute originAttribute = this.createAttributeObject(currentUserID, importedGermplasm.getSource(),
					plotCodeFldNo, locationId, gDate);
			attributesPerGermplasm.add(originAttribute);

			// Adding Instance number, plot number and replication number as
			// attributes of germplasm for trial advancing
			if (this.userSelection.isTrial()) {
				final Attribute plotNumberAttribute = this.createAttributeObject(currentUserID, importedGermplasm.getPlotNumber(),
						plotFldNo, locationId, gDate);
				attributesPerGermplasm.add(plotNumberAttribute);

				final String replicationNumber = importedGermplasm.getReplicationNumber();
				if (StringUtils.isNotBlank(replicationNumber)) {
					final Attribute repNoAttribute = this.createAttributeObject(currentUserID, replicationNumber,
							repFldNo, locationId, gDate);
					attributesPerGermplasm.add(repNoAttribute);
				}

				final Attribute instanceNoAttribute = this.createAttributeObject(currentUserID, importedGermplasm.getTrialInstanceNumber(),
						trialInstanceFldNo, locationId, gDate);
				attributesPerGermplasm.add(instanceNoAttribute);
			}

			germplasmAttributes.add(new ImmutablePair<Germplasm, List<Attribute>>(germplasm, Lists.newArrayList(attributesPerGermplasm)));
		}
	}

	private Integer getPassportAttributeForCode(final String code) {
		return this.germplasmDataManager.getUserDefinedFieldByTableTypeAndCode("ATRIBUTS", "PASSPORT", code).getFldno();
	}

	private Attribute createAttributeObject(final Integer currentUserID, final String attributeValue, final Integer typeId,
			final Integer locationId, final Integer gDate) {
		final Attribute originAttribute = new Attribute();
		originAttribute.setAval(attributeValue);
		originAttribute.setTypeId(typeId);
		originAttribute.setUserId(currentUserID);
		originAttribute.setAdate(gDate);
		originAttribute.setLocationId(locationId);
		return originAttribute;
	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/germplasm/detail/url", method = RequestMethod.GET)
	public String getGermplasmUrl() {

		return this.fieldbookProperties.getGermplasmDetailsUrl();
	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/germplasm/import/url", method = RequestMethod.GET)
	public String getImportGermplasmUrl(final HttpServletRequest request) {
		final String contextParams = org.generationcp.commons.util.ContextUtil.getContextParameterString(request);
		return this.fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams;
	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/loadInitGermplasmTree/{isFolderOnly}", method = RequestMethod.GET)
	public String loadInitialGermplasmTree(@PathVariable final String isFolderOnly) {
		final List<TreeNode> rootNodes = new ArrayList<>();
		rootNodes.add(new TreeNode(GermplasmTreeController.LISTS, AppConstants.LISTS.getString(), true, "lead",
				AppConstants.FOLDER_ICON_PNG.getString(), this.getCurrentProgramUUID()));
		return TreeViewUtil.convertTreeViewToJson(rootNodes);

	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/loadInitGermplasmTreeTable", method = RequestMethod.GET)
	public String loadInitialGermplasmTreeTable(final Model model) {
		final List<TreeTableNode> rootNodes = new ArrayList<>();
		final TreeTableNode localNode =
				new TreeTableNode(GermplasmTreeController.LISTS, AppConstants.LISTS.getString(), null, null, null, null, "1");
		rootNodes.add(localNode);
		model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_ROOT_NODES, rootNodes);
		return super.showAjaxPage(model, GermplasmTreeController.GERMPLASM_LIST_TABLE_PAGE);
	}

	protected List<GermplasmList> getGermplasmListChildren(final String id, final String programUUID) {
		List<GermplasmList> children = new ArrayList<>();
		if (GermplasmTreeController.LISTS.equals(id)) {
			children = this.germplasmListManager.getAllTopLevelLists(programUUID);
		} else if (NumberUtils.isNumber(id)) {
			final int parentId = Integer.valueOf(id);
			children = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentId, programUUID,
					GermplasmTreeController.BATCH_SIZE);
		} else {
			GermplasmTreeController.LOG.error("germplasm id = " + id + " is not a number");
		}
		return children;
	}

	protected List<TreeTableNode> getGermplasmListFolderChildNodes(final TreeTableNode node, final String programUUID) {
		final List<TreeTableNode> childNodes = this.getGermplasmListFolderChildNodes(node.getId(), programUUID);
		if (childNodes != null) {
			node.setNumOfChildren(Integer.toString(childNodes.size()));
		} else {
			node.setNumOfChildren("0");
		}
		return childNodes;
	}

	protected List<TreeTableNode> getGermplasmListFolderChildNodes(final String id, final String programUUID) {
		List<TreeTableNode> childNodes = new ArrayList<>();
		if (id != null && !"".equals(id)) {
			childNodes = this.getGermplasmFolderChildrenNode(id, programUUID);
		}
		return childNodes;
	}

	private List<TreeNode> getGermplasmChildNodes(final String parentKey, final boolean isFolderOnly, final String programUUID) {
		if (!(parentKey != null && !"".equals(parentKey))) {
			return new ArrayList<>();
		}

		final List<GermplasmList> rootLists;
		if (GermplasmTreeController.LISTS.equals(parentKey)) {
			rootLists = this.germplasmListManager.getAllTopLevelLists(programUUID);
		} else if (NumberUtils.isNumber(parentKey)) {
			rootLists = this.getGermplasmChildrenNode(parentKey, isFolderOnly, programUUID);
		} else {
			throw new IllegalStateException("Add a message");
		}
		final List<TreeNode> childNodes = TreeViewUtil.convertGermplasmListToTreeView(rootLists, isFolderOnly);

		final Map<Integer, GermplasmFolderMetadata> allListMetaData = germplasmListManager.getGermplasmFolderMetadata(rootLists);

		for (final TreeNode newNode : childNodes) {
			GermplasmFolderMetadata nodeMetaData = allListMetaData.get(Integer.parseInt(newNode.getKey()));
			if (nodeMetaData != null && nodeMetaData.getNumberOfChildren() > 0) {
				newNode.setIsLazy(true);
			}
		}
		return childNodes;
	}

	private List<GermplasmList> getGermplasmChildrenNode(final String parentKey, final boolean isFolderOnly, final String programUUID) {
		final int parentId = Integer.valueOf(parentKey);
		return this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentId, programUUID,
				GermplasmTreeController.BATCH_SIZE);
	}

	private List<TreeTableNode> getGermplasmFolderChildrenNode(final String id, final String programUUID) {
		return TreeViewUtil.convertGermplasmListToTreeTableNodes(this.getGermplasmListChildren(id, programUUID), this.germplasmListManager);
	}

	/**
	 * Load initial germplasm tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/germplasm/list/header/details/{listId}", method = RequestMethod.GET)
	public Map<String, Object> getGermplasmListHeaderDetails(@PathVariable final int listId) {
		final Map<String, Object> dataResults = new HashMap<>();
		try {
			final GermplasmList germplasmList = this.fieldbookMiddlewareService.getGermplasmListById(listId);
			dataResults.put("name", germplasmList.getName());
			dataResults.put("description", germplasmList.getDescription());
			Integer listRef = germplasmList.getListRef();
			if(listRef != null) {
				final GermplasmList parentGermplasmList = this.fieldbookMiddlewareService.getGermplasmListById(listRef);
				dataResults.put("type", this.getTypeString(parentGermplasmList.getType()));
			} else {
				dataResults.put("type", this.getTypeString(germplasmList.getType()));
			}

			String statusValue = "Unlocked List";
			if (germplasmList.getStatus() >= 100) {
				statusValue = "Locked List";
			}

			dataResults.put("status", statusValue);
			dataResults.put("date", germplasmList.getDate());
			dataResults.put("owner", this.fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));
			dataResults.put("notes", germplasmList.getNotes());
			if (germplasmList.getType() != null
					&& (germplasmList.getType().equalsIgnoreCase(GermplasmListType.NURSERY.toString())
							|| germplasmList.getType().equalsIgnoreCase(GermplasmListType.TRIAL.toString()))
					|| germplasmList.getType().equalsIgnoreCase(GermplasmListType.CHECK.toString())
					|| germplasmList.getType().equalsIgnoreCase(GermplasmListType.CROSSES.toString())
					|| germplasmList.getType().equalsIgnoreCase(GermplasmListType.ADVANCED.toString())) {
				dataResults.put("totalEntries", this.fieldbookMiddlewareService.countListDataProjectGermplasmListDataByListId(listId));
			} else {
				dataResults.put("totalEntries", this.fieldbookMiddlewareService.countGermplasmListDataByListId(listId));
			}

		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return dataResults;
	}

	private String getTypeString(final String typeCode) {
		try {
			final List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();

			for (final UserDefinedField listType : listTypes) {
				if (typeCode.equals(listType.getFcode())) {
					return listType.getFname();
				}
			}
		} catch (final MiddlewareQueryException ex) {
			GermplasmTreeController.LOG.error("Error in getting list types.", ex);
			return "Error in getting list types.";
		}

		return "Germplasm List";
	}

	/**
	 * Expand germplasm list folder.
	 *
	 * @param id the germplasm list ID
	 * @return the response page
	 */
	@RequestMapping(value = "/expandGermplasmListFolder/{id}", method = RequestMethod.GET)
	public String expandGermplasmListFolder(@PathVariable final String id, final Model model) {
		try {
			final List<TreeTableNode> childNodes = this.getGermplasmListFolderChildNodes(id, this.getCurrentProgramUUID());
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_CHILD_NODES, childNodes);
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, GermplasmTreeController.GERMPLASM_LIST_TABLE_ROWS_PAGE);
	}

	/**
	 * Expand germplasm tree.
	 *
	 * @param parentKey the parent key
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/expandGermplasmTree/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String expandGermplasmTree(@PathVariable final String parentKey, @PathVariable final String isFolderOnly) {
		final boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly);
		try {
			final List<TreeNode> childNodes = this.getGermplasmChildNodes(parentKey, isFolderOnlyBool, this.getCurrentProgramUUID());
			return TreeViewUtil.convertTreeViewToJson(childNodes);
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/expandGermplasmTree/{parentKey}", method = RequestMethod.GET)
	public String expandGermplasmAllTree(@PathVariable final String parentKey) {
		return this.expandGermplasmTree(parentKey, "0");
	}

	protected void checkIfUnique(final String folderName, final String programUUID) throws MiddlewareException {
		final List<GermplasmList> duplicate = this.germplasmListManager.getGermplasmListByName(folderName, programUUID, 0, 1, null);
		if (duplicate != null && !duplicate.isEmpty()) {
			throw new MiddlewareException(GermplasmTreeController.NAME_NOT_UNIQUE);
		}
		if (this.isSimilarToRootFolderName(folderName)) {
			throw new MiddlewareException(GermplasmTreeController.NAME_NOT_UNIQUE);
		}
	}

	protected boolean isSimilarToRootFolderName(final String itemName) {
		return itemName.equalsIgnoreCase(AppConstants.LISTS.getString());

	}

	@ResponseBody
	@RequestMapping(value = "/addGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> addGermplasmFolder(final HttpServletRequest req) {
		final String id = req.getParameter("parentFolderId");
		final String folderName = req.getParameter("folderName");
		final Map<String, Object> resultsMap = new HashMap<>();

		GermplasmList gpList = null;
		GermplasmList newList = null;
		try {
			final String programUUID = this.getCurrentProgramUUID();
			this.checkIfUnique(folderName, programUUID);
			final Integer userId = this.getCurrentIbdbUserId();

			if (id == null) {
				newList = new GermplasmList(null, folderName,
						Long.valueOf(new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance().getTime())),
						GermplasmTreeController.FOLDER, userId, folderName, null, 0);
			} else {
				gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(id));

				if (gpList != null && !gpList.isFolder()) {
					GermplasmList parent = null;

					parent = gpList.getParent();

					if (parent == null) {
						newList = new GermplasmList(null, folderName,
								Long.valueOf(
										new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance().getTime())),
								GermplasmTreeController.FOLDER, userId, folderName, null, 0);
					} else {
						newList = new GermplasmList(null, folderName,
								Long.valueOf(
										new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance().getTime())),
								GermplasmTreeController.FOLDER, userId, folderName, parent, 0);
					}
				} else {
					newList = new GermplasmList(null, folderName,
							Long.valueOf(
									new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance().getTime())),
							GermplasmTreeController.FOLDER, userId, folderName, gpList, 0);
				}

			}

			newList.setDescription(folderName);
			newList.setProgramUUID(programUUID);
			final Integer germplasmListFolderId = this.germplasmListManager.addGermplasmList(newList);
			resultsMap.put("id", germplasmListFolderId);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/renameGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> renameStudyFolder(final HttpServletRequest req) {
		final Map<String, Object> resultsMap = new HashMap<>();
		final String newName = req.getParameter("newFolderName");
		final String folderId = req.getParameter("folderId");

		try {

			final GermplasmList gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(folderId));

			this.checkIfUnique(newName, this.getCurrentProgramUUID());
			gpList.setName(newName);

			this.germplasmListManager.updateGermplasmList(gpList);

			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	public boolean hasChildren(final Integer id, final String programUUID) throws MiddlewareQueryException {
		return !this.germplasmListManager.getGermplasmListByParentFolderId(id, programUUID).isEmpty();
	}

	@ResponseBody
	@RequestMapping(value = "/deleteGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> deleteGermplasmFolder(final HttpServletRequest req) {
		final Map<String, Object> resultsMap = new HashMap<>();

		GermplasmList gpList = null;
		final String folderId = req.getParameter("folderId");
		try {
			gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(folderId));

			if (this.hasChildren(gpList.getId(), this.getCurrentProgramUUID())) {
				throw new MiddlewareException(GermplasmTreeController.HAS_CHILDREN);
			}
			this.germplasmListManager.deleteGermplasmList(gpList);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/moveGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> moveStudyFolder(final HttpServletRequest req) {
		final String sourceId = req.getParameter("sourceId");
		final String targetId = req.getParameter("targetId");

		final Map<String, Object> resultsMap = new HashMap<>();

		try {
			final GermplasmList gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(sourceId));

			if (targetId != null) {
				final GermplasmList parent = this.germplasmListManager.getGermplasmListById(Integer.parseInt(targetId));
				gpList.setParent(parent);
			} else {
				gpList.setParent(null);
			}

			this.germplasmListManager.updateGermplasmList(gpList);

		} catch (final Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/save/state/{type}")
	public String saveTreeState(@PathVariable final String type, @RequestParam(value = "expandedNodes[]") final String[] expandedNodes) {
		GermplasmTreeController.LOG.debug("Save the debug nodes");
		final List<String> states = new ArrayList<>();
		String status = "OK";
		try {
			
			if (!GermplasmTreeController.NODE_NONE.equalsIgnoreCase(expandedNodes[0])) {
				for (int index = 0; index < expandedNodes.length; index++) {
					states.add(expandedNodes[index]);
				}
			}

			if(states.isEmpty()) {
				states.add(DEFAULT_STATE_SAVED_FOR_GERMPLASM_LIST);
			}
			
			this.userTreeStateService.saveOrUpdateUserProgramTreeState(this.contextUtil.getCurrentUserLocalId(),
					this.getCurrentProgramUUID(), type, states);
		} catch (final MiddlewareQueryException e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			status = "ERROR";
		}
		return status;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieve/state/{type}/{saveMode}", method = RequestMethod.GET)
	public String retrieveTreeState(@PathVariable final String type, @PathVariable final Boolean saveMode) {

		final List<String> stateList;
		final Integer userID = this.contextUtil.getCurrentUserLocalId();
		final String programUUID = this.getCurrentProgramUUID();
		if (saveMode) {
			stateList = this.userTreeStateService.getUserProgramTreeStateForSaveList(userID, programUUID);
		} else {
			stateList = this.userTreeStateService.getUserProgramTreeStateByUserIdProgramUuidAndType(userID, programUUID, type);
		}
		
		return super.convertObjectToJson(stateList);
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	@Override
	public String getContentName() {
		return null;
	}

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected void setUserSelection(final UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
}
