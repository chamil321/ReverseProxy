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
    }


    private void start() {
        System.out.println("inbound listening port "+ this.localport);
        // Configure the bootstrap.
        eventLoopGroup = new NioEventLoopGroup(10);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)      //Specify the use of an NIO transport Channel
                    .handler(new LoggingHandler(LogLevel.INFO)) //logging
                    .childHandler(new TransportHandlerInitializer(host, hostPort, connections)) //Add an childHandler to the Channel's ChannelPipeline
                    .childOption(ChannelOption.AUTO_READ, false);   //applies to the channel Configuration

            Channel channel = null;
            try {
                channel = serverBootstrap.bind(localport).sync().channel();     //Bind the server; sync waits for the server to close
                channel.closeFuture().sync();   //Close the channel and wait until it is closed
                System.out.println("Listening");

            }catch (InterruptedException e){
                System.out.println("Exception caught");
            }
        }
        finally {
            eventLoopGroup.shutdownGracefully();    //Shutdown the EventLoopGroup, which releases all resources.
        }

    }

    public static void main(String[] args) {
        ReverseProxy inboundListener = new ReverseProxy();      //create listener instance
        inboundListener.start();                                //call start method
    }
}
