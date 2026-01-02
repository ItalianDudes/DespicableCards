package it.italiandudes.despicable_cards.db;

import it.italiandudes.despicable_cards.javafx.utils.DBDataHandler;
import it.italiandudes.despicable_cards.utils.Defs;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;

@SuppressWarnings({"unused", "SqlDialectInspection", "DuplicatedCode", "SqlNoDataSourceInspection"})
public final class DBManager {

    // Attributes
    private static Connection dbConnection = null;
    private static final String DB_PREFIX = "jdbc:sqlite:";

    // Generic SQLite Connection Initializer
    private static void setConnection(@NotNull final String DB_ABSOLUTE_PATH) throws SQLException {
        String url = DB_PREFIX+DB_ABSOLUTE_PATH+"?allowMultiQueries=true";
        dbConnection = DriverManager.getConnection(DB_PREFIX + DB_ABSOLUTE_PATH);
        dbConnection.setAutoCommit(true);
        Statement st = dbConnection.createStatement();
        st.execute("PRAGMA foreign_keys = ON;");
        st.close();
    }

    // Methods
    @SuppressWarnings("resource")
    public static ResultSet dbAllRowsSearch(@NotNull final String tableName) throws SQLException {
        String query = "SELECT * FROM " + tableName + ";";
        PreparedStatement ps = DBManager.preparedStatement(query);
        if (ps == null) throw new SQLException("Database connection is null");
        return ps.executeQuery();
    }
    public static void connectToDB(@NotNull final File DB_PATH) throws IOException, SQLException {
        if (!DB_PATH.exists() || DB_PATH.isDirectory()) throw new IOException("This db doesn't exist");
        setConnection(DB_PATH.getAbsolutePath());
    }
    public static void closeConnection() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            }catch (Exception ignored){}
        }
    }
    public static PreparedStatement preparedStatement(@NotNull final String query) throws SQLException {
        if (dbConnection != null) {
            //noinspection SqlSourceToSinkFlow
            return dbConnection.prepareStatement(query);
        }
        return null;
    }
    public static void commit() throws SQLException {
        if (dbConnection != null) dbConnection.commit();
    }

    // DB Creator
    public static void createDB(@NotNull final String DB_PATH) throws SQLException {
        setConnection(DB_PATH);
        Scanner reader = new Scanner(Defs.Resources.getAsStream(Defs.Resources.SQL.SQL_DATABASE), StandardCharsets.UTF_8);
        StringBuilder queryReader = new StringBuilder();
        String query;
        String buffer;

        while (reader.hasNext()) {
            buffer = reader.nextLine();
            queryReader.append(buffer);
            if (buffer.endsWith(";")) {
                query = queryReader.toString();
                PreparedStatement ps = dbConnection.prepareStatement(query);
                ps.execute();
                ps.close();
                queryReader = new StringBuilder();
            } else {
                queryReader.append('\n');
            }
        }
        reader.close();
        DBDataHandler.writeKeyParameter(KeyParameters.DB_VERSION, Defs.DB_VERSION);
    }
}
