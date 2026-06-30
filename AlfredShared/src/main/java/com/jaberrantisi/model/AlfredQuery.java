package com.jaberrantisi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlfredQuery {
    private String serviceName;
    private Instant timestamp;
}
