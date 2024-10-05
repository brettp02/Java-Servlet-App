package nz.ac.wgtn.swen301.a3.server;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
public class TestStatsXLS {
    private static StatsExcelServlet servlet;

    @BeforeAll
    public static void setup() {
        servlet = new StatsExcelServlet();

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
    public void testGetStatsXLS() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/stats/excel");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        assertEquals(200, response.getStatus(), "HTTP Status should be 200 OK");

        String expectedContentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        assertEquals(expectedContentType, response.getContentType(), "Content-Type should be application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // Get the response content as a byte array
        byte[] content = response.getContentAsByteArray();
        assertTrue(content.length > 0, "Excel response should not be empty");

        // Parse the Excel content using Apache POI
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            assertEquals(1, workbook.getNumberOfSheets(), "Workbook should contain exactly one sheet");
            Sheet sheet = workbook.getSheet("stats");
            assertNotNull(sheet, "Workbook should contain a sheet named 'stats'");

            Iterator<Row> rowIterator = sheet.iterator();

            assertTrue(rowIterator.hasNext(), "Sheet should contain at least one row for headers");
            Row headerRow = rowIterator.next();
            List<String> expectedHeader = Arrays.asList("logger", "ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");
            List<String> actualHeader = new ArrayList<>();
            for (Cell cell : headerRow) {
                actualHeader.add(cell.getStringCellValue());
            }
            assertEquals(expectedHeader, actualHeader, "Excel header should match expected columns");

            Map<String, Map<String, Integer>> expectedCounts = new HashMap<>();
            List<String> logLevels = Arrays.asList("ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");
            for (LogEntry log : Persistency.DB) {
                String logger = log.logger();
                String level = log.level().toUpperCase();
                expectedCounts.putIfAbsent(logger, new HashMap<>());
                Map<String, Integer> levelMap = expectedCounts.get(logger);
                levelMap.put(level, levelMap.getOrDefault(level, 0) + 1);
            }

            while (rowIterator.hasNext()) {
                Row dataRow = rowIterator.next();
                List<String> rowData = new ArrayList<>();
                for (Cell cell : dataRow) {
                    if (cell.getCellType() == CellType.NUMERIC) {
                        rowData.add(String.valueOf((int) cell.getNumericCellValue()));
                    } else {
                        rowData.add(cell.getStringCellValue());
                    }
                }

                assertEquals(9, rowData.size(), "Each data row should have 9 columns");

                String loggerName = rowData.get(0);
                assertTrue(expectedCounts.containsKey(loggerName), "Unexpected logger found: " + loggerName);

                for (int i = 1; i < rowData.size(); i++) {
                    String level = expectedHeader.get(i);
                    int expectedCount = expectedCounts.get(loggerName).getOrDefault(level, 0);
                    int actualCount = Integer.parseInt(rowData.get(i));
                    assertEquals(expectedCount, actualCount, "Count mismatch for logger '" + loggerName + "' and level '" + level + "'");
                }
            }
        }
    }

    @Test
    public void testGetStatsXLS_Simple() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/stats/excel");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        assertEquals(200, response.getStatus(), "HTTP Status should be 200 OK");

        String expectedContentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        assertEquals(expectedContentType, response.getContentType(), "Content-Type should be application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        byte[] content = response.getContentAsByteArray();
        assertTrue(content.length > 0, "Excel response should not be empty");

        // Parse the Excel content using Apache POI
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            // Verify that there is exactly one sheet named "stats"
            assertEquals(1, workbook.getNumberOfSheets(), "Workbook should contain exactly one sheet");
            Sheet sheet = workbook.getSheet("stats");
            assertNotNull(sheet, "Workbook should contain a sheet named 'stats'");

            // Get the first row
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow, "Header row should exist");
            List<String> expectedHeader = Arrays.asList("logger", "ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");
            List<String> actualHeader = new ArrayList<>();
            for (Cell cell : headerRow) {
                actualHeader.add(cell.getStringCellValue());
            }
            assertEquals(expectedHeader, actualHeader, "Excel header should match expected columns");

            Row firstDataRow = sheet.getRow(1);
            assertNotNull(firstDataRow, "First data row should exist");
            List<String> expectedFirstRow = Arrays.asList("Logger1", "0", "0", "0", "1", "0", "1", "0", "0");
            List<String> actualFirstRow = new ArrayList<>();
            for (Cell cell : firstDataRow) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    actualFirstRow.add(String.valueOf((int) cell.getNumericCellValue()));
                } else {
                    actualFirstRow.add(cell.getStringCellValue());
                }
            }
            assertEquals(expectedFirstRow, actualFirstRow, "First data row should match expected values");
        }
    }
}


