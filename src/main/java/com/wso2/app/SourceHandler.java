package com.wso2.app;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

/**
 * Created by chamile on 11/15/15.
 */
public class SourceHandler extends ChannelInboundHandlerAdapter {

    private volatile Channel outboundChannel;
    private final int connections;

    public SourceHandler(int connections) {

        this.connections = connections;
    }

    public void channelActive(ChannelHandlerContext context) {      //called after the connection to the server is established
        System.out.println("Channel Active");
        final Channel inChannel = context.channel();

        //start the attempt
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(inChannel.eventLoop())      //sets the EventLoopGroup that will handle all events for the Channel
                .channel(context.channel().getClass())      //specifies the Channel implementation class.
                .handler(new ChannelInitializer<SocketChannel>() { //sets the ChannelHandler that is added to the ChannelPipeline to receive
                    //event notification.

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeLine = socketChannel.pipeline();
                        //Enable HTTPS if necessary.
                        pipeLine.addLast("codec", new HttpClientCodec(102400, 102400, 102400)); //maxInitialLength, maxHeaderSize, maxChunkSize
                        pipeLine.addLast(new TargetHandler(inChannel));
                    }
                })
                .option(ChannelOption.AUTO_READ, false);        //one channel writes a lot of data before the other can consume it
        ChannelFuture channelFuture = bootstrap.connect(ReverseProxy.host, ReverseProxy.hostPort);
        outboundChannel = channelFuture.channel();
        channelFuture.addListener(new ChannelFutureListener() {     //Add ChannelFutureListener to receive notification after read completes
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {       //callback method when it is completed
                if (channelFuture.isSuccess()) {
                    inChannel.read();       //connection complete. start to read
                } else {
                    inChannel.close();      //close connection
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {  //called for each incoming message
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        //ctx.channel().read();       //flushed and read data
                    } else {
                        channelFuture.channel().close();
                    }
                }
            });
        } else {
            System.out.println("Channel not active");
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel Inactive");
        if (outboundChannel != null){
            //close channels after queued write
            closeOnFlush(outboundChannel);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {      //called if an exception was raised during processing
        System.out.println("Exception caught");
        cause.printStackTrace();
        //close channels after queued write
        closeOnFlush(ctx.channel());
    }

    public static void closeOnFlush(Channel channel){
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

        }
    }


}


