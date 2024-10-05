package nz.ac.wgtn.swen301.a3.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 *  The Persistency class simulates a database by storing logs of type 'nz.ac.wgtn.swen301.server.LogEntry' in a java.util.List
 */
public class Persistency {
    public static List<LogEntry> DB = new CopyOnWriteArrayList<>();

    /**
     * Clears the Persistency DB used for LogsServlet::doDelete() and for tests
     */
    public static void clearDB() {
        DB.clear();
    }
}
