package com.enzhico.jwt.config;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.enzhico.jwt.common.util.JWTUtil;
import com.enzhico.jwt.config.properties.MyProperties;
import com.enzhico.jwt.dao.entity.ManagerInfo;
import com.enzhico.jwt.service.ApiService;
import com.enzhico.jwt.service.ManagerInfoService;
import com.enzhico.jwt.shiro.ShiroKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * NettySocketConfig
 *
 * @author XiongNeng
 * @version 1.0
 * @since 2018/1/19
 */
@Configuration
public class NettySocketConfig {

    @Resource
    private MyProperties myProperties;

    @Resource
    private ApiService apiService;

    @Resource
    private ManagerInfoService managerInfoService;

    private static final Logger logger = LoggerFactory.getLogger(NettySocketConfig.class);

    @Bean
    public SocketIOServer socketIOServer() {
        /*
         * 创建Socket，并设置监听端口
         */
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // 设置主机名，默认是0.0.0.0
        // config.setHostname("localhost");
        // 设置监听端口
        config.setPort(myProperties.getSocketPort());
        // 协议升级超时时间（毫秒），默认10000。HTTP握手升级为ws协议超时时间
        config.setUpgradeTimeout(10000);
        // Ping消息间隔（毫秒），默认25000。客户端向服务器发送一条心跳消息间隔
        config.setPingInterval(myProperties.getPingInterval());
        // Ping消息超时时间（毫秒），默认60000，这个时间间隔内没有接收到心跳消息就会发送超时事件
        config.setPingTimeout(myProperties.getPingTimeout());
        // 握手协议参数使用JWT的Token认证方案
        config.setAuthorizationListener(data -> {
            // 可以使用如下代码获取用户密码信息
            String token = data.getSingleUrlParam("token");
            String username = JWTUtil.getSocketUsername(token);
            ManagerInfo managerInfo = managerInfoService.findByUsername(username);
            return JWTUtil.verifySocket(token, managerInfo.getPassword());
        });
        return new SocketIOServer(config);
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        return new SpringAnnotationScanner(socketServer);
    }
}
