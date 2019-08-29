package com.foo.mqtt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

@Testcontainers
@Slf4j
@Disabled
class VernemqTest extends AbstractMqttTest {

  @Container
  private static final GenericContainer<?> broker = new GenericContainer<>("erlio/docker-vernemq")
      .withLogConsumer(outputFrame -> log.debug(outputFrame.getUtf8String()))
      .withEnv("DOCKER_VERNEMQ_ALLOW_ANONYMOUS", "on")
      .withExposedPorts(1883)
      // .withStartupTimeout(Duration.ofMinutes(2));
      .waitingFor(Wait.forHealthcheck());

  @BeforeAll
  public static void beforeAll() {
    MqttPortUtil.setMqttPort(broker.getMappedPort(1883));
  }
}
