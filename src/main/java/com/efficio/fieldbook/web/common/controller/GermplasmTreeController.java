/* Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.common.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
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
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ListDataProjectUtil;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;

/**
 * The Class GermplasmTreeController.
 */
@Controller
@RequestMapping(value = "/ListTreeManager")
public class GermplasmTreeController extends AbstractBaseFieldbookController {

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
	public static final int BATCH_SIZE = 50;

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

	private static final String NAME_NOT_UNIQUE = "Name not unique";
	private static final String HAS_CHILDREN = "Folder has children";
	private static final String FOLDER = "FOLDER";

	private static final String IS_SUCCESS = "isSuccess";

	private static final String MESSAGE = "message";

	private static final String DATE_FORMAT = "yyyyMMdd";

	@Resource
	private ResourceBundleMessageSource messageSource;
	@Resource
	private UserSelection userSelection;
	@Resource
	public ContextUtil contextUtil;
	@Resource
	private UserProgramStateDataManager userProgramStateDataManager;

	/**
	 * Load initial germplasm tree.
	 * 
	 * @return the string
	 */
	@RequestMapping(value = "/saveList/{listIdentifier}", method = RequestMethod.GET)
	public String saveList(@ModelAttribute("saveListForm") SaveListForm form,
			@PathVariable String listIdentifier, Model model, HttpSession session) {

		try {
			form.setListDate(DateUtil.getCurrentDateInUIFormat());
			form.setListIdentifier(listIdentifier);
			List<UserDefinedField> germplasmListTypes = this.germplasmListManager
					.getGermplasmListTypes();
			form.setListType(AppConstants.GERMPLASM_LIST_TYPE_HARVEST.getString());
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_TYPES, germplasmListTypes);

		} catch (Exception e) {
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
	public Map<String, Object> savePost(@ModelAttribute("saveListForm") SaveListForm form,
			Model model, HttpSession session) {
		Map<String, Object> results = new HashMap<>();

		try {
			GermplasmList germplasmListIsNew = this.fieldbookMiddlewareService
					.getGermplasmListByName(form.getListName());
			if (germplasmListIsNew == null && !this.isSimilarToRootFolderName(form.getListName())) {
				Map<Germplasm, GermplasmListData> listDataItems = new HashMap<>();
				Integer germplasmListId = this.saveGermplasmList(form, listDataItems);

				List<GermplasmListData> data = new ArrayList<GermplasmListData>();
				data.addAll(this.germplasmListManager.getGermplasmListDataByListId(germplasmListId,
						0, Integer.MAX_VALUE));
				List<ListDataProject> listDataProject = ListDataProjectUtil
						.createListDataProjectFromGermplasmListData(data);

				Integer listDataProjectListId = this.saveListDataProjectList(form, germplasmListId,
						listDataProject);
				results.put(GermplasmTreeController.IS_SUCCESS, 1);
				results.put("germplasmListId", germplasmListId);
				results.put("uniqueId", form.getListIdentifier());
				results.put("listName", form.getListName());

				if (GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE.equals(form
						.getGermplasmListType())) {
					results.put("advancedGermplasmListId", listDataProjectListId);
				} else if (GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS.equals(form
						.getGermplasmListType())) {
					results.put("crossesListId", listDataProjectListId);
				}
			} else {
				results.put(GermplasmTreeController.IS_SUCCESS, 0);
				String nameUniqueError = "germplasm.save.list.name.unique.error";
				Locale locale = LocaleContextHolder.getLocale();
				results.put(GermplasmTreeController.MESSAGE,
						this.messageSource.getMessage(nameUniqueError, null, locale));
			}
		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			results.put(GermplasmTreeController.IS_SUCCESS, 0);
			results.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}

		return results;
	}

	protected Integer saveGermplasmList(SaveListForm form,
			Map<Germplasm, GermplasmListData> listDataItems) throws MiddlewareQueryException {
		Integer currentUserId = this.getCurrentIbdbUserId();
		GermplasmList germplasmList = this.createGermplasmList(form, currentUserId);
		if (GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE.equals(form.getGermplasmListType())) {
			AdvancingNurseryForm advancingNurseryForm = this.getPaginationListSelection()
					.getAdvanceDetails(form.getListIdentifier());
			Map<Germplasm, List<Name>> germplasms = new HashMap<>();

			this.populateGermplasmListDataFromAdvanced(germplasmList, advancingNurseryForm, form,
					germplasms, listDataItems, currentUserId);
			return this.fieldbookMiddlewareService.saveNurseryAdvanceGermplasmList(germplasms,
					listDataItems, germplasmList);
		} else if (GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS.equals(form
				.getGermplasmListType())) {
			CrossSetting crossSetting = this.userSelection.getCrossSettings();
			ImportedCrossesList importedCrossesList = this.userSelection.getImportedCrossesList();
			this.crossingService.applyCrossSetting(crossSetting, importedCrossesList,
					this.getCurrentIbdbUserId());
			this.populateGermplasmListData(germplasmList, listDataItems,
					importedCrossesList.getImportedCrosses());
			return this.fieldbookMiddlewareService.saveGermplasmList(listDataItems, germplasmList);
		} else {
			throw new IllegalArgumentException(
					"Unknown germplasm list type supplied when saving germplasm list");
		}
	}

	protected Integer saveListDataProjectList(SaveListForm form, Integer germplasmListId,
			List<ListDataProject> dataProjectList) throws MiddlewareException {
		GermplasmListType type;
		Integer currentUserID = this.getCurrentIbdbUserId();
		if (GermplasmTreeController.GERMPLASM_LIST_TYPE_ADVANCE.equals(form.getGermplasmListType())) {
			type = GermplasmListType.ADVANCED;

		} else if (GermplasmTreeController.GERMPLASM_LIST_TYPE_CROSS.equals(form
				.getGermplasmListType())) {
			type = GermplasmListType.CROSSES;
			// need to add the copying of the duplicate entry here
			FieldbookUtil.copyDupeNotesToListDataProject(dataProjectList, this.userSelection
					.getImportedCrossesList().getImportedCrosses());
		} else {
			throw new IllegalArgumentException(
					"Unknown germplasm list type supplied when saving germplasm list");
		}

		int studyId = 0;
		if (this.userSelection.getWorkbook() != null
				&& this.userSelection.getWorkbook().getStudyDetails() != null
				&& this.userSelection.getWorkbook().getStudyDetails().getId() != null) {
			studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
		}

		return this.fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId, type,
				germplasmListId, dataProjectList, currentUserID);

	}

