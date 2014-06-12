/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.web.nursery.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

/**
 * This class was copied from CIMMYT's Fieldbook Code. 
 */
public class CSVOziel {

	private static final Logger LOG = LoggerFactory.getLogger(CSVOziel.class);

	private Workbook workbook;
	private List<MeasurementRow> observations;
	private List<MeasurementVariable> headers;
	private List<MeasurementVariable> variateHeaders;
	private boolean isDataKapture;
	
    private String stringTraitToEvaluate = "GY";
    private MeasurementVariable selectedTrait;
    private List<MeasurementRow> trialObservations;

    public CSVOziel(Workbook workbook, List<MeasurementRow> observations, List<MeasurementRow> trialObservations) {
    	this(workbook, observations, trialObservations, false);
    }
    
    public CSVOziel(Workbook workbook, List<MeasurementRow> observations, List<MeasurementRow> trialObservations, boolean isDataKapture) {
    	this.workbook = workbook;
    	this.headers = workbook.getMeasurementDatasetVariables();
    	this.observations = observations; //workbook.getObservations();
    	this.variateHeaders = workbook.getVariates();
    	this.trialObservations = trialObservations;
    	this.isDataKapture = isDataKapture;
    }

    public void writeColums(CsvWriter csvOutput, int columnas) {
        for (int i = 0; i < columnas; i++) {
            String cad = null;
            try {
                csvOutput.write(cad);
            } catch (IOException ex) {
            }

        }
    }

    public void writeRows(CsvWriter csvOutput, int rows) {
        try {
            for (int j = 0; j < rows; j++) {
                writeColums(csvOutput, 129);
                csvOutput.endRecord();
            }
        } catch (IOException ex) {
        }
    }

    public void writeTraitsFromObservations(CsvWriter csvOutput) {
        try {
            int tot = 0;

            for (MeasurementVariable variate : this.variateHeaders) {
           		csvOutput.write(variate.getName());
                tot++;
            }

            if (!this.isDataKapture) {
	            writeColums(csvOutput, 104 - tot);
	            csvOutput.write("IBFB");
            }

        } catch (IOException ex) {
        }
    }

    public void writeTraitsR(CsvWriter csvOutput) {
        try {
            for (MeasurementVariable variate : this.variateHeaders) {
               String valor = variate.getName();
                if (!valor.equals(stringTraitToEvaluate)) {
                    
	                if(valor.isEmpty()){
	                    valor=".";
	                }
	                csvOutput.write(valor);
                }
            }

        } catch (IOException ex) {
        }
    }
   

