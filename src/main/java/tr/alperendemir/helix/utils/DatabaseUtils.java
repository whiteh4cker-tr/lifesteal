package tr.alperendemir.helix.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {

    public static Connection makeFileConnection(File file) {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");

            return DriverManager.getConnection("jdbc:hsqldb:file:" + file.getAbsolutePath(), "SA", "");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
