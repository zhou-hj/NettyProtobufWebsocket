package cn.xiaosheng996.NettyProtobufWebsocketServer;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter{

    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);


    private final ConcurrentMap<Channel, Channel> ref = new ConcurrentHashMap<>();

    protected ServerHandler() {
    	
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        
        log.info("[{}] connected", ctx.channel().remoteAddress());
        System.out.println("----channel:"+ctx.channel());
        ref.put(ctx.channel(), ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet)msg;
        Channel channel = ref.get(ctx.channel());
        
        ProtoManager.handleProto(packet, channel);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (log.isDebugEnabled())
            log.debug("["+ctx.channel().remoteAddress()+"] disconnected");
        Channel channel = ref.remove(ctx.channel());
        if (channel != null)
        	channel.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        if (ctx.channel().isWritable()) {
        	Channel channel = ref.get(ctx.channel());
            if (log.isDebugEnabled())
                log.debug("connection["+channel+"] is available, flush the queue of connection");
            if (channel != null)
            	channel.flush();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("handle packet from ["+ctx.channel().id().asLongText()+"] failed!", cause);
        Channel channel = ref.get(ctx.channel());
        if (channel != null) {
        	channel.close();
        } else {
            log.error("connection["+ctx.channel().id().asLongText()+"] not found in manager");
            ctx.channel().close();
        }
    }
}
