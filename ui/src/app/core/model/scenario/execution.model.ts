/**
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

import { ExecutionStatus } from '@core/model/scenario/execution-status';
import { Campaign, CampaignExecutionReport } from '@core/model';

export class Execution {

  public static NO_EXECUTION: Execution = new Execution(null, null, null, null, null, null, null);

  constructor(
    public duration: number,
    public status: ExecutionStatus,
    public report: string,
    public executionId: number,
    public time: Date,
    public environment: string,
    public user: string,
    public info?: string,
    public error?: string,
    public scenarioId?: string,
    public campaignReport?: CampaignExecutionReport,
  ) { }

  static deserializeExecutions(jsonObject: any): Execution[] {
    return jsonObject.map(execution => Execution.deserialize(execution));
  }

  static deserialize(jsonObject: any): Execution {
    return new Execution(
      jsonObject.duration,
      jsonObject.status,
      jsonObject.report,
      jsonObject.executionId,
      new Date(jsonObject.time),
      jsonObject.environment,
      jsonObject.user,
      jsonObject.info,
      jsonObject.error,
      jsonObject.scenarioId,
      jsonObject.campaignReport,
    );
  }
}
