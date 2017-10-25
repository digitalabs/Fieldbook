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
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	 * The default folder open state stored when closing the germplasm lists
	 * browser.
	 */
	static final String DEFAULT_STATE_SAVED_FOR_SAMPLE_LIST = "Lists";

	private static final String COMMON_SAVE_GERMPLASM_LIST = "Common/saveGermplasmList";

	private static final String COMMON_SAVE_SAMPLE_LIST = "Common/saveSampleListDetails";

	private static final String GERMPLASM_LIST_TYPES = "germplasmListTypes";

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SampleTreeController.class);

	private static final String GERMPLASM_LIST_TABLE_PAGE = "Common/includes/germplasmListTable";
	public static final String GERMPLASM_LIST_ROOT_NODES = "germplasmListRootNodes";
	private static final String LISTS = "LISTS";

	public static final String NODE_NONE = "None";


	/**
	 * The Constant BATCH_SIZE.
	 */
	public static final int BATCH_SIZE = 500;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

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
		rootNodes.add(new TreeNode(SampleTreeController.LISTS, "Sample List", true, "lead", AppConstants.FOLDER_ICON_PNG.getString(), this.getCurrentProgramUUID()));
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
		final TreeTableNode localNode =
			new TreeTableNode(SampleTreeController.LISTS, AppConstants.SAMPLES.getString(), null, null, null, null, "1");
		rootNodes.add(localNode);
		model.addAttribute(SampleTreeController.GERMPLASM_LIST_ROOT_NODES, rootNodes);
		return super.showAjaxPage(model, SampleTreeController.GERMPLASM_LIST_TABLE_PAGE);
	}

	private List<TreeNode> getSampleChildNodes(final String parentKey, final boolean isFolderOnly, final String programUUID) {
		if (!(parentKey != null && !"".equals(parentKey))) {
			return new ArrayList<>();
		}

		final List<SampleList> rootLists;
		if (SampleTreeController.LISTS.equals(parentKey)) {
			rootLists = this.fieldbookMiddlewareService.getAllSampleTopLevelLists(programUUID);
		} else if (NumberUtils.isNumber(parentKey)) {
			rootLists = this.getSampleChildrenNode(parentKey, programUUID);
		} else {
			throw new IllegalStateException("Add a message");
		}
		final List<TreeNode> childNodes = TreeViewUtil.convertListToTreeView(rootLists, isFolderOnly);

		final Map<Integer, GermplasmFolderMetadata> allListMetaData = this.fieldbookMiddlewareService.getSampleFolderMetadata(rootLists);

		for (final TreeNode newNode : childNodes) {
			final GermplasmFolderMetadata nodeMetaData = allListMetaData.get(Integer.parseInt(newNode.getKey()));
			if (nodeMetaData != null && nodeMetaData.getNumberOfChildren() > 0) {
				newNode.setIsLazy(true);
			}
		}
		return childNodes;
	}

	private List<SampleList> getSampleChildrenNode(final String parentKey, final String programUUID) {
		final int parentId = Integer.parseInt(parentKey);
		return this.fieldbookMiddlewareService.getSampleListByParentFolderIdBatched(parentId, programUUID, SampleTreeController.BATCH_SIZE);
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

	protected void setFieldbookMiddlewareService(final FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

}
