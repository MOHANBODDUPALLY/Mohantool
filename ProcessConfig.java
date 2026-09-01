package rates_upd;

public class ProcessConfig {

    private String processKey;

    private String referenceField;

    private String inputFieldProperty;

    private String databaseFieldProperty;

    private String module;

    private String updateCondition;

    private String functionNames;

    private String[] inputFields;

    private String[] databaseFields;

    public static ProcessConfig load(
            FieldProperties fp,
            String processName)
            throws Exception {

        String processKey =
            "mercury_exim." + processName;

        String processValue =
            fp.getRequired(processKey);

        String[] parts =
            processValue.split(",", -1);

        if (parts.length < 2) {

            throw new Exception(
                "Invalid process configuration: "
                + processKey
            );
        }

        ProcessConfig config =
            new ProcessConfig();

        config.processKey =
            processKey;

        config.referenceField =
            parts[0].trim();

        config.inputFieldProperty =
            parts[1].trim();

        if (parts.length >= 3) {

            config.databaseFieldProperty =
                parts[2].trim();
        }

        if (parts.length >= 4) {

            config.module =
                parts[3].trim();
        }

        if (parts.length >= 5) {

            config.updateCondition =
                parts[4].trim();
        }

        if (parts.length >= 7) {

            config.functionNames =
                parts[6].trim();
        }

        String inputFieldValue =
            fp.getRequired(
                config.inputFieldProperty
            );

        String dbFieldValue =
            fp.getRequired(
                config.databaseFieldProperty
            );

        config.inputFields =
            splitFields(inputFieldValue);

        config.databaseFields =
            splitFields(dbFieldValue);

        if (config.inputFields.length !=
            config.databaseFields.length) {

            throw new Exception(
                "Input field count and DB field count "
                + "do not match for process: "
                + processName
            );
        }

        return config;
    }

    private static String[] splitFields(
            String value) {

        String[] fields =
            value.split(",");

        for (int i = 0;
             i < fields.length;
             i++) {

            fields[i] =
                fields[i].trim();
        }

        return fields;
    }

    public String getProcessKey() {
        return processKey;
    }

    public String getReferenceField() {
        return referenceField;
    }

    public String getInputFieldProperty() {
        return inputFieldProperty;
    }

    public String getDatabaseFieldProperty() {
        return databaseFieldProperty;
    }

    public String getModule() {
        return module;
    }

    public String getUpdateCondition() {
        return updateCondition;
    }

    public String getFunctionNames() {
        return functionNames;
    }

    public String[] getInputFields() {
        return inputFields;
    }

    public String[] getDatabaseFields() {
        return databaseFields;
    }
}