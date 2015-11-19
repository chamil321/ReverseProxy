package com.wso2.app;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by chamile on 11/16/15.
 */
public class ReverseProxy {

    public static String host = null;
    public static int hostPort;

    private int connections;
    private int localport;
    private EventLoopGroup eventLoopGroup;


    public ReverseProxy() {

        System.out.println("Initial data");
        //backend address
        host = "10.100.4.14";
        hostPort = 9000;

        //Reverse proxy port
        localport = 8443;
        connections = 1000;
    }


    private void start() {

        System.out.println("inbound listening port "+ this.localport);
        // Configure the bootstrap.
        eventLoopGroup = new NioEventLoopGroup(10);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new TransportHandlerInitializer(host, hostPort, connections))
                    .childOption(ChannelOption.AUTO_READ, false);

            Channel channel = null;
            try {
                channel = serverBootstrap.bind(localport).sync().channel();
                channel.closeFuture().sync();
                //system.out.println("test");  //debug print
                System.out.println("Listening");

            }catch (InterruptedException e){
                System.out.println("Exception caught");
            }
        }
        finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        ReverseProxy inboundListener = new ReverseProxy();
        inboundListener.start();
    }
}
