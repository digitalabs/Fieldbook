package com.efficio.fieldbook.web.label.printing.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang.math.NumberUtils;

public class BarcodeLabelPrintingSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final String DELIMITER = ",";

	private boolean isBarcodeNeeded;
	private String barcodeFormat;
	private String barcodeFieldsString;
	
	public BarcodeLabelPrintingSetting(){
		
	}
	
	public BarcodeLabelPrintingSetting(boolean isBarcodeNeeded, String barcodeFormat,
			String barcodeFieldsString) {
		super();
		this.isBarcodeNeeded = isBarcodeNeeded;
		this.barcodeFormat = barcodeFormat;
		this.setBarcodeFieldsString(barcodeFieldsString);
	}
	
	@XmlAttribute
	public boolean isBarcodeNeeded() {
		return isBarcodeNeeded;
	}
	
	public void setBarcodeNeeded(boolean isBarcodeNeeded) {
		this.isBarcodeNeeded = isBarcodeNeeded;
	}
	
	@XmlAttribute
	public String getBarcodeFormat() {
		return barcodeFormat;
	}
	public void setBarcodeFormat(String barcodeFormat) {
		this.barcodeFormat = barcodeFormat;
	}

	@XmlAttribute
	public String getBarcodeFieldsString() {
		return barcodeFieldsString;
	}

	public void setBarcodeFieldsString(String barcodeFieldsString) {
		this.barcodeFieldsString = barcodeFieldsString;
	}
	
	
	public List<Integer> getBarcodeFieldsList() {
		List<Integer> barcodeFieldsList = new ArrayList<Integer>();
		if (!barcodeFieldsString.isEmpty()){
			for (String traitId : barcodeFieldsString.split(DELIMITER)){
				if (NumberUtils.isDigits(traitId)){
					barcodeFieldsList.add(Integer.parseInt(traitId));
				}
			}
		}
		return barcodeFieldsList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((barcodeFieldsString == null) ? 0 : barcodeFieldsString.hashCode());
		result = prime * result + ((barcodeFormat == null) ? 0 : barcodeFormat.hashCode());
		result = prime * result + (isBarcodeNeeded ? 1231 : 1237);
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
		BarcodeLabelPrintingSetting other = (BarcodeLabelPrintingSetting) obj;
		if (barcodeFieldsString == null) {
			if (other.barcodeFieldsString != null)
				return false;
		} else if (!barcodeFieldsString.equals(other.barcodeFieldsString))
			return false;
		if (barcodeFormat == null) {
			if (other.barcodeFormat != null)
				return false;
		} else if (!barcodeFormat.equals(other.barcodeFormat))
			return false;
		if (isBarcodeNeeded != other.isBarcodeNeeded)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BarcodeLabelPrintingSetting [isBarcodeNeeded=" + isBarcodeNeeded
				+ ", barcodeFormat=" + barcodeFormat + ", barcodeFieldsString="
				+ barcodeFieldsString + "]";
	}
	
	

}
