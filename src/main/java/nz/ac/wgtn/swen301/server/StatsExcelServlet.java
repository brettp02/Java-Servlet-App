package nz.ac.wgtn.swen301.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.wgtn.swen301.server.LogEntry;
import nz.ac.wgtn.swen301.server.Persistency;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.*;

/**
 * The StatsExcelServlet class handles GET requests to /stats/excel and
 * returns log statistics in Excel format.
 */
@WebServlet("/stats/excel")
public class StatsExcelServlet extends HttpServlet{

    /**
     * Default constructor
     */
    public StatsExcelServlet() {
        super();
    }

    /**
     * Handles GET requests to /stats/excel
     * Generates Excel statistics for logs.
     *
     * @param req - HttpServletRequest object
     * @param resp - HttpServletResponse object
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Log levels in same structure as example
        List<String> logLevels = Arrays.asList("ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF");

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        //resp.setCharacterEncoding("UTF-8");

        // Apache POI Workbook
        Workbook workbook = new XSSFWorkbook();

        try {
            //create stats sheet
            Sheet sheet = workbook.createSheet("stats");

            // Header row
            Row headerRow = sheet.createRow(0);
            Cell headerCell0 = headerRow.createCell(0);
            headerCell0.setCellValue("logger");

            // Set cell values for header columns
            for (int i = 0; i < logLevels.size(); i++) {
                Cell cell = headerRow.createCell(i + 1);
                cell.setCellValue(logLevels.get(i));
            }

            Set<String> uniqueLoggers = new TreeSet<>();
            for(LogEntry log : Persistency.DB) {
                uniqueLoggers.add(log.logger());
            }

            // Map to hold counts per logger
            Map<String, Map<String, Integer>> loggerLevelCounts = new HashMap<>();

            // Initialize counts to zero for all loggers and levels
            for (String logger : uniqueLoggers) {
                Map<String, Integer> levelCounts = new LinkedHashMap<>();
                for (String level : logLevels) {
                    levelCounts.put(level, 0);
                }
                loggerLevelCounts.put(logger, levelCounts);
            }

            for (LogEntry log : Persistency.DB) {
                String logger = log.logger();
                String level = log.level().toUpperCase(); // Ensure levels are uppercase
                if (loggerLevelCounts.containsKey(logger) && loggerLevelCounts.get(logger).containsKey(level)) {
                    loggerLevelCounts.get(logger).put(level, loggerLevelCounts.get(logger).get(level) + 1);
                }
            }

            int rowNum = 1;
            for (String logger : uniqueLoggers) {
                Row row = sheet.createRow(rowNum++);
                Cell loggerCell = row.createCell(0);
                loggerCell.setCellValue(logger);
                Map<String, Integer> counts = loggerLevelCounts.get(logger);
                for (int i = 0; i < logLevels.size(); i++) {
                    Cell cell = row.createCell(i + 1);
                    cell.setCellValue(counts.get(logLevels.get(i)));
                }
            }

            for (int i = 0; i <= logLevels.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(resp.getOutputStream());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occured when generating spreadheet");
        } finally {
            workbook.close();
        }
    }
}
