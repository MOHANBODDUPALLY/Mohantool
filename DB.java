package rates_upd;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {

    private static Connection connection;

    private DB() {
    }

    public static Connection getConnection()
            throws Exception {

        if (connection == null ||
            connection.isClosed()) {

            FieldProperties fp =
                new FieldProperties();

            String driver =
                fp.required("db.driver");

            String url =
                fp.required("db.url");

            String username =
                fp.required("db.username");

            String password =
                fp.required("db.password");

            Class.forName(driver);

            connection =
                DriverManager.getConnection(
                    url,
                    username,
                    password
                );

            connection.setAutoCommit(false);

            System.out.println(
                "Oracle connection established."
            );
        }

        return connection;
    }

    public static void closeConnection() {

        try {

            if (connection != null &&
                !connection.isClosed()) {

                connection.close();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        connection = null;
    }
}