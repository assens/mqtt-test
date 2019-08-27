package com.foo.mqtt;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

public class MqttPortUtil {

  public static String RANDOM_MQTT_PORT = String.valueOf(findAvailableTcpPort(2883, 3883));

  private MqttPortUtil() {
  }

  public static String getMqttPort() {
    return System.getProperty("MQTT_PORT", RANDOM_MQTT_PORT);
  }

}
