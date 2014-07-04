package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
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

import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.TreeViewUtil;
import com.efficio.pojos.treeview.TreeNode;

@Controller
@RequestMapping(StudyTreeController.URL)
public class StudyTreeController {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyTreeController.class);
	public static final String URL = "/StudyTreeManager";

	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Resource
	private StudyDataManager studyDataManager;
	@Autowired
    public MessageSource messageSource;

	@ResponseBody
	@RequestMapping(value = "/loadInitialTree/{isFolderOnly}/{type}", method = RequestMethod.GET)
	public String loadInitialTree(@PathVariable String isFolderOnly, @PathVariable String type) {
		boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		boolean isNursery = (type != null && type.equalsIgnoreCase("N")) ? true : false;
		try {
			List<TreeNode> rootNodes = new ArrayList<TreeNode>();
			String localName = isNursery ? AppConstants.PROGRAM_NURSERIES.getString() : AppConstants.PROGRAM_TRIALS.getString();
			String centralName = isNursery ? AppConstants.PUBLIC_NURSERIES.getString() : AppConstants.PUBLIC_TRIALS.getString();
			TreeNode localTreeNode = new TreeNode("LOCAL", localName, true, "lead", AppConstants.FOLDER_ICON_PNG.getString());			
			rootNodes.add(localTreeNode);
			if(isFolderOnlyBool == false){
				TreeNode centralTreeNode = new TreeNode("CENTRAL", centralName, true, "lead", AppConstants.FOLDER_ICON_PNG.getString());
				rootNodes.add(centralTreeNode);
			}
			return TreeViewUtil.convertTreeViewToJson(rootNodes);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return "[]";
	}

	private List<TreeNode> getChildNodes(String parentKey, boolean isNursery, boolean isFolderOnly) throws MiddlewareQueryException{
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		if(parentKey != null && !parentKey.equalsIgnoreCase("")){
			
			try {
				if (Database.LOCAL.toString().equals(parentKey) || Database.CENTRAL.toString().equals(parentKey)) {
					
						Database instance = Database.LOCAL;
						if (Database.CENTRAL.toString().equals(parentKey)) {
							instance = Database.CENTRAL;
						}

						List<FolderReference> rootFolders = fieldbookMiddlewareService.getRootFolders(instance);
						childNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(rootFolders, isNursery, false, true,
								fieldbookMiddlewareService, isFolderOnly);
					
				} else if (NumberUtils.isNumber(parentKey)) {
					childNodes = getChildrenTreeNodes(parentKey, isNursery, isFolderOnly);
				} else {
					LOG.error("parentKey = " + parentKey + " is not a number");
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}			
		}
		/*
		for(TreeNode newNode : childNodes){
			newNode.setChildren(getChildNodes(newNode.getKey(), isFolderOnly));
		}
		*/
		for(TreeNode newNode : childNodes){
			List<TreeNode> childOfChildNode = getChildrenTreeNodes(newNode.getKey(), isNursery, isFolderOnly);
			//newNode.setChildren(childOfChildNode);
			if(childOfChildNode.size() == 0) {
				newNode.setIsLazy(false);
			}
		}
		return childNodes;
	}
	
	private List<TreeNode> getChildrenTreeNodes(String parentKey, boolean isNursery, boolean isFolderOnly) throws MiddlewareQueryException{
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		int parentId = Integer.valueOf(parentKey);
		List<Reference> folders = fieldbookMiddlewareService.getChildrenOfFolder(parentId);
		// convert reference to folder refence
		List<FolderReference> folRefs = TreeViewUtil.convertReferenceToFolderReference(folders);
		childNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(folRefs, isNursery, false, true, fieldbookMiddlewareService,
				isFolderOnly);
		return childNodes;
	}

	@ResponseBody
	@RequestMapping(value = "/expandTree/{type}/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String expandNurseryTree(@PathVariable String parentKey, @PathVariable String isFolderOnly, @PathVariable String type) {
		boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		boolean isNursery = (type != null && type.equalsIgnoreCase("N")) ? true : false;
		try {
			List<TreeNode> childNodes = getChildNodes(parentKey, isNursery, isFolderOnlyBool);
			return TreeViewUtil.convertTreeViewToJson(childNodes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/retrieveChildren/{parentKey}/{isFolderOnly}", method = RequestMethod.GET)
	public String retrieveChildren(@PathVariable String parentKey, @PathVariable String isFolderOnly) {
		boolean isFolderOnlyBool = "1".equalsIgnoreCase(isFolderOnly) ? true : false;
		try {
			if (Database.LOCAL.toString().equals(parentKey) || Database.CENTRAL.toString().equals(parentKey)) {
				try {
					Database instance = Database.LOCAL;
					if (Database.CENTRAL.toString().equals(parentKey)) {
						instance = Database.CENTRAL;
					}

					List<FolderReference> rootFolders = fieldbookMiddlewareService.getRootFolders(instance);
					String jsonResponse = TreeViewUtil.convertStudyFolderReferencesToJson(rootFolders, true, false, true,
							fieldbookMiddlewareService, isFolderOnlyBool);
					return jsonResponse;

				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
				return "[]";
			} else if (NumberUtils.isNumber(parentKey)) {

				int parentId = Integer.valueOf(parentKey);
				List<Reference> folders = fieldbookMiddlewareService.getChildrenOfFolder(parentId);
				// convert reference to folder refence
				List<FolderReference> folRefs = TreeViewUtil.convertReferenceToFolderReference(folders);
				return TreeViewUtil.convertStudyFolderReferencesToJson(folRefs, true, false, true, fieldbookMiddlewareService, isFolderOnlyBool);

			} else {
				LOG.error("parentKey = " + parentKey + " is not a number");
			}

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return "[]";
	}

	@ResponseBody
	@RequestMapping(value = "/isNameUnique", method = RequestMethod.POST)
	public Map<String, Object> isNameUnique(HttpServletRequest req) {
		String studyId = req.getParameter("studyId");
		String studyName = req.getParameter("name");
		Integer studyIdInt = Integer.valueOf(studyId);
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		try {

			int studyIdDb = fieldbookMiddlewareService.getProjectIdByName(studyName);									
			
			if (studyIdInt.intValue() == 0 && studyIdDb != 0) {
				// meaning new
				resultsMap.put("isSuccess", "0");
			} else if (studyIdInt.intValue() == studyIdDb) {
				resultsMap.put("isSuccess", "1");
			}

		} catch (NullPointerException ee) {
			// meaning there is no study
			resultsMap.put("isSuccess", "1");

		} catch (Exception e) {
			e.printStackTrace();
			resultsMap.put("isSuccess", "0");
			resultsMap.put("message", e.getMessage());
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
			if(folderName.equalsIgnoreCase(AppConstants.PROGRAM_NURSERIES.getString()) ||
					folderName.equalsIgnoreCase(AppConstants.PUBLIC_NURSERIES.getString())){
				
				 throw new MiddlewareQueryException(messageSource.getMessage("folder.name.not.unique", null, locale));
			}
			Integer parentFolderId = Integer.parseInt(parentKey);
			if (!TreeViewUtil.isFolder(parentFolderId, fieldbookMiddlewareService)) {
				DmsProject project = studyDataManager.getParentFolder(parentFolderId);
				if (project == null) {
					throw new Error("Parent folder cannot be null");
				}
				parentFolderId = project.getProjectId();
			}
			studyDataManager.addSubFolder(parentFolderId, folderName, folderName);
			resultsMap.put("isSuccess", "1");
		} catch (Exception e) {
			e.printStackTrace();
			resultsMap.put("isSuccess", "0");
			resultsMap.put("message", e.getMessage());
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
			if(newFolderName.equalsIgnoreCase(AppConstants.PROGRAM_NURSERIES.getString()) ||
					newFolderName.equalsIgnoreCase(AppConstants.PUBLIC_NURSERIES.getString())){
				
				 throw new MiddlewareQueryException(messageSource.getMessage("folder.name.not.unique", null, locale));
			}
			
			this.studyDataManager.renameSubFolder(newFolderName, Integer.parseInt(folderId));
			resultsMap.put("isSuccess", "1");
		} catch (MiddlewareQueryException e) {
			LOG.error(e.toString() + "\n" + e.getStackTrace());
			resultsMap.put("isSuccess", "0");
			resultsMap.put("message", e.getMessage());
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
			studyDataManager.deleteEmptyFolder(Integer.parseInt(folderId));
			resultsMap.put("isSuccess", "1");
		} catch (MiddlewareQueryException e) {
			LOG.error(e.toString() + "\n" + e.getStackTrace());
			resultsMap.put("isSuccess", "0");
			resultsMap.put("message", messageSource.getMessage("browse.nursery.delete.folder.has.children", null, locale));
			
		}
		return resultsMap;
	}

	@ResponseBody
	@RequestMapping(value = "/moveStudyFolder", method = RequestMethod.POST)
	public Map<String, Object> moveStudyFolder(HttpServletRequest req) {
		String sourceId = req.getParameter("sourceId");
		String targetId = req.getParameter("targetId");
		String isStudy = req.getParameter("isStudy");
		boolean isAStudy = "1".equalsIgnoreCase(isStudy) ? true : false;
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		try {
			studyDataManager.moveDmsProject(Integer.parseInt(sourceId), Integer.parseInt(targetId), isAStudy);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.toString() + "\n" + e.getStackTrace());
			throw new Error(e.getMessage());
		}
		return resultsMap;
	}
}
