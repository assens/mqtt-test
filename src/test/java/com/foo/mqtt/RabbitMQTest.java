package com.foo.mqtt;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
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
class RabbitMQTest extends AbstractMqttTest {

  @Container
  private static final GenericContainer<?> broker = new GenericContainer<>(
      new ImageFromDockerfile()
          .withDockerfileFromBuilder(builder -> builder
              .from("rabbitmq:3.7-management")
              .run("rabbitmq-plugins enable --offline rabbitmq_mqtt")
              .build()))
                  .withLogConsumer(outputFrame -> log.info(outputFrame.getUtf8String()))
                  .withExposedPorts(1883);

  @BeforeAll
  public static void beforeAll() {
    System.setProperty("MQTT_PORT", String.valueOf(broker.getMappedPort(1883)));
  }
}
