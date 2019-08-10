package cn.xiaosheng996.NettyProtobufWebsocketServer;

import org.apache.log4j.PropertyConfigurator;

/**
 * Java游戏服务器编程
 * @author 小圣996
 * https://www.jianshu.com/u/711bb4362a2a
 */
public class App {
    public static void main( String[] args ){
    	// 装入log4j配置信息
    	PropertyConfigurator.configure("src/main/resources/log4j.properties");
    	
    	NettyWebsocketServer tcpServer = new NettyWebsocketServer();
        tcpServer.bind("127.0.0.1", 38996);
        tcpServer.start();
    }
}
