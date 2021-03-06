package eki.ekilex.runner;

import static java.util.stream.Collectors.toMap;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import eki.common.constant.FormMode;
import eki.common.constant.FreeformType;
import eki.common.constant.LifecycleEntity;
import eki.common.constant.LifecycleEventType;
import eki.common.constant.LifecycleLogOwner;
import eki.common.constant.LifecycleProperty;
import eki.common.constant.ReferenceType;
import eki.common.constant.SourceType;
import eki.common.constant.TableName;
import eki.common.constant.WordRelationGroupType;
import eki.common.data.Count;
import eki.common.data.PgVarcharArray;
import eki.common.service.db.BasicDbService;
import eki.ekilex.constant.SystemConstant;
import eki.ekilex.data.transform.Form;
import eki.ekilex.data.transform.Guid;
import eki.ekilex.data.transform.Lexeme;
import eki.ekilex.data.transform.Meaning;
import eki.ekilex.data.transform.Paradigm;
import eki.ekilex.data.transform.Source;
import eki.ekilex.data.transform.Usage;
import eki.ekilex.data.transform.UsageTranslation;
import eki.ekilex.data.transform.Word;
import eki.ekilex.service.XmlReader;

public abstract class AbstractLoaderRunner implements InitializingBean, SystemConstant, TableName {

	private static Logger logger = LoggerFactory.getLogger(AbstractLoaderRunner.class);

	abstract String getDataset();

	private static final String SQL_SELECT_WORD_BY_FORM_AND_HOMONYM = "sql/select_word_by_form_and_homonym.sql";

	private static final String SQL_SELECT_WORD_BY_DATASET_AND_GUID = "sql/select_word_by_dataset_and_guid.sql";

	private static final String SQL_SELECT_WORD_MAX_HOMONYM = "sql/select_word_max_homonym.sql";

	private static final String SQL_SELECT_LEXEME_FREEFORM_BY_TYPE_AND_VALUE = "sql/select_lexeme_freeform_by_type_and_value.sql";

	private static final String SQL_SELECT_SOURCE_BY_TYPE_AND_NAME = "sql/select_source_by_type_and_name.sql";

	private static final String SQL_SELECT_WORD_GROUP_WITH_MEMBERS = "sql/select_word_group_with_members.sql";

	private static final String CLASSIFIERS_MAPPING_FILE_PATH = "./fileresources/csv/classifier-main-map.csv";

	private static final char[] RESERVED_DIACRITIC_CHARS = new char[] {'õ', 'ä', 'ö', 'ü', 'š', 'ž', 'Õ', 'Ä', 'Ö', 'Ü', 'Š', 'Ž'};

	private static final String[] DISCLOSED_DIACRITIC_LANGS = new String[] {"rus"};

	protected static final String EKI_CLASSIFIER_STAATUS = "staatus";
	protected static final String EKI_CLASSIFIER_MÕISTETÜÜP = "mõistetüüp";
	protected static final String EKI_CLASSIFIER_KEELENDITÜÜP = "keelenditüüp";
	protected static final String EKI_CLASSIFIER_LIIKTYYP = "liik_tyyp";
	protected static final String EKI_CLASSIFIER_DKTYYP = "dk_tyyp";
	protected static final String EKI_CLASSIFIER_SLTYYP = "sl_tyyp";
	protected static final String EKI_CLASSIFIER_ASTYYP = "as_tyyp";
	protected static final String EKI_CLASSIFIER_VKTYYP = "vk_tyyp";
	protected static final String EKI_CLASSIFIER_MSAGTYYP = "msag_tyyp";
	protected static final String EKI_CLASSIFIER_STYYP = "s_tyyp";
	protected static final String EKI_CLASSIFIER_ETYMPLTYYP = "etympl_tyyp";
	protected static final String EKI_CLASSIFIER_ENTRY_CLASS = "entry class";

	@Autowired
	protected XmlReader xmlReader;

	@Autowired
	protected BasicDbService basicDbService;

	protected String sqlSelectWordByFormAndHomonym;

	protected String sqlSelectWordByDatasetAndGuid;

	protected String sqlSelectWordMaxHomonym;

	protected String sqlSelectLexemeFreeform;

	protected String sqlSourceByTypeAndName;

	protected String sqlWordGroupWithMembers;

	private Pattern ekiEntityPatternV;

	abstract void initialise() throws Exception;

	@Override
	public void afterPropertiesSet() throws Exception {

		initialise();

		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream resourceFileInputStream;

		resourceFileInputStream = classLoader.getResourceAsStream(SQL_SELECT_WORD_BY_FORM_AND_HOMONYM);
		sqlSelectWordByFormAndHomonym = getContent(resourceFileInputStream);

		resourceFileInputStream = classLoader.getResourceAsStream(SQL_SELECT_WORD_BY_DATASET_AND_GUID);
		sqlSelectWordByDatasetAndGuid = getContent(resourceFileInputStream);

		resourceFileInputStream = classLoader.getResourceAsStream(SQL_SELECT_WORD_MAX_HOMONYM);
		sqlSelectWordMaxHomonym = getContent(resourceFileInputStream);

		resourceFileInputStream = classLoader.getResourceAsStream(SQL_SELECT_LEXEME_FREEFORM_BY_TYPE_AND_VALUE);
		sqlSelectLexemeFreeform = getContent(resourceFileInputStream);

		resourceFileInputStream = classLoader.getResourceAsStream(SQL_SELECT_SOURCE_BY_TYPE_AND_NAME);
		sqlSourceByTypeAndName = getContent(resourceFileInputStream);

		resourceFileInputStream = classLoader.getResourceAsStream(SQL_SELECT_WORD_GROUP_WITH_MEMBERS);
		sqlWordGroupWithMembers = getContent(resourceFileInputStream);

		ekiEntityPatternV = Pattern.compile("(&(ehk|Hrl|hrl|ja|jne|jt|ka|nt|puudub|v|vm|vms|vrd|vt|напр.|и др.|и т. п.|г.);)");
	}

