/*
 * This file is generated by jOOQ.
*/
package eki.wordweb.data.db;


import javax.annotation.Generated;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.impl.DSL;

import eki.wordweb.data.db.tables.Dblink;
import eki.wordweb.data.db.tables.DblinkFetch;
import eki.wordweb.data.db.tables.DblinkGetPkey;
import eki.wordweb.data.db.tables.DblinkGetResult;
import eki.wordweb.data.db.tables.MviewWwClassifier;
import eki.wordweb.data.db.tables.MviewWwDataset;
import eki.wordweb.data.db.tables.MviewWwForm;
import eki.wordweb.data.db.tables.MviewWwLexeme;
import eki.wordweb.data.db.tables.MviewWwMeaning;
import eki.wordweb.data.db.tables.MviewWwWord;
import eki.wordweb.data.db.tables.records.DblinkFetchRecord;
import eki.wordweb.data.db.tables.records.DblinkGetPkeyRecord;
import eki.wordweb.data.db.tables.records.DblinkGetResultRecord;
import eki.wordweb.data.db.tables.records.DblinkRecord;


/**
 * Convenience access to all tables in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>public.dblink</code>.
     */
    public static final Dblink DBLINK = eki.wordweb.data.db.tables.Dblink.DBLINK;

    /**
     * Call <code>public.dblink</code>.
     */
    public static Result<DblinkRecord> DBLINK(Configuration configuration, String __1, String __2) {
        return DSL.using(configuration).selectFrom(eki.wordweb.data.db.tables.Dblink.DBLINK.call(__1, __2)).fetch();
    }

    /**
     * Get <code>public.dblink</code> as a table.
     */
    public static Dblink DBLINK(String __1, String __2) {
        return eki.wordweb.data.db.tables.Dblink.DBLINK.call(__1, __2);
    }

    /**
     * Get <code>public.dblink</code> as a table.
     */
    public static Dblink DBLINK(Field<String> __1, Field<String> __2) {
        return eki.wordweb.data.db.tables.Dblink.DBLINK.call(__1, __2);
    }

    /**
     * The table <code>public.dblink_fetch</code>.
     */
    public static final DblinkFetch DBLINK_FETCH = eki.wordweb.data.db.tables.DblinkFetch.DBLINK_FETCH;

    /**
     * Call <code>public.dblink_fetch</code>.
     */
    public static Result<DblinkFetchRecord> DBLINK_FETCH(Configuration configuration, String __1, Integer __2, Boolean __3) {
        return DSL.using(configuration).selectFrom(eki.wordweb.data.db.tables.DblinkFetch.DBLINK_FETCH.call(__1, __2, __3)).fetch();
    }

    /**
     * Get <code>public.dblink_fetch</code> as a table.
     */
    public static DblinkFetch DBLINK_FETCH(String __1, Integer __2, Boolean __3) {
        return eki.wordweb.data.db.tables.DblinkFetch.DBLINK_FETCH.call(__1, __2, __3);
    }

    /**
     * Get <code>public.dblink_fetch</code> as a table.
     */
    public static DblinkFetch DBLINK_FETCH(Field<String> __1, Field<Integer> __2, Field<Boolean> __3) {
        return eki.wordweb.data.db.tables.DblinkFetch.DBLINK_FETCH.call(__1, __2, __3);
    }

    /**
     * The table <code>public.dblink_get_pkey</code>.
     */
    public static final DblinkGetPkey DBLINK_GET_PKEY = eki.wordweb.data.db.tables.DblinkGetPkey.DBLINK_GET_PKEY;

    /**
     * Call <code>public.dblink_get_pkey</code>.
     */
    public static Result<DblinkGetPkeyRecord> DBLINK_GET_PKEY(Configuration configuration, String __1) {
        return DSL.using(configuration).selectFrom(eki.wordweb.data.db.tables.DblinkGetPkey.DBLINK_GET_PKEY.call(__1)).fetch();
    }

    /**
     * Get <code>public.dblink_get_pkey</code> as a table.
     */
    public static DblinkGetPkey DBLINK_GET_PKEY(String __1) {
        return eki.wordweb.data.db.tables.DblinkGetPkey.DBLINK_GET_PKEY.call(__1);
    }

    /**
     * Get <code>public.dblink_get_pkey</code> as a table.
     */
    public static DblinkGetPkey DBLINK_GET_PKEY(Field<String> __1) {
        return eki.wordweb.data.db.tables.DblinkGetPkey.DBLINK_GET_PKEY.call(__1);
    }

    /**
     * The table <code>public.dblink_get_result</code>.
     */
    public static final DblinkGetResult DBLINK_GET_RESULT = eki.wordweb.data.db.tables.DblinkGetResult.DBLINK_GET_RESULT;

    /**
     * Call <code>public.dblink_get_result</code>.
     */
    public static Result<DblinkGetResultRecord> DBLINK_GET_RESULT(Configuration configuration, String __1, Boolean __2) {
        return DSL.using(configuration).selectFrom(eki.wordweb.data.db.tables.DblinkGetResult.DBLINK_GET_RESULT.call(__1, __2)).fetch();
    }

    /**
     * Get <code>public.dblink_get_result</code> as a table.
     */
    public static DblinkGetResult DBLINK_GET_RESULT(String __1, Boolean __2) {
        return eki.wordweb.data.db.tables.DblinkGetResult.DBLINK_GET_RESULT.call(__1, __2);
    }

    /**
     * Get <code>public.dblink_get_result</code> as a table.
     */
    public static DblinkGetResult DBLINK_GET_RESULT(Field<String> __1, Field<Boolean> __2) {
        return eki.wordweb.data.db.tables.DblinkGetResult.DBLINK_GET_RESULT.call(__1, __2);
    }

    /**
     * The table <code>public.mview_ww_classifier</code>.
     */
    public static final MviewWwClassifier MVIEW_WW_CLASSIFIER = eki.wordweb.data.db.tables.MviewWwClassifier.MVIEW_WW_CLASSIFIER;

    /**
     * The table <code>public.mview_ww_dataset</code>.
     */
    public static final MviewWwDataset MVIEW_WW_DATASET = eki.wordweb.data.db.tables.MviewWwDataset.MVIEW_WW_DATASET;

    /**
     * The table <code>public.mview_ww_form</code>.
     */
    public static final MviewWwForm MVIEW_WW_FORM = eki.wordweb.data.db.tables.MviewWwForm.MVIEW_WW_FORM;

    /**
     * The table <code>public.mview_ww_lexeme</code>.
     */
    public static final MviewWwLexeme MVIEW_WW_LEXEME = eki.wordweb.data.db.tables.MviewWwLexeme.MVIEW_WW_LEXEME;

    /**
     * The table <code>public.mview_ww_meaning</code>.
     */
    public static final MviewWwMeaning MVIEW_WW_MEANING = eki.wordweb.data.db.tables.MviewWwMeaning.MVIEW_WW_MEANING;

    /**
     * The table <code>public.mview_ww_word</code>.
     */
    public static final MviewWwWord MVIEW_WW_WORD = eki.wordweb.data.db.tables.MviewWwWord.MVIEW_WW_WORD;
}
