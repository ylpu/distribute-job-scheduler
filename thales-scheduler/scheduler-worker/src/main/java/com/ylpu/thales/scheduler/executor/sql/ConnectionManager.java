package com.ylpu.thales.scheduler.executor.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import com.ylpu.thales.scheduler.core.rest.JobManager;
import com.ylpu.thales.scheduler.enums.DBType;
import com.ylpu.thales.scheduler.response.ConnectionResponse;

public class ConnectionManager {
	
    public static Connection getConnection(String connectionId) throws Exception {
 	   ConnectionResponse cr = JobManager.getConnection(connectionId);
 	   DBType dbType = DBType.getDBType(cr.getConnectionType());
 	   String className = DriverProvider.getDriver(dbType);
 	   String url = "jdbc:" + dbType.toString().toLowerCase() + "://" + cr.getHostname() + ":" + cr.getPort() + "/" + cr.getDbSchema();
 	   String userName = cr.getUsername();
 	   String password = cr.getPassword();
       Class.forName(className);
       Connection connection =  DriverManager.getConnection(url, userName, password);
       return connection;
    }
}
