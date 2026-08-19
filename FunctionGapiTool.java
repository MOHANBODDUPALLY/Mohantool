package com.sbi.tool;

import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Stream;

public class FunctionGapiTool extends JFrame {

    private final JTextField excelField = new JTextField();
    private final JTextField functionFolderField = new JTextField();
    private final JTextField gapiFolderField = new JTextField();
    private final JTextField outputFolderField = new JTextField();

    private final JTextArea logArea = new JTextArea();

    private static final List<String> FUNCTION_COLUMNS =
            List.of(
                    "BRU FUNC ID",
                    "RETURN FUNC ID",
                    "DEO FUNC ID"
            );

    public FunctionGapiTool() {

        setTitle("Function -> GAPI XML Generator");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setSize(900, 600);

        setLocationRelativeTo(null);

        JPanel inputPanel =
                new JPanel(
                        new GridLayout(5, 3, 8, 8)
                );

        inputPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        10,
                        15
                )
        );

        add(
                inputPanel,
                BorderLayout.NORTH
        );

        addRow(
                inputPanel,
                "Excel File",
                excelField,
                true
        );

        addRow(
                inputPanel,
                "Function Folder",
                functionFolderField,
                false
        );

        addRow(
                inputPanel,
                "GAPI Folder",
                gapiFolderField,
                false
        );

        addRow(
                inputPanel,
                "Output Folder",
                outputFolderField,
                false
        );

        inputPanel.add(new JLabel());

        JButton generateButton =
                new JButton("Generate");

        generateButton.addActionListener(
                e -> generate()
        );

        inputPanel.add(generateButton);

        inputPanel.add(new JLabel());

        logArea.setEditable(false);

        logArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        12
                )
        );

        add(
                new JScrollPane(logArea),
                BorderLayout.CENTER
        );
    }

    private void addRow(
            JPanel panel,
            String label,
            JTextField field,
            boolean file
    ) {

        panel.add(
                new JLabel(label)
        );

        panel.add(field);

        JButton browseButton =
                new JButton("Browse...");

        browseButton.addActionListener(
                e -> {

                    JFileChooser chooser =
                            new JFileChooser();

                    if (file) {

                        chooser.setFileSelectionMode(
                                JFileChooser.FILES_ONLY
                        );

                    } else {

                        chooser.setFileSelectionMode(
                                JFileChooser.DIRECTORIES_ONLY
                        );
                    }

                    if (
                            chooser.showOpenDialog(this)
                                    == JFileChooser.APPROVE_OPTION
                    ) {

                        field.setText(
                                chooser
                                        .getSelectedFile()
                                        .getAbsolutePath()
                        );
                    }
                }
        );

        panel.add(browseButton);
    }

    private void generate() {

        try {

            Path excel =
                    Paths.get(
                            excelField
                                    .getText()
                                    .trim()
                    );

            Path functionFolder =
                    Paths.get(
                            functionFolderField
                                    .getText()
                                    .trim()
                    );

            Path gapiFolder =
                    Paths.get(
                            gapiFolderField
                                    .getText()
                                    .trim()
                    );

            Path outputFolder =
                    Paths.get(
                            outputFolderField
                                    .getText()
                                    .trim()
                    );

            if (!Files.exists(excel)) {

                throw new RuntimeException(
                        "Excel file does not exist."
                );
            }

            if (!Files.isDirectory(functionFolder)) {

                throw new RuntimeException(
                        "Function folder does not exist."
                );
            }

            if (!Files.isDirectory(gapiFolder)) {

                throw new RuntimeException(
                        "GAPI folder does not exist."
                );
            }

            Files.createDirectories(
                    outputFolder
            );

            Set<String> outputLines =
                    new LinkedHashSet<>();

            List<String[]> report =
                    new ArrayList<>();

            processExcel(
                    excel,
                    functionFolder,
                    gapiFolder,
                    outputLines,
                    report
            );

            Path xmlFile =
                    outputFolder.resolve(
                            "generated_gapi.xml"
                    );

            Files.write(
                    xmlFile,
                    outputLines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            Path reportFile =
                    outputFolder.resolve(
                            "generation_report.csv"
                    );

            writeReport(
                    reportFile,
                    report
            );

            log(
                    "======================================"
            );

            log("COMPLETED");

            log(
                    "XML entries generated: "
                            + outputLines.size()
            );

            log(
                    "XML: "
                            + xmlFile
            );

            log(
                    "REPORT: "
                            + reportFile
            );

            log(
                    "======================================"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Generation completed.\n\n"
                            + "XML entries: "
                            + outputLines.size()
                            + "\n\n"
                            + "Output:\n"
                            + xmlFile
                            + "\n\n"
                            + "Report:\n"
                            + reportFile,
                    "Completed",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            log(
                    "ERROR: "
                            + e.getMessage()
            );

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void processExcel(
            Path excel,
            Path functionFolder,
            Path gapiFolder,
            Set<String> outputLines,
            List<String[]> report
    ) throws Exception {

        try (
                InputStream input =
                        Files.newInputStream(excel);

                Workbook workbook =
                        WorkbookFactory.create(input)
        ) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            Row header =
                    sheet.getRow(0);

            if (header == null) {

                throw new RuntimeException(
                        "Excel header row not found."
                );
            }

            Map<String, Integer> columns =
                    new HashMap<>();

            for (Cell cell : header) {

                String value =
                        cellText(cell)
                                .trim()
                                .toUpperCase();

                if (!value.isEmpty()) {

                    columns.put(
                            value,
                            cell.getColumnIndex()
                    );
                }
            }

            for (
                    String columnName :
                    FUNCTION_COLUMNS
            ) {

                Integer column =
                        columns.get(
                                columnName.toUpperCase()
                        );

                if (column == null) {
                    continue;
                }

                for (
                        int rowIndex = 1;
                        rowIndex <= sheet.getLastRowNum();
                        rowIndex++
                ) {

                    Row row =
                            sheet.getRow(rowIndex);

                    if (row == null) {
                        continue;
                    }

                    String functionId =
                            cellText(
                                    row.getCell(column)
                            ).trim();

                    if (functionId.isEmpty()) {
                        continue;
                    }

                    processFunction(
                            functionId,
                            functionFolder,
                            gapiFolder,
                            outputLines,
                            report
                    );
                }
            }
        }
    }

    private void processFunction(
            String functionId,
            Path functionFolder,
            Path gapiFolder,
            Set<String> outputLines,
            List<String[]> report
    ) throws IOException {

        Path functionFile =
                findFile(
                        functionFolder,
                        functionId,
                        List.of(
                                "func_",
                                ""
                        ),
                        ".xml"
                );

        if (functionFile == null) {

            report.add(
                    new String[]{
                            functionId,
                            "",
                            "",
                            "",
                            "FUNCTION FILE NOT FOUND",
                            ""
                    }
            );

            log(
                    "Function file not found: "
                            + functionId
            );

            return;
        }

        String gapiValue =
                getGapiValue(functionFile);

        if (
                gapiValue == null
                        || gapiValue.isBlank()
        ) {

            report.add(
                    new String[]{
                            functionId,
                            functionFile.toString(),
                            "",
                            "",
                            "GAPI TAG NOT FOUND",
                            ""
                    }
            );

            return;
        }

        Path gapiFile =
                findFile(
                        gapiFolder,
                        gapiValue,
                        List.of(
                                "gapi_",
                                ""
                        ),
                        ".js"
                );

        if (gapiFile == null) {

            report.add(
                    new String[]{
                            functionId,
                            functionFile.toString(),
                            gapiValue,
                            "",
                            "GAPI FILE NOT FOUND",
                            ""
                    }
            );

            log(
                    "GAPI file not found: "
                            + gapiValue
            );

            return;
        }

        List<GapiMapping> mappings =
                extractMappings(gapiFile);

        if (mappings.isEmpty()) {

            report.add(
                    new String[]{
                            functionId,
                            functionFile.toString(),
                            gapiValue,
                            gapiFile.toString(),
                            "NO GAPI MAPPING FOUND",
                            ""
                    }
            );

            return;
        }

        for (
                GapiMapping mapping :
                mappings
        ) {

            String line =
                    "<"
                            + functionId
                            + ">gapi_"
                            + mapping.name()
                            + ";"
                            + mapping.value()
                            + "</"
                            + functionId
                            + ">";

            outputLines.add(line);

            report.add(
                    new String[]{
                            functionId,
                            functionFile.toString(),
                            gapiValue,
                            gapiFile.toString(),
                            "SUCCESS",
                            line
                    }
            );

            log(
                    "SUCCESS: "
                            + line
            );
        }
    }

    private static Path findFile(
            Path folder,
            String value,
            List<String> prefixes,
            String extension
    ) throws IOException {

        for (String prefix : prefixes) {

            Path exact =
                    folder.resolve(
                            prefix
                                    + value
                                    + extension
                    );

            if (Files.exists(exact)) {
                return exact;
            }
        }

        try (
                Stream<Path> stream =
                        Files.walk(folder)
        ) {

            return stream
                    .filter(Files::isRegularFile)
                    .filter(
                            p -> {

                                String name =
                                        p.getFileName()
                                                .toString()
                                                .toLowerCase();

                                return
                                        name.contains(
                                                value.toLowerCase()
                                        )
                                                &&
                                        name.endsWith(
                                                extension
                                                        .toLowerCase()
                                        );
                            }
                    )
                    .findFirst()
                    .orElse(null);
        }
    }

    private static String getGapiValue(
            Path functionFile
    ) throws IOException {

        String text =
                Files.readString(
                        functionFile,
                        StandardCharsets.UTF_8
                );

        Pattern pattern =
                Pattern.compile(
                        "<GAPI>\\s*(.*?)\\s*</GAPI>",
                        Pattern.CASE_INSENSITIVE
                                | Pattern.DOTALL
                );

        Matcher matcher =
                pattern.matcher(text);

        if (matcher.find()) {

            return matcher
                    .group(1)
                    .trim();
        }

        return null;
    }

    private static List<GapiMapping> extractMappings(
            Path gapiFile
    ) throws IOException {

        String text =
                Files.readString(
                        gapiFile,
                        StandardCharsets.UTF_8
                );

        List<GapiMapping> mappings =
                new ArrayList<>();

        Set<String> found =
                new HashSet<>();

        /*
         * Example:
         *
         * if (acpt_rej == '6'){
         *     DV.appendField("Payment_Bills_EE2CC");
         * }
         */

        Pattern pattern =
                Pattern.compile(
                        "if\\s*\\([^)]*?"
                                + "==\\s*['\"]([^'\"]+)['\"]"
                                + "\\s*\\)"
                                + "\\s*\\{?\\s*.*?"
                                + "DV\\.appendField"
                                + "\\s*\\("
                                + "\\s*['\"]([^'\"]+)['\"]",
                        Pattern.CASE_INSENSITIVE
                                | Pattern.DOTALL
                );

        Matcher matcher =
                pattern.matcher(text);

        while (matcher.find()) {

            String value =
                    matcher
                            .group(1)
                            .trim();

            String name =
                    matcher
                            .group(2)
                            .trim();

            String key =
                    name
                            + "\u0000"
                            + value;

            if (found.add(key)) {

                mappings.add(
                        new GapiMapping(
                                name,
                                value
                        )
                );
            }
        }

        return mappings;
    }

    private static String cellText(
            Cell cell
    ) {

        if (cell == null) {
            return "";
        }

        return new DataFormatter()
                .formatCellValue(cell);
    }

    private static void writeReport(
            Path file,
            List<String[]> rows
    ) throws IOException {

        try (
                BufferedWriter writer =
                        Files.newBufferedWriter(
                                file,
                                StandardCharsets.UTF_8
                        )
        ) {

            writer.write(
                    "Function ID,Function File,GAPI Value,"
                            + "GAPI File,Status,Output"
            );

            writer.newLine();

            for (String[] row : rows) {

                writer.write(
                        csv(row)
                );

                writer.newLine();
            }
        }
    }

    private static String csv(
            String[] values
    ) {

        StringBuilder result =
                new StringBuilder();

        for (
                int i = 0;
                i < values.length;
                i++
        ) {

            if (i > 0) {
                result.append(',');
            }

            String value =
                    values[i] == null
                            ? ""
                            : values[i];

            value =
                    value.replace(
                            "\"",
                            "\"\""
                    );

            result.append('"')
                    .append(value)
                    .append('"');
        }

        return result.toString();
    }

    private void log(
            String message
    ) {

        logArea.append(
                message
                        + System.lineSeparator()
        );

        logArea.setCaretPosition(
                logArea.getDocument().getLength()
        );
    }

    private record GapiMapping(
            String name,
            String value
    ) {}

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(
                () -> {

                    try {

                        UIManager.setLookAndFeel(
                                UIManager
                                        .getSystemLookAndFeelClassName()
                        );

                    } catch (Exception ignored) {
                    }

                    new FunctionGapiTool()
                            .setVisible(true);
                }
        );
    }
}