package nz.ac.wgtn.swen301.server;

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
        Persistency.DB.clear();
    }

}
