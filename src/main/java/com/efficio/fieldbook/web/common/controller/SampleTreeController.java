package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

	public static final String NODE_NONE = "None";

	@Resource
	private UserTreeStateService userTreeStateService;

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
					.saveOrUpdateUserProgramTreeState(this.contextUtil.getCurrentWorkbenchUserId(), this.getCurrentProgramUUID(), type, states);
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
		final Integer userID = this.contextUtil.getCurrentWorkbenchUserId();
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

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

}
