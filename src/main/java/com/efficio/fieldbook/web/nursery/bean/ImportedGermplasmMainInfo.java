package com.efficio.fieldbook.web.nursery.bean;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import org.apache.poi.ss.usermodel.Workbook;

public class ImportedGermplasmMainInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3328879715589849561L;

	public File file;

    private String tempFileName;
    
    private Integer currentSheet;
    private Integer currentRow;
    private Integer currentColumn;
    
    private String originalFilename;
    private String listName;
    private String listTitle;
    private String listType;
    private Date listDate;
    
    private InputStream inp;
    private Workbook wb;
    
    private ImportedGermplasmList importedGermplasmList;
    
    private Boolean fileIsValid;
    
    
}
