package com.efficio.etl.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.util.Message;

import com.efficio.etl.web.bean.IndexValueDTO;
import com.efficio.etl.web.bean.RowDTO;
import com.efficio.etl.web.bean.SheetDTO;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.bean.VariableDTO;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public interface ETLService {

  /**
   * Takes in an input stream representing the Excel file to be read, and returns the temporary file name used to store it in the system
   *
   * @param in
   * @return
   */
  public String storeUserWorkbook(InputStream in) throws IOException;

  /**
   * Retrieves
   *
   * @param userSelection
   * @return
   * @throws WorkbookParserException
   */
  public Workbook retrieveCurrentWorkbook(UserSelection userSelection) throws IOException;

  public File retrieveCurrentWorkbookAsFile(UserSelection userSelection) throws IOException;

  public List<SheetDTO> retrieveSheetInformation(Workbook workbook);

  public List<RowDTO> retrieveRowInformation(Workbook workbook, int sheetIndex, int startRow, int endRow, int maxRowContentLength);

  public List<IndexValueDTO> retrieveColumnInformation(Workbook workbook, int sheetIndex, int rowIndex);

  public int calculateObservationRows(Workbook workbook, int sheetIndex, int contentRowIndex, int indexColumnIndex);

  public List<String> retrieveColumnHeaders(Workbook workbook, UserSelection userSelection, Boolean addObsUnitId);

  public int getAvailableRowsForDisplay(Workbook workbook, int selectedSheetIndex);

  public int getAvailableRowsForDisplay(Workbook workbook, UserSelection userSelection);

  public org.generationcp.middleware.domain.etl.Workbook convertToWorkbook(UserSelection userSelection);

  public Map<PhenotypicType, List<VariableDTO>> prepareInitialCategorization(List<String> headers, UserSelection selection);

  public VariableDTO retrieveStandardVariableByID(int id);

  public void mergeVariableData(VariableDTO[] variables, Workbook workbook, UserSelection userSelection);

  public List<MeasurementRow> extractExcelFileData(Workbook workbook, UserSelection userSelection,
		  org.generationcp.middleware.domain.etl.Workbook importData, boolean discardInvalidValues);

  public PhenotypicType retrievePhenotypicType(String typeName);

  public String getCVDefinitionById(int termId);

  public List<String> convertMessageList(List<Message> messages);

  public String convertMessage(Message message);

  public List<StudyDetails> retrieveExistingStudyDetails(String programUUID);

  public Map<String, List<Message>> validateProjectOntology(org.generationcp.middleware.domain.etl.Workbook importData);

  public int saveProjectOntology(org.generationcp.middleware.domain.etl.Workbook importData, String programUUID)
		  throws MiddlewareQueryException;

  public Map<String, List<Message>> validateProjectData(org.generationcp.middleware.domain.etl.Workbook importData, String programUUID);

  public int saveProjectData(org.generationcp.middleware.domain.etl.Workbook importData, String programUUID)
		  throws MiddlewareQueryException;

  public org.generationcp.middleware.domain.etl.Workbook retrieveAndSetProjectOntology(UserSelection userSelection,
		  boolean isMeansDataImport) throws MiddlewareException;

  public Map<String, List<Message>> checkForMismatchedHeaders(List<String> fileHeaders, List<MeasurementVariable> studyHeaders,
		  boolean isMeansDataImport);

  public Tool getFieldbookWebTool();

  public Workbook retrieveCurrentWorkbookWithValidation(UserSelection userSelection) throws IOException, WorkbookParserException;

  int getIndexColumnIndex(List<String> fileHeaders, List<MeasurementVariable> studyHeaders);

  public StudyDetails readStudyDetails(Sheet sheet);

  public boolean hasMeansDataset(int studyId) throws MiddlewareException;

  public boolean hasMeasurementEffectDataset(int studyId) throws MiddlewareException;

  /**
   * Checks the uploaded workbook for out of bounds data. Returns true if there are out of bounds data.
   *
   * @param userSelection
   * @return
   * @throws IOException
   */
  boolean checkOutOfBoundsData(UserSelection userSelection) throws IOException;

  /**
   * Checks whether the workbook has observation records or not
   *
   * @param userSelection
   * @param errors
   * @param workbook
   * @return
   */
  boolean isWorkbookHasObservationRecords(UserSelection userSelection, List<String> errors, Workbook workbook);

  /**
   * Checks if the number of observations in the uploaded file is over the maximum limit
   *
   * @param userSelection
   * @param errors
   * @param workbook
   * @return
   */
  boolean isObservationOverMaximumLimit(UserSelection userSelection, List<String> errors, Workbook workbook);

  /**
   * Creates a Workbook from UserSelection
   *
   * @param userSelection
   * @param isMeansDataImport
   * @return
   */
  org.generationcp.middleware.domain.etl.Workbook createWorkbookFromUserSelection(UserSelection userSelection, boolean isMeansDataImport);

  /**
   * Verify if the file header contains OBS_UNIT_ID
   * @param importData
   * @return
   */
  boolean headersContainsObsUnitId(final org.generationcp.middleware.domain.etl.Workbook importData);
  
  /**
   * Returns all available entry types at the moment in the form of a map <Name, CVTermId> i.e <C,10170>
   * @param programUUID 
   * 
   * @return map <Name, CVTermId>
   **/
  Map<String, Integer> retrieveAvailableEntryTypes(String programUUID);

}
