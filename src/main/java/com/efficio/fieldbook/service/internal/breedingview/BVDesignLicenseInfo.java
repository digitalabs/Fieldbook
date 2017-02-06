package com.efficio.fieldbook.service.internal.breedingview;

public class BVDesignLicenseInfo {

	private Status status;

	public Status getStatus ()
	{
		return status;
	}

	public void setStatus (Status status)
	{
		this.status = status;
	}

	@Override
	public String toString()
	{
		return "BVDesignLicenseInfo [status = "+status+"]";
	}

}
