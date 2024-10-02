package nz.ac.wgtn.swen301.server;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
public class TestPostLogs {
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
    public void testPostLogs_Success() throws ServletException, IOException {
        // set content type for request
        request.setContentType("application/json");

        // Request body
        String jsonLog = "{"
                + "\"id\": \"1\","
                + "\"message\": \"Test log\","
                + "\"timestamp\": \"2023-10-15T14:00:00Z\","
                + "\"thread\": \"main\","
                + "\"logger\": \"Logger1\","
                + "\"level\": \"info\""
                + "}";
        request.setContent(jsonLog.getBytes());

        logsServlet.doPost(request, response);

        assertEquals(201, response.getStatus());

        assertEquals(1, Persistency.DB.size());

        LogEntry addedLog = Persistency.DB.get(0);
        assertEquals("1", addedLog.id());
        assertEquals("Test log", addedLog.message());
        assertEquals("2023-10-15T14:00:00Z", addedLog.timestamp());
        assertEquals("main", addedLog.thread());
        assertEquals("Logger1", addedLog.logger());
        assertEquals("info", addedLog.level());
    }

    @Test
    public void testPostLogs_DuplicateId() throws ServletException, IOException {
        // Add a log with id "1"
        Persistency.DB.add(new LogEntry("1", "Existing log", "2023-10-15T12:00:00Z", "main", "Logger1", "info"));

        request.setContentType("application/json");

        // Duplicate ID test
        String jsonLog = "{"
                + "\"id\": \"1\","
                + "\"message\": \"New log\","
                + "\"timestamp\": \"2023-10-15T14:00:00Z\","
                + "\"thread\": \"main\","
                + "\"logger\": \"Logger1\","
                + "\"level\": \"info\""
                + "}";
        request.setContent(jsonLog.getBytes());

        logsServlet.doPost(request, response);

        assertEquals(409, response.getStatus());

        String errorMessage = response.getErrorMessage();
        assertEquals("Log Entry Already Exists", errorMessage);

        assertEquals(1, Persistency.DB.size());
    }

    @Test
    public void testPostLogs_InvalidLogEntry() throws ServletException, IOException {
        request.setContentType("application/json");

        String jsonLog = "{"
                + "\"id\": \"\","
                + "\"message\": \"\","
                + "\"timestamp\": \"invalid-timestamp\","
                + "\"thread\": \"\","
                + "\"logger\": \"\","
                + "\"level\": \"\""
                + "}";
        request.setContent(jsonLog.getBytes());

        logsServlet.doPost(request,response);

        assertEquals(400, response.getStatus());

        String errorMsg = response.getErrorMessage();
        assertEquals("Invalid Log Entry", errorMsg);

        assertEquals(0, Persistency.DB.size());
    }
}
