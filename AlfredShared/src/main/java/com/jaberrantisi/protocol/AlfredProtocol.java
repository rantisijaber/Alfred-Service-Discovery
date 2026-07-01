package com.jaberrantisi.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

@Getter
public class AlfredProtocol {
    private final ObjectMapper mapper;

    public AlfredProtocol() {
        this.mapper = new ObjectMapper();
        this.mapper.findAndRegisterModules();
    }
}
