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
        switch (status) {
            case ExecutionStatus.SUCCESS:
               return 'OK';
            case ExecutionStatus.FAILURE:
               return 'KO';
            case ExecutionStatus.RUNNING:
               return 'RUNNING';
            case ExecutionStatus.PAUSED:
               return 'PAUSE';
            case ExecutionStatus.STOPPED:
               return 'STOP';
            case ExecutionStatus.NOT_EXECUTED:
               return 'NOT EXECUTED';
        }
    }
}
