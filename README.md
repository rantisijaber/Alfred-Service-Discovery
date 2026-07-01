# AlfredRegistry

>  **Work in Progress** — This project is under active development and is not yet complete. 

A lightweight service registry for Java microservice architectures. Alfred allows services to register themselves, send periodic heartbeats, and be discovered by other services — similar in role to Netflix Eureka, but without the Spring Cloud dependency.

---

## Overview

Alfred is a standalone Java application that acts as a central registry for your distributed services. Client services register on startup, send heartbeats on an interval, and can query the registry to discover other live services by name.

Services that stop sending heartbeats are automatically transitioned through a `LIMBO` state and eventually marked `DOWN` and evicted from the registry.

---

## Features

- TCP-based communication using a line-delimited JSON protocol
- Service registration and instance tracking by `instanceId`
- Heartbeat-driven health monitoring with configurable TTL per service
- Automatic status transitions: `UP` → `LIMBO` → `DOWN`
- Periodic background sweep to evict dead instances
- Thread-safe registry using `ConcurrentHashMap` and `CopyOnWriteArrayList`
- Client-side load balancing across discovered instances (algorithm TBD)
- Configurable via YAML

---

## Architecture

```
Client Service A  ──┐
Client Service B  ──┼──► AlfredRegistry (Netty TCP Server, NIO)
Client Service C  ──┘         │
                               ├── Boss thread: accepts incoming connections
                               ├── Worker threads: handle each connection concurrently
                               ├── Stores: serviceName → [AlfredService instances]
                               ├── Accepts heartbeats
                               ├── Tracks lastHeartbeat + TTL per instance
                               └── Sweeps and evicts dead instances every 45s
```

Each client service:
1. Registers itself on startup with its name, IP, port, and TTL
2. Sends periodic heartbeats to keep itself alive in the registry
3. Can query the registry to discover instances of other services by name

**Netty** is used as the TCP server layer, configured with a `NioEventLoopGroup` boss/worker model — one boss thread accepts incoming connections and hands them off to a pool of worker threads which process messages concurrently. This means `channelRead` can be invoked simultaneously across multiple worker threads for different connections, all sharing the same `AlfredServiceManager`. The registry is built to handle this safely — `ConcurrentHashMap`, `CopyOnWriteArrayList`, and `volatile` fields are used throughout to ensure thread-safe access to shared state without coarse-grained locking.

---

## Service Lifecycle

| Status  | Meaning                                              |
|---------|------------------------------------------------------|
| `UP`    | Registered and sending heartbeats within expected interval |
| `LIMBO` | Missed heartbeats but still within TTL grace period  |
| `DOWN`  | TTL exceeded — will be evicted on next sweep         |

The status sweep runs every 45 seconds. Any instance that has not sent a heartbeat in over 45 seconds is evaluated against its TTL:

- `timeSinceHeartbeat < ttl` → marked `LIMBO`
- `timeSinceHeartbeat >= ttl` → marked `DOWN` and removed

---

## Notes

- This project is in active development. Peer replication across multiple registry instances is not yet implemented.
- For production use, consider running multiple registry instances and having clients configured with all their addresses for failover.
- Client-side load balancing is planned — when `getActiveServices(String name)` returns multiple instances of a service, the client will automatically select one using a configurable load balancing strategy. The specific algorithm (round-robin, random, least-connections, etc.) has not yet been decided.
