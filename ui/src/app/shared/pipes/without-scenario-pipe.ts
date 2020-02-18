import { Pipe, PipeTransform } from '@angular/core';
import { ScenarioIndex } from '@model';

@Pipe({
    name: 'withoutScenario'
})
export class WithoutScenarioPipe implements PipeTransform {

    transform(input: Array<ScenarioIndex>, scenarioToExclude: Array<ScenarioIndex>) {
        return input.filter((item) => {
            return !scenarioToExclude === undefined || !scenarioToExclude.includes(item);
        });
    }

}
