package com.pa.plugin.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author puan
 * @date 2019-07-24 11:59
 **/
public class MysqlConnectionParam extends AbstractDbConnectionParam {

    private static final String TEST_CON_SQL = "select 1 from dual";

    private static final String GET_TABLE_SQL = "select table_name, table_comment " +
            "FROM information_schema.TABLES WHERE table_schema = '?' " +
            "and table_type = 'BASE TABLE' ORDER BY table_name";

    private static final String GET_COLUMN_SQL = "SELECT COLUMN_NAME columnname, COLUMN_TYPE columntype,"
            + "COLUMN_KEY  columnkey, IS_NULLABLE isnullable,"
            + "COLUMN_DEFAULT columndefault, COLUMN_COMMENT columncomment"
            + " from INFORMATION_SCHEMA.COLUMNS where table_schema = ? AND table_name = ?";

    public MysqlConnectionParam() {

    }

    public MysqlConnectionParam(String url, String username, String password, String schema) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.schema = schema;
        this.nameLabel = "table_name";
        this.commentLabel = "table_comment";
    }

    @Override
    protected Douple buildDouple(ResultSet rs) {
        try {
            String name = rs.getString(nameLabel);
            String comment = rs.getString(commentLabel);
            return new Douple(name, comment);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("获取字段失败");
        }
    }

    @Override
    protected String buildGetTablesSql() {
        return GET_TABLE_SQL.replace("?", schema);
    }

    @Override
    protected ResultSet getColumns(Douple douple) {
        try {
            if (getColumnsStatement == null) {
                getColumnsStatement = connection.prepareStatement(GET_COLUMN_SQL);
            }
            getColumnsStatement.setString(1, schema);
            getColumnsStatement.setString(2, douple.getName());
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
