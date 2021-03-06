package eki.ekilex.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eki.common.data.AppData;
import eki.common.web.AppDataHolder;
import eki.ekilex.constant.SystemConstant;
import eki.ekilex.constant.WebConstant;
import eki.ekilex.data.Classifier;
import eki.ekilex.data.ClassifierSelect;
import eki.ekilex.data.Meaning;
import eki.ekilex.data.MeaningsResult;
import eki.ekilex.data.WordDetails;
import eki.ekilex.data.WordsResult;
import eki.ekilex.service.CommonDataService;
import eki.ekilex.service.FileService;
import eki.ekilex.service.LexSearchService;
import eki.ekilex.service.TermSearchService;

@ConditionalOnWebApplication
@RestController
public class DataController implements SystemConstant, WebConstant {

	@Autowired
	private AppDataHolder appDataHolder;

	@Autowired
	private FileService fileService;

	@Autowired
	private CommonDataService commonDataService;

	@Autowired
	private LexSearchService lexSearchService;

	@Autowired
	private TermSearchService termSearchService;

	@GetMapping(REST_SERVICES_URI + "/app")
	public AppData getAppData(HttpServletRequest request) {
		return appDataHolder.getAppData(request, POM_PATH);
	}

	@GetMapping("/files/images/{fileName:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveImage(@PathVariable String fileName) {

		Resource resource = fileService.getFileAsResource(fileName);
		return ResponseEntity
				.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}

	//TODO temp for testing. remove later
	@GetMapping({
			REST_SERVICES_URI + LEX_SEARCH_URI + "/{word}",
			REST_SERVICES_URI + LEX_SEARCH_URI + "/{word}/{datasets}"
	})
	@ResponseBody
	public WordsResult lexSearch(@PathVariable("word") String word, @PathVariable(value = "datasets", required = false) String datasetsStr) {

		boolean fetchAll = true;
		List<String> datasets = parseDatasets(datasetsStr);
		WordsResult results = lexSearchService.findWords(word, datasets, fetchAll);
		return results;
	}

	//TODO temp for testing. remove later
	@GetMapping({
			REST_SERVICES_URI + WORD_DETAILS_URI + "/{wordId}",
			REST_SERVICES_URI + WORD_DETAILS_URI + "/{wordId}/{datasets}"
	})
	@ResponseBody
	public WordDetails wordDetails(@PathVariable("wordId") String wordIdStr, @PathVariable(value = "datasets", required = false) String datasetsStr) {

		if (!StringUtils.isNumeric(wordIdStr)) {
			return null;
		}
		Long wordId = Long.valueOf(wordIdStr);
		List<String> datasets = parseDatasets(datasetsStr);
		WordDetails result = lexSearchService.getWordDetails(wordId, datasets);
		return result;
	}

	//TODO temp for testing. remove later
	@GetMapping({
			REST_SERVICES_URI + TERM_SEARCH_URI + "/{word}",
			REST_SERVICES_URI + TERM_SEARCH_URI + "/{word}/{datasets}"
	})
	@ResponseBody
	public MeaningsResult termSearch(@PathVariable("word") String word, @PathVariable(value = "datasets", required = false) String datasetsStr) {

		boolean fetchAll = true;
		List<String> datasets = parseDatasets(datasetsStr);
		String resultLang = null;
		MeaningsResult results = termSearchService.findMeanings(word, datasets, resultLang, fetchAll);
		return results;
	}

	//TODO temp for testing. remove later
	@GetMapping({
			REST_SERVICES_URI + MEANING_DETAILS_URI + "/{meaningId}",
			REST_SERVICES_URI + MEANING_DETAILS_URI + "/{meaningId}/{datasets}"
	})
	@ResponseBody
	public Meaning meaningDetails(@PathVariable("meaningId") String meaningIdStr, @PathVariable(value = "datasets", required = false) String datasetsStr) {

		if (!StringUtils.isNumeric(meaningIdStr)) {
			return null;
		}
		Long meaningId = Long.valueOf(meaningIdStr);
		List<String> datasets = parseDatasets(datasetsStr);
		List<Classifier> allLanguages = commonDataService.getLanguages();
		List<ClassifierSelect> languagesOrder = convert(allLanguages);
		Meaning meaning = termSearchService.getMeaning(meaningId, datasets, languagesOrder);
		return meaning;
	}

	private List<ClassifierSelect> convert(List<Classifier> allLanguages) {
		List<ClassifierSelect> languagesOrder = allLanguages.stream().map(language -> {
			ClassifierSelect languageSelect = new ClassifierSelect();
			languageSelect.setCode(language.getCode());
			languageSelect.setValue(language.getValue());
			languageSelect.setSelected(true);
			return languageSelect;
		}).collect(Collectors.toList());
		return languagesOrder;
	}


	private List<String> parseDatasets(String datasetsStr) {
		List<String> datasets = Collections.emptyList();
		if (StringUtils.isNotBlank(datasetsStr)) {
			String[] datasetsArr = StringUtils.split(datasetsStr, ',');
			datasets = Arrays.asList(datasetsArr);
		}
		return datasets;
	}
}
