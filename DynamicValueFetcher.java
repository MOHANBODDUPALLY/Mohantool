package rates_upd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class DynamicValueFetcher {

    public static Map<String, String> fetchValues(
            Connection con,
            FieldProperties fp,
            ProcessConfig config,
            String cMainRef)
            throws Exception {

        String processName =
            config.getProcessKey()
                   .substring("mercury_exim.".length());

        String sqlProperty =
            "source_sql." + processName;

        if (!fp.contains(sqlProperty)) {

            throw new Exception(
                "Dynamic source SQL not configured for process: "
                + processName
                + ". Add property: "
                + sqlProperty
            );
        }

        String sql =
            fp.getRequired(sqlProperty);

        System.out.println();
        System.out.println(
            "Fetching dynamic values for process: "
            + processName
        );

        System.out.println(
            "Source SQL: " + sql
        );

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = con.prepareStatement(sql);

            /*
             * The source SQL must contain
             * exactly one '?' for C_MAIN_REF.
             */
            ps.setString(1, cMainRef);

            rs = ps.executeQuery();

            if (!rs.next()) {

                throw new Exception(
                    "No dynamic source data found for C_MAIN_REF: "
                    + cMainRef
                );
            }

            Map<String, String> values =
                new LinkedHashMap<String, String>();

            String[] inputFields =
                config.getInputFields();

            String[] dbFields =
                config.getDatabaseFields();

            for (int i = 0;
                 i < inputFields.length;
                 i++) {

                String jsonField =
                    inputFields[i];

                String dbField =
                    dbFields[i];

                String value;

                try {

                    value =
                        rs.getString(dbField);

                } catch (Exception e) {

                    throw new Exception(
                        "Source SQL does not return column: "
                        + dbField
                        + " for process: "
                        + processName,
                        e
                    );
                }

                values.put(
                    jsonField,
                    value
                );
            }

            return values;

        } finally {

            if (rs != null) {
                rs.close();
            }

            if (ps != null) {
                ps.close();
            }
        }
    }
}