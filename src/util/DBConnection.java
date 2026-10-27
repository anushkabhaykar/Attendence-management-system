package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Properties;

/**
 * Utility class managing database connections using JDBC.
 * Supports SQLite (default, zero-configuration) and MySQL.
 * Location: src/util/DBConnection.java
 */
public class DBConnection {

    private static String dbType = "sqlite"; // "sqlite" or "mysql"
    private static String sqliteFile = "attendance_system.db";

    private static String host = "localhost";
    private static String port = "3306";
    private static String dbName = "attendance_system";
    private static String username = "root";
    private static String password = "";

    private static boolean dbInitialized = false;

    static {
        loadProperties();
        loadDrivers();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
                dbType = props.getProperty("db.type", dbType).trim().toLowerCase();
                sqliteFile = props.getProperty("db.sqlite.file", sqliteFile).trim();

                host = props.getProperty("db.host", host).trim();
                port = props.getProperty("db.port", port).trim();
                dbName = props.getProperty("db.name", dbName).trim();
                username = props.getProperty("db.user", username).trim();
                password = props.getProperty("db.password", password).trim();
                System.out.println("[DBConnection] Loaded configurations from db.properties (Database Type: " + dbType + ")");
            } catch (Exception e) {
                System.err.println("[DBConnection] Warning: Could not read db.properties, using defaults. " + e.getMessage());
            }
        }
    }

    private static void loadDrivers() {
        if ("sqlite".equalsIgnoreCase(dbType)) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("[DBConnection] SQLite JDBC Driver not found in classpath: " + e.getMessage());
            }
        } else {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("[DBConnection] MySQL JDBC Driver not found in classpath: " + e.getMessage());
            }
        }
    }

    /**
     * Builds and returns a fresh JDBC Connection.
     */
    public static Connection getConnection() throws SQLException {
        if ("sqlite".equalsIgnoreCase(dbType)) {
            String url = "jdbc:sqlite:" + sqliteFile;
            Connection conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            checkAndInitDatabase(conn);
            return conn;
        } else {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
            return DriverManager.getConnection(url, username, password);
        }
    }

    /**
     * Automatically creates tables and seeds initial data for SQLite if not already present.
     */
    private static synchronized void checkAndInitDatabase(Connection conn) {
        if (dbInitialized || !"sqlite".equalsIgnoreCase(dbType)) {
            return;
        }

        try {
            boolean usersTableExists = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='users'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    usersTableExists = true;
                }
            }

            if (!usersTableExists) {
                System.out.println("[DBConnection] First run detected. Initializing SQLite tables and seeds from schema_sqlite.sql...");
                File schemaFile = new File("schema_sqlite.sql");
                boolean scriptSuccess = false;
                if (schemaFile.exists()) {
                    try {
                        executeSqlScript(conn, schemaFile);
                        scriptSuccess = true;
                    } catch (Exception ex) {
                        System.err.println("[DBConnection] Warning: Error executing schema_sqlite.sql (" + ex.getMessage() + "). Using embedded schema fallback...");
                    }
                }
                if (!scriptSuccess) {
                    executeBuiltinSqliteSchema(conn);
                }
                System.out.println("[DBConnection] SQLite database initialized successfully!");
            }
            dbInitialized = true;
        } catch (Exception e) {
            System.err.println("[DBConnection] Error initializing SQLite database: " + e.getMessage());
        }
    }

    /**
     * Executes SQL statements from a file.
     */
    public static void executeSqlScript(Connection conn, File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int commentIdx = line.indexOf("--");
                if (commentIdx >= 0) {
                    line = line.substring(0, commentIdx);
                }
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    sb.append(trimmed).append(" ");
                }
            }
        }

        String[] statements = sb.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String cleanSql = sql.trim();
                if (!cleanSql.isEmpty()) {
                    stmt.execute(cleanSql);
                }
            }
        }
    }

    /**
     * Fallback embedded SQLite schema creation in case external sql file is missing.
     */
    private static void executeBuiltinSqliteSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT NOT NULL UNIQUE, "
                    + "password_hash TEXT NOT NULL, "
                    + "full_name TEXT NOT NULL, "
                    + "role TEXT NOT NULL DEFAULT 'TEACHER', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS students ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "roll_no TEXT NOT NULL UNIQUE, "
                    + "name TEXT NOT NULL, "
                    + "gender TEXT NOT NULL, "
                    + "dob TEXT NOT NULL, "
                    + "department TEXT NOT NULL, "
                    + "course TEXT NOT NULL, "
                    + "year INTEGER NOT NULL, "
                    + "division TEXT NOT NULL, "
                    + "email TEXT NOT NULL UNIQUE, "
                    + "phone TEXT NOT NULL, "
                    + "face_reference TEXT DEFAULT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_department ON students(department)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_year_div ON students(year, division)");

            stmt.execute("CREATE TABLE IF NOT EXISTS face_data ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "student_id INTEGER NOT NULL, "
                    + "encoding TEXT NOT NULL, "
                    + "sample_count INTEGER NOT NULL DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE)");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_student_face ON face_data(student_id)");

            stmt.execute("CREATE TABLE IF NOT EXISTS attendance ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "student_id INTEGER NOT NULL, "
                    + "attendance_date TEXT NOT NULL, "
                    + "attendance_time TEXT NOT NULL, "
                    + "status TEXT NOT NULL DEFAULT 'PRESENT', "
                    + "method TEXT NOT NULL DEFAULT 'Face Recognition', "
                    + "session_id TEXT NOT NULL DEFAULT 'Morning Session', "
                    + "marked_by TEXT NOT NULL DEFAULT 'System', "
                    + "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, "
                    + "UNIQUE (student_id, attendance_date, session_id))");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(attendance_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_student ON attendance(student_id)");

            // Seeds (Only administrative users, no fake students)
            stmt.execute("INSERT OR IGNORE INTO users (id, username, password_hash, full_name, role) VALUES "
                    + "(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator', 'ADMIN'), "
                    + "(2, 'teacher', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'Prof. Sharma', 'TEACHER')");
        }
    }

    /**
     * Cleans all previous dummy student and attendance records, giving a completely fresh state.
     */
    public static void clearAllPreviousData() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM attendance");
            stmt.execute("DELETE FROM face_data");
            stmt.execute("DELETE FROM students");
            System.out.println("[DBConnection] Cleared all previous students, attendance, and biometric records.");
        } catch (SQLException e) {
            System.err.println("[DBConnection] Error clearing previous data: " + e.getMessage());
        }

        // Clean sample face directory
        try {
            File dir = new File("data/faces");
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        deleteRecursive(f);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }

    /**
     * Tests the database connection.
     * @return true if connected successfully, false otherwise.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Safely closes ResultSet, Statement, and Connection objects.
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignored) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Safe helper to parse java.sql.Date across SQLite (String) and MySQL.
     */
    public static Date safeGetDate(ResultSet rs, String column) throws SQLException {
        try {
            Date d = rs.getDate(column);
            if (d != null) {
                return d;
            }
        } catch (Exception ignored) {
        }
        String str = rs.getString(column);
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        str = str.trim();
        if (str.contains(" ")) {
            str = str.split(" ")[0];
        }
        try {
            return Date.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safe helper to parse java.sql.Time across SQLite (String) and MySQL.
     */
    public static Time safeGetTime(ResultSet rs, String column) throws SQLException {
        try {
            Time t = rs.getTime(column);
            if (t != null) {
                return t;
            }
        } catch (Exception ignored) {
        }
        String str = rs.getString(column);
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        str = str.trim();
        if (str.contains(" ")) {
            str = str.split(" ")[1];
        }
        if (str.contains(".")) {
            str = str.substring(0, str.indexOf('.'));
        }
        try {
            return Time.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isSqlite() {
        return "sqlite".equalsIgnoreCase(dbType);
    }

    public static String getDbType() {
        return dbType;
    }

    public static String getDbName() {
        return isSqlite() ? sqliteFile : dbName;
    }

    public static String getHost() {
        return isSqlite() ? "local" : host;
    }

    public static String getDisplayInfo() {
        if (isSqlite()) {
            return "SQLite (" + sqliteFile + ")";
        } else {
            return "MySQL (" + dbName + "@" + host + ":" + port + ")";
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("   CampusFace Database Setup & Diagnostics Utility      ");
        System.out.println("========================================================");
        System.out.println("Active Database Type : " + dbType.toUpperCase());
        System.out.println("Connection Target    : " + getDisplayInfo());

        boolean reset = args.length > 0 && ("--reset".equalsIgnoreCase(args[0]) || "-reset".equalsIgnoreCase(args[0]));

        if (isSqlite() && reset) {
            File dbFile = new File(sqliteFile);
            if (dbFile.exists()) {
                System.out.println("Reset requested: Removing existing database file '" + sqliteFile + "'...");
                if (!dbFile.delete()) {
                    System.err.println("Notice: Could not delete existing file (it may be open by another process).");
                } else {
                    System.out.println("Existing database file removed.");
                }
            }
            dbInitialized = false;
        }

        try (Connection conn = getConnection()) {
            System.out.println("[SUCCESS] Database connected and schema verified!");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users")) {
                if (rs.next()) {
                    System.out.println("  • Users registered    : " + rs.getInt(1));
                }
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM students")) {
                if (rs.next()) {
                    System.out.println("  • Students enrolled   : " + rs.getInt(1));
                }
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM attendance")) {
                if (rs.next()) {
                    System.out.println("  • Attendance records  : " + rs.getInt(1));
                }
            }
            System.out.println("========================================================");
            System.out.println("Database is completely ready for CampusFace application!");
            System.out.println("========================================================");
        } catch (Exception e) {
            System.err.println("[ERROR] Database operation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
