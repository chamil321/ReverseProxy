package com.wso2.app;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


/**
 * Created by chamile on 11/16/15.
 */
public class TransportHandlerInitializer extends ChannelInitializer<SocketChannel>{

    private String host;
    private int port;
   // private final SslContext sslCtx;
    private int connections;

    public TransportHandlerInitializer(String host, int port, int connections) {
        this.host = host;
        this.port = port;
        this.connections = connections;

    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeLine = socketChannel.pipeline();


        pipeLine.addLast(new HttpServerCodec(102400,102400,102400));
        pipeLine.addLast(new SourceHandler(connections));
        pipeLine.addLast(new LoggingHandler(LogLevel.INFO));

    }
}
