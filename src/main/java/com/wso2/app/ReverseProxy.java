package com.wso2.app;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;

/**
 * Created by chamile on 11/16/15.
 */
public class ReverseProxy {

    public static String Host = null;
    public static int Host_Port;

    private int connections;
    private boolean is_ssl;
    private int rev_port;
    private EventLoopGroup commonEventLoopGroup;


    //public ReverseProxy(int port) {
        //this.rev_port = port;
    //}

    public ReverseProxy() {
        System.out.println("Initial data");

        //backend address
        Host = "127.0.0.1";
        Host_Port = 5000;

        //Reverse proxy port
        rev_port = 3031;

        is_ssl = false;

        if (is_ssl){
            rev_port = 5050;
        }
        //bossgroupSize

        connections = 1000;
        System.out.println("done");




    }


    private void start() {
        System.out.println("Start server");

        System.out.println("inbound listener port "+ this.rev_port);

        //config ssl
        //SslContext sslContext = null;
        //if()

        // Configure the bootstrap.
        commonEventLoopGroup = new NioEventLoopGroup(2);

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(commonEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TransportHandlerInitializer(Host, Host_Port, connections))
                    .childOption(ChannelOption.AUTO_READ, false);
            //option

            Channel channel = null;
            try {

                channel = serverBootstrap.bind(rev_port).sync().channel();

                channel.closeFuture().sync();


                System.out.println("eubnfqf");
                System.out.println("inbound listener started");

            }catch (InterruptedException e){
                System.out.println("Exception caught");
            }
        }
        finally {
            commonEventLoopGroup.shutdownGracefully();
        }





    }

    public static void main(String[] args) {
        ReverseProxy inboundListener = new ReverseProxy();
        inboundListener.start();
    }
}
