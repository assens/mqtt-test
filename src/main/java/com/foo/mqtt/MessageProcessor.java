package com.foo.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

@FunctionalInterface
public interface MessageProcessor {

  void process(MqttMessage message);

}
