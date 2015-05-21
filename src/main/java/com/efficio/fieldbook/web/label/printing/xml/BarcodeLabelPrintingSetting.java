
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

	public BarcodeLabelPrintingSetting() {

	}

	public BarcodeLabelPrintingSetting(boolean isBarcodeNeeded, String barcodeFormat, String barcodeFieldsString) {
		super();
		this.isBarcodeNeeded = isBarcodeNeeded;
		this.barcodeFormat = barcodeFormat;
		this.setBarcodeFieldsString(barcodeFieldsString);
	}

	@XmlAttribute
	public boolean isBarcodeNeeded() {
		return this.isBarcodeNeeded;
	}

	public void setBarcodeNeeded(boolean isBarcodeNeeded) {
		this.isBarcodeNeeded = isBarcodeNeeded;
	}

	@XmlAttribute
	public String getBarcodeFormat() {
		return this.barcodeFormat;
	}

	public void setBarcodeFormat(String barcodeFormat) {
		this.barcodeFormat = barcodeFormat;
	}

	@XmlAttribute
	public String getBarcodeFieldsString() {
		return this.barcodeFieldsString;
	}

	public void setBarcodeFieldsString(String barcodeFieldsString) {
		this.barcodeFieldsString = barcodeFieldsString;
	}

	public List<Integer> getBarcodeFieldsList() {
		List<Integer> barcodeFieldsList = new ArrayList<Integer>();
		if (!this.barcodeFieldsString.isEmpty()) {
			for (String traitId : this.barcodeFieldsString.split(BarcodeLabelPrintingSetting.DELIMITER)) {
				if (NumberUtils.isDigits(traitId)) {
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
		result = prime * result + (this.barcodeFieldsString == null ? 0 : this.barcodeFieldsString.hashCode());
		result = prime * result + (this.barcodeFormat == null ? 0 : this.barcodeFormat.hashCode());
		result = prime * result + (this.isBarcodeNeeded ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		BarcodeLabelPrintingSetting other = (BarcodeLabelPrintingSetting) obj;
		if (this.barcodeFieldsString == null) {
			if (other.barcodeFieldsString != null) {
				return false;
			}
		} else if (!this.barcodeFieldsString.equals(other.barcodeFieldsString)) {
			return false;
		}
		if (this.barcodeFormat == null) {
			if (other.barcodeFormat != null) {
				return false;
			}
		} else if (!this.barcodeFormat.equals(other.barcodeFormat)) {
			return false;
		}
		if (this.isBarcodeNeeded != other.isBarcodeNeeded) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BarcodeLabelPrintingSetting [isBarcodeNeeded=" + this.isBarcodeNeeded + ", barcodeFormat=" + this.barcodeFormat + ", barcodeFieldsString="
				+ this.barcodeFieldsString + "]";
	}

}
