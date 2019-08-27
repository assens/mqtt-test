package com.foo.mqtt;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(properties = {
    "spring.artemis.embedded.enabled=true",
    "spring.artemis.embedded.persistent=false",
    "spring.artemis.embedded.data-directory=./target/data",
    "spring.main.banner-mode='off'",
    "logging.level.org.apache.activemq.audit=warn"
})
@DirtiesContext
class ArtemisTest extends AbstractMqttTest {

}
