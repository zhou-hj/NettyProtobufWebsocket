package NettyProtobufWebsocketClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.protobuf.Message;

@SuppressWarnings("rawtypes")
public class ProtoManager {
	
	private static Map<Integer, Class<?>> reqMap = null;
	private static Map<Integer, Class<?>> respMap = null;

    static {
        String packageName = "proto";
        Class clazz = Message.class;
        try {
            reqMap = ClassUtils.getClasses(packageName, clazz, "Req_");
            respMap = ClassUtils.getClasses(packageName, clazz, "Resp_");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static ByteBuf wrapBuffer(Message msg) {
        ByteBufAllocator alloc = ByteBufAllocator.DEFAULT;
        int protocol = 0;
        Set<Entry<Integer, Class<?>>> set = reqMap.entrySet();
        for (Entry<Integer, Class<?>> entry : set) {
            if (entry.getValue().isInstance(msg)) {
                protocol = entry.getKey();
                break;
            }
        }
        byte[] data = msg.toByteArray();
        // 消息长度=协议号4位+数据体长度
        int length = data.length + 4;
        // 数据包=消息长度+协议号+数据体
        // 数据包长度=4+消息长度
        ByteBuf buffer = alloc.buffer(length + 4);
        buffer.writeByte((byte)0x80);
        buffer.writeShort(length);
        buffer.writeInt(protocol);
        buffer.writeBytes(data);

        if (buffer.readableBytes() > 4096) {
//            logger.warn(protocol + " " + buffer.readableBytes() + " too big");
        }
        return buffer;
    }

	public static Map<Integer, Class<?>> getRespMap() {
		return respMap;
	}

	public static void setRespMap(Map<Integer, Class<?>> respMap) {
		ProtoManager.respMap = respMap;
	}
    
	public static int getMessageID(Message msg) {
		int protocol = 0;
        Set<Entry<Integer, Class<?>>> set = reqMap.entrySet();
        for (Entry<Integer, Class<?>> entry : set) {
            if (entry.getValue().isInstance(msg)) {
                protocol = entry.getKey();
                break;
            }
        }
        return protocol;
	}
}
