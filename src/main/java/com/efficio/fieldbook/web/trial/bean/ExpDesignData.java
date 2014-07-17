package com.efficio.fieldbook.web.trial.bean;

import java.util.List;

public class ExpDesignData  implements TabInfoBean{
	private List<ExpDesignDataDetail> expDesignDetailList;

	public ExpDesignData(){
		super();
	}
			
	public ExpDesignData(List<ExpDesignDataDetail> expDesignDetailList) {
		super();
		this.expDesignDetailList = expDesignDetailList;
	}

	public List<ExpDesignDataDetail> getExpDesignDetailList() {
		return expDesignDetailList;
	}

	public void setExpDesignDetailList(List<ExpDesignDataDetail> expDesignDetailList) {
		this.expDesignDetailList = expDesignDetailList;
	}
	
	
}
