package com.jaberrantisi.registry;

import com.jaberrantisi.protocol.AlfredProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

/**
 * Hello world!
 *
 */
public class AlfredRegistry {
    public static void main(String[] args) throws InterruptedException {
        AlfredServiceManager serviceManager = new AlfredServiceManager();
        AlfredProtocol alfredProtocol = new AlfredProtocol();

        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup workers = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline()
                                .addLast(new LineBasedFrameDecoder(1024))
                                .addLast(new StringDecoder(StandardCharsets.UTF_8))
                                .addLast(new StringEncoder(StandardCharsets.UTF_8))
                                .addLast(new AlfredChannelHandler(serviceManager, alfredProtocol));
                    }
        });

        bootstrap.bind(8080)
                .sync()
                .channel()
                .closeFuture();
    }
}
