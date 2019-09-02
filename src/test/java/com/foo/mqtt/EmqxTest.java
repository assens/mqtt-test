package com.foo.mqtt;

import static java.time.Duration.ofMinutes;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

@Testcontainers
@Slf4j
class EmqxTest extends AbstractMqttTest {

  @Container
  private static final GenericContainer<?> broker = new GenericContainer<>("emqx/emqx:latest")
      .withLogConsumer(outputFrame -> log.debug(outputFrame.getUtf8String()))
      .withExposedPorts(1883)
      .withStartupTimeout(ofMinutes(2));

  @BeforeAll
  public static void beforeAll() {
    MqttPortUtil.setMqttPort(broker.getMappedPort(1883));
  }
}
