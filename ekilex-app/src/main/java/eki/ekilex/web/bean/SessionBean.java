package eki.ekilex.web.bean;

import java.util.List;

import eki.common.data.AbstractDataObject;
import eki.ekilex.data.ClassifierSelect;

public class SessionBean extends AbstractDataObject {

	private static final long serialVersionUID = 1L;

	private List<String> selectedDatasets;

	private String resultLang;

	private List<ClassifierSelect> languagesOrder;

	public List<String> getSelectedDatasets() {
		return selectedDatasets;
	}

	public void setSelectedDatasets(List<String> selectedDatasets) {
		this.selectedDatasets = selectedDatasets;
	}

	public String getResultLang() {
		return resultLang;
	}

	public void setResultLang(String resultLang) {
		this.resultLang = resultLang;
	}

	public List<ClassifierSelect> getLanguagesOrder() {
		return languagesOrder;
	}

	public void setLanguagesOrder(List<ClassifierSelect> languagesOrder) {
		this.languagesOrder = languagesOrder;
	}

}
