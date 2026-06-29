package com.jaberrantisi.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaberrantisi.model.AlfredMessageType;
import com.jaberrantisi.model.AlfredObj;
import lombok.Getter;

@Getter
public class AlfredProtocol {
    private final ObjectMapper mapper = new ObjectMapper();

    public AlfredMessageType getMessageType(AlfredObj message) {
        String messageType = message.getMessageType().toLowerCase();
        switch (messageType) {
            case "heartbeat": return AlfredMessageType.HEARTBEAT;
            case "registration": return AlfredMessageType.REGISTRATION;
            default: throw new RuntimeException(messageType + ": Is not a valid message type");
        }
    }


}
