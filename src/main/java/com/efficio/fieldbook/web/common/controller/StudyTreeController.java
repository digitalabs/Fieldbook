
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;

@Controller
@RequestMapping(StudyTreeController.URL)
public class StudyTreeController {

	private static final Logger LOG = LoggerFactory.getLogger(StudyTreeController.class);
	public static final String URL = "/StudyTreeManager";
	public static final String LOCAL = "LOCAL";
	private static final String HAS_OBSERVATIONS = "hasObservations";
	private static final String IS_SUCCESS = "isSuccess";
	private static final String MESSAGE = "message";

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private StudyDataManager studyDataManager;
	@Autowired
	public MessageSource messageSource;

	@Autowired
	public ContextUtil contextUtil;

	@Autowired
	HttpServletRequest request;

	@ResponseBody
	@RequestMapping(value = "/loadInitialTree/{isFolderOnly}/{type}", method = RequestMethod.GET)
	public String loadInitialTree(@PathVariable String isFolderOnly, @PathVariable String type) {
		boolean isNursery = type != null && "N".equalsIgnoreCase(type) ? true : false;
		try {
			List<TreeNode> rootNodes = new ArrayList<TreeNode>();
			String localName = isNursery ? AppConstants.NURSERIES.getString() : AppConstants.TRIALS.getString();
			TreeNode localTreeNode =
					new TreeNode(StudyTreeController.LOCAL, localName, true, "lead", AppConstants.FOLDER_ICON_PNG.getString(),
							this.getCurrentProgramUUID());
			rootNodes.add(localTreeNode);
			return TreeViewUtil.convertTreeViewToJson(rootNodes);
		} catch (Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	private List<TreeNode> getChildNodes(String parentKey, boolean isNursery, boolean isFolderOnly) throws MiddlewareQueryException {
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		if (parentKey != null && !"".equals(parentKey)) {
			try {
				if (StudyTreeController.LOCAL.equals(parentKey)) {
					List<FolderReference> rootFolders = this.fieldbookMiddlewareService.getRootFolders(this.getCurrentProgramUUID());
					childNodes =
							TreeViewUtil.convertStudyFolderReferencesToTreeView(rootFolders, isNursery, false, true,
									this.fieldbookMiddlewareService, isFolderOnly);
				} else if (NumberUtils.isNumber(parentKey)) {
					childNodes = this.getChildrenTreeNodes(parentKey, isNursery, isFolderOnly);
				} else {
					StudyTreeController.LOG.error("parentKey = " + parentKey + " is not a number");
				}
			} catch (Exception e) {
				StudyTreeController.LOG.error(e.getMessage(), e);
			}
		}

		for (TreeNode newNode : childNodes) {
			List<TreeNode> childOfChildNode = this.getChildrenTreeNodes(newNode.getKey(), isNursery, isFolderOnly);
			if (childOfChildNode.isEmpty()) {
				newNode.setIsLazy(false);
			}
		}
		return childNodes;
	}

	private List<TreeNode> getChildrenTreeNodes(String parentKey, boolean isNursery, boolean isFolderOnly) throws MiddlewareQueryException {
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		int parentId = Integer.valueOf(parentKey);
		List<Reference> folders = this.fieldbookMiddlewareService.getChildrenOfFolder(parentId, this.getCurrentProgramUUID());

		// convert reference to folder reference
		List<FolderReference> folRefs = TreeViewUtil.convertReferenceToFolderReference(folders);
		childNodes =
				TreeViewUtil.convertStudyFolderReferencesToTreeView(folRefs, isNursery, false, true, this.fieldbookMiddlewareService,
						isFolderOnly);
		return childNodes;
	}

	@ResponseBody
	@RequestMapping(value = "/expandTree/{type}/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String expandTree(@PathVariable String parentKey, @PathVariable String isFolderOnly, @PathVariable String type) {
		boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		boolean isNursery = type != null && "N".equalsIgnoreCase(type) ? true : false;
		try {
			List<TreeNode> childNodes = this.getChildNodes(parentKey, isNursery, isFolderOnlyBool);
			return TreeViewUtil.convertTreeViewToJson(childNodes);
		} catch (Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveChildren/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String retrieveChildren(@PathVariable String parentKey, @PathVariable String isFolderOnly) {
		boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		try {
			if (StudyTreeController.LOCAL.equals(parentKey)) {
				return this.getRootFolders(isFolderOnlyBool);
			} else if (NumberUtils.isNumber(parentKey)) {

				int parentId = Integer.valueOf(parentKey);
				List<Reference> folders = this.fieldbookMiddlewareService.getChildrenOfFolder(parentId, this.getCurrentProgramUUID());
				// convert reference to folder refence
				List<FolderReference> folRefs = TreeViewUtil.convertReferenceToFolderReference(folders);
				return TreeViewUtil.convertStudyFolderReferencesToJson(folRefs, true, false, true, this.fieldbookMiddlewareService,
						isFolderOnlyBool);

			} else {
				StudyTreeController.LOG.error("parentKey = " + parentKey + " is not a number");
			}

		} catch (Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	private String getRootFolders(boolean isFolderOnly) {
		try {
			List<FolderReference> rootFolders = this.fieldbookMiddlewareService.getRootFolders(this.getCurrentProgramUUID());
			return TreeViewUtil.convertStudyFolderReferencesToJson(rootFolders, true, false, true, this.fieldbookMiddlewareService,
					isFolderOnly);
		} catch (Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/has/observations/{studyId}/{studyName}", method = RequestMethod.GET)
	public Map<String, String> hasObservations(@PathVariable int studyId, @PathVariable String studyName) {
		Map<String, String> dataResults = new HashMap<String, String>();

		int datasetId;
		try {
			datasetId = this.fieldbookMiddlewareService.getMeasurementDatasetId(studyId, studyName);
			long observationCount = this.fieldbookMiddlewareService.countObservations(datasetId);
			if (observationCount > 0) {
				dataResults.put(StudyTreeController.HAS_OBSERVATIONS, "1");
			} else {
				dataResults.put(StudyTreeController.HAS_OBSERVATIONS, "0");
			}
		} catch (MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			dataResults.put(StudyTreeController.HAS_OBSERVATIONS, "0");
		}

		return dataResults;
	}

	@ResponseBody
	@RequestMapping(value = "/isNameUnique", method = RequestMethod.POST)
	public Map<String, Object> isNameUnique(HttpServletRequest req) {
		String studyId = req.getParameter("studyId");
		String studyName = req.getParameter("name");
		Integer studyIdInt = Integer.valueOf(studyId);

		Map<String, Object> resultsMap = new HashMap<String, Object>();
		try {

			Integer studyIdDb =
					this.fieldbookMiddlewareService.getProjectIdByNameAndProgramUUID(HtmlUtils.htmlEscape(studyName),
							this.getCurrentProgramUUID());

			if (studyIdDb == null) {
				// meaning there is no study
				resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
			} else if (studyIdInt.intValue() == 0 && studyIdDb != 0) {
				// meaning new
				resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			} else if (studyIdInt.intValue() == studyIdDb) {
				resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
			}

		} catch (Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;

	}

	@ResponseBody
	@RequestMapping(value = "/addStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> addStudyFolder(HttpServletRequest req) {
		String parentKey = req.getParameter("parentFolderId");
		String folderName = req.getParameter("folderName");
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		Locale locale = LocaleContextHolder.getLocale();
		try {
			if (folderName.equalsIgnoreCase(AppConstants.NURSERIES.getString())
					|| folderName.equalsIgnoreCase(AppConstants.TRIALS.getString())) {
				throw new MiddlewareQueryException(this.messageSource.getMessage("folder.name.not.unique", null, locale));
			}
			Integer parentFolderId = Integer.parseInt(parentKey);
			if (!TreeViewUtil.isFolder(parentFolderId, this.fieldbookMiddlewareService)) {
				DmsProject project = this.studyDataManager.getParentFolder(parentFolderId);
				if (project == null) {
					throw new MiddlewareQueryException("Parent folder cannot be null");
				}
				parentFolderId = project.getProjectId();
			}
			int newFolderId = this.studyDataManager.addSubFolder(parentFolderId, folderName, folderName, this.getCurrentProgramUUID());
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
			resultsMap.put("newFolderId", Integer.toString(newFolderId));
		} catch (Exception e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;

	}

	@ResponseBody
	@RequestMapping(value = "/renameStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> renameStudyFolder(HttpServletRequest req) {
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		Locale locale = LocaleContextHolder.getLocale();
		try {
			String newFolderName = req.getParameter("newFolderName");
			String folderId = req.getParameter("folderId");
			if (newFolderName.equalsIgnoreCase(AppConstants.NURSERIES.getString())
					|| newFolderName.equalsIgnoreCase(AppConstants.TRIALS.getString())) {
				throw new MiddlewareQueryException(this.messageSource.getMessage("folder.name.not.unique", null, locale));
			}
			this.studyDataManager.renameSubFolder(newFolderName, Integer.parseInt(folderId), this.getCurrentProgramUUID());
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
		} catch (MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE, e.getMessage());
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/deleteStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> deleteStudyListFolder(HttpServletRequest req) {
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		Locale locale = LocaleContextHolder.getLocale();
		try {
			String folderId = req.getParameter("folderId");
			this.studyDataManager.deleteEmptyFolder(Integer.parseInt(folderId), this.getCurrentProgramUUID());
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
		} catch (MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			resultsMap.put(StudyTreeController.MESSAGE,
					this.messageSource.getMessage("browse.nursery.delete.folder.has.children", null, locale));

		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/moveStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> moveStudyFolder(HttpServletRequest req) throws MiddlewareQueryException {
		String sourceId = req.getParameter("sourceId");
		String targetId = req.getParameter("targetId");
		String isStudy = req.getParameter("isStudy");
		boolean isAStudy = "1".equalsIgnoreCase(isStudy) ? true : false;
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		try {
			this.studyDataManager.moveDmsProject(Integer.parseInt(sourceId), Integer.parseInt(targetId), isAStudy);
		} catch (MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			throw new MiddlewareQueryException(e.getMessage(), e);
		}
		return resultsMap;
	}

	protected String getCurrentProgramUUID() throws MiddlewareQueryException {
		return this.contextUtil.getCurrentProgramUUID();
	}
}
