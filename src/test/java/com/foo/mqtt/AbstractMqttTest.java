package com.foo.mqtt;

import static java.util.Objects.nonNull;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_HUNDRED_MILLISECONDS;
import static org.awaitility.Durations.FIVE_SECONDS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.javafaker.ChuckNorris;
import com.github.javafaker.Faker;

@TestMethodOrder(OrderAnnotation.class)
public abstract class AbstractMqttTest {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected MqttClientService mqttPublisher;

  protected MqttClientService mqttConsumerCount;
  protected MqttClientService mqttConsumerRetained;

  protected final AtomicInteger publishCount = new AtomicInteger(0);
  protected final AtomicInteger arrivedCount = new AtomicInteger();
  protected final AtomicReference<MqttMessage> lastMessagePublished = new AtomicReference<>();
  protected final AtomicReference<MqttMessage> lastMessageArrived = new AtomicReference<>();

  protected final String topic = "fact";

  private final ChuckNorris chuckNorris = (new Faker()).chuckNorris();
  private final int numberOfMessages = 100;

  @BeforeEach
  public void beforeEach() throws MqttException {
    final MqttMessage clearRetainedMessage = new MqttMessage(new byte[] {});
    clearRetainedMessage.setRetained(true);
    clearRetainedMessage.setQos(1);
    mqttPublisher.publish(topic, clearRetainedMessage);

    publishCount.set(0);
    mqttConsumerCount = new MqttClientService("consumer-count", null);
    mqttConsumerCount.init();
    mqttConsumerCount.setMessageProcessor(messageArrived -> publishCount.incrementAndGet());

    arrivedCount.set(0);
    mqttConsumerRetained = new MqttClientService("consumer-retained",
        messageArrived -> {
          final String payload = new String(messageArrived.getPayload());
          lastMessageArrived.set(messageArrived);
          arrivedCount.incrementAndGet();
          log.info("[MQTT][arrived][retained: {}][duplicate: {}][qos: {}] {}",
              messageArrived.isRetained(), messageArrived.isDuplicate(), messageArrived.getQos(), payload);
        });
    mqttConsumerRetained.init();
  }

  @AfterEach
  public void afterEach() throws MqttException {
    mqttConsumerCount.unsubsribe(topic);
    mqttConsumerRetained.unsubsribe(topic);
    mqttConsumerCount.destroy();
    mqttConsumerRetained.destroy();
  }

  @RepeatedTest(value = 10)
  @Order(1)
  @DisplayName("4.3.1 QoS 0: At most once delivery")
  public void testAtMostOnce() throws MqttException {
    // Act
    publish(0);

    // Assert
    mqttConsumerRetained.subscribe(topic);
    awaitUntilLastMessageArrived();

    assertEquals(1, arrivedCount.get());
    assertLastMessageArrivedEqualsLastMessagePublished();
  }

  @RepeatedTest(value = 10)
  @Order(2)
  @DisplayName("4.3.2 QoS 1: At least once delivery")
  public void testAtLeastOnce() throws MqttException {
    // Act
    publish(1);

    // Assert
    mqttConsumerRetained.subscribe(topic);
    awaitUntilLastMessageArrived();

    assertEquals(1, arrivedCount.get());
    assertLastMessageArrivedEqualsLastMessagePublished();
  }

  @RepeatedTest(value = 10)
  @Order(2)
  @DisplayName("4.3.3 QoS 2: Exactly once delivery")
  public void testExactlyOnce() throws MqttException {
    // Act
    publish(2);

    // Assert
    mqttConsumerRetained.subscribe(topic);
    awaitUntilLastMessageArrived();

    assertEquals(1, arrivedCount.get());
    assertLastMessageArrivedEqualsLastMessagePublished();
  }

  protected void publish(final int qos) throws MqttException {
    mqttConsumerCount.subscribe(topic);
    IntStream.range(0, numberOfMessages).forEach(i -> {
      final String fact = String.format("[%s] %s", i, chuckNorris.fact());
      final MqttMessage message = message(fact, qos, true);
      mqttPublisher.publish(topic, message);
      lastMessagePublished.set(message);
    });
    awaitUntilPiblishCount();

  }

  protected MqttMessage message(final String payload, final int qos, final boolean retained) {
    final MqttMessage message = new MqttMessage();
    message.setQos(qos);
    message.setRetained(retained);
    message.setPayload(payload.getBytes());
    return message;
  }

  private void awaitUntilPiblishCount() {
    await()
        .with()
        .pollDelay(FIVE_HUNDRED_MILLISECONDS)
        .atMost(FIVE_SECONDS)
        .until(() -> publishCount.get() >= numberOfMessages);
    log.info("[MQTT][publish][retained: {}][duplicate: {}][qos: {}] {}",
        lastMessagePublished.get().isRetained(), lastMessagePublished.get().isDuplicate(), lastMessagePublished.get().getQos(), lastMessagePublished.get());
  }

  private void awaitUntilLastMessageArrived() {
    await()
        .pollDelay(FIVE_HUNDRED_MILLISECONDS)
        .atMost(FIVE_SECONDS)
        .until(() -> nonNull(lastMessageArrived.get()));
  }

  private void assertLastMessageArrivedEqualsLastMessagePublished() {
    assertArrayEquals(lastMessagePublished.get().getPayload(), lastMessageArrived.get().getPayload(),
        String.format("\nMessage arrived is different from the last published message!\nPublished: %s\nArrived  : %s\n",
            new String(lastMessagePublished.get().getPayload()), new String(lastMessageArrived.get().getPayload())));
  }

}
