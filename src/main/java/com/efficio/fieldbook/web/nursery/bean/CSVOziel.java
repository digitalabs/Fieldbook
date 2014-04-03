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
	 
	private List<MeasurementRow> observations;
	private List<MeasurementVariable> headers;
	private List<MeasurementVariable> variateHeaders;
	
    private String stringTraitToEvaluate = "GY";
    private Integer selectedTraitId;
    private List<MeasurementRow> trialObservations;

    public CSVOziel(Workbook workbook, List<MeasurementRow> observations, List<MeasurementRow> trialObservations) {
    	this.headers = workbook.getMeasurementDatasetVariables();
    	this.observations = observations; //workbook.getObservations();
    	this.variateHeaders = workbook.getVariates();
    	this.trialObservations = trialObservations;
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

            writeColums(csvOutput, 104 - tot);
            csvOutput.write("IBFB");

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
                	csvOutput.write(WorkbookUtil.getValueByIdInRow(this.headers, this.selectedTraitId, mRow));
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
//                String trial = csvReader.get("Trial");
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
                    myrow = findRow(Integer.parseInt(plot));
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

    private int findRow(int plot) {
        int row = 0;

        String plotLabel = getLabel(TermId.PLOT_NO.getId());
        if (plotLabel == null) {
        	plotLabel = getLabel(TermId.PLOT_NNO.getId());
        }
        
        for (MeasurementRow mRow : this.observations) {
        	String plotValueStr = mRow.getMeasurementDataValue(plotLabel);
        	if (plotValueStr != null && NumberUtils.isNumber(plotValueStr)) {
        		int plotValue = Integer.valueOf(plotValueStr);
        		if (plotValue == plot) {
        			return row;
        		}
        	}
        	row++;
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
    
    public void DefineTraitToEvaluate(String stringTraitToEval) {
        this.stringTraitToEvaluate=stringTraitToEval;
    }

    private void setObservationData(String label, int rowIndex, String value) {
    	if (rowIndex < this.observations.size()) {
	    	MeasurementRow row = this.observations.get(rowIndex);
	    	for (MeasurementData data : row.getDataList()) {
	    		if (data.getLabel().equals(label)) {
	    			if (data.getMeasurementVariable().getPossibleValues() != null && !data.getMeasurementVariable().getPossibleValues().isEmpty()) {
	    				data.setValue(ExportImportStudyUtil.getCategoricalIdCellValue(value, data.getMeasurementVariable().getPossibleValues()));
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
    
    public void setSelectedTraitId(Integer selectedTraitId) {
    	this.selectedTraitId = selectedTraitId;
    }

    private String getDisplayValue(String value) {
    	return value != null ? value : "";
    }
    
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
