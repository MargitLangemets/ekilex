package eki.ekilex.runner;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.replaceChars;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import eki.common.constant.WordRelationGroupType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eki.common.constant.FreeformType;
import eki.ekilex.data.transform.Guid;
import eki.ekilex.data.transform.Lexeme;
import eki.ekilex.data.transform.Meaning;
import eki.ekilex.data.transform.Paradigm;
import eki.ekilex.data.transform.Usage;
import eki.ekilex.data.transform.UsageTranslation;
import eki.ekilex.data.transform.Word;
import eki.ekilex.service.ReportComposer;

@Component
public class Ev2LoaderRunner extends SsBasedLoaderRunner {

	private static Logger logger = LoggerFactory.getLogger(Ev2LoaderRunner.class);

	private final static String SQL_SELECT_WORD_BY_DATASET = "sql/select_word_by_dataset.sql";
	private final static String russianLang = "rus";
	private final static String ASPECT_TYPE_SOV = "sov";
	private final static String ASPECT_TYPE_NESOV = "nesov";
	private final static String ASPECT_TYPE_SOV_NESOV = "sov/nesov";

	private String sqlSelectWordByDataset;

	@Override
	protected Map<String, String> xpathExpressions() {
		Map<String, String> experssions = new HashMap<>();
		experssions.put("reportingId", "x:P/x:mg/x:m"); // use first word as id for reporting
		experssions.put("word", "x:m");
		experssions.put("wordDisplayMorph", "x:vk");
		experssions.put("wordVocalForm", "x:hld");
		experssions.put("grammarValue", "x:gki");
		experssions.put("domain", "x:dg/x:v");
		return experssions;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream resourceFileInputStream;

		resourceFileInputStream = classLoader.getResourceAsStream(SQL_SELECT_WORD_BY_DATASET);
		sqlSelectWordByDataset = getContent(resourceFileInputStream);
	}

	@Override
	public String getDataset() {
		return "ev2";
	}

	@Transactional
	public void execute(String dataXmlFilePath1, String dataXmlFilePath2, Map<String, List<Guid>> ssGuidMap, boolean doReports) throws Exception {

		long t1, t2;
		t1 = System.currentTimeMillis();
		logger.debug("Loading EV2...");

		reportingEnabled = doReports;
		if (reportingEnabled) {
			reportComposer = new ReportComposer("EV2 import", ARTICLES_REPORT_NAME, DESCRIPTIONS_REPORT_NAME, MEANINGS_REPORT_NAME);
		}

		String[] dataXmlFilePaths = new String[] {dataXmlFilePath1, dataXmlFilePath2};
		Document dataDoc;
		Element rootElement;
		List<Element> allArticleNodes = new ArrayList<>();
		List<Element> articleNodes;
		for (String dataXmlFilePath : dataXmlFilePaths) {
			dataDoc = xmlReader.readDocument(dataXmlFilePath);
			rootElement = dataDoc.getRootElement();
			articleNodes = (List<Element>) rootElement.content().stream().filter(node -> node instanceof Element).collect(toList());
			allArticleNodes.addAll(articleNodes);
		}
		long articleCount = allArticleNodes.size();
		writeToLogFile("Artiklite töötlus", "", "");
		logger.debug("{} articles found", articleCount);

		Context context = new Context();
		long progressIndicator = articleCount / Math.min(articleCount, 100);
		long articleCounter = 0;
		for (Element articleNode : allArticleNodes) {
			processArticle(articleNode, ssGuidMap, context);
			articleCounter++;
			if (articleCounter % progressIndicator == 0) {
				long progressPercent = articleCounter / progressIndicator;
				logger.debug("{}% - {} articles iterated", progressPercent, articleCounter);
			}
		}
		logger.debug("total {} articles iterated", articleCounter);
		processLatinTerms(context);

		logger.debug("Found {} reused words", context.reusedWordCount.getValue());
		logger.debug("Found {} ss words", context.ssWordCount.getValue());

		t2 = System.currentTimeMillis();
		logger.debug("Done loading in {} ms", (t2 - t1));
	}

