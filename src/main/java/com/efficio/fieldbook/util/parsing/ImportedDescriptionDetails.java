package com.efficio.fieldbook.util.parsing;

import com.efficio.fieldbook.web.nursery.bean.ImportedCondition;
import com.efficio.fieldbook.web.nursery.bean.ImportedConstant;
import com.efficio.fieldbook.web.nursery.bean.ImportedFactor;
import com.efficio.fieldbook.web.nursery.bean.ImportedVariate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cyrus on 4/24/15.
 */
public class ImportedDescriptionDetails implements Serializable {

	/** The filename. */
	protected String filename;

	/** The name. */
	protected String name;

	/** The title. */
	protected String title;

	/** The type. */
	protected String type;

	/** The date. */
	protected Date date;

	/** The imported conditions. */
	protected List<ImportedCondition> importedConditions = new ArrayList<>();

	/** The imported factors. */
	protected List<ImportedFactor> importedFactors = new ArrayList<>();;

	/** The imported variates. */
	protected List<ImportedVariate> importedVariates = new ArrayList<>();;

	/** The imported constants */
	protected List<ImportedConstant> importedConstants  = new ArrayList<>();;

	public ImportedDescriptionDetails() {
	}

	public ImportedDescriptionDetails(String filename, String name, String title, String type,
			Date date) {
		this.filename = filename;
		this.name = name;
		this.title = title;
		this.type = type;
		this.date = date;
	}

	/**
	 * Retrieves the list of imported conditions
	 * @return
	 */
	public List<ImportedCondition> getImportedConditions() {
		return importedConditions;
	}

	/**
	 * Gets the imported factors.
	 *
	 * @return the imported factors
	 */
	public List<ImportedFactor> getImportedFactors() {
		return importedFactors;
	}

	/**
	 * Gets the imported variates.
	 *
	 * @return the imported variates
	 */
	public List<ImportedVariate> getImportedVariates() {
		return importedVariates;
	}

	public List<ImportedConstant> getImportedConstants() {
		return importedConstants;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Adds the imported factor.
	 *
	 * @param importedFactor the imported factor
	 */
	public void addImportedFactor(ImportedFactor importedFactor){
		this.importedFactors.add(importedFactor);
	}

	public void addImportedCondition(ImportedCondition importedCondition) {
		this.importedConditions.add(importedCondition);
	}

	public void addImportedConstant(ImportedConstant importedConstant) {
		this.importedConstants.add(importedConstant);
	}

	/**
	 * Adds the imported variate.
	 *
	 * @param importedVariate the imported variate
	 */
	public void addImportedVariate(ImportedVariate importedVariate){
		this.importedVariates.add(importedVariate);
	}

	public int sizeOfObservationHeader() {
		return this.getImportedFactors().size() + this
				.getImportedVariates().size();
	}

}
