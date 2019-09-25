package com.pa.plugin.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author puan
 * @date 2019-07-24 12:00
 **/
public class PgsqlConnectionParam extends AbstractDbConnectionParam {

    private Map<String, Long> idMap = new HashMap<>(50);

    private static final String TEST_CON_SQL = "select 1";

    private static final String GET_TABLE_SQL = "select distinct pc.oid, pc.relname, pd.description from pg_class pc " +
            "inner join pg_attribute pa on pa.attrelid = pc.oid left join pg_description pd " +
            "on pd.objoid = pc.oid and pd.objsubid = 0 where pa.attstattarget = -1 " +
            "and pc.relkind = 'r' and pc.relname not like 'pg_%' and pc.relname not like 'sql_%' " +
            "order by pc.relname";

    private static final String GET_COLUMN_SQL = "select pa.attname columnname," +
            "format_type(pa.atttypid, pa.atttypmod) columntype," +
            "case " +
            "when pc_p.conrelid is not null then '主键' " +
            "when pc_u.conrelid is not null then '唯一' " +
            "when pc_f.conrelid is not null then '外键' " +
            "else '' end columnkey," +
            "(case when pa.attnotnull then '否' else '是' end) isnullable," +
            "col_description(pa.attrelid, pa.attnum) columncomment, " +
            "'' columndefault " +
            //TODO 字段内容太长，不好识别
//            "COALESCE(paf.adsrc, 'NULL') columndefault " +
            "from pg_attribute pa " +
            "left join pg_attrdef paf " +
            "on paf.adnum = pa.attnum " +
            "and paf.adrelid = pa.attrelid " +
            "left join pg_constraint pc_p " +
            "on pc_p.conrelid = pa.attrelid " +
            "and pc_p.conkey [ 1] = pa.attnum " +
            "and pc_p.contype = 'p' " +
            "left join pg_constraint pc_u " +
            "on pc_u.conrelid = pa.attrelid " +
            "and pc_u.conkey [ 1] = pa.attnum " +
            "and pc_u.contype = 'u' " +
            "left join pg_constraint pc_f " +
            "on pc_f.conrelid = pa.attrelid " +
            "and pc_f.conkey [ 1] = pa.attnum " +
            "and pc_f.contype = 'f' " +
            "where pa.attstattarget = -1 " +
            "  and pa.attrelid = ?";

    public PgsqlConnectionParam(String url, String username, String password, String schema) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.schema = schema;
        this.nameLabel = "relname";
        this.commentLabel = "description";
    }

    @Override
    protected Douple buildDouple(ResultSet rs) {
        try {
            String name = rs.getString(nameLabel);
            String comment = rs.getString(commentLabel);
            idMap.put(name, rs.getLong("oid"));
            return new Douple(name, comment);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("获取字段失败");
        }
    }

    @Override
    protected String buildGetTablesSql() {
        return GET_TABLE_SQL;
    }

    @Override
    protected ResultSet getColumns(Douple douple) {
        try {
            if (getColumnsStatement == null) {
                getColumnsStatement = connection.prepareStatement(GET_COLUMN_SQL);
            }
            getColumnsStatement.setLong(1, idMap.get(douple.getName()));
            return getColumnsStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("获取数据库表名列表异常");
        }
    }

    @Override
    protected String getTestSql() {
        return TEST_CON_SQL;
    }

}
