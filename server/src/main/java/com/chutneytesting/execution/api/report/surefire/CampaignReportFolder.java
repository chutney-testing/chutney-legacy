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

package com.chutneytesting.execution.api.report.surefire;

import java.util.Set;

/**
 * Represents data needed to create campaign-named folder in surefire reports ZIP.
 */
class CampaignReportFolder {
    final String name;
    final Set<Testsuite> scenariosReport;

    CampaignReportFolder(String name, Set<Testsuite> scenariosReport) {
        this.name = name;
        this.scenariosReport = scenariosReport;
    }
}
