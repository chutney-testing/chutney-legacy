import { Component, Input } from '@angular/core';
import { ScenarioIndex } from '@core/model';


@Component({
  selector: 'chutney-tree',
  templateUrl: './scenario-tree.component.html',
  styleUrls: ['./scenario-tree.component.scss']
})
export class ScenarioTreeComponent {

  @Input() node: ScenarioChutneyNode;

  tree: ScenarioChutneyNode;
  constructor() { }

}

export class ScenarioChutneyNode {
  name: String;
  children: Array<ScenarioChutneyNode> = [];
  scenarios: Array<ScenarioIndex> = [];

  constructor(name: String) {
      this.name = name;
  }

  addChild(node: ScenarioChutneyNode) {
      this.children.push(node);
      return node
  }

  child(path: string): ScenarioChutneyNode {
      for (let i = 0; i < this.children.length; i++) {
          let child = this.children[i];
          if (child.name == path) {
              return child;
          }
      }
      return this.addChild(new ScenarioChutneyNode(path));
  }

  static buildTree(scenarios: ScenarioIndex[]): ScenarioChutneyNode {
      var tree = new ScenarioChutneyNode("root");
      var current = tree;

      scenarios.forEach(
          s => {
              var root = current;
              var paths = s.path.split("/");
              for (let i = 0; i < paths.length; i++) {
                  let p = paths[i];
                  current = current.child(p)
              }
              current.scenarios.push(s);
              current = root;
          }
      );
      return tree
  }

}