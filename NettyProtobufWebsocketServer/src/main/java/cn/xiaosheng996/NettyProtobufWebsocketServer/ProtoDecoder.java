package cn.xiaosheng996.NettyProtobufWebsocketServer;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import tools.CRC16CheckSum;

public class ProtoDecoder extends ChannelInboundHandlerAdapter{
	public static final AttributeKey<Short> RECV_SID = AttributeKey.valueOf("RECV_SID");
	
	private final int limit;
    private final CRC16CheckSum checkSum;

    public ProtoDecoder(CRC16CheckSum checkSum, int limit) {
        this.limit = limit;
        this.checkSum = checkSum;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame frame = (BinaryWebSocketFrame)msg;
            try {
                ByteBuf in = frame.content();
				if (checkSum == null) {
					if (in.readableBytes() < 7)
						throw new IllegalArgumentException();
					byte head = in.readByte();
					short length = in.readShort();
					if (length <= 0 || length > limit)
						throw new IllegalArgumentException();
					int cmd = in.readInt();
					if (in.readableBytes() < length - 4)
						throw new IllegalArgumentException();
					byte[] bytes = new byte[length - 4];
					in.readBytes(bytes);
					ctx.fireChannelRead(new Packet(head, cmd, bytes));
				} else {
					if (in.readableBytes() < 7 + checkSum.length())
						throw new IllegalArgumentException();
					in.markReaderIndex();
					byte head = in.readByte();

					byte[] orig = new byte[checkSum.length()];
					in.readBytes(orig);
					short sid = in.readShort();

					if (!checkSid(ctx, sid))
						throw new IllegalArgumentException();

					short length = in.readShort();
					if (length <= 0 || length > limit)
						throw new IllegalArgumentException();
					int cmd = in.readInt();
					if (in.readableBytes() < length - 4)
						throw new IllegalArgumentException();
					byte[] bytes = new byte[length - 4];
					in.readBytes(bytes);

					byte[] check = new byte[2 + 2 + length];
					in.resetReaderIndex();
					in.skipBytes(1 + checkSum.length());
					in.readBytes(check);

					byte[] compare = checkSum.checksum(check);
					for (int i = 0; i < orig.length; i++) {
						if (orig[i] != compare[i]) {
							throw new IllegalArgumentException();
						}
					}

					ctx.fireChannelRead(new Packet(head, sid, cmd, bytes));
				}
                return;
            } finally {
                frame.release();
            }
        }
        ctx.fireChannelRead(msg);
    }

	private boolean checkSid(ChannelHandlerContext ctx, short sid) {
		Attribute<Short> attr = ctx.channel().attr(RECV_SID);
		if (attr.get() == null) {
			attr.set((short)1);
			return sid == 1;
		}
		if (sid != attr.get() + 1)
			return false;
		if (sid == Short.MAX_VALUE)
			attr.set((short)0);
		else
			attr.set(sid);
		return true;
	}
}
