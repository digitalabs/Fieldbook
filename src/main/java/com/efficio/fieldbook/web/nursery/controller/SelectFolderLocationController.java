package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.util.TreeViewUtil;


@Controller
@RequestMapping(SelectFolderLocationController.URL)
public class SelectFolderLocationController extends
        AbstractBaseFieldbookController{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SelectFolderLocationController.class);

    /** The Constant URL. */
    public static final String URL = "/Common/selectFolderLocation";

    @Resource
    private StudyDataManager studyDataManager;
    
    @Override
    public String getContentName() {
        return "Common/selectFolderLocation";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String show(Model model) {
        try {
            
            List<FolderReference> folderTree = studyDataManager.getFolderTree();
            List<FolderReference> rootFolder = new ArrayList<FolderReference>();
            rootFolder.add(new FolderReference(1, "Root Folder", "Root Folder"));
            rootFolder.get(0).setSubFolders(folderTree);
            model.addAttribute("folderBrowserTree", TreeViewUtil.convertFolderReferencesToJson(rootFolder, false));

        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return super.showAjaxPage(model, getContentName());
    }

}
