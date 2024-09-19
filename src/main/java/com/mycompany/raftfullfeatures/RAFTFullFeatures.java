package com.mycompany.raftfullfeatures;

import java.util.*;
import java.util.concurrent.*;

/*
DISCLAIMER: This RAFT implementation includes additional features for simulating real-world distributed systems:

- Dynamic Cluster Reconfiguration: Servers can be added or removed dynamically, with new servers syncing logs from the leader.
- Persistent Log Storage: Logs are saved in a file system or database, allowing recovery after failures.
- Fault Simulation: Simulates leader and follower failures, showing RAFT’s fault tolerance with leader elections and log consistency.
*/

enum State {
    FOLLOWER, CANDIDATE, LEADER         // Possible Server's states.
}

public class RAFTFullFeatures {
    private State state;    
    private int term;           // Logical time period used to maintain consistency across the distributed system.
    private int votes;          
    private int id;         
    private static final int TIMEOUT = 3000; // Variable used for defining the interval for sending heartbeats by the Leader.

    private static List<RAFTFullFeatures> servers = Collections.synchronizedList(new ArrayList<>());
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> electionFuture;          // Variable used for holding a reference to a scheduled Task that starts an election.
    private static final int MIN_TIMEOUT = 1500;            // Minimum 1.5 seconds.
    private static final int MAX_TIMEOUT = 3000;            // Maximum 3 seconds.
    private List<LogEntry> log = new ArrayList<>();          // List to store log entries.
    private ScheduledFuture<?> heartbeatFuture;
    private boolean active = true;          // If false means the server is down or inactive.
    
    /*
    The `getRandomTimeout` function is responsible for generating a random timeout value within a specified range. 
    This timeout determines how long a server will wait before starting a new election if it hasn't received a heartbeat from a leader.
    */
    private int getRandomTimeout() {
        return MIN_TIMEOUT + new Random().nextInt(MAX_TIMEOUT - MIN_TIMEOUT);
    }
    
    /*
    The initializeLogs() function loads previously saved log entries from persistent storage and adds them to the server's current log, 
    ensuring that the server retains its log history even after a restart, for consistency purposes.
    */
    private void initializeLogs() {
        List<LogEntry> logs = LogStorage.loadLogs();
        log.addAll(logs);
    }

    /*
    RAFTServers inizialization.
    */
    public RAFTFullFeatures(int id) {
        this.id = id;
        this.state = State.FOLLOWER;
        this.term = 0;
        initializeLogs();
    }

    /*
    The `start` function initializes the server and schedules an election timeout task to be executed after a random Delay. 
    This delay is determined by the getRandomTimeout() method, which generates a random timeout value between a minimum and maximum duration.
    */
    public void start() {
        System.out.println("Server " + id + " started as FOLLOWER");
        
        // Schedule the election Time-Out.
        electionFuture = executor.schedule(this::startElection, getRandomTimeout(), TimeUnit.MILLISECONDS);
    }

    /*
    The `startElection` function changes the server's state to CANDIDATE, increments the term, and initiates an election by requesting votes from other servers. 
    It then schedules another election timeout to potentially start a new election if needed.
    */
    private synchronized void startElection() {
        if (state == State.LEADER || !active) return;

        state = State.CANDIDATE;
        term++;
        votes = 1; // Vote for itself
        System.out.println("Server " + id + " starting election, term: " + term);

        for (RAFTFullFeatures server : servers) {
            if (server.id != this.id && server.active) {
                server.requestVote(this);
            }
        }

        electionFuture = executor.schedule(this::startElection, getRandomTimeout(), TimeUnit.MILLISECONDS);
    }
    
    /*
    The `requestVote` function processes a vote request from another server, updating its term and state if needed. 
    It grants a vote if the requesting server's term matches its own and the server is currently a `FOLLOWER`.
    */
    private synchronized void requestVote(RAFTFullFeatures candidate) {
        if (!active) return; // Ignore if the server is not active

        System.out.println("Server " + id + " received vote request from server " + candidate.id + " for term " + candidate.term);

        if (candidate.term > this.term) {
            this.term = candidate.term;
            this.state = State.FOLLOWER;
            System.out.println("Server " + id + " updated term to " + this.term + " and changed state to FOLLOWER");
        }
        // Decide whether to vote for the candidate.
        if (this.state == State.FOLLOWER && candidate.term == this.term) {
            candidate.receiveVote();
            System.out.println("Server " + id + " voted for server " + candidate.id);
        }
    }
    
