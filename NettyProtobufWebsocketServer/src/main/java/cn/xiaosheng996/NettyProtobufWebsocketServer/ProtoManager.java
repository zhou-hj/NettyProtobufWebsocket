package cn.xiaosheng996.NettyProtobufWebsocketServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import proto.RoleProto.LoginResp_1001001;

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
        Set<Entry<Integer, Class<?>>> set = respMap.entrySet();
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
        // ByteBuf buffer = Unpooled.buffer(length + 4);
        ByteBuf buffer = alloc.buffer(length + 4);
       /* buffer.writeInt(length);
        buffer.writeInt(protocol);
        buffer.writeBytes(data);*/
        //HEAD_TCP = (byte)0x80
        buffer.writeByte((byte)0x80);
        buffer.writeShort(length);
        buffer.writeInt(protocol);
        buffer.writeBytes(data);

        if (buffer.readableBytes() > 4096) {
//            LogUtil.warn(ProtobufCenter.toString(protocol) + " " + buffer.readableBytes() + " too big");
        }
        return buffer;
    }
    
    public static Map<Integer, Class<?>> getReqMap() {
    	return reqMap;
    }
    
    public static void handleProto(Packet packet, Channel channel){
    	//游戏业务线程池处理游戏逻辑
    	//......
    	//......
    	
    	int cmd = packet.getCmd();
    	if(cmd == 1001001){
    		LoginResp(channel);
    	}
    	
    	//打印协议
    	Class<?> clz = getReqMap().get(packet.getCmd());
		try {
			Method method = clz.getMethod("parseFrom", byte[].class);
			Object object = method.invoke(clz, packet.getBytes());
			
			ProtoPrinter.print(object);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }
    
    public static int getMessageID(Message msg) {
		int protocol = 0;
        Set<Entry<Integer, Class<?>>> set = respMap.entrySet();
        for (Entry<Integer, Class<?>> entry : set) {
            if (entry.getValue().isInstance(msg)) {
                protocol = entry.getKey();
                break;
            }
        }
        return protocol;
	}
    
    //发送协议
	public static void send(Message msg, Channel channel) {
		if (channel == null || msg == null || !channel.isWritable()) {
			return;
		}
		int cmd = ProtoManager.getMessageID(msg);
		Packet packet = new Packet(Packet.HEAD_TCP, cmd, msg.toByteArray());
		channel.writeAndFlush(packet);
	}
	
	private static void LoginResp(Channel channel) {
		LoginResp_1001001.Builder builder = LoginResp_1001001.newBuilder();
		builder.setAccount("xiaosheng996");
		builder.setRid(100000001);
		builder.setLevel((int)(Math.random() * 100));
		send(builder.build(), channel);
	}
}
