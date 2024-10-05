package nz.ac.wgtn.swen301.a3.server;

/**
 * Basic record of logEntry with id,message,timestamp,thread,logger, and level fields with built in getters/setters + equals/hashcode/toString methods
 */
public record LogEntry(String id, String message, String timestamp, String thread, String logger, String level){}