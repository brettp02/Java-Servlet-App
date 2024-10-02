package nz.ac.wgtn.swen301.server;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
public class TestGetLogs {
    private LogsServlet logsServlet;
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;


    @BeforeEach
    public void setup() {
        logsServlet = new LogsServlet();
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();

        // Clear DB before each test
        Persistency.clearDB();
    }

    @Test
    public void testGetLogs_Success() throws ServletException, IOException {
        /// Add dummy log to DB
        Persistency.DB.add(new LogEntry("1","Test message 1","2023-10-15T12:00:00Z","main","Logger1","info"));
        Persistency.DB.add(new LogEntry("2","Test message 2","2023-10-15T12:05:00Z","main","Logger1","error"));

        // Request params
        request.setParameter("limit","10");
        request.setParameter("level","info");

        // Call doGet()
        logsServlet.doGet(request,response);

        // Assert response status code
        assertEquals(200, response.getStatus());

        assertEquals("application/json",response.getContentType());

        String content = response.getContentAsString();
        assertNotNull(content);
        assertTrue(content.contains("\"id\": \"1\""));
        assertTrue(content.contains("\"message\": \"Test message 1\""));
        assertTrue(content.contains("\"id\": \"2\""));
        assertTrue(content.contains("\"message\": \"Test message 2\""));
    }

    @Test
    public void testGetLogs_MissingParameters() throws ServletException, IOException {
        // No limit/level parameters

        logsServlet.doGet(request,response);

        assertEquals(400, response.getStatus());

        String errorMsg = response.getErrorMessage();
        assertEquals("Missing required parameters: 'limit' and 'level'",errorMsg);
    }

    @Test
    public void testGetLogs_InvalidLimit() throws ServletException, IOException {
        // Set invalid limit param
        request.setParameter("limit","-1");
        request.setParameter("level","info");

        logsServlet.doGet(request,response);

        assertEquals(400, response.getStatus());

        String errorMsg = response.getErrorMessage();
        assertEquals("Limit must be a positive integer", errorMsg);
    }

    @Test
    public void testGetLogs_InvalidLevel() throws ServletException, IOException {
        // Set invalid limit param
        request.setParameter("limit","10");
        request.setParameter("level","invalidLevel");

        logsServlet.doGet(request,response);

        assertEquals(400, response.getStatus());

        String errorMsg = response.getErrorMessage();
        assertEquals("Invalid level parameter", errorMsg);
    }

    @Test
    public void testGetLogs_NoLogsFound() throws ServletException, IOException {
        // set params
        request.setParameter("limit","10");
        request.setParameter("level","info");

        logsServlet.doGet(request,response);

        assertEquals(200, response.getStatus());

        assertEquals("application/json", response.getContentType());

        String content = response.getContentAsString();
        assertEquals("[]",content.trim());
    }

}