	@Transactional
	void processArticle(
			Element articleNode,
			Map<String, List<Guid>> ssGuidMap,
			Context context) throws Exception {

		final String articleHeaderExp = "x:P";
		final String articleBodyExp = "x:S";
		final String articleGuidExp = "x:G";
		final String articlePhraseologyExp = "x:F";

		String reportingId = extractReporingId(articleNode);
		String guid = extractGuid(articleNode, articleGuidExp);
		List<WordData> newWords = new ArrayList<>();

		Element headerNode = (Element) articleNode.selectSingleNode(articleHeaderExp);
		processArticleHeader(headerNode, guid, reportingId, newWords, ssGuidMap, context);
		try {
			Element contentNode = (Element) articleNode.selectSingleNode(articleBodyExp);
			if (contentNode != null) {
				processArticleContent(reportingId, contentNode, newWords, context);
			}
			Element phraseologyNode = (Element) articleNode.selectSingleNode(articlePhraseologyExp);
			if (phraseologyNode != null) {
				processPhraseology(reportingId, phraseologyNode, context);
			}
		} catch (Exception e) {
			logger.debug("KEYWORD : {}", reportingId);
			throw e;
		}
		context.importedWords.addAll(newWords);
	}

	private void processPhraseology(String reportingId, Element node, Context context) throws Exception {

		final String phraseologyGroupExp = "x:fg";
		final String phraseologyValueExp = "x:f";
		final String meaningGroupExp = "x:fqnp";
		final String governmentsExp = "x:r";
		final String domainsExp = "x:v";
		final String registersExp = "x:s";
		final String definitionsExp = "x:fd";
		final String translationGroupExp = "x:fqng";
		final String translationValueExp = "x:qf";

		List<Element> groupNodes = node.selectNodes(phraseologyGroupExp);
		for (Element groupNode : groupNodes) {
			List<String> wordValues = extractValuesAsStrings(groupNode, phraseologyValueExp);
			for (String wordValue : wordValues) {
				String word = cleanUp(wordValue);
				if (isNotWordInSs1(word)) {
					continue;
				}
				WordData wordData = findOrCreateWord(context, word, wordValue, dataLang, null);
				List<Element> meaningGroupNodes = groupNode.selectNodes(meaningGroupExp);
				int lexemeLevel1 = 1;
				for (Element meaningGroupNode: meaningGroupNodes) {
					Long meaningId = createMeaning(new Meaning());

					List<String> definitions = extractValuesAsStrings(meaningGroupNode, definitionsExp);
					for (String definition : definitions) {
						createDefinition(meaningId, definition, dataLang, getDataset());
					}

					List<String> domains = extractValuesAsStrings(meaningGroupNode, domainsExp);
					processDomains(null, meaningId, domains);

					Lexeme lexeme = new Lexeme();
					lexeme.setWordId(wordData.id);
					lexeme.setMeaningId(meaningId);
					lexeme.setLevel1(lexemeLevel1);
					lexeme.setLevel2(1);
					lexeme.setLevel3(1);
					Long lexemeId = createLexeme(lexeme, getDataset());
					if (lexemeId != null) {
						List<String> governments = extractValuesAsStrings(meaningGroupNode, governmentsExp);
						List<String> registers = extractValuesAsStrings(meaningGroupNode, registersExp);
						saveGovernments(wordData, governments, lexemeId);
						saveRegisters(lexemeId, registers, reportingId);
					}
					lexemeLevel1++;

					List<Element> translationGroupNodes = meaningGroupNode.selectNodes(translationGroupExp);
					for(Element transalationGroupNode : translationGroupNodes) {
						String russianWord = extractAsString(transalationGroupNode, translationValueExp);
						WordData russianWordData = findOrCreateWord(context, cleanUp(russianWord), russianWord, russianLang, null);
						List<String> russianRegisters = extractValuesAsStrings(transalationGroupNode, registersExp);
						Lexeme russianLexeme = new Lexeme();
						russianLexeme.setWordId(russianWordData.id);
						russianLexeme.setMeaningId(meaningId);
						russianLexeme.setLevel1(russianWordData.level1);
						russianWordData.level1++;
						russianLexeme.setLevel2(1);
						russianLexeme.setLevel3(1);
						Long russianLexemeId = createLexeme(russianLexeme, getDataset());
						if (russianLexemeId != null) {
							saveRegisters(russianLexemeId, russianRegisters, reportingId);
						}
						List<String> additionalDomains = extractValuesAsStrings(transalationGroupNode, domainsExp);
						processDomains(null, meaningId, additionalDomains);
					}
				}
			}
		}
	}

