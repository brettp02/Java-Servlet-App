package nz.ac.wgtn.swen301.server;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
public class TestStatsHTML {
    private static StatsHTMLServlet servlet;

    @BeforeAll
    public static void setup() {
        servlet = new StatsHTMLServlet();

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
    public void testGetStatsHTML() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/stats/html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        assertEquals(200, response.getStatus(), "HTTP status should be 200");

        assertEquals("text/html",response.getContentType(), "Content Type should be 'text/html'");

        String content = response.getContentAsString();

        Document doc = Jsoup.parse(content);

        Element table = doc.selectFirst("table");
        assertNotNull(table, "HTML should have a <table> element for this task");

        Elements rows = table.select("tr");
        assertFalse(rows.isEmpty(), "Table should have atleast header row");

        Element headerRow = rows.get(0);
        Elements headerCols = headerRow.select("th");
        List<String> expectedHeader = Arrays.asList("logger", "ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");
        List<String> actualHeader = new ArrayList<>();
        for (Element th : headerCols) {
            actualHeader.add(th.text());
        }

        assertEquals(expectedHeader, actualHeader, "Header should match the specification");

        Map<String, Map<String,Integer>> expectedCounts = new HashMap<>();
        List<String> logLevels = Arrays.asList("ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");
        for (LogEntry log : Persistency.DB) {
            String logger = log.logger();
            String level = log.level().toUpperCase();
            expectedCounts.putIfAbsent(logger, new HashMap<>());
            Map<String, Integer> levelMap = expectedCounts.get(logger);
            levelMap.put(level, levelMap.getOrDefault(level, 0) + 1);
        }

        for (int i = 1; i < rows.size(); i++) {
            Element dataRow = rows.get(i);
            Elements dataCols = dataRow.select("td");
            assertEquals(9, dataCols.size(), "Each data row should have 9 cols");

            String loggerName = dataCols.get(0).text();
            assertTrue(expectedCounts.containsKey(loggerName), "unexpected logger found: " + loggerName);

            for (int j = 1; j < dataCols.size(); j++) {
                String level = expectedHeader.get(j);
                int expectedCount = expectedCounts.get(loggerName).getOrDefault(level, 0);
                int actualCount = Integer.parseInt(dataCols.get(j).text());
                assertEquals(expectedCount, actualCount, "Count missmatch for logger: " + loggerName);
            }

        }
    }

    @Test
    public void testGetStatsHTML_Simple() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/stats/html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doGet(request, response);

        assertEquals(200, response.getStatus(), "HTTP Status should be 200 OK");

        assertEquals("text/html", response.getContentType(), "Content-Type should be text/html");

        String content = response.getContentAsString();

        Document doc = Jsoup.parse(content);

        Element table = doc.selectFirst("table");
        assertNotNull(table, "HTML should contain a <table> element");

        Elements rows = table.select("tr");
        assertFalse(rows.isEmpty(), "Table should contain at least one row");

        Element headerRow = rows.get(0);
        Elements headerCols = headerRow.select("th");
        String expectedHeaderRow = "logger\tALL\tTRACE\tDEBUG\tINFO\tWARN\tERROR\tFATAL\tOFF";
        String actualHeaderRow = headerCols.stream().map(Element::text).reduce((a, b) -> a + "\t" + b).orElse("");
        assertEquals(expectedHeaderRow, actualHeaderRow, "Header row mismatch");

        assertTrue(rows.size() >= 2, "Should have at least one data row");
        Element firstDataRow = rows.get(1);
        Elements dataCols = firstDataRow.select("td");
        assertEquals(9, dataCols.size(), "Data row should have 9 columns");
        assertEquals("Logger1", dataCols.get(0).text(), "Logger1 name mismatch");
        assertEquals("0", dataCols.get(1).text(), "Logger1 ALL count mismatch");
        assertEquals("0", dataCols.get(2).text(), "Logger1 TRACE count mismatch");
    }
}
