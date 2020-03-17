package com.chutneytesting.task.kafka;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import com.chutneytesting.task.amqp.utils.JsonPathEvaluator;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.time.Duration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;

public class KafkaBasicConsumeTask implements Task {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KafkaBasicConsumeTask.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String topic;
    private final String group;
    private final Logger logger;
    private final Integer nbMessages;
    private final String timeout;
    private final String selector;
    // we probably want to add something more, like 'auto.offset.reset', see: https://kafka.apache.org/documentation/#consumerconfigs
    private final Map<String, String> properties;
    private final ConsumerFactory<String, String> consumerFactory;
    private final CountDownLatch countDownLatch;
    private final List<Map<String, Object>> consumedMessages = new ArrayList<>();
    private final ConcurrentMessageListenerContainer<String, String> messageListenerContainer;


    public KafkaBasicConsumeTask(Target target,
                                 @Input("topic") String topic,
                                 @Input("group") String group,
                                 @Input("properties") Map<String, String> properties,
                                 @Input("nb-messages") Integer nbMessages,
                                 @Input("selector") String selector,
                                 @Input("timeout") String timeout,
                                 Logger logger) {
        this.topic = topic;
        this.group = group;
        this.properties = defaultIfNull(properties, Collections.emptyMap());
        this.nbMessages = defaultIfNull(nbMessages, 1);
        this.selector = selector;
        this.timeout = defaultIfEmpty(timeout, "60 sec");
        this.consumerFactory = new KafkaConsumerFactoryFactory().create(target, this.group, this.properties);
        this.countDownLatch = new CountDownLatch(this.nbMessages);
        this.messageListenerContainer = createMessageListenerContainer(createMessageListener());
        this.logger = logger;
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            logger.info("Consuming message from topic " + topic);
            messageListenerContainer.start();
            countDownLatch.await(Duration.parse(timeout).toMilliseconds(), TimeUnit.MILLISECONDS);
            if (consumedMessages.size() != nbMessages) {
                logger.error("Unable to get the expected number of messages [" + nbMessages + "] during " + timeout + "from topic " + topic + ".");
                return TaskExecutionResult.ko();
            }
            logger.info("Consumed [" + nbMessages + "] Kafka Messages from topic " + topic);
            Map<String, Object> results = new HashMap<>();
            results.put("body", consumedMessages);
            results.put("payloads", consumedMessages.stream().map(e -> e.get("payload")).collect(toList()));
            results.put("headers", consumedMessages.stream().map(e -> e.get("headers")).collect(toList()));
            return TaskExecutionResult.ok(results);
        } catch (Exception e) {
            logger.error("An exception occurs when consuming a message to Kafka server: " + e.getMessage());
            return TaskExecutionResult.ko();
        } finally {
            messageListenerContainer.stop();
        }
    }

    private MessageListener<String, String> createMessageListener() {
        return record -> {
            if (countDownLatch.getCount() <= 0) {
                return;
            }
            final Map<String, Object> message = extractMessageFromRecord(record);
            if (StringUtils.isBlank(selector)) {
                addMessageToResultAndCountDown(message);
            } else {
                try {
                    String messageAsString = OBJECT_MAPPER.writeValueAsString(message);
                    if (JsonPathEvaluator.evaluate(messageAsString, selector)) {
                        addMessageToResultAndCountDown(message);
                    }
                } catch (JsonProcessingException e) {
                    LOGGER.warn("Received a message, however cannot read process it as json, Ignoring message selection.", e);
                }
            }
        };
    }

    private void addMessageToResultAndCountDown(Map<String, Object> message) {
        consumedMessages.add(message);
        countDownLatch.countDown();
    }

    private Object extractPayload(ConsumerRecord<String, String> record) {
        Object payload;
        try {
            payload = OBJECT_MAPPER.readValue(record.value(), Map.class);
        } catch (IOException e) {
            LOGGER.warn("Received a message, however cannot read it as Json fallback as String.", e);
            payload = record.value();
        }
        return payload;
    }


    private Map<String, Object> extractMessageFromRecord(ConsumerRecord<String, String> record) {
        final Map<String, Object> message = new HashMap<>();
        final Map<String, Object> headerz = extractHeaders(record);
        Object payload = extractPayload(record);
        message.put("headers", headerz);
        message.put("payload", payload);
        return message;
    }

    private Map<String, Object> extractHeaders(ConsumerRecord<String, String> record) {
        return Stream.of(record.headers().toArray()).collect(toMap(Header::key, header -> new String(header.value(), UTF_8)));
    }

    private ConcurrentMessageListenerContainer<String, String> createMessageListenerContainer(MessageListener<String, String> messageListener) {
        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setMessageListener(messageListener);
        return new ConcurrentMessageListenerContainer<>(
            consumerFactory,
            containerProperties);
    }

}
