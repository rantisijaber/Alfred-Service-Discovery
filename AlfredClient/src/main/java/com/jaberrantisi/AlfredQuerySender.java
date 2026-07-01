package com.jaberrantisi;

import com.jaberrantisi.model.AlfredMessageType;
import com.jaberrantisi.model.AlfredObj;
import com.jaberrantisi.model.AlfredQuery;
import com.jaberrantisi.model.AlfredService;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.net.Socket;
import java.time.Instant;
import java.util.List;

public class AlfredQuerySender  {
    private final AlfredConnectionConfig config;

    public AlfredQuerySender(String host, int port) {
        this.config = new AlfredConnectionConfig(host, port);
    }

    public List<AlfredService> getActiveServices(String name) throws IOException {
        AlfredObj msg = AlfredObj.builder()
                .messageType(AlfredMessageType.QUERY)
                .payload(new AlfredQuery(name, Instant.now()))
                .build();
        String msgJson = config.mapper().writeValueAsString(msg);
        try (Socket socket = new Socket(config.host(), config.port())) {
            socket.setSoTimeout(5000);
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer.write(msgJson);
            writer.newLine();
            writer.flush();
            String strResult = reader.readLine();
            if (strResult == null) throw new IOException("Did not receive list of services");
            AlfredObj obj = config.mapper()
                    .readValue(strResult, AlfredObj.class);
            return config.mapper()
                    .convertValue(obj.getPayload(), new TypeReference<>() {});
        }
    }

}