	private void processArticleHeader(
			Element headerNode,
			String guid,
			String reportingId,
			List<WordData> newWords,
			Map<String, List<Guid>> ssGuidMap,
			Context context) throws Exception {

		final String wordGroupExp = "x:mg";
		final String wordPosCodeExp = "x:sl";
		final String wordGrammarPosCodesExp = "x:grk/x:sl";

		List<Element> wordGroupNodes = headerNode.selectNodes(wordGroupExp);
		for (Element wordGroupNode : wordGroupNodes) {
			WordData wordData = new WordData();
			wordData.reportingId = reportingId;

			Word word = extractWordData(wordGroupNode, wordData, guid, 0);
			if (word != null) {
				List<Paradigm> paradigms = extractParadigms(wordGroupNode, wordData);
				wordData.id = createOrSelectWord(word, paradigms, getDataset(), ssGuidMap, context.ssWordCount, context.reusedWordCount);
			}

			List<PosData> posCodes = extractPosCodes(wordGroupNode, wordPosCodeExp);
			wordData.posCodes.addAll(posCodes);
			posCodes = extractPosCodes(wordGroupNode, wordGrammarPosCodesExp);
			wordData.posCodes.addAll(posCodes);

			List<String> governments = extractGovernments(wordGroupNode);
			if (!governments.isEmpty()) {
				wordData.governments.addAll(governments);
			}

			newWords.add(wordData);
		}
	}

