package rates_upd;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class FieldProperties {

    private static final String PROPERTY_FILE =
        "/TFEEAPP/base_domain/base_domain/Utilities/properties/Field.properties";

    private final Properties properties;

    public FieldProperties() throws Exception {

        properties = new Properties();

        System.out.println(
            "Loading properties from:"
        );

        System.out.println(PROPERTY_FILE);

        InputStream input =
            new FileInputStream(PROPERTY_FILE);

        try {

            properties.load(input);

        } finally {

            input.close();
        }

        System.out.println(
            "Field.properties loaded successfully."
        );
    }

    public String get(String key) {

        return properties.getProperty(key);
    }

    public String getRequired(String key)
            throws Exception {

        String value =
            properties.getProperty(key);

        if (value == null ||
            value.trim().length() == 0) {

            throw new Exception(
                "Required property not found: " + key
            );
        }

        return value.trim();
    }

    public boolean contains(String key) {

        return properties.containsKey(key);
    }

    public Properties getProperties() {

        return properties;
    }
}