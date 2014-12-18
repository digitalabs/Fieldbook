package com.efficio.fieldbook.web.label.printing.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.math.NumberUtils;

public class CSVExcelLabelPrintingSetting implements Serializable {

	private static final long serialVersionUID = -6130830859384859857L;
	public static final String DELIMITER = ",";
			
	private boolean isIncludeColumnHeadingsInOutput;
	private String selectedFieldsString;
	
	public CSVExcelLabelPrintingSetting(){
		
	}
	
	public CSVExcelLabelPrintingSetting(boolean isIncludeColumnHeadingsInOutput,
			String selectedFieldsString) {
		super();
		this.isIncludeColumnHeadingsInOutput = isIncludeColumnHeadingsInOutput;
		this.selectedFieldsString = selectedFieldsString;
	}

	@XmlAttribute
	public boolean isIncludeColumnHeadingsInOutput() {
		return isIncludeColumnHeadingsInOutput;
	}

	public void setIncludeColumnHeadingsInOutput(boolean isIncludeColumnHeadingsInOutput) {
		this.isIncludeColumnHeadingsInOutput = isIncludeColumnHeadingsInOutput;
	}

	public List<Integer> getSelectedFieldsList() {
		List<Integer> selectedFieldsList = new ArrayList<Integer>();
		if (!selectedFieldsString.isEmpty()){
			for (String traitId : selectedFieldsString.split(DELIMITER)){
				if (NumberUtils.isDigits(traitId)){
					selectedFieldsList.add(Integer.parseInt(traitId));
				}
			}
		}
		return selectedFieldsList;
	}


	@XmlAttribute
	public String getSelectedFieldsString() {
		return selectedFieldsString;
	}

	public void setSelectedFieldsString(String selectedFieldsString) {
		this.selectedFieldsString = selectedFieldsString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isIncludeColumnHeadingsInOutput ? 1231 : 1237);
		result = prime * result
				+ ((selectedFieldsString == null) ? 0 : selectedFieldsString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CSVExcelLabelPrintingSetting other = (CSVExcelLabelPrintingSetting) obj;
		if (isIncludeColumnHeadingsInOutput != other.isIncludeColumnHeadingsInOutput)
			return false;
		if (selectedFieldsString == null) {
			if (other.selectedFieldsString != null)
				return false;
		} else if (!selectedFieldsString.equals(other.selectedFieldsString))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CSVExcelLabelPrintingSetting [isIncludeColumnHeadingsInOutput="
				+ isIncludeColumnHeadingsInOutput + ", selectedFieldsString="
				+ selectedFieldsString + "]";
	}
	
	

}