	private void processArticleContent(String reportingId, Element contentNode, List<WordData> newWords, Context context) throws Exception {

		final String meaningNumberGroupExp = "x:tp";
		final String meaningPosCodeExp = "x:sl";
		final String meaningPosCode2Exp = "x:grk/x:sl";
		final String meaningGroupExp = "x:tg";
		final String meaningDefinitionExp = "x:dg/x:d";
		final String registerExp = "x:dg/x:s";
		final String latinTermExp = "x:dg/x:ld";

		List<Element> meaningNumberGroupNodes = contentNode.selectNodes(meaningNumberGroupExp);
		int lexemeLevel1 = 0;
		for (Element meaningNumberGroupNode : meaningNumberGroupNodes) {
			lexemeLevel1++;
			List<PosData> meaningPosCodes = extractPosCodes(meaningNumberGroupNode, meaningPosCodeExp);
			meaningPosCodes.addAll(extractPosCodes(meaningNumberGroupNode, meaningPosCode2Exp));
			List<String> meaningGovernments = extractGovernments(meaningNumberGroupNode);
			List<String> meaningGrammars = extractGrammar(meaningNumberGroupNode);
			List<Element> meaningGroupNodes = meaningNumberGroupNode.selectNodes(meaningGroupExp);
			List<Usage> usages = extractUsages(meaningNumberGroupNode);

			int lexemeLevel2 = 0;
			for (Element meaningGroupNode : meaningGroupNodes) {
				lexemeLevel2++;
				List<PosData> lexemePosCodes =  extractPosCodes(meaningGroupNode, meaningPosCodeExp);
				List<String> lexemeGrammars = extractGrammar(meaningGroupNode);
				List<String> registers = extractValuesAsStrings(meaningGroupNode, registerExp);

				Long meaningId;
				List<String> definitionsToAdd = new ArrayList<>();
				List<String> definitionsToCache = new ArrayList<>();
				List<String> definitions = extractValuesAsStrings(meaningGroupNode, meaningDefinitionExp);
				List<LexemeToWordData> meaningLatinTerms = extractLatinTerms(meaningGroupNode, latinTermExp, reportingId);
				List<String> additionalDomains = new ArrayList<>();
				List<List<LexemeToWordData>> aspectGroups = new ArrayList<>();
				List<LexemeToWordData> meaningRussianWords = extractRussianWords(meaningGroupNode, additionalDomains, aspectGroups, reportingId);
				List<LexemeToWordData> connectedWords =
						Stream.of(
								meaningLatinTerms.stream()
						).flatMap(i -> i).collect(toList());
				WordToMeaningData meaningData = findExistingMeaning(context, newWords.get(0), lexemeLevel1, connectedWords, definitions);
				if (meaningData == null) {
					meaningId = createMeaning(new Meaning());
					definitionsToAdd.addAll(definitions);
					definitionsToCache.addAll(definitions);
				} else {
					meaningId = meaningData.meaningId;
					validateMeaning(meaningData, definitions, reportingId);
					definitionsToAdd = definitions.stream().filter(def -> !meaningData.meaningDefinitions.contains(def)).collect(toList());
					meaningData.meaningDefinitions.addAll(definitionsToAdd);
					definitionsToCache.addAll(meaningData.meaningDefinitions);
				}
				if (!definitionsToAdd.isEmpty()) {
					for (String definition : definitionsToAdd) {
						createDefinition(meaningId, definition, dataLang, getDataset());
					}
					if (definitionsToAdd.size() > 1) {
						writeToLogFile(DESCRIPTIONS_REPORT_NAME, reportingId, "Leitud rohkem kui üks seletus <s:d>", newWords.get(0).value);
					}
				}
				cacheMeaningRelatedData(context, meaningId, definitionsToCache, newWords.get(0), lexemeLevel1, meaningLatinTerms);

				processDomains(meaningGroupNode, meaningId, additionalDomains);

				int lexemeLevel3 = 1;
				for (WordData newWordData : newWords) {
					Lexeme lexeme = new Lexeme();
					lexeme.setWordId(newWordData.id);
					lexeme.setMeaningId(meaningId);
					lexeme.setLevel1(lexemeLevel1);
					lexeme.setLevel2(lexemeLevel2);
					lexeme.setLevel3(lexemeLevel3);
					Long lexemeId = createLexeme(lexeme, getDataset());
					if (lexemeId != null) {
						// FIXME: add usages and subword relations only to first lexeme on the second level
						// this is temporary solution, till EKI provides better one
						if (lexemeLevel2 == 1) {
							createUsages(lexemeId, usages, dataLang);
						}
						saveGovernments(newWordData, meaningGovernments, lexemeId);
						savePosAndDeriv(newWordData, meaningPosCodes, lexemePosCodes, lexemeId, reportingId);
						saveGrammars(newWordData, meaningGrammars, lexemeGrammars, lexemeId);
						saveRegisters(lexemeId, registers, reportingId);
					}
				}
				processRussianWords(context, meaningRussianWords, aspectGroups, meaningId);
			}
			processWordsInUsageGroups(context, meaningNumberGroupNode, reportingId);
		}
	}

