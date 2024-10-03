package nz.ac.wgtn.swen301.server;

import nz.ac.wgtn.swen301.server.LogEntry;
import nz.ac.wgtn.swen301.server.Persistency;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
public class TestStatsCSV {
    private static StatsCSVServlet servlet;

    @BeforeAll
    public static void setup() {
        servlet = new StatsCSVServlet();

        // Initialize Persistency.DB with dummy data
        Persistency.DB.clear();
        Persistency.DB.add(new LogEntry("1", "Info log", "2024-04-01T10:00:00Z", "Thread-1", "Logger1", "INFO"));
        Persistency.DB.add(new LogEntry("2", "Error log", "2024-04-01T10:05:00Z", "Thread-1", "Logger1", "ERROR"));
        Persistency.DB.add(new LogEntry("3", "Debug log", "2024-04-01T10:10:00Z", "Thread-2", "Logger2", "DEBUG"));
        Persistency.DB.add(new LogEntry("4", "Trace log", "2024-04-01T10:15:00Z", "Thread-2", "Logger2", "TRACE"));
        Persistency.DB.add(new LogEntry("5", "Warn log", "2024-04-01T10:20:00Z", "Thread-3", "Logger3", "WARN"));
        Persistency.DB.add(new LogEntry("6", "Fatal log", "2024-04-01T10:25:00Z", "Thread-3", "Logger3", "FATAL"));
        Persistency.DB.add(new LogEntry("7", "Off log", "2024-04-01T10:30:00Z", "Thread-4", "Logger4", "OFF"));
    }

    @Test
    public void testGetStatsCSV() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/stats/csv");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        // Assert HTTP status 200
        assertEquals(200, response.getStatus(), "HTTP Status should be 200 OK");

        // Assert Content-Type is text/csv
        assertEquals("text/csv", response.getContentType(), "Content-Type should be text/csv");

        // Get the response content as a string
        String content = response.getContentAsString();

        String[] lines = content.split("\n");

        // Assert that there are header and data rows
        assertTrue(lines.length >= 2, "CSV should contain at least header and one data row");

        // Parse header
        String header = lines[0];
        String[] headerColumns = header.split("\t");
        List<String> expectedHeader = Arrays.asList("logger", "ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");
        assertEquals(expectedHeader, Arrays.asList(headerColumns), "CSV header should match expected columns");

        // Parse data rows
        Map<String, Map<String, Integer>> expectedCounts = new HashMap<>();

        // Initialize expectedCounts
        for (LogEntry log : Persistency.DB) {
            String logger = log.logger();
            String level = log.level().toUpperCase();
            expectedCounts.putIfAbsent(logger, new HashMap<>());
            Map<String, Integer> levelMap = expectedCounts.get(logger);
            levelMap.put(level, levelMap.getOrDefault(level, 0) + 1);
        }

        System.out.println(expectedCounts);

        // Iterate over data rows
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue; // Skip empty lines
            String[] columns = line.split("\t");

            // First column is logger
            String logger = columns[0];
            assertTrue(expectedCounts.containsKey(logger), "Unexpected logger found: " + logger);

            // Iterate over log levels
            for (int j = 1; j < columns.length; j++) {
                String level = expectedHeader.get(j);
                int expectedCount = expectedCounts.get(logger).getOrDefault(level, 0);
                int actualCount = Integer.parseInt(columns[j]);
                assertEquals(expectedCount, actualCount, "Count mismatch for logger '" + logger + "' and level '" + level + "'");
            }
        }
    }

    @Test
    public void testGetStatsCSV_Simple() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/stats/csv");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        assertEquals(200, response.getStatus(), "HTTP Status should be 200 OK");

        // Assert Content-Type is text/csv
        assertEquals("text/csv", response.getContentType(), "Content-Type should be text/csv");

        // Get the response content as a string
        String content = response.getContentAsString();

        // Print the content
        System.out.println("CSV Response Content:\n" + content);

        // Check header
        String[] lines = content.split("\n");
        assertEquals("logger\tALL\tTRACE\tDEBUG\tINFO\tWARN\tERROR\tFATAL\tOFF", lines[0].trim(), "Header row mismatch");

        // Check first data row
        assertTrue(lines.length >= 2, "Should have at least one data row");
        String[] dataColumns = lines[1].trim().split("\t");
        assertEquals(9, dataColumns.length, "Data row should have 9 columns");
        assertEquals("Logger1", dataColumns[0], "Logger1 name mismatch");
        assertEquals("0", dataColumns[1], "Logger1 ALL count mismatch");
        assertEquals("0", dataColumns[2], "Logger1 TRACE count mismatch");
    }
}
