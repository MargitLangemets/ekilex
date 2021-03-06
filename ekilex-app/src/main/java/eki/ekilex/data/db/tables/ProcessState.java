/*
 * This file is generated by jOOQ.
*/
package eki.ekilex.data.db.tables;


import eki.ekilex.data.db.Indexes;
import eki.ekilex.data.db.Keys;
import eki.ekilex.data.db.Public;
import eki.ekilex.data.db.tables.records.ProcessStateRecord;

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
        "jOOQ version:3.10.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProcessState extends TableImpl<ProcessStateRecord> {

    private static final long serialVersionUID = -1934927715;

    /**
     * The reference instance of <code>public.process_state</code>
     */
    public static final ProcessState PROCESS_STATE = new ProcessState();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ProcessStateRecord> getRecordType() {
        return ProcessStateRecord.class;
    }

    /**
     * The column <code>public.process_state.code</code>.
     */
    public final TableField<ProcessStateRecord, String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>public.process_state.datasets</code>.
     */
    public final TableField<ProcessStateRecord, String[]> DATASETS = createField("datasets", org.jooq.impl.SQLDataType.VARCHAR.getArrayDataType(), this, "");

    /**
     * Create a <code>public.process_state</code> table reference
     */
    public ProcessState() {
        this(DSL.name("process_state"), null);
    }

    /**
     * Create an aliased <code>public.process_state</code> table reference
     */
    public ProcessState(String alias) {
        this(DSL.name(alias), PROCESS_STATE);
    }

    /**
     * Create an aliased <code>public.process_state</code> table reference
     */
    public ProcessState(Name alias) {
        this(alias, PROCESS_STATE);
    }

    private ProcessState(Name alias, Table<ProcessStateRecord> aliased) {
        this(alias, aliased, null);
    }

    private ProcessState(Name alias, Table<ProcessStateRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.PROCESS_STATE_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ProcessStateRecord> getPrimaryKey() {
        return Keys.PROCESS_STATE_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ProcessStateRecord>> getKeys() {
        return Arrays.<UniqueKey<ProcessStateRecord>>asList(Keys.PROCESS_STATE_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessState as(String alias) {
        return new ProcessState(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessState as(Name alias) {
        return new ProcessState(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ProcessState rename(String name) {
        return new ProcessState(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ProcessState rename(Name name) {
        return new ProcessState(name, null);
    }
}