    public void writeDATA(CsvWriter csvOutput) {
    	int tot = this.variateHeaders.size();

    	try {
    		
    		Map<Long, String> map = new HashMap<Long, String>();
    		for (MeasurementRow row : this.trialObservations) {
   				map.put(row.getLocationId(), WorkbookUtil.getValueByIdInRow(row.getMeasurementVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId(), row));
    		}
    		
            for (MeasurementRow row : this.observations) {
                csvOutput.write(getDisplayValue(map.get(row.getLocationId())));
                csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.REP_NO.getId(), row));
                csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.BLOCK_NO.getId(), row));
                
                String plot = WorkbookUtil.getValueByIdInRow(this.headers, TermId.PLOT_NO.getId(), row);
                if (plot == null || "".equals(plot.trim())) {
                	plot = WorkbookUtil.getValueByIdInRow(this.headers, TermId.PLOT_NNO.getId(), row);
                }
                csvOutput.write(plot);
                csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.ENTRY_NO.getId(), row));
                writeColums(csvOutput, 2);
                csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.DESIG.getId(), row));
                writeColums(csvOutput, 15);
                csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.GID.getId(), row));
                writeColums(csvOutput, 2);


                for (MeasurementVariable variate : this.variateHeaders) {
                   String valor = variate.getName();
                   if (!valor.equals(stringTraitToEvaluate)) {
                        try {
                        	if (variate.getPossibleValues() != null && !variate.getPossibleValues().isEmpty()) {
                        		csvOutput.write(ExportImportStudyUtil.getCategoricalCellValue(row.getMeasurementDataValue(valor), variate.getPossibleValues()));
                        	}
                        	else {
                        		csvOutput.write(row.getMeasurementDataValue(valor));
                        	}
                        	
                        } catch (NullPointerException ex) {
                            String cad = ".";
                            csvOutput.write(cad);
                        }
                    }

                }

                writeColums(csvOutput, 104 - tot);
                csvOutput.write("END");
                csvOutput.endRecord();
            }
        } catch (IOException ex) {
        }

    }
    public void writeDATAR(CsvWriter csvOutput) {
        try {

    		Map<Long, String> map = new HashMap<Long, String>();
    		for (MeasurementRow row : this.trialObservations) {
   				map.put(row.getLocationId(), WorkbookUtil.getValueByIdInRow(row.getMeasurementVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId(), row));
    		}
    		
            for (MeasurementRow mRow : this.observations) {
                csvOutput.write(getDisplayValue(map.get(mRow.getLocationId())));
                csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.REP_NO.getId(), mRow));
                csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.BLOCK_NO.getId(), mRow));

                csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, TermId.ENTRY_NO.getId(), mRow));
                try {
                	if (this.selectedTrait != null) {
	                	String value = WorkbookUtil.getValueByIdInRow(this.headers, this.selectedTrait.getTermId(), mRow);
	                	if (this.selectedTrait != null && selectedTrait.getPossibleValues() != null && !selectedTrait.getPossibleValues().isEmpty()) {
	                		csvOutput.write(ExportImportStudyUtil.getCategoricalCellValue(value, selectedTrait.getPossibleValues()));
	                	}
	                	else {
	                		csvOutput.write(value);
	                	}
                	}
                } catch (NullPointerException ex) {
                    String cad = ".";
                    
                    csvOutput.write(cad);
                }


                for (MeasurementVariable variate : this.variateHeaders) {
                    String valor = variate.getName();

                     if (!valor.equals(stringTraitToEvaluate)) {
                        try {
                        	if (variate.getPossibleValues() != null && !variate.getPossibleValues().isEmpty()) {
                        		csvOutput.write(ExportImportStudyUtil.getCategoricalCellValue(mRow.getMeasurementDataValue(variate.getName()), variate.getPossibleValues()));
                        	}
                        	else {
                        		csvOutput.write(mRow.getMeasurementDataValue(variate.getName()));
                        	}
                        } catch (NullPointerException ex) {
                            String cad = ".";
                            csvOutput.write(cad);
                        }
                    }

                }

                csvOutput.endRecord();
            }
        } catch (IOException ex) {
        }

    }

    public void readDATAnew(File file) {

        List<String> titulos = new ArrayList<String>();
        int add = 0;
        String before = "";
        String actual = "";

        try {
            CsvReader csvReader = new CsvReader(file.toString());
            csvReader.readHeaders();
            String[] headers = csvReader.getHeaders();

            for (int i = 26; i < headers.length - 1; i++) {
                String titulo = headers[i];
                if (!titulo.equals("")) {
                    titulos.add(titulo);
                }
            }

            for (int i = 0; i < 23; i++) {
                csvReader.skipRecord();
            }

            int myrow = 0;
            while (csvReader.readRecord()) {

                String dataOfTraits = "";
                before = actual;
                String trial = csvReader.get("Trial");
//                String rep = csvReader.get("Rep");
//                String block = csvReader.get("Block");
                String plot = csvReader.get("Plot");
                String entry = csvReader.get("Entry");
//                String ped = csvReader.get("BreedersPedigree1");
//                String gid = csvReader.get("GID");

                actual = entry;

                if (before.equals(entry)) {
                    add++;
                } else {
                    add = 0;
                }


                try {
                	int trialNumber = 1;
                	if (trial != null && NumberUtils.isNumber(trial)) {
                		if (trial.indexOf(".") > -1) {
                			trialNumber = Integer.parseInt(trial.substring(0, trial.indexOf(".")));
                		} else {
                			trialNumber = Integer.parseInt(trial);
                		}
                	}
                    myrow = findRow(trialNumber, Integer.parseInt(plot));
                } catch (NumberFormatException ex) {
                    return;
                }

                for (int i = 0; i < titulos.size(); i++) {
                    String head = titulos.get(i).toString();
                    int col = buscaCol(head);
                    if (col >= 0) {
                        String data = csvReader.get(head);
                        setObservationData(head, myrow + add, data);
                        dataOfTraits = dataOfTraits + " " + data;
                    } else {
                        col = buscaCol(head);
                        String data = csvReader.get(head);
                        setObservationData(head, myrow + add, data);
                        dataOfTraits = dataOfTraits + " " + data;
                    }
                }
            }
            csvReader.close();
        } catch (FileNotFoundException ex) {

        } catch (IOException e) {
        }
    }

    private int findRow(int trial, int plot) {
        int row = 0;

        String plotLabel = getLabel(TermId.PLOT_NO.getId());
        if (plotLabel == null) {
        	plotLabel = getLabel(TermId.PLOT_NNO.getId());
        }

        if (this.observations != null) {
        	boolean match = false;
	        List<MeasurementVariable> variables = this.observations.get(0).getMeasurementVariables();
	        for (MeasurementRow mRow : this.observations) {
	        	String plotValueStr = mRow.getMeasurementDataValue(plotLabel);
	        	String trialValueStr = WorkbookUtil.getValueByIdInRow(variables, TermId.TRIAL_INSTANCE_FACTOR.getId(), mRow);
	        	if (plotValueStr != null && NumberUtils.isNumber(plotValueStr)) {
	        		int plotValue = Integer.valueOf(plotValueStr);
	        		if (plotValue == plot) {
	        			//return row;
	        			match = true;
	        		}
	        	}
	        	if (match) {
	        		if (trialValueStr != null && NumberUtils.isNumber(trialValueStr)) {
		        		int trialValue = Integer.valueOf(trialValueStr);
		        		if (trialValue == trial) {
		        			return row;
		        		}
		        		else {
		        			match = false;
		        		}
	        		} else {
	        			return row;
	        		}
	        	}
	        	row++;
	        }
        }

        return row;
    }

    public boolean isValid(File file) {
        boolean isvalid = false;
        try {
            CsvReader csvReader = new CsvReader(file.toString());
            csvReader.readHeaders();
            String[] headers = csvReader.getHeaders();

            if (headers[headers.length - 1].equals("IBFB")) {
                isvalid = true;
            } else {
                isvalid = false;
            }
        } catch (IOException ex) {
        }
        return isvalid;
    }

    private int buscaCol(String head) {
        int col = -1;
        
        int index = 0;
        for (MeasurementVariable mVar : this.headers) {
        	if (mVar.getName().equalsIgnoreCase(head)) {
        		return index;
        	}
        	index++;
        }
        
        return col;
    }
    
    public void defineTraitToEvaluate(String stringTraitToEval) {
        this.stringTraitToEvaluate = stringTraitToEval;
    }

    private void setObservationData(String label, int rowIndex, String value) {
    	if (rowIndex < this.observations.size()) {
	    	MeasurementRow row = this.observations.get(rowIndex);
	    	for (MeasurementData data : row.getDataList()) {
	    		if (data.getLabel().equals(label)) {
	    			if (data.getMeasurementVariable().getPossibleValues() != null && !data.getMeasurementVariable().getPossibleValues().isEmpty()) {
	    				data.setValue(ExportImportStudyUtil.getCategoricalIdCellValue(value, data.getMeasurementVariable().getPossibleValues()));
	    				if (data != null && data.getValue() != null && "".equals(data.getValue())) {
	    					data.setcValueId(null);
	    					data.setValue(null);
	    				}
	    				else if (data.getMeasurementVariable().getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
	    					data.setcValueId(data.getValue());
	    					data.setValue(data.getValue());
	    				}
	    			}
	    			else {
			    		if (!"N".equalsIgnoreCase(data.getDataType())
			    				|| ("N".equalsIgnoreCase(data.getDataType()) && value != null && NumberUtils.isNumber(value))) {
	
			    			data.setValue(value);
			    		}
			    		else {
			    			data.setValue(null);
			    		}
	    			}
	    			break;
	    		}
	    	}
    	}
    }

    private String getLabel(int termId) {
        for (MeasurementVariable mVar : this.headers) {
        	if (mVar.getTermId() == termId) {
        		return mVar.getName();
        	}
        }
        return null;
    }
    
    public void setSelectedTrait(MeasurementVariable selectedTrait) {
    	this.selectedTrait = selectedTrait;
    }

    private String getDisplayValue(String value) {
    	return value != null ? value : "";
    }
    
	public String getStringTraitToEvaluate() {
		return stringTraitToEvaluate;
	}

	//Start copied from CSVFileManager (old Fb)
    public void writeDataDataKapture(CsvWriter csvOutput) {
        Map<Long, String> map = new HashMap<Long, String>();
		for (MeasurementRow row : this.trialObservations) {
			map.put(row.getLocationId(), WorkbookUtil.getValueByIdInRow(row.getMeasurementVariables(), TermId.TRIAL_INSTANCE_FACTOR.getId(), row));
		}

		/**
         * Type
         */
        String strStudyType = workbook.getStudyDetails().getStudyType().toString();
        String trialNumber = "";
        String strLocationName = "";
        String strCycle = "";

        /**
         * TrialNumber, Location Name, Cycle
         */
        for (MeasurementVariable condition : workbook.getStudyConditions()) {
            if ("TID".equalsIgnoreCase(condition.getName())) {
                trialNumber = condition.getValue();
            }
            else if ("LID".equalsIgnoreCase(condition.getName())) {
                strLocationName = condition.getValue();
            }
            else if ("Cycle".equalsIgnoreCase(condition.getName())) {
                strCycle = condition.getValue();
            }
        }
        
        try {
            for (MeasurementRow row : observations) {

                /**
                 * Site
                 */
                csvOutput.write(strLocationName);
                /**
                 * Type
                 */
                csvOutput.write(strStudyType);
                /**
                 * Year (cycle)
                 */
                csvOutput.write(strCycle);
                /**
                 * TrialNumber
                 */
                if (trialNumber == null || "".equals(trialNumber)) {
                    csvOutput.write(map.get(row.getLocationId()));
                } else {
                	csvOutput.write(trialNumber);
                }

                /*
                 * El row y column es una manera de dividir el campo como un
                 * plano cartesiano. Esa manera solo aplica en experimentos de
                 * otros paises. Aqui no se maneja de la misma forma. Mientras
                 * no haya forma de realizarlo se imprime una constante.
                 */
                csvOutput.write("1");             //row
                csvOutput.write("1");             //column 

                /**
                 * plotBarCode
                 */
                String plot = WorkbookUtil.getValueByIdInRow(this.headers, TermId.PLOT_NO.getId(), row);
                if (plot == null || "".equals(plot.trim())) {
                	plot = WorkbookUtil.getValueByIdInRow(this.headers, TermId.PLOT_NNO.getId(), row);
                }
                csvOutput.write(plot);

                /**
                 * GID
                 */
                String gid = WorkbookUtil.getValueByIdInRow(this.headers, TermId.GID.getId(), row);
                csvOutput.write(gid);

                /**
                 * Genotype
                 */
                //es el Nombre, no necesariament se encuentra en las entradas del germoplasma
                csvOutput.write("");

                /**
                 * Pedigree
                 */
                String desig = WorkbookUtil.getValueByIdInRow(this.headers, TermId.DESIG.getId(), row);
                csvOutput.write(desig);

                /**
                 * Rep
                 */
                //Actualmente se guarda la ocurrencia, pero no necesariamente debe ser asi.
                //se acordo con Celso dejar ahi la occurencia.
                String rep = WorkbookUtil.getValueByIdInRow(this.headers, TermId.REP_NO.getId(), row);
                csvOutput.write(rep);

                for (int z = 0; z < this.variateHeaders.size(); z++) {
                    csvOutput.write("");
                }

                //csvOutput.write("END");
                csvOutput.endRecord();
            }
        } catch (IOException ex) {
            LOG.error("ERROR AL GENERAR DATA CSV " + ex, ex);
        }
    }

    public void writeTraitsDataKapture(CsvWriter csvOutput) {

        try {

            for (MeasurementVariable variate : this.variateHeaders) {
                String strTraitName = "";
                String strTrailValRule = "";
                String strDataType = "";

                strDataType = variate.getDataTypeDisplay();
                strTraitName = variate.getName();

                /**
                 * Trait Name
                 */
                csvOutput.write(strTraitName);

                /**
                 * Trait Value Rule
                 */
                if (variate.getMinRange() != null && variate.getMaxRange() != null) {
                	strTrailValRule = variate.getMinRange().toString() + ".." + variate.getMaxRange().toString();
                }
                csvOutput.write(strTrailValRule);

                /**
                 * Data Type
                 */
                csvOutput.write(strDataType);

                /**
                 * Auto Progress Field Length
                 */
                csvOutput.write("1");
                /**
                 * Is Days Trait
                 */
                csvOutput.write("1");
                /**
                 * DateStamp
                 */
                csvOutput.write("1");
                /**
                 * Trait Units
                 */
                csvOutput.write("1");
                /**
                 * Connection
                 */
                csvOutput.write("0");

                csvOutput.endRecord();
            }
        } catch (IOException ex) {
            LOG.error("Error al generar el archivo csv: " + ex, ex);
        }
    }

    public void readDATACapture(File file) {

        int variateCol = 0;
        HashMap<String, Integer> traitsMap = new HashMap<String, Integer>();
//        for (MeasurementVariable variate : this.variateHeaders) {
//            variateCol = modelo.getHeaderIndex(Workbook.getStringWithOutBlanks(variate.getProperty()+variate.getScale()));
//            traitsMap.put(variate.getName(), variateCol);
//        }
//        int add = 0;

        try {
            CsvReader csvReader = new CsvReader(file.toString());
            csvReader.readHeaders();
            String[] headers = csvReader.getHeaders();

//            int myrow = 0;
            while (csvReader.readRecord()) {

            	for (MeasurementVariable variate : this.variateHeaders) {
            		String csvTrial = csvReader.get("TrialNumber");
            		String csvPlot = csvReader.get("PlotBarCode");
            		int trial = 1;
            		if (csvTrial != null && NumberUtils.isNumber(csvTrial)) {
            			trial = Integer.parseInt(csvTrial); 
            		}
            		int plot = 1;
            		if (csvPlot != null && NumberUtils.isNumber(csvPlot)) {
            			plot = Integer.parseInt(csvPlot);
            		}
            		int rowNum = findRow(trial, plot);
            		if (rowNum > -1) {
	            		String value = csvReader.get(variate.getName());
	            		setObservationData(variate.getName(), rowNum, value);
            		}
            	}
            	
//            	for (MeasurementVariable variate : this.variateHeaders) {
//                    String head = variate.getName();
//                    int col = traitsMap.get(head);
//                    if (col >= 0) {
//                        String data = csvReader.get(head);
//                        modelo.setValueAt(data, myrow + add, col);
//                    }
//                }

//                myrow++;
            }
            csvReader.close();
            
        } catch (FileNotFoundException ex) {
            LOG.error("FILE NOT FOUND. readDATAcsv. " + ex);

        } catch (IOException e) {
            LOG.error("IO EXCEPTION. readDATAcsv. " + e);
        }
    }
    //end copied from CSVFileManager (old Fb)

    
    
    //These methods were not used YET, temporarily commented out while not in use.
    //TODO cleanup once we have confirmed that these methods will no longer 
