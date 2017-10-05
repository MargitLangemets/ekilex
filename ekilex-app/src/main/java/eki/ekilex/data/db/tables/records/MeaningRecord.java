/*
 * This file is generated by jOOQ.
*/
package eki.ekilex.data.db.tables.records;


import eki.ekilex.data.db.tables.Meaning;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.0"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MeaningRecord extends UpdatableRecordImpl<MeaningRecord> implements Record2<Long, String[]> {

    private static final long serialVersionUID = -538995853;

    /**
     * Setter for <code>public.meaning.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.meaning.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.meaning.datasets</code>.
     */
    public void setDatasets(String... value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.meaning.datasets</code>.
     */
    public String[] getDatasets() {
        return (String[]) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<Long, String[]> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<Long, String[]> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Meaning.MEANING.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String[]> field2() {
        return Meaning.MEANING.DATASETS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] component2() {
        return getDatasets();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] value2() {
        return getDatasets();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeaningRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeaningRecord value2(String... value) {
        setDatasets(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeaningRecord values(Long value1, String[] value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MeaningRecord
     */
    public MeaningRecord() {
        super(Meaning.MEANING);
    }

    /**
     * Create a detached, initialised MeaningRecord
     */
    public MeaningRecord(Long id, String[] datasets) {
        super(Meaning.MEANING);

        set(0, id);
        set(1, datasets);
    }
}