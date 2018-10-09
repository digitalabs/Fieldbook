package com.efficio.fieldbook.web.trial.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping({LockUnlockStudyController.URL})
public class LockUnlockStudyController extends AbstractBaseFieldbookController {

	public static final String URL = "/TrialManager/changeLockedStatus";
	public static final String IS_SUCCESS = "isSuccess";
	
	@Resource
	private StudyDataManager studyDataManager;
	
	@ResponseBody
	@RequestMapping(value = "/lock/{studyId}", method = RequestMethod.POST)
	public Map<String, Object> changeLockedStatus(@PathVariable final int studyId, final HttpServletRequest req) {
		final Map<String, Object> results = new HashMap<>();
		final boolean isLocked = "1".equalsIgnoreCase(req.getParameter("isLocked"));
		this.studyDataManager.updateStudyLockedStatus(studyId, isLocked);
		
		results.put(LockUnlockStudyController.IS_SUCCESS, "1");
		return results;		
	}
	
	@Override
	public String getContentName() {
		return null;
	}

}
