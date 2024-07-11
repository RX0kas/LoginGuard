package fr.ninjagoku4560.loginguard;

import java.io.File;
import java.sql.*;

public class SQLiteHandler {

    private static Connection dataconnection;

    public static void getDataconnection() {
        if (dataconnection == null) {
            try {
                // Load the SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                LoginGuard.LOGGER.info("SQLite JDBC driver loaded successfully");

                // Define the path where the SQLite database will be created
                String dbPath = "LoginGuard Data" + File.separator + "LoginGuard.db";
                File dbFile = new File(dbPath);

                // Ensure the folder exists
                File dataFolder = new File("LoginGuard Data");
                if (!dataFolder.exists()) {
                    boolean dirCreated = dataFolder.mkdirs();
                    if (dirCreated) {
                        LoginGuard.LOGGER.info("Data folder created successfully");
                    } else {
                        LoginGuard.LOGGER.error("Failed to create data folder");
                        return;
                    }
                }

                // Connect to SQLite database (create new if not exists)
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                dataconnection = DriverManager.getConnection(url);
                LoginGuard.LOGGER.info("Connected to SQLite database");

                boolean tableCreation = createDataTableIfNotExists();
                if(!tableCreation){
                    LoginGuard.LOGGER.error("Database Tables were not created");
                }

            } catch (ClassNotFoundException e) {
                LoginGuard.LOGGER.error("SQLite JDBC driver not found: " + e.getMessage());
            } catch (SQLException e) {
                LoginGuard.LOGGER.error("Error connecting to SQLite database: " + e.getMessage());
            }
        }
    }

    public static void insertRegData(String PlayerName, String password) throws SQLException {
        String sql = "INSERT INTO playerRegData (username, password) VALUES (?, ?)";

        try (PreparedStatement pstmt = dataconnection.prepareStatement(sql)) {
            pstmt.setString(1, PlayerName);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        }
    }

    public static void SetPosData(String PlayerName, float x, float y, float z) throws SQLException {
        String sqlSelect = "SELECT username FROM playerPosData WHERE username = ?";
        String sqlInsert = "INSERT INTO playerPosData (username, xpos, ypos, zpos) VALUES (?, ?, ?, ?)";
        String sqlUpdate = "UPDATE playerPosData SET xpos = ?, ypos = ?, zpos = ? WHERE username = ?";

        try (PreparedStatement pstmtSelect = dataconnection.prepareStatement(sqlSelect)) {
            pstmtSelect.setString(1, PlayerName);
            try (ResultSet rs = pstmtSelect.executeQuery()) {
                if (rs.next()) {
                    // Entry exists, perform update
                    try (PreparedStatement pstmtUpdate = dataconnection.prepareStatement(sqlUpdate)) {
                        pstmtUpdate.setFloat(1, x);
                        pstmtUpdate.setFloat(2, y);
                        pstmtUpdate.setFloat(3, z);
                        pstmtUpdate.setString(4, PlayerName);
                        pstmtUpdate.executeUpdate();
                    }
                } else {
                    // Entry does not exist, perform insert
                    try (PreparedStatement pstmtInsert = dataconnection.prepareStatement(sqlInsert)) {
                        pstmtInsert.setString(1, PlayerName);
                        pstmtInsert.setFloat(2, x);
                        pstmtInsert.setFloat(3, y);
                        pstmtInsert.setFloat(4, z);
                        pstmtInsert.executeUpdate();
                    }
                }
            }
        }
    }


    public static String retrieveRegData(String playerName) throws SQLException {
        String sql = "SELECT password FROM playerRegData WHERE username = ?";
        String password = null;

        try (PreparedStatement pstmt = dataconnection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    password = rs.getString("password");
                } else {
                    LoginGuard.LOGGER.warn("No data found for username: " + playerName);
                    return "NULL";
                }
            }
        } catch (SQLException e) {
            LoginGuard.LOGGER.error("Error retrieving registration data for username " + playerName + ": " + e.getMessage());
            throw e;
        }

        return password;
    }

    public static double[] retrievePosData(String playerName) throws SQLException {
        String sql = "SELECT xpos, ypos, zpos FROM playerPosData WHERE username = ?";
        double[] position = null;

        try (PreparedStatement pstmt = dataconnection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double xpos = rs.getDouble("xpos");
                    double ypos = rs.getDouble("ypos");
                    double zpos = rs.getDouble("zpos");
                    position = new double[]{xpos, ypos, zpos};
                } else {
                    LoginGuard.LOGGER.warn("No data found for username: " + playerName);
                }
            }
        } catch (SQLException e) {
            LoginGuard.LOGGER.error("Error retrieving position data for username " + playerName + ": " + e.getMessage());
            throw e; // Propagate the exception to the caller or handle it as appropriate
        }

        return position;
    }

    public static boolean createDataTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS playerRegData (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "password TEXT NOT NULL" +
                ");";

        try (Statement statement = dataconnection.createStatement()){
            statement.execute(sql);
        } catch (SQLException e) {
            LoginGuard.LOGGER.error("Failed to create Login data Table");
            return false;
        }

        sql = "CREATE TABLE IF NOT EXISTS playerPosData (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "xpos REAL NOT NULL," +
                "ypos REAL NOT NULL," +
                "zpos REAL NOT NULL" +
                ");";

        try (Statement statement = dataconnection.createStatement()){
            statement.execute(sql);
        } catch (SQLException e) {
            LoginGuard.LOGGER.error("Failed to create Positions data Table");
            return false;
        }

        return true;

    }
}
