package nz.ac.wgtn.swen301.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.wgtn.swen301.server.LogEntry;
import nz.ac.wgtn.swen301.server.Persistency;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * The StatsHTMLServlet class handles GET requests to stats/html and returns log statistics as an HTML web page with an embedded table
 */

@WebServlet("/stats/html")
public class StatsHTMLServlet extends HttpServlet{

    /**
     * Default constructor
     */
    public StatsHTMLServlet() {
        super();
    }

    /**
     * Handles GET requests to /stats/html
     * Generates HTML statistics for logs.
     *
     * @param resp HttpServletResponse object
     * @param req HttpServletRequest object
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Log levels in same structure as example
        List<String> logLevels = Arrays.asList("ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");

        // Set content to text/csv UTF-8
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

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

        // Create HTML page
        StringBuilder sb = new StringBuilder();


        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n<head>\n<title>Log Statistics</title>\n");
        sb.append("<style>\n")
                .append("table { border-collapse: collapse; width: 100%; }\n")
                .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: center; }\n")
                .append("th { background-color: #f2f2f2; }\n")
                .append("tr:hover {background-color: #f5f5f5;}\n")
                .append("</style>\n");
        sb.append("</head>\n<body>\n");

        sb.append("<table>\n");

        // Header Row
        sb.append("<tr><th>Logger</th>");
        for (String level : logLevels) {
            sb.append("<th>").append(level).append("</th>");
        }
        sb.append("</tr>\n");

        // Data Rows
        for (String logger : uniqueLoggers) {
            sb.append("<tr><td>").append(logger).append("</td>");
            Map<String, Integer> counts = loggerLevelCounts.get(logger);
            for (String level : logLevels) {
                sb.append("<td>").append(counts.get(level)).append("</td>");
            }
            sb.append("</tr>\n");
        }

        sb.append("</table>\n");

        sb.append("</body>\n</html>");

        out.write(sb.toString());

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
