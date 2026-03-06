package dataaccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    static public void executeUpdate(String statement) throws SQLException {
        var initialize = "USE " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var initializeStatement = conn.prepareStatement(initialize)) {
            initializeStatement.executeUpdate();

            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new SQLException("failed to create database", ex);
        }
    }

    static public ArrayList<ArrayList<String>> executeQuery(String statement) throws SQLException {
        var initialize = "USE " + databaseName;
        var results = new ArrayList<ArrayList<String>>();

        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var initializeStatement = conn.prepareStatement(initialize)) {
            initializeStatement.executeUpdate();

            var preparedStatement = conn.prepareStatement(statement);
            var result = preparedStatement.executeQuery();

            int columnCount = result.getMetaData().getColumnCount();

            while (result.next()) {
                var row = new ArrayList<String>();

                for (int i = 1; i <= columnCount; i ++) {
                    row.add(result.getString(i));
                }
                results.add(row);
            }

            return results;

        } catch (SQLException ex) {
            throw new SQLException("failed to create database", ex);
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws SQLException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new SQLException("failed to create database", ex);
        }
    }

    static public void createTables() throws SQLException {
        String[] statements = new String[4];

        statements[0] = "USE " + databaseName + ";";
        statements[1] = "CREATE TABLE IF NOT EXISTS users (username VARCHAR(100) PRIMARY KEY, password VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL);";
        statements[2] = "CREATE TABLE IF NOT EXISTS sessions (authToken VARCHAR(100) PRIMARY KEY, username VARCHAR(100) NOT NULL);";
        statements[3] = "CREATE TABLE IF NOT EXISTS games (gameID int PRIMARY KEY, whiteUsername VARCHAR(100), blackUsername VARCHAR(100), gameName VARCHAR(100), game JSON);";
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword)) {
             for (String statement : statements) {
                 var preparedStatement = conn.prepareStatement(statement);
                 preparedStatement.executeUpdate();
             }
        } catch (SQLException ex) {
            throw new SQLException("failed to create database", ex);
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws SQLException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new SQLException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}
