package com.efficio.fieldbook.web.nursery.bean;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.apache.poi.ss.usermodel.Workbook;

public class ImportedGermplasmMainInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3328879715589849561L;

	public File file;

    private String tempFileName;
    
    private String serverFilename;
    private String originalFilename;
    private String listName;
    private String listTitle;
    private String listType;
    private Date listDate;
    
    private InputStream inp;
    private Workbook wb;
    
    private ImportedGermplasmList importedGermplasmList;
    
    private Boolean fileIsValid;
    
    private Set<String> errorMessages;
    
    

	public Set<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(Set<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getTempFileName() {
		return tempFileName;
	}

	public void setTempFileName(String tempFileName) {
		this.tempFileName = tempFileName;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getListTitle() {
		return listTitle;
	}

	public void setListTitle(String listTitle) {
		this.listTitle = listTitle;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public Date getListDate() {
		return listDate;
	}

	public void setListDate(Date listDate) {
		this.listDate = listDate;
	}

	public InputStream getInp() {
		return inp;
	}

	public void setInp(InputStream inp) {
		this.inp = inp;
	}

	public Workbook getWb() {
		return wb;
	}

	public void setWb(Workbook wb) {
		this.wb = wb;
	}

	public ImportedGermplasmList getImportedGermplasmList() {
		return importedGermplasmList;
	}

	public void setImportedGermplasmList(ImportedGermplasmList importedGermplasmList) {
		this.importedGermplasmList = importedGermplasmList;
	}

	public Boolean getFileIsValid() {
		return fileIsValid;
	}

	public void setFileIsValid(Boolean fileIsValid) {
		this.fileIsValid = fileIsValid;
	}

	public String getServerFilename() {
		return serverFilename;
	}

	public void setServerFilename(String serverFilename) {
		this.serverFilename = serverFilename;
	}
    
    
    
}
