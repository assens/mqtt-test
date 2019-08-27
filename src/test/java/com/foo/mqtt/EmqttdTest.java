package com.foo.mqtt;

import static org.testcontainers.containers.wait.strategy.Wait.forHealthcheck;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(properties = {
    "spring.artemis.embedded.enabled=false",
    "spring.main.banner-mode='off'",
    "logging.level.org.apache.activemq.audit=warn"
})
@DirtiesContext
@Testcontainers
@Slf4j
//@Disabled
class EmqttdTest extends AbstractMqttTest {

  @Container
  private static final GenericContainer<?> broker = new GenericContainer<>("emqttd/emqttd")
      .withLogConsumer(outputFrame -> log.info(outputFrame.getUtf8String()))
      .withExposedPorts(1883)
      .waitingFor(forHealthcheck());

  @BeforeAll
  public static void beforeAll() {
    System.setProperty("MQTT_PORT", String.valueOf(broker.getMappedPort(1883)));
  }
}
