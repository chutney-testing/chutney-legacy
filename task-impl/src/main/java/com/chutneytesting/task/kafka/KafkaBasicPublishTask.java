package com.chutneytesting.task.kafka;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.exec.util.MapUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaBasicPublishTask implements Task {

    private final ChutneyKafkaProducerFactory producerFactory = new ChutneyKafkaProducerFactory();

    private final Target target;
    private final String topic;
    private final Map<String, String> headers;
    private final String payload;
    private final Map<String, String> properties;
    private final Logger logger;

    public KafkaBasicPublishTask(Target target,
                                 @Input("topic") String topic,
                                 @Input("headers") Map<String, String> headers,
                                 @Input("payload") String payload,
                                 @Input("properties") Map<String, String> properties,
                                 Logger logger) {
        this.target = target;
        this.topic = topic;
        this.headers = headers != null ? headers : emptyMap();
        this.payload = payload;
        this.properties = ofNullable(
            MapUtils.merge(extractProducerConfig(target), properties)
        ).orElse(new HashMap<>());
        this.logger = logger;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(topic, "topic"),
            notBlankStringValidation(payload, "payload"),
            targetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            List<Header> recordHeaders = headers.entrySet().stream()
                .map(it -> new RecordHeader(it.getKey(), it.getValue().getBytes()))
                .collect(Collectors.toList());

            logger.info("sending message to topic=" + topic);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topic, null, null, payload, recordHeaders);

            KafkaTemplate<String, String> kafkaTemplate = producerFactory.create(target, properties);
            kafkaTemplate.send(producerRecord).get(5, SECONDS);

            logger.info("Published Kafka Message on topic " + topic);
            return TaskExecutionResult.ok(toOutputs(headers, payload));
        } catch (Exception e) {
            logger.error("An exception occurs when sending a message to Kafka server: " + e.getMessage());
            return TaskExecutionResult.ko();
        } finally {
            try {
                producerFactory.destroy();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    private Map<String, Object> toOutputs(Map<String, String> headers, String payload) {
        Map<String, Object> results = new HashMap<>();
        results.put("payload", payload);
        results.put("headers", headers.entrySet().stream()
            .map(Map.Entry::toString)
            .collect(joining(";", "[", "]"))
        );
        return results;
    }

    private Map<String, String> extractProducerConfig(Target target) {
        if (target != null) {
            Map<String, String> config = new HashMap<>();
            ProducerConfig.configDef().configKeys().keySet().forEach(ck ->
                target.property(ck).ifPresent(cv -> config.put(ck, cv))
            );
            return config;
        }
        return emptyMap();
    }

}