	/**
	 * Load initial germplasm tree for crosses.
	 * 
	 * @return the string
	 */
	@RequestMapping(value = "/saveCrossesList", method = RequestMethod.GET)
	public String saveList(@ModelAttribute("saveListForm") SaveListForm form, Model model,
			HttpSession session) {

		try {
			String listName = "";
			String listDescription = "";
			String listType = AppConstants.GERMPLASM_LIST_TYPE_GENERIC_LIST.getString();
			String listDate = DateUtil.getCurrentDateInUIFormat();

			if (this.userSelection.getImportedCrossesList() != null) {
				listName = this.userSelection.getImportedCrossesList().getName();
				listDescription = this.userSelection.getImportedCrossesList().getTitle();
				listType = this.userSelection.getImportedCrossesList().getType();
				listDate = DateUtil.getDateInUIFormat(this.userSelection.getImportedCrossesList()
						.getDate());
			}

			form.setListName(listName);
			form.setListDescription(listDescription);
			form.setListType(listType);
			form.setListDate(listDate);

			List<UserDefinedField> germplasmListTypes = this.germplasmListManager
					.getGermplasmListTypes();
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_TYPES, germplasmListTypes);

		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, GermplasmTreeController.COMMON_SAVE_GERMPLASM_LIST);
	}

	protected int saveCrossesList(Integer germplasmListId, List<ListDataProject> listDataProject,
			Integer userId) throws MiddlewareQueryException {
		int studyId = 0;

		if (this.userSelection.getWorkbook() != null
				&& this.userSelection.getWorkbook().getStudyDetails() != null
				&& this.userSelection.getWorkbook().getStudyDetails().getId() != null) {
			studyId = this.userSelection.getWorkbook().getStudyDetails().getId();
		}

		int crossesId = this.fieldbookMiddlewareService.saveOrUpdateListDataProject(studyId,
				GermplasmListType.CROSSES, germplasmListId, listDataProject, userId);
		this.userSelection.addImportedCrossesId(crossesId);
		return crossesId;
	}

	private GermplasmList createGermplasmList(SaveListForm saveListForm, Integer currentUserId) {

		// Create germplasm list
		String listName = saveListForm.getListName();
		String listType = saveListForm.getListType();

		String description = saveListForm.getListDescription();
		GermplasmList parent = null;
		Integer parentId = null;
		GermplasmList gpList = null;
		if (saveListForm.getParentId() != null
				&& !GermplasmTreeController.LISTS.equals(saveListForm.getParentId())) {
			parentId = Integer.valueOf(saveListForm.getParentId());
			try {
				gpList = this.germplasmListManager.getGermplasmListById(parentId);
			} catch (MiddlewareQueryException e) {
				GermplasmTreeController.LOG.error(e.getMessage(), e);
			}
		}

		if (gpList != null && gpList.isFolder()) {

			parent = gpList;

		}

		Integer status = 1;
		Long dateLong = Long.valueOf(DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(),
				saveListForm.getListDate()));

		return new GermplasmList(null, listName, dateLong, listType, currentUserId, description,
				parent, status, saveListForm.getListNotes());

	}

	private void populateGermplasmListData(GermplasmList germplasmList,
			Map<Germplasm, GermplasmListData> listDataItems,
			List<ImportedCrosses> importedGermplasmList) {
		// Common germplasm list data fields
		Integer listDataId = null;
		Integer listDataStatus = 0;
		Integer localRecordId = 0;

		// Create germplasms to save - Map<Germplasm, List<Name>>
		for (ImportedCrosses importedCrosses : importedGermplasmList) {
			Integer gid = Integer.valueOf(importedCrosses.getGid());

			Germplasm germplasm = new Germplasm();
			germplasm.setGid(gid);

			// Create list data items to save - Map<Germplasm,
			// GermplasmListData>
			Integer entryId = importedCrosses.getEntryId();
			String entryCode = importedCrosses.getEntryCode();
			String seedSource = importedCrosses.getSource();
			String designation = importedCrosses.getDesig();
			String groupName = importedCrosses.getCross();

			if (groupName == null) {
				// Default value if null
				groupName = "-";
			}

			GermplasmListData listData = new GermplasmListData(listDataId, germplasmList, gid,
					entryId, entryCode, seedSource, designation, groupName, listDataStatus,
					localRecordId);

			listDataItems.put(germplasm, listData);
		}
	}

	/**
	 * Creates the nursery advance germplasm list.
	 * 
	 * @param form
	 *            the form
	 * @param germplasms
	 *            the germplasms
	 * @param listDataItems
	 *            the list data items
	 * @return the germplasm list
	 */

	private void populateGermplasmListDataFromAdvanced(GermplasmList germplasmList,
			AdvancingNurseryForm form, SaveListForm saveListForm,
			Map<Germplasm, List<Name>> germplasms, Map<Germplasm, GermplasmListData> listDataItems,
			Integer currentUserID) {

		String harvestDate = form.getHarvestYear() + form.getHarvestMonth() + "00";

		// Common germplasm fields
		Integer lgid = 0;
		Integer locationId = 0;
		String harvestLocationId = form.getHarvestLocationId();
		if (harvestLocationId != null && !"".equals(harvestLocationId)) {
			locationId = Integer.valueOf(harvestLocationId);
		}
		Integer gDate = DateUtil.getCurrentDateAsIntegerValue();

		// Common germplasm list data fields
		Integer listDataId = null;
		Integer listDataStatus = 0;
		Integer localRecordId = 0;

		// Common name fields
		Integer nDate = gDate;
		Integer nRef = 0;

		// Create germplasms to save - Map<Germplasm, List<Name>>
		for (ImportedGermplasm importedGermplasm : form.getGermplasmList()) {

			Integer gid = null;

			if (importedGermplasm.getGid() != null) {
				gid = Integer.valueOf(importedGermplasm.getGid());
			}

			Integer methodId = importedGermplasm.getBreedingMethodId();
			Integer gnpgs = importedGermplasm.getGnpgs();
			Integer gpid1 = importedGermplasm.getGpid1();
			Integer gpid2 = importedGermplasm.getGpid2();

			List<Name> names = importedGermplasm.getNames();
			Name preferredName = names.get(0);

			for (Name name : names) {

				name.setLocationId(locationId);
				name.setNdate(nDate);
				name.setUserId(currentUserID);
				name.setReferenceId(nRef);

				// If crop == CIMMYT WHEAT (crop with more than one name saved)
				// Germplasm name is the Names entry with NType = 1027, NVal =
				// table.desig, NStat = 0
				if (name.getNstat() == 0
						&& name.getTypeId() == GermplasmNameType.UNRESOLVED_NAME
								.getUserDefinedFieldID()) {
					preferredName = name;
				}
			}

			Integer trueGdate = harvestDate != null && !"".equals(harvestDate.trim()) ? Integer
					.valueOf(harvestDate) : gDate;
			Germplasm germplasm = new Germplasm(gid, methodId, gnpgs, gpid1, gpid2, currentUserID,
					lgid, locationId, trueGdate, preferredName);

			germplasms.put(germplasm, names);

			// Create list data items to save - Map<Germplasm,
			// GermplasmListData>
			Integer entryId = importedGermplasm.getEntryId();
			String entryCode = importedGermplasm.getEntryCode();
			String seedSource = importedGermplasm.getSource();
			String designation = importedGermplasm.getDesig();
			String groupName = importedGermplasm.getCross();
			if (groupName == null) {
				// Default value if null
				groupName = "-";
			}

			GermplasmListData listData = new GermplasmListData(listDataId, germplasmList, gid,
					entryId, entryCode, seedSource, designation, groupName, listDataStatus,
					localRecordId);

			listDataItems.put(germplasm, listData);
		}
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
	public String getImportGermplasmUrl(HttpServletRequest request) {
		String contextParams = org.generationcp.commons.util.ContextUtil
				.getContextParameterString(request);
		return this.fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams;
	}

	/**
	 * Load initial germplasm tree.
	 * 
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/loadInitGermplasmTree/{isFolderOnly}", method = RequestMethod.GET)
	public String loadInitialGermplasmTree(@PathVariable String isFolderOnly) {
		try {
			List<TreeNode> rootNodes = new ArrayList<TreeNode>();
			rootNodes.add(new TreeNode(GermplasmTreeController.LISTS, AppConstants.LISTS
					.getString(), true, "lead", AppConstants.FOLDER_ICON_PNG.getString(),
					this.contextUtil.getCurrentProgramUUID()));
			return TreeViewUtil.convertTreeViewToJson(rootNodes);

		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	/**
	 * Load initial germplasm tree.
	 * 
	 * @return the string
	 */
	@RequestMapping(value = "/loadInitGermplasmTreeTable", method = RequestMethod.GET)
	public String loadInitialGermplasmTreeTable(Model model) {
		try {
			List<TreeTableNode> rootNodes = new ArrayList<TreeTableNode>();
			TreeTableNode localNode = new TreeTableNode(GermplasmTreeController.LISTS,
					AppConstants.LISTS.getString(), null, null, null, null, "1");
			rootNodes.add(localNode);
			rootNodes.addAll(this.getGermplasmListFolderChildNodes(localNode));
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_ROOT_NODES, rootNodes);
		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, GermplasmTreeController.GERMPLASM_LIST_TABLE_PAGE);
	}

	protected void markIfHasChildren(TreeTableNode node) throws MiddlewareQueryException {
		List<GermplasmList> children = this.getGermplasmListChildren(node.getId());
		node.setNumOfChildren(Integer.toString(children.size()));
	}

	protected List<GermplasmList> getGermplasmListChildren(String id)
			throws MiddlewareQueryException {
		List<GermplasmList> children = new ArrayList<GermplasmList>();
		if (GermplasmTreeController.LISTS.equals(id)) {
			children = this.germplasmListManager
					.getAllTopLevelListsBatched(GermplasmTreeController.BATCH_SIZE);
		} else if (NumberUtils.isNumber(id)) {
			int parentId = Integer.valueOf(id);
			children = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentId,
					GermplasmTreeController.BATCH_SIZE);
		} else {
			GermplasmTreeController.LOG.error("germplasm id = " + id + " is not a number");
		}
		return children;
	}

	protected List<TreeTableNode> getGermplasmListFolderChildNodes(TreeTableNode node)
			throws MiddlewareQueryException {
		List<TreeTableNode> childNodes = this.getGermplasmListFolderChildNodes(node.getId());
		if (childNodes != null) {
			node.setNumOfChildren(Integer.toString(childNodes.size()));
		} else {
			node.setNumOfChildren("0");
		}
		return childNodes;
	}

	protected List<TreeTableNode> getGermplasmListFolderChildNodes(String id)
			throws MiddlewareQueryException {
		List<TreeTableNode> childNodes = new ArrayList<TreeTableNode>();
		if (id != null && !"".equals(id)) {
			childNodes = this.getGermplasmFolderChildrenNode(id);
			for (TreeTableNode newNode : childNodes) {
				this.markIfHasChildren(newNode);
			}
		}
		return childNodes;
	}

	private List<TreeNode> getGermplasmChildNodes(String parentKey, boolean isFolderOnly)
			throws MiddlewareQueryException {
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		if (parentKey != null && !"".equals(parentKey)) {
			try {
				if (GermplasmTreeController.LISTS.equals(parentKey)) {
					List<GermplasmList> rootLists = this.germplasmListManager
							.getAllTopLevelListsBatched(GermplasmTreeController.BATCH_SIZE);
					childNodes = TreeViewUtil.convertGermplasmListToTreeView(rootLists,
							isFolderOnly);
				} else if (NumberUtils.isNumber(parentKey)) {
					childNodes = this.getGermplasmChildrenNode(parentKey, isFolderOnly);
				} else {
					GermplasmTreeController.LOG.error("parentKey = " + parentKey
							+ " is not a number");
				}

			} catch (Exception e) {
				GermplasmTreeController.LOG.error(e.getMessage(), e);
			}
		}

		for (TreeNode newNode : childNodes) {
			List<TreeNode> childOfChildNode = this.getGermplasmChildrenNode(newNode.getKey(),
					isFolderOnly);
			if (childOfChildNode.isEmpty()) {
				newNode.setIsLazy(false);
			} else {
				newNode.setIsLazy(true);
			}
		}
		return childNodes;
	}

	private List<TreeNode> getGermplasmChildrenNode(String parentKey, boolean isFolderOnly)
			throws MiddlewareQueryException {
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		int parentId = Integer.valueOf(parentKey);
		List<GermplasmList> childLists = this.germplasmListManager
				.getGermplasmListByParentFolderIdBatched(parentId,
						GermplasmTreeController.BATCH_SIZE);
		childNodes = TreeViewUtil.convertGermplasmListToTreeView(childLists, isFolderOnly);
		return childNodes;
	}

	private List<TreeTableNode> getGermplasmFolderChildrenNode(String id)
			throws MiddlewareQueryException {
		return TreeViewUtil.convertGermplasmListToTreeTableNodes(this.getGermplasmListChildren(id),
				this.userDataManager, this.germplasmListManager);
	}

	/**
	 * Load initial germplasm tree.
	 * 
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/germplasm/list/header/details/{listId}", method = RequestMethod.GET)
	public Map<String, Object> getGermplasmListHeaderDetails(@PathVariable int listId) {
		Map<String, Object> dataResults = new HashMap<String, Object>();
		try {
			GermplasmList germplasmList = this.fieldbookMiddlewareService
					.getGermplasmListById(listId);
			dataResults.put("name", germplasmList.getName());
			dataResults.put("description", germplasmList.getDescription());
			dataResults.put("type", this.getTypeString(germplasmList.getType()));

			String statusValue = "Unlocked List";
			if (germplasmList.getStatus() >= 100) {
				statusValue = "Locked List";
			}

			dataResults.put("status", statusValue);
			dataResults.put("date", germplasmList.getDate());
			dataResults.put("owner",
					this.fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));
			dataResults.put("notes", germplasmList.getNotes());
			if (germplasmList.getType() != null
					&& (germplasmList.getType().equalsIgnoreCase(
							GermplasmListType.NURSERY.toString()) || germplasmList.getType()
							.equalsIgnoreCase(GermplasmListType.TRIAL.toString()))
					|| germplasmList.getType().equalsIgnoreCase(GermplasmListType.CHECK.toString())
					|| germplasmList.getType().equalsIgnoreCase(
							GermplasmListType.CROSSES.toString())
					|| germplasmList.getType().equalsIgnoreCase(
							GermplasmListType.ADVANCED.toString())) {
				dataResults.put("totalEntries", this.fieldbookMiddlewareService
						.countListDataProjectGermplasmListDataByListId(listId));
			} else {
				dataResults.put("totalEntries",
						this.fieldbookMiddlewareService.countGermplasmListDataByListId(listId));
			}

		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return dataResults;
	}

	private String getTypeString(String typeCode) {
		try {
			List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();

			for (UserDefinedField listType : listTypes) {
				if (typeCode.equals(listType.getFcode())) {
					return listType.getFname();
				}
			}
		} catch (MiddlewareQueryException ex) {
			GermplasmTreeController.LOG.error("Error in getting list types.", ex);
			return "Error in getting list types.";
		}

		return "Germplasm List";
	}

	/**
	 * Expand germplasm list folder.
	 * 
	 * @param id
	 *            the germplasm list ID
	 * @return the response page
	 */
	@RequestMapping(value = "/expandGermplasmListFolder/{id}", method = RequestMethod.GET)
	public String expandGermplasmListFolder(@PathVariable String id, Model model) {
		try {
			List<TreeTableNode> childNodes = this.getGermplasmListFolderChildNodes(id);
			model.addAttribute(GermplasmTreeController.GERMPLASM_LIST_CHILD_NODES, childNodes);
		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, GermplasmTreeController.GERMPLASM_LIST_TABLE_ROWS_PAGE);
	}

	/**
	 * Expand germplasm tree.
	 * 
	 * @param parentKey
	 *            the parent key
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/expandGermplasmTree/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String expandGermplasmTree(@PathVariable String parentKey,
			@PathVariable String isFolderOnly) {
		boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		try {
			List<TreeNode> childNodes = this.getGermplasmChildNodes(parentKey, isFolderOnlyBool);
			return TreeViewUtil.convertTreeViewToJson(childNodes);
		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/expandGermplasmTree/{parentKey}", method = RequestMethod.GET)
	public String expandGermplasmAllTree(@PathVariable String parentKey) {
		return this.expandGermplasmTree(parentKey, "0");
	}

	protected void checkIfUnique(String folderName) throws MiddlewareException {
		List<GermplasmList> duplicate = this.germplasmListManager.getGermplasmListByName(
				folderName, 0, 1, null);
		if (duplicate != null && !duplicate.isEmpty()) {
			throw new MiddlewareException(GermplasmTreeController.NAME_NOT_UNIQUE);
		}
		if (this.isSimilarToRootFolderName(folderName)) {
			throw new MiddlewareException(GermplasmTreeController.NAME_NOT_UNIQUE);
		}
	}

	protected boolean isSimilarToRootFolderName(String itemName) {
		if (itemName.equalsIgnoreCase(AppConstants.LISTS.getString())) {
			return true;
		}

		return false;
	}

	@ResponseBody
	@RequestMapping(value = "/addGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> addGermplasmFolder(HttpServletRequest req) {
		String id = req.getParameter("parentFolderId");
		String folderName = req.getParameter("folderName");
		Map<String, Object> resultsMap = new HashMap<>();

		GermplasmList gpList = null;
		GermplasmList newList = null;
		try {

			this.checkIfUnique(folderName);
			Integer userId = this.getCurrentIbdbUserId();

			if (id == null) {
				newList = new GermplasmList(null, folderName, Long.valueOf(new SimpleDateFormat(
						GermplasmTreeController.DATE_FORMAT).format(Calendar.getInstance()
						.getTime())), GermplasmTreeController.FOLDER, userId, folderName, null, 0);
			} else {
				gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(id));

				if (gpList != null && !gpList.isFolder()) {
					GermplasmList parent = null;

					parent = gpList.getParent();

					if (parent == null) {
						newList = new GermplasmList(null, folderName,
								Long.valueOf(new SimpleDateFormat(
										GermplasmTreeController.DATE_FORMAT).format(Calendar
										.getInstance().getTime())), GermplasmTreeController.FOLDER,
								userId, folderName, null, 0);
					} else {
						newList = new GermplasmList(null, folderName,
								Long.valueOf(new SimpleDateFormat(
										GermplasmTreeController.DATE_FORMAT).format(Calendar
										.getInstance().getTime())), GermplasmTreeController.FOLDER,
								userId, folderName, parent, 0);
					}
				} else {
					newList = new GermplasmList(null, folderName,
							Long.valueOf(new SimpleDateFormat(GermplasmTreeController.DATE_FORMAT)
									.format(Calendar.getInstance().getTime())),
							GermplasmTreeController.FOLDER, userId, folderName, gpList, 0);
				}

			}

			newList.setDescription("(NEW FOLDER) " + folderName);
			Integer germplasmListFolderId = this.germplasmListManager.addGermplasmList(newList);
			resultsMap.put("id", germplasmListFolderId);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/renameGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> renameStudyFolder(HttpServletRequest req) {
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		String newName = req.getParameter("newFolderName");
		String folderId = req.getParameter("folderId");

		try {

			GermplasmList gpList = this.germplasmListManager.getGermplasmListById(Integer
					.parseInt(folderId));

			this.checkIfUnique(newName);
			gpList.setName(newName);

			this.germplasmListManager.updateGermplasmList(gpList);

			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	public boolean hasChildren(Integer id) throws MiddlewareQueryException {
		return !this.germplasmListManager
				.getGermplasmListByParentFolderId(id, 0, Integer.MAX_VALUE).isEmpty();
	}

	@ResponseBody
	@RequestMapping(value = "/deleteGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> deleteGermplasmFolder(HttpServletRequest req) {
		Map<String, Object> resultsMap = new HashMap<>();

		GermplasmList gpList = null;
		String folderId = req.getParameter("folderId");
		try {
			gpList = this.germplasmListManager.getGermplasmListById(Integer.parseInt(folderId));

			if (this.hasChildren(gpList.getId())) {
				throw new MiddlewareException(GermplasmTreeController.HAS_CHILDREN);
			}
			this.germplasmListManager.deleteGermplasmList(gpList);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "1");
		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(GermplasmTreeController.IS_SUCCESS, "0");
			resultsMap.put(GermplasmTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/moveGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> moveStudyFolder(HttpServletRequest req) {
		String sourceId = req.getParameter("sourceId");
		String targetId = req.getParameter("targetId");

		Map<String, Object> resultsMap = new HashMap<>();

		try {
			GermplasmList gpList = this.germplasmListManager.getGermplasmListById(Integer
					.parseInt(sourceId));

			if (targetId != null) {
				GermplasmList parent = this.germplasmListManager.getGermplasmListById(Integer
						.parseInt(targetId));
				gpList.setParent(parent);
			} else {
				gpList.setParent(null);
			}

			this.germplasmListManager.updateGermplasmList(gpList);

		} catch (Exception e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/save/state/{type}")
	public String saveTreeState(@PathVariable String type,
			@RequestParam(value = "expandedNodes[]") String[] expandedNodes) {
		GermplasmTreeController.LOG.debug("Save the debug nodes");
		List<String> states = new ArrayList<String>();
		String status = "OK";
		try {
			if(!NODE_NONE.equalsIgnoreCase(expandedNodes[0])){				
				for (int index = 0; index < expandedNodes.length; index++) {
					states.add(expandedNodes[index]);
				}
			}
			this.userProgramStateDataManager.saveOrUpdateUserProgramTreeState(
					this.contextUtil.getCurrentWorkbenchUserId(),
					this.contextUtil.getCurrentProgramUUID(), type, states);
		} catch (MiddlewareQueryException e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
			status = "ERROR";
		}
		return status;
	}

	@ResponseBody
	@RequestMapping(value = "/retrieve/state/{type}", method = RequestMethod.GET)
	public String retrieveTreeState(@PathVariable String type) {

		List<String> stateList = new ArrayList<String>();
		try {
			stateList = this.userProgramStateDataManager
					.getUserProgramTreeStateByUserIdProgramUuidAndType(
							this.contextUtil.getCurrentWorkbenchUserId(),
							this.contextUtil.getCurrentProgramUUID(), type);
		} catch (MiddlewareQueryException e) {
			GermplasmTreeController.LOG.error(e.getMessage(), e);
		}
		return super.convertObjectToJson(stateList);
	}

	@Override
	public String getContentName() {
		return null;
	}

	protected void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected void setUserSelection(UserSelection userSelection) {
		this.userSelection = userSelection;
	}

	public void setUserProgramStateDataManager(
			UserProgramStateDataManager userProgramStateDataManager) {
		this.userProgramStateDataManager = userProgramStateDataManager;
	}

	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