	private void processRussianWords(Context context, List<LexemeToWordData> meaningRussianWords, List<List<LexemeToWordData>> aspectGroups, Long meaningId) throws Exception {
		for (LexemeToWordData russianWordData : meaningRussianWords) {
			WordData russianWord = findOrCreateWord(context, russianWordData.word, russianWordData.displayForm, russianLang, russianWordData.aspect);
			russianWordData.wordId = russianWord.id;
			Lexeme lexeme = new Lexeme();
			lexeme.setWordId(russianWord.id);
			lexeme.setMeaningId(meaningId);
			lexeme.setLevel1(russianWord.level1);
			russianWord.level1++;
			lexeme.setLevel2(1);
			lexeme.setLevel3(1);
			Long lexemeId = createLexeme(lexeme, getDataset());
			if (lexemeId != null) {
				if (StringUtils.isNotBlank(russianWordData.government)) {
					createOrSelectLexemeFreeform(lexemeId, FreeformType.GOVERNMENT, russianWordData.government);
				}
				if (StringUtils.isNotBlank(russianWordData.register)) {
					saveRegisters(lexemeId, asList(russianWordData.register), russianWordData.word);
				}
			}
		}
		for (List<LexemeToWordData> aspectGroup : aspectGroups) {
			List<Long> memberIds = aspectGroup.stream().map(w -> w.wordId).collect(toList());
			if (hasNoWordRelationGroupWithMembers(WordRelationGroupType.ASPECTS, memberIds)) {
				Long wordGroup = createWordRelationGroup(WordRelationGroupType.ASPECTS);
				for (LexemeToWordData wordData : aspectGroup) {
					createWordRelationGroupMember(wordGroup, wordData.wordId);
				}
			}
		}
	}

	private void processWordsInUsageGroups(Context context, Element node, String reportingId) throws Exception {

		final String usageBlockExp = "x:np";
		final String usageGroupExp = "x:ng";
		final String usageExp = "x:n";
		final String meaningGroupExp = "x:qnp";
		final String registersExp = "x:s";
		final String domainsExp = "x:v";
		final String definitionExp = "x:nd";
		final String translationGroupExp = "x:qng";
		final String translationValueExp = "x:qn";
		final String latinTermExp = "x:ld";

		List<Element> usageBlockNodes = node.selectNodes(usageBlockExp);
		for (Element usageBlockNode : usageBlockNodes) {
			if (isRestricted(usageBlockNode)) continue;
			List<Element> usageGroupNodes = usageBlockNode.selectNodes(usageGroupExp);
			for (Element usageGroupNode : usageGroupNodes) {
				List<String> wordValues = extractValuesAsStrings(usageGroupNode, usageExp);
				for (String wordValue : wordValues) {
					if (!isUsage(cleanUp(wordValue))) {
						int level1 = 1;
						WordData wordData = findOrCreateWord(context, cleanUp(wordValue), wordValue, dataLang, null);
						List<Element> meaningGroupNodes = usageGroupNode.selectNodes(meaningGroupExp);
						for (Element meaningGroupNode: meaningGroupNodes) {
							Map<String, Object> lexemeForWord = findExistingLexemeForWord(wordData.id, level1);
							boolean useExistingLexeme = (lexemeForWord != null);
							Long meaningId;
							if (useExistingLexeme) {
								meaningId = (Long)lexemeForWord.get("meaning_id");
							} else {
								meaningId = createMeaning(new Meaning());
							}

							List<LexemeToWordData> latinTerms = extractLatinTerms(meaningGroupNode, latinTermExp, reportingId);
							latinTerms.forEach(term -> term.meaningId = meaningId);
							if (useExistingLexeme) {
								boolean latinTermExists = context.latinTermins.stream().anyMatch(latinTerm -> latinTerm.meaningId.equals(meaningId));
								if (!latinTermExists) {
									context.latinTermins.addAll(latinTerms);
								}
							} else {
								context.latinTermins.addAll(latinTerms);
							}

							List<String> domains = extractValuesAsStrings(meaningGroupNode, domainsExp);
							processDomains(null, meaningId, domains);

							List<String> definitions = extractValuesAsStrings(meaningGroupNode, definitionExp);
							for (String definition : definitions) {
								createDefinition(meaningId, definition, dataLang, getDataset());
								if (useExistingLexeme) {
									writeToLogFile(reportingId, "lisan definitsiooni olemasolevale mõistele", wordData.value + " : " + definition);
								}
							}

							Long lexemeId;
							if (useExistingLexeme) {
								lexemeId = (Long)lexemeForWord.get("id");
							} else {
								Lexeme lexeme = new Lexeme();
								lexeme.setWordId(wordData.id);
								lexeme.setMeaningId(meaningId);
								lexeme.setLevel1(level1);
								lexeme.setLevel2(1);
								lexeme.setLevel3(1);
								lexemeId = createLexeme(lexeme, getDataset());
							}
							if (lexemeId != null) {
								List<String> registers = extractValuesAsStrings(meaningGroupNode, registersExp);
								saveRegisters(lexemeId, registers, reportingId);
							}
							level1++;

							List<Element> translationGroupNodes = meaningGroupNode.selectNodes(translationGroupExp);
							for(Element transalationGroupNode : translationGroupNodes) {
								String russianWord = extractAsString(transalationGroupNode, translationValueExp);
								WordData russianWordData = findOrCreateWord(context, cleanUp(russianWord), russianWord, russianLang, null);
								List<String> russianRegisters = extractValuesAsStrings(transalationGroupNode, registersExp);
								boolean createNewRussianLexeme = true;
								Long russianLexemeId = null;
								if (useExistingLexeme) {
									createNewRussianLexeme = findExistingLexemeForWordAndMeaning(russianWordData.id, meaningId) == null;
									if (createNewRussianLexeme) {
										writeToLogFile(reportingId, "lisan vene vaste olemasolevale mõistele", wordData.value + " : " + russianWordData.value);
									}
								}
								if (createNewRussianLexeme) {
									Lexeme russianLexeme = new Lexeme();
									russianLexeme.setWordId(russianWordData.id);
									russianLexeme.setMeaningId(meaningId);
									russianLexeme.setLevel1(1);
									russianLexeme.setLevel2(1);
									russianLexeme.setLevel3(1);
									russianLexemeId = createLexeme(russianLexeme, getDataset());
								}
								if (russianLexemeId != null) {
									saveRegisters(russianLexemeId, russianRegisters, reportingId);
								}
								List<String> additionalDomains = extractValuesAsStrings(transalationGroupNode, domainsExp);
								processDomains(null, meaningId, additionalDomains);
							}
						}
					}
				}
			}
		}
	}

