package rates_upd;

import java.sql.Connection;
import java.util.Map;

public class RateUpdateService {

    public void process(
            String cMainRef,
            String processName)
            throws Exception {

        System.out.println();
        System.out.println(
            "=============================================="
        );

        System.out.println(
            "Starting process"
        );

        System.out.println(
            "C_MAIN_REF : " + cMainRef
        );

        System.out.println(
            "PROCESS    : " + processName
        );

        System.out.println(
            "=============================================="
        );

        if (cMainRef == null ||
            cMainRef.trim().length() == 0) {

            throw new Exception(
                "C_MAIN_REF cannot be empty."
            );
        }

        if (processName == null ||
            processName.trim().length() == 0) {

            throw new Exception(
                "PROCESS_NAME cannot be empty."
            );
        }

        Connection con = null;

        try {

            /*
             * 1. Load external Field.properties
             */
            FieldProperties fp =
                new FieldProperties();

            /*
             * 2. Read process configuration
             */
            ProcessConfig config =
                ProcessConfig.load(
                    fp,
                    processName
                );

            System.out.println();
            System.out.println(
                "Process found in Field.properties:"
            );

            System.out.println(
                config.getProcessKey()
            );

            System.out.println(
                "Input field property: "
                + config.getInputFieldProperty()
            );

            System.out.println(
                "DB field property: "
                + config.getDatabaseFieldProperty()
            );

            /*
             * 3. Connect to DB
             */
            con =
                DB.getConnection();

            /*
             * 4. Get C_TEMP_DATA
             */
            String tempData =
                DatabaseClass.selectTempData(
                    con,
                    cMainRef
                );

            if (tempData == null) {

                throw new Exception(
                    "C_TEMP_DATA not found for C_MAIN_REF: "
                    + cMainRef
                );
            }

            System.out.println(
                "C_TEMP_DATA retrieved."
            );

            /*
             * 5. Decompress
             */
            String xml =
                XmlProcessor.decompressTwice(
                    tempData
                );

            System.out.println(
                "C_TEMP_DATA decompressed successfully."
            );

            /*
             * 6. Fetch dynamic rate / amount /
             *    Mercury reference etc.
             */
            Map<String, String> values =
                DynamicValueFetcher.fetchValues(
                    con,
                    fp,
                    config,
                    cMainRef
                );

            System.out.println();
            System.out.println(
                "Dynamic values fetched:"
            );

            for (Map.Entry<String, String> entry :
                 values.entrySet()) {

                System.out.println(
                    entry.getKey()
                    + " = "
                    + entry.getValue()
                );
            }

            /*
             * 7. Update XML
             */
            String updatedXml =
                XmlProcessor.updateXml(
                    xml,
                    values
                );

            /*
             * 8. IMPORTANT:
             *
             * Your original Java code was updating
             * C_TEMP_DATA with plain XML.
             *
             * If the production application expects
             * compressed C_TEMP_DATA, we must compress
             * the XML again before UPDATE.
             *
             * For now this code uses the XML directly.
             *
             * If your C_TEMP_DATA is always stored in
             * compressed/Base64 format, use the
             * compressor component discussed below.
             */

            int updatedRows =
                DatabaseClass.updateTempData(
                    con,
                    cMainRef,
                    updatedXml
                );

            System.out.println();
            System.out.println(
                "Rows updated: " + updatedRows
            );

            if (updatedRows == 0) {

                throw new Exception(
                    "No rows updated for C_MAIN_REF: "
                    + cMainRef
                );
            }

            con.commit();

            System.out.println(
                "COMMIT successful."
            );

            System.out.println(
                "Process completed successfully."
            );

        } catch (Exception e) {

            if (con != null) {

                try {
                    con.rollback();
                } catch (Exception ignored) {
                }
            }

            System.err.println(
                "Process failed."
            );

            throw e;

        } finally {

            DB.closeConnection();
        }
    }
}