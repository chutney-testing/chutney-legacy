/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.kafka;

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Failure;
import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Success;
import static java.util.Collections.emptyMap;
import static java.util.Collections.shuffle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.TestTarget;
import com.chutneytesting.action.http.HttpsServerStartActionTest;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("unchecked")
public class KafkaBasicPublishActionTest {

    private static final String TOPIC = "topic";
    private static final String PAYLOAD = "payload";
    private static final String GROUP = "mygroup";
    private static final String KEYSTORE_JKS = HttpsServerStartActionTest.class.getResource("/security/server.jks").getPath();
    private final EmbeddedKafkaBroker embeddedKafkaBroker = new EmbeddedKafkaKraftBroker(1, 1,  TOPIC);

    private TestLogger logger;

    @BeforeEach
    public void before() {
      logger = new TestLogger();
    }

    private Target getKafkaTarget() {
        return TestTarget.TestTargetBuilder.builder()
            .withTargetId("kafka")
            .withUrl("127.0.0.1:5555")
            .build();
    }

    @Test
    void should_set_inputs_default_values() {
        KafkaBasicPublishAction defaultAction = new KafkaBasicPublishAction(null, null, null, null, null, null);
        assertThat(defaultAction)
            .hasFieldOrPropertyWithValue("topic", null)
            .hasFieldOrPropertyWithValue("headers", emptyMap())
            .hasFieldOrPropertyWithValue("payload", null)
            .hasFieldOrPropertyWithValue("properties", emptyMap())
        ;
    }

    @Test
    void should_validate_all_mandatory_inputs() {
        KafkaBasicPublishAction defaultAction = new KafkaBasicPublishAction(null, null, null, null, null, null);
        List<String> errors = defaultAction.validateInputs();

        assertThat(errors.size()).isEqualTo(8);
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(errors.get(0)).isEqualTo("No topic provided (String)");
        softly.assertThat(errors.get(1)).isEqualTo("topic should not be blank");

        softly.assertThat(errors.get(2)).isEqualTo("No payload provided (String)");
        softly.assertThat(errors.get(3)).isEqualTo("payload should not be blank");

        softly.assertThat(errors.get(4)).isEqualTo("No target provided");
        softly.assertThat(errors.get(5)).isEqualTo("[Target name is blank] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(6)).isEqualTo("[Target url is not valid: null target] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(7)).isEqualTo("[Target url has an undefined host: null target] not applied because of exception java.lang.NullPointerException(null)");

        softly.assertAll();
    }

    @Test
    void should_merge_kafka_producer_target_properties_with_input_properties() {
        List<String> producerConfigKeys = new ArrayList<>(ProducerConfig.configNames());
        shuffle(producerConfigKeys);
        String targetProperty = producerConfigKeys.get(0);
        String propertyToOverride = producerConfigKeys.get(1);
        String inputProperty = producerConfigKeys.get(2);

        Target target = TestTarget.TestTargetBuilder.builder()
            .withProperty(targetProperty, "a value")
            .withProperty(propertyToOverride, "a target value")
            .build();

        Map<String, String> properties = Map.of(
            inputProperty, "a VALUE",
            propertyToOverride, "a property value"
        );

        Map<String, String> expectedConfig = Map.of(
            targetProperty, "a value",
            inputProperty, "a VALUE",
            propertyToOverride, "a property value"
        );

        KafkaBasicPublishAction defaultAction = new KafkaBasicPublishAction(target, null, null, null, properties, null);
        assertThat(defaultAction)
            .hasFieldOrPropertyWithValue("properties", expectedConfig)
        ;
    }

    @Test
    public void basic_publish_action_should_success() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Action action = new KafkaBasicPublishAction(getKafkaTarget(), TOPIC, null, PAYLOAD, null, logger);
        //mocks
        ChutneyKafkaProducerFactory producerFactoryMock = mock(ChutneyKafkaProducerFactory.class);
        KafkaTemplate<String, String> kafkaTemplateMock = mock(KafkaTemplate.class);
        when(producerFactoryMock.create(any(), any())).thenReturn(kafkaTemplateMock);

        CompletableFuture<SendResult<String, String>> listenableFutureMock = mock(CompletableFuture.class);
        when(listenableFutureMock.get(anyLong(), any(TimeUnit.class))).thenReturn(null);
        when(kafkaTemplateMock.send(ArgumentMatchers.<ProducerRecord<String, String>>any())).thenReturn(listenableFutureMock);