	private WordData findOrCreateWord(Context context, String wordValue, String wordDisplayForm, String wordLanguage, String aspect) throws Exception {
		Optional<WordData> word = context.importedWords.stream()
				.filter(w -> Objects.equals(w.value, wordValue) && Objects.equals(w.displayForm, wordDisplayForm)).findFirst();
		if (word.isPresent()) {
			return word.get();
		} else {
			WordData newWord = createDefaultWordFrom(wordValue, wordDisplayForm, wordLanguage, null, null, aspect);
			context.importedWords.add(newWord);
			return newWord;
		}
	}

	private String cleanRussianTranslation(String usageValue) {
		return replaceChars(usageValue, "\"", "");
	}

	private void cacheMeaningRelatedData(
			Context context, Long meaningId, List<String> definitions, WordData keyword, int level1,
			List<LexemeToWordData> latinTerms
	) {
		latinTerms.forEach(data -> data.meaningId = meaningId);
		context.latinTermins.addAll(latinTerms);

		context.meanings.stream()
				.filter(m -> Objects.equals(m.meaningId, meaningId))
				.forEach(m -> {m.meaningDefinitions.clear(); m.meaningDefinitions.addAll(definitions);});
		List<WordData> words = new ArrayList<>();
		words.add(keyword);
		words.forEach(word -> {
			context.meanings.addAll(convertToMeaningData(latinTerms, word, level1, definitions));
		});
	}

