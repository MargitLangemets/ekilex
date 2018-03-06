/*
 * This file is generated by jOOQ.
*/
package eki.ekilex.data.db.tables;


import eki.ekilex.data.db.Indexes;
import eki.ekilex.data.db.Keys;
import eki.ekilex.data.db.Public;
import eki.ekilex.data.db.tables.records.FreeformRefLinkRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
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
        "jOOQ version:3.10.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class FreeformRefLink extends TableImpl<FreeformRefLinkRecord> {

    private static final long serialVersionUID = 1872375490;

    /**
     * The reference instance of <code>public.freeform_ref_link</code>
     */
    public static final FreeformRefLink FREEFORM_REF_LINK = new FreeformRefLink();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<FreeformRefLinkRecord> getRecordType() {
        return FreeformRefLinkRecord.class;
    }

    /**
     * The column <code>public.freeform_ref_link.id</code>.
     */
    public final TableField<FreeformRefLinkRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('freeform_ref_link_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.freeform_ref_link.freeform_id</code>.
     */
    public final TableField<FreeformRefLinkRecord, Long> FREEFORM_ID = createField("freeform_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.freeform_ref_link.ref_type</code>.
     */
    public final TableField<FreeformRefLinkRecord, String> REF_TYPE = createField("ref_type", org.jooq.impl.SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>public.freeform_ref_link.ref_id</code>.
     */
    public final TableField<FreeformRefLinkRecord, Long> REF_ID = createField("ref_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.freeform_ref_link.name</code>.
     */
    public final TableField<FreeformRefLinkRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.freeform_ref_link.order_by</code>.
     */
    public final TableField<FreeformRefLinkRecord, Long> ORDER_BY = createField("order_by", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('freeform_ref_link_order_by_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * Create a <code>public.freeform_ref_link</code> table reference
     */
    public FreeformRefLink() {
        this(DSL.name("freeform_ref_link"), null);
    }

    /**
     * Create an aliased <code>public.freeform_ref_link</code> table reference
     */
    public FreeformRefLink(String alias) {
        this(DSL.name(alias), FREEFORM_REF_LINK);
    }

    /**
     * Create an aliased <code>public.freeform_ref_link</code> table reference
     */
    public FreeformRefLink(Name alias) {
        this(alias, FREEFORM_REF_LINK);
    }

    private FreeformRefLink(Name alias, Table<FreeformRefLinkRecord> aliased) {
        this(alias, aliased, null);
    }

    private FreeformRefLink(Name alias, Table<FreeformRefLinkRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.FREEFORM_REF_LINK_FREEFORM_ID_IDX, Indexes.FREEFORM_REF_LINK_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<FreeformRefLinkRecord, Long> getIdentity() {
        return Keys.IDENTITY_FREEFORM_REF_LINK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<FreeformRefLinkRecord> getPrimaryKey() {
        return Keys.FREEFORM_REF_LINK_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<FreeformRefLinkRecord>> getKeys() {
        return Arrays.<UniqueKey<FreeformRefLinkRecord>>asList(Keys.FREEFORM_REF_LINK_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<FreeformRefLinkRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<FreeformRefLinkRecord, ?>>asList(Keys.FREEFORM_REF_LINK__FREEFORM_REF_LINK_FREEFORM_ID_FKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FreeformRefLink as(String alias) {
        return new FreeformRefLink(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FreeformRefLink as(Name alias) {
        return new FreeformRefLink(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public FreeformRefLink rename(String name) {
        return new FreeformRefLink(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public FreeformRefLink rename(Name name) {
        return new FreeformRefLink(name, null);
    }
}
