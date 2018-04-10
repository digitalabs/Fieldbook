package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;
import com.efficio.pojos.treeview.TreeTableNode;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmFolderMetadata;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.service.api.SampleListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Class SampleTreeController. <br/>
 * TODO Extract supper class with {@link GermplasmTreeController}
 */
@Controller
@RequestMapping(value = "/SampleListTreeManager")
@Transactional
public class SampleTreeController extends AbstractBaseFieldbookController {

	/**
	 * The default folder open state stored when closing the lists
	 * browser.
	 */
	static final String DEFAULT_STATE_SAVED_FOR_SAMPLE_LIST = "Lists";

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SampleTreeController.class);

	private static final String LIST_TABLE_PAGE = "Common/includes/list/listTable";
	public static final String LIST_ROOT_NODES = "listRootNodes";
	private static final String LIST_TABLE_ROWS_PAGE = "Common/includes/list/listTableRows";
	public static final String LIST_CHILD_NODES = "listChildNodes";
	protected static final String PROGRAM_LISTS = "LISTS";
	protected static final String CROP_LISTS = "CROPLISTS";

	public static final String NODE_NONE = "None";

	/**
	 * The Constant BATCH_SIZE.
	 */
	public static final int BATCH_SIZE = 500;

	@Resource
	private SampleListService sampleListService;

	@Resource
	private UserTreeStateService userTreeStateService;

	/**
	 * Load initial sample tree.
	 *
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/loadInitTree/{isFolderOnly}", method = RequestMethod.GET)
	public String loadInitialSampleTree(@PathVariable final String isFolderOnly) {
		final List<TreeNode> rootNodes = new ArrayList<>();
		rootNodes.add(new TreeNode(SampleTreeController.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, "lead",
				AppConstants.FOLDER_ICON_PNG.getString(), null));
		rootNodes.add(new TreeNode(SampleTreeController.PROGRAM_LISTS, AppConstants.SAMPLE_LISTS.getString(), true, "lead",
				AppConstants.FOLDER_ICON_PNG.getString(), this.getCurrentProgramUUID()));
		return TreeViewUtil.convertTreeViewToJson(rootNodes);
	}

	/**
	 * Load initial sample tree table.
	 *
	 * @return the string
	 */
	@RequestMapping(value = "/loadInitTreeTable", method = RequestMethod.GET)
	public String loadInitialSampleTreeTable(final Model model) {
		final List<TreeTableNode> rootNodes = new ArrayList<>();
		rootNodes.add(new TreeTableNode(SampleTreeController.CROP_LISTS, AppConstants.CROP_LISTS.getString(),
			null, null, null, null, "1"));
		rootNodes.add(new TreeTableNode(SampleTreeController.PROGRAM_LISTS, AppConstants.SAMPLE_LISTS.getString(),
			null, null, null, null, "1"));
		model.addAttribute(SampleTreeController.LIST_ROOT_NODES, rootNodes);
		return super.showAjaxPage(model, SampleTreeController.LIST_TABLE_PAGE);
	}

	protected List<TreeNode> getSampleChildNodes(final String parentKey, final boolean isFolderOnly, final String programUUID) {
		if (!(parentKey != null && !"".equals(parentKey))) {
			return new ArrayList<>();
		}

		final List<SampleList> rootLists;
		if (SampleTreeController.PROGRAM_LISTS.equals(parentKey)) {
			rootLists = this.sampleListService.getAllSampleTopLevelLists(programUUID);
		} else if (SampleTreeController.CROP_LISTS.equals(parentKey)) {
			rootLists = this.sampleListService.getAllSampleTopLevelLists(null);
		} else if (NumberUtils.isNumber(parentKey)) {
			rootLists = this.getSampleChildrenNode(parentKey, programUUID);
		} else {
			throw new IllegalStateException("Add a message");
		}
		final List<TreeNode> childNodes = TreeViewUtil.convertListToTreeView(rootLists, isFolderOnly);

		final Map<Integer, GermplasmFolderMetadata> allListMetaData = this.sampleListService.getFolderMetadata(rootLists);

		for (final TreeNode newNode : childNodes) {
			final GermplasmFolderMetadata nodeMetaData = allListMetaData.get(Integer.parseInt(newNode.getKey()));
			if (nodeMetaData != null && nodeMetaData.getNumberOfChildren() > 0) {
				newNode.setIsLazy(true);
				newNode.setNumOfChildren(nodeMetaData.getNumberOfChildren());
			}
			newNode.setParentId(parentKey);
		}
		return childNodes;
	}

	private List<SampleList> getSampleChildrenNode(final String parentKey, final String programUUID) {
		final int parentId = Integer.parseInt(parentKey);
		return this.sampleListService.getSampleListByParentFolderIdBatched(parentId, programUUID, SampleTreeController.BATCH_SIZE);
	}

	/**
	 * Expand list folder.
	 *
	 * @param id the list ID
	 * @return the response page
	 */
	@RequestMapping(value = "/expandGermplasmListFolder/{id}", method = RequestMethod.GET)
	public String expandListFolder(@PathVariable final String id, final Model model) {
		try {
			final List<TreeNode> childNodes = this.getSampleChildNodes(id, false, this.getCurrentProgramUUID());
			model.addAttribute(SampleTreeController.LIST_CHILD_NODES, TreeViewUtil.convertToTableNode(childNodes));
		} catch (final Exception e) {
			SampleTreeController.LOG.error(e.getMessage(), e);
		}

		return super.showAjaxPage(model, SampleTreeController.LIST_TABLE_ROWS_PAGE);
	}

	/**
	 * Expand sample tree.
	 *
	 * @param parentKey the parent key
	 * @return the string
	 */
	@ResponseBody
	@RequestMapping(value = "/expandTree/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String expandSampleTree(@PathVariable final String parentKey, @PathVariable final String isFolderOnly) {
		final boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly);
		try {
			final List<TreeNode> childNodes = this.getSampleChildNodes(parentKey, isFolderOnlyBool, this.getCurrentProgramUUID());
			return TreeViewUtil.convertTreeViewToJson(childNodes);
		} catch (final Exception e) {
			SampleTreeController.LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/expandTreeTable/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public ResponseEntity<List<TreeTableNode>> expandSampleTreeTable(@PathVariable final String parentKey, @PathVariable final String isFolderOnly) {
		final boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly);
		try {
			final List<TreeNode> childNodes = this.getSampleChildNodes(parentKey, isFolderOnlyBool, this.getCurrentProgramUUID());
			return new ResponseEntity<>(TreeViewUtil.convertToTableNode(childNodes), HttpStatus.OK);
		} catch (final Exception e) {
			SampleTreeController.LOG.error(e.getMessage(), e);
		}

		return new ResponseEntity<>(Collections.<TreeTableNode>emptyList(), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/expandTree/{parentKey}", method = RequestMethod.GET)
	public String expandSampleAllTree(@PathVariable final String parentKey) {
		return this.expandSampleTree(parentKey, "0");
	}

	@ResponseBody
	@RequestMapping(value = "/save/state/{type}")
	public String saveTreeState(@PathVariable final String type, @RequestParam(value = "expandedNodes[]") final String[] expandedNodes) {
		SampleTreeController.LOG.debug("Save the debug nodes");
		final List<String> states = new ArrayList<>();
		String status = "OK";
		try {

			if (!SampleTreeController.NODE_NONE.equalsIgnoreCase(expandedNodes[0])) {
				for (int index = 0; index < expandedNodes.length; index++) {
					states.add(expandedNodes[index]);
				}
			}

			if (states.isEmpty()) {
				states.add(SampleTreeController.DEFAULT_STATE_SAVED_FOR_SAMPLE_LIST);
			}

			this.userTreeStateService
					.saveOrUpdateUserProgramTreeState(this.contextUtil.getCurrentUserLocalId(), this.getCurrentProgramUUID(), type, states);
		} catch (final MiddlewareQueryException e) {
			SampleTreeController.LOG.error(e.getMessage(), e);
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
			stateList = this.userTreeStateService.getUserProgramTreeStateForSaveSampleList(userID, programUUID, type);
		} else {
			stateList = this.userTreeStateService.getUserProgramTreeStateByUserIdProgramUuidAndType(userID, programUUID, type);
		}
		return super.convertObjectToJson(stateList);
	}

	@Override
	public String getContentName() {
		return null;
	}

	protected void setSampleListService(final SampleListService sampleListService) {
		this.sampleListService = sampleListService;
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

}
