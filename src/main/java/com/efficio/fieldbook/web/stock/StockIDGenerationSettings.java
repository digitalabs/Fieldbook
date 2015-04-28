package com.efficio.fieldbook.web.stock;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 4/24/2015
 * Time: 4:44 PM
 */
public class StockIDGenerationSettings {
	private String breederIdentifier;
	private String separator;

	public StockIDGenerationSettings() {
	}

	public StockIDGenerationSettings(String breederIdentifier, String separator) {
		this.breederIdentifier = breederIdentifier;
		this.separator = separator;
	}

	public String getBreederIdentifier() {
		return breederIdentifier;
	}

	public String getSeparator() {
		return separator;
	}

	public void setBreederIdentifier(String breederIdentifier) {
		this.breederIdentifier = breederIdentifier;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void copy(StockIDGenerationSettings settings) {
		this.breederIdentifier = settings.getBreederIdentifier();
		this.separator = settings.getSeparator();
	}

}