	protected String getContent(InputStream resourceInputStream) throws Exception {
		String content = IOUtils.toString(resourceInputStream, UTF_8);
		resourceInputStream.close();
		return content;
	}

	protected boolean isLang(String lang) {
		Locale locale = new Locale(lang);
		String displayName = locale.getDisplayName();
		boolean isLang = !StringUtils.equalsIgnoreCase(lang, displayName);
		return isLang;
	}

	protected String unifyLang(String lang) {
		if (StringUtils.isBlank(lang)) {
			return null;
		}
		Locale locale = new Locale(lang);
		lang = locale.getISO3Language();
		return lang;
	}

	protected String cleanEkiEntityMarkup(String value) {
		if (StringUtils.isBlank(value)) {
			return value;
		}
		Matcher matcher = ekiEntityPatternV.matcher(value);
		if (matcher.find()) {
			value = matcher.replaceAll(matcher.group(2));
		}
		return StringUtils.removePattern(value, "[&]\\w+[;]");
	}

	protected String removeAccents(String value, String lang) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		if (ArrayUtils.contains(DISCLOSED_DIACRITIC_LANGS, lang)) {
			return null;
		}
		boolean isAlreadyClean = Normalizer.isNormalized(value, Normalizer.Form.NFD);
		if (isAlreadyClean) {
			return null;
		}
		StringBuffer cleanValueBuf = new StringBuffer();
		char[] chars = value.toCharArray();
		String decomposedChars;
		String charAsStr;
		char primaryChar;
		for (char c : chars) {
			boolean isReservedChar = ArrayUtils.contains(RESERVED_DIACRITIC_CHARS, c);
			if (isReservedChar) {
				cleanValueBuf.append(c);
			} else {
				charAsStr = Character.toString(c);
				decomposedChars = Normalizer.normalize(charAsStr, Normalizer.Form.NFD);
				if (decomposedChars.length() > 1) {
					primaryChar = decomposedChars.charAt(0);
					cleanValueBuf.append(primaryChar);
				} else {
					cleanValueBuf.append(c);
				}
			}
		}
		String cleanValue = cleanValueBuf.toString();
		if (StringUtils.equals(value, cleanValue)) {
			return null;
		}
		return cleanValue;
	}

	protected Long createOrSelectWord(Word word, List<Paradigm> paradigms, String dataset, Count reusedWordCount) throws Exception {

		String wordValue = word.getValue();
		String wordLang = word.getLang();
		String[] wordComponents = word.getComponents();
		String wordDisplayForm = word.getDisplayForm();
		String wordVocalForm = word.getVocalForm();
		int homonymNr = word.getHomonymNr();
		String wordMorphCode = word.getMorphCode();
		String wordDisplayMorph = word.getDisplayMorph();
		String guid = word.getGuid();
		String genderCode = word.getGenderCode();
		String typeCode = word.getWordTypeCode();
		String aspectCode = word.getAspectTypeCode();

		Map<String, Object> tableRowValueMap = getWord(wordValue, homonymNr, wordLang);
		Long wordId;

		if (tableRowValueMap == null) {
			wordId = createWord(wordValue, wordMorphCode, homonymNr, wordLang, wordDisplayMorph, genderCode, typeCode, aspectCode);
			if (StringUtils.isNotBlank(dataset) && StringUtils.isNotBlank(guid)) {
				createWordGuid(wordId, dataset, guid);
			}
			if (CollectionUtils.isEmpty(paradigms)) {
				Long paradigmId = createParadigm(wordId, null, false);
				createFormWithAsWord(paradigmId, wordValue, wordLang, wordComponents, wordDisplayForm, wordVocalForm, wordMorphCode, FormMode.WORD);
			}
		} else {
			wordId = (Long) tableRowValueMap.get("id");
			if (reusedWordCount != null) {
				reusedWordCount.increment();
			}
		}

		word.setId(wordId);
		if (CollectionUtils.isNotEmpty(paradigms)) {
			for (Paradigm paradigm : paradigms) {
				Long paradigmId = createParadigm(wordId, paradigm.getInflectionTypeNr(), paradigm.isSecondary());
				// mab forms
				List<Form> forms = paradigm.getForms();
				if (CollectionUtils.isEmpty(forms)) {
					createFormWithAsWord(paradigmId, wordValue, wordLang, wordComponents, wordDisplayForm, wordVocalForm, wordMorphCode, FormMode.WORD);
				} else {
					for (Form form : forms) {
						if (form.getMode().equals(FormMode.WORD)) {
							createFormWithAsWord(paradigmId, wordValue, wordLang, null, wordDisplayForm, wordVocalForm, form.getMorphCode(), form.getMode());
						} else {
							createForm(paradigmId, form.getValue(), null, form.getDisplayForm(), null, form.getMorphCode(), form.getMode());
						}
					}
				}
			}
		}
		return wordId;
	}

	protected Long createOrSelectWord(
			Word word, List<Paradigm> paradigms, String dataset, Map<String, List<Guid>> ssGuidMap,
			Count ssWordCount, Count reusedWordCount) throws Exception {

		if (MapUtils.isEmpty(ssGuidMap)) {
			return createOrSelectWord(word, paradigms, dataset, reusedWordCount);
		}

		String wordValue = word.getValue();
		String guid = word.getGuid();

		List<Guid> mappedGuids = ssGuidMap.get(guid);
		if (CollectionUtils.isEmpty(mappedGuids)) {
			return createOrSelectWord(word, paradigms, dataset, reusedWordCount);
		}

		for (Guid ssGuidObj : mappedGuids) {

			String ssWordValue = ssGuidObj.getWord();
			String ssGuid = ssGuidObj.getValue();
			String ssDataset = "ss1";

			if (StringUtils.equalsIgnoreCase(wordValue, ssWordValue)) {
				List<Map<String, Object>> tableRowValueMaps = getWord(wordValue, ssGuid, ssDataset);
				Map<String, Object> tableRowValueMap = null;
				if (CollectionUtils.size(tableRowValueMaps) == 1) {
					tableRowValueMap = tableRowValueMaps.get(0);
				} else if (CollectionUtils.size(tableRowValueMaps) > 1) {
					tableRowValueMap = tableRowValueMaps.get(0);
					logger.warn("There are multiple words with same value and guid in {}: \"{}\" - \"{}\"", ssDataset, wordValue, ssGuid);
				}
				if (tableRowValueMap == null) {
					return createOrSelectWord(word, paradigms, dataset, reusedWordCount);
				}
				ssWordCount.increment();
				Long wordId = (Long) tableRowValueMap.get("id");
				word.setId(wordId);
				return wordId;
			}
		}
		List<String> mappedWordValues = mappedGuids.stream().map(Guid::getWord).collect(Collectors.toList());
		logger.debug("Word value doesn't match guid mapping(s): \"{}\" / \"{}\"", wordValue, mappedWordValues);

		return createOrSelectWord(word, paradigms, dataset, reusedWordCount);
	}

	private Map<String, Object> getWord(String word, int homonymNr, String lang) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("word", word);
		tableRowParamMap.put("homonymNr", homonymNr);
		tableRowParamMap.put("lang", lang);
		Map<String, Object> tableRowValueMap = basicDbService.queryForMap(sqlSelectWordByFormAndHomonym, tableRowParamMap);
		return tableRowValueMap;
	}

	private List<Map<String, Object>> getWord(String word, String guid, String dataset) throws Exception {

		guid = guid.toLowerCase();

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("word", word);
		tableRowParamMap.put("guid", guid);
		tableRowParamMap.put("dataset", dataset);
		List<Map<String, Object>> tableRowValueMaps = basicDbService.queryList(sqlSelectWordByDatasetAndGuid, tableRowParamMap);
		return tableRowValueMaps;
	}

	private void createFormWithAsWord(Long paradigmId, String form, String lang, String[] wordComponents, String wordDisplayForm, String wordVocalForm, String morphCode, FormMode mode) throws Exception {

		createForm(paradigmId, form, wordComponents, wordDisplayForm, wordVocalForm, morphCode, mode);
		if (mode.equals(FormMode.WORD)) {
			String asWordValue = removeAccents(form, lang);
			if (StringUtils.isNotBlank(asWordValue)) {
				createForm(paradigmId, asWordValue, null, null, null, morphCode, FormMode.AS_WORD);
			}
		}
	}

	private void createForm(Long paradigmId, String form, String[] wordComponents, String wordDisplayForm, String wordVocalForm, String morphCode, FormMode mode) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("paradigm_id", paradigmId);
		tableRowParamMap.put("morph_code", morphCode);
		tableRowParamMap.put("mode", mode.name());
		tableRowParamMap.put("value", form);
		if (wordComponents != null) {
			tableRowParamMap.put("components", new PgVarcharArray(wordComponents));
		}
		if (StringUtils.isNotBlank(wordDisplayForm)) {
			tableRowParamMap.put("display_form", wordDisplayForm);
		}
		if (StringUtils.isNotBlank(wordVocalForm)) {
			tableRowParamMap.put("vocal_form", wordVocalForm);
		}
		basicDbService.create(FORM, tableRowParamMap);
	}

	private Long createParadigm(Long wordId, String inflectionTypeNr, boolean isSecondary) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("word_id", wordId);
		tableRowParamMap.put("is_secondary", isSecondary);
		if (inflectionTypeNr != null) {
			tableRowParamMap.put("inflection_type_nr", inflectionTypeNr);
		}
		Long paradigmId = basicDbService.create(PARADIGM, tableRowParamMap);
		return paradigmId;
	}

	private Long createWord(String word, final String morphCode, final int homonymNr, String lang, String displayMorph, String genderCode, String typeCode, String aspectCode) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("lang", lang);
		tableRowParamMap.put("morph_code", morphCode);
		tableRowParamMap.put("homonym_nr", homonymNr);
		tableRowParamMap.put("display_morph_code", displayMorph);
		tableRowParamMap.put("gender_code", genderCode);
		tableRowParamMap.put("type_code", typeCode);
		tableRowParamMap.put("aspect_code", aspectCode);
		Long wordId = basicDbService.create(WORD, tableRowParamMap);
		createLifecycleLog(LifecycleLogOwner.WORD, wordId, LifecycleEventType.CREATE, LifecycleEntity.WORD, LifecycleProperty.VALUE, wordId, word);
		return wordId;
	}

	private void createWordGuid(Long wordId, String dataset, String guid) throws Exception {

		guid = guid.toLowerCase();

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("word_id", wordId);
		tableRowParamMap.put("dataset_code", dataset);
		tableRowParamMap.put("guid", guid);
		basicDbService.create(WORD_GUID, tableRowParamMap);
	}

	protected int getWordMaxHomonymNr(String word, String lang) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("word", word);
		tableRowParamMap.put("lang", lang);
		tableRowParamMap.put("mode", FormMode.WORD.name());
		Map<String, Object> tableRowValueMap = basicDbService.queryForMap(sqlSelectWordMaxHomonym, tableRowParamMap);
		if (MapUtils.isEmpty(tableRowValueMap)) {
			return 0;
		}
		Object result = tableRowValueMap.get("max_homonym_nr");
		if (result == null) {
			return 0;
		}
		int homonymNr = (int) result;
		return homonymNr;
	}

	protected Long createMeaning(Meaning meaning) throws Exception {

		Map<String, Object> tableRowParamMap;

		tableRowParamMap = new HashMap<>();
		Timestamp createdOn = meaning.getCreatedOn();
		if (createdOn != null) {
			tableRowParamMap.put("created_on", createdOn);
		}
		String createdBy = meaning.getCreatedBy();
		if (StringUtils.isNotBlank(createdBy)) {
			tableRowParamMap.put("created_by", createdBy);
		}
		Timestamp modifiedOn = meaning.getModifiedOn();
		if (modifiedOn != null) {
			tableRowParamMap.put("modified_on", modifiedOn);
		}
		String modifiedBy = meaning.getModifiedBy();
		if (StringUtils.isNotBlank(modifiedBy)) {
			tableRowParamMap.put("modified_by", modifiedBy);
		}
		String processStateCode = meaning.getProcessStateCode();
		if (StringUtils.isNotBlank(processStateCode)) {
			tableRowParamMap.put("process_state_code", processStateCode);
		}
		Long meaningId;
		if (MapUtils.isEmpty(tableRowParamMap)) {
			meaningId = basicDbService.create(MEANING);
		} else {
			meaningId = basicDbService.create(MEANING, tableRowParamMap);
		}
		meaning.setMeaningId(meaningId);
		return meaningId;
	}

	protected Long createMeaning() throws Exception {

		Long meaningId = basicDbService.create(MEANING);
		return meaningId;
	}

	protected void createMeaningDomain(Long meaningId, String domainCode, String domainOrigin) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("meaning_id", meaningId);
		tableRowParamMap.put("domain_code", domainCode);
		tableRowParamMap.put("domain_origin", domainOrigin);
		Long meaningDomainId = basicDbService.create(MEANING_DOMAIN, tableRowParamMap);
		createLifecycleLog(LifecycleLogOwner.MEANING, meaningId, LifecycleEventType.CREATE, LifecycleEntity.MEANING, LifecycleProperty.DOMAIN, meaningDomainId, domainCode);
	}

	protected Long createDefinition(Long meaningId, String definition, String lang, String dataset) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("meaning_id", meaningId);
		tableRowParamMap.put("value", definition);
		tableRowParamMap.put("lang", lang);
		Long definitionId = basicDbService.create(DEFINITION, tableRowParamMap);
		createLifecycleLog(LifecycleLogOwner.MEANING, meaningId, LifecycleEventType.CREATE, LifecycleEntity.DEFINITION, LifecycleProperty.VALUE, definitionId, definition);
		if (definitionId != null) {
			tableRowParamMap.clear();
			tableRowParamMap.put("definition_id", definitionId);
			tableRowParamMap.put("dataset_code", dataset);
			basicDbService.createWithoutId(DEFINITION_DATASET, tableRowParamMap);
		}
		return definitionId;
	}

	protected Long createLexeme(Lexeme lexeme, String dataset) throws Exception {

		Long wordId = lexeme.getWordId();
		Long meaningId = lexeme.getMeaningId();
		String createdBy = lexeme.getCreatedBy();
		Timestamp createdOn = lexeme.getCreatedOn();
		String modifiedBy = lexeme.getModifiedBy();
		Timestamp modifiedOn = lexeme.getModifiedOn();
		Integer lexemeLevel1 = lexeme.getLevel1();
		Integer lexemeLevel2 = lexeme.getLevel2();
		Integer lexemeLevel3 = lexeme.getLevel3();
		String frequencyGroup = lexeme.getFrequencyGroup();
		String valueStateCode = lexeme.getValueStateCode();
		String processStateCode = lexeme.getProcessStateCode();

		Map<String, Object> criteriaParamMap = new HashMap<>();
		criteriaParamMap.put("word_id", wordId);
		criteriaParamMap.put("meaning_id", meaningId);
		criteriaParamMap.put("dataset_code", dataset);
		Long lexemeId = basicDbService.createIfNotExists(LEXEME, criteriaParamMap);
		lexeme.setLexemeId(lexemeId);
		if (lexemeId != null) {
			criteriaParamMap.clear();
			criteriaParamMap.put("id", lexemeId);
			Map<String, Object> valueParamMap = new HashMap<>();
			if (StringUtils.isNotBlank(createdBy)) {
				valueParamMap.put("created_by", createdBy);
			}
			if (createdOn != null) {
				valueParamMap.put("created_on", createdOn);
			}
			if (StringUtils.isNotBlank(modifiedBy)) {
				valueParamMap.put("modified_by", modifiedBy);
			}
			if (modifiedOn != null) {
				valueParamMap.put("modified_on", modifiedOn);
			}
			if (lexemeLevel1 != null) {
				valueParamMap.put("level1", lexemeLevel1);
			}
			if (lexemeLevel2 != null) {
				valueParamMap.put("level2", lexemeLevel2);
			}
			if (lexemeLevel3 != null) {
				valueParamMap.put("level3", lexemeLevel3);
			}
			if (StringUtils.isNotBlank(frequencyGroup)) {
				valueParamMap.put("frequency_group", frequencyGroup);
			}
			if (StringUtils.isNotBlank(valueStateCode)) {
				valueParamMap.put("value_state_code", valueStateCode);
			}
			if (StringUtils.isNotBlank(processStateCode)) {
				valueParamMap.put("process_state_code", processStateCode);
			}
			if (MapUtils.isNotEmpty(valueParamMap)) {
				basicDbService.update(LEXEME, criteriaParamMap, valueParamMap);
			}
		}
		return lexemeId;
	}

	protected void createUsages(Long lexemeId, List<Usage> usages, String dataLang) throws Exception {

		if (CollectionUtils.isEmpty(usages)) {
			return;
		}

		for (Usage usage : usages) {
			String usageValue = usage.getValue();
			String usageType = usage.getUsageType();
			String author = usage.getAuthor();
			String authorTypeStr = usage.getAuthorType();
			String extSourceId = usage.getExtSourceId();
			Long usageId = createLexemeFreeform(lexemeId, FreeformType.USAGE, usageValue, dataLang);
			if (StringUtils.isNotBlank(usageType)) {
				createFreeformClassifier(FreeformType.USAGE_TYPE, usageId, usageType);
			}
			if (StringUtils.isBlank(extSourceId)) {
				extSourceId = "n/a";
			}
			if (StringUtils.isNotBlank(author)) {
				Long authorId = getSource(SourceType.PERSON, extSourceId, author);
				if (authorId == null) {
					authorId = createSource(SourceType.PERSON, extSourceId, author);
				}
				ReferenceType referenceType;
				if (StringUtils.isEmpty(authorTypeStr)) {
					referenceType = ReferenceType.AUTHOR;
				} else {
					referenceType = ReferenceType.TRANSLATOR;
				}
				Long freeformSourceLinkId = createFreeformSourceLink(usageId, referenceType, authorId, null, author);
				createLifecycleLog(LifecycleLogOwner.LEXEME, lexemeId, LifecycleEventType.CREATE, LifecycleEntity.FREEFORM_SOURCE_LINK, LifecycleProperty.VALUE, freeformSourceLinkId, author);
			}
			if (CollectionUtils.isNotEmpty(usage.getDefinitions())) {
				for (String usageDefinition : usage.getDefinitions()) {
					Long usageDefinitionId = createFreeformTextOrDate(FreeformType.USAGE_DEFINITION, usageId, usageDefinition, dataLang);
					createLifecycleLog(LifecycleLogOwner.LEXEME, lexemeId, LifecycleEventType.CREATE, LifecycleEntity.USAGE_DEFINITION, LifecycleProperty.VALUE, usageDefinitionId, usageDefinition);
				}
			}
			if (CollectionUtils.isNotEmpty(usage.getUsageTranslations())) {
				for (UsageTranslation usageTranslation : usage.getUsageTranslations()) {
					String usageTranslationValue = usageTranslation.getValue();
					String usageTranslationLang = usageTranslation.getLang();
					Long usageTranslationId = createFreeformTextOrDate(FreeformType.USAGE_TRANSLATION, usageId, usageTranslationValue, usageTranslationLang);
					createLifecycleLog(
							LifecycleLogOwner.LEXEME, lexemeId, LifecycleEventType.CREATE, LifecycleEntity.USAGE_TRANSLATION, LifecycleProperty.VALUE, usageTranslationId, usageTranslationValue);
				}
			}
		}
	}

	protected Long createLexemeFreeform(Long lexemeId, FreeformType freeformType, Object value, String lang) throws Exception {

		Long freeformId = createFreeformTextOrDate(freeformType, null, value, lang);

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("lexeme_id", lexemeId);
		tableRowParamMap.put("freeform_id", freeformId);
		basicDbService.create(LEXEME_FREEFORM, tableRowParamMap);

		try {
			LifecycleEntity lifecycleEntity = LifecycleEntity.valueOf(freeformType.name());
			createLifecycleLog(LifecycleLogOwner.LEXEME, lexemeId, LifecycleEventType.CREATE, lifecycleEntity, LifecycleProperty.VALUE, freeformId, value.toString());
		} catch (Exception e) {
		}

		return freeformId;
	}

	protected Long createOrSelectLexemeFreeform(Long lexemeId, FreeformType freeformType, String freeformValue) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("lexeme_id", lexemeId);
		tableRowParamMap.put("value", freeformValue);
		tableRowParamMap.put("type", freeformType.name());
		Map<String, Object> freeform = basicDbService.queryForMap(sqlSelectLexemeFreeform, tableRowParamMap);
		Long freeformId;
		if (freeform == null) {
			freeformId = createLexemeFreeform(lexemeId, freeformType, freeformValue, null);
		} else {
			freeformId = (Long) freeform.get("id");
		}
		return freeformId;
	}

	protected Long createMeaningFreeform(Long meaningId, FreeformType freeformType, Object value) throws Exception {

		Long freeformId = createFreeformTextOrDate(freeformType, null, value, null);

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("meaning_id", meaningId);
		tableRowParamMap.put("freeform_id", freeformId);
		basicDbService.create(MEANING_FREEFORM, tableRowParamMap);

		try {
			LifecycleEntity lifecycleEntity = LifecycleEntity.valueOf(freeformType.name());
			createLifecycleLog(LifecycleLogOwner.MEANING, meaningId, LifecycleEventType.CREATE, lifecycleEntity, LifecycleProperty.VALUE, freeformId, value.toString());
		} catch (Exception e) {
		}

		return freeformId;
	}

	protected Long createDefinitionFreeform(Long definitionId, FreeformType freeformType, Object value) throws Exception {

		Long freeformId = createFreeformTextOrDate(freeformType, null, value, null);

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("definition_id", definitionId);
		tableRowParamMap.put("freeform_id", freeformId);
		basicDbService.create(DEFINITION_FREEFORM, tableRowParamMap);

		return freeformId;
	}

	protected Long createFreeformTextOrDate(FreeformType freeformType, Long parentId, Object value, String lang) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("type", freeformType.name());
		if (parentId != null) {
			tableRowParamMap.put("parent_id", parentId);
		}
		if (value != null) {
			if (value instanceof String) {
				tableRowParamMap.put("value_text", value);
			} else if (value instanceof Timestamp) {
				tableRowParamMap.put("value_date", value);
			} else {
				throw new Exception("Not yet supported freeform data type " + value);
			}
		}
		if (StringUtils.isNotBlank(lang)) {
			tableRowParamMap.put("lang", lang);
		}
		Long freeformId = basicDbService.create(FREEFORM, tableRowParamMap);
		return freeformId;
	}

	protected Long createFreeformClassifier(FreeformType freeformType, Long parentId, String classifierCode) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("type", freeformType.name());
		if (parentId != null) {
			tableRowParamMap.put("parent_id", parentId);
		}
		tableRowParamMap.put("classif_code", classifierCode);
		return basicDbService.create(FREEFORM, tableRowParamMap);
	}

	protected void updateFreeformText(Long freeformId, String value) throws Exception {

		Map<String, Object> criteriaParamMap = new HashMap<>();
		criteriaParamMap.put("id", freeformId);
		Map<String, Object> valueParamMap = new HashMap<>();
		valueParamMap.put("value_text", value);
		basicDbService.update(FREEFORM, criteriaParamMap, valueParamMap);
	}

	protected Long createFreeformSourceLink(Long freeformId, ReferenceType refType, Long sourceId, String name, String value) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("freeform_id", freeformId);
		tableRowParamMap.put("type", refType.name());
		tableRowParamMap.put("source_id", sourceId);
		if (StringUtils.isNotBlank(name)) {
			tableRowParamMap.put("name", name);
		}
		if (StringUtils.isNotBlank(value)) {
			tableRowParamMap.put("value", value);
		}
		Long refLinkId = basicDbService.create(FREEFORM_SOURCE_LINK, tableRowParamMap);
		return refLinkId;
	}

	protected Long createLexemeSourceLink(Long lexemeId, ReferenceType refType, Long sourceId, String name, String value) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("lexeme_id", lexemeId);
		tableRowParamMap.put("type", refType.name());
		tableRowParamMap.put("source_id", sourceId);
		if (StringUtils.isNotBlank(name)) {
			tableRowParamMap.put("name", name);
		}
		if (StringUtils.isNotBlank(value)) {
			tableRowParamMap.put("value", value);
		}
		Long sourceLinkId = basicDbService.create(LEXEME_SOURCE_LINK, tableRowParamMap);

		createLifecycleLog(LifecycleLogOwner.LEXEME, lexemeId, LifecycleEventType.CREATE, LifecycleEntity.LEXEME_SOURCE_LINK, LifecycleProperty.VALUE, sourceLinkId, value);

		return sourceLinkId;
	}

	protected Long createDefinitionSourceLink(Long definitionId, ReferenceType refType, Long sourceId, String name, String value) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("definition_id", definitionId);
		tableRowParamMap.put("type", refType.name());
		tableRowParamMap.put("source_id", sourceId);
		if (StringUtils.isNotBlank(name)) {
			tableRowParamMap.put("name", name);
		}
		if (StringUtils.isNotBlank(value)) {
			tableRowParamMap.put("value", value);
		}
		Long refLinkId = basicDbService.create(DEFINITION_SOURCE_LINK, tableRowParamMap);
		return refLinkId;
	}

	protected void createLexemeRelation(Long lexemeId1, Long lexemeId2, String relationType) throws Exception {

		Map<String, Object> relationParams = new HashMap<>();
		relationParams.put("lexeme1_id", lexemeId1);
		relationParams.put("lexeme2_id", lexemeId2);
		relationParams.put("lex_rel_type_code", relationType);
		Long lexemeRelationId = basicDbService.createIfNotExists(LEXEME_RELATION, relationParams);

		if (lexemeRelationId != null) {
			createLifecycleLog(LifecycleLogOwner.LEXEME, lexemeId1, LifecycleEventType.CREATE, LifecycleEntity.LEXEME_RELATION, LifecycleProperty.VALUE, lexemeRelationId, relationType);
		}
	}

	protected boolean hasNoWordRelationGroupWithMembers(WordRelationGroupType groupType, List<Long> memberIds) throws Exception {

		Map<String, Object> params = new HashMap<>();
		params.put("word_rel_type_code", groupType.name());
		params.put("memberIds", memberIds);
		params.put("nrOfMembers", memberIds.size());
		return MapUtils.isEmpty(basicDbService.queryForMap(sqlWordGroupWithMembers, params));
	}

	protected Long createWordRelationGroup(WordRelationGroupType groupType) throws Exception {

		Map<String, Object> params = new HashMap<>();
		params.put("word_rel_type_code", groupType.name());
		return basicDbService.create(WORD_RELATION_GROUP, params);
	}

	protected Long createWordRelationGroupMember(Long groupId, Long lexemeId) throws Exception {

		Map<String, Object> params = new HashMap<>();
		params.put("word_group_id", groupId);
		params.put("word_id", lexemeId);
		return basicDbService.create(WORD_RELATION_GROUP_MEMBER, params);
	}

	protected void createWordRelation(Long wordId1, Long wordId2, String relationType) throws Exception {

		Map<String, Object> relationParams = new HashMap<>();
		relationParams.put("word1_id", wordId1);
		relationParams.put("word2_id", wordId2);
		relationParams.put("word_rel_type_code", relationType);
		Long wordRelationId = basicDbService.createIfNotExists(WORD_RELATION, relationParams);

		if (wordRelationId != null) {
			createLifecycleLog(LifecycleLogOwner.WORD, wordId1, LifecycleEventType.CREATE, LifecycleEntity.WORD_RELATION, LifecycleProperty.VALUE, wordRelationId, relationType);
		}
	}

	protected void createMeaningRelation(Long meaningId1, Long meaningId2, String relationType) throws Exception {

		Map<String, Object> relationParams = new HashMap<>();
		relationParams.put("meaning1_id", meaningId1);
		relationParams.put("meaning2_id", meaningId2);
		relationParams.put("meaning_rel_type_code", relationType);
		Long meaningRelationId = basicDbService.createIfNotExists(MEANING_RELATION, relationParams);

		if (meaningRelationId != null) {
			createLifecycleLog(LifecycleLogOwner.MEANING, meaningId1, LifecycleEventType.CREATE, LifecycleEntity.MEANING_RELATION, LifecycleProperty.VALUE, meaningRelationId, relationType);
		}
	}

	protected void createLexemeRegister(Long lexemeId, String registerCode) throws Exception {

		Map<String, Object> params = new HashMap<>();
		params.put("lexeme_id", lexemeId);
		params.put("register_code", registerCode);
		Long lexemeRegisterId = basicDbService.createIfNotExists(LEXEME_REGISTER, params);

		if (lexemeRegisterId != null) {
			createLifecycleLog(LifecycleLogOwner.LEXEME, lexemeId, LifecycleEventType.CREATE, LifecycleEntity.LEXEME, LifecycleProperty.REGISTER, lexemeRegisterId, registerCode);
		}
	}

	protected Long createSource(Source source) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		String concept = source.getExtSourceId();
		SourceType type = source.getType();
		tableRowParamMap.put("ext_source_id", concept);
		tableRowParamMap.put("type", type.name());
		Timestamp createdOn = source.getCreatedOn();
		if (createdOn != null) {
			tableRowParamMap.put("created_on", createdOn);
		}
		String createdBy = source.getCreatedBy();
		if (StringUtils.isNotBlank(createdBy)) {
			tableRowParamMap.put("created_by", createdBy);
		}
		Timestamp modifiedOn = source.getModifiedOn();
		if (modifiedOn != null) {
			tableRowParamMap.put("modified_on", modifiedOn);
		}
		String modifiedBy = source.getModifiedBy();
		if (StringUtils.isNotBlank(modifiedBy)) {
			tableRowParamMap.put("modified_by", modifiedBy);
		}
		String processStateCode = source.getProcessStateCode();
		if (StringUtils.isNotBlank(processStateCode)) {
			tableRowParamMap.put("process_state_code", processStateCode);
		}
		Long sourceId = basicDbService.create(SOURCE, tableRowParamMap);
		source.setSourceId(sourceId);
		return sourceId;
	}

	protected Long createSource(SourceType sourceType, String extSourceId, String sourceName) throws Exception {

		Source source = new Source();
		source.setType(sourceType);
		source.setExtSourceId(extSourceId);
		Long sourceId = createSource(source);
		createSourceFreeform(sourceId, FreeformType.SOURCE_NAME, sourceName);

		return sourceId;
	}

	protected Long createSourceFreeform(Long sourceId, FreeformType freeformType, Object value) throws Exception {

		Long freeformId = createFreeformTextOrDate(freeformType, null, value, null);

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("source_id", sourceId);
		tableRowParamMap.put("freeform_id", freeformId);
		basicDbService.create(SOURCE_FREEFORM, tableRowParamMap);

		return freeformId;
	}

	protected Long getSource(SourceType sourceType, String extSourceId, String sourceName) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("sourceType", sourceType.name());
		tableRowParamMap.put("extSourceId", extSourceId);
		tableRowParamMap.put("sourcePropertyTypeName", FreeformType.SOURCE_NAME.name());
		tableRowParamMap.put("sourceName", sourceName);
		List<Map<String, Object>> sources = basicDbService.queryList(sqlSourceByTypeAndName, tableRowParamMap);

		if (CollectionUtils.isEmpty(sources)) {
			return null;
		}
		Map<String, Object> sourceRecord = sources.get(0);
		Long sourceId = (Long) sourceRecord.get("id");
		return sourceId;
	}

	protected void createLifecycleLog(
			LifecycleLogOwner logOwner, Long ownerId, LifecycleEventType eventType, LifecycleEntity entity, LifecycleProperty property, Long entityId, String entry) throws Exception {

		String eventBy = "Ekileks " + getDataset() + "-laadur";

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("entity_id", entityId);
		tableRowParamMap.put("entity_name", entity.name());
		tableRowParamMap.put("entity_prop", property.name());
		tableRowParamMap.put("event_type", eventType.name());
		tableRowParamMap.put("event_by", eventBy);
		tableRowParamMap.put("entry", entry);
		Long lifecycleLogId = basicDbService.create(LIFECYCLE_LOG, tableRowParamMap);

		if (LifecycleLogOwner.LEXEME.equals(logOwner)) {
			createLexemeLifecycleLog(ownerId, lifecycleLogId);
		} else if (LifecycleLogOwner.MEANING.equals(logOwner)) {
			createMeaningLifecycleLog(ownerId, lifecycleLogId);
		} else if (LifecycleLogOwner.WORD.equals(logOwner)) {
			createWordLifecycleLog(ownerId, lifecycleLogId);
		}
	}

	private void createMeaningLifecycleLog(Long meaningId, Long lifecycleLogId) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("meaning_id", meaningId);
		tableRowParamMap.put("lifecycle_log_id", lifecycleLogId);
		basicDbService.create(MEANING_LIFECYCLE_LOG, tableRowParamMap);
	}

	private void createWordLifecycleLog(Long wordId, Long lifecycleLogId) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("word_id", wordId);
		tableRowParamMap.put("lifecycle_log_id", lifecycleLogId);
		basicDbService.create(WORD_LIFECYCLE_LOG, tableRowParamMap);
	}

	private void createLexemeLifecycleLog(Long lexemeId, Long lifecycleLogId) throws Exception {

		Map<String, Object> tableRowParamMap = new HashMap<>();
		tableRowParamMap.put("lexeme_id", lexemeId);
		tableRowParamMap.put("lifecycle_log_id", lifecycleLogId);
		basicDbService.create(LEXEME_LIFECYCLE_LOG, tableRowParamMap);
	}

	protected Map<String, String> loadClassifierMappingsFor(String ekiClassifierName) throws Exception {
		return loadClassifierMappingsFor(ekiClassifierName, null);
	}

	protected Map<String, String> loadClassifierMappingsFor(String ekiClassifierName, String lexClassifierName) throws Exception {
		// in case of duplicate keys, last value is used
		return readFileLines(CLASSIFIERS_MAPPING_FILE_PATH).stream()
				.filter(line -> line.startsWith(ekiClassifierName))
				.map(line -> StringUtils.split(line, CSV_SEPARATOR))
				.filter(cells -> lexClassifierName == null || StringUtils.equalsIgnoreCase(lexClassifierName, cells[5]))
				.filter(cells -> "et".equals(cells[4]))
				.filter(cells -> !"-".equals(cells[5]))
				.collect(toMap(cells -> cells[2], cells -> cells[6], (c1, c2) -> c2));
	}

	protected List<String> readFileLines(String sourcePath) throws Exception {
		try (InputStream resourceInputStream = new FileInputStream(sourcePath)) {
			return IOUtils.readLines(resourceInputStream, UTF_8);
		}
	}

}
