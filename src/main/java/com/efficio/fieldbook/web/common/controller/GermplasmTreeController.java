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
import org.generationcp.middleware.pojos.*;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.text.SimpleDateFormat;
import java.util.*;

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
			@PathVariable String listIdentifier,
			Model model, HttpSession session) {

		try {
			form.setListDate(DateUtil.getCurrentDateInUIFormat());
			form.setListIdentifier(listIdentifier);
			List<UserDefinedField> germplasmListTypes = germplasmListManager
					.getGermplasmListTypes();
			form.setListType(AppConstants.GERMPLASM_LIST_TYPE_HARVEST.getString());
			model.addAttribute(GERMPLASM_LIST_TYPES, germplasmListTypes);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, COMMON_SAVE_GERMPLASM_LIST);
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
			GermplasmList germplasmListIsNew = fieldbookMiddlewareService
					.getGermplasmListByName(form.getListName());
			if (germplasmListIsNew == null && !isSimilarToRootFolderName(form.getListName())) {
				Map<Germplasm, GermplasmListData> listDataItems = new HashMap<>();
				Integer germplasmListId = saveGermplasmList(form, listDataItems);

				List<GermplasmListData> data = new ArrayList<GermplasmListData>();
				data.addAll(germplasmListManager
						.getGermplasmListDataByListId(germplasmListId, 0,
								Integer.MAX_VALUE));
				List<ListDataProject> listDataProject = ListDataProjectUtil
						.createListDataProjectFromGermplasmListData(data);

				Integer listDataProjectListId = saveListDataProjectList(form, germplasmListId,
						listDataProject);
				results.put(IS_SUCCESS, 1);
				results.put("germplasmListId", germplasmListId);
				results.put("uniqueId", form.getListIdentifier());
				results.put("listName", form.getListName());

				if (GERMPLASM_LIST_TYPE_ADVANCE.equals(form.getGermplasmListType())) {
					results.put("advancedGermplasmListId", listDataProjectListId);
				} else if (GERMPLASM_LIST_TYPE_CROSS.equals(form.getGermplasmListType())) {
					results.put("crossesListId", listDataProjectListId);
				}
			} else {
				results.put(IS_SUCCESS, 0);
				String nameUniqueError = "germplasm.save.list.name.unique.error";
				Locale locale = LocaleContextHolder.getLocale();
				results.put(MESSAGE, messageSource.getMessage(
						nameUniqueError, null, locale));
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			results.put(IS_SUCCESS, 0);
			results.put(MESSAGE, e.getMessage());
		}

		return results;
	}

	protected Integer saveGermplasmList(SaveListForm form,
			Map<Germplasm, GermplasmListData> listDataItems) throws MiddlewareQueryException {
		Integer currentUserId = getCurrentIbdbUserId();
		GermplasmList germplasmList = createGermplasmList(form, currentUserId);
		if (GERMPLASM_LIST_TYPE_ADVANCE.equals(form.getGermplasmListType())) {
			AdvancingNurseryForm advancingNurseryForm = getPaginationListSelection()
					.getAdvanceDetails(form.getListIdentifier());
			Map<Germplasm, List<Name>> germplasms = new HashMap<>();

			populateGermplasmListDataFromAdvanced(germplasmList, advancingNurseryForm, form,
					germplasms, listDataItems, currentUserId);
			return fieldbookMiddlewareService
					.saveNurseryAdvanceGermplasmList(germplasms, listDataItems,
							germplasmList);
		} else if (GERMPLASM_LIST_TYPE_CROSS.equals(form.getGermplasmListType())) {
			CrossSetting crossSetting = userSelection.getCrossSettings();
			ImportedCrossesList importedCrossesList = userSelection.getImportedCrossesList();
			crossingService
					.applyCrossSetting(crossSetting, importedCrossesList, getCurrentIbdbUserId());
			populateGermplasmListData(germplasmList, listDataItems,
					importedCrossesList.getImportedCrosses());
			return fieldbookMiddlewareService
					.saveGermplasmList(listDataItems, germplasmList);
		} else {
			throw new IllegalArgumentException(
					"Unknown germplasm list type supplied when saving germplasm list");
		}
	}

	protected Integer saveListDataProjectList(SaveListForm form, Integer germplasmListId,
			List<ListDataProject> dataProjectList) throws MiddlewareException {
		GermplasmListType type;
		Integer currentUserID = getCurrentIbdbUserId();
		if (GERMPLASM_LIST_TYPE_ADVANCE.equals(form.getGermplasmListType())) {
			type = GermplasmListType.ADVANCED;

		} else if (GERMPLASM_LIST_TYPE_CROSS.equals(form.getGermplasmListType())) {
			type = GermplasmListType.CROSSES;
			//need to add the copying of the duplicate entry here
			FieldbookUtil.copyDupeNotesToListDataProject(dataProjectList, userSelection.getImportedCrossesList().getImportedCrosses());
		} else {
			throw new IllegalArgumentException(
					"Unknown germplasm list type supplied when saving germplasm list");
		}

		int studyId = 0;
		if (userSelection.getWorkbook() != null
				&& userSelection.getWorkbook().getStudyDetails() != null
				&& userSelection.getWorkbook().getStudyDetails().getId() != null) {
			studyId = userSelection.getWorkbook().getStudyDetails().getId();
		}

		return fieldbookMiddlewareService
				.saveOrUpdateListDataProject(studyId, type,
						germplasmListId,
						dataProjectList, currentUserID);



	}

	/**
	 * Load initial germplasm tree for crosses.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/saveCrossesList", method = RequestMethod.GET)
	public String saveList(@ModelAttribute("saveListForm") SaveListForm form,
			Model model, HttpSession session) {

		try {
			String listName = "";
			String listDescription = "";
			String listType = AppConstants.GERMPLASM_LIST_TYPE_GENERIC_LIST.getString();
			String listDate = DateUtil.getCurrentDateInUIFormat();

			if (userSelection.getImportedCrossesList() != null) {
				listName = userSelection.getImportedCrossesList().getName();
				listDescription = userSelection.getImportedCrossesList().getTitle();
				listType = userSelection.getImportedCrossesList().getType();
				listDate = DateUtil.getDateInUIFormat(userSelection.getImportedCrossesList().getDate());
			}

			form.setListName(listName);
			form.setListDescription(listDescription);
			form.setListType(listType);
			form.setListDate(listDate);

			List<UserDefinedField> germplasmListTypes = germplasmListManager
					.getGermplasmListTypes();
			model.addAttribute(GERMPLASM_LIST_TYPES, germplasmListTypes);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, COMMON_SAVE_GERMPLASM_LIST);
	}

	protected int saveCrossesList(Integer germplasmListId, List<ListDataProject> listDataProject,
			Integer userId) throws MiddlewareQueryException {
		int studyId = 0;

		if (userSelection.getWorkbook() != null
				&& userSelection.getWorkbook().getStudyDetails() != null
				&& userSelection.getWorkbook().getStudyDetails().getId() != null) {
			studyId = userSelection.getWorkbook().getStudyDetails().getId();
		}

		int crossesId = fieldbookMiddlewareService
				.saveOrUpdateListDataProject(studyId, GermplasmListType.CROSSES, germplasmListId,
						listDataProject, userId);
		userSelection.addImportedCrossesId(crossesId);
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
		if (saveListForm.getParentId() != null && !LISTS.equals(saveListForm.getParentId())) {
			parentId = Integer.valueOf(saveListForm.getParentId());
			try {
				gpList = germplasmListManager.getGermplasmListById(parentId);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		if (gpList != null && gpList.isFolder()) {

			parent = gpList;

		}

		Integer status = 1;
		Long dateLong = Long.valueOf(DateUtil.convertToDBDateFormat(TermId.DATE_VARIABLE.getId(),
				saveListForm.getListDate()));

		return new GermplasmList(null, listName, dateLong, listType,
				currentUserId, description, parent, status, saveListForm.getListNotes());

	}

	private void populateGermplasmListData(GermplasmList germplasmList,
			Map<Germplasm, GermplasmListData> listDataItems,
			List<ImportedCrosses> importedGermplasmList) {
		//Common germplasm list data fields
		Integer listDataId = null;
		Integer listDataStatus = 0;
		Integer localRecordId = 0;

		// Create germplasms to save - Map<Germplasm, List<Name>>
		for (ImportedCrosses importedCrosses : importedGermplasmList) {
			Integer gid = Integer.valueOf(importedCrosses.getGid());

			Germplasm germplasm = new Germplasm();
			germplasm.setGid(gid);

			// Create list data items to save - Map<Germplasm, GermplasmListData>
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
					entryId, entryCode, seedSource,
					designation, groupName, listDataStatus, localRecordId);

			listDataItems.put(germplasm, listData);
		}
	}

	/**
	 * Creates the nursery advance germplasm list.
	 *
	 * @param form          the form
	 * @param germplasms    the germplasms
	 * @param listDataItems the list data items
	 * @return the germplasm list
	 */

	private void populateGermplasmListDataFromAdvanced(GermplasmList germplasmList,
			AdvancingNurseryForm form,
			SaveListForm saveListForm
			, Map<Germplasm, List<Name>> germplasms
			, Map<Germplasm, GermplasmListData> listDataItems
			, Integer currentUserID) {

		String harvestDate = form.getHarvestYear() + form.getHarvestMonth() + "00";

		//Common germplasm fields
		Integer lgid = 0;
		Integer locationId = 0;
		String harvestLocationId = form.getHarvestLocationId();
		if (harvestLocationId != null && !"".equals(harvestLocationId)) {
			locationId = Integer.valueOf(harvestLocationId);
		}
		Integer gDate = DateUtil.getCurrentDateAsIntegerValue();

		//Common germplasm list data fields
		Integer listDataId = null;
		Integer listDataStatus = 0;
		Integer localRecordId = 0;

		//Common name fields
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
				// Germplasm name is the Names entry with NType = 1027, NVal = table.desig, NStat = 0
				if (name.getNstat() == 0
						&& name.getTypeId() == GermplasmNameType.UNRESOLVED_NAME
						.getUserDefinedFieldID()) {
					preferredName = name;
				}
			}

			Integer trueGdate = harvestDate != null && !"".equals(harvestDate.trim()) ?
					Integer.valueOf(harvestDate) : gDate;
			Germplasm germplasm = new Germplasm(gid, methodId, gnpgs, gpid1, gpid2
					, currentUserID, lgid, locationId, trueGdate, preferredName);

			germplasms.put(germplasm, names);

			// Create list data items to save - Map<Germplasm, GermplasmListData>
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
					entryId, entryCode, seedSource,
					designation, groupName, listDataStatus, localRecordId);

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

		return fieldbookProperties.getGermplasmDetailsUrl();
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
		return fieldbookProperties.getGermplasmImportUrl() + "?" + contextParams;
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
			rootNodes.add(new TreeNode(LISTS, AppConstants.LISTS.getString(), true, "lead",
					AppConstants.FOLDER_ICON_PNG.getString(), contextUtil.getCurrentProgramUUID()));
			return TreeViewUtil.convertTreeViewToJson(rootNodes);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
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
			TreeTableNode localNode = new TreeTableNode(
					LISTS, AppConstants.LISTS.getString(),
					null, null, null, null, "1");
			rootNodes.add(localNode);
			rootNodes.addAll(getGermplasmListFolderChildNodes(localNode));
			model.addAttribute(GERMPLASM_LIST_ROOT_NODES, rootNodes);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return super.showAjaxPage(model, GERMPLASM_LIST_TABLE_PAGE);
	}

	protected void markIfHasChildren(TreeTableNode node) throws MiddlewareQueryException {
		List<GermplasmList> children = getGermplasmListChildren(node.getId());
		node.setNumOfChildren(Integer.toString(children.size()));
	}

	protected List<GermplasmList> getGermplasmListChildren(String id)
			throws MiddlewareQueryException {
		List<GermplasmList> children = new ArrayList<GermplasmList>();
		if (LISTS.equals(id)) {
			children = germplasmListManager
					.getAllTopLevelListsBatched(BATCH_SIZE);
		} else if (NumberUtils.isNumber(id)) {
			int parentId = Integer.valueOf(id);
			children = germplasmListManager
					.getGermplasmListByParentFolderIdBatched(parentId, BATCH_SIZE);
		} else {
			LOG.error("germplasm id = " + id + " is not a number");
		}
		return children;
	}

	protected List<TreeTableNode> getGermplasmListFolderChildNodes(TreeTableNode node)
			throws MiddlewareQueryException {
		List<TreeTableNode> childNodes = getGermplasmListFolderChildNodes(node.getId());
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
			childNodes = getGermplasmFolderChildrenNode(id);
			for (TreeTableNode newNode : childNodes) {
				markIfHasChildren(newNode);
			}
		}
		return childNodes;
	}

	private List<TreeNode> getGermplasmChildNodes(String parentKey, boolean isFolderOnly)
			throws MiddlewareQueryException {
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		if (parentKey != null && !"".equals(parentKey)) {
			try {
				if (LISTS.equals(parentKey)) {
					List<GermplasmList> rootLists = germplasmListManager
							.getAllTopLevelListsBatched(BATCH_SIZE);
					childNodes = TreeViewUtil
							.convertGermplasmListToTreeView(rootLists, isFolderOnly);
				} else if (NumberUtils.isNumber(parentKey)) {
					childNodes = getGermplasmChildrenNode(parentKey, isFolderOnly);
				} else {
					LOG.error("parentKey = " + parentKey + " is not a number");
				}

			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}

		for (TreeNode newNode : childNodes) {
			List<TreeNode> childOfChildNode = getGermplasmChildrenNode(newNode.getKey(),
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
		List<GermplasmList> childLists = germplasmListManager
				.getGermplasmListByParentFolderIdBatched(parentId, BATCH_SIZE);
		childNodes = TreeViewUtil.convertGermplasmListToTreeView(childLists, isFolderOnly);
		return childNodes;
	}

	private List<TreeTableNode> getGermplasmFolderChildrenNode(String id)
			throws MiddlewareQueryException {
		return TreeViewUtil.convertGermplasmListToTreeTableNodes(
				getGermplasmListChildren(id), userDataManager, germplasmListManager);
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
			GermplasmList germplasmList = fieldbookMiddlewareService.getGermplasmListById(listId);
			dataResults.put("name", germplasmList.getName());
			dataResults.put("description", germplasmList.getDescription());
			dataResults.put("type", getTypeString(germplasmList.getType()));

			String statusValue = "Unlocked List";
			if (germplasmList.getStatus() >= 100) {
				statusValue = "Locked List";
			}

			dataResults.put("status", statusValue);
			dataResults.put("date", germplasmList.getDate());
			dataResults.put("owner",
					fieldbookMiddlewareService.getOwnerListName(germplasmList.getUserId()));
			dataResults.put("notes", germplasmList.getNotes());
			if (germplasmList.getType() != null &&
					(germplasmList.getType().equalsIgnoreCase(GermplasmListType.NURSERY.toString())
							||
							germplasmList.getType()
									.equalsIgnoreCase(GermplasmListType.TRIAL.toString())) ||
					germplasmList.getType().equalsIgnoreCase(GermplasmListType.CHECK.toString()) ||
					germplasmList.getType().equalsIgnoreCase(GermplasmListType.CROSSES.toString()) ||
					germplasmList.getType().equalsIgnoreCase(GermplasmListType.ADVANCED.toString())
					) {
				dataResults.put("totalEntries", fieldbookMiddlewareService
						.countListDataProjectGermplasmListDataByListId(listId));
			} else {
				dataResults.put("totalEntries",
						fieldbookMiddlewareService.countGermplasmListDataByListId(listId));
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return dataResults;
	}

	private String getTypeString(String typeCode) {
		try {
			List<UserDefinedField> listTypes = germplasmListManager.getGermplasmListTypes();

			for (UserDefinedField listType : listTypes) {
				if (typeCode.equals(listType.getFcode())) {
					return listType.getFname();
				}
			}
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error in getting list types.", ex);
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
	public String expandGermplasmListFolder(@PathVariable String id, Model model) {
		try {
			List<TreeTableNode> childNodes = getGermplasmListFolderChildNodes(id);
			model.addAttribute(GERMPLASM_LIST_CHILD_NODES, childNodes);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, GERMPLASM_LIST_TABLE_ROWS_PAGE);
	}

	/**
	 * Expand germplasm tree.
	 *
	 * @param parentKey the parent key
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/expandGermplasmTree/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String expandGermplasmTree(@PathVariable String parentKey,
			@PathVariable String isFolderOnly) {
		boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		try {
			List<TreeNode> childNodes = getGermplasmChildNodes(parentKey, isFolderOnlyBool);
			return TreeViewUtil.convertTreeViewToJson(childNodes);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/expandGermplasmTree/{parentKey}", method = RequestMethod.GET)
	public String expandGermplasmAllTree(@PathVariable String parentKey) {
		return expandGermplasmTree(parentKey, "0");
	}

	protected void checkIfUnique(String folderName) throws MiddlewareException {
		List<GermplasmList> duplicate = germplasmListManager.
				getGermplasmListByName(folderName, 0, 1, null);
		if (duplicate != null && !duplicate.isEmpty()) {
			throw new MiddlewareException(NAME_NOT_UNIQUE);
		}
		if (isSimilarToRootFolderName(folderName)) {
			throw new MiddlewareException(NAME_NOT_UNIQUE);
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

			checkIfUnique(folderName);
			Integer userId = this.getCurrentIbdbUserId();

			if (id == null) {
				newList = new GermplasmList(null, folderName, Long.valueOf(
						(new SimpleDateFormat(DATE_FORMAT))
								.format(Calendar.getInstance().getTime())), FOLDER, userId,
						folderName, null, 0);
			} else {
				gpList = germplasmListManager.getGermplasmListById(Integer.parseInt(id));

				if (gpList != null && !gpList.isFolder()) {
					GermplasmList parent = null;

					parent = gpList.getParent();

					if (parent == null) {
						newList = new GermplasmList(null, folderName, Long.valueOf(
								(new SimpleDateFormat(DATE_FORMAT))
										.format(Calendar.getInstance().getTime())), FOLDER, userId,
								folderName, null, 0);
					} else {
						newList = new GermplasmList(null, folderName, Long.valueOf(
								(new SimpleDateFormat(DATE_FORMAT))
										.format(Calendar.getInstance().getTime())), FOLDER, userId,
								folderName, parent, 0);
					}
				} else {
					newList = new GermplasmList(null, folderName, Long.valueOf(
							(new SimpleDateFormat(DATE_FORMAT))
									.format(Calendar.getInstance().getTime())), FOLDER, userId,
							folderName, gpList, 0);
				}

			}

			newList.setDescription("(NEW FOLDER) " + folderName);
			Integer germplasmListFolderId = germplasmListManager.addGermplasmList(newList);
			resultsMap.put("id", germplasmListFolderId);
			resultsMap.put(IS_SUCCESS, "1");
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			resultsMap.put(IS_SUCCESS, "0");
			resultsMap.put(MESSAGE, e.getMessage());
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

			GermplasmList gpList = germplasmListManager
					.getGermplasmListById(Integer.parseInt(folderId));

			checkIfUnique(newName);
			gpList.setName(newName);

			germplasmListManager.updateGermplasmList(gpList);

			resultsMap.put(IS_SUCCESS, "1");
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			resultsMap.put(IS_SUCCESS, "0");
			resultsMap.put(MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	public boolean hasChildren(Integer id) throws MiddlewareQueryException {
		return !germplasmListManager.getGermplasmListByParentFolderId(id, 0, Integer.MAX_VALUE)
				.isEmpty();
	}

	@ResponseBody
	@RequestMapping(value = "/deleteGermplasmFolder", method = RequestMethod.POST)
	public Map<String, Object> deleteGermplasmFolder(HttpServletRequest req) {
		Map<String, Object> resultsMap = new HashMap<>();

		GermplasmList gpList = null;
		String folderId = req.getParameter("folderId");
		try {
			gpList = germplasmListManager.getGermplasmListById(Integer.parseInt(folderId));

			if (hasChildren(gpList.getId())) {
				throw new MiddlewareException(HAS_CHILDREN);
			}
			germplasmListManager.deleteGermplasmList(gpList);
			resultsMap.put(IS_SUCCESS, "1");
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			resultsMap.put(IS_SUCCESS, "0");
			resultsMap.put(MESSAGE, e.getMessage());
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
			GermplasmList gpList = germplasmListManager
					.getGermplasmListById(Integer.parseInt(sourceId));

			if (targetId != null) {
				GermplasmList parent = germplasmListManager
						.getGermplasmListById(Integer.parseInt(targetId));
				gpList.setParent(parent);
			} else {
				gpList.setParent(null);
			}

			germplasmListManager.updateGermplasmList(gpList);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return resultsMap;
	}
	
	@ResponseBody
	@RequestMapping(value = "/save/state/{type}")
	public String saveTreeState(
			@PathVariable String type,
			Model model, HttpServletRequest request, @RequestParam(value = "expandedNodes[]") String[] expandedNodes) {
		LOG.debug("Save the debug nodes");
		List<String> states = new ArrayList<String>();
		String status = "OK";		
		try {
			for(int index = 0 ; index < expandedNodes.length ; index++){
				states.add(expandedNodes[index]);
			}			
			userProgramStateDataManager.saveOrUpdateUserProgramTreeState(contextUtil.getCurrentWorkbenchUserId(), contextUtil.getProjectInContext().getUniqueID(), type, states);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		return status;
	}
	
	@ResponseBody
	@RequestMapping(value = "/retrieve/state/{type}", method = RequestMethod.GET)
	public String retrieveTreeState(
			@PathVariable String type,
			Model model, HttpSession session) {
		
		List<String> stateList = new ArrayList<String>();
		try {
			stateList = userProgramStateDataManager.getUserProgramTreeStateByUserIdProgramUuidAndType(contextUtil.getCurrentWorkbenchUserId(), contextUtil.getProjectInContext().getUniqueID(), type);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
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

}
