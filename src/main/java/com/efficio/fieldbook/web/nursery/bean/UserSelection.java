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

import java.io.Serializable;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;

/**
 * This bean models the various input that the user builds up over time
 * to perform the actual loading operation.
 */
public class UserSelection implements Serializable {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The actual file name. */
    private String actualFileName;
    
    /** The server file name. */
    private String serverFileName;
    
    /** The workbook. */
    private transient Workbook workbook;
    
    /** The field layout random. */
    private boolean fieldLayoutRandom;
    
    /** The imported germplasm main info. */
    private ImportedGermplasmMainInfo importedGermplasmMainInfo;

    /** The is import valid. */
    private boolean isImportValid;
    
    /** The study details list.*/
    private transient List<StudyDetails> studyDetailsList;
    
	/** The measurement row list. */
	private List<MeasurementRow> measurementRowList;
	
    /** The current page. */
    private int currentPage;
    
    /** The current page germplasm list. */
    private int currentPageGermplasmList;
    
	/**
	 * Gets the current page.
	 *
	 * @return the current page
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * Sets the current page.
	 *
	 * @param currentPage the new current page
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	
	/**
     * Gets the current page germplasm list.
     *
     * @return the current page germplasm list
     */
    public int getCurrentPageGermplasmList() {
		return currentPageGermplasmList;
	}

	/**
	 * Sets the current page germplasm list.
	 *
	 * @param currentPageGermplasmList the new current page germplasm list
	 */
	public void setCurrentPageGermplasmList(int currentPageGermplasmList) {
		this.currentPageGermplasmList = currentPageGermplasmList;
	}

	/**
     * Checks if is import valid.
     *
     * @return true, if is import valid
     */
    public boolean isImportValid() {
        return isImportValid;
    }
    
    /**
     * Sets the import valid.
     *
     * @param isImportValid the new import valid
     */
    public void setImportValid(boolean isImportValid) {
        this.isImportValid = isImportValid;
    }

    /**
     * Gets the imported germplasm main info.
     *
     * @return the imported germplasm main info
     */
    public ImportedGermplasmMainInfo getImportedGermplasmMainInfo() {
        return importedGermplasmMainInfo;
    }

    /**
     * Sets the imported germplasm main info.
     *
     * @param importedGermplasmMainInfo the new imported germplasm main info
     */
    public void setImportedGermplasmMainInfo(
            ImportedGermplasmMainInfo importedGermplasmMainInfo) {
        this.importedGermplasmMainInfo = importedGermplasmMainInfo;
    }

    /**
     * Gets the actual file name.
     *
     * @return the actual file name
     */
    public String getActualFileName() {
        return actualFileName;
    }

    /**
     * Sets the actual file name.
     *
     * @param actualFileName the new actual file name
     */
    public void setActualFileName(String actualFileName) {
        this.actualFileName = actualFileName;
    }

    /**
     * Gets the server file name.
     *
     * @return the server file name
     */
    public String getServerFileName() {
        return serverFileName;
    }

    /**
     * Sets the server file name.
     *
     * @param serverFileName the new server file name
     */
    public void setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
    }

    /**
     * Gets the workbook.
     *
     * @return the workbook
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * Sets the workbook.
     *
     * @param workbook the new workbook
     */
    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * Gets the field layout random.
     *
     * @return the field layout random
     */
    public Boolean getFieldLayoutRandom() {
        return fieldLayoutRandom;
    }

    /**
     * Sets the field layout random.
     *
     * @param fieldLayoutRandom the new field layout random
     */
    public void setFieldLayoutRandom(Boolean fieldLayoutRandom) {
        this.fieldLayoutRandom = fieldLayoutRandom;
    }
    
    /**
     * Gets the study details list.
     *
     * @return the study details list
     */
    public List<StudyDetails> getStudyDetailsList(){
        return studyDetailsList;
    }

    /**
     * Sets the study details list.
     *
     * @param studyDetailsList the new study details list
     */
    public void setStudyDetailsList(List<StudyDetails> studyDetailsList) {
        this.studyDetailsList = studyDetailsList;
    }

	/**
	 * Gets the measurement row list.
	 *
	 * @return the measurement row list
	 */
	public List<MeasurementRow> getMeasurementRowList() {
		return measurementRowList;
	}

	/**
	 * Sets the measurement row list.
	 *
	 * @param measurementRowList the new measurement row list
	 */
	public void setMeasurementRowList(List<MeasurementRow> measurementRowList) {
		this.measurementRowList = measurementRowList;
	}

    
}