	private void savePosAndDeriv(WordData newWordData, List<PosData> meaningPosCodes, List<PosData> lexemePosCodes, Long lexemeId, String reportingId) {

		Set<PosData> combinedPosCodes = new HashSet<>();
		try {
			combinedPosCodes.addAll(lexemePosCodes);
			if (combinedPosCodes.isEmpty()) {
				combinedPosCodes.addAll(meaningPosCodes);
			}
			if (combinedPosCodes.isEmpty()) {
				combinedPosCodes.addAll(newWordData.posCodes);
			}
			if (combinedPosCodes.size() > 1) {
				String posCodesStr = combinedPosCodes.stream().map(p -> p.code).collect(Collectors.joining(","));
				//					logger.debug("Found more than one POS code <s:mg/s:sl> : {} : {}", reportingId, posCodesStr);
				writeToLogFile(reportingId, "Leiti rohkem kui üks sõnaliik <x:sl>", posCodesStr);
			}
			for (PosData posCode : combinedPosCodes) {
				if (posCodes.containsKey(posCode.code)) {
					Map<String, Object> params = new HashMap<>();
					params.put("lexeme_id", lexemeId);
					params.put("pos_code", posCodes.get(posCode.code));
					params.put("process_state_code", processStateCodes.get(posCode.processStateCode));
					basicDbService.create(LEXEME_POS, params);
				}
			}
		} catch (Exception e) {
			logger.debug("lexemeId {} : newWord : {}, {}, {}",
					lexemeId, newWordData.value, newWordData.id, combinedPosCodes.stream().map(p -> p.code).collect(Collectors.joining(",")));
			logger.error("ERROR", e);
		}
	}

	private void saveGrammars(WordData newWordData, List<String> meaningGrammars, List<String> lexemeGrammars, Long lexemeId) throws Exception {

		Set<String> grammars = new HashSet<>();
		grammars.addAll(lexemeGrammars);
		grammars.addAll(meaningGrammars);
		grammars.addAll(newWordData.grammars);
		for (String grammar : grammars) {
			createLexemeFreeform(lexemeId, FreeformType.GRAMMAR, grammar, dataLang);
		}
	}

	private void saveGovernments(WordData wordData, List<String> meaningGovernments, Long lexemeId) throws Exception {

		Set<String> governments = new HashSet<>();
		governments.addAll(meaningGovernments);
		if (governments.isEmpty()) {
			governments.addAll(wordData.governments);
		}
		for (String government : governments) {
			createOrSelectLexemeFreeform(lexemeId, FreeformType.GOVERNMENT, government);
		}
	}

	private List<Usage> extractUsages(Element node) {

		final String usageBlockExp = "x:np";
		final String usageGroupExp = "x:ng";
		final String usageExp = "x:n";
		final String usageDefinitionExp = "x:qnp/x:nd";

		List<Usage> usages = new ArrayList<>();
		List<Element> usageBlockNodes = node.selectNodes(usageBlockExp);
		for (Element usageBlockNode : usageBlockNodes) {
			if (isRestricted(usageBlockNode)) continue;
			List<Element> usageGroupNodes = usageBlockNode.selectNodes(usageGroupExp);
			for (Element usageGroupNode : usageGroupNodes) {
				List<String> usageValues = extractValuesAsStrings(usageGroupNode, usageExp);
				for (String usageValue : usageValues) {
					if (isUsage(usageValue)) {
						Usage usage = new Usage();
						usage.setValue(usageValue);
						usage.setDefinitions(extractValuesAsStrings(usageGroupNode, usageDefinitionExp));
						usage.setUsageTranslations(extractUsageTranslations(usageGroupNode));
						usages.add(usage);
					}
				}
			}
		}
		return usages;
	}

	private boolean isUsage(String usageValue) {
		return usageValue.contains(" ") && isNotWordInSs1(usageValue);
	}

	private List<UsageTranslation> extractUsageTranslations(Element node) {

		final String usageTranslationExp = "x:qnp/x:qng/x:qn";

		List<UsageTranslation> translations = new ArrayList<>();
		List<String> translationValues = extractValuesAsStrings(node, usageTranslationExp);
		for (String translationValue : translationValues) {
			UsageTranslation translation = new UsageTranslation();
			translation.setValue(cleanRussianTranslation(translationValue));
			translation.setLang(russianLang);
			translations.add(translation);
		}

		return translations;
	}

