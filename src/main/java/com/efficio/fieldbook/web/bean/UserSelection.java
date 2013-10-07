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
package com.efficio.fieldbook.web.bean;

import java.io.Serializable;
import org.generationcp.middleware.domain.etl.Workbook;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;

/**
 * Created by IntelliJ IDEA.
 * This bean models the various input that the user builds up over time to perform the actual loading operation
 */
public class UserSelection implements Serializable {
    
private static final long serialVersionUID = 1L;

    private String actualFileName;
    
    private String serverFileName;
    
    private Workbook workbook;
    private ImportedGermplasmMainInfo importedGermplasmMainInfo;
    
    

    public ImportedGermplasmMainInfo getImportedGermplasmMainInfo() {
		return importedGermplasmMainInfo;
	}

	public void setImportedGermplasmMainInfo(
			ImportedGermplasmMainInfo importedGermplasmMainInfo) {
		this.importedGermplasmMainInfo = importedGermplasmMainInfo;
	}

	public String getActualFileName() {
        return actualFileName;
    }

    public void setActualFileName(String actualFileName) {
        this.actualFileName = actualFileName;
    }

    public String getServerFileName() {
        return serverFileName;
    }

    public void setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

}
