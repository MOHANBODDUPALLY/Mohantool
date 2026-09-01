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
                fp.getRequired("db.driver");

            String url =
                fp.getRequired("db.url");

            String username =
                fp.getRequired("db.username");

            String password =
                fp.getRequired("db.password");

            Class.forName(driver);

            System.out.println(
                "Connecting to Oracle DB..."
            );

            connection =
                DriverManager.getConnection(
                    url,
                    username,
                    password
                );

            connection.setAutoCommit(false);

            System.out.println(
                "Database connection successful."
            );
        }

        return connection;
    }

    public static void closeConnection() {

        try {

            if (connection != null &&
                !connection.isClosed()) {

                connection.close();

                System.out.println(
                    "Database connection closed."
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}