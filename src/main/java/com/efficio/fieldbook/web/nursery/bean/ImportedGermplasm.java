package com.efficio.fieldbook.web.nursery.bean;

import java.io.Serializable;

public class ImportedGermplasm implements Serializable {
	private Integer entryId;
    private String desig;
    private String gid;
    private String cross;
    private String source;
    private String entryCode;
    
    public ImportedGermplasm(){
        
    }
    
    public ImportedGermplasm(Integer entryId, String desig){
        this.entryId = entryId;
        this.desig = desig;
    }
    
    public ImportedGermplasm(Integer entryId, String desig, String gid, String cross, String source, String entryCode){
        this.entryId = entryId;
        this.desig = desig;
        this.gid = gid;
        this.cross = cross;
        this.source = source;
        this.entryCode = entryCode;
    }
    
    
    
    public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getCross() {
		return cross;
	}

	public void setCross(String cross) {
		this.cross = cross;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getEntryCode() {
		return entryCode;
	}

	public void setEntryCode(String entryCode) {
		this.entryCode = entryCode;
	}

	public Integer getEntryId(){
        return entryId;
    }
    
    public void setEntryId(Integer entryId){
        this.entryId = entryId;
    }
    
    public String getDesig(){
        return desig;
    }
    
    public void setDesig(String desig){
        this.desig = desig;
    }
}
