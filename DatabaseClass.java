package rates_upd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseClass {

    private DatabaseClass() {
    }

    public static String selectTempData(
            Connection con,
            String cMainRef)
            throws Exception {

        FieldProperties fp =
            new FieldProperties();

        String tableName =
            fp.getRequired("ledger.table");

        String sql =
            "SELECT C_TEMP_DATA " +
            "FROM " + tableName + " " +
            "WHERE C_MAIN_REF = ? " +
            "AND C_EVENT_STATUS = 'S'";

        System.out.println(
            "Select C_TEMP_DATA SQL: " + sql
        );

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = con.prepareStatement(sql);

            ps.setString(1, cMainRef);

            rs = ps.executeQuery();

            if (rs.next()) {

                String tempData =
                    rs.getString("C_TEMP_DATA");

                System.out.println(
                    "C_TEMP_DATA found."
                );

                return tempData;
            }

            System.out.println(
                "No C_TEMP_DATA found for C_MAIN_REF: "
                + cMainRef
            );

            return null;

        } finally {

            if (rs != null) {
                rs.close();
            }

            if (ps != null) {
                ps.close();
            }
        }
    }

    public static int updateTempData(
            Connection con,
            String cMainRef,
            String newTempData)
            throws Exception {

        FieldProperties fp =
            new FieldProperties();

        String tableName =
            fp.getRequired("ledger.table");

        String sql =
            "UPDATE " + tableName + " " +
            "SET C_TEMP_DATA = ? " +
            "WHERE C_MAIN_REF = ? " +
            "AND C_EVENT_STATUS = 'S'";

        System.out.println(
            "Update C_TEMP_DATA SQL: " + sql
        );

        PreparedStatement ps = null;

        try {

            ps = con.prepareStatement(sql);

            ps.setString(1, newTempData);
            ps.setString(2, cMainRef);

            return ps.executeUpdate();

        } finally {

            if (ps != null) {
                ps.close();
            }
        }
    }
}