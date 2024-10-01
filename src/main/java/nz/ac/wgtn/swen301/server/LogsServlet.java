package nz.ac.wgtn.swen301.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.*;
import java.util.stream.Collectors;


/**
 *  The LogsServlet Class  Extends jakarta.servlet.http.HttpServlet to implement the /logs services
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
        String limitParam = req.getParameter("limit");
        String levelParam = req.getParameter("level");

        // Make sure parameters are non null
        if (limitParam == null || levelParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Missinng required parameters: 'limit' and 'level'");
            return;
        }

        int limit;
        try {
            limit = Integer.parseInt(limitParam);
            if (limit < 1) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Limit must be a positive integer");
                return;
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Limit must be a integer");
            return;
        }

        // Valid levels
        List<String> validLevels = Arrays.asList("all","debug","info","warn","error","fatal","trace","off");
        if (!validLevels.contains(levelParam.toLowerCase())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Inavalid level parameter");
            return;
        }

        // Severity levels
        Map<String, Integer> levelSeverity = new HashMap<>();
        levelSeverity.put("off",0);
        levelSeverity.put("fatal",1);
        levelSeverity.put("error", 2);
        levelSeverity.put("warn", 3);
        levelSeverity.put("info", 4);
        levelSeverity.put("debug", 5);
        levelSeverity.put("trace", 6);
        levelSeverity.put("all", 7);

        int requestedSeverity = levelSeverity.get(levelParam.toLowerCase());

        List<LogEntry> filteredLogs = Persistency.DB.stream()
                .filter(log -> levelSeverity.get(log.level().toLowerCase()) <= requestedSeverity)
                .sorted(Comparator.comparing(LogEntry::timestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // Convert log to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonResponse = gson.toJson(filteredLogs);

        //set response header/body
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(jsonResponse);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            LogEntry newLog = gson.fromJson(req.getReader(),LogEntry.class);

            if(!isValidLogEntry(newLog)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid Log Entry");
                return;
            }

            boolean exists = Persistency.DB.stream()
                    .anyMatch(log -> log.id().equals(newLog.id()));
            if (exists) {
                resp.sendError(HttpServletResponse.SC_CONFLICT,"Log Entry Already Exists");
                return;
            }

            Persistency.DB.add(newLog);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (JsonSyntaxException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Improper JSON");
        }
    }

    /**
     * Checks if the current LogEntry object is valid
     *
     * @param logEntry - the logEntry object
     * @return
     */
    private boolean isValidLogEntry(LogEntry logEntry) {
        if (logEntry == null || logEntry.id() == null || logEntry.id().isEmpty() || logEntry.message() == null || logEntry.timestamp() == null || logEntry.thread() == null || logEntry.logger() == null || logEntry.level() == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Persistency.DB.clear();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
