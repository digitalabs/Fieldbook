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

import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;

import java.io.Serializable;
import java.util.List;

/**
 * The Class ImportedCrosses.
 */
public class ImportedCrosses implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private String notes;
	private String seedsHarvested;
	private String crossingDate;
	private String rawBreedingMethod;

	/**
	 * The entry id.
	 */
	private Integer entryId;

	/**
	 * The female desig.
	 */
	private String femaleDesig;

	/**
	 * The male desig.
	 */
	private String maleDesig;

	/**
	 * The female gid.
	 */
	private String femaleGid;

	/**
	 * The male gid.
	 */
	private String maleGid;

	/**
	 * The desig.
	 */
	private String desig;

	/**
	 * The gid.
	 */
	private String gid;

	/**
	 * The cross.
	 */
	private String cross;

	/**
	 * The source.
	 */
	private String source;

	/**
	 * The entry code.
	 */
	private String entryCode;

	/**
	 * The check.
	 */
	private String check;

	/**
	 * The breeding method id.
	 */
	private Integer breedingMethodId;

	/**
	 * Germplasm's GPID1
	 */
	private Integer gpid1;

	/**
	 * Germplasm's GPID2
	 */
	private Integer gpid2;

	/**
	 * Germplasm's Number of Parents
	 */
	private Integer gnpgs;

	/**
	 * List of Names associated with the Germplasm
	 */
	private List<Name> names;

	private Integer checkId;

	private String checkName;

	private Integer index;

	private String groupName;

	/**
	 * Instantiates a new imported germplasm.
	 */
	public ImportedCrosses() {

	}

	/**
	 * Instantiates a new imported germplasm.
	 *
	 * @param entryId the entry id
	 * @param desig   the desig
	 * @param check   the check
	 */
	public ImportedCrosses(Integer entryId, String desig, String check) {
		this.entryId = entryId;
		this.desig = desig;
		this.check = check;
	}

	/**
	 * Instantiates a new imported germplasm.
	 *
	 * @param entryId   the entry id
	 * @param desig     the desig
	 * @param gid       the gid
	 * @param cross     the cross
	 * @param source    the source
	 * @param entryCode the entry code
	 * @param check     the check
	 */
	public ImportedCrosses(Integer entryId, String desig, String gid, String cross
			, String source, String entryCode, String check) {
		this.entryId = entryId;
		this.desig = desig;
		this.gid = gid;
		this.cross = cross;
		this.source = source;
		this.entryCode = entryCode;
		this.check = check;
	}

	/**
	 * Special constructor to create crosses driectly from parsed data
	 *
	 * @param femaleListData
	 * @param maleListData
	 * @param maleStudyName
	 * @param rawBreedingMethod
	 * @param crossingDate
	 * @param seedsHarvested
	 * @param notes
	 * @param entryId
	 */
	public ImportedCrosses(ListDataProject femaleListData, ListDataProject maleListData,
			String maleStudyName, String rawBreedingMethod, String crossingDate,
			String seedsHarvested, String notes, int entryId) {
		this.setFemaleDesig(femaleListData.getDesignation());
		this.setMaleDesig(maleListData.getDesignation());
		this.setFemaleGid(femaleListData.getGermplasmId().toString());
		this.setMaleGid(maleListData.getGermplasmId().toString());

		this.setEntryId(entryId);
		//DESIGNATION: "female designation / male designation"
		this.setDesig(femaleListData.getDesignation() + " / " + maleListData.getDesignation());
		// <Source Female Nursery>: "<Source Female Entry ID> / <Male Nursery>:<Source Male Entry ID>"
		this.setSource(femaleListData.getEntryId() + " / " + maleStudyName + ":" + maleListData
				.getEntryId());

		this.setEntryCode(String.valueOf(entryId));

		// the optionals
		this.rawBreedingMethod = rawBreedingMethod;
		this.crossingDate = crossingDate;
		this.seedsHarvested = seedsHarvested;
		this.notes = notes;

	}

	public ImportedCrosses(Integer entryId, String desig, String gid, String cross
			, String source, String entryCode, String check, Integer breedingMethodId) {

		this(entryId, desig, gid, cross, source, entryCode, check);
		this.breedingMethodId = breedingMethodId;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
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
	public Integer getEntryId() {
		return entryId;
	}

	/**
	 * Sets the entry id.
	 *
	 * @param entryId the new entry id
	 */
	public void setEntryId(Integer entryId) {
		this.entryId = entryId;
	}

	/**
	 * Gets the desig.
	 *
	 * @return the desig
	 */
	public String getDesig() {
		return desig;
	}

	/**
	 * Sets the desig.
	 *
	 * @param desig the new desig
	 */
	public void setDesig(String desig) {
		this.desig = desig;
	}

	/**
	 * Gets the check.
	 *
	 * @return the check
	 */
	public String getCheck() {
		return check;
	}

	/**
	 * Sets the check.
	 *
	 * @param check the new check
	 */
	public void setCheck(String check) {
		this.check = check;
	}

	/**
	 * @return the breedingMethodId
	 */
	public Integer getBreedingMethodId() {
		return breedingMethodId;
	}

	/**
	 * @param breedingMethodId the breedingMethodId to set
	 */
	public void setBreedingMethodId(Integer breedingMethodId) {
		this.breedingMethodId = breedingMethodId;
	}

	/**
	 * @return the gpid1
	 */
	public Integer getGpid1() {
		return gpid1;
	}

	/**
	 * @param gpid1 the gpid1 to set
	 */
	public void setGpid1(Integer gpid1) {
		this.gpid1 = gpid1;
	}

	/**
	 * @return the gpid2
	 */
	public Integer getGpid2() {
		return gpid2;
	}

	/**
	 * @param gpid2 the gpid2 to set
	 */
	public void setGpid2(Integer gpid2) {
		this.gpid2 = gpid2;
	}

	/**
	 * @return the gnpgs
	 */
	public Integer getGnpgs() {
		return gnpgs;
	}

	/**
	 * @param gnpgs the gnpgs to set
	 */
	public void setGnpgs(Integer gnpgs) {
		this.gnpgs = gnpgs;
	}

	/**
	 * @return the names
	 */
	public List<Name> getNames() {
		return names;
	}

	/**
	 * @param names the names to set
	 */
	public void setNames(List<Name> names) {
		this.names = names;
	}

	/**
	 * @return the checkId
	 */
	public Integer getCheckId() {
		return checkId;
	}

	/**
	 * @param checkId the checkId to set
	 */
	public void setCheckId(Integer checkId) {
		this.checkId = checkId;
	}

	/**
	 * @return the checkName
	 */
	public String getCheckName() {
		return checkName;
	}

	/**
	 * @param checkName the checkName to set
	 */
	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
     */
	@Override
	public String toString() {
		return "ImportedCrosses [entryId=" + entryId + ", desig=" + desig
				+ ", maleDesig=" + maleDesig + ", femaleDesig=" + femaleDesig
				+ ", maleGid=" + maleGid + ", femaleGid=" + femaleGid
				+ ", gid=" + gid + ", cross=" + cross + ", source=" + source
				+ ", entryCode=" + entryCode + ", check=" + check
				+ ", breedingMethodId=" + breedingMethodId + ", gpid1=" + gpid1
				+ ", gpid2=" + gpid2 + ", gnpgs=" + gnpgs + ", names=" + names
				+ "]";
	}

	public ImportedCrosses copy() {
		ImportedCrosses rec = new ImportedCrosses(this.entryId,
				this.desig, this.gid, this.cross, this.source, this.entryCode,
				this.check, this.breedingMethodId);

		rec.setGpid1(this.gpid1);
		rec.setGpid2(this.gpid2);
		rec.setGnpgs(this.gnpgs);
		rec.setNames(this.names);
		rec.setCheckId(this.checkId);
		rec.setCheckName(this.checkName);
		rec.setIndex(this.index);
		rec.setFemaleDesig(femaleDesig);
		rec.setMaleDesig(femaleDesig);
		rec.setFemaleGid(femaleGid);
		rec.setMaleGid(femaleGid);

		return rec;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getFemaleDesig() {
		return femaleDesig;
	}

	public void setFemaleDesig(String femaleDesig) {
		this.femaleDesig = femaleDesig;
	}

	public String getMaleDesig() {
		return maleDesig;
	}

	public void setMaleDesig(String maleDesig) {
		this.maleDesig = maleDesig;
	}

	public String getFemaleGid() {
		return femaleGid;
	}

	public void setFemaleGid(String femaleGid) {
		this.femaleGid = femaleGid;
	}

	public String getMaleGid() {
		return maleGid;
	}

	public void setMaleGid(String maleGid) {
		this.maleGid = maleGid;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getSeedsHarvested() {
		return seedsHarvested;
	}

	public void setSeedsHarvested(String seedsHarvested) {
		this.seedsHarvested = seedsHarvested;
	}

	public String getCrossingDate() {
		return crossingDate;
	}

	public void setCrossingDate(String crossingDate) {
		this.crossingDate = crossingDate;
	}

	/**
	 * Retrieves the breeding method of this cross.
	 * Note that this breeding method is unprocessed, its possible that this might not exists in the methods table at all
	 * to process the method, retrive the method from the database and set this.breedingMethodId
	 *
	 * @return
	 */
	public String getRawBreedingMethod() {
		return rawBreedingMethod;
	}

	public void setRawBreedingMethod(String rawBreedingMethod) {
		this.rawBreedingMethod = rawBreedingMethod;
	}
}
