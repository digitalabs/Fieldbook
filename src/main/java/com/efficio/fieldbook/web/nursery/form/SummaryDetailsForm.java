/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.nursery.form;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;

/**
 * Form for the Nursery Wizard - Step 5: Summary.
 */
public class SummaryDetailsForm {
    
    /** The nursery name. */
    private String nurseryName;
    
    /** The title. */
    private String title;
    
    /** The objective. */
    private String objective;
    
    /** The book name. */
    private String bookName;
    
    /* Nursery Conditions */
    /** The sequence number. */
    private String sequenceNumber;
    
    /** The principal investigator. */
    private String principalInvestigator;
    
    /** The location. */
    private String location;
    
    /** The breeding method. */
    private String breedingMethod;
    
    /** Traits to be measured */
    private List<MeasurementVariable> traits;
    
    public SummaryDetailsForm(){
        createDummyData();
    }

    /**
     * Instantiates a new summary details form.
     */
    public SummaryDetailsForm(boolean withDummy){
        createDummyData();
    }
    
    //TODO Remove later. Only for initial testing
    /**
     * Creates the dummy data.
     */
    private void createDummyData(){
        nurseryName = "UCR 2012 Nurseries";
        title = "UCR 2012 Nurseries";
        objective = "UCR 2012 Study for Population Development Nurseries";
        bookName = "UCR2012F1";
        sequenceNumber = "F2";
        principalInvestigator = "Jean Philips";
        location = "University of California - Riverside";
        breedingMethod = "Random Bulk CF";
        
        traits = new ArrayList<MeasurementVariable>();
        traits.add(new MeasurementVariable("BB", "Resistance to bacterial blight", null, null, null, null, null, null));
        traits.add(new MeasurementVariable("FLOW50", "Days to 50% flowering", null, null, null, null, null, null));
        traits.add(new MeasurementVariable("MAT95", "Days to 95% maturity", null, null, null, null, null, null));
        traits.add(new MeasurementVariable("NOTES", "BREEDERS NOTES", null, null, null, null, null, null));
        traits.add(new MeasurementVariable("NPSEL", "NUMBER OF PLANTS SELECTED", null, null, null, null, null, null));
        traits.add(new MeasurementVariable("SDSTORE", "AMOUNT OF SEED HARVESTED FOR STORAGE", null, null, null, null, null, null));             
    }
    
    /**
     * Gets the nursery name.
     *
     * @return the nursery name
     */
    public String getNurseryName() {
        return nurseryName;
    }
    
    /**
     * Sets the nursery name.
     *
     * @param nurseryName the new nursery name
     */
    public void setNurseryName(String nurseryName) {
        this.nurseryName = nurseryName;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the objective.
     *
     * @return the objective
     */
    public String getObjective() {
        return objective;
    }
    
    /**
     * Sets the objective.
     *
     * @param objective the new objective
     */
    public void setObjective(String objective) {
        this.objective = objective;
    }

    /**
     * Gets the book name.
     *
     * @return the book name
     */
    public String getBookName() {
        return bookName;
    }

    /**
     * Sets the book name.
     *
     * @param bookName the new book name
     */
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    
    /**
     * Gets the sequence number.
     *
     * @return the sequence number
     */
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    
    /**
     * Sets the sequence number.
     *
     * @param sequenceNumber the new sequence number
     */
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    /**
     * Gets the principal investigator.
     *
     * @return the principal investigator
     */
    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }
    
    /**
     * Sets the principal investigator.
     *
     * @param principalInvestigator the new principal investigator
     */
    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }
    
    /**
     * Gets the location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Sets the location.
     *
     * @param location the new location
     */
    public void setLocation(String location) {
        this.location = location;
    }
    
    /**
     * Gets the breeding method.
     *
     * @return the breeding method
     */
    public String getBreedingMethod() {
        return breedingMethod;
    }
    
    /**
     * Sets the breeding method.
     *
     * @param breedingMethod the new breeding method
     */
    public void setBreedingMethod(String breedingMethod) {
        this.breedingMethod = breedingMethod;
    }
    
    public List<MeasurementVariable> getTraits() {
        return traits;
    }
    
    public void setTraits(List<MeasurementVariable> traits) {
        this.traits = traits;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SummaryDetailsForm [nurseryName=");
        builder.append(nurseryName);
        builder.append(", title=");
        builder.append(title);
        builder.append(", objective=");
        builder.append(objective);
        builder.append(", bookName=");
        builder.append(bookName);
        builder.append(", sequenceNumber=");
        builder.append(sequenceNumber);
        builder.append(", principalInvestigator=");
        builder.append(principalInvestigator);
        builder.append(", location=");
        builder.append(location);
        builder.append(", breedingMethod=");
        builder.append(breedingMethod);
        builder.append(", traits=");
        for (MeasurementVariable trait : traits){
            builder.append("[name=").append(trait.getName())
                    .append(", description=").append(trait.getDescription())
                    .append("]");
        }
        builder.append("]");
        return builder.toString();
    } 
    
}
