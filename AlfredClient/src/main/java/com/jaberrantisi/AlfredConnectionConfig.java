package com.jaberrantisi;

import com.fasterxml.jackson.databind.ObjectMapper;

public record AlfredConnectionConfig (String host, int port, ObjectMapper mapper) {
    public AlfredConnectionConfig(String host, int port) {
        this(host, port, new ObjectMapper().findAndRegisterModules());
    }
}
