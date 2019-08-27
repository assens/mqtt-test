package com.foo.mqtt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableAutoConfiguration
@EnableJms
public class MqttTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(MqttTestApplication.class, args);
  }

}
