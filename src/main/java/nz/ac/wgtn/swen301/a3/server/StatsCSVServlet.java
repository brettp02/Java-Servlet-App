package nz.ac.wgtn.swen301.a3.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * The StatsCSVServlet handles GET requests to stats/csv and
 * returns log statistics in a specified CSV format.
 */
@WebServlet("/stats/csv")
public class StatsCSVServlet extends HttpServlet{

    /**
     *  Default Constructor
     */
    public StatsCSVServlet() {
        super();
    }

    /**
     * Handles GET requests to stats/csv
     * Generate CSV statistics for logs
     *
     * @param req - HttpServletRequests object
     * @param resp - HttpServletResponse Object
     * @throws ServletException
     * @throws IOException
     *
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Log levels in same structure as example
        List<String> logLevels = Arrays.asList("ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");

        // Set content to text/csv UTF-8
        resp.setContentType("text/csv");
        //resp.setCharacterEncoding("UTF-8");

        // Get printwriter obj
        PrintWriter out = resp.getWriter();

        Set<String> uniqueLoggers = new TreeSet<>();
        for (LogEntry log : Persistency.DB) {
            uniqueLoggers.add(log.logger());
        }

        // Map for counts
        Map<String, Map<String,Integer>> loggerLevelCounts = new HashMap<>();

        for (String logger : uniqueLoggers) {
            Map<String, Integer> levelCounts = new LinkedHashMap<>();
            for (String level : logLevels) {
                levelCounts.put(level, 0);
            }
            loggerLevelCounts.put(logger,levelCounts);
        }

        for (LogEntry log : Persistency.DB) {
            String logger = log.logger();
            String level = log.level().toUpperCase();
            if (loggerLevelCounts.containsKey(logger) && loggerLevelCounts.get(logger).containsKey(level)) {
                loggerLevelCounts.get(logger).put(level, loggerLevelCounts.get(logger).get(level) + 1);
            }
        }

        // Build CSV
        StringBuilder sb = new StringBuilder();

        sb.append("logger");
        for (String level : logLevels) {
            sb.append("\t").append(level);
        }
        sb.append("\n");

        for (String logger : uniqueLoggers) {
            sb.append(logger);
            Map<String, Integer> counts = loggerLevelCounts.get(logger);
            for (String level : logLevels) {
                sb.append("\t").append(counts.get(level));
            }
            sb.append("\n");
        }

        out.write(sb.toString());
        resp.setStatus(HttpServletResponse.SC_OK);

        out.flush();
        out.close();
    }
}