/*
    public void writeTraits(CsvWriter csvOutput) {
        try {
            listModel = (DefaultListModel) lista.getModel();
            int tot = listModel.size();

            for (int i = 0; i < tot; i++) {
                String cadena = listModel.getElementAt(i).toString();
                int espacio = cadena.indexOf("(");
                String valor = cadena.substring(0, espacio - 1).trim();
                csvOutput.write(valor);
            }


            writeColums(csvOutput, 104 - tot);
            csvOutput.write("IBFB");


        } catch (IOException ex) {
            System.out.println("ERROR AL GENERAR TRAITS CSV " + ex);
        }
    }

    public void writeTraitsR(CsvWriter csvOutput) {
        try {

            listModel = (DefaultListModel) lista.getModel();
            int tot = listModel.size();

            for (int i = 0; i < tot; i++) {
                String cadena = listModel.getElementAt(i).toString();
                int espacio = cadena.indexOf("(");
                String valor = cadena.substring(0, espacio - 1).trim();
                if (!valor.equals(stringTraitToEvaluate)) {
                    if(valor.isEmpty()){
                    valor=".";
                }
                    csvOutput.write(valor);
                }
            }

        } catch (IOException ex) {
            System.out.println("ERROR AL GENERAR TRAITS CSV " + ex);
        }
    }

    public void writeDATA(CsvWriter csvOutput) {
        //int total = observations.size();
        //int tot = listModel.size();
        try {
            //for (int i = 0; i < total; i++) {
        	for (MeasurementRow row : this.observations) {

                csvOutput.write(row.getMeasurementDataValue(label));
                csvOutput.write(modeloFiltro.getValueAt(i, modeloFiltro.findColumn("REP")).toString());
                csvOutput.write(modeloFiltro.getValueAt(i, modeloFiltro.findColumn("BLOCK")).toString());
                csvOutput.write(modeloFiltro.getValueAt(i, modeloFiltro.findColumn("PLOT")).toString());
                csvOutput.write(modeloFiltro.getValueAt(i, modeloFiltro.findColumn("ENTRY")).toString());
                writeColums(csvOutput, 2);
                csvOutput.write(modeloFiltro.getValueAt(i, modeloFiltro.findColumn("DESIG")).toString());
                writeColums(csvOutput, 15);
                csvOutput.write(modeloFiltro.getValueAt(i, modeloFiltro.findColumn("GID")).toString());
                writeColums(csvOutput, 2);

                for (int j = 0; j < tot; j++) {
                    String cadena = listModel.getElementAt(j).toString();
                    int espacio = cadena.indexOf("(");
                    String valor = cadena.substring(0, espacio - 1).trim();


                    try {
                        csvOutput.write(modeloFiltro.getValueAt(i, modeloFiltro.findColumn(valor)).toString());
                    } catch (NullPointerException ex) {
                        String cad = null;
                        csvOutput.write(cad);
                    }
                }

                writeColums(csvOutput, 104 - tot);
                csvOutput.write("END");
                csvOutput.endRecord();
            }
        } catch (IOException ex) {
            System.out.println("ERROR AL GENERAR DATA CSV " + ex);
        }

    }

    public void writeDATAR(CsvWriter csvOutput, DefaultTableModel modeloFilter) {

        int total = modeloFilter.getRowCount();
        int tot = listModel.size();

        try {


            for (int i = 0; i < total; i++) {

                csvOutput.write(modeloFilter.getValueAt(i, modeloFilter.findColumn("NURSERY")).toString());
                csvOutput.write(modeloFilter.getValueAt(i, modeloFilter.findColumn("REP")).toString());
                csvOutput.write(modeloFilter.getValueAt(i, modeloFilter.findColumn("BLOCK")).toString());
                csvOutput.write(modeloFilter.getValueAt(i, modeloFilter.findColumn("ENTRY")).toString());
                try {
                    csvOutput.write(modeloFilter.getValueAt(i, modeloFilter.findColumn(stringTraitToEvaluate)).toString());
                } catch (NullPointerException ex) {
                    String cad = null;
                    csvOutput.write(cad);
                }


                for (int j = 0; j < tot; j++) {
                    String cadena = listModel.getElementAt(j).toString();
                    int espacio = cadena.indexOf("(");
                    String valor = cadena.substring(0, espacio - 1).trim();

                    if (!valor.equals(stringTraitToEvaluate)) {
                        try {
                            csvOutput.write(modeloFilter.getValueAt(i, modeloFilter.findColumn(valor)).toString());
                        } catch (NullPointerException ex) {
                            String cad = null;
                            csvOutput.write(cad);
                        }
                    }

                }

                csvOutput.endRecord();
            }
        } catch (IOException ex) {
            System.out.println("ERROR AL GENERAR DATA CSV FOR R" + ex);
        }

    }
    @SuppressWarnings("unchecked")
    public void readDATA(File file) {

        ArrayList titulos = new ArrayList();
        DefaultTableModel modelo = (DefaultTableModel) jTableObservations.getModel();
        //ObservationsTableModel modelo =  (ObservationsTableModel)jTableObservations.getModel();
        System.out.println("TENEMOS: " + dameTotalDatos(file));

        try {
            CsvReader csvReader = new CsvReader(file.toString());
            csvReader.readHeaders();
            String[] headers = csvReader.getHeaders();

            if (headers[headers.length - 1].equals("IBFB")) {
                System.out.println("ES DEL IBFB");
            } else {
                System.out.println("NO ES DEL IBFB");
            }

            for (int i = 26; i < headers.length - 1; i++) {
                String titulo = headers[i];
                if (!titulo.equals("")) {
                    System.out.println(titulo);
                    titulos.add(titulo);
                }
            }

            for (int i = 0; i < 23; i++) {
                csvReader.skipRecord();

            }

            System.out.println("TENEMOS traits: " + titulos.size());


            int myrow = 0;
            while (csvReader.readRecord()) {
                String dataOfTraits = "";

                String trial = csvReader.get("Trial");
                String rep = csvReader.get("Rep");
                String block = csvReader.get("Block");
                String plot = csvReader.get("Plot");
                String entry = csvReader.get("Entry");
                String ped = csvReader.get("BreedersPedigree1");
                String gid = csvReader.get("GID");


                for (int i = 0; i < titulos.size(); i++) {

                    String head = titulos.get(i).toString();

                    int col = buscaCol(head);

                    if (col >= 0) {
                        String data = csvReader.get(head);
                        modelo.setValueAt(data, myrow, col);

                        dataOfTraits = dataOfTraits + " " + data;
                    } else {
                        modelo.addColumn(head);

                        col = buscaCol(head);
                        String data = csvReader.get(head);
                        modelo.setValueAt(data, myrow, col);

                        dataOfTraits = dataOfTraits + " " + data;

                    }


                }

                myrow++;

                System.out.println(trial + " " + rep + " " + block + " " + plot + " " + entry + " " + ped + " " + gid + dataOfTraits);
            }

            csvReader.close();

        } catch (FileNotFoundException ex) {
            System.out.println("FILE NOT FOUND. readDATAcsv. " + ex);

        } catch (IOException e) {
            System.out.println("IO EXCEPTION. readDATAcsv. " + e);
        }
    }
    public int dameTotalDatos(File file) {
        int total = 0;
        try {
            CsvReader csvReader = new CsvReader(file.toString());

            for (int i = 0; i < 24; i++) {
                csvReader.skipRecord();
            }

            while (csvReader.readRecord()) {
                total++;
            }
        } catch (IOException ex) {
            System.out.println("ERROR EN CONTAR REGISTROS CSV READER");
            total = 0;
        }

        return total;
    }

    public int dameTotalColumnas(File file) {
        int total = 0;
        try {
            CsvReader csvReader = new CsvReader(file.toString());

            total = csvReader.getHeaderCount();


        } catch (IOException ex) {
            System.out.println("ERROR EN CONTAR REGISTROS CSV READER");
            total = 0;
        }

        return total;
    }
*/
}
