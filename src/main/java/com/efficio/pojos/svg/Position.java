
package com.efficio.pojos.svg;

public class Position{

    private int row;
    private int col;
    private int range;
    private int plot;
    
    public Position(int row, int col, int range, int plot) {
        this.row = row;
        this.col = col;
        this.range = range;
        this.plot = plot;
    }
    
    public int getRow() {
        return row;
    }
    
    public void setRow(int row) {
        this.row = row;
    }
    
    public int getCol() {
        return col;
    }
    
    public void setCol(int col) {
        this.col = col;
    }
    
    public int getRange() {
        return range;
    }
    
    public void setRange(int range) {
        this.range = range;
    }
    
    public int getPlot() {
        return plot;
    }
    
    public void setPlot(int plot) {
        this.plot = plot;
    }

}
