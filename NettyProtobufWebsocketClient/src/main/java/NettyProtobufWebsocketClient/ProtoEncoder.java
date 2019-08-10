package NettyProtobufWebsocketClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.CRC16CheckSum;

public class ProtoEncoder extends ChannelOutboundHandlerAdapter {
	private static final Logger log = LoggerFactory.getLogger(ProtoEncoder.class);
	public static final AttributeKey<Short> SEND_SID = AttributeKey.valueOf("SEND_SID");
	
	private final int limit;
    private final CRC16CheckSum checkSum;

    public ProtoEncoder(CRC16CheckSum checkSum, int limit) {
    	this.checkSum = checkSum;
        this.limit = limit;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            if (packet.getBytes().length > limit && log.isWarnEnabled())
				log.warn("cmd[{}], packet size[{}], is over limit[{}]", packet.getCmd(), packet.getBytes().length, limit);
            
	        if (checkSum == null) {
		        int size = 7 + packet.getBytes().length;
		        ByteBuf buf = ctx.alloc().buffer(size);
		        try {
			        buf.writeByte(packet.getHead());
			        buf.writeShort(packet.getBytes().length + 4);
			        buf.writeInt(packet.getCmd());
			        buf.writeBytes(packet.getBytes());
			        msg = new BinaryWebSocketFrame(buf);
		        } catch (Exception e) {
			        buf.release();
			        throw e;
		        }
	        } else {
		        int size = 7 + packet.getBytes().length + checkSum.length();
		        ByteBuf buf = ctx.alloc().buffer(size);
		        try {
			        buf.writeByte(packet.getHead());
			        size = 2 + 2 + 4 + packet.getBytes().length;
			        ByteBuf temp = Unpooled.buffer(size, size);
			        temp.writeShort(getSid(ctx));
			        temp.writeShort(packet.getBytes().length + 4);
			        temp.writeInt(packet.getCmd());
			        temp.writeBytes(packet.getBytes());
			        byte[] check = checkSum.checksum(temp.array());
			        buf.writeBytes(check);
			        buf.writeBytes(temp);
			        temp.release();
			        msg = new BinaryWebSocketFrame(buf);
		        } catch (Exception e) {
			        buf.release();
			        throw e;
		        }
	        }
        }
        super.write(ctx, msg, promise);
    }

	private short getSid(ChannelHandlerContext ctx) {
		Attribute<Short> attr = ctx.channel().attr(SEND_SID);
		if (attr.get() == null) {
			attr.set((short)1);
			return 1;
		}
		short sid = (short)(attr.get() + 1);
		if (sid == Short.MAX_VALUE) {
			attr.set((short)0);
		} else {
			attr.set(sid);
		}
		return sid;
	}
}
