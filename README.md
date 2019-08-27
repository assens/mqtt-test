# MQTT Retained Message Test

### Reference Documentation
For further reference, please consider the following:

* [Apache ActiveMQ Artemis Documentation](https://activemq.apache.org/components/artemis/documentation/latest/mqtt.html)
* [MQTT Version 3.1.1](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html)

### Test case
The tests submit multiple retained messages with different QoS setting.

After that an MQTT client subscribes and receives the retained message.

Assertions verify that the received retained message is equal to the last message published.

Messages are published by a client with id: publish.

Messages are received by a client with id: arrive.

### Conclusion



