/**
 Snametype: is null or contains a name type id number from the UDFLDS table. See below for some example Snametypes. 
If not null and if the germplasm being advanced has a name of the specified type, then this is used as the root name of the advanced strain
otherwise the preferred name of the source is used.
If the root name is a cross string (contains one or more /s not enclosed within the range of a pair of parentheses) 
then enclose the root name in parentheses. 
*/

package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.middleware.pojos.Name;

import java.util.List;

public class RootNameExpression implements Expression {

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder value : values) {
			Integer snametype = source.getBreedingMethod().getSnametype();
	    	String nameString = null;
	    	List<Name> names = source.getNames();
	    	if (names != null && !names.isEmpty()) {
	    		if (snametype != null) {
		    		for (Name name : names) {
		    			if (name.getTypeId() != null && name.getTypeId().equals(snametype)) {
		    				nameString = name.getNval();
							source.setRootNameType(name.getTypeId());
		    				break;
		    			}
		    		}
	    		}
	    		if (nameString == null) {
		    		//if no sname type defined or if no name found that matched the snametype
		    		for (Name name : names) {
		    			if (name.getNstat() != null && name.getNstat().equals(1)) {
		    				nameString = name.getNval();
							source.setRootNameType(name.getTypeId());
		    			}
		    		}
	    		}
	    	}
	    	
	    	if (nameString == null) {
	    		break;
	    	}

			source.setRootName(nameString);
	    	
	    	if (!checkNameIfEnclosed(nameString)) {
	    		value.append("(").append(nameString).append(")");
	    	} else {
	    		value.append(nameString);
	    	}
		}
	}

	@Override
	public String getExpressionKey() {
		return null;
	}
	
	private boolean checkNameIfEnclosed(String name) {
		int index = name.indexOf("/", 0);
		while (index > -1 && index < name.length()) {
			if (!checkIfEnclosed(name, index)) {
				return false;
			}
			
			index = name.indexOf("/", index + 1);
		}
		return true;
	}

	private boolean checkIfEnclosed(String name, int index) {
		
		return checkNeighbor(name, index, '(', -1, 0, ')')
			&& checkNeighbor(name, index, ')', 1, name.length() - 1, '(');
	}
	
	private boolean checkNeighbor(String name, int index, char literal, int delta, int stopPoint, char oppositeLiteral) {
		int oppositeCount = 0;
		for (int i = index + delta; i != stopPoint + delta; i = i + delta) {
			if (name.charAt(i) == literal) {
				if (oppositeCount == 0) {
					return true;
				} else {
					oppositeCount--;
				}
			} else if (name.charAt(i) == oppositeLiteral) {
				oppositeCount++;
			}
		}
		return false;
	}
}
