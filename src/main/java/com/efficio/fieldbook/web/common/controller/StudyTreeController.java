
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
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareException;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
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
	public static final String IS_SUCCESS = "isSuccess";
	public static final String MESSAGE = "message";
	public static final String NEW_FOLDER_ID = "newFolderId";

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private StudyDataManager studyDataManager;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ContextUtil contextUtil;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private PlatformTransactionManager transactionManager;

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

	private List<TreeNode> getChildNodes(String parentKey, boolean isNursery, boolean isFolderOnly) {
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		if (parentKey != null && !"".equals(parentKey)) {
			try {
				if (StudyTreeController.LOCAL.equals(parentKey)) {
					List<Reference> rootFolders =
							this.studyDataManager.getRootFolders(this.getCurrentProgramUUID(), isNursery ? StudyType.nurseries()
									: StudyType.trials());
					childNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(rootFolders, false, true, isFolderOnly);
				} else if (NumberUtils.isNumber(parentKey)) {
					childNodes = this.getChildrenTreeNodes(parentKey, isNursery, isFolderOnly);
				} else {
					StudyTreeController.LOG.error("parentKey = " + parentKey + " is not a number");
				}
			} catch (Exception e) {
				StudyTreeController.LOG.error(e.getMessage(), e);
			}
		}
		return childNodes;
	}

	private List<TreeNode> getChildrenTreeNodes(String parentKey, boolean isNursery, boolean isFolderOnly) {
		List<TreeNode> childNodes = new ArrayList<TreeNode>();
		int parentId = Integer.valueOf(parentKey);
		List<Reference> folders =
				this.studyDataManager.getChildrenOfFolder(parentId, this.getCurrentProgramUUID(), isNursery ? StudyType.nurseries()
						: StudyType.trials());

		childNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(folders, false, true, isFolderOnly);
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
				List<Reference> folders =
						this.studyDataManager.getChildrenOfFolder(parentId, this.getCurrentProgramUUID(), StudyType.nurseriesAndTrials());
				return TreeViewUtil.convertStudyFolderReferencesToJson(folders, false, true, isFolderOnlyBool);

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
			List<Reference> rootFolders =
					this.studyDataManager.getRootFolders(this.getCurrentProgramUUID(), StudyType.nurseriesAndTrials());
			return TreeViewUtil.convertStudyFolderReferencesToJson(rootFolders, false, true, isFolderOnly);
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
		} catch (MiddlewareException e) {
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
	public Map<String, Object> addStudyFolder(final HttpServletRequest req) {

		final Map<String, Object> resultsMap = new HashMap<String, Object>();

		try {

			final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {

					final String parentKey = req.getParameter("parentFolderId");
					final String folderName = req.getParameter("folderName");
					final Locale locale = LocaleContextHolder.getLocale();

					if (folderName.equalsIgnoreCase(AppConstants.NURSERIES.getString())
							|| folderName.equalsIgnoreCase(AppConstants.TRIALS.getString())) {
						throw new MiddlewareQueryException(StudyTreeController.this.messageSource.getMessage("folder.name.not.unique",
								null, locale));
					}
					Integer parentFolderId = Integer.parseInt(parentKey);
					if (StudyTreeController.this.studyDataManager.isStudy(parentFolderId)) {
						DmsProject project = StudyTreeController.this.studyDataManager.getParentFolder(parentFolderId);
						if (project == null) {
							throw new MiddlewareQueryException("Parent folder cannot be null");
						}
						parentFolderId = project.getProjectId();
					}
					int newFolderId =
							StudyTreeController.this.studyDataManager.addSubFolder(parentFolderId, folderName, folderName,
									StudyTreeController.this.getCurrentProgramUUID());
					resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
					resultsMap.put(StudyTreeController.NEW_FOLDER_ID, Integer.toString(newFolderId));

				}
			});

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
	@RequestMapping(value = "/isFolderEmpty/{folderId}/{studyType}/{folderName}", method = RequestMethod.POST)
	public Map<String, Object> isFolderEmpty(HttpServletRequest req, @PathVariable String folderId, @PathVariable String studyType, @PathVariable String folderName) {
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		Locale locale = LocaleContextHolder.getLocale();
		boolean isFolderEmpty = this.studyDataManager.isFolderEmpty(Integer.parseInt(folderId), this.getCurrentProgramUUID(), StudyType.nurseriesAndTrials());
		if(isFolderEmpty){
			resultsMap.put(StudyTreeController.IS_SUCCESS, "1");
		} else {
			resultsMap.put(StudyTreeController.IS_SUCCESS, "0");
			List<StudyType> studyTypeList = studyType.equals(StudyType.N.getName())? StudyType.nurseries():StudyType.trials();
			isFolderEmpty = this.studyDataManager.isFolderEmpty(Integer.parseInt(folderId), this.getCurrentProgramUUID(), studyTypeList);
			String message;
			if(!isFolderEmpty){
				message = "browse.nursery.delete.folder.not.empty";
			} else {
				message = studyType.equals(StudyType.N.getName())? "browse.trial.delete.folder.contains.trials":"browse.nursery.delete.folder.contains.nurseries";
			}
			resultsMap.put(StudyTreeController.MESSAGE, this.messageSource.getMessage(message, new Object[] {folderName},
					locale));
			
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
			this.studyDataManager.moveDmsProject(Integer.parseInt(sourceId), Integer.parseInt(targetId), isAStudy);
		} catch (MiddlewareQueryException e) {
			StudyTreeController.LOG.error(e.getMessage(), e);
			throw new MiddlewareQueryException(e.getMessage(), e);
		}
		return resultsMap;
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	void setFieldbookMiddlewareService(FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	void setStudyDataManager(StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

	void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	void setRequest(HttpServletRequest request) {
		this.request = request;
	}
}
