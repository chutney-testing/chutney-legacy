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

package com.chutneytesting.action.kafka.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

public class EmbeddedKafkaBrokerSerializer extends StdSerializer<EmbeddedKafkaBroker> {

    protected EmbeddedKafkaBrokerSerializer() {
        super(EmbeddedKafkaBroker.class);
    }

    @Override
    public void serialize(EmbeddedKafkaBroker value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("Embedded Kafka Broker", value.getBrokersAsString());
        gen.writeEndObject();
    }
}
