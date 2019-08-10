package cn.xiaosheng996.NettyProtobufWebsocketServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProtoEncoder extends ChannelOutboundHandlerAdapter{
	private static final Logger log = LoggerFactory.getLogger(ProtoEncoder.class);

	private final int limit;

	public ProtoEncoder(int limit) {
		this.limit = limit;
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof Packet) {
			Packet packet = (Packet) msg;
			if (packet.getBytes().length > limit && log.isWarnEnabled())
				log.warn("cmd[{}], packet size[{}], is over limit[{}]", packet.getCmd(), packet.getBytes().length, limit);

			int size = 7 + packet.getBytes().length;
			ByteBuf buf = ctx.alloc().buffer(size);
			try {
				buf.writeByte(packet.getHead());
				buf.writeShort(packet.getBytes().length + 4);
				buf.writeInt(packet.getCmd());
				buf.writeBytes(packet.getBytes());
				BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buf);
				ctx.writeAndFlush(frame);
				return;
			} catch (Exception e) {
				buf.release();
			}
		}
		ctx.writeAndFlush(msg);
	}
}
