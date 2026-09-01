package rates_upd;

public class ProcessConfig {

    private String processName;
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

        String key =
            "mercury_exim." + processName;

        String definition =
            fp.required(key);

        String[] parts =
            definition.split(",", -1);

        ProcessConfig config =
            new ProcessConfig();

        config.processName =
            processName;

        config.processKey =
            key;

        config.referenceField =
            parts[0].trim();

        config.inputFieldProperty =
            parts[1].trim();

        if (parts.length > 2) {
            config.databaseFieldProperty =
                parts[2].trim();
        }

        if (parts.length > 3) {
            config.module =
                parts[3].trim();
        }

        if (parts.length > 4) {
            config.updateCondition =
                parts[4].trim();
        }

        if (parts.length > 6) {
            config.functionNames =
                parts[6].trim();
        }

        config.inputFields =
            split(
                fp.required(
                    config.inputFieldProperty
                )
            );

        config.databaseFields =
            split(
                fp.required(
                    config.databaseFieldProperty
                )
            );

        if (config.inputFields.length !=
            config.databaseFields.length) {

            throw new Exception(
                "Field count mismatch for "
                + processName
            );
        }

        return config;
    }

    private static String[] split(
            String value) {

        String[] result =
            value.split(",");

        for (int i = 0;
             i < result.length;
             i++) {

            result[i] =
                result[i].trim();
        }

        return result;
    }

    public String getProcessName() {
        return processName;
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