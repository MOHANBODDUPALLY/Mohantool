package rates_upd;

import com.cs.eximap.utility.Base64;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

public class XmlProcessor {

    private XmlProcessor() {
    }

    public static String decompress(
            String value)
            throws Exception {

        if (value == null) {
            return null;
        }

        value = value.trim();

        /*
         * If it already looks like XML,
         * do not decompress.
         */
        if (value.indexOf(">") >= 0) {

            return value;
        }

        byte[] bytes =
            Base64.decode(value);

        ByteArrayInputStream bis =
            new ByteArrayInputStream(bytes);

        GZIPInputStream gzip =
            new GZIPInputStream(bis);

        ObjectInputStream objectInput =
            new ObjectInputStream(gzip);

        try {

            Object object =
                objectInput.readObject();

            if (!(object instanceof String)) {

                throw new Exception(
                    "Decompressed object is not String."
                );
            }

            return (String)object;

        } finally {

            objectInput.close();
        }
    }

    public static String decompressTwice(
            String value)
            throws Exception {

        String first =
            decompress(value);

        if (first == null) {
            return null;
        }

        /*
         * Your existing code performs:
         *
         * decompress(decompress(str))
         */
        return decompress(first);
    }

    public static Document toDocument(
            String xml)
            throws Exception {

        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();

        /*
         * Basic XXE protection.
         */
        try {
            factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
            );
        } catch (Exception ignored) {
        }

        DocumentBuilder builder =
            factory.newDocumentBuilder();

        StringReader reader =
            new StringReader(xml);

        InputSource source =
            new InputSource(reader);

        return builder.parse(source);
    }

    public static String updateXml(
            String xml,
            Map<String, String> values)
            throws Exception {

        Document document =
            toDocument(xml);

        Node root =
            document.getDocumentElement();

        NodeList children =
            root.getChildNodes();

        for (int i = 0;
             i < children.getLength();
             i++) {

            Node node =
                children.item(i);

            String nodeName =
                node.getNodeName();

            if (values.containsKey(nodeName)) {

                String newValue =
                    values.get(nodeName);

                if (newValue != null) {

                    node.setTextContent(
                        newValue
                    );

                    System.out.println(
                        "Updated XML field: "
                        + nodeName
                        + " = "
                        + newValue
                    );
                }
            }
        }

        return documentToString(document);
    }

    private static String documentToString(
            Document document)
            throws Exception {

        TransformerFactory factory =
            TransformerFactory.newInstance();

        Transformer transformer =
            factory.newTransformer();

        transformer.setOutputProperty(
            OutputKeys.OMIT_XML_DECLARATION,
            "yes"
        );

        transformer.setOutputProperty(
            OutputKeys.INDENT,
            "no"
        );

        StringWriter writer =
            new StringWriter();

        transformer.transform(
            new DOMSource(document),
            new StreamResult(writer)
        );

        return writer.toString();
    }
}