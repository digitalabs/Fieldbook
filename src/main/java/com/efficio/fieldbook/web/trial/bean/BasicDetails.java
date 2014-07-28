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
    private Map<String, String> basicDetails;

    private Integer folderId;
    private String folderName;
    private String folderNameLabel;
    private String userName;
    private Integer userID;
    private Integer studyID;

    public BasicDetails() {
        basicDetails = new HashMap<String, String>();
    }

    public Map<String, String> getBasicDetails() {
        return basicDetails;
    }

    public void setBasicDetails(Map<String, String> basicDetails) {
        this.basicDetails = basicDetails;
    }

    public Integer getFolderId() {
        return folderId;
    }

    public void setFolderId(Integer folderId) {
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Integer getStudyID() {
        return studyID;
    }

    public void setStudyID(Integer studyID) {
        this.studyID = studyID;
    }
}
