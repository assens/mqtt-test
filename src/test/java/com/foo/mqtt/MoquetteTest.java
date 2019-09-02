package com.foo.mqtt;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;

@Disabled
class MoquetteTest extends AbstractMqttTest {

  final static Server server = new Server();

  @BeforeAll
  public static void beforeAll() throws IOException {
    MqttPortUtil.setRandomMqttPort();

    final Properties properties = new Properties();
    final MemoryConfig config = new MemoryConfig(properties);
    config.setProperty(BrokerConstants.NETTY_MAX_BYTES_PROPERTY_NAME, "268435455");
    config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, String.valueOf(MqttPortUtil.getMqttPort()));
    config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, "0.0.0.0");
    server.startServer(config);
  }

  @AfterAll
  public static void afterAll() throws IOException {
    server.stopServer();
  }
}
