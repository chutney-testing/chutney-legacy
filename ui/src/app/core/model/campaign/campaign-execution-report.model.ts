import { ScenarioExecutionReportOutline } from '.';
import { ExecutionStatus } from '../scenario/execution-status';

export interface CampaignExecutionReport {
    executionId?: number,
    scenarioExecutionReports: Array<ScenarioExecutionReportOutline>,
    status?: string,
    duration?: string,
    startDate?: string,
    campaignName?: string,
    partialExecution?: boolean,
    executionEnvironment?: string,
    campaignId?: string,
    user: string
}

export class CampaignReport {
    readonly report: CampaignExecutionReport;

    readonly passed: number;
    readonly running: number;
    readonly failed: number;
    readonly stopped: number;
    readonly notexecuted: number;
    readonly pause: number;
    readonly total: number;

    constructor(report: CampaignExecutionReport) {
        this.report = report;

        const counts = this.initCounts(report);
        this.notexecuted = counts[0];
        this.running = counts[1];
        this.passed = counts[2];
        this.failed = counts[3];
        this.stopped = counts[4];
        this.pause = counts[5];
        this.total = this.passed + this.failed + this.stopped + this.notexecuted + this.running + this.pause;
    }

    private initCounts(report: CampaignExecutionReport): Array<number> {
        var runnings = 0;
        var success = 0;
        var failures = 0;
        var stops = 0;
        var notExecuted = 0;
        var pauses = 0;
        report.scenarioExecutionReports.forEach(r => {
            if (ExecutionStatus.NOT_EXECUTED === r.status) {
                notExecuted++;
                return;
            }
            if (ExecutionStatus.RUNNING === r.status) {
                runnings++;
                return;
            }
            if (ExecutionStatus.SUCCESS === r.status) {
                success++;
                return;
            }
            if (ExecutionStatus.FAILURE === r.status) {
                failures++;
                return;
            }
            if (ExecutionStatus.STOPPED === r.status) {
                stops++;
                return;
            }
            if (ExecutionStatus.STOPPED === r.status) {
                pauses++;
                return;
            }
        });
        return [notExecuted, runnings, success, failures, stops, pauses];
    }

    allPassed() {
        return this.passed == this.report.scenarioExecutionReports.length;
    }

    hasPassed() {
        return this.passed > 0;
    }

    hasFailed() {
        return this.failed > 0;
    }

    hasStopped() {
        return this.stopped > 0;
    }

    hasNotExecuted() {
        return this.notexecuted > 0;
    }

    hasPaused() {
        return this.pause > 0;
    }

    hasRunning() {
        return this.running > 0;
    }

    isRunning() {
        return ExecutionStatus.RUNNING === this.report.status;
    }

    isPaused() {
        return ExecutionStatus.PAUSED === this.report.status;
    }

    isStopped() {
        return ExecutionStatus.STOPPED === this.report.status;
    }
}
