package com.mycompany.raftfullfeatures;

import java.io.Serializable;
import java.time.Instant;

/*
The structure of these logs is designed to facilitate the simulation and illustrate how log replication works during leader transitions in the RAFT algorithm.
*/

public class LogEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String operation;
    private final Instant timestamp; // Timestamp added

    public LogEntry(int id, String operation) {
        this.id = id;
        this.operation = operation;
        this.timestamp = Instant.now(); // Current Timestamp setted
    }

    public int getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "LogEntry {id=" + id + ", operation='" + operation + "', timestamp=" + timestamp + "}";
    }
}


