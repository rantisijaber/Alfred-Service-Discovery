package com.jaberrantisi;


import com.fasterxml.jackson.databind.ObjectMapper;

public class AlfredRegistrySender {
    protected final String host;
    protected final int port;
    private final ObjectMapper mapper;

    public AlfredRegistrySender(String host, int port) {
        this.host = host;
        this.port = port;
        mapper = new ObjectMapper();
    }
}
