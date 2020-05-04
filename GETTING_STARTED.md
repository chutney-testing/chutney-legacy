## Getting Started

Everything you need to run the app and start coding.

## Summary

* [How to run](#use)
* [How to code](#code)


## <a name="use"></a> How to run

### Run in local-dev mode
To launch Chutney in _local-dev_ mode, use
* the classpath of [packaging/local-dev](packaging/local-dev) module
* `com.chutneytesting.ServerBootstrap` as main class


## <a name="code"></a> How to code

### Prerequisites

* [Maven](https://maven.apache.org/) - version 3.3 or higher - Java dependency management
* [Node](https://nodejs.org/en/) - version 12.16.2 or higher - JavaScript runtime
* [Npm] (https://www.npmjs.com/) - version 6.14.4 or higher - JavaScript package manager


If you use **direnv** and **nix** packages manager, we provide 2 files for the ui module : [.env.nix](.env.nix) and [.envrc](.envrc).

Upon running **direnv allow** inside ui module folder, it will install node, npm, and some usefull symlinks you can use for configuring IDE or other tools.

You can use a Javascript launcher such as [Volta](https://volta.sh/) to take care of **Node** and **Npm** by using the additional command line property: `-DuseExternalNpm=true`

### Installing

git clone  
`mvn install` or `mvn install -DuseExternalNpm=true`  
Main class : com.chutneytesting.ServerBootstrap

### Modules

* cli: First draft of a cli
* engine: Execution engine which sole responsibility is to execute scenarios and provide a report for each execution
* packaging: default packaging used to start Chutney
* server: Central module that
    * Back-end for front-end
    * Store scenarios (json), execution report and campaigns in jdbc database
    * Store scenarios in Orient database for composed scenarios
    * Store in files target and environment information
    * Send scenario to the execution engine and retrieve reports
* task-impl: Default implementation of task (Sql, Http, Jms,...)
* task-spi: Contains interfaces to extend the engine 
* tools: Utility class with no dependency 
* ui : front-end of Chutney

### Running the tests

mvn test to launch to type of test : 
	- unit test
	- cucumber test. Features are in [server/src/test/resources/blackbox](server/src/test/resources/blackbox)
	- [run configuration for Intellij IDEA](https://github.com/chutney-testing/chutney/tree/master/.idea/runConfigurations)


