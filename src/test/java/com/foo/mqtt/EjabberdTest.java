package com.foo.mqtt;

import static org.testcontainers.containers.BindMode.READ_ONLY;

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
class EjabberdTest extends AbstractMqttTest {

  @Container
  private static final GenericContainer<?> broker = new GenericContainer<>("ejabberd/ecs")
      .withLogConsumer(outputFrame -> log.debug(outputFrame.getUtf8String()))
      .withExposedPorts(1883)
      .withClasspathResourceMapping("/ejabberd/conf/ejabberd.yml", "/home/ejabberd/conf/ejabberd.yml", READ_ONLY);

  @BeforeAll
  public static void beforeAll() {
    System.setProperty("MQTT_PORT", String.valueOf(broker.getMappedPort(1883)));
  }
}
