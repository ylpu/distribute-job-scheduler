package com.ylpu.thales.scheduler.executor.sql;

import java.util.HashMap;
import java.util.Map;
import com.ylpu.thales.scheduler.enums.DBType;

public class DriverProvider {

    private static Map<DBType, String> map = new HashMap<DBType, String>();

    static {
        map.put(DBType.ORACLE, "oracle.jdbc.driver.OracleDriver");
        map.put(DBType.MYSQL, "com.mysql.jdbc.Driver");
        map.put(DBType.SQLSERVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        map.put(DBType.CLICKHOUSE, "ru.yandex.clickhouse.ClickHouseDriver");
    }

    public static String getDriver(DBType dbType) {
        return map.get(dbType);
    }
}
