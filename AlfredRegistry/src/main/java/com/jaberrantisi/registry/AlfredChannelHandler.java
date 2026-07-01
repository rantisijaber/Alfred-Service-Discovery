package com.jaberrantisi.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaberrantisi.model.*;
import com.jaberrantisi.protocol.AlfredProtocol;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;

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
        AlfredMessageType msgType = msgObject.getMessageType();
        switch (msgType) {
            case AlfredMessageType.REGISTRATION:
                AlfredService service = alfredProtocol.getMapper()
                        .convertValue(msgObject.getPayload(), AlfredService.class);
                serviceManager.addService(service);
                context.writeAndFlush("Service successfully registered\n");
                context.close();
                break;
            case AlfredMessageType.HEARTBEAT:
                AlfredHeartbeat heartbeat = alfredProtocol.getMapper()
                        .convertValue(msgObject.getPayload(), AlfredHeartbeat.class);
                serviceManager.acceptHeartbeat(heartbeat);
                context.writeAndFlush("Successfully received heartbeat\n");
                context.close();
                break;
            case AlfredMessageType.QUERY:
                AlfredQuery query = alfredProtocol.getMapper()
                        .convertValue(msgObject.getPayload(), AlfredQuery.class);
                String listServices = protocolMapper.writeValueAsString(new AlfredObj(AlfredMessageType.QUERY_RESPONSE,
                        serviceManager.getServiceList(query))) + '\n';
                context.writeAndFlush(listServices);
                context.close();
                break;
            default:
                context.writeAndFlush("Error: Unknown message type -> " + msgType + '\n');
                context.close();
                break;
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
