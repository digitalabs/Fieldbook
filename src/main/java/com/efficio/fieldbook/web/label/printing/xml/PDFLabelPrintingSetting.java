package com.efficio.fieldbook.web.label.printing.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.math.NumberUtils;

public class PDFLabelPrintingSetting implements Serializable {

	private static final long serialVersionUID = -5011706202570105720L;
	public static final String DELIMITER = ",";
	
	private String sizeOfLabelSheet;
	private int numberOfRowsPerPage;
	private String selectedLeftFieldsString;
	private String selectedRightFieldsString;
	
	public PDFLabelPrintingSetting(){
		
	}
	

	public PDFLabelPrintingSetting(String sizeOfLabelSheet, int numberOfRowsPerPage,
			String selectedLeftFieldsString, String selectedRightFieldsString) {
		super();
		this.sizeOfLabelSheet = sizeOfLabelSheet;
		this.numberOfRowsPerPage = numberOfRowsPerPage;
		this.selectedLeftFieldsString = selectedLeftFieldsString;
		this.selectedRightFieldsString = selectedRightFieldsString;
	}


	@XmlAttribute
	public String getSizeOfLabelSheet() {
		return sizeOfLabelSheet;
	}

	public void setSizeOfLabelSheet(String sizeOfLabelSheet) {
		this.sizeOfLabelSheet = sizeOfLabelSheet;
	}

	@XmlAttribute
	public int getNumberOfRowsPerPage() {
		return numberOfRowsPerPage;
	}

	public void setNumberOfRowsPerPage(int numberOfRowsPerPage) {
		this.numberOfRowsPerPage = numberOfRowsPerPage;
	}

	@XmlAttribute
	public String getSelectedLeftFieldsString() {
		return selectedLeftFieldsString;
	}

	public void setSelectedLeftFieldsString(String selectedLeftFieldsString) {
		this.selectedLeftFieldsString = selectedLeftFieldsString;
	}

	@XmlAttribute
	public String getSelectedRightFieldsString() {
		return selectedRightFieldsString;
	}

	public void setSelectedRightFieldsString(String selectedRightFieldsString) {
		this.selectedRightFieldsString = selectedRightFieldsString;
	}
	
	public List<Integer> getSelectedLeftFieldsList() {
		List<Integer> selectedLeftFieldsList = new ArrayList<Integer>();
		if (!selectedLeftFieldsString.isEmpty()){
			for (String traitId : selectedLeftFieldsString.split(DELIMITER)){
				if (NumberUtils.isNumber(traitId)){
					selectedLeftFieldsList.add(Integer.parseInt(traitId));
				}
			}
		}
		return selectedLeftFieldsList;
	}
	
	public List<Integer> getSelectedRightFieldsList() {
		List<Integer> selectedRightFieldsList = new ArrayList<Integer>();
		if (!selectedRightFieldsString.isEmpty()){
			for (String traitId : selectedRightFieldsString.split(DELIMITER)){
				if (NumberUtils.isNumber(traitId)){
					selectedRightFieldsList.add(Integer.parseInt(traitId));
				}
			}
		}
		return selectedRightFieldsList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numberOfRowsPerPage;
		result = prime * result
				+ ((selectedLeftFieldsString == null) ? 0 : selectedLeftFieldsString.hashCode());
		result = prime * result
				+ ((selectedRightFieldsString == null) ? 0 : selectedRightFieldsString.hashCode());
		result = prime * result + ((sizeOfLabelSheet == null) ? 0 : sizeOfLabelSheet.hashCode());
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
		PDFLabelPrintingSetting other = (PDFLabelPrintingSetting) obj;
		if (numberOfRowsPerPage != other.numberOfRowsPerPage)
			return false;
		if (selectedLeftFieldsString == null) {
			if (other.selectedLeftFieldsString != null)
				return false;
		} else if (!selectedLeftFieldsString.equals(other.selectedLeftFieldsString))
			return false;
		if (selectedRightFieldsString == null) {
			if (other.selectedRightFieldsString != null)
				return false;
		} else if (!selectedRightFieldsString.equals(other.selectedRightFieldsString))
			return false;
		if (sizeOfLabelSheet == null) {
			if (other.sizeOfLabelSheet != null)
				return false;
		} else if (!sizeOfLabelSheet.equals(other.sizeOfLabelSheet))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PDFLabelPrintingSetting [sizeOfLabelSheet=" + sizeOfLabelSheet
				+ ", numberOfRowsPerPage=" + numberOfRowsPerPage + ", selectedLeftFieldsString="
				+ selectedLeftFieldsString + ", selectedRightFieldsString="
				+ selectedRightFieldsString + "]";
	}
	
	

}
