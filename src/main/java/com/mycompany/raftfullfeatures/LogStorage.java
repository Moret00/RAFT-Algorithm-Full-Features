package com.mycompany.raftfullfeatures;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

/*
The `LogStorage` class is responsible for providing persistent storage for the RAFT log entries.
This enables the RAFT implementation to save and load logs from a file, allowing recovery from server failures.
*/
public class LogStorage {

    private static final String FILE_NAME = "FILE_NAME.txt";            // The file where log entries will be saved
    
    /*
    The `ensureFileExists` method checks if the log file exists, and if it doesn't, creates a new file.
    This is essential for avoiding errors when attempting to write logs to a non-existent file.
    */
    private static void ensureFileExists() {
        if (!Files.exists(Paths.get(FILE_NAME))) {
            try {
                Files.createFile(Paths.get(FILE_NAME));
                System.out.println("File created: " + FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    The `saveLogs` method takes a list of log entries and writes them to the file.
    Each log entry consists of an ID, operation, and timestamp. This method ensures logs are persistent.
    */
    public static void saveLogs(List<LogEntry> logs) {
        ensureFileExists(); // Ensure the file exists before writing to it.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            for (LogEntry log : logs) {
                writer.write(log.getId() + " | " + log.getOperation() + " | " + log.getTimestamp());
                writer.newLine();           // Write each log entry on a new line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    The `loadLogs` method reads the log entries from the file and loads them into memory.
    If the file is not found, it returns an empty list. Otherwise, it parses each line to reconstruct log entries.
    */
    public static List<LogEntry> loadLogs() {
        List<LogEntry> logs = new ArrayList<>();
        if (!Files.exists(Paths.get(FILE_NAME))) {          // If the file doesn't exist, return an empty log list
            System.out.println("File not found. No logs to load.");
            return logs;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");           // Each line is split into ID, operation, and timestamp
                if (parts.length == 3) {
                    int id = Integer.parseInt(parts[0]);
                    String operation = parts[1];
                    Instant timestamp = Instant.parse(parts[2]);            // Convert the string back to an Instant
                    logs.add(new LogEntry(id, operation));          // Recreate log entry objects
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logs;            // Return the loaded log entries
    }
}
