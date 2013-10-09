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


// TODO: Auto-generated Javadoc
/**
 * The Class GitRepositoryState.
 *
 * @author Efficio.Daniel
 */
public class GitRepositoryState {
    
    /** The branch. */
    String branch; // =${git.branch}
    
    /** The commit id. */
    String commitId; // =${git.commit.id}
    
    /** The commit id abbrev. */
    String commitIdAbbrev; // =${git.commit.id.abbrev}
    
    /** The build user name. */
    String buildUserName; // =${git.build.user.name}
    
    /** The build user email. */
    String buildUserEmail; // =${git.build.user.email}
    
    /** The build time. */
    String buildTime; // =${git.build.time}
    
    /** The commit user name. */
    String commitUserName; // =${git.commit.user.name}
    
    /** The commit user email. */
    String commitUserEmail; // =${git.commit.user.email}
    
    /** The commit message full. */
    String commitMessageFull; // =${git.commit.message.full}
    
    /** The commit message short. */
    String commitMessageShort; // =${git.commit.message.short}
    
    /** The commit time. */
    String commitTime; // =${git.commit.time}
    
    /** The describe. */
    String describe;//git.commit.id.describe
    
    /** The maven project version. */
    String mavenProjectVersion; // =${maven.project.version}

    /**
     * Instantiates a new git repository state.
     */
    public GitRepositoryState() {
    }

    /**
     * Instantiates a new git repository state.
     *
     * @param properties the properties
     */
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
    
    
    
    
    /**
     * Gets the describe.
     *
     * @return the describe
     */
    public String getDescribe() {
        return describe;
    }

    
    /**
     * Sets the describe.
     *
     * @param describe the new describe
     */
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    /**
     * Gets the branch.
     *
     * @return the branch
     */
    public String getBranch() {
      return branch;
    }

    /**
     * Sets the branch.
     *
     * @param branch the new branch
     */
    public void setBranch(String branch) {
      this.branch = branch;
    }

    /**
     * Gets the commit id.
     *
     * @return the commit id
     */
    public String getCommitId() {
      return commitId;
    }

    /**
     * Sets the commit id.
     *
     * @param commitId the new commit id
     */
    public void setCommitId(String commitId) {
      this.commitId = commitId;
    }

    /**
     * Sets the commit id abbrev.
     *
     * @param commitIdAbbrev the new commit id abbrev
     */
    public void setCommitIdAbbrev(String commitIdAbbrev) {
      this.commitIdAbbrev = commitIdAbbrev;
    }

    /**
     * Gets the commit id abbrev.
     *
     * @return the commit id abbrev
     */
    public String getCommitIdAbbrev() {
      return commitIdAbbrev;
    }

    /**
     * Gets the builds the user name.
     *
     * @return the builds the user name
     */
    public String getBuildUserName() {
      return buildUserName;
    }

    /**
     * Sets the builds the user name.
     *
     * @param buildUserName the new builds the user name
     */
    public void setBuildUserName(String buildUserName) {
      this.buildUserName = buildUserName;
    }

    /**
     * Gets the builds the user email.
     *
     * @return the builds the user email
     */
    public String getBuildUserEmail() {
      return buildUserEmail;
    }

    /**
     * Sets the builds the user email.
     *
     * @param buildUserEmail the new builds the user email
     */
    public void setBuildUserEmail(String buildUserEmail) {
      this.buildUserEmail = buildUserEmail;
    }

    /**
     * Gets the commit user name.
     *
     * @return the commit user name
     */
    public String getCommitUserName() {
      return commitUserName;
    }

    /**
     * Sets the commit user name.
     *
     * @param commitUserName the new commit user name
     */
    public void setCommitUserName(String commitUserName) {
      this.commitUserName = commitUserName;
    }

    /**
     * Gets the commit user email.
     *
     * @return the commit user email
     */
    public String getCommitUserEmail() {
      return commitUserEmail;
    }

    /**
     * Sets the commit user email.
     *
     * @param commitUserEmail the new commit user email
     */
    public void setCommitUserEmail(String commitUserEmail) {
      this.commitUserEmail = commitUserEmail;
    }

    /**
     * Gets the commit message full.
     *
     * @return the commit message full
     */
    public String getCommitMessageFull() {
      return commitMessageFull;
    }

    /**
     * Sets the commit message full.
     *
     * @param commitMessageFull the new commit message full
     */
    public void setCommitMessageFull(String commitMessageFull) {
      this.commitMessageFull = commitMessageFull;
    }

    /**
     * Gets the commit message short.
     *
     * @return the commit message short
     */
    public String getCommitMessageShort() {
      return commitMessageShort;
    }

    /**
     * Sets the commit message short.
     *
     * @param commitMessageShort the new commit message short
     */
    public void setCommitMessageShort(String commitMessageShort) {
      this.commitMessageShort = commitMessageShort;
    }

    /**
     * Gets the commit time.
     *
     * @return the commit time
     */
    public String getCommitTime() {
      return commitTime;
    }

    /**
     * Sets the commit time.
     *
     * @param commitTime the new commit time
     */
    public void setCommitTime(String commitTime) {
      this.commitTime = commitTime;
    }

    /**
     * Gets the builds the time.
     *
     * @return the builds the time
     */
    public String getBuildTime() {
      return buildTime;
    }

    /**
     * Sets the builds the time.
     *
     * @param buildTime the new builds the time
     */
    public void setBuildTime(String buildTime) {
      this.buildTime = buildTime;
    }

    /**
     * Gets the maven project version.
     *
     * @return the maven project version
     */
    public String getMavenProjectVersion() {
      return mavenProjectVersion;
    }

    /**
     * Sets the maven project version.
     *
     * @param mavenProjectVersion the new maven project version
     */
    public void setMavenProjectVersion(String mavenProjectVersion) {
      this.mavenProjectVersion = mavenProjectVersion;
    }

    

  }