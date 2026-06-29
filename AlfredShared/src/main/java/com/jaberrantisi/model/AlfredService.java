package com.jaberrantisi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlfredService {
    private UUID instanceId;
    private String serviceName;
    private String ipAddress;
    private Integer port;
    private Instant timeRegistered;
    private Instant lastHeartbeat;
    private Integer ttl;
}
