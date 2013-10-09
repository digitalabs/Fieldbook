/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.util;

import java.util.Properties;


/**
 * @author Efficio.Daniel
 *
 */
public class GitRepositoryState {
    String branch; // =${git.branch}
    String commitId; // =${git.commit.id}
    String commitIdAbbrev; // =${git.commit.id.abbrev}
    String buildUserName; // =${git.build.user.name}
    String buildUserEmail; // =${git.build.user.email}
    String buildTime; // =${git.build.time}
    String commitUserName; // =${git.commit.user.name}
    String commitUserEmail; // =${git.commit.user.email}
    String commitMessageFull; // =${git.commit.message.full}
    String commitMessageShort; // =${git.commit.message.short}
    String commitTime; // =${git.commit.time}
    String describe;//git.commit.id.describe
    String mavenProjectVersion; // =${maven.project.version}

    public GitRepositoryState() {
    }

    public GitRepositoryState(Properties properties)
    {
       this.branch = properties.get("git.branch").toString();
       this.describe = properties.get("git.commit.id.describe").toString();
       this.commitId = properties.get("git.commit.id").toString();
       this.buildUserName = properties.get("git.build.user.name").toString();
       this.buildUserEmail = properties.get("git.build.user.email").toString();
       this.buildTime = properties.get("git.build.time").toString();
       this.commitUserName = properties.get("git.commit.user.name").toString();
       this.commitUserEmail = properties.get("git.commit.user.email").toString();
       this.commitMessageShort = properties.get("git.commit.message.short").toString();
       this.commitMessageFull = properties.get("git.commit.message.full").toString();
       this.commitTime = properties.get("git.commit.time").toString();
       this.commitIdAbbrev = properties.get("git.commit.id.abbrev").toString();
    }
    
    
    
    
    public String getDescribe() {
        return describe;
    }

    
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getBranch() {
      return branch;
    }

    public void setBranch(String branch) {
      this.branch = branch;
    }

    public String getCommitId() {
      return commitId;
    }

    public void setCommitId(String commitId) {
      this.commitId = commitId;
    }

    public void setCommitIdAbbrev(String commitIdAbbrev) {
      this.commitIdAbbrev = commitIdAbbrev;
    }

    public String getCommitIdAbbrev() {
      return commitIdAbbrev;
    }

    public String getBuildUserName() {
      return buildUserName;
    }

    public void setBuildUserName(String buildUserName) {
      this.buildUserName = buildUserName;
    }

    public String getBuildUserEmail() {
      return buildUserEmail;
    }

    public void setBuildUserEmail(String buildUserEmail) {
      this.buildUserEmail = buildUserEmail;
    }

    public String getCommitUserName() {
      return commitUserName;
    }

    public void setCommitUserName(String commitUserName) {
      this.commitUserName = commitUserName;
    }

    public String getCommitUserEmail() {
      return commitUserEmail;
    }

    public void setCommitUserEmail(String commitUserEmail) {
      this.commitUserEmail = commitUserEmail;
    }

    public String getCommitMessageFull() {
      return commitMessageFull;
    }

    public void setCommitMessageFull(String commitMessageFull) {
      this.commitMessageFull = commitMessageFull;
    }

    public String getCommitMessageShort() {
      return commitMessageShort;
    }

    public void setCommitMessageShort(String commitMessageShort) {
      this.commitMessageShort = commitMessageShort;
    }

    public String getCommitTime() {
      return commitTime;
    }

    public void setCommitTime(String commitTime) {
      this.commitTime = commitTime;
    }

    public String getBuildTime() {
      return buildTime;
    }

    public void setBuildTime(String buildTime) {
      this.buildTime = buildTime;
    }

    public String getMavenProjectVersion() {
      return mavenProjectVersion;
    }

    public void setMavenProjectVersion(String mavenProjectVersion) {
      this.mavenProjectVersion = mavenProjectVersion;
    }

    

  }