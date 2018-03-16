/*
 * This file is generated by jOOQ.
*/
package eki.wordweb.data.db.tables;


import eki.wordweb.data.db.Public;
import eki.wordweb.data.db.tables.records.DblinkRecord;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Dblink extends TableImpl<DblinkRecord> {

    private static final long serialVersionUID = -989018862;

    /**
     * The reference instance of <code>public.dblink</code>
     */
    public static final Dblink DBLINK = new Dblink();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DblinkRecord> getRecordType() {
        return DblinkRecord.class;
    }

    /**
     * The column <code>public.dblink.dblink</code>.
     */
    public final TableField<DblinkRecord, Record> DBLINK_ = createField("dblink", org.jooq.impl.SQLDataType.RECORD, this, "");

    /**
     * Create a <code>public.dblink</code> table reference
     */
    public Dblink() {
        this(DSL.name("dblink"), null);
    }

    /**
     * Create an aliased <code>public.dblink</code> table reference
     */
    public Dblink(String alias) {
        this(DSL.name(alias), DBLINK);
    }

    /**
     * Create an aliased <code>public.dblink</code> table reference
     */
    public Dblink(Name alias) {
        this(alias, DBLINK);
    }

    private Dblink(Name alias, Table<DblinkRecord> aliased) {
        this(alias, aliased, new Field[2]);
    }

    private Dblink(Name alias, Table<DblinkRecord> aliased, Field<?>[] parameters) {
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
    public Dblink as(String alias) {
        return new Dblink(DSL.name(alias), this, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dblink as(Name alias) {
        return new Dblink(alias, this, parameters);
    }

    /**
     * Rename this table
     */
    @Override
    public Dblink rename(String name) {
        return new Dblink(DSL.name(name), null, parameters);
    }

    /**
     * Rename this table
     */
    @Override
    public Dblink rename(Name name) {
        return new Dblink(name, null, parameters);
    }

    /**
     * Call this table-valued function
     */
    public Dblink call(String __1, String __2) {
        return new Dblink(DSL.name(getName()), null, new Field[] { 
              DSL.val(__1, org.jooq.impl.SQLDataType.CLOB)
            , DSL.val(__2, org.jooq.impl.SQLDataType.CLOB)
        });
    }

    /**
     * Call this table-valued function
     */
    public Dblink call(Field<String> __1, Field<String> __2) {
        return new Dblink(DSL.name(getName()), null, new Field[] { 
              __1
            , __2
        });
    }
}