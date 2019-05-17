package com.chess.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 
 * @description ： WebSocketConfig
 * @author :Daniel Pine
 * @email :danielpine@sina.com
 * @date :2019年5月17日-上午10:18:07
 */
@Configuration
public class WebSocketConfig {

  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }
}
