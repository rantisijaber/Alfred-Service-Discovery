package com.jaberrantisi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AlfredHeartbeat {
    private UUID instanceId;
    private String serviceName;
    private String ipAddress;
    private Integer port;
    private Instant timeSent;

}
