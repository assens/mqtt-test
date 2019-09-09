# MQTT Retained Message Test

### Reference Documentation
For further reference, please consider the following:

* [Apache ActiveMQ Artemis Documentation](https://activemq.apache.org/components/artemis/documentation/latest/mqtt.html)
* [MQTT Version 3.1.1](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html)

### Test case
The tests submit multiple retained messages with different QoS setting.

After that an MQTT clients subscribe and receive the retained message.

Assertions verify that the received retained message is equal to the last message published.

### Observation

The tests log the published message.
After that the tests log the last retained message received by a subscription which was made prior the publishing. (\[MQTT\]\[before \]]

When running with Artemis Broker (ArtemisTest) the test will also log the content of the queue used to keep the retained messages. The log shows that the queue holds multiple messages.

After that the tests log the last retained message received by two subscriptions made after the publishing has completed. (\[MQTT\]\[after  \], \[MQTT\]\[after2 \])

When running with Artemis Broker (ArtemisTest), you can observe that most times, the messages received by subscriptions made after the publishing has completed, are not equal to the last message published. In fact, the received retained message is the first one in the retained message queue.

### Artemis Broker MQTT Protocol implementation

The Artemis Broker MQTT Protocol is implemented in the artemis-mqtt-protocol maven module. 

The MQTTRetainMessageManager class is responsible for handling the retained messages. There's a comment in the code sayin:

```
   /**
    * FIXME
    * Retained messages should be handled in the core API.  There is currently no support for retained messages
    * at the time of writing.  Instead we handle retained messages here.  This method will create a new queue for
    * every address that is used to store retained messages.  THere should only ever be one message in the retained
    * message queue.  When a new subscription is created the queue should be browsed and the message copied onto
    * the subscription queue for the consumer.  When a new retained message is received the message will be sent to
    * the retained queue and the previous retain message consumed to remove it from the queue.
    */
```

The ArtemisTest demonstrates that

1. The retained message queue holds multiple messages, instead of one.
2. The retained message delivered to new subscriptions is not equal to the last message published on the given topic.






