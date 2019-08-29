package com.foo.mqtt;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.artemis.embedded.enabled=true",
    "spring.artemis.embedded.persistent=false",
    "spring.artemis.embedded.data-directory=./target/data",
    "spring.main.banner-mode='off'",
    "logging.level.org.apache.activemq.audit=warn"
})
class ArtemisTest extends AbstractMqttTest {

  @BeforeAll
  public static void beforeAll() {
    MqttPortUtil.setRandomMqttPort();
  }
}
