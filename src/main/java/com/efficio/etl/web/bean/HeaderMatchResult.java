
package com.efficio.etl.web.bean;

import org.generationcp.middleware.domain.dms.StandardVariable;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Deprecated
public class HeaderMatchResult {

	private final String headerName;
	private int matchedID;
	private String matchedName;

	public HeaderMatchResult(String headerName, StandardVariable matchedResult) {
		this.headerName = headerName;

		if (matchedResult != null) {
			this.matchedID = matchedResult.getId();
			this.matchedName = matchedResult.getName();
		}
	}

	public String getHeaderName() {
		return this.headerName;
	}

	public int getMatchedID() {
		return this.matchedID;
	}

	public String getMatchedName() {
		return this.matchedName;
	}
}
