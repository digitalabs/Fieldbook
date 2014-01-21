package com.efficio.fieldbook.util;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;

import com.efficio.fieldbook.web.fieldmap.bean.Plot;

public class FieldMapUtilityHelper {
	
	/** The Constant NEXT_LINE. */
    private static final String NEXT_LINE = "<br/>";
    
	/**
     * Populate plot data.
     *
     * @param counter the counter
     * @param labels the labels
     * @param col the col
     * @param range the range
     * @param plots the plots
     * @param isUpward the is upward
     * @param startCol the start col
     * @param startRange the start range
     * @param isStartOk the is start ok
     * @param deletedPlot the deleted plot
     * @param isTrial the is trial
     * @return the int
     */
    public static int populatePlotData(int counter, List<FieldMapLabel> labels, int col, int range, Plot[][] plots,
            boolean isUpward, int startCol, int startRange, boolean isStartOk, Map deletedPlot, boolean isTrial){

        String stringToDisplay = "";
        int i = col;
        int j = range;
        boolean hasAvailableEntries = true;
        if(counter < labels.size()){
            stringToDisplay = getDisplayString(labels.get(counter), isTrial);
        }else{
            hasAvailableEntries = false;
        }
        //plots[i][j].setUpward(isUpward);
    
        if(isStartOk){
            plots[i][j].setNotStarted(false);
            if(isDeleted(i,j, deletedPlot) == false){
                plots[i][j].setPlotDeleted(false);
                if(hasAvailableEntries){
                    //meaning we can plant already and move to the next plant
                    plots[i][j].setDisplayString(stringToDisplay);
                    //plots[i][j].setExperimentId(labels.get(counter).getExperimentId());
                    labels.get(counter).setColumn(i+1);
                    labels.get(counter).setRange(j+1);
                    
                    plots[i][j].setNoMoreEntries(false);
                    counter++;
                }else{
                    //there are space but no more entries to plant
                    plots[i][j].setNoMoreEntries(true);
                }
            }
            else{
                //meaing this plot is deleted
                plots[i][j].setPlotDeleted(true);
            }
        }else{
            //meaning we haven't started
            plots[i][j].setNotStarted(true);
        }
        return counter;
    }
    
    /**
     * Checks if is deleted.
     *
     * @param col the col
     * @param range the range
     * @param deletedPlot the deleted plot
     * @return true, if is deleted
     */
    public static boolean isDeleted(int col, int range, Map deletedPlot){
        if(deletedPlot.get(col+"_"+range) != null)
            return true;
        return false;
    }    
    
    /**
     * Gets the display string.
     *
     * @param label the label
     * @param isTrial the is trial
     * @return the display string
     */
    public static String getDisplayString(FieldMapLabel label, boolean isTrial) {
        StringBuilder textLabel = new StringBuilder();
        textLabel.append(label.getStudyName());
        textLabel.append(NEXT_LINE + "Entry " + label.getEntryNumber());
        if (isTrial) {
            textLabel.append(NEXT_LINE + "Rep " + label.getRep());
        }
        return textLabel.toString();
    }
}
