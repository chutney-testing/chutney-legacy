package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestFinallyActionRegistry;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.qpid.server.SystemLauncher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

public class MultiHostsAmqpTaskTest {

    @Test
    void should_consume_produce_on_multi_hosts_target_url(@TempDir Path tmpDir) throws Exception {
        SystemLauncher server_7654 = null;

        try {
            String queue = "test";

            server_7654 = startAMQPServer(7654, tmpDir);

            Target multiTarget = buildAMQPServerTarget("amqp://localhost:5672,localhost:7654");
            Target target_7654 = buildAMQPServerTarget("amqp://localhost:7654");

            createTempQueue(target_7654, queue);
            publishBlankMessage(target_7654, queue);

            consumeMessage(multiTarget, queue);
            publishBlankMessage(multiTarget, queue);
        } finally {
            ofNullable(server_7654).ifPresent(SystemLauncher::shutdown);
        }
    }

    @Test
    void should_consume_produce_on_multi_hosts_target_addresses_property(@TempDir Path tmpDir) throws Exception {
        SystemLauncher server_7654 = null;

        try {
            String queue = "test";

            server_7654 = startAMQPServer(7654, tmpDir);

            Target multiTarget = buildAMQPServerTarget("amqp://localhost:666", "addresses", "localhost:5672,localhost:7654");
            Target target_7654 = buildAMQPServerTarget("amqp://localhost:7654");

            createTempQueue(target_7654, queue);
            publishBlankMessage(target_7654, queue);

            consumeMessage(multiTarget, queue);
            publishBlankMessage(multiTarget, queue);
        } finally {
            ofNullable(server_7654).ifPresent(SystemLauncher::shutdown);
        }
    }

    private SystemLauncher startAMQPServer(int port, Path initConfigTmpDir) throws IOException {
        String memory_config = Files.readString(
            new ClassPathResource("com/chutneytesting/task/amqp/default_qpid.json").getFile().toPath()
        );
        String cfgWithPort = memory_config.replace("${qpid.amqp_port}", String.valueOf(port));
        Path tmpCfgPath = Files.writeString(initConfigTmpDir.resolve("cfg_" + port + ".json"), cfgWithPort);

        QpidServerStartTask sut = new QpidServerStartTask(new TestLogger(), new TestFinallyActionRegistry(), tmpCfgPath.toAbsolutePath().toString());
        TaskExecutionResult firstResult = sut.execute();
        return (SystemLauncher) firstResult.outputs.get("qpidLauncher");
    }

    private Target buildAMQPServerTarget(String url, String... properties) {
        TestTarget.TestTargetBuilder targetBuilder = TestTarget.TestTargetBuilder.builder()
            .withUrl(url)
            .withProperty("user", "guest")
            .withProperty("password", "guest");

        for (int i = 0; i < properties.length; i = i + 2) {
            targetBuilder.withProperty(properties[i], properties[i + 1]);
        }

        return targetBuilder.build();
    }

    private void createTempQueue(Target target, String queue) {
        TaskExecutionResult createTmpQueue = new AmqpCreateBoundTemporaryQueueTask(
            target,
            "amq.direct",
            "routemeplease",
            queue,
            new TestLogger(),
            new TestFinallyActionRegistry()
        ).execute();
        assertThat(createTmpQueue.status).isEqualTo(Success);
    }

    private void publishBlankMessage(Target target, String queue) {
        TaskExecutionResult createTmpQueue = new AmqpCreateBoundTemporaryQueueTask(
            target,
            "amq.direct",
            "routemeplease",
            queue,
            new TestLogger(),
            new TestFinallyActionRegistry()
        ).execute();
        assertThat(createTmpQueue.status).isEqualTo(Success);

        TaskExecutionResult publish = new AmqpBasicPublishTask(
            target,
            "amq.direct",
            "routemeplease",
            null,
            singletonMap("content_type", "application/json"),
            "{}",
            new TestLogger()
        ).execute();
        assertThat(publish.status).isEqualTo(Success);
    }

    private void consumeMessage(Target target, String queue) {
        TaskExecutionResult consume = new AmqpBasicConsumeTask(
            target,
            queue,
            1,
            null,
            "5 s",
            true,
            new TestLogger()
        ).execute();
        assertThat(consume.status).isEqualTo(Success);
    }
}
