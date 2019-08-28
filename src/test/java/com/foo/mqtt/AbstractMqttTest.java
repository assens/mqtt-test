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
import org.junit.jupiter.api.RepetitionInfo;
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
  protected MqttClientService mqttConsumerBeforePublish;
  protected MqttClientService mqttConsumerAfterPublish;

  protected final AtomicInteger publishCount = new AtomicInteger(0);

  protected final AtomicInteger arrivedCountBeforePublish = new AtomicInteger();
  protected final AtomicInteger arrivedCountAferPublish = new AtomicInteger();

  protected final AtomicReference<MqttMessage> lastMessagePublished = new AtomicReference<>();
  protected final AtomicReference<MqttMessage> lastMessageArrivedOnConsumerBeforePublish = new AtomicReference<>();
  protected final AtomicReference<MqttMessage> lastMessageArrivedOnConsumerAfterPublish = new AtomicReference<>();

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

    arrivedCountBeforePublish.set(0);
    mqttConsumerBeforePublish = new MqttClientService("consumer-before",
        messageArrived -> {
          final String payload = new String(messageArrived.getPayload());
          lastMessageArrivedOnConsumerBeforePublish.set(messageArrived);
          arrivedCountBeforePublish.incrementAndGet();
          log.debug("[MQTT][before ][retained: {}][duplicate: {}][qos: {}] {}",
              messageArrived.isRetained(), messageArrived.isDuplicate(), messageArrived.getQos(), payload);
        });
    mqttConsumerBeforePublish.init();

    arrivedCountAferPublish.set(0);
    mqttConsumerAfterPublish = new MqttClientService("consumer-after",
        messageArrived -> {
          final String payload = new String(messageArrived.getPayload());
          lastMessageArrivedOnConsumerAfterPublish.set(messageArrived);
          arrivedCountAferPublish.incrementAndGet();
          log.info("[MQTT][after  ][retained: {}][duplicate: {}][qos: {}] {}",
              messageArrived.isRetained(), messageArrived.isDuplicate(), messageArrived.getQos(), payload);
        });
    mqttConsumerAfterPublish.init();
  }

  @AfterEach
  public void afterEach() throws MqttException {
    mqttConsumerCount.unsubsribe(topic);
    mqttConsumerCount.destroy();

    mqttConsumerBeforePublish.unsubsribe(topic);
    mqttConsumerBeforePublish.destroy();

    mqttConsumerAfterPublish.unsubsribe(topic);
    mqttConsumerAfterPublish.destroy();

  }

  @RepeatedTest(value = 10)
  @Order(1)
  @DisplayName("4.3.1 QoS 0: At most once delivery")
  public void testAtMostOnce(final RepetitionInfo repetitionInfo) throws MqttException {
    actAndAssert(repetitionInfo, 0);
  }

  @RepeatedTest(value = 10)
  @Order(2)
  @DisplayName("4.3.2 QoS 1: At least once delivery")
  public void testAtLeastOnce(final RepetitionInfo repetitionInfo) throws MqttException {
    actAndAssert(repetitionInfo, 1);
  }

  @RepeatedTest(value = 10)
  @Order(3)
  @DisplayName("4.3.3 QoS 2: Exactly once delivery")
  public void testExactlyOnce(final RepetitionInfo repetitionInfo) throws MqttException {
    actAndAssert(repetitionInfo, 2);
  }

  private void actAndAssert(final RepetitionInfo repetitionInfo, int qos) throws MqttException {
    // Act
    mqttConsumerBeforePublish.subscribe(topic, qos);
    publish(qos);
    logAftePublish(repetitionInfo, qos);
    mqttConsumerAfterPublish.subscribe(topic, qos);
    awaitUntilLastMessageArrivedOnConsumerAfterPublish();

    // Assert
    assertEquals(1, arrivedCountAferPublish.get());
    assertLastMessageOnConsumerBeforePublishArrivedEqualsLastMessagePublished();
    assertLastMessageOnConsumerAfterPublishArrivedEqualsLastMessagePublished();
  }

  protected void publish(final int qos) throws MqttException {
    mqttConsumerCount.subscribe(topic, qos);
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
  }

  private void awaitUntilLastMessageArrivedOnConsumerAfterPublish() {
    await()
        .pollDelay(FIVE_HUNDRED_MILLISECONDS)
        .atMost(FIVE_SECONDS)
        .until(() -> nonNull(lastMessageArrivedOnConsumerAfterPublish.get()));
  }

  private void assertLastMessageOnConsumerBeforePublishArrivedEqualsLastMessagePublished() {
    assertArrayEquals(lastMessagePublished.get().getPayload(), lastMessageArrivedOnConsumerBeforePublish.get().getPayload(),
        String.format(
            "\nMessage arrived on consumer subscribed before the publish is different from the last published message!\nPublished: %s\nArrived  : %s\n",
            new String(lastMessagePublished.get().getPayload()), new String(lastMessageArrivedOnConsumerAfterPublish.get().getPayload())));
  }

  private void assertLastMessageOnConsumerAfterPublishArrivedEqualsLastMessagePublished() {
    assertArrayEquals(lastMessagePublished.get().getPayload(), lastMessageArrivedOnConsumerAfterPublish.get().getPayload(),
        String.format(
            "\nMessage arrived on consumer subscribed after the publish  is different from the last published message!\nPublished: %s\nArrived  : %s\n",
            new String(lastMessagePublished.get().getPayload()), new String(lastMessageArrivedOnConsumerAfterPublish.get().getPayload())));
  }

  private void logAftePublish(final RepetitionInfo repetitionInfo,  int qos) {
    log.info("--- QoS: {} --- {}/{} ---", qos, repetitionInfo.getCurrentRepetition(), repetitionInfo.getTotalRepetitions());
    log.info("[MQTT][publish][retained: {}][duplicate: {}][qos: {}] {}",
        lastMessagePublished.get().isRetained(), lastMessagePublished.get().isDuplicate(), lastMessagePublished.get().getQos(), lastMessagePublished.get());
    log.info("[MQTT][before ][retained: {}][duplicate: {}][qos: {}] {}",
        lastMessageArrivedOnConsumerBeforePublish.get().isRetained(),
        lastMessageArrivedOnConsumerBeforePublish.get().isDuplicate(),
        lastMessageArrivedOnConsumerBeforePublish.get().getQos(),
        new String(lastMessageArrivedOnConsumerBeforePublish.get().getPayload()));
  }

}
