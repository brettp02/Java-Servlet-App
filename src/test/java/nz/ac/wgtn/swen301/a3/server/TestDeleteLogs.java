package nz.ac.wgtn.swen301.a3.server;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class TestDeleteLogs {
    private LogsServlet logsServlet;
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;

    @BeforeEach
    public void setup() {
        logsServlet = new LogsServlet();
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();

        // Add test logs to DB for testing doDelete()
        Persistency.DB.add(new LogEntry("1","Test message 1","2023-10-15T12:00:00Z","main","Logger1","info"));
        Persistency.DB.add(new LogEntry("2","Test message 2","2023-10-15T12:05:00Z","main","Logger1","error"));
    }

    @Test
    public void testDeleteLogs_Success() throws ServletException, IOException {
        assertEquals(2, Persistency.DB.size());

        logsServlet.doDelete(request,response);

        assertEquals(200, response.getStatus());

        assertEquals(0, Persistency.DB.size());
    }

    @Test
    public void testDeleteLogs_NoLogs() throws ServletException, IOException {
        Persistency.DB.clear();

        logsServlet.doDelete(request, response);

        assertEquals(200, response.getStatus());

        assertEquals(0, Persistency.DB.size());
    }
}
