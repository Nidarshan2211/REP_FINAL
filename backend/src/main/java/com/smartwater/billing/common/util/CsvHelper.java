package com.smartwater.billing.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvHelper {

    /**
     * Parses an input stream of CSV content into a list of row string arrays.
     */
    public static List<List<String>> parseCsv(InputStream inputStream) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines or pure whitespace
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> columns = parseCsvLine(line);
                rows.add(columns);
            }
        }
        return rows;
    }

    /**
     * Parses a single line of CSV text adhering to RFC-4180 rules (quoted fields, commas inside quotes).
     */
    public static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                // Check for escaped quote ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    /**
     * Formats a single CSV field, escaping quotes and wrapping in quotes if needed.
     * Also prevents CSV injection by neutralizing leading formula operators (=, +, -, @).
     */
    public static String escapeCsvField(String value) {
        if (value == null) {
            return "";
        }
        String sanitized = value;
        // Neutralize spreadsheet formula injection
        if (!sanitized.isEmpty() && (sanitized.startsWith("=") || sanitized.startsWith("+") || sanitized.startsWith("-") || sanitized.startsWith("@"))) {
            sanitized = "'" + sanitized;
        }

        if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n") || sanitized.contains("\r")) {
            return "\"" + sanitized.replace("\"", "\"\"") + "\"";
        }
        return sanitized;
    }

    /**
     * Converts a list of records into a valid CSV byte array.
     */
    public static byte[] createCsv(List<String> headers, List<List<String>> dataRows) {
        StringBuilder sb = new StringBuilder();

        // 1. Write Header
        for (int i = 0; i < headers.size(); i++) {
            sb.append(escapeCsvField(headers.get(i)));
            if (i < headers.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("\r\n");

        // 2. Write Data
        for (List<String> row : dataRows) {
            for (int i = 0; i < row.size(); i++) {
                sb.append(escapeCsvField(row.get(i)));
                if (i < row.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("\r\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
