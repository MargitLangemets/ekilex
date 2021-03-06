/*
 * This file is generated by jOOQ.
*/
package eki.ekilex.data.db.tables.records;


import eki.ekilex.data.db.tables.ViewWwWordRelation;
import eki.ekilex.data.db.udt.records.TypeWordRelationRecord;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ViewWwWordRelationRecord extends TableRecordImpl<ViewWwWordRelationRecord> implements Record2<Long, TypeWordRelationRecord[]> {

    private static final long serialVersionUID = 1917179230;

    /**
     * Setter for <code>public.view_ww_word_relation.word_id</code>.
     */
    public void setWordId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.view_ww_word_relation.word_id</code>.
     */
    public Long getWordId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.view_ww_word_relation.related_words</code>.
     */
    public void setRelatedWords(TypeWordRelationRecord... value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.view_ww_word_relation.related_words</code>.
     */
    public TypeWordRelationRecord[] getRelatedWords() {
        return (TypeWordRelationRecord[]) get(1);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<Long, TypeWordRelationRecord[]> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<Long, TypeWordRelationRecord[]> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return ViewWwWordRelation.VIEW_WW_WORD_RELATION.WORD_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<TypeWordRelationRecord[]> field2() {
        return ViewWwWordRelation.VIEW_WW_WORD_RELATION.RELATED_WORDS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getWordId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeWordRelationRecord[] component2() {
        return getRelatedWords();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getWordId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeWordRelationRecord[] value2() {
        return getRelatedWords();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewWwWordRelationRecord value1(Long value) {
        setWordId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewWwWordRelationRecord value2(TypeWordRelationRecord... value) {
        setRelatedWords(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewWwWordRelationRecord values(Long value1, TypeWordRelationRecord[] value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ViewWwWordRelationRecord
     */
    public ViewWwWordRelationRecord() {
        super(ViewWwWordRelation.VIEW_WW_WORD_RELATION);
    }

    /**
     * Create a detached, initialised ViewWwWordRelationRecord
     */
    public ViewWwWordRelationRecord(Long wordId, TypeWordRelationRecord[] relatedWords) {
        super(ViewWwWordRelation.VIEW_WW_WORD_RELATION);

        set(0, wordId);
        set(1, relatedWords);
    }
}
