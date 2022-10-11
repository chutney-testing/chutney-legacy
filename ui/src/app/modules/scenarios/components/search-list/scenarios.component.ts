import { Component, OnInit } from '@angular/core';
import { ScenarioIndex } from '@core/model';
import { ScenarioChutneyNode } from '@shared/components';

@Component({
    selector: 'chutney-scenarios',
    templateUrl: './scenarios.component.html',
    styleUrls: ['./scenarios.component.scss']
})
export class ScenariosComponent implements OnInit {

    root: ScenarioChutneyNode = new ScenarioChutneyNode("root");
    scenarios: ScenarioIndex[] = [];

    constructor() {
    }

    ngOnInit(): void {
        let scenario1 = new ScenarioIndex();
        scenario1.title = "title1";
        scenario1.path = "FIRST/TEST/SERVICE";
        this.scenarios.push(scenario1);
        let scenario2 = new ScenarioIndex();
        scenario2.title = "title2";
        scenario2.path = "SECOND/ALLO";
        this.scenarios.push(scenario2);
        let scenario3 = new ScenarioIndex();
        scenario3.title = "title3";
        scenario3.path = "FIRST/TEST/SERVICE";
        this.scenarios.push(scenario3); 
        
        this.root = ScenarioChutneyNode.buildTree(this.scenarios);
    }

}




