/*
 * This file is generated by jOOQ.
*/
package eki.eve.data.db.tables;


import eki.eve.data.db.Indexes;
import eki.eve.data.db.Keys;
import eki.eve.data.db.Public;
import eki.eve.data.db.tables.records.LexemeTypeRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class LexemeType extends TableImpl<LexemeTypeRecord> {

    private static final long serialVersionUID = -1618755003;

    /**
     * The reference instance of <code>public.lexeme_type</code>
     */
    public static final LexemeType LEXEME_TYPE = new LexemeType();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<LexemeTypeRecord> getRecordType() {
        return LexemeTypeRecord.class;
    }

    /**
     * The column <code>public.lexeme_type.code</code>.
     */
    public final TableField<LexemeTypeRecord, String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>public.lexeme_type.datasets</code>.
     */
    public final TableField<LexemeTypeRecord, String[]> DATASETS = createField("datasets", org.jooq.impl.SQLDataType.VARCHAR.getArrayDataType(), this, "");

    /**
     * Create a <code>public.lexeme_type</code> table reference
     */
    public LexemeType() {
        this(DSL.name("lexeme_type"), null);
    }

    /**
     * Create an aliased <code>public.lexeme_type</code> table reference
     */
    public LexemeType(String alias) {
        this(DSL.name(alias), LEXEME_TYPE);
    }

    /**
     * Create an aliased <code>public.lexeme_type</code> table reference
     */
    public LexemeType(Name alias) {
        this(alias, LEXEME_TYPE);
    }

    private LexemeType(Name alias, Table<LexemeTypeRecord> aliased) {
        this(alias, aliased, null);
    }

    private LexemeType(Name alias, Table<LexemeTypeRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.LEXEME_TYPE_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<LexemeTypeRecord> getPrimaryKey() {
        return Keys.LEXEME_TYPE_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<LexemeTypeRecord>> getKeys() {
        return Arrays.<UniqueKey<LexemeTypeRecord>>asList(Keys.LEXEME_TYPE_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LexemeType as(String alias) {
        return new LexemeType(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LexemeType as(Name alias) {
        return new LexemeType(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public LexemeType rename(String name) {
        return new LexemeType(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public LexemeType rename(Name name) {
        return new LexemeType(name, null);
    }
}
