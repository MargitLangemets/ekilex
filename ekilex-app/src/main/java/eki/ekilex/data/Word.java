package eki.ekilex.data;

import eki.common.data.AbstractDataObject;

import javax.persistence.Column;
import java.util.function.Consumer;

public class Word extends AbstractDataObject {

	private static final long serialVersionUID = 1L;

	@Column(name = "word_id")
	private Long wordId;

	@Column(name = "word")
	private String value;

	@Column(name = "homonym_nr")
	private Integer homonymNumber;

	@Column(name = "lang")
	private String language;

	@Column(name = "dataset_codes")
	private String[] datasetCodes;

	public Word() {
	}

	public Word(Consumer<Word> builder) {
		builder.accept(this);
	}

	public Long getWordId() {
		return wordId;
	}

	public void setWordId(Long wordId) {
		this.wordId = wordId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getHomonymNumber() {
		return homonymNumber;
	}

	public void setHomonymNumber(Integer homonymNumber) {
		this.homonymNumber = homonymNumber;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String[] getDatasetCodes() {
		return datasetCodes;
	}

	public void setDatasetCodes(String[] datasetCodes) {
		this.datasetCodes = datasetCodes;
	}

}
