package com.chutneytesting.task.amqp.consumer;

import static com.chutneytesting.task.amqp.utils.AmqpUtils.convertMapLongStringToString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import com.chutneytesting.task.amqp.utils.JsonPathEvaluator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueingConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueingConsumer.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final long maxAwait;
    private final Channel channel;
    private final String queueName;
    private final String selector;
    private final CountDownLatch messageCounter;
    private final Result result;

    public QueueingConsumer(Channel channel, String queueName, int nbMessages, String selector, long maxAwait) {
        this.selector = selector;
        this.maxAwait = maxAwait;
        this.channel = channel;
        this.queueName = queueName;
        this.result = new Result();
        this.messageCounter = new CountDownLatch(nbMessages);
    }

    public Result consume(boolean autoAck) throws IOException, InterruptedException {
        channel.basicConsume(queueName, autoAck, this::deliveryCallback, this::cancelCallback);
        messageCounter.await(maxAwait, TimeUnit.MILLISECONDS);
        return result;
    }

    private void deliveryCallback(String consumerTag, Delivery delivery) {
        handleDelivery(delivery);
    }

    private void handleDelivery(Delivery delivery) {
        if (messageCounter.getCount() <= 0) {
            return;
        }
        final Map<String, Object> headerz = convertMapLongStringToString(delivery.getProperties().getHeaders());
        final Map<String, Object> message = new HashMap<>();
        Object payload = extractPayload(delivery);
        message.put("headers", headerz);
        message.put("payload", payload);
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

    }

    private void addMessageToResultAndCountDown(Map<String, Object> message) {
        result.handleMessage(message);
        messageCounter.countDown();
    }

    private Object extractPayload(Delivery delivery) {
        Object payload;
        try {
            payload = OBJECT_MAPPER.readValue(new String(delivery.getBody()), Map.class);
        } catch (IOException e) {
            LOGGER.warn("Received a message, however cannot read it as Json fallback as String.", e);
            payload = new String(delivery.getBody());
        }
        return payload;
    }

    private void cancelCallback(String consumerTag) {
        // do nothing
    }

    public static class Result {
        public final List<Map<String, Object>> messages = new ArrayList<>();
        public final List<Object> payloads = new ArrayList<>();
        public final List<Map<String, Object>> headers = new ArrayList<>();

        private void handleMessage(Map<String, Object> message) {
            messages.add(message);
            headers.add((Map<String, Object>) message.get("headers"));
            payloads.add(message.get("payload"));
        }
    }
}
