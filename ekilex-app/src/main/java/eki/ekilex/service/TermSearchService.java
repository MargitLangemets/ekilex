package eki.ekilex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eki.ekilex.data.Classifier;
import eki.ekilex.data.Definition;
import eki.ekilex.data.FreeForm;
import eki.ekilex.data.Lexeme;
import eki.ekilex.data.Meaning;
import eki.ekilex.data.Rection;
import eki.ekilex.data.RectionUsageTranslationDefinitionTuple;
import eki.ekilex.data.TermDetails;
import eki.ekilex.service.db.SearchDbService;
import eki.ekilex.service.db.TermSearchDbService;
import eki.ekilex.service.util.ConversionUtil;

@Component
public class TermSearchService {

	@Autowired
	private TermSearchDbService termSearchDbService;

	@Autowired
	private SearchDbService searchDbService;

	@Autowired
	private ConversionUtil conversionUtil;

	@Transactional
	public TermDetails findWordDetailsInDatasets(Long formId, List<String> selectedDatasets) {

		Map<String, String> datasetNameMap = searchDbService.getDatasetNameMap();
		List<Meaning> meanings = termSearchDbService.findFormMeanings(formId, selectedDatasets).into(Meaning.class);

		for (Meaning meaning : meanings) {

			Long meaningId = meaning.getMeaningId();
			List<Long> lexemeIds = meaning.getLexemeIds();

			List<Definition> definitions = searchDbService.findMeaningDefinitions(meaningId).into(Definition.class);
			List<Classifier> domains = searchDbService.findMeaningDomains(meaningId).into(Classifier.class);
			List<FreeForm> meaningFreeforms = searchDbService.findMeaningFreeforms(meaningId).into(FreeForm.class);
			List<Lexeme> lexemes = new ArrayList<>();

			meaning.setDefinitions(definitions);
			meaning.setDomains(domains);
			meaning.setFreeforms(meaningFreeforms);
			meaning.setLexemes(lexemes);

			for (Long lexemeId : lexemeIds) {

				// lexeme is duplicated if many form.is_word-s
				List<Lexeme> lexemeWords = termSearchDbService.getLexemeWords(lexemeId).into(Lexeme.class);
				List<FreeForm> lexemeFreeforms = searchDbService.findLexemeFreeforms(lexemeId).into(FreeForm.class);
				List<RectionUsageTranslationDefinitionTuple> rectionUsageTranslationDefinitionTuples =
						searchDbService.findRectionUsageTranslationDefinitionTuples(lexemeId).into(RectionUsageTranslationDefinitionTuple.class);
				List<Rection> rections = conversionUtil.composeRections(rectionUsageTranslationDefinitionTuples);

				for (Lexeme lexeme : lexemeWords) {

					String dataset = lexeme.getDataset();
					dataset = datasetNameMap.get(dataset);
					lexeme.setDataset(dataset);
					lexeme.setFreeforms(lexemeFreeforms);
					lexeme.setRections(rections);
					lexemes.add(lexeme);
				}
			}
		}

		TermDetails termDetails = new TermDetails();
		termDetails.setMeanings(meanings);

		return termDetails;
	}
}