package com.foo.mqtt;

import static org.testcontainers.containers.BindMode.READ_ONLY;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

@Testcontainers
@Slf4j
class EjabberdTest extends AbstractMqttTest {

  @Container
  private static final GenericContainer<?> broker = new GenericContainer<>("ejabberd/ecs")
      .withClasspathResourceMapping("/ejabberd/conf/ejabberd.yml", "/home/ejabberd/conf/ejabberd.yml", READ_ONLY)
      .withLogConsumer(outputFrame -> log.debug(outputFrame.getUtf8String()))
      .withExposedPorts(1883);

  @BeforeAll
  public static void beforeAll() {
    MqttPortUtil.setMqttPort(broker.getMappedPort(1883));
  }
}
