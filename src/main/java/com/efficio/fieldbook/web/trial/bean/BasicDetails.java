package com.efficio.fieldbook.web.trial.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 7/8/2014
 * Time: 5:14 PM
 */
public class BasicDetails implements TabInfoBean{
    private Map<Integer, String> basicDetails;

    private int folderId;
    private String folderName;
    private String folderNameLabel;

    public BasicDetails() {
        basicDetails = new HashMap<Integer, String>();
    }

    public Map<Integer, String> getBasicDetails() {
        return basicDetails;
    }

    public void setBasicDetails(Map<Integer, String> basicDetails) {
        this.basicDetails = basicDetails;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderNameLabel() {
        return folderNameLabel;
    }

    public void setFolderNameLabel(String folderNameLabel) {
        this.folderNameLabel = folderNameLabel;
    }
}
