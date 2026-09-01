package rates_upd;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class FieldProperties {

    private static final String FILE_PATH =
        "/TFEEAPP/base_domain/base_domain/Utilities/properties/Field.properties";

    private final Properties properties;

    public FieldProperties() throws Exception {

        properties = new Properties();

        System.out.println(
            "Loading Field.properties:"
        );

        System.out.println(FILE_PATH);

        InputStream input =
            new FileInputStream(FILE_PATH);

        try {

            properties.load(input);

        } finally {

            input.close();
        }

        System.out.println(
            "Field.properties loaded."
        );
    }

    public String get(String key) {

        return properties.getProperty(key);
    }

    public String required(String key)
            throws Exception {

        String value =
            properties.getProperty(key);

        if (value == null ||
            value.trim().length() == 0) {

            throw new Exception(
                "Missing property: " + key
            );
        }

        return value.trim();
    }

    public boolean contains(String key) {

        return properties.containsKey(key);
    }
}