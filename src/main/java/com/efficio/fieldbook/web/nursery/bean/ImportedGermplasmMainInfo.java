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
package com.efficio.fieldbook.web.nursery.bean;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.apache.poi.ss.usermodel.Workbook;

/*
 * Daniel Jao
 * This should hold information when doing import of germplasm list
 * The Class ImportedGermplasmMainInfo.
 */
public class ImportedGermplasmMainInfo implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3328879715589849561L;

	/** The file. */
	public File file;

    /** The temp file name. */
    private String tempFileName;
    
    /** The server filename. */
    private String serverFilename;
    
    /** The original filename. */
    private String originalFilename;
    
    /** The list name. */
    private String listName;
    
    /** The list title. */
    private String listTitle;
    
    /** The list type. */
    private String listType;
    
    /** The list date. */
    private Date listDate;
    
    /** The inp. */
    private InputStream inp;
    
    /** The wb. */
    private Workbook wb;
    
    /** The imported germplasm list. */
    private ImportedGermplasmList importedGermplasmList;
    
    /** The file is valid. */
    private Boolean fileIsValid;
    
    /** The error messages. */
    private Set<String> errorMessages;
    
    /** The is advance import type. */
    private boolean isAdvanceImportType;
    
	/**
	 * Checks if is advance import type.
	 *
	 * @return true, if is advance import type
	 */
	public boolean isAdvanceImportType() {
		return isAdvanceImportType;
	}

	/**
	 * Sets the advance import type.
	 *
	 * @param isAdvanceImportType the new advance import type
	 */
	public void setAdvanceImportType(boolean isAdvanceImportType) {
		this.isAdvanceImportType = isAdvanceImportType;
	}

	/**
	 * Gets the error messages.
	 *
	 * @return the error messages
	 */
	public Set<String> getErrorMessages() {
		return errorMessages;
	}

	/**
	 * Sets the error messages.
	 *
	 * @param errorMessages the new error messages
	 */
	public void setErrorMessages(Set<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Sets the file.
	 *
	 * @param file the new file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Gets the temp file name.
	 *
	 * @return the temp file name
	 */
	public String getTempFileName() {
		return tempFileName;
	}

	/**
	 * Sets the temp file name.
	 *
	 * @param tempFileName the new temp file name
	 */
	public void setTempFileName(String tempFileName) {
		this.tempFileName = tempFileName;
	}

	/**
	 * Gets the original filename.
	 *
	 * @return the original filename
	 */
	public String getOriginalFilename() {
		return originalFilename;
	}

	/**
	 * Sets the original filename.
	 *
	 * @param originalFilename the new original filename
	 */
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	/**
	 * Gets the list name.
	 *
	 * @return the list name
	 */
	public String getListName() {
		return listName;
	}

	/**
	 * Sets the list name.
	 *
	 * @param listName the new list name
	 */
	public void setListName(String listName) {
		this.listName = listName;
	}

	/**
	 * Gets the list title.
	 *
	 * @return the list title
	 */
	public String getListTitle() {
		return listTitle;
	}

	/**
	 * Sets the list title.
	 *
	 * @param listTitle the new list title
	 */
	public void setListTitle(String listTitle) {
		this.listTitle = listTitle;
	}

	/**
	 * Gets the list type.
	 *
	 * @return the list type
	 */
	public String getListType() {
		return listType;
	}

	/**
	 * Sets the list type.
	 *
	 * @param listType the new list type
	 */
	public void setListType(String listType) {
		this.listType = listType;
	}

	/**
	 * Gets the list date.
	 *
	 * @return the list date
	 */
	public Date getListDate() {
		return listDate;
	}

	/**
	 * Sets the list date.
	 *
	 * @param listDate the new list date
	 */
	public void setListDate(Date listDate) {
		this.listDate = listDate;
	}

	/**
	 * Gets the inp.
	 *
	 * @return the inp
	 */
	public InputStream getInp() {
		return inp;
	}

	/**
	 * Sets the inp.
	 *
	 * @param inp the new inp
	 */
	public void setInp(InputStream inp) {
		this.inp = inp;
	}

	/**
	 * Gets the wb.
	 *
	 * @return the wb
	 */
	public Workbook getWb() {
		return wb;
	}

	/**
	 * Sets the wb.
	 *
	 * @param wb the new wb
	 */
	public void setWb(Workbook wb) {
		this.wb = wb;
	}

	/**
	 * Gets the imported germplasm list.
	 *
	 * @return the imported germplasm list
	 */
	public ImportedGermplasmList getImportedGermplasmList() {
		return importedGermplasmList;
	}

	/**
	 * Sets the imported germplasm list.
	 *
	 * @param importedGermplasmList the new imported germplasm list
	 */
	public void setImportedGermplasmList(ImportedGermplasmList importedGermplasmList) {
		this.importedGermplasmList = importedGermplasmList;
	}

	/**
	 * Gets the file is valid.
	 *
	 * @return the file is valid
	 */
	public Boolean getFileIsValid() {
		return fileIsValid;
	}

	/**
	 * Sets the file is valid.
	 *
	 * @param fileIsValid the new file is valid
	 */
	public void setFileIsValid(Boolean fileIsValid) {
		this.fileIsValid = fileIsValid;
	}

	/**
	 * Gets the server filename.
	 *
	 * @return the server filename
	 */
	public String getServerFilename() {
		return serverFilename;
	}

	/**
	 * Sets the server filename.
	 *
	 * @param serverFilename the new server filename
	 */
	public void setServerFilename(String serverFilename) {
		this.serverFilename = serverFilename;
	}
    
}
