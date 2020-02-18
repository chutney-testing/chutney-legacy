export class Task {

  constructor(
    public identifier: string,
    public inputs: Array<InputTask> = [],
    public target: boolean
  ) { }

}

export class InputTask {

    constructor(
      public name: string,
      public type: string,
    ) { }
}
