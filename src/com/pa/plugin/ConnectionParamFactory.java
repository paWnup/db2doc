package com.pa.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.pa.plugin.model.AbstractDbConnectionParam;
import com.pa.plugin.model.MysqlConnectionParam;
import com.pa.plugin.model.PgsqlConnectionParam;
import groovy.util.logging.Slf4j;
import sun.reflect.misc.ConstructorUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author puan
 * @date 2019-07-24 13:52
 **/
@Slf4j
class ConnectionParamFactory {

    private static Logger log = Logger.getInstance(ConnectionParamFactory.class);

    static AbstractDbConnectionParam getConnectionParam(String url, String username, String password, String schema) {
        AbstractDbConnectionParam connectionParam = build(url, username, password, schema);
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            connectionParam.setConnection(connection);
            return connectionParam;
        } catch (SQLException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private static AbstractDbConnectionParam build(String url, String username, String password, String schema) {
        String type = url.split(":")[1];
        AbstractDbConnectionParam connectionParam = null;
        try {
            Class[] params = new Class[]{String.class, String.class, String.class, String.class};
            for (DbType dbType : DbType.values()) {
                if (dbType.type.equals(type)) {
                    Class.forName(dbType.driverClass);//加载数据库驱动
                    Constructor constructor = ConstructorUtil.getConstructor(dbType.clazz, params);
                    connectionParam = (AbstractDbConnectionParam) constructor.newInstance(url, username, password, schema);
                    break;
                }
            }
            if (connectionParam == null) {
                throw new RuntimeException("数据库类型暂不支持");
            }
            return connectionParam;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            log.error(e);
            throw new RuntimeException("未找到对应的构造器");
        }
    }

    private enum DbType {
        /**
         * mysql
         */
        MYSQL("mysql", MysqlConnectionParam.class, "com.mysql.jdbc.Driver"),
        /**
         * pgsql
         */
        PGSQL("postgresql", PgsqlConnectionParam.class, "org.postgresql.Driver");

        /**
         * 数据库类型，数据库连接url中“jdbc:?:”的?部分
         */
        private String type;

        /**
         * 数据库对应的连接参数类型
         */
        private Class clazz;

        /**
         * 数据库对应的连接驱动
         */
        private String driverClass;

        DbType(String type, Class clazz, String driverClass) {
            this.type = type;
            this.clazz = clazz;
            this.driverClass = driverClass;
        }
    }
}
