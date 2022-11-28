export enum ExecutionStatus {
    SUCCESS = 'SUCCESS',
    FAILURE = 'FAILURE',
    STOPPED = 'STOPPED',
    RUNNING = 'RUNNING',
    PAUSED = 'PAUSED',
    NOT_EXECUTED = 'NOT_EXECUTED'
}

export namespace ExecutionStatus {

    export function toString(status: ExecutionStatus): string {
        return status && `global.status.${status.toLowerCase()}`
    }
}
