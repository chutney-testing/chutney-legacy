import { ScenarioIndex } from '@core/model';
import { ScenarioChutneyNode } from './scenario-tree.component';

describe("Build tree from scenarios", function () {

  it("On scenario : a tree with only one level", function () {
    let scenarios:ScenarioIndex[] = [];
    let scenario1 = new ScenarioIndex();
    scenario1.title = "title1";
    scenario1.path = "FIRST";
    scenarios.push(scenario1);

    let root = ScenarioChutneyNode.buildTree(scenarios);

    expect(root.children.length).toBe(1);
    expect(root.name).toBe("root");

    let firstChild = root.children[0];
    expect(firstChild.children.length).toBe(0);
    expect(firstChild.name).toBe("FIRST");

  });

  it("One scenario: a tree with three level", function () {
    let scenarios:ScenarioIndex[] = [];
    let scenario1 = new ScenarioIndex();
    scenario1.title = "title1";
    scenario1.path = "FIRST/TEST/SERVICE";
    scenarios.push(scenario1);

    let root = ScenarioChutneyNode.buildTree(scenarios);

    expect(root.children.length).toBe(1);
    expect(root.name).toBe("root");

    let firstChild = root.children[0];
    expect(firstChild.children.length).toBe(1);
    expect(firstChild.name).toBe("FIRST");

    let testChild = firstChild.children[0];
    expect(testChild.children.length).toBe(1);
    expect(testChild.name).toBe("TEST");

    let serviceChild = testChild.children[0];
    expect(serviceChild.children.length).toBe(0);
    expect(serviceChild.name).toBe("SERVICE");
  });

  it("Multpiple scenarios : a tree with three level", function () {
    let scenarios:ScenarioIndex[] = [];
    let scenario1 = new ScenarioIndex();
    scenario1.title = "title3";
    scenario1.path = "FIRST/TEST/SERVICE";
    scenarios.push(scenario1);
    let scenario2 = new ScenarioIndex();
    scenario2.title = "title2";
    scenario2.path = "SECOND";
    scenarios.push(scenario2);
    let scenario3 = new ScenarioIndex();
    scenario3.title = "title3";
    scenario3.path = "FIRST/TEST";
    scenarios.push(scenario3);   

    let root = ScenarioChutneyNode.buildTree(scenarios);

    expect(root.children.length).toBe(2);
    expect(root.name).toBe("root");

    let firstChild = root.children[0];
    expect(firstChild.children.length).toBe(1);
    expect(firstChild.name).toBe("FIRST");

    let testChild = firstChild.children[0];
    expect(testChild.children.length).toBe(1);
    expect(testChild.name).toBe("TEST");

    let serviceChild = testChild.children[0];
    expect(serviceChild.children.length).toBe(0);
    expect(serviceChild.name).toBe("SERVICE");
  });
});
