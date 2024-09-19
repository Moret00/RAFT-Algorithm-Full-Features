# RAFT-Algorithm-Full-Features

This RAFT implementation has been enhanced to simulate real-world distributed systems by incorporating additional features:

- **Dynamic Cluster Reconfiguration**: Servers can be dynamically added or removed without interrupting the entire system, ensuring that new servers synchronize their logs with the current leader to maintain consistency.
- **Persistent Log Storage**: Logs are stored persistently in a file system or database, allowing the system to recover and reload logs after failures, enhancing reliability and durability in case of node restarts or crashes.
- **Fault Simulation**: The system simulates leader and follower failures to observe the recovery process, demonstrating the fault tolerance and resilience of the RAFT algorithm by ensuring new leaders are elected and log consistency is preserved.

The base version of the RAFT Algorithm is available at this link: [RAFT-Algorithm](https://github.com/Moret00/RAFT-Algorithm-Demo).
Further details on this RAFT implementation can be found [here](https://github.com/Moret00/RAFT-Algorithm-Demo/blob/main/RAFT-Algorithm.md).

