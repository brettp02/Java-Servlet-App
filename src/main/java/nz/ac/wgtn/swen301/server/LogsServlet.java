package nz.ac.wgtn.swen301.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


/**
 *  The LogsServlet Class uses Extends jakarta.servlet.http.HttpServlet to implement the /logs services
 *  by overriding the standard 'doGet()', 'doPost()', and 'doDelete()' methods
 *
 * @studentId - 300635306
 */
public class LogsServlet extends HttpServlet {

    /**
     * Default constructor (public, no parameters)
     */
    public LogsServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }
}
