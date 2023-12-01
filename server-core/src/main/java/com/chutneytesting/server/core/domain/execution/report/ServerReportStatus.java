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

package com.chutneytesting.server.core.domain.execution.report;

import com.google.common.collect.Ordering;
import java.util.Objects;
import java.util.stream.StreamSupport;

public enum ServerReportStatus {
    SUCCESS, WARN, FAILURE, NOT_EXECUTED, STOPPED, PAUSED, RUNNING, EXECUTED;

    private static final Ordering<ServerReportStatus> EXECUTION_STATUS_STATUS_ORDERING = Ordering.explicit(PAUSED, RUNNING, STOPPED, FAILURE, WARN, NOT_EXECUTED, SUCCESS);

    public static ServerReportStatus worst(Iterable<ServerReportStatus> severalStatus) {
        return StreamSupport
            .stream(severalStatus.spliterator(), false)
            .filter(Objects::nonNull)
            .reduce(SUCCESS, EXECUTION_STATUS_STATUS_ORDERING::min);
    }

    public boolean isFinal() {
        return this.equals(FAILURE) || this.equals(SUCCESS) || this.equals(STOPPED);
    }

    public interface HavingStatus {
        ServerReportStatus getStatus();
    }
}
