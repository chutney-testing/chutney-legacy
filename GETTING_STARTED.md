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
* [Node](https://nodejs.org/en/) - version 7.10 or higher - JavaScript runtime
* [Npm] (https://www.npmjs.com/) - version 4.2 or higher - JavaScript package manager


If you use **direnv** and **nix** packages manager, we provide 2 files for the ui module : [.env.nix](.env.nix) and [.envrc](.envrc).

Upon running **direnv allow** inside ui module folder, it will install node, npm, and some usefull symlinks you can use for configuring IDE or other tools.

You can use a Javascript launcher such as [Volta](https://volta.sh/) to take care of **Node** and **Npm** by using the additional command line property: `-DuseExternalNpm=true`

### Installing

git clone  
`mvn install` or `mvn install -DuseExternalNpm=true`  
Main class : com.chutneytesting.ServerBootstrap

### Modules

// TODO

### Running the tests

mvn test to launch to type of test : 
	- unit test
	- cucumber test. Features are in [server/src/test/resources/blackbox](server/src/test/resources/blackbox)

### Deployment
// TODO

#### Local deployment
// TODO
