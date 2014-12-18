package com.efficio.fieldbook.web.label.printing.bean;

public class LabelPrintingPresets {
	public static final int STANDARD_PRESET = 0;
	public static final int PROGRAM_PRESET = 1;

	private int id;
	private String name;
	private int type;
	
	public LabelPrintingPresets(){
		super();
	}
	
	public LabelPrintingPresets(int id, String name, int type) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	
}
