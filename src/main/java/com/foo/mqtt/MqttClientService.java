package com.foo.mqtt;

import static java.util.Objects.nonNull;
import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@DependsOn("jmsTemplate")
@Slf4j
public class MqttClientService implements MqttCallback {

  private final String clientId;

  @Setter
  private MessageProcessor messageProcessor;

  private MqttClient mqttClient;

  private MemoryPersistence persistence = new MemoryPersistence();
  private ScheduledExecutorService executorService;
  private int corePoolSize = 5;

  public MqttClientService() {
    this("producer", null);
  }

  public MqttClientService(final String clientId, MessageProcessor messageProcessor) {
    this.clientId = clientId;
    this.messageProcessor = messageProcessor;
  }

  @PostConstruct
  public void init() throws MqttException {
    final String serverURI = String.format("tcp://localhost:%s", MqttPortUtil.getMqttPort());
    final MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(false);
    options.setMaxInflight(10);
    options.setServerURIs(new String[] {serverURI});
    options.setMqttVersion(MQTT_VERSION_3_1_1);

    final ThreadFactory threadFactory = new CustomizableThreadFactory("mqtt-client-exec");
    executorService = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    mqttClient = new MqttClient(serverURI, clientId, persistence, executorService);
    mqttClient.setTimeToWait(-1);
    mqttClient.connect(options);
    mqttClient.setCallback(this);
    log.debug("[MQTT][Connected][client: {}]", clientId);
  }

  @PreDestroy
  public void destroy() throws MqttException {
    mqttClient.disconnect();
    executorService.shutdownNow();
    log.debug("[MQTT][Disconnected][client: {}]", clientId);
  }

  @Override
  public void connectionLost(Throwable cause) {
    log.error("[MQTT][connectionLost][{}]", cause.getMessage());
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) {
    log.debug("[MQTT][messageArrived][client: {}][topic: {}][message: {}]", clientId, topic, message);
    if (nonNull(messageProcessor)) {
      messageProcessor.process(message);
    }
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    log.trace("[MQTT][deliveryComplete][token: {}]", token);
  }

  public void publish(final String topic, final MqttMessage message) {
    try {
      mqttClient.publish(topic, message);
    } catch (final MqttException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void subscribe(final String topicFilter) throws MqttException {
    mqttClient.subscribe(topicFilter);

  }

  public void unsubsribe(final String topicFilter) throws MqttException {
    mqttClient.unsubscribe(topicFilter);
  }
}
