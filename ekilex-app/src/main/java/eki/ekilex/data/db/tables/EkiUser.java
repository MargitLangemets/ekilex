/*
 * This file is generated by jOOQ.
*/
package eki.ekilex.data.db.tables;


import eki.ekilex.data.db.Indexes;
import eki.ekilex.data.db.Keys;
import eki.ekilex.data.db.Public;
import eki.ekilex.data.db.tables.records.EkiUserRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
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
        "jOOQ version:3.10.0"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class EkiUser extends TableImpl<EkiUserRecord> {

    private static final long serialVersionUID = 642629233;

    /**
     * The reference instance of <code>public.eki_user</code>
     */
    public static final EkiUser EKI_USER = new EkiUser();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EkiUserRecord> getRecordType() {
        return EkiUserRecord.class;
    }

    /**
     * The column <code>public.eki_user.id</code>.
     */
    public final TableField<EkiUserRecord, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('eki_user_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.eki_user.name</code>.
     */
    public final TableField<EkiUserRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.eki_user.password</code>.
     */
    public final TableField<EkiUserRecord, String> PASSWORD = createField("password", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.eki_user.created</code>.
     */
    public final TableField<EkiUserRecord, Timestamp> CREATED = createField("created", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.field("statement_timestamp()", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * Create a <code>public.eki_user</code> table reference
     */
    public EkiUser() {
        this(DSL.name("eki_user"), null);
    }

    /**
     * Create an aliased <code>public.eki_user</code> table reference
     */
    public EkiUser(String alias) {
        this(DSL.name(alias), EKI_USER);
    }

    /**
     * Create an aliased <code>public.eki_user</code> table reference
     */
    public EkiUser(Name alias) {
        this(alias, EKI_USER);
    }

    private EkiUser(Name alias, Table<EkiUserRecord> aliased) {
        this(alias, aliased, null);
    }

    private EkiUser(Name alias, Table<EkiUserRecord> aliased, Field<?>[] parameters) {
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
        return Arrays.<Index>asList(Indexes.EKI_USER_NAME_KEY, Indexes.EKI_USER_PKEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<EkiUserRecord, Long> getIdentity() {
        return Keys.IDENTITY_EKI_USER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<EkiUserRecord> getPrimaryKey() {
        return Keys.EKI_USER_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<EkiUserRecord>> getKeys() {
        return Arrays.<UniqueKey<EkiUserRecord>>asList(Keys.EKI_USER_PKEY, Keys.EKI_USER_NAME_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EkiUser as(String alias) {
        return new EkiUser(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EkiUser as(Name alias) {
        return new EkiUser(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public EkiUser rename(String name) {
        return new EkiUser(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EkiUser rename(Name name) {
        return new EkiUser(name, null);
    }
}