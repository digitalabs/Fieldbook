
package com.efficio.fieldbook.web.label.printing.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LabelPrintingSetting implements Serializable {

	private static final long serialVersionUID = 5959321174670052845L;

	private String name;
	private String outputType;
	private CSVExcelLabelPrintingSetting csvExcelSetting;
	private PDFLabelPrintingSetting pdfSetting;
	private BarcodeLabelPrintingSetting barcodeSetting;

	public LabelPrintingSetting() {

	}

	public LabelPrintingSetting(String name, String outputType, CSVExcelLabelPrintingSetting csvExcelSetting,
			PDFLabelPrintingSetting pdfSetting, BarcodeLabelPrintingSetting barcodeSetting) {
		super();
		this.name = name;
		this.outputType = outputType;
		this.csvExcelSetting = csvExcelSetting;
		this.pdfSetting = pdfSetting;
		this.barcodeSetting = barcodeSetting;
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getOutputType() {
		return this.outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	@XmlElement
	public CSVExcelLabelPrintingSetting getCsvExcelSetting() {
		return this.csvExcelSetting;
	}

	public void setCsvExcelSetting(CSVExcelLabelPrintingSetting csvExcelSetting) {
		this.csvExcelSetting = csvExcelSetting;
	}

	@XmlElement
	public PDFLabelPrintingSetting getPdfSetting() {
		return this.pdfSetting;
	}

	public void setPdfSetting(PDFLabelPrintingSetting pdfSetting) {
		this.pdfSetting = pdfSetting;
	}

	@XmlElement
	public BarcodeLabelPrintingSetting getBarcodeSetting() {
		return this.barcodeSetting;
	}

	public void setBarcodeSetting(BarcodeLabelPrintingSetting barcodeSetting) {
		this.barcodeSetting = barcodeSetting;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.barcodeSetting == null ? 0 : this.barcodeSetting.hashCode());
		result = prime * result + (this.csvExcelSetting == null ? 0 : this.csvExcelSetting.hashCode());
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		result = prime * result + (this.outputType == null ? 0 : this.outputType.hashCode());
		result = prime * result + (this.pdfSetting == null ? 0 : this.pdfSetting.hashCode());
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

		LabelPrintingSetting other = (LabelPrintingSetting) obj;
		if (this.barcodeSetting == null) {
			if (other.barcodeSetting != null) {
				return false;
			}
		} else if (!this.barcodeSetting.equals(other.barcodeSetting)) {
			return false;
		}

		if (this.csvExcelSetting == null) {
			if (other.csvExcelSetting != null) {
				return false;
			}
		} else if (!this.csvExcelSetting.equals(other.csvExcelSetting)) {
			return false;
		}

		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}

		if (this.outputType == null) {
			if (other.outputType != null) {
				return false;
			}

		} else if (!this.outputType.equals(other.outputType)) {
			return false;
		}

		if (this.pdfSetting == null) {
			if (other.pdfSetting != null) {
				return false;
			}

		} else if (!this.pdfSetting.equals(other.pdfSetting)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "LabelPrintingSetting [name=" + this.name + ", outputType=" + this.outputType + ", csvExcelSetting=" + this.csvExcelSetting
				+ ", pdfSetting=" + this.pdfSetting + ", barcodeSetting=" + this.barcodeSetting + "]";
	}

}
