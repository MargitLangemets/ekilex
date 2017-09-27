/*
 * This file is generated by jOOQ.
*/
package eki.eve.data.db.tables;


import eki.eve.data.db.Keys;
import eki.eve.data.db.Public;
import eki.eve.data.db.tables.records.GenderRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Gender extends TableImpl<GenderRecord> {

    private static final long serialVersionUID = 695971820;

    /**
     * The reference instance of <code>public.gender</code>
     */
    public static final Gender GENDER = new Gender();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<GenderRecord> getRecordType() {
        return GenderRecord.class;
    }

    /**
     * The column <code>public.gender.code</code>.
     */
    public final TableField<GenderRecord, String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");

    /**
     * The column <code>public.gender.dataset</code>.
     */
    public final TableField<GenderRecord, String[]> DATASET = createField("dataset", org.jooq.impl.SQLDataType.VARCHAR.getArrayDataType(), this, "");

    /**
     * Create a <code>public.gender</code> table reference
     */
    public Gender() {
        this("gender", null);
    }

    /**
     * Create an aliased <code>public.gender</code> table reference
     */
    public Gender(String alias) {
        this(alias, GENDER);
    }

    private Gender(String alias, Table<GenderRecord> aliased) {
        this(alias, aliased, null);
    }

    private Gender(String alias, Table<GenderRecord> aliased, Field<?>[] parameters) {
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
    public UniqueKey<GenderRecord> getPrimaryKey() {
        return Keys.GENDER_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<GenderRecord>> getKeys() {
        return Arrays.<UniqueKey<GenderRecord>>asList(Keys.GENDER_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Gender as(String alias) {
        return new Gender(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Gender rename(String name) {
        return new Gender(name, null);
    }
}
