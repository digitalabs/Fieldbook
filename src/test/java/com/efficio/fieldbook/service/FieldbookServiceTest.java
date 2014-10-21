package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Person;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.efficio.fieldbook.web.nursery.bean.PossibleValuesCache;

public class FieldbookServiceTest {
       
    private FieldbookServiceImpl fieldbookServiceImpl;
    private MeasurementVariable locationVariable;
    private MeasurementVariable nonLocationVariable;
    
    @Before
    public void setUp() throws MiddlewareQueryException{
    	org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService = Mockito.mock(org.generationcp.middleware.service.api.FieldbookService.class);
    	List<Location> allLocation = new ArrayList<Location>();
    	allLocation.add(new Location(1));
    	allLocation.add(new Location(2));
    	Mockito.when(fieldbookMiddlewareService.getAllLocations()).thenReturn(allLocation);
    	Mockito.when(fieldbookMiddlewareService.getAllBreedingLocations()).thenReturn(new ArrayList<Location>());
    	
    	List<Person> personsList = new ArrayList<Person>();
    	personsList.add(createPerson(200));
    	
    	Mockito.when(fieldbookMiddlewareService.getAllPersonsOrderedByLocalCentral()).thenReturn(personsList);
    	
    	fieldbookServiceImpl = new FieldbookServiceImpl(fieldbookMiddlewareService, new PossibleValuesCache());
    	
    	List<ValueReference> possibleValues = new ArrayList<ValueReference>();
    	for(int i = 0 ; i < 5 ; i++){
    		possibleValues.add(new ValueReference(i, "Name: "+i));
    	}
    	
    	locationVariable = new MeasurementVariable();
    	nonLocationVariable = new MeasurementVariable();
    	
    	locationVariable.setTermId(TermId.LOCATION_ID.getId());    	
    	nonLocationVariable.setTermId(TermId.PI_ID.getId());
    	nonLocationVariable.setPossibleValues(possibleValues);    	
    }
    
    private Person createPerson(int id){
    	Person person = new Person("First Name", "Middle Name", "Last Name");
    	person.setId(id);
    	return person;
    }
    
    @Test
    public void testGetVariablePossibleValuesWhenVariableIsLocation() throws Exception {    	
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getVariablePossibleValues(locationVariable);
    	Assert.assertEquals("The results of get all possible values for the location should return a total of 2 records", 2, resultPossibleValues.size());
    }
    @Test
    public void testGetVariablePossibleValuesWhenVariableIsNonLocation() throws Exception {    	
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getVariablePossibleValues(nonLocationVariable);
    	Assert.assertEquals("The results of get all possible values for the non-location should return a total of 5 records", 5, resultPossibleValues.size());
    }    
    @Test
    public void testGetAllLocations() throws Exception {
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getAllLocations();
    	Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1), resultPossibleValues.get(0).getId());
    	Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2), resultPossibleValues.get(1).getId());
    	Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
    }
    @Test
    public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsTrue() throws Exception {
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getAllPossibleValues(locationVariable.getTermId(), true);
    	Assert.assertEquals("First possible value should have an id of 1 as per our test data", Integer.valueOf(1), resultPossibleValues.get(0).getId());
    	Assert.assertEquals("Second possible value should have an id of 2 as per our test data", Integer.valueOf(2), resultPossibleValues.get(1).getId());
    	Assert.assertEquals("There should only be 2 records as per our test data", 2, resultPossibleValues.size());
    }      
    @Test
    public void testGetAllPossibleValuesWhenIdIsLocationAndGetAllRecordsIsFalse() throws Exception {
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getAllPossibleValues(locationVariable.getTermId(), false);
    	Assert.assertEquals("There should be no records as per our test data", 0, resultPossibleValues.size());
    }     
    @Test
    public void testGetAllPossibleValuesWhenIdIsNonLocation() throws Exception {
    	List<ValueReference> resultPossibleValues = fieldbookServiceImpl.getAllPossibleValues(nonLocationVariable.getTermId(), false);
    	Assert.assertEquals("There should be 1 record as per our test data", 1, resultPossibleValues.size());
    	Assert.assertEquals("First possible value should have an id of 200 as per our test data", Integer.valueOf(200), resultPossibleValues.get(0).getId());
    }      
}
