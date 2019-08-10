package NettyProtobufWebsocketClient;

import org.apache.log4j.PropertyConfigurator;

import proto.RoleProto.LoginReq_1001001;

/**
 * Java游戏服务器编程
 * @author 小圣996
 * https://www.jianshu.com/u/711bb4362a2a
 */
public class App {
    public static void main( String[] args ){
    	// 装入log4j配置信息
    	PropertyConfigurator.configure("src/main/resources/log4j.properties");
    	
    	NettyWebsocketClient.instance().connect("127.0.0.1", 38996);
    	
    	login();
    }
    
    //请求登录
    private static void login(){
    	LoginReq_1001001.Builder builder = LoginReq_1001001.newBuilder();
    	builder.setAccount("xiaosheng996");
    	builder.setPassword("jianshu");
    	NettyWebsocketClient.instance().send(builder.build());
    }
}
