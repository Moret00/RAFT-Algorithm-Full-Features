# RAFT Full-Features

This RAFT implementation builds upon the core RAFT algorithm by incorporating three key features designed to simulate more realistic distributed system behavior:

1. **Dynamic Cluster Reconfiguration**
The code supports dynamic addition and removal of servers from the cluster. This functionality allows the cluster to scale by adding new servers or decommissioning existing ones:

_AddServer and RemoveServer_ methods: These allow the administrator to introduce new servers to the cluster. The new servers automatically synchronize their logs with the leader to ensure consistency. When a server is removed, its scheduled tasks and heartbeats are cancelled, and it is removed from the active list.
_Log synchronization_: When a new server joins, it copies the leader’s log, ensuring it is in sync with the cluster's current state.<br><br>

2. **Persistent Log Storage**:
Logs are not only stored in memory but are also saved persistently using the file system, allowing the system to recover from failures:

_LogStorage_ class: This class handles the persistent storage of log entries. It uses the file system to save and load logs (saveLogs and loadLogs methods), ensuring that a server can retrieve its log history after a failure.
_File-based recovery_: Logs are saved in a text file, and when a server restarts, it loads the logs from the file into memory, ensuring no data is lost due to crashes or restarts.<br><br>

3. **Fault Simulation**:
The system allows the simulation of server failures and recovers from them gracefully:

_SimulateFailure_ method: This feature simulates the failure of any server. The server stops sending heartbeats and becomes inactive, allowing other servers to detect its failure and trigger a new election.
_Leader election_: When a leader fails, other servers can detect the absence of heartbeats and automatically initiate a new election. This illustrates RAFT’s fault-tolerant capabilities, ensuring that the system continues to function even when some servers go offline.

**Testing Highlights: Dynamic Cluster Reconfiguration**

<br><br>
<p align="center">
  <img src="dynamicAddServer.png" alt="Example Image"/>
</p>

This log represents the Dynamic Cluster Reconfiguration process in action, specifically the addition of a new server (Server 4) to an already running RAFT cluster. Here's a step-by-step breakdown of what each line means:

- _Server 4 added to the cluster_ : This log entry indicates that a new server, Server 4, has been introduced into the cluster. This is handled by the addServer method, which adds Server 4 to the list of active servers.

- _Server 4 synchronized with leader's log._ : After being added, Server 4 synchronizes its log with the leader's existing log. This ensures that Server 4 is up-to-date with all previous operations performed in the cluster before it joined. This is a critical part of maintaining consistency in the RAFT algorithm and is done by copying the leader's log to the new server.

- _Server 4 started as FOLLOWER_ : Once synchronized, Server 4 begins its operation in the FOLLOWER state, which is the default state for all servers when they join a RAFT cluster. As a follower, it will listen for heartbeats from the leader to maintain its state and avoid triggering an election.

- _Server 2 sending heartbeats_ : This indicates that Server 2 is currently the leader of the cluster and is sending out periodic heartbeats to all other servers, including the newly added Server 4. The heartbeats are used to confirm leadership and prevent other servers from starting an election.

- _Server 4 received heartbeat from server 2 and changed state to FOLLOWER_ : Upon receiving a heartbeat from Server 2, Server 4 confirms that Server 2 is the valid leader and remains in the FOLLOWER state. The heartbeat also ensures that Server 4 doesn't start a new election, since it knows the cluster is functioning correctly with a leader in place.<br><br>
