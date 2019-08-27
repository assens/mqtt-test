package com.foo.mqtt;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArtemisConfigurationCustomizerConfiguration {

  @Value("${ARTEMIS_OPTIONS:&useEpoll=false&useKQueue=false}")
  private String artemisOptions;

  @Bean
  public ArtemisConfigurationCustomizer customizer() {
    return configuration -> {
      try {
        final String acceptorURI = String.format("tcp://0.0.0.0:%s?protocols=MQTT%s", MqttPortUtil.getMqttPort(), artemisOptions);
        configuration.addAcceptorConfiguration("MQTT", acceptorURI);
      } catch (final Exception e) {
        throw new BeanCreationException(e.getMessage(), e);
      }
    };
  }
}
