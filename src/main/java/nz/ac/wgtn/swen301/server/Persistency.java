package nz.ac.wgtn.swen301.server;

import java.util.ArrayList;
import java.util.List;

/**
 *  The Persistency class simulates a database by storing logs of type 'nz.ac.wgtn.swen301.server.LogEntry' in a java.util.List
 */
public class Persistency {
    public static List<LogEntry> DB = new ArrayList<>();
}
