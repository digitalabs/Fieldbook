package com.efficio.fieldbook.service;


//import com.efficio.etl.web.bean.IndexValueDTO;
//import com.efficio.etl.web.bean.SheetDTO;
//import com.efficio.etl.web.bean.UserSelection;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ETLServiceTest {

    @Resource
    private Workbook workbook;

    //@Resource
    //private ETLService etlService;

    //private UserSelection userSelection;

    private final static int ALL_OBSERVATION_ROWS = 447;
    private final static int GID_COLUMN = 1;
    private final static int OBSERVATION_HEADER_ROW = 0;
    private final static int OBSERVATION_CONTENT_ROW = 1;
    private final static int OBSERVATION_SHEET_INDEX = 1;

    private final static int COLUMN_WITH_BLANK_CELL = 12;
    private final static int CELL_COUNT_BEFORE_BLANK = 36;

    private final static int DESCRIPTION_SHEET_INDEX = 0;
    private final static int DESCRIPTION_HEADER_ROW = 8;
    private final static int DESCRIPTION_CONTENT_ROW = 9;


    private final static String[] COLUMN_HEADERS = new String[]{"ENTRY", "GID", "DESIG", "CROSS", "SOURCE", "PLOT", "BLOCK", "REP", "ROW", "COL",
            "NBEPm2", "GYLD", "equi-Kkni", "equi-Tiand", "DTFL", "DFLF", "FDect", "GDENS", "TGW", "PERTH", "PH1", "PH2", "INTNN1", "INTNN2", "PEDL1",
            "PEDL2", "PANL1", "PANL2", "NHH", "NBGPAN", "PH", "INTNN", "PEDL", "PANL"};

    private final static String SHEET_1_NAME = "Description";
    private final static String SHEET_2_NAME = "Observation";


    @Before
    public void setUp() {
	/*
        userSelection = new UserSelection();
        userSelection.setHeaderRowIndex(OBSERVATION_HEADER_ROW);
        userSelection.setContentRowIndex(OBSERVATION_CONTENT_ROW);
        userSelection.setSelectedSheet(OBSERVATION_SHEET_INDEX);
		*/
    }
	/*
    @Test
    public void testRetrieveSheetInformationPositive() {
        List<SheetDTO> sheetDTOs = etlService.retrieveSheetInformation(workbook);

        assertEquals(2, sheetDTOs.size());

        assertEquals(SHEET_1_NAME, sheetDTOs.get(0).getSheetName());
        assertEquals(SHEET_2_NAME, sheetDTOs.get(1).getSheetName());
    }

    @Test
    public void testRetrieveColumnInformationPositive() {
        List<IndexValueDTO> indexValueDTOs = etlService.retrieveColumnInformation(workbook, OBSERVATION_SHEET_INDEX, OBSERVATION_HEADER_ROW);

        assertEquals(COLUMN_HEADERS.length, indexValueDTOs.size());

        for (int i = 0; i < COLUMN_HEADERS.length; i++) {
            assertEquals(COLUMN_HEADERS[i], indexValueDTOs.get(i).getValue());
        }
    }


    @Test
    public void testComputeObservationRowsAll() {
        assertEquals(etlService.calculateObservationRows(workbook,
                OBSERVATION_SHEET_INDEX, OBSERVATION_CONTENT_ROW, GID_COLUMN), ALL_OBSERVATION_ROWS);
    }

    @Test
    public void testComputeObservationRowsWithBlank() {
        assertTrue(etlService.calculateObservationRows(workbook, OBSERVATION_SHEET_INDEX, OBSERVATION_CONTENT_ROW,
                COLUMN_WITH_BLANK_CELL) < ALL_OBSERVATION_ROWS);

        assertEquals(etlService.calculateObservationRows(workbook, OBSERVATION_SHEET_INDEX, OBSERVATION_CONTENT_ROW,
                COLUMN_WITH_BLANK_CELL), CELL_COUNT_BEFORE_BLANK);
    }

    @Test
    public void testExtractColumnHeadersPositive() {

        assertArrayEquals(COLUMN_HEADERS, etlService.retrieveColumnHeaders(workbook, userSelection).toArray());

    }

    @Test
    public void testRetrieveNameSuggestions() {
        prepareUserSelectionForNameSuggestionRetrieval();

        assertTrue(etlService.calculateNameTitleSuggestions(workbook, userSelection).size() > 0);
    }

    protected void prepareUserSelectionForNameSuggestionRetrieval() {
        userSelection.setSelectedSheet(DESCRIPTION_SHEET_INDEX);
        userSelection.setContentRowIndex(DESCRIPTION_CONTENT_ROW);
        userSelection.setHeaderRowIndex(DESCRIPTION_HEADER_ROW);
        userSelection.setActualFileName("Population114_Pheno_FB_1.xls");
    }
	*/
}
