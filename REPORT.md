Relatório da fase 3
================

To ensure fault tolerance, the system must be designed to operate correctly even if some servers fail or if network communication is disrupted. In this architecture, servers broadcast their local state to other servers periodically, allowing them to converge to a consistent view of the system's state.

The gossip architecture is based on the idea of epidemics, where a small number of infective nodes spread information to other nodes, causing the information to spread exponentially until all nodes have the same information. The vectorClocks method is used to accomplish this.

The vector Clock is stored as arrays of integers, although a class was also considered for its readability between files. The first step of the project involves implementing a read operation where clients have a vector clock for each replica. The client sends the request and its previous vector clock, and the replica responds with the response and a new vector clock. The client then updates its previous vector clock from the new one.

The second step of the project involves implementing a write operation where a client sends a request and its previous vector clock to the replica. The replica creates an operation timestamp and adds it to the ledger. If the previous vector clock is less than or equal to the current vector clock, the operation is considered stable, added to the ledger, and executed.

The project aims to implement a gossip-based protocol for a distributed system that can handle read and write operations with vector clocks to prevent inconsistencies and conflicts between replicas.