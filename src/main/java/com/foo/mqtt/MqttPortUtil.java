package com.foo.mqtt;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

public class MqttPortUtil {

  private static final ThreadLocal<Integer> MQTT_PORT = new ThreadLocal<>();

  private MqttPortUtil() {
  }

  public static Integer getMqttPort() {
    return MQTT_PORT.get();
  }

  public static void setMqttPort(Integer port) {
    MQTT_PORT.set(port);
  }

  public static void setRandomMqttPort() {
    MQTT_PORT.set(findAvailableTcpPort(2883, 3883));
  }

}