    /*
    The `receiveVote` function increments the vote count for the Server and checks if it has received a majority of votes. 
    If so, it transitions to the LEADER state.
    */
    private synchronized void receiveVote() {
        votes++;
        System.out.println("Server " + id + " received vote, total votes: " + votes);
        if (state != State.LEADER && votes > servers.size() / 2) {
            becomeLeader();
        }
    }
    
    /*
    The `becomeLeader` function changes the server's state to `LEADER`, prints a message indicating it has become the leader.
    After that will start sending heartbeats to other Servers.
    */
    private synchronized void becomeLeader() {
        state = State.LEADER;
        System.out.println("Server " + id + " is now the leader for term " + term + "!");

        // Add some Log entries with an ID.
        log.add(new LogEntry(0, "Operation 1"));
        log.add(new LogEntry(1, "Operation 2"));

        // Save Logs to file.
        LogStorage.saveLogs(log);

        // Start sending heartbeats.
        sendHeartbeats();

        // Replicate log entries to followers.
        replicateLogEntries();
    }
    
    /*
    The `sendHeartbeats` function sends heartbeat messages to all follower servers to maintain leadership and prevent them from starting new elections. 
    It then schedules the next heartbeat to be sent after a fixed interval.
    */
    private void sendHeartbeats() {
        if (state != State.LEADER || !active) return; // Only the leader should send heartbeats

        System.out.println("Server " + id + " sending heartbeats");
        for (RAFTFullFeatures server : servers) {
            if (server.id != this.id && server.active) {
                server.receiveHeartbeat(this);
            }
        }

        heartbeatFuture = executor.schedule(this::sendHeartbeats, TIMEOUT / 2, TimeUnit.MILLISECONDS);
    }
    
    /*
    The `stopHeartbeats` function stops the scheduled heartbeats and marks the server as inactive.
    */
    public void stopHeartbeats() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(true);
        }
        active = false; // Mark server as inactive
    }

    /*
    The `simulateFailure` function simulates a server failure.
    */
    public void simulateFailure() {
        System.out.println("Server " + id + " is simulated as failed.");
        stopHeartbeats(); // Stop any active heartbeats
        active = false; // Mark server as inactive
        cancelScheduledTasks(); // Cancel any scheduled tasks
    }

    /*
    The `receiveHeartbeat` function updates the server's term and state to `FOLLOWER` if the received heartbeat comes from a Server with a higher term.
    This operation stands for indicating a new Leader.
    */
    private synchronized void receiveHeartbeat(RAFTFullFeatures leader) {
        if (!active) return; // Ignore if the server is not active

        if (leader.term > this.term) {
            this.term = leader.term;
            this.state = State.FOLLOWER;
            System.out.println("Server " + id + " received heartbeat from server " + leader.id + " and changed state to FOLLOWER");
        }
    }
    
    /*
    The `replicateLogEntries` function simulates the replication of log entries from the leader to follower servers.
    */
    private synchronized void replicateLogEntries() {
        for (RAFTFullFeatures server : servers) {
            if (server.id != this.id && server.active) {
                for (int i = 0; i < log.size(); i++) {
                    System.out.println("Log entry replicated to Server " + server.id + ": " + log.get(i));
                }
            }
        }
    }

    /*
    Adds a new server to the cluster and synchronizes it with the leader's log.
    */
    public static synchronized void addServer(RAFTFullFeatures server) {
        servers.add(server);
        System.out.println("Server " + server.id + " added to the cluster.");

        if (server.state == State.FOLLOWER && server.active) {
            RAFTFullFeatures leader = getCurrentLeader();
            if (leader != null) {
                server.log = new ArrayList<>(leader.log);
                System.out.println("Server " + server.id + " synchronized with leader's log.");
            }
        }
    }

    /*
    Removes a Server from the Cluster.
    */
    public static synchronized void removeServer(RAFTFullFeatures server) {
        if (server.active) {
            server.stopHeartbeats();
        }
        servers.remove(server);
        System.out.println("Server " + server.id + " removed from the cluster.");
    }

    /*
    Returns the current Leader of the Cluster.
    */
    private static RAFTFullFeatures getCurrentLeader() {
        for (RAFTFullFeatures server : servers) {
            if (server.state == State.LEADER) {
                return server;
            }
        }
        return null;
    }
    
    /*
    Cancels all scheduled tasks and shuts down the Server.
    */
    public void shutdown() {
        stopHeartbeats();
        if (electionFuture != null) {
            electionFuture.cancel(true);
        }
        executor.shutdown();
        System.out.println("Server " + id + " shutting down.");
    }

    /*
    The `cancelScheduledTasks` function cancels all scheduled tasks for the server.
    */
    private void cancelScheduledTasks() {
        if (electionFuture != null) {
            electionFuture.cancel(true);
        }
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(true);
        }
    }
}
