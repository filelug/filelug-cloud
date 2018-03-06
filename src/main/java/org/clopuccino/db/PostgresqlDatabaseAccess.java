package org.clopuccino.db;

import ch.qos.logback.classic.Logger;
import org.apache.commons.dbcp2.BasicDataSource;
import org.clopuccino.service.BaseService;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * <code>PostgresqlDatabaseAccess</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class PostgresqlDatabaseAccess implements DatabaseAccess {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("DB");

    private static BasicDataSource dataSource;

    public void initDatabase() throws Exception {
        /* 設定connection pool */
        if (dataSource == null) {
            String dbUser;
            String dbPassword;
            int dbInitialSize;
            int dbMaxTotal;
            boolean dbTestWhileIdle;
            String dbValidationQuery;
            boolean dbPoolPreparedStatement;
            String dbDriver;
            String dbStartUrl;

            BaseService baseService = new BaseService();

            if (baseService.getRepositoryUseHttps()) {
                // production

                dbUser = "filelugadmin";
                dbPassword = "Lug123wed59";
                dbInitialSize = 10;
                dbMaxTotal = 50; // type sql 'show max_connection' to show the max connection for this DB instance
                dbTestWhileIdle = true;
                dbValidationQuery = "SELECT 1";
                dbPoolPreparedStatement = true;
                dbDriver = "org.postgresql.Driver";
                dbStartUrl = "jdbc:postgresql://filelugdb.cft59vx1t7ga.us-west-2.rds.amazonaws.com:5432/filelugdb";
            } else {
                // testing

                dbUser = "masonhsieh";
                dbPassword = "";
                dbInitialSize = 2;
                dbMaxTotal = 10;
                dbTestWhileIdle = true;
                dbValidationQuery = "SELECT 1";
                dbPoolPreparedStatement = true;
                dbDriver = "org.postgresql.Driver";
                dbStartUrl = "jdbc:postgresql://127.0.0.1:5432/masonhsieh";
            }

            dataSource = new BasicDataSource();

            dataSource.setDriverClassName(dbDriver);
            dataSource.setUrl(dbStartUrl);
            dataSource.setUsername(dbUser);
            dataSource.setPassword(dbPassword);

            dataSource.setDefaultAutoCommit(true);

            dataSource.setInitialSize(dbInitialSize);
            dataSource.setMaxTotal(dbMaxTotal);
            dataSource.setTestWhileIdle(dbTestWhileIdle);
            dataSource.setValidationQuery(dbValidationQuery);
            dataSource.setPoolPreparedStatements(dbPoolPreparedStatement);
        }
    }

    public BasicDataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

    @Override
    public void closeDataSource(boolean shutdownDbServer) throws Exception {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        } finally {
            if (shutdownDbServer) {
                LOGGER.warn("Can not shutdown database server. Function not supported.");
            }
        }
    }

    public void close(ResultSet rs, Statement statement, PreparedStatement pStatement, Connection conn) throws Exception {
        if (rs != null && !rs.isClosed()) {
            rs.close();
        }

        if (statement != null && !statement.isClosed()) {
            statement.close();
        }

        if (pStatement != null && !pStatement.isClosed()) {
            pStatement.close();
        }

        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
