package com.efficio.fieldbook.web.stock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 4/24/2015
 * Time: 4:44 PM
 */
public class StockListGenerationSettings {
	public static final int VALID_SETTINGS = 1;
	public static final int NUMBERS_FOUND = -1;
	public static final int SPACE_FOUND = -2;

	private String breederIdentifier;
	private String separator;

	private boolean addPedigreeDuplicate;
	private boolean addPlotReciprocal;
	private boolean addPedigreeReciprocal;
	public StockListGenerationSettings() {
	}

	public StockListGenerationSettings(String breederIdentifier, String separator) {
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

	public void copy(StockListGenerationSettings settings) {
		this.breederIdentifier = settings.getBreederIdentifier();
		this.separator = settings.getSeparator();
	}

	public int validateSettings() {

		if (hasNumber()) {
			return NUMBERS_FOUND;
		}

		if (hasSpace()) {
			return SPACE_FOUND;
		}

		return VALID_SETTINGS;
	}

	protected boolean hasNumber() {
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher matcher = pattern.matcher(getBreederIdentifier());

		return matcher.find();
	}

	protected boolean hasSpace() {
		return getBreederIdentifier().contains(" ");
	}

	public boolean isAddPedigreeDuplicate() {
		return addPedigreeDuplicate;
	}

	public void setAddPedigreeDuplicate(boolean addPedigreeDuplicate) {
		this.addPedigreeDuplicate = addPedigreeDuplicate;
	}

	public boolean isAddPlotReciprocal() {
		return addPlotReciprocal;
	}

	public void setAddPlotReciprocal(boolean addPlotReciprocal) {
		this.addPlotReciprocal = addPlotReciprocal;
	}

	public boolean isAddPedigreeReciprocal() {
		return addPedigreeReciprocal;
	}

	public void setAddPedigreeReciprocal(boolean addPedigreeReciprocal) {
		this.addPedigreeReciprocal = addPedigreeReciprocal;
	}

	public boolean hasBulkInstructions() {
		return isAddPedigreeReciprocal() || isAddPlotReciprocal() || isAddPedigreeDuplicate();
	}

}
