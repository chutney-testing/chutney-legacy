import { StepModule } from './step.module';

describe('StepModule', () => {
  let stepModule: StepModule;

  beforeEach(() => {
    stepModule = new StepModule();
  });

  it('should create an instance', () => {
    expect(stepModule).toBeTruthy();
  });
});
