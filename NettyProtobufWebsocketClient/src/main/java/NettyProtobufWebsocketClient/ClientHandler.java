package NettyProtobufWebsocketClient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;

import java.lang.reflect.Method;

import com.google.protobuf.Message;

public class ClientHandler extends SimpleChannelInboundHandler<Object> {
	WebSocketClientHandshaker handshaker;
    ChannelPromise handshakeFuture;
    
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }
    public WebSocketClientHandshaker getHandshaker() {
        return handshaker;
    }

    public void setHandshaker(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelPromise getHandshakeFuture() {
        return handshakeFuture;
    }

    public void setHandshakeFuture(ChannelPromise handshakeFuture) {
        this.handshakeFuture = handshakeFuture;
    }

    public ChannelFuture handshakeFuture() {
        return this.handshakeFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.println("channelRead0  " + this.handshaker.isHandshakeComplete());
        Channel ch = ctx.channel();
        FullHttpResponse response;
        if (!this.handshaker.isHandshakeComplete()) {
            try {
                response = (FullHttpResponse)msg;
                //握手协议返回，设置结束握手
                this.handshaker.finishHandshake(ch, response);
                //设置成功
                this.handshakeFuture.setSuccess();
                //System.out.println("WebSocket Client connected! response headers[sec-websocket-extensions]:{}"+response.headers());
            } catch (WebSocketHandshakeException var7) {
                FullHttpResponse res = (FullHttpResponse)msg;
                String errorMsg = String.format("WebSocket Client failed to connect,status:%s,reason:%s", res.status(), res.content().toString(CharsetUtil.UTF_8));
                this.handshakeFuture.setFailure(new Exception(errorMsg));
            }
        } else if (msg instanceof FullHttpResponse) {//1.第一次握手请求消息由HTTP协议承载，所以它是一个HTTP消息，执行handleHttpRequest方法来处理WebSocket握手请求。
            response = (FullHttpResponse)msg;
            //this.listener.onFail(response.status().code(), response.content().toString(CharsetUtil.UTF_8));
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        } else if(msg instanceof Packet){
        	Packet packet = (Packet)msg;
        	System.out.println("\n<<<<<<<<<<<<收到服务端协议:"+packet.getCmd()+"<<<<<<<<<<<<");
        	
        	Class<?> clazz = ProtoManager.getRespMap().get(packet.getCmd());
        	Method m = ClassUtils.findMethod(clazz, "getDefaultInstance");
        	Message message = (Message) m.invoke(null);
        	msg = message.newBuilderForType().mergeFrom(packet.getBytes()).build();
        	ProtoPrinter.print(msg);
        }else {//2.客户端通过socket提交请求消息给服务端，WebSocketServerHandler接收到的是已经解码后的WebSocketFrame消息。
            WebSocketFrame frame = (WebSocketFrame)msg;
            if (frame instanceof TextWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
                //this.listener.onMessage(textFrame.text());
                System.out.println("TextWebSocketFrame");
            } else if (frame instanceof BinaryWebSocketFrame) {
            	BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame)frame;
            	System.out.println("BinaryWebSocketFrame received------------------------");
            } else if (frame instanceof PongWebSocketFrame) {
                System.out.println("WebSocket Client received pong");
            } else if (frame instanceof CloseWebSocketFrame) {
                System.out.println("receive close frame");
                //this.listener.onClose(((CloseWebSocketFrame)frame).statusCode(), ((CloseWebSocketFrame)frame).reasonText());
                ch.close();
            }
        }
    }
}