	private List<LexemeToWordData> extractLatinTerms(Element node, String latinTermExp, String reportingId) throws Exception {
		return extractLexemeMetadata(node, latinTermExp, null, reportingId);
	}

	private List<String> extractGovernments(Element node) {
		final String wordGovernmentExp = "x:grk/x:r";
		return extractValuesAsStrings(node, wordGovernmentExp);
	}

	private List<LexemeToWordData> extractRussianWords(Element node, List<String> additionalDomains, List<List<LexemeToWordData>> aspectGroups, String reportingId) {

		final String wordGroupExp = "x:xp/x:xg";
		final String wordExp = "x:x";
		final String registerExp = "x:s";
		final String governmentExp = "x:vrek";
		final String domainExp = "x:v";
		final String aspectValueExp = "x:aspg/x:aspvst";

		List<LexemeToWordData> dataList = new ArrayList<>();
		List<Element> wordGroupNodes = node.selectNodes(wordGroupExp);
		for (Element wordGroupNode : wordGroupNodes) {
			String word = extractAsString(wordGroupNode, wordExp);
			String aspectWord = extractAsString(wordGroupNode, aspectValueExp);
			LexemeToWordData wordData = new LexemeToWordData();
			wordData.word = cleanUp(word);

			if (StringUtils.isBlank(wordData.word)) continue;

			wordData.displayForm = word;
			wordData.reportingId = reportingId;
			wordData.register = extractAsString(wordGroupNode, registerExp);
			wordData.government = extractAsString(wordGroupNode, governmentExp);
			String domainCode = extractAsString(wordGroupNode, domainExp);
			if (domainCode != null) {
				additionalDomains.add(domainCode);
			}
			boolean wordHasAspect = StringUtils.isNotBlank(aspectWord);
			if (wordHasAspect) {
				wordData.aspect = calculateAspectType(word);
			}
			dataList.add(wordData);

			if (wordHasAspect) {
				LexemeToWordData aspectData = new LexemeToWordData();
				aspectData.aspect = calculateAspectType(aspectWord);
				aspectData.word = cleanUp(aspectWord);
				aspectData.displayForm = aspectWord;
				aspectData.reportingId = reportingId;
				dataList.add(aspectData);
				List<LexemeToWordData> aspectGroup = new ArrayList<>();
				aspectGroup.add(wordData);
				aspectGroup.add(aspectData);
				aspectGroups.add(aspectGroup);
			}
		}
		return dataList;
	}

	private String calculateAspectType(String word) {
		if (word.endsWith("[*]")) {
			return ASPECT_TYPE_SOV_NESOV;
		} else if (word.endsWith("*")) {
			return ASPECT_TYPE_SOV;
		}
		return ASPECT_TYPE_NESOV;
	}

	private String extractAsString(Element node, String xpathExp) {
		Element wordNode = (Element) node.selectSingleNode(xpathExp);
		return wordNode == null ? null : cleanEkiEntityMarkup(wordNode.getTextTrim());
	}

	private boolean isNotWordInSs1(String word) {

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("word", word);
		paramMap.put("dataset", "ss1");
		List<Map<String, Object>> words = basicDbService.queryList(sqlSelectWordByDataset, paramMap);
		return CollectionUtils.isEmpty(words);
	}

	private Map<String, Object> findExistingLexemeForWord(Long wordId, int level1) throws Exception {

		Map<String, Object> params = new HashMap<>();
		params.put("word_id", wordId);
		params.put("level1", level1);
		params.put("dataset_code", getDataset());
		List<Map<String, Object>> lexemes = basicDbService.selectAll(LEXEME, params);
		return lexemes.size() == 1 ? lexemes.get(0) : null;
	}

	private Map<String, Object> findExistingLexemeForWordAndMeaning(Long wordId, Long meaningId) throws Exception {

		Map<String, Object> params = new HashMap<>();
		params.put("word_id", wordId);
		params.put("level1", 1);
		params.put("meaning_id", meaningId);
		params.put("dataset_code", getDataset());
		return basicDbService.select(LEXEME, params);
	}

}
