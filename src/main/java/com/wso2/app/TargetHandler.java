package com.wso2.app;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

/**
 * Created by chamile on 11/15/15.
 */
public class TargetHandler extends ChannelInboundHandlerAdapter {

    private final Channel inChannel;

    public  TargetHandler(Channel inChannel) {
        this.inChannel = inChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
        System.out.println("Channel active");   //debug print
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        System.out.println("receive data");
        inChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws InterruptedException {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SourceHandler.closeOnFlush(inChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        SourceHandler.closeOnFlush(ctx.channel());
    }

}
