import { Equals, areEquals } from '@shared/equals';
import { cloneAsPossible, Clonable } from '@shared/clonable';

export class ExampleParameter implements Equals<ExampleParameter>, Clonable<ExampleParameter> {
  constructor(
    public name: string,
    public value: any
  ) { }

  public equals(obj: ExampleParameter): boolean {
    return obj && areEquals(this.name, obj.name) && areEquals(this.value, obj.value);
  }

  public clone(): ExampleParameter {
    return new ExampleParameter(
      cloneAsPossible(this.name),
      cloneAsPossible(this.value)
    );
  }
}

export class ExampleParameters implements Equals<ExampleParameters>, Clonable<ExampleParameters> {

  constructor(
    public params: ExampleParameter[] = []
  ) { }

  static deserialize(jsonObject: any): ExampleParameters {
    if (jsonObject === undefined) {
      return new ExampleParameters();
    }

    const exampleParameters = new ExampleParameters();

    Object.keys(jsonObject).forEach(k => {
      exampleParameters.params.push(new ExampleParameter(k, jsonObject[k]));
    });

    exampleParameters.orderParams();
    return exampleParameters;
  }

  serialize(): any {
    const paramJsonObject = {};
    this.params.forEach(param => {
      paramJsonObject[param.name] = (param.value != null ? param.value : '');
    });
    return paramJsonObject;
  }

  private orderParams() {
    this.params.sort((a, b) => a.name.localeCompare(b.name));
  }

  public equals(obj: ExampleParameters): boolean {
    return obj && areEquals(this.params, obj.params);
  }

  public clone(): ExampleParameters {
    return new ExampleParameters(cloneAsPossible(this.params));
  }
}
