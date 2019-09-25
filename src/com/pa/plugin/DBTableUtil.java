package com.pa.plugin;

import com.pa.plugin.model.AbstractDbConnectionParam;
import com.pa.plugin.model.Douple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出数据库表结构文档
 *
 * @author puan
 * @date 2019-03-9:
 **/
public class DBTableUtil {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://172.16.21.150:5432/ga_dds?currentSchema=public";
        String username = "postgres";
        String password = "123456";
        String schema = "public";

//        String url = "jdbc:mysql://47.95.118.118:3306/demo";
//        String username = "root";
//        String password = "123456";
//        String schema = "demo";

        Map<String, Object> map = getDatabaseConstructureMap(url, username, password, schema);
        DocUtil.createDocByTemplate(map, "database_tables", "C:/Users/Administrator/Desktop/数据库表结构文档.doc");
    }

    public static void db2doc(String url, String username, String password, String schema, String outputPath) {
        Map<String, Object> map = getDatabaseConstructureMap(url, username, password, schema);
        DocUtil.createDocByTemplate(map, "database_tables", outputPath);
    }


    public static boolean testConnection(String url, String username, String password, String schema) {
        try {
            AbstractDbConnectionParam connectionParam = ConnectionParamFactory.getConnectionParam(url, username, password, schema);
            return connectionParam.testConnection();
        } catch (Throwable e) {
            return false;
        }
    }

    private static Map<String, Object> getDatabaseConstructureMap(String url, String username,
                                                                  String password, String schema) {
        AbstractDbConnectionParam connectionParam = ConnectionParamFactory.getConnectionParam(url, username, password, schema);
        Map<String, Object> map = new HashMap<>();
        List<Douple> tables = connectionParam.getTables();
        List<Map<String, Object>> list = new ArrayList<>();
        int index = 1;
        for (Douple tripul : tables) {
            Map<String, Object> table1 = new HashMap<>(70);
            table1.put("tableName", tripul.getName());
            table1.put("no", index++);
            table1.put("tableComment", tripul.getComment());
            List<Map<String, String>> columns = connectionParam.getTableColumns(tripul);
            table1.put("columns", columns);
            list.add(table1);
        }
        connectionParam.close();
        map.put("maps", list);
        return map;
    }
}
