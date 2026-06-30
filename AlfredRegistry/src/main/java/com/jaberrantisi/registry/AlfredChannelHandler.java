package com.jaberrantisi.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaberrantisi.model.*;
import com.jaberrantisi.protocol.AlfredProtocol;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;
import java.util.List;

@AllArgsConstructor
@ChannelHandler.Sharable
public class AlfredChannelHandler extends ChannelInboundHandlerAdapter {
    private final AlfredServiceManager serviceManager;
    private final AlfredProtocol alfredProtocol;

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws JsonProcessingException {
        String strMsg = (String)msg;
        ObjectMapper protocolMapper = alfredProtocol.getMapper();
        AlfredObj msgObject = protocolMapper.readValue(strMsg, AlfredObj.class);
        String msgType = msgObject.getMessageType();
        switch (msgType.toLowerCase()) {
            case "registration":
                AlfredService service = (AlfredService) msgObject.getPayload();
                serviceManager.addService(service);
                context.writeAndFlush("Service successfully registered\n");
                context.close();
            case "heartbeat":
                AlfredHeartbeat heartbeat = (AlfredHeartbeat) msgObject.getPayload();
                serviceManager.acceptHeartbeat(heartbeat);
                context.writeAndFlush("Successfully received heartbeat\n");
                context.close();
            case "query":
                AlfredQuery query = (AlfredQuery) msgObject.getPayload();
                List<AlfredService> ref = serviceManager.getServiceMap().get(query.getServiceName());
                List<AlfredService> copy  = ref == null ? List.of() : List.copyOf(ref)
                        .stream()
                        .filter(alfredService ->
                                alfredService.getStatus() == AlfredServiceStatus.HEALTHY)
                        .toList();
                String listServices = protocolMapper.writeValueAsString(copy);
                context.writeAndFlush("List of " + query.getServiceName() + ": " + listServices + '\n');
                context.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        System.out.println("Client connected " + context.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        System.out.println("Client disconnected " +  context.channel().remoteAddress());
    }
}
