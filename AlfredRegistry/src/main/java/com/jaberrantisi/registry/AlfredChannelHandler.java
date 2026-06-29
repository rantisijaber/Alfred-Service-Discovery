package com.jaberrantisi.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jaberrantisi.model.AlfredObj;
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
        AlfredObj msgObject = alfredProtocol.getMapper().readValue(strMsg, AlfredObj.class);
        String msgType = msgObject.getMessageType();
        if (msgType.equals("registration")) {
            serviceManager.addService(serviceManager.getServiceAsString(msgObject));
            context.writeAndFlush("Object added to map");
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
