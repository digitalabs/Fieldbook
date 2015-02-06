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
	
	
	public LabelPrintingSetting(){
		
	}
	
	public LabelPrintingSetting(String name, String outputType,
			CSVExcelLabelPrintingSetting csvExcelSetting, PDFLabelPrintingSetting pdfSetting,
			BarcodeLabelPrintingSetting barcodeSetting) {
		super();
		this.name = name;
		this.outputType = outputType;
		this.csvExcelSetting = csvExcelSetting;
		this.pdfSetting = pdfSetting;
		this.barcodeSetting = barcodeSetting;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public String getOutputType() {
		return outputType;
	}
	
	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}
	
	@XmlElement
	public CSVExcelLabelPrintingSetting getCsvExcelSetting() {
		return csvExcelSetting;
	}
	
	public void setCsvExcelSetting(CSVExcelLabelPrintingSetting csvExcelSetting) {
		this.csvExcelSetting = csvExcelSetting;
	}
	
	@XmlElement
	public PDFLabelPrintingSetting getPdfSetting() {
		return pdfSetting;
	}
	
	public void setPdfSetting(PDFLabelPrintingSetting pdfSetting) {
		this.pdfSetting = pdfSetting;
	}
	
	@XmlElement
	public BarcodeLabelPrintingSetting getBarcodeSetting() {
		return barcodeSetting;
	}
	
	public void setBarcodeSetting(BarcodeLabelPrintingSetting barcodeSetting) {
		this.barcodeSetting = barcodeSetting;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((barcodeSetting == null) ? 0 : barcodeSetting.hashCode());
		result = prime * result + ((csvExcelSetting == null) ? 0 : csvExcelSetting.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((outputType == null) ? 0 : outputType.hashCode());
		result = prime * result + ((pdfSetting == null) ? 0 : pdfSetting.hashCode());
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

		if (getClass() != obj.getClass()) {
			return false;
		}

		LabelPrintingSetting other = (LabelPrintingSetting) obj;
		if (barcodeSetting == null) {
			if (other.barcodeSetting != null) {
				return false;
			}
		} else if (!barcodeSetting.equals(other.barcodeSetting)) {
			return false;
		}

		if (csvExcelSetting == null) {
			if (other.csvExcelSetting != null) {
				return false;
			}
		} else if (!csvExcelSetting.equals(other.csvExcelSetting)) {
			return false;
		}

		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}

		if (outputType == null) {
			if (other.outputType != null) {
				return false;
			}

		} else if (!outputType.equals(other.outputType)) {
			return false;
		}

		if (pdfSetting == null) {
			if (other.pdfSetting != null) {
				return false;
			}

		} else if (!pdfSetting.equals(other.pdfSetting)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "LabelPrintingSetting [name=" + name + ", outputType=" + outputType
				+ ", csvExcelSetting=" + csvExcelSetting + ", pdfSetting=" + pdfSetting
				+ ", barcodeSetting=" + barcodeSetting + "]";
	}
	
	
	
}
