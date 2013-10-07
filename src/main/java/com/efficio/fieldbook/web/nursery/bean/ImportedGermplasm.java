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

// TODO: Auto-generated Javadoc
/**
 * The Class ImportedGermplasm.
 */
public class ImportedGermplasm implements Serializable {
	
	/** The entry id. */
	private Integer entryId;
    
    /** The desig. */
    private String desig;
    
    /** The gid. */
    private String gid;
    
    /** The cross. */
    private String cross;
    
    /** The source. */
    private String source;
    
    /** The entry code. */
    private String entryCode;
    
    /**
     * Instantiates a new imported germplasm.
     */
    public ImportedGermplasm(){
        
    }
    
    /**
     * Instantiates a new imported germplasm.
     *
     * @param entryId the entry id
     * @param desig the desig
     */
    public ImportedGermplasm(Integer entryId, String desig){
        this.entryId = entryId;
        this.desig = desig;
    }
    
    /**
     * Instantiates a new imported germplasm.
     *
     * @param entryId the entry id
     * @param desig the desig
     * @param gid the gid
     * @param cross the cross
     * @param source the source
     * @param entryCode the entry code
     */
    public ImportedGermplasm(Integer entryId, String desig, String gid, String cross, String source, String entryCode){
        this.entryId = entryId;
        this.desig = desig;
        this.gid = gid;
        this.cross = cross;
        this.source = source;
        this.entryCode = entryCode;
    }
    
    
    
    /**
     * Gets the gid.
     *
     * @return the gid
     */
    public String getGid() {
		return gid;
	}

	/**
	 * Sets the gid.
	 *
	 * @param gid the new gid
	 */
	public void setGid(String gid) {
		this.gid = gid;
	}

	/**
	 * Gets the cross.
	 *
	 * @return the cross
	 */
	public String getCross() {
		return cross;
	}

	/**
	 * Sets the cross.
	 *
	 * @param cross the new cross
	 */
	public void setCross(String cross) {
		this.cross = cross;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gets the entry code.
	 *
	 * @return the entry code
	 */
	public String getEntryCode() {
		return entryCode;
	}

	/**
	 * Sets the entry code.
	 *
	 * @param entryCode the new entry code
	 */
	public void setEntryCode(String entryCode) {
		this.entryCode = entryCode;
	}

	/**
	 * Gets the entry id.
	 *
	 * @return the entry id
	 */
	public Integer getEntryId(){
        return entryId;
    }
    
    /**
     * Sets the entry id.
     *
     * @param entryId the new entry id
     */
    public void setEntryId(Integer entryId){
        this.entryId = entryId;
    }
    
    /**
     * Gets the desig.
     *
     * @return the desig
     */
    public String getDesig(){
        return desig;
    }
    
    /**
     * Sets the desig.
     *
     * @param desig the new desig
     */
    public void setDesig(String desig){
        this.desig = desig;
    }
}
