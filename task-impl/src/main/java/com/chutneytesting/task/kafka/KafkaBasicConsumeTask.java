package com.chutneytesting.task.kafka;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML;

import com.chutneytesting.task.amqp.utils.JsonPathEvaluator;
import com.chutneytesting.task.function.XPathFunction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

public class KafkaBasicConsumeTask implements Task {

    static final String OUTPUT_BODY = "body";
    static final String OUTPUT_BODY_HEADERS_KEY = "headers";
    static final String OUTPUT_BODY_PAYLOAD_KEY = "payload";
    static final String OUTPUT_HEADERS = "headers";
    static final String OUTPUT_PAYLOADS = "payloads";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String topic;
    private final Logger logger;
    private final Integer nbMessages;
    private MimeType contentType;
    private final String timeout;
    private final String selector;
    private final String headerSelector;
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
                                 @Input("header-selector") String headerSelector,
                                 @Input("content-type") String contentType,
                                 @Input("timeout") String timeout,
                                 Logger logger) {
        this.topic = topic;
        this.nbMessages = defaultIfNull(nbMessages, 1);
        this.selector = selector;
        this.headerSelector = headerSelector;
        this.contentType = ofNullable(contentType).map(ct -> defaultIfEmpty(ct, APPLICATION_JSON_VALUE)).map(MimeTypeUtils::parseMimeType).orElse(APPLICATION_JSON);
        this.timeout = defaultIfEmpty(timeout, "60 sec");
        this.consumerFactory = new KafkaConsumerFactoryFactory().create(target, group, defaultIfNull(properties, emptyMap()));
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
                logger.error("Unable to get the expected number of messages [" + nbMessages + "] during " + timeout + " from topic " + topic + ".");
                return TaskExecutionResult.ko();
            }
            logger.info("Consumed [" + nbMessages + "] Kafka Messages from topic " + topic);
            return TaskExecutionResult.ok(toOutputs());
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
            if (applySelector(message) && applyHeaderSelector(message)) {
                addMessageToResultAndCountDown(message);
            }
        };
    }

    private boolean applySelector(Map<String, Object> message) {
        if (isBlank(selector)) {
            return true;
        }

        if (contentType.getSubtype().contains(APPLICATION_JSON.getSubtype())) {
            try {
                String messageAsString = OBJECT_MAPPER.writeValueAsString(message);
                return JsonPathEvaluator.evaluate(messageAsString, selector);
            } catch (Exception e) {
                logger.info("Received a message, however cannot read process it as json, ignoring payload selection : " + e.getMessage());
                return true;
            }
        } else if (contentType.getSubtype().contains(APPLICATION_XML.getSubtype())) {
            try {
                Object result = XPathFunction.xpath((String) message.get(OUTPUT_BODY_PAYLOAD_KEY), selector);
                return ofNullable(result).isPresent();
            } catch (Exception e) {
                logger.info("Received a message, however cannot read process it as xml, ignoring payload selection : " + e.getMessage());
                return true;
            }
        } else {
            logger.info("Applying selector as text");
            return ((String) message.get(OUTPUT_BODY_PAYLOAD_KEY)).contains(selector);
        }
    }

    private boolean applyHeaderSelector(Map<String, Object> message) {
        if (isBlank(headerSelector)) {
            return true;
        }

        try {
            String messageAsString = OBJECT_MAPPER.writeValueAsString(message.get(OUTPUT_BODY_HEADERS_KEY));
            return JsonPathEvaluator.evaluate(messageAsString, headerSelector);
        } catch (Exception e) {
            logger.error("\"Received a message, however cannot process headers selection, Ignoring header selection");
            return true;
        }
    }

    private void addMessageToResultAndCountDown(Map<String, Object> message) {
        consumedMessages.add(message);
        countDownLatch.countDown();
    }

    private Object extractPayload(ConsumerRecord<String, String> record) {
        if (contentType.getSubtype().contains(APPLICATION_JSON.getSubtype())) {
            try {
                return OBJECT_MAPPER.readValue(record.value(), Map.class);
            } catch (IOException e) {
                logger.info("Received a message, however cannot read it as Json fallback as String.");
            }
        }
        return record.value();
    }

    private Map<String, Object> extractMessageFromRecord(ConsumerRecord<String, String> record) {
        final Map<String, Object> message = new HashMap<>();
        final Map<String, Object> headers = extractHeaders(record);
        checkContentTypeHeader(headers);
        Object payload = extractPayload(record);
        message.put(OUTPUT_BODY_HEADERS_KEY, headers);
        message.put(OUTPUT_BODY_PAYLOAD_KEY, payload);
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

    private Map<String, Object> toOutputs() {
        Map<String, Object> results = new HashMap<>();
        results.put(OUTPUT_BODY, consumedMessages);
        results.put(OUTPUT_PAYLOADS, consumedMessages.stream().map(e -> e.get(OUTPUT_BODY_PAYLOAD_KEY)).collect(toList()));
        results.put(OUTPUT_HEADERS, consumedMessages.stream().map(e -> e.get(OUTPUT_BODY_HEADERS_KEY)).collect(toList()));
        return results;
    }

    private void checkContentTypeHeader(Map<String, Object> headers) {
        Optional<MimeType> contentType = headers.entrySet().stream()
            .filter(e -> e.getKey().replaceAll("[- ]", "").equalsIgnoreCase("contenttype"))
            .findAny()
            .map(Map.Entry::getValue)
            .map(Object::toString)
            .map(MimeTypeUtils::parseMimeType);

        contentType.ifPresent(ct -> {
            logger.info("Found content type header " + ct);
            this.contentType = ct;
        });
    }
}
