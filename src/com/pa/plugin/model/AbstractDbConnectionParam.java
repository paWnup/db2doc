package com.pa.plugin.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库连接参数超类
 *
 * @author puan
 * @date 2019-07-24 11:57
 **/
public abstract class AbstractDbConnectionParam {

    String url;

    String username;

    String password;

    String schema;

    String nameLabel;

    String commentLabel;

    Connection connection;

    PreparedStatement getColumnsStatement;

    public List<Douple> getTables() {
        ResultSet rs = null;
        try {
            String sql = buildGetTablesSql();
            System.out.println(sql);
            Statement statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            List<Douple> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(buildDouple(rs));
            }
            rs.close();
            return tables;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("获取数据库表名列表异常");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Map<String, String>> getTableColumns(Douple tripul) {
        ResultSet rs = null;
        try {
            rs = getColumns(tripul);
            List<Map<String, String>> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(buildPenta(rs));
            }
            rs.close();
            return tables;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("获取数据库表名列表异常");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected abstract Douple buildDouple(ResultSet rs);

    private Map<String, String> buildPenta(ResultSet rs) {
        try {
            Map<String, String> map = new HashMap<>(10);
            map.put("columnName", rs.getString("columnname"));
            map.put("columnType", rs.getString("columntype"));
            map.put("columnKey", rs.getString("columnkey"));
            map.put("isNullable", rs.getString("isnullable"));
            map.put("columnDefault", rs.getString("columndefault"));
            map.put("columnComment", rs.getString("columncomment"));
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("获取数据库表名列表异常");
        }
    }

    /**
     * 构建获取表明列表的sql
     *
     * @return 获取表明列表的sql
     */
    protected abstract String buildGetTablesSql();

    /**
     * 查询数据表的列信息
     *
     * @param tripul
     * @return 查询结果
     */
    protected abstract ResultSet getColumns(Douple tripul);

    /**
     * 关闭连接
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("关闭连接失败");
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getNameLabel() {
        return nameLabel;
    }

    public void setNameLabel(String nameLabel) {
        this.nameLabel = nameLabel;
    }

    public String getCommentLabel() {
        return commentLabel;
    }

    public void setCommentLabel(String commentLabel) {
        this.commentLabel = commentLabel;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public PreparedStatement getGetColumnsStatement() {
        return getColumnsStatement;
    }

    public void setGetColumnsStatement(PreparedStatement getColumnsStatement) {
        this.getColumnsStatement = getColumnsStatement;
    }

    public boolean testConnection() {
        ResultSet rs = null;
        try {
            Statement statement = connection.createStatement();
            rs = statement.executeQuery(getTestSql());
            rs.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected abstract String getTestSql();
}
