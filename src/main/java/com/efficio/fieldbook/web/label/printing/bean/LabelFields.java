/**
 * ****************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * <p/>
 * *****************************************************************************
 */
package com.efficio.fieldbook.web.label.printing.bean;

import java.io.Serializable;

/**
 * The Class LabelFields.
 *
 * @author Efficio.Daniel
 */
public class LabelFields implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The name. */
    private String name;

    /** The id. */
    private int id;

    /** The marker if the field is part of germplasm list */
    private boolean isGermplasmListField = false;

    /**
     * Instantiates a new label fields.
     *
     * @param name the name
     * @param id the id
     */
    public LabelFields(String name, int id, boolean isGermplasmListField) {
        this.name = name;
        this.id = id;
        this.isGermplasmListField = isGermplasmListField;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Check if the field is part of germplasm list fields
     * @return isGermplasmListField
     */
    public boolean isGermplasmListField() {
        return isGermplasmListField;
    }

    /**
     * Mark the field if it is part of germplasm list fields
     * @param isGermplasmListField
     */
    public void setGermplasmListField(boolean isGermplasmListField) {
        this.isGermplasmListField = isGermplasmListField;
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

        LabelFields other = (LabelFields) obj;
        if (id != other.id) {
            return false;
        }

        if (isGermplasmListField != other.isGermplasmListField) {
            return false;
        }

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + (isGermplasmListField ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
}
