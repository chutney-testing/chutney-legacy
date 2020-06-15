export class Execution {

  public static NO_EXECUTION: Execution = new Execution(null, null, null, null, null, null, null);

  constructor(
    public duration: number,
    public status: string,
    public report: string,
    public executionId: number,
    public time: Date,
    public environment: string,
    public user: string,
    public info?: string,
    public error?: string,
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
      jsonObject.error
    );
  }
}