        ReflectionTestUtils.setField(action, "producerFactory", producerFactoryMock);

        //when
        ActionExecutionResult actionExecutionResult = action.execute();

        //Then
        assertThat(actionExecutionResult.status).isEqualTo(Success);
        assertThat(logger.errors).isEmpty();
        verify(listenableFutureMock).get(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void basic_publish_action_should_failed_when_timeout() throws Exception {
        //given
        TestLogger logger = new TestLogger();
        Action action = new KafkaBasicPublishAction(getKafkaTarget(), TOPIC, null, PAYLOAD, null, logger);
        //mocks
        ChutneyKafkaProducerFactory producerFactoryMock = mock(ChutneyKafkaProducerFactory.class);
        KafkaTemplate<String, String> kafkaTemplateMock = mock(KafkaTemplate.class);
        when(producerFactoryMock.create(any(), any())).thenReturn(kafkaTemplateMock);

        CompletableFuture<SendResult<String, String>> listenableFutureMock = mock(CompletableFuture.class);
        when(listenableFutureMock.get(anyLong(), any(TimeUnit.class))).thenThrow(TimeoutException.class);
        when(kafkaTemplateMock.send(ArgumentMatchers.<ProducerRecord<String, String>>any())).thenReturn(listenableFutureMock);

        ReflectionTestUtils.setField(action, "producerFactory", producerFactoryMock);

        //when
        ActionExecutionResult actionExecutionResult = action.execute();

        //Then
        assertThat(actionExecutionResult.status).isEqualTo(Failure);
        assertThat(logger.errors).isNotEmpty();
    }

    @Test
    public void should_produce_message_to_broker_without_truststore() {
      embeddedKafkaBroker.afterPropertiesSet();
      Consumer<Integer, String> consumer = configureConsumer();

      Target target = TestTarget.TestTargetBuilder.builder()
          .withTargetId("kafka")
          .withUrl("tcp://" + embeddedKafkaBroker.getBrokersAsString())
          .build();

      Map<String, String> props = new HashMap<>();
      props.put("group.id", GROUP);
      props.put("auto.commit.interval.ms", "10");
      props.put("session.timeout.ms", "60000");
      props.put("auto.offset.reset", "earliest");

      Action sut = new KafkaBasicPublishAction(target, TOPIC, Map.of(), "my-test-value", props, logger);

      ActionExecutionResult actionExecutionResult = sut.execute();

      assertThat(actionExecutionResult.status).isEqualTo(Success);

      ConsumerRecord<Integer, String> singleRecord = KafkaTestUtils.getSingleRecord(consumer, TOPIC);
      assertThat(singleRecord.value()).isEqualTo("my-test-value");

      consumer.close();
    }

    @Test
    public void producer_from_target_with_truststore_should_reject_ssl_connection_with_broker_without_truststore_configured() {
      embeddedKafkaBroker.afterPropertiesSet();
      Consumer<Integer, String> consumer = configureConsumer();

      Target target = TestTarget.TestTargetBuilder.builder()
          .withTargetId("kafka")
          .withUrl("tcp://" + embeddedKafkaBroker.getBrokersAsString())
          .withProperty("trustStore", KEYSTORE_JKS)
          .withProperty("trustStorePassword", "server")
          .withProperty("security.protocol", "SSL")
          .build();

      Map<String, String> props = new HashMap<>();
      props.put("group.id", GROUP);
      props.put("auto.commit.interval.ms", "10");
      props.put("session.timeout.ms", "3000");
      props.put("auto.offset.reset", "earliest");

      Action sut = new KafkaBasicPublishAction(target, TOPIC, Map.of(), "my-test-value", props, logger);

      ActionExecutionResult actionExecutionResult = sut.execute();

      assertThat(actionExecutionResult.status).isEqualTo(Failure);

      consumer.close();
    }

    private Consumer<Integer, String> configureConsumer() {
      Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
      consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      consumerProps.put("bootstrap.servers", embeddedKafkaBroker.getBrokersAsString());
      Consumer<Integer, String> consumer = new DefaultKafkaConsumerFactory<Integer, String>(consumerProps)
          .createConsumer();
      consumer.subscribe(Collections.singleton(TOPIC));
      return consumer;
    }
}
