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
package com.efficio.fieldbook.web.nursery.form;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;

/**
 * The Class ImportGermplasmListForm.
 */
public class ImportGermplasmListForm {
	
	/** The file. */
	private MultipartFile file;
	
	/** The has error. */
	private String hasError;
	
	/** The imported germplasm main info. */
	private ImportedGermplasmMainInfo importedGermplasmMainInfo;
	
	/** The imported germplasm. */
	private List<ImportedGermplasm> importedGermplasm;
	//for pagination
	/** The paginated imported germplasm. */
	private List<ImportedGermplasm> paginatedImportedGermplasm;	
	
	/** The current page. */
	private int currentPage;
	
	/** The total pages. */
	private int totalPages;
	
	/** The result per page. */
	private int resultPerPage = 10;	
	
	/** The method ids. */
	private Integer[] check;
	
	/** The choose specify check. */
	private String chooseSpecifyCheck;
	
	
	
    /**
     * Gets the result per page.
     *
     * @return the result per page
     */
    public int getResultPerPage() {
        return resultPerPage;
    }
    
    /**
     * Sets the result per page.
     *
     * @param resultPerPage the new result per page
     */
    public void setResultPerPage(int resultPerPage) {
        this.resultPerPage = resultPerPage;
    }

    /**
     * Gets the total pages.
     *
     * @return the total pages
     */
    public int getTotalPages(){
	    if(importedGermplasm != null && !importedGermplasm.isEmpty()){           
            totalPages = (int) Math.ceil((importedGermplasm.size() * 1f) / getResultPerPage()); 
        }else{
            totalPages = 0;
        }
	    return totalPages;
	}

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
        
        //assumption is there is an imported germplasm already
        if(importedGermplasm != null && !importedGermplasm.isEmpty()){
            int totalItemsPerPage = getResultPerPage();
            int start = (currentPage - 1) * totalItemsPerPage;
            int end = start + totalItemsPerPage;
            if(importedGermplasm.size() < end){
                end = importedGermplasm.size();
            }
            paginatedImportedGermplasm = importedGermplasm.subList(start, end);
            this.currentPage = currentPage;
        }else{
            this.currentPage = 0;
        }
    }

    /**
     * Sets the total pages.
     *
     * @param totalPages the new total pages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
        
    /**
     * Gets the paginated imported germplasm.
     *
     * @return the paginated imported germplasm
     */
    public List<ImportedGermplasm> getPaginatedImportedGermplasm() {
        //return paginatedImportedGermplasm;
    	return getImportedGermplasm();
    }
    
    /**
     * Sets the paginated imported germplasm.
     *
     * @param paginatedImportedGermplasm the new paginated imported germplasm
     */
    public void setPaginatedImportedGermplasm(
            List<ImportedGermplasm> paginatedImportedGermplasm) {
        this.paginatedImportedGermplasm = paginatedImportedGermplasm;
    }
   //end of pagination code

    /**
     * Gets the imported germplasm.
     *
     * @return the imported germplasm
     */
    public List<ImportedGermplasm> getImportedGermplasm() {
        return importedGermplasm;
    }
    
    /**
     * Sets the imported germplasm.
     *
     * @param importedGermplasm the new imported germplasm
     */
    public void setImportedGermplasm(List<ImportedGermplasm> importedGermplasm) {
        this.importedGermplasm = importedGermplasm;
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
	 * Instantiates a new import germplasm list form.
	 */
	public ImportGermplasmListForm(){
		setHasError("0");
	}
	
    /**
     * Gets the checks for error.
     *
     * @return the checks for error
     */
    public String getHasError() {
		return hasError;
	}

	/**
	 * Sets the checks for error.
	 *
	 * @param hasError the new checks for error
	 */
	public void setHasError(String hasError) {
		this.hasError = hasError;
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
    public MultipartFile getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file the new file
     */
    public void setFile(MultipartFile file) {
        this.file = file;
    }

	/**
	 * Gets the check.
	 *
	 * @return the check
	 */
	public Integer[] getCheck() {
		return check;
	}

	/**
	 * Sets the check.
	 *
	 * @param check the new check
	 */
	public void setCheck(Integer[] check) {
		this.check = check;
	}

	/**
	 * Gets the choose specify check.
	 *
	 * @return the choose specify check
	 */
	public String getChooseSpecifyCheck() {
		return chooseSpecifyCheck;
	}

	/**
	 * Sets the choose specify check.
	 *
	 * @param chooseSpecifyCheck the new choose specify check
	 */
	public void setChooseSpecifyCheck(String chooseSpecifyCheck) {
		this.chooseSpecifyCheck = chooseSpecifyCheck;
	}

		
    
    
}
