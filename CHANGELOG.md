# Changelog

## [2.4.0](https://github.com/chutney-testing/chutney/tree/2.4.0)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/2.3.0...2.4.0)

## What's Changed
### 🚀 Implemented enhancements:
* feat(ui): add metrics page by @rbenyoussef in [#1173](https://github.com/chutney-testing/chutney/pull/1173)
* bugfix(engine): Manage parent step on ifStrategy by @nbrouand in [#1175](https://github.com/chutney-testing/chutney/pull/1175)
* feat(action-impl): soft assert for jsonAssertAction by @rbenyoussef in [#1176](https://github.com/chutney-testing/chutney/pull/1176)
* New scenario execution report view by @boddissattva in [#1174](https://github.com/chutney-testing/chutney/pull/1174)
### 🔧 Technical enhancements:
* chore: Remove last orient dependency by @nbrouand in [#1171](https://github.com/chutney-testing/chutney/pull/1171)
* Add closeable on TestEngine by @nbrouand in [#1172](https://github.com/chutney-testing/chutney/pull/1172)
### 👒 Dependencies:
* chore(deps): Bump sshd.version from 2.9.2 to 2.11.0 by @dependabot in [#1162](https://github.com/chutney-testing/chutney/pull/1162)
* chore(deps-dev): Bump net.jqwik:jqwik from 1.8.0 to 1.8.1 by @dependabot in [#1169](https://github.com/chutney-testing/chutney/pull/1169)
* chore(deps): Bump org.liquibase:liquibase-core from 4.23.0 to 4.24.0 by @dependabot in [#1170](https://github.com/chutney-testing/chutney/pull/1170)




## [2.3.0](https://github.com/chutney-testing/chutney/tree/2.3.0)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/2.2.2...2.3.0)

## What's Changed
### 🚀 Implemented enhancements:
* Endpoint to get the last campaign execution for a given campaign by @DelaunayAlex in https://github.com/chutney-testing/chutney/pull/1142
* Preview imported execution by @rbenyoussef in https://github.com/chutney-testing/chutney/pull/1158
* Add headless for selenium action by @nbrouand in https://github.com/chutney-testing/chutney/pull/1157
* Executions purge by @boddissattva in https://github.com/chutney-testing/chutney/pull/1144
* if strategy by @nbrouand in https://github.com/chutney-testing/chutney/pull/1159
### 🐛 Fixed bugs:
* Scenarios with same name bug on surefire campaign report by @rbenyoussef in https://github.com/chutney-testing/chutney/pull/1156
### 🔧 Technical enhancements:
* Remove components by @nbrouand in https://github.com/chutney-testing/chutney/pull/1143
* Chore debt on webconfig by @DelaunayAlex in https://github.com/chutney-testing/chutney/pull/1155
* Fix Windows build and actions-impl dependency by @boddissattva in https://github.com/chutney-testing/chutney/pull/1138
* Add unsecure api only packaging by @boddissattva in https://github.com/chutney-testing/chutney/pull/1141
### 👒 Dependencies:
* chore(deps-dev): Bump net.jqwik:jqwik from 1.7.4 to 1.8.0 by @dependabot in https://github.com/chutney-testing/chutney/pull/1154
* chore(deps): Bump org.apache.maven.plugins:maven-javadoc-plugin from 3.4.1 to 3.6.0 by @dependabot in https://github.com/chutney-testing/chutney/pull/1149
* chore(deps): Bump qpid-broker.version from 9.0.0 to 9.1.0 by @dependabot in https://github.com/chutney-testing/chutney/pull/1147

## [2.2.2](https://github.com/chutney-testing/chutney/tree/2.2.2)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/2.2.1...2.2.2)

## What's Changed
### 🚀 Implemented enhancements:
* feat(server): Stop running scenario executions reports by @DelaunayAlex in https://github.com/chutney-testing/chutney/pull/1130
* feat(action): Use target security properties whenever it's possible (Mongo, Kafka, http) by @DelaunayAlex in https://github.com/chutney-testing/chutney/pull/1129

### 🐛 Fixed bugs:
* fix(server): Add partialExecution in mapper by @nbrouand in https://github.com/chutney-testing/chutney/pull/1135
* fix(): Include UriBuilder from javax.ws.rs.core for jira and Chutney 2.2.1 with SpringBoot 3 by @nbrouand in https://github.com/chutney-testing/chutney/pull/1136
* fix(ui): fix global var root url by rbenyoussef in https://github.com/chutney-testing/chutney/pull/1137


## [2.2.1](https://github.com/chutney-testing/chutney/tree/2.2.1)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/2.2.0...2.2.1)

## What's Changed
### 🐛 Fixed bugs:
* fix(action-impl): kafka action - filter by content type by @rbenyoussef in https://github.com/chutney-testing/chutney/pull/1070
* Campaign retry by @boddissattva in https://github.com/chutney-testing/chutney/pull/1132

### 🔧 Technical enhancements:
* chore: Bump SpringBoot version from 2.7.5 to 3.1.2 by @bessonm in https://github.com/chutney-testing/chutney/pull/1107
### 👒 Dependencies:
* chore(deps): Bump org.testcontainers:testcontainers-bom from 1.18.3 to 1.19.0 by @dependabot in https://github.com/chutney-testing/chutney/pull/1122
* chore(deps): Bump org.hjson:hjson from 3.0.0 to 3.0.1 by @dependabot in https://github.com/chutney-testing/chutney/pull/1128
* chore(deps): Bump com.github.eirslett:frontend-maven-plugin from 1.12.1 to 1.13.4 by @dependabot in https://github.com/chutney-testing/chutney/pull/1124


## [2.2.0](https://github.com/chutney-testing/chutney/tree/2.2.0)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/2.1.0...2.2.0)

## What's Changed

### 🚀 Implemented enhancements:
* feat(scenario): Allow creation of scenario in database with explicit id by @DelaunayAlex in [\#1112](https://github.com/chutney-testing/chutney/pull/1112)
* feat(ui): Add filter by status for scenarios list by @rbenyoussef in [\#1115](https://github.com/chutney-testing/chutney/pull/1115)


* fix(ui): Fix navigation in empty dataset by @KarimGl in [\#1113](https://github.com/chutney-testing/chutney/pull/1113)
* fix(engine): Evaluate dataset content by @bessonm in [\#1114](https://github.com/chutney-testing/chutney/pull/1114)
* fix(engine): Indexing iteration preserves input types by @bessonm in [\#1116](https://github.com/chutney-testing/chutney/pull/1116)


## [2.1.0](https://github.com/chutney-testing/chutney/tree/2.1.0)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/2.0.0...2.1.0)

## What's Changed

### 🚀 Implemented enhancements:
* feat(ui): persist dataset filter by @rbenyoussef in [\#1066](https://github.com/chutney-testing/chutney/pull/1066)
* fix(server): save campaign - scenario link at scenario execution end by @rbenyoussef in [\#1037](https://github.com/chutney-testing/chutney/pull/1037)
* feature(functions) : jsonSet now accept objects, not only strings by @DelaunayAlex in [\#1086](https://github.com/chutney-testing/chutney/pull/1086)
* feature(search sceanario) : allow research with nested quotes by @DelaunayAlex in [\#1084](https://github.com/chutney-testing/chutney/pull/1084)
* feature(scenario execution): show not executed scenario when campain stop by @DelaunayAlex in [\#1093](https://github.com/chutney-testing/chutney/pull/1093)
* feature(ui,dataset): handle and display error while trying to save dataset with duplicated headers by @DelaunayAlex in [\#1105](https://github.com/chutney-testing/chutney/pull/1105)

### 🐛 Fixed bugs:
* fix(ui): fix target properties edition by @KarimGl in [\#1069](https://github.com/chutney-testing/chutney/pull/1069)
* fix(pkg): Fix parallel exec - Set maxPoolSize to 1 for SQLite by @bessonm in [\#1090](https://github.com/chutney-testing/chutney/pull/1090)

### 🔧 Technical enhancements:
* fix(environment) : check that all targets are unique before saving by @DelaunayAlex in [\#1081](https://github.com/chutney-testing/chutney/pull/1081)
* fix(target) : fix url regex to include numbers, +, - and . in the protocol section by @DelaunayAlex in [\#1082](https://github.com/chutney-testing/chutney/pull/1082)
* Remove complex object from action input by @nbrouand in [\#1083](https://github.com/chutney-testing/chutney/pull/1083)
* doc: Improve GPG signin keys instructions by @bessonm in [\#1068](https://github.com/chutney-testing/chutney/pull/1068)

### 👒 Dependencies:
* chore(deps): Bump maven-enforcer-plugin from 3.1.0 to 3.3.0 by @dependabot in [\#1064](https://github.com/chutney-testing/chutney/pull/1064)
* chore(deps): Bump testcontainers-bom from 1.16.3 to 1.18.3 by @dependabot in [\#1071](https://github.com/chutney-testing/chutney/pull/1071)
* chore(deps-dev): Bump net.jqwik:jqwik from 1.7.3 to 1.7.4 by @dependabot in [\#1102](https://github.com/chutney-testing/chutney/pull/1102)
* chore(deps): Bump org.apache.maven.plugins:maven-source-plugin from 3.2.1 to 3.3.0 by @dependabot in [\#1095](https://github.com/chutney-testing/chutney/pull/1095)
* chore(deps): Bump org.jacoco:jacoco-maven-plugin from 0.8.8 to 0.8.10 by @dependabot in [\#1099](https://github.com/chutney-testing/chutney/pull/1099)
* chore(deps): Bump org.apache.maven.plugins:maven-resources-plugin from 3.3.0 to 3.3.1 by @dependabot in [\#1100](https://github.com/chutney-testing/chutney/pull/1100)
* chore(deps-dev): Bump com.mockrunner:mockrunner-jms from 1.1.2 to 2.0.7 by @dependabot in [\#1103](https://github.com/chutney-testing/chutney/pull/1103)
* chore(ui): Bump nodejs version from v16.15.0 to v16.19.1 by @bessonm in [\#1091](https://github.com/chutney-testing/chutney/pull/1091)
* chore: Bump SpringBoot version from 2.7.5 to 2.7.14 by @bessonm in [\#1108](https://github.com/chutney-testing/chutney/pull/1108)

**Full Changelog**: https://github.com/chutney-testing/chutney/compare/2.0.0...2.1.0


## [2.0.0](https://github.com/chutney-testing/chutney/tree/2.0.0)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.7.1...2.0.0)

## What's Changed
### 🚀 Implemented enhancements:
* Step iterations dataset by [\#1021](https://github.com/chutney-testing/chutney/pull/1021)
* Targets new UI/UX [\#1020](https://github.com/chutney-testing/chutney/pull/1020)
* Show error message for invalid environment name [\#1054](https://github.com/chutney-testing/chutney/pull/1054)
* Run all scenario format using a dataset [\#1065](https://github.com/chutney-testing/chutney/pull/1065)
### 🔧 Technical enhancements:
* Remove 'test' workflow & improve issue template [\#1051](https://github.com/chutney-testing/chutney/pull/1051)
* Toggle components feature when it's not available [\#1038](https://github.com/chutney-testing/chutney/pull/1038)
* Include private and package-only fields in SqlAction ou… [\#1052](https://github.com/chutney-testing/chutney/pull/1052)
* SQLite migration with JPA : Add campaign and executions [\#1040](https://github.com/chutney-testing/chutney/pull/1040)
### 👒 Dependencies:
* Bump maven-surefire-plugin from 3.0.0-M7 to 3.0.0 [\#1049](https://github.com/chutney-testing/chutney/pull/1049)
* Bump jqwik from 1.7.1 to 1.7.3 [\#1041](https://github.com/chutney-testing/chutney/pull/1041)


**Full Changelog**: https://github.com/chutney-testing/chutney/compare/1.7.1...2.0.0

## [1.7.1](https://github.com/chutney-testing/chutney/tree/1.7.1)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.7.0...1.7.1)

## What's Changed
### 🚀 Implemented enhancements:
* Sort scenarios on campaign execution report [\#1022](https://github.com/chutney-testing/chutney/pull/1022)
* Getting result by headers is non sensitive [\#1004](https://github.com/chutney-testing/chutney/pull/1004)
### 🐛 Fixed bugs:
* Fix some bug in UI [\#1017](https://github.com/chutney-testing/chutney/pull/1017)
### 🔧 Technical enhancements:
* Put env in context once for all steps [\#1016](https://github.com/chutney-testing/chutney/pull/1016)
* Rename Action pause/resume/stop as Command [\#1015](https://github.com/chutney-testing/chutney/pull/1015)
* Upgrade to java 17 [\#1019](https://github.com/chutney-testing/chutney/pull/1019)


## [1.7.0](https://github.com/chutney-testing/chutney/tree/1.7.0)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.6.0...1.7.0)
## What's Changed

### 🚀 Implemented enhancements:
* New campaign execution ui [\#1001](https://github.com/chutney-testing/chutney/pull/1001)
* Log stepDefinition raw inputs on assert action [\#968](https://github.com/chutney-testing/chutney/pull/968)
* Execution report new UX [\#971](https://github.com/chutney-testing/chutney/pull/971)
### 🐛 Fixed bugs:
* Fix xsd validation action using file [\#998](https://github.com/chutney-testing/chutney/pull/998)
* Performance issue on scenario list [\#1002](https://github.com/chutney-testing/chutney/pull/1002)
* Make step details sticky in scenario report [\#1000](https://github.com/chutney-testing/chutney/pull/1000)
* Scenario creation date should not be updated [\#965](https://github.com/chutney-testing/chutney/pull/965)
* Fix ts errors on scenario and campaign pages [\#970](https://github.com/chutney-testing/chutney/pull/970)
* Scenario execution fault barrier extension to catch Throwable but VM Errors [\#984](https://github.com/chutney-testing/chutney/pull/984)
* Fix doc broken link [\#985](https://github.com/chutney-testing/chutney/pull/985)
* Missing linkifier pipe in Scenario title & desc [\#987](https://github.com/chutney-testing/chutney/pull/987)
### 🔧 Technical enhancements:
* Refactor theme switch & add themes to storybook [\#997](https://github.com/chutney-testing/chutney/pull/997)
* Remove component dependency [\#999](https://github.com/chutney-testing/chutney/pull/999)
* Fix chutney version and update release management [\#967](https://github.com/chutney-testing/chutney/pull/967)
* Decouple components backup [\#972](https://github.com/chutney-testing/chutney/pull/972)
* Add storybook components & pages [\#973](https://github.com/chutney-testing/chutney/pull/973)
* Test fault barrier only with catch errors [\#986](https://github.com/chutney-testing/chutney/pull/986)
* Add support of sqlite and jpa for scenario repository only [\#974](https://github.com/chutney-testing/chutney/pull/974)


## [1.6.0](https://github.com/chutney-testing/chutney/tree/1.6.0)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.5.6...1.6.0)

**Implemented enhancements:**

- 🚀 | Scenario executions history new UX [\#906](https://github.com/chutney-testing/chutney/pull/906) 
- 🚀 | Add utiliy method on sql Records [\#886](https://github.com/chutney-testing/chutney/issues/886)
- 🚀 | Documentation of version convergence [\#959](https://github.com/chutney-testing/chutney/pull/959) 
- 🚀 | Add affected rows count for single statement [\#958](https://github.com/chutney-testing/chutney/pull/958) 
- 🚀 | Improve dark theme [\#941](https://github.com/chutney-testing/chutney/pull/941)
- 🚀 | Test campaign exec history with not executed scenario [\#931](https://github.com/chutney-testing/chutney/pull/931) 
- 🚀 | SQL records utilities [\#897](https://github.com/chutney-testing/chutney/pull/897) 

**Fixed bugs:**

- 🐛 | Xpath function make report empty [\#960](https://github.com/chutney-testing/chutney/issues/960)
- 🐛 | Quick fix to allow serialization of jdom element [\#961](https://github.com/chutney-testing/chutney/pull/961) 
- 🐛 | Fix raw edition error message [\#945](https://github.com/chutney-testing/chutney/pull/945) 
- 🐛 | Fix white page when cookies expired [\#935](https://github.com/chutney-testing/chutney/pull/935)
- 🐛 | Fix database admin page overflow [\#933](https://github.com/chutney-testing/chutney/pull/933)
- 🐛 | Fix backup page css [\#932](https://github.com/chutney-testing/chutney/pull/932) 
- 🐛 | Fix sequence recalculate at starting [\#930](https://github.com/chutney-testing/chutney/pull/930) 

**Technical enhancements:**

- 🔧 | Local dev ldap [\#922](https://github.com/chutney-testing/chutney/pull/922) 
- 🔧 | Spring Boot maven plugin configuration : wait and JMX [\#942](https://github.com/chutney-testing/chutney/pull/942) 
- 🔧 | Scp test on windows only + agent feature first [\#943](https://github.com/chutney-testing/chutney/pull/943) 
- 🔧 | Change chutney configuration keys [\#944](https://github.com/chutney-testing/chutney/pull/944) 
- 🔧 | Rename Task to Action [\#844](https://github.com/chutney-testing/chutney/pull/844) 
- 🔧 | Remove broken links, component refs and lighten readme [\#934](https://github.com/chutney-testing/chutney/pull/934) 
- 🔧 | Move spring-security-ldap dependency from server to local-dev [\#940](https://github.com/chutney-testing/chutney/pull/940) 

**Dependencies updated:**

- Bump maven-install-plugin from 3.0.1 to 3.1.0 [\#952](https://github.com/chutney-testing/chutney/pull/952) 
- Bump sshj from 0.33.0 to 0.34.0 [\#951](https://github.com/chutney-testing/chutney/pull/951) 
- Bump jqwik from 1.6.5 to 1.7.1 [\#950](https://github.com/chutney-testing/chutney/pull/950) 
- Bump rabbitmq-mock from 1.1.1 to 1.2.0 [\#949](https://github.com/chutney-testing/chutney/pull/949) 
- Bump qpid-broker.version from 8.0.6 to 9.0.0 [\#948](https://github.com/chutney-testing/chutney/pull/948) 
- Bump sshd.version from 2.9.1 to 2.9.2 [\#947](https://github.com/chutney-testing/chutney/pull/947) 
- Bump orient to 3.2.12 [\#939](https://github.com/chutney-testing/chutney/pull/939) 
- Bump springboot.version from 2.7.4 to 2.7.5 [\#911](https://github.com/chutney-testing/chutney/pull/911) 


## [1.5.6](https://github.com/chutney-testing/chutney/tree/1.5.6)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.5.5...1.5.6)

**Implemented enhancements:**

- 🚀 | feat(task): Document unsupported radius protocols mschapv2 & eap [\#921](https://github.com/chutney-testing/chutney/pull/921)

**Fixed bugs:**

- 🐛 | bugfix(jira): Close jira client [\#923](https://github.com/chutney-testing/chutney/pull/923)
- 🐛 | bugfix: Upgrade guava version to 31.0.1-jre. Mandatory for selenium 4.1 [\#924](https://github.com/chutney-testing/chutney/pull/924)

**Merged pull requests:**

- chore(): Do not deploy packaging pom [\#926](https://github.com/chutney-testing/chutney/pull/926)
- chore(): Delete unknown dependency in parent pom [\#925](https://github.com/chutney-testing/chutney/pull/925)
- chore(deps): Remove maven-assembly-plugin [\#909](https://github.com/chutney-testing/chutney/pull/909)

## [1.5.5](https://github.com/chutney-testing/chutney/tree/1.5.5)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.5.4...1.5.5)

**Implemented enhancements:**

- 🚀 | Create xray test execution from test plan [\#607](https://github.com/chutney-testing/chutney/issues/607)

**Fixed bugs:**

- 🐛 | Cannot edit global variable [\#917](https://github.com/chutney-testing/chutney/pull/917)

**Merged pull requests:**

- chore(deps): Bump maven-resources-plugin from 3.2.0 to 3.3.0 [\#907](https://github.com/chutney-testing/chutney/pull/907)
- Enforce dependency convergence [\#894](https://github.com/chutney-testing/chutney/pull/894)

## [1.5.4](https://github.com/chutney-testing/chutney/tree/1.5.4)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.5.2...1.5.4)

**Implemented enhancements:**

- 🚀 | Enable dynamic host for Http targets [\#889](https://github.com/chutney-testing/chutney/issues/889)
- 🚀 | Use a given proxy for HTTP tasks [\#881](https://github.com/chutney-testing/chutney/issues/881)
- 🚀 | Add attribute rel to link [\#892](https://github.com/chutney-testing/chutney/pull/892)
- 🚀 | Remove default role [\#891](https://github.com/chutney-testing/chutney/pull/891) 
- 🚀 | Encrypt and hash sensitive data in config files [\#880](https://github.com/chutney-testing/chutney/pull/880) 

**Fixed bugs:**

- 🐛 | Error message on login page [\#883](https://github.com/chutney-testing/chutney/issues/883)
- 🐛 | User migration issue [\#896](https://github.com/chutney-testing/chutney/pull/896) 
- 🐛 | Fix scenario read angle color [\#888](https://github.com/chutney-testing/chutney/pull/888) 
- 🐛 | Toggle unused header features & restore backup menu [\#887](https://github.com/chutney-testing/chutney/pull/887) 

**Merged pull requests:**
 
- chore\(\): Use spring boot version property for spring boot maven plugin [\#884](https://github.com/chutney-testing/chutney/pull/884) 
- chore\(deps\): bump springboot.version from 2.7.0 to 2.7.4 [\#862](https://github.com/chutney-testing/chutney/pull/862) 


## [1.5.2](https://github.com/chutney-testing/chutney/tree/1.5.2)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.5.1...1.5.2)

**Security enhancements:**

- 🚀 |  fix(xxe): Disable doctype declaration [\#857](https://github.com/chutney-testing/chutney/pull/857)

**Implemented enhancements:**

- 🚀 |  Get local IP reachable from remote IP [\#860](https://github.com/chutney-testing/chutney/issue/860) [\#876](https://github.com/chutney-testing/chutney/pull/876)([gissehel](https://github.com/gissehel))

**Dependencies bumps:**

- chore(deps): bump maven-javadoc-plugin from 3.4.0 to 3.4.1 [\#870](https://github.com/chutney-testing/chutney/pull/870)
- chore(deps): bump jacoco-maven-plugin from 0.8.7 to 0.8.8 [\#873](https://github.com/chutney-testing/chutney/pull/873)


## [1.5.1](https://github.com/chutney-testing/chutney/tree/1.5.1)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.9...1.5.1)

**Implemented enhancements:**

- 🚀 | New chutney instance should create at least a default environment [\#838](https://github.com/chutney-testing/chutney/issues/838)
- 🚀 | Add log to help understanding client errors [\#802](https://github.com/chutney-testing/chutney/issues/802)
- 🚀 | New interface for chutney (login page and layout)[\#827](https://github.com/chutney-testing/chutney/pull/827)
- 🚀 | enable xsd validation from file in classpath, jar in classpath and file system [\#849](https://github.com/chutney-testing/chutney/pull/849) 

**Fixed bugs:**

- 🐛 | Buttons style KO  after deleting all environments [\#832](https://github.com/chutney-testing/chutney/issues/832)
- 🐛 | If no environment, it bugs on interface [\#808](https://github.com/chutney-testing/chutney/issues/808)
- 🐛 | AMQP target with cluster addresses [\#766](https://github.com/chutney-testing/chutney/issues/766)
- 🐛 | Avoid NPE in Micrometer function [\#854](https://github.com/chutney-testing/chutney/pull/854)
- 🐛 | Avoid NPE when exploring target with unknown port [\#850](https://github.com/chutney-testing/chutney/pull/850) 
- 🐛 | Fix linkifier regex validation [\#839](https://github.com/chutney-testing/chutney/pull/839) 
- 🐛 | Scenario execution preview too slow [\#789](https://github.com/chutney-testing/chutney/pull/789)

**Merged pull requests:**

- Delete git backup feature [\#859](https://github.com/chutney-testing/chutney/pull/859) 
- chore/angular13 [\#748](https://github.com/chutney-testing/chutney/pull/748)
- chore\(\): Add thread pool executor for async/sse rest endpoints [\#765](https://github.com/chutney-testing/chutney/pull/765) 
- chore\(deps\): bump maven-javadoc-plugin from 3.3.2 to 3.4.0 [\#799](https://github.com/chutney-testing/chutney/pull/799) 
- chore\(deps\): bump sshd.version from 2.8.0 to 2.9.0 [\#797](https://github.com/chutney-testing/chutney/pull/797) 
- chore\(deps\): bump maven-install-plugin from 2.5.2 to 3.0.1 [\#796](https://github.com/chutney-testing/chutney/pull/796)
- chore\(deps\): bump maven-surefire-plugin from 3.0.0-M5 to 3.0.0-M7 [\#794](https://github.com/chutney-testing/chutney/pull/794) 
- chore\(deps\): bump spring-boot-maven-plugin from 2.7.1 to 2.7.2 [\#793](https://github.com/chutney-testing/chutney/pull/793)
- chore\(deps\): bump groovy from 3.0.11 to 3.0.12 [\#792](https://github.com/chutney-testing/chutney/pull/792) 
- chore\(deps\): bump spring-boot-maven-plugin from 2.7.0 to 2.7.1 [\#782](https://github.com/chutney-testing/chutney/pull/782)
- chore\(deps\): bump caffeine from 3.0.6 to 3.1.1 [\#781](https://github.com/chutney-testing/chutney/pull/781)
- chore\(deps\): bump org.eclipse.jgit.ssh.jsch from 6.1.0.202203080745-r to 6.2.0.202206071550-r [\#780](https://github.com/chutney-testing/chutney/pull/780) 
- chore\(deps\): bump maven-clean-plugin from 3.1.0 to 3.2.0 [\#779](https://github.com/chutney-testing/chutney/pull/779) 
- chore\(deps\): bump h2 from 2.1.210 to 2.1.214 [\#776](https://github.com/chutney-testing/chutney/pull/776) 
- chore\(deps\): bump h2 from 1.4.197 to 2.1.210 in /packaging [\#768](https://github.com/chutney-testing/chutney/pull/768)
- chore\(deps\): bump springboot.version from 2.6.4 to 2.7.0 [\#749](https://github.com/chutney-testing/chutney/pull/749)
- chore\(deps\): bump spring-boot-maven-plugin from 2.7.2 to 2.7.3 [\#847](https://github.com/chutney-testing/chutney/pull/847)
- chore\(deps\): bump exec-maven-plugin from 3.0.0 to 3.1.0 [\#817](https://github.com/chutney-testing/chutney/pull/817)

## [1.4.9](https://github.com/chutney-testing/chutney/tree/HEAD)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.8...1.4.9)

**Implemented enhancements:**

- 🚀 | Allow to report only in scenario reports without logfile [\#741](https://github.com/chutney-testing/chutney/issues/741)
- 🚀 | Delete an environment [\#157](https://github.com/chutney-testing/chutney/issues/157)

**Fixed bugs:**

- 🐛 | FileUtils - initFolder method parallel problem [\#746](https://github.com/chutney-testing/chutney/issues/746)
- 🐛 | Scenario execution - retry step is not updated. Informations are not updated. [\#745](https://github.com/chutney-testing/chutney/issues/745)
- 🐛 | HTTP tasks - Cannot recover Key error when no target keyPassword given [\#744](https://github.com/chutney-testing/chutney/issues/744)
- 🐛 | Target import does not work [\#634](https://github.com/chutney-testing/chutney/issues/634)
- 🐛 | Error message don't disappear from screen  [\#163](https://github.com/chutney-testing/chutney/issues/163)

**Merged pull requests:**

- chore\(\): Bump to rxJava 3 [\#764](https://github.com/chutney-testing/chutney/pull/764) 
- feat\(task\): add configurable ssl context to amqp task [\#763](https://github.com/chutney-testing/chutney/pull/763) 
- feat\(task-impl\): should assert enum as string in jsonassert [\#762](https://github.com/chutney-testing/chutney/pull/762) 
- chore\(deps\): bump groovy from 3.0.9 to 3.0.11 [\#759](https://github.com/chutney-testing/chutney/pull/759) 
- chore\(deps\): bump spring-boot-maven-plugin from 2.6.7 to 2.7.0 [\#757](https://github.com/chutney-testing/chutney/pull/757) 
- chore\(deps\): bump mongodb.version from 4.5.1 to 4.6.0 [\#755](https://github.com/chutney-testing/chutney/pull/755) 
- chore\(deps\): bump jackson.version from 2.13.2 to 2.13.3 [\#753](https://github.com/chutney-testing/chutney/pull/753) 
- chore\(deps-dev\): bump mockito.version from 4.4.0 to 4.6.0 [\#752](https://github.com/chutney-testing/chutney/pull/752) 
- chore\(deps-dev\): bump jqwik from 1.6.2 to 1.6.5 [\#751](https://github.com/chutney-testing/chutney/pull/751) 
- chore\(deps-dev\): bump activemq.version from 5.16.4 to 5.17.1 [\#750](https://github.com/chutney-testing/chutney/pull/750) 
- fix\(tools\): init folder parallel execution support. [\#747](https://github.com/chutney-testing/chutney/pull/747) 
- refacto\(components\): remove pagination [\#743](https://github.com/chutney-testing/chutney/pull/743) 
- feat: Allow to log only for scenario reports but not logfiles [\#742](https://github.com/chutney-testing/chutney/pull/742) 
- feat\(security\): grant full access to admin [\#739](https://github.com/chutney-testing/chutney/pull/739) 
- Create specific module for component/dataset/orient [\#738](https://github.com/chutney-testing/chutney/pull/738) 
- SPI Target simplification [\#737](https://github.com/chutney-testing/chutney/pull/737) 
- tech\(server\): Move campaign & globalvar to own pkg [\#733](https://github.com/chutney-testing/chutney/pull/733) 
- doc: Document how to sign commits and release artifacts [\#732](https://github.com/chutney-testing/chutney/pull/732) 


## [1.4.8](https://github.com/chutney-testing/chutney/tree/1.4.8)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.7...1.4.8)

**Fixed bugs:**

- 🐛 | NPE - Report info or error with null values [\#719](https://github.com/chutney-testing/chutney/issues/719)
- 🐛 | Engine - Final task with environment null [\#718](https://github.com/chutney-testing/chutney/issues/718)

**Merged pull requests:**

- Final task environment null [\#731](https://github.com/chutney-testing/chutney/pull/731) 
- chore\(deps\): bump sshj from 0.32.0 to 0.33.0 [\#730](https://github.com/chutney-testing/chutney/pull/730) 
- chore\(deps\): bump value from 2.8.8 to 2.9.0 [\#729](https://github.com/chutney-testing/chutney/pull/729) 
- chore\(deps\): bump amqp-client from 5.14.1 to 5.14.2 [\#728](https://github.com/chutney-testing/chutney/pull/728) 
- chore\(deps\): bump postgresql from 42.3.3 to 42.3.4 [\#727](https://github.com/chutney-testing/chutney/pull/727) 
- chore\(deps\): bump guava from 31.0.1-jre to 31.1-jre [\#726](https://github.com/chutney-testing/chutney/pull/726) 
- chore\(deps\): bump maven-compiler-plugin from 3.10.0 to 3.10.1 [\#724](https://github.com/chutney-testing/chutney/pull/724) 
- chore\(deps\): bump spring-boot-maven-plugin from 2.6.3 to 2.6.7 [\#723](https://github.com/chutney-testing/chutney/pull/723) 
- chore\(deps\): bump org.eclipse.jgit.ssh.jsch from 5.13.0.202109080827-r to 6.1.0.202203080745-r [\#721](https://github.com/chutney-testing/chutney/pull/721) 
- Report info or error null values [\#720](https://github.com/chutney-testing/chutney/pull/720) 



## [1.4.7](https://github.com/chutney-testing/chutney/tree/1.4.7) (2022-04-26)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.6...1.4.7)

**Implemented enhancements:**

- 🚀 | Add some JSON modifications functions [\#698](https://github.com/chutney-testing/chutney/issues/698)

**Merged pull requests:**

- bugfix\(server\): Quickfix unoptimize code causing huge slowdown [\#717](https://github.com/chutney-testing/chutney/pull/717) 
- fix\(ui\): disable jira call without test exec id [\#716](https://github.com/chutney-testing/chutney/pull/716) ([rbenyoussef](https://github.com/rbenyoussef))
- refactor\(\): Clean old scenario version support [\#714](https://github.com/chutney-testing/chutney/pull/714) 
- feat\(ui\) : Component edition space [\#713](https://github.com/chutney-testing/chutney/pull/713) 
- feat\(func\): Add JSON functions available in SpEL [\#712](https://github.com/chutney-testing/chutney/pull/712) 
- Chore/dette [\#711](https://github.com/chutney-testing/chutney/pull/711) 
- feat\(ui\): Add search on tags in text search [\#710](https://github.com/chutney-testing/chutney/pull/710) 
- chore\(deps-dev\): bump json-path from 2.6.0 to 2.7.0 [\#709](https://github.com/chutney-testing/chutney/pull/709) 
- chore\(deps\): bump everit-json-schema from 1.14.0 to 1.14.1 [\#708](https://github.com/chutney-testing/chutney/pull/708) 
- chore\(deps\): bump maven-javadoc-plugin from 3.3.1 to 3.3.2 [\#707](https://github.com/chutney-testing/chutney/pull/707) 
- chore\(deps-dev\): bump mockito.version from 4.3.1 to 4.4.0 [\#706](https://github.com/chutney-testing/chutney/pull/706) 
- chore\(deps\): bump caffeine from 3.0.5 to 3.0.6 [\#705](https://github.com/chutney-testing/chutney/pull/705) 
- chore\(deps\): bump jackson.version from 2.13.1 to 2.13.2 [\#704](https://github.com/chutney-testing/chutney/pull/704) 
- chore\(deps-dev\): bump spring-kafka-test from 2.8.2 to 2.8.4 [\#703](https://github.com/chutney-testing/chutney/pull/703) 
- chore\(deps\): bump mongodb.version from 4.4.1 to 4.5.1 [\#702](https://github.com/chutney-testing/chutney/pull/702) 
- chore\(deps\): bump selenium.version from 4.1.2 to 4.1.3 [\#701](https://github.com/chutney-testing/chutney/pull/701) 
- chore\(deps-dev\): bump awaitility from 4.1.1 to 4.2.0 [\#700](https://github.com/chutney-testing/chutney/pull/700) 

## [1.4.6](https://github.com/chutney-testing/chutney/tree/1.4.6) (2022-03-11)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.5...1.4.6)

**Fixed bugs:**

- 🐛 | In chutney junit verify final status of an scenario [\#683](https://github.com/chutney-testing/chutney/issues/683)
- 🐛 | Final task with validations on outputs doesn't work [\#678](https://github.com/chutney-testing/chutney/issues/678)
- 🐛 | Do not refresh all the page during an execution [\#633](https://github.com/chutney-testing/chutney/issues/633)

**Merged pull requests:**

- chore\(\): Fix kafka test dependencies. Put chutney-junit-engine in test scope [\#697](https://github.com/chutney-testing/chutney/pull/697) 
- fix\(ui\): scenario execution - Refresh only report's parts that have changed [\#696](https://github.com/chutney-testing/chutney/pull/696) 
- chore\(\): clean tests [\#695](https://github.com/chutney-testing/chutney/pull/695) 
- chore\(deps-dev\): bump assertj-core from 3.21.0 to 3.22.0 [\#694](https://github.com/chutney-testing/chutney/pull/694) 
- chore\(deps\): bump orientdb.version from 3.2.4 to 3.2.5 [\#693](https://github.com/chutney-testing/chutney/pull/693) 
- chore\(deps\): bump maven-compiler-plugin from 3.8.1 to 3.10.0 [\#692](https://github.com/chutney-testing/chutney/pull/692) 
- chore\(deps\): bump json-smart from 2.4.2 to 2.4.8 [\#691](https://github.com/chutney-testing/chutney/pull/691) 
- chore\(deps\): bump tinyradius from 1.1.0 to 1.1.3 [\#690](https://github.com/chutney-testing/chutney/pull/690) 
- chore\(deps\): bump postgresql from 42.3.1 to 42.3.3 [\#689](https://github.com/chutney-testing/chutney/pull/689) 
- chore\(deps\): bump picocli from 4.6.2 to 4.6.3 [\#688](https://github.com/chutney-testing/chutney/pull/688) 
- chore\(deps\): bump sl4j.api.version from 1.7.35 to 1.7.36 [\#687](https://github.com/chutney-testing/chutney/pull/687) 
- chore\(deps-dev\): bump activemq.version from 5.16.3 to 5.16.4 [\#686](https://github.com/chutney-testing/chutney/pull/686) 
- chore\(deps\): bump springboot.version from 2.6.3 to 2.6.4 [\#685](https://github.com/chutney-testing/chutney/pull/685) 
- fix\(683\): check if report status is not SUCCESS instead of just FAILURE [\#684](https://github.com/chutney-testing/chutney/pull/684) 
- fix mongodb and kafka versions after springboot upgrade [\#682](https://github.com/chutney-testing/chutney/pull/682) 
- bugfix\(engine+server\): fix validations on registred final task [\#681](https://github.com/chutney-testing/chutney/pull/681) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(spring\): upgrade spring boot version [\#680](https://github.com/chutney-testing/chutney/pull/680) 
- feat/json lenient compare & assert [\#677](https://github.com/chutney-testing/chutney/pull/677) 
- feat\(docker\): add docker packaging [\#676](https://github.com/chutney-testing/chutney/pull/676) 
- feat\(fun\): Generate a file with random content [\#675](https://github.com/chutney-testing/chutney/pull/675) 


## [1.4.5](https://github.com/chutney-testing/chutney/tree/1.4.5) (2022-02-08)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.4...1.4.5)

**Fixed bugs:**
- 🐛 | chore(jira): Remove infra dependency in JiraXrayService [\#673] (https://github.com/chutney-testing/chutney/pull/673)

**Merged pull requests:**

- chore\(jira\): Remove infra dependency in JiraXrayService [\#673](https://github.com/chutney-testing/chutney/pull/673) 
- bugfix\(jira+ui\): create jira service bugfix [\#672](https://github.com/chutney-testing/chutney/pull/672) 
- chore\(deps\): bump build-helper-maven-plugin from 3.2.0 to 3.3.0 [\#670](https://github.com/chutney-testing/chutney/pull/670) 
- chore\(deps\): bump maven-jar-plugin from 3.2.0 to 3.2.2 [\#669](https://github.com/chutney-testing/chutney/pull/669) 
- chore\(deps\): bump spring-boot-maven-plugin from 2.6.2 to 2.6.3 [\#666](https://github.com/chutney-testing/chutney/pull/666) 
- chore\(deps\): bump amqp-client from 5.14.0 to 5.14.1 [\#665](https://github.com/chutney-testing/chutney/pull/665) 
- chore\(deps-dev\): bump mockito.version from 4.1.0 to 4.3.1 [\#664](https://github.com/chutney-testing/chutney/pull/664) 
- chore\(deps\): bump sl4j.api.version from 1.7.32 to 1.7.35 [\#663](https://github.com/chutney-testing/chutney/pull/663) 
- chore\(deps\): bump selenium.version from 4.1.1 to 4.1.2 [\#662](https://github.com/chutney-testing/chutney/pull/662) 
- Feat/ftp task [\#661](https://github.com/chutney-testing/chutney/pull/661) 
- chore\(server\): Replace Guava cache by Caffeine [\#660](https://github.com/chutney-testing/chutney/pull/660) 
- fix build win [\#659](https://github.com/chutney-testing/chutney/pull/659) 

## [1.4.4](https://github.com/chutney-testing/chutney/tree/1.4.4) (2022-01-27)
[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.3...1.4.4)

**Implemented enhancements:**

- 🚀 | Add Scp task [\#638](https://github.com/chutney-testing/chutney/issues/638)
- 🚀 | UI - add a filter on JIRA id to search scenario [\#609](https://github.com/chutney-testing/chutney/issues/609)
- 🚀 | Filter the tests to add to the campaign according to the status in the test exec [\#606](https://github.com/chutney-testing/chutney/issues/606)
- 🚀 | Update of the status of an xray test from the chutney execution report [\#605](https://github.com/chutney-testing/chutney/issues/605)
- 🚀 | add search with jira id [\#576](https://github.com/chutney-testing/chutney/issues/576)

**Fixed bugs:**

- 🐛 | Do not evaluate output if task is on error [\#629](https://github.com/chutney-testing/chutney/issues/629)
- 🐛 |  Parameters page is not displayed for execution [\#613](https://github.com/chutney-testing/chutney/issues/613)
- 🐛 | Output of task in report have serialization error [\#424](https://github.com/chutney-testing/chutney/issues/424)

**Merged pull requests:**

- fix\(server\): Scheduled campaigns : Referential update by only one thread [\#658](https://github.com/chutney-testing/chutney/pull/658) 
- feat\(task\): SCP client upload/download [\#657](https://github.com/chutney-testing/chutney/pull/657) 
- feat\(jira+ui\): Update of the status of an xray test from campaign execution report [\#656](https://github.com/chutney-testing/chutney/pull/656) 
- chore\(\): Codeql [\#654](https://github.com/chutney-testing/chutney/pull/654) 
- chore\(deps\): bump spring-boot-maven-plugin from 2.6.1 to 2.6.2 [\#652](https://github.com/chutney-testing/chutney/pull/652) 
- chore\(deps-dev\): bump jqwik from 1.5.6 to 1.6.2 [\#651](https://github.com/chutney-testing/chutney/pull/651) 
- chore\(deps\): bump orientdb.version from 3.0.30 to 3.2.4 [\#650](https://github.com/chutney-testing/chutney/pull/650) 
- chore\(deps\): bump picocli from 4.6.1 to 4.6.2 [\#649](https://github.com/chutney-testing/chutney/pull/649) 
- chore\(deps\): bump jackson.version from 2.13.0 to 2.13.1 [\#648](https://github.com/chutney-testing/chutney/pull/648) 
- chore\(deps\): bump guava from 30.1.1-jre to 31.0.1-jre [\#647](https://github.com/chutney-testing/chutney/pull/647) 
- chore\(deps\): bump selenium.version from 4.0.0 to 4.1.1 [\#646](https://github.com/chutney-testing/chutney/pull/646) 
- chore\(deps\): bump frontend-maven-plugin from 1.8.0 to 1.12.1 [\#645](https://github.com/chutney-testing/chutney/pull/645) 
- feat\(task-impl\): Kafka consume with acknowledge mode as input [\#643](https://github.com/chutney-testing/chutney/pull/643) 
- chore\(\): doc enhancement [\#642](https://github.com/chutney-testing/chutney/pull/642) 
- feat/datetime functions [\#641](https://github.com/chutney-testing/chutney/pull/641) 
- feat\(engine\): Run step validations & outputs only on successfull task [\#636](https://github.com/chutney-testing/chutney/pull/636) 
- Filter the tests to add to the campaign according to the status in the test exec [\#635](https://github.com/chutney-testing/chutney/pull/635) 
- feat\(functions\): Add spEL functions [\#628](https://github.com/chutney-testing/chutney/pull/628) 
- fix\(ui\): Check if scenario has parameters before manual execution [\#627](https://github.com/chutney-testing/chutney/pull/627) 
- Feat/609 [\#626](https://github.com/chutney-testing/chutney/pull/626) 
- Feat/jira refacto [\#625](https://github.com/chutney-testing/chutney/pull/625) 
- chore\(deps-dev\): bump awaitility from 4.1.0 to 4.1.1 [\#623](https://github.com/chutney-testing/chutney/pull/623) 
- chore\(deps-dev\): bump mockito.version from 4.0.0 to 4.1.0 [\#622](https://github.com/chutney-testing/chutney/pull/622) 
- chore\(deps\): bump spring-boot-maven-plugin from 2.5.3 to 2.6.1 [\#621](https://github.com/chutney-testing/chutney/pull/621) 
- chore\(deps\): bump amqp-client from 5.13.0 to 5.14.0 [\#620](https://github.com/chutney-testing/chutney/pull/620) 
- chore\(deps\): bump junit5.version from 5.8.1 to 5.8.2 [\#619](https://github.com/chutney-testing/chutney/pull/619) 
- chore\(deps\): bump pitest.version from 1.7.2 to 1.7.3 [\#616](https://github.com/chutney-testing/chutney/pull/616) 
- fix\(server\): Scenario execution report JSON with numbers as strings [\#611](https://github.com/chutney-testing/chutney/pull/611) 
- chore\(deps\): bump selenium.version from 3.141.59 to 4.0.0 [\#596](https://github.com/chutney-testing/chutney/pull/596) 


## [1.4.3](https://github.com/chutney-testing/chutney/tree/1.4.3) (2021-11-29)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.2...1.4.3)

**Fixed bugs:**

- 🐛 | Parralel issue on campagin schedule [\#590](https://github.com/chutney-testing/chutney/issues/590)

**Merged pull requests:**

- fix\(server\): Check we have at least one remote to export [\#612](https://github.com/chutney-testing/chutney/pull/612) 
- feat\(task\): HTTPS Server mock trust all by default [\#610](https://github.com/chutney-testing/chutney/pull/610) 
- fix/scheduled campaigns [\#604](https://github.com/chutney-testing/chutney/pull/604) 
- chore/fix build [\#603](https://github.com/chutney-testing/chutney/pull/603) 
- feat: Use key store key password for JMS and Wiremock [\#602](https://github.com/chutney-testing/chutney/pull/602) 
- chore: Auto set UI node version for nix [\#601](https://github.com/chutney-testing/chutney/pull/601) 
- chore\(deps\): bump sshj from 0.31.0 to 0.32.0 [\#592](https://github.com/chutney-testing/chutney/pull/592) 


## [1.4.2](https://github.com/chutney-testing/chutney/tree/1.4.2) (2021-11-09)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.1...1.4.2)

**Implemented enhancements:**

- 🚀 | Add Radius operation in task-impl [\#535](https://github.com/chutney-testing/chutney/issues/535)

**Fixed bugs:**

- 🐛 | Multi consumer on same queue for amqp basic consume [\#587](https://github.com/chutney-testing/chutney/issues/587)

**Merged pull requests:**

- chore\(deps\): bump jackson.version from 2.12.5 to 2.13.0 [\#599](https://github.com/chutney-testing/chutney/pull/599) 
- chore\(deps\): bump pitest.version from 1.7.1 to 1.7.2 [\#598](https://github.com/chutney-testing/chutney/pull/598) 
- chore\(deps\): bump postgresql from 42.2.16 to 42.3.1 [\#597](https://github.com/chutney-testing/chutney/pull/597) 
- chore\(deps-dev\): bump assertj-core from 3.20.2 to 3.21.0 [\#595](https://github.com/chutney-testing/chutney/pull/595) 
- chore\(deps\): bump everit-json-schema from 1.12.2 to 1.14.0 [\#594](https://github.com/chutney-testing/chutney/pull/594) 
- chore\(deps-dev\): bump mockito.version from 3.12.4 to 4.0.0 [\#591](https://github.com/chutney-testing/chutney/pull/591) 
- feat: Use keystore keypassword [\#589](https://github.com/chutney-testing/chutney/pull/589) 
- bugfix\(task-impl\): Allow only one consumer by queue at a time for amqp [\#588](https://github.com/chutney-testing/chutney/pull/588) 
- chore\(deps\): bump groovy from 2.5.14 to 3.0.9 [\#549](https://github.com/chutney-testing/chutney/pull/549) 
- feat\(task-impl\): Add radius tasks [\#536](https://github.com/chutney-testing/chutney/pull/536) 

## [1.4.1](https://github.com/chutney-testing/chutney/tree/1.4.1) (2021-10-12)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.4.0...1.4.1)

**Implemented enhancements:**

- 🚀 | Add validation input to task-spi [\#585](https://github.com/chutney-testing/chutney/issues/585)
- 🚀 | Make json compare as soft assert [\#577](https://github.com/chutney-testing/chutney/issues/577)

**Fixed bugs:**

- 🐛 | SSH client task : Cannot use private key with passphrase authentication [\#564](https://github.com/chutney-testing/chutney/issues/564)
- 🐛 | With parrarell testing, some test failed randomly [\#561](https://github.com/chutney-testing/chutney/issues/561)
- 🐛 | Search scenario issues [\#554](https://github.com/chutney-testing/chutney/issues/554)

**Merged pull requests:**

- Task input validators [\#584](https://github.com/chutney-testing/chutney/pull/584) 
- chore\(deps\): bump maven-javadoc-plugin from 3.3.0 to 3.3.1 [\#575](https://github.com/chutney-testing/chutney/pull/575) 
- chore\(deps\): bump org.eclipse.jgit.ssh.jsch from 5.12.0.202106070339-r to 5.13.0.202109080827-r [\#574](https://github.com/chutney-testing/chutney/pull/574) 
- chore\(deps-dev\): bump jqwik from 1.5.1 to 1.5.6 [\#573](https://github.com/chutney-testing/chutney/pull/573) 
- chore\(deps\): bump wss4j from 1.6.17 to 1.6.19 [\#572](https://github.com/chutney-testing/chutney/pull/572) 
- chore\(deps\): bump mongodb.version from 3.12.8 to 3.12.10 [\#570](https://github.com/chutney-testing/chutney/pull/570) 
- chore\(deps\): bump pitest.version from 1.6.9 to 1.7.1 [\#567](https://github.com/chutney-testing/chutney/pull/567) 
- chore\(deps\): bump junit5.version from 5.7.2 to 5.8.1 [\#566](https://github.com/chutney-testing/chutney/pull/566) 
- SSH client task - Private key with passphrase authentication [\#565](https://github.com/chutney-testing/chutney/pull/565) 
- Chore/UI clean [\#563](https://github.com/chutney-testing/chutney/pull/563) 
- Chore/parallel test pb [\#562](https://github.com/chutney-testing/chutney/pull/562) 
- bugfix\(ui\): Debounce on full text search + redesign scenario search UI + [\#560](https://github.com/chutney-testing/chutney/pull/560) 



## [1.4.0](https://github.com/chutney-testing/chutney/tree/1.4.0) (2021-09-16)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.12...1.4.0)

**Implemented enhancements:**

- 🚀 | Import/Export Chutney content as files from git [\#552](https://github.com/chutney-testing/chutney/issues/552)
- 🚀 | Allow the declaration of finally action in the scenario [\#534](https://github.com/chutney-testing/chutney/issues/534)

**Merged pull requests:**

- feat/finally action register task [\#551](https://github.com/chutney-testing/chutney/pull/551) 
- feat\(ui/server\): Import content from git [\#550](https://github.com/chutney-testing/chutney/pull/550) 
- chore: Add unit test for testing url security [\#548](https://github.com/chutney-testing/chutney/pull/548) 
- chore: Add architecture image to readme [\#537](https://github.com/chutney-testing/chutney/pull/537) 


## [1.3.12](https://github.com/chutney-testing/chutney/tree/1.3.12) (2021-09-03)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.11...1.3.12)

**Implemented enhancements:**

- 🚀 | Simplify campaign scheduling  [\#511](https://github.com/chutney-testing/chutney/issues/511)

**Fixed bugs:**

- 🐛 | Task development with complex object inputs [\#526](https://github.com/chutney-testing/chutney/issues/526)
- 🐛 | Add try catch with log on schedule campaign [\#498](https://github.com/chutney-testing/chutney/issues/498)
- 🐛 | Log authentication errors [\#495](https://github.com/chutney-testing/chutney/issues/495)
- 🐛 | First campaign execution error [\#456](https://github.com/chutney-testing/chutney/issues/456)
- 🐛 | Panel of last 10 executions disappear [\#324](https://github.com/chutney-testing/chutney/issues/324)
- 🐛 | Cannot see report of running execution [\#306](https://github.com/chutney-testing/chutney/issues/306)
- 🐛 | Dataset export/import to csv has line end issue [\#212](https://github.com/chutney-testing/chutney/issues/212)
- 🐛 | Too many open files [\#159](https://github.com/chutney-testing/chutney/issues/159)
- 🐛 | Lost of info and error when we stop scenario [\#119](https://github.com/chutney-testing/chutney/issues/119)

**Closed issues:**

- Link to campaign execution [\#17](https://github.com/chutney-testing/chutney/issues/17)
- Is it necessary to have the java exception in the error message? [\#165](https://github.com/chutney-testing/chutney/issues/165)
- Add progress bar for running campaign [\#112](https://github.com/chutney-testing/chutney/issues/112)

**Merged pull requests:**

- chore\(deps\): bump jackson.version from 2.12.4 to 2.12.5 [\#547](https://github.com/chutney-testing/chutney/pull/547) 
- chore\(deps\): bump qpid-broker.version from 8.0.5 to 8.0.6 [\#543](https://github.com/chutney-testing/chutney/pull/543) 
- chore\(deps-dev\): bump mockito.version from 3.11.2 to 3.12.4 [\#542](https://github.com/chutney-testing/chutney/pull/542) 
- chore\(deps-dev\): bump awaitility from 4.0.3 to 4.1.0 [\#540](https://github.com/chutney-testing/chutney/pull/540) 
- chore\(deps\): bump pitest.version from 1.6.8 to 1.6.9 [\#539](https://github.com/chutney-testing/chutney/pull/539) 
- chore\(deps-dev\): bump activemq.version from 5.16.1 to 5.16.3 [\#538](https://github.com/chutney-testing/chutney/pull/538) 
-  Fix missing column import in dataset if no semi colon at the end of the file [\#533](https://github.com/chutney-testing/chutney/pull/533) 
- feat\(engine\): Git export on shutdown [\#531](https://github.com/chutney-testing/chutney/pull/531) 
- feat\(engine\): On retry strategy, add last error message [\#530](https://github.com/chutney-testing/chutney/pull/530) 
- feat\(server\): Remove scheduletime from campaign [\#529](https://github.com/chutney-testing/chutney/pull/529) 
- feat\(server\): Export campaigns and composable steps [\#528](https://github.com/chutney-testing/chutney/pull/528) 
- bugfix\(engine\): Complex input without annotation does not appear in UI [\#527](https://github.com/chutney-testing/chutney/pull/527) 
- chore/junit parallel [\#525](https://github.com/chutney-testing/chutney/pull/525) 
- chore\(deps\): bump spring-boot-maven-plugin from 2.1.3.RELEASE to 2.5.3 [\#524](https://github.com/chutney-testing/chutney/pull/524) 
- chore\(deps\): bump commons-io from 2.10.0 to 2.11.0 [\#523](https://github.com/chutney-testing/chutney/pull/523) 
- chore\(deps\): bump jackson.version from 2.12.3 to 2.12.4 [\#522](https://github.com/chutney-testing/chutney/pull/522) 
- chore\(deps\): bump org.eclipse.jgit.ssh.jsch from 5.11.0.202103091610-r to 5.12.0.202106070339-r [\#521](https://github.com/chutney-testing/chutney/pull/521) 
- chore\(deps\): bump amqp-client from 5.8.0 to 5.13.0 [\#520](https://github.com/chutney-testing/chutney/pull/520) 
- chore\(deps-dev\): bump json-path from 2.4.0 to 2.6.0 [\#519](https://github.com/chutney-testing/chutney/pull/519) 
- chore\(deps\): bump sl4j.api.version from 1.7.31 to 1.7.32 [\#518](https://github.com/chutney-testing/chutney/pull/518) 
- chore\(deps\): bump pitest.version from 1.5.1 to 1.6.8 [\#515](https://github.com/chutney-testing/chutney/pull/515) 
- fix\(server\): Warn when scenario is not found getting campaign last executions [\#514](https://github.com/chutney-testing/chutney/pull/514) 
- fix/logauth and campaign UI [\#512](https://github.com/chutney-testing/chutney/pull/512) 
- fix\(server\): Campaign scheduler robustness [\#510](https://github.com/chutney-testing/chutney/pull/510) 


## [1.3.11](https://github.com/chutney-testing/chutney/tree/1.3.11) (2021-07-19)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.10...1.3.11)

**Fixed bugs:**

- 🐛 | Links to JIRA are not working [\#508](https://github.com/chutney-testing/chutney/issues/508)

**Merged pull requests:**

- fix\(server+ui\): JIRA configuration url endpoint produces plain text [\#509](https://github.com/chutney-testing/chutney/pull/509) 


## [1.3.10](https://github.com/chutney-testing/chutney/tree/1.3.10) (2021-07-13)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.9...1.3.10)

**Fixed bugs:**

- 🐛 | LDAP authentification problem - Authorities have role prefix ROLE\_ [\#506](https://github.com/chutney-testing/chutney/issues/506)
- 🐛 | Execute button of scenario does not work [\#505](https://github.com/chutney-testing/chutney/issues/505)

**Merged pull requests:**

- fix/scenario execute UI [\#507](https://github.com/chutney-testing/chutney/pull/507) 


## [1.3.9](https://github.com/chutney-testing/chutney/tree/1.3.9) (2021-07-12)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.8...1.3.9)

**Implemented enhancements:**

- 🚀 | JSON assert : JSONPath with selector returns array problem with placeholder [\#344](https://github.com/chutney-testing/chutney/issues/344)
- 🚀 | Add full scenario text research on scenario page [\#331](https://github.com/chutney-testing/chutney/issues/331)
- 🚀 | Add authorization management [\#95](https://github.com/chutney-testing/chutney/issues/95)

**Fixed bugs:**

- 🐛 | soft strategy with validation should be in error [\#502](https://github.com/chutney-testing/chutney/issues/502)
- 🐛 | UI lags on complex component testcases [\#494](https://github.com/chutney-testing/chutney/issues/494)
- 🐛 | Datasets : search filter clear button is not working [\#481](https://github.com/chutney-testing/chutney/issues/481)
- 🐛 | Chutney logo on login page unresolved when logout [\#480](https://github.com/chutney-testing/chutney/issues/480)
- 🐛 | Wrong configuration folder during tests [\#460](https://github.com/chutney-testing/chutney/issues/460)
- 🐛 | core\_unit\_tests run configuration also run core\_integration\_tests [\#402](https://github.com/chutney-testing/chutney/issues/402)
- 🐛 | No error message when wrong login [\#338](https://github.com/chutney-testing/chutney/issues/338)

**Closed issues:**

- Out of memory on big SQL result [\#69](https://github.com/chutney-testing/chutney/issues/69)

**Merged pull requests:**

- fix\(egine\): Validations only set FAILURE step status [\#504](https://github.com/chutney-testing/chutney/pull/504) 
- fix\(ui\): Map deselect all event on angular-multiselect [\#497](https://github.com/chutney-testing/chutney/pull/497) 
- fix\(junit\): Use class filter in junit engine [\#496](https://github.com/chutney-testing/chutney/pull/496) 
- chore\(ui+server\): Load independently testcase header and content [\#493](https://github.com/chutney-testing/chutney/pull/493) 
- chore\(deps-dev\): bump assertj-core from 3.19.0 to 3.20.2 [\#491](https://github.com/chutney-testing/chutney/pull/491) 
- chore\(deps\): bump sl4j.api.version from 1.7.30 to 1.7.31 [\#490](https://github.com/chutney-testing/chutney/pull/490) 
- chore\(deps-dev\): bump mockito.version from 3.10.0 to 3.11.2 [\#489](https://github.com/chutney-testing/chutney/pull/489) 
- chore\(deps\): bump commons-io from 2.8.0 to 2.10.0 [\#487](https://github.com/chutney-testing/chutney/pull/487) 
- chore\(deps\): bump maven-javadoc-plugin from 3.2.0 to 3.3.0 [\#485](https://github.com/chutney-testing/chutney/pull/485) 
- chore\(deps-dev\): bump otj-pg-embedded from 0.13.1 to 0.13.4 [\#484](https://github.com/chutney-testing/chutney/pull/484) 
- chore\(deps\): bump qpid-broker.version from 7.0.6 to 8.0.5 [\#483](https://github.com/chutney-testing/chutney/pull/483) 
- Feat/authorizations [\#477](https://github.com/chutney-testing/chutney/pull/477) 

## [1.3.8](https://github.com/chutney-testing/chutney/tree/1.3.8) (2021-06-23)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.7...1.3.8)

**Implemented enhancements:**

- 🚀 | Include jira link in backup [\#458](https://github.com/chutney-testing/chutney/issues/458)
- 🚀 | Add research full text in scenario [\#397](https://github.com/chutney-testing/chutney/issues/397)
- 🚀 | Add global variables support to new ui [\#390](https://github.com/chutney-testing/chutney/issues/390)

**Fixed bugs:**

- 🐛 | SQL task - mapping and print errors when having 2 or more column with same header [\#478](https://github.com/chutney-testing/chutney/issues/478)
- 🐛 | Pause button doesnt appear anymore [\#476](https://github.com/chutney-testing/chutney/issues/476)
- 🐛 | SQL client change [\#470](https://github.com/chutney-testing/chutney/issues/470)
- 🐛 | Regression in scenario execution [\#469](https://github.com/chutney-testing/chutney/issues/469)
- 🐛 | Random classnotfound exeception [\#466](https://github.com/chutney-testing/chutney/issues/466)
- 🐛 | Missing dataset data in git export [\#461](https://github.com/chutney-testing/chutney/issues/461)
- 🐛 | Environment backup [\#457](https://github.com/chutney-testing/chutney/issues/457)
- 🐛 | Navigation to scenario components is broken [\#434](https://github.com/chutney-testing/chutney/issues/434)

**Merged pull requests:**

- fix\(task\): SQL result with duplicated column name [\#479](https://github.com/chutney-testing/chutney/pull/479) 
- bugfix\(engine\): Add pause as report status for running scenario [\#475](https://github.com/chutney-testing/chutney/pull/475) 
- fix\(server\): Backup all environments [\#474](https://github.com/chutney-testing/chutney/pull/474) 
- fix\(server\): Includes datatable & constants on exporting dataset to git [\#473](https://github.com/chutney-testing/chutney/pull/473) 
- feat\(ui+server\): Backup jira links [\#472](https://github.com/chutney-testing/chutney/pull/472) 
- Bugfix/sql task change [\#471](https://github.com/chutney-testing/chutney/pull/471) 
- fix\(task-impl\): Keep numeric and date JDBC SQL Types in task result [\#468](https://github.com/chutney-testing/chutney/pull/468) 
- Chore/taskexecutor [\#467](https://github.com/chutney-testing/chutney/pull/467) 
- feat\(ui+server\): Add full text scenario search [\#465](https://github.com/chutney-testing/chutney/pull/465) 


## [1.3.7](https://github.com/chutney-testing/chutney/tree/1.3.7) (2021-06-09)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.6...1.3.7)

**Fixed bugs:**

- 🐛 | When setting Xms on Chutney, SQL task always raise NotEnoughMemoryException [\#463](https://github.com/chutney-testing/chutney/issues/463)

**Merged pull requests:**

- bugfix\(engine+tools\): Fix memory check on sql task. Should not use co… [\#464](https://github.com/chutney-testing/chutney/pull/464) 


## [1.3.6](https://github.com/chutney-testing/chutney/tree/1.3.6) (2021-06-08)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.5...1.3.6)

**Implemented enhancements:**

- 🚀 | Chutney working directory configuration [\#437](https://github.com/chutney-testing/chutney/issues/437)

**Fixed bugs:**

- 🐛 | Empty campaign execution user [\#454](https://github.com/chutney-testing/chutney/issues/454)
- 🐛 | Empty tag on component scenarios [\#441](https://github.com/chutney-testing/chutney/issues/441)
- 🐛 | Component's validations list keeps adding empty validations [\#439](https://github.com/chutney-testing/chutney/issues/439)

**Merged pull requests:**

- feat\(task\) : Prevent OOM on large SQL queries [\#462](https://github.com/chutney-testing/chutney/pull/462) 
- bugfix\(ui\): fix empty campaign execution user [\#455](https://github.com/chutney-testing/chutney/pull/455) ([rbenyoussef](https://github.com/rbenyoussef))
- chore\(deps\): bump junit5.version from 5.7.1 to 5.7.2 [\#453](https://github.com/chutney-testing/chutney/pull/453) 
- chore\(deps-dev\): bump mockito-core from 2.8.9 to 3.10.0 [\#452](https://github.com/chutney-testing/chutney/pull/452) 
- chore\(deps\): bump exec-maven-plugin from 1.6.0 to 3.0.0 [\#451](https://github.com/chutney-testing/chutney/pull/451) 
- chore\(deps\): bump jacoco-maven-plugin from 0.8.5 to 0.8.7 [\#450](https://github.com/chutney-testing/chutney/pull/450) 
- chore\(deps\): bump cxf-xjc-plugin from 3.3.0 to 3.3.1 [\#448](https://github.com/chutney-testing/chutney/pull/448) 
- chore\(deps\): bump everit-json-schema from 1.11.0 to 1.12.2 [\#444](https://github.com/chutney-testing/chutney/pull/444) 
- chore: Upgrade selenium version to 3.141.59 [\#443](https://github.com/chutney-testing/chutney/pull/443) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(server\): Prevent creating empty tags [\#442](https://github.com/chutney-testing/chutney/pull/442) 
- bugfix\(ui\): Reset component validations list when selecting another one [\#440](https://github.com/chutney-testing/chutney/pull/440) 
- chore\(\): Single chutney working directory configuration [\#436](https://github.com/chutney-testing/chutney/pull/436) 

## [1.3.5](https://github.com/chutney-testing/chutney/tree/1.3.5) (2021-05-25)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.4...1.3.5)

**Merged pull requests:**

- bugfix\(task-impl\): fix NPE on null sql result column [\#438](https://github.com/chutney-testing/chutney/pull/438) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.3.4](https://github.com/chutney-testing/chutney/tree/1.3.4) (2021-05-20)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.3...1.3.4)

**Implemented enhancements:**

- 🚀 | Search campaign more easily [\#355](https://github.com/chutney-testing/chutney/issues/355)

**Fixed bugs:**

- 🐛 | Cannot edit component since 1.3.3 [\#433](https://github.com/chutney-testing/chutney/issues/433)

**Merged pull requests:**

- bugfix\(ui\): Fix component & scenario edition [\#435](https://github.com/chutney-testing/chutney/pull/435) 
- chore: Add maven badge [\#432](https://github.com/chutney-testing/chutney/pull/432) 
- feat\(ui+server\): add tags for campaigns [\#431](https://github.com/chutney-testing/chutney/pull/431) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.3.3](https://github.com/chutney-testing/chutney/tree/1.3.3) (2021-05-19)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.2...1.3.3)

**Implemented enhancements:**

- Enhance read-only scenario with parameters [\#158](https://github.com/chutney-testing/chutney/issues/158)

**Fixed bugs:**

- 🐛 |  Cannot load home page [\#429](https://github.com/chutney-testing/chutney/issues/429)

**Merged pull requests:**

- bugfix\(server\): Fix home page serialization [\#430](https://github.com/chutney-testing/chutney/pull/430) 
- 🚀 feat\(tasks\): Pretty print SQL tasks results for execution reports [\#428](https://github.com/chutney-testing/chutney/pull/428) 
- feat\(server+ui\): Evaluate parameters for reading scenario [\#427](https://github.com/chutney-testing/chutney/pull/427) 

## [1.3.2](https://github.com/chutney-testing/chutney/tree/1.3.2) (2021-05-17)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.1...1.3.2)

**Implemented enhancements:**

- 🚀 | Add navigation between component [\#332](https://github.com/chutney-testing/chutney/issues/332)
- 🚀 | Show line count on matrix dataset \(csv\) [\#211](https://github.com/chutney-testing/chutney/issues/211)
- 🚀 | Display total scenario count on campaign page [\#206](https://github.com/chutney-testing/chutney/issues/206)

**Closed issues:**

- 🚀 | Migrate to maven central [\#328](https://github.com/chutney-testing/chutney/issues/328)

**Merged pull requests:**

- feat\(ui\):display total scenarios count on campaign page [\#426](https://github.com/chutney-testing/chutney/pull/426) ([amalmtt](https://github.com/amalmtt))
- feat\(ui\): Add navigation between components [\#408](https://github.com/chutney-testing/chutney/pull/408) ([rbenyoussef](https://github.com/rbenyoussef))
- Upgrade to GitHub-native Dependabot [\#407](https://github.com/chutney-testing/chutney/pull/407) 
- feat\(ui\): show line count on matrix dataset [\#406](https://github.com/chutney-testing/chutney/pull/406) ([rbenyoussef](https://github.com/rbenyoussef))
- chore: upgrade spring boot version [\#405](https://github.com/chutney-testing/chutney/pull/405) 
- chore: Release to maven central [\#404](https://github.com/chutney-testing/chutney/pull/404) 
- feat\(engine\): Add finally action in report [\#403](https://github.com/chutney-testing/chutney/pull/403) 
- Feat/git export [\#394](https://github.com/chutney-testing/chutney/pull/394) 


## [1.3.1](https://github.com/chutney-testing/chutney/tree/1.3.1) (2021-04-22)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.0...1.3.1)

**Implemented enhancements:**

- 🚀 | Create a smart git export of all Chutney data [\#342](https://github.com/chutney-testing/chutney/issues/342)
- 🚀 | Improve campaign scheduling [\#330](https://github.com/chutney-testing/chutney/issues/330)
- 🚀 | Allow assertions directly in task [\#303](https://github.com/chutney-testing/chutney/issues/303)
- 🚀 | Report does not contain finally actions execution [\#302](https://github.com/chutney-testing/chutney/issues/302)

**Fixed bugs:**

- 🐛 | tastk-impl project in in dependency of engine project [\#400](https://github.com/chutney-testing/chutney/issues/400)
- 🐛 | Http error responses are not print on the UI [\#396](https://github.com/chutney-testing/chutney/issues/396)
- 🐛 | Global Variables resolution is not complete [\#385](https://github.com/chutney-testing/chutney/issues/385)
- Dataset key with tab space could be saved but could not be used anymore [\#346](https://github.com/chutney-testing/chutney/issues/346)

**Closed issues:**

- Remove forcing campaign name in uppercase [\#337](https://github.com/chutney-testing/chutney/issues/337)

**Merged pull requests:**

- feat\(engine\): Add finally action in report [\#403](https://github.com/chutney-testing/chutney/pull/403) 
- Chore/engine dependency fix [\#401](https://github.com/chutney-testing/chutney/pull/401) 
- bugfix\(ui\): Show error message to users [\#395](https://github.com/chutney-testing/chutney/pull/395) 
- Feat/git export [\#394](https://github.com/chutney-testing/chutney/pull/394) 
- feat\(ui\): remove forcing campaigns & scenarios uppercased titles [\#392](https://github.com/chutney-testing/chutney/pull/392) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(server\): Dataset with tab space at extrimity could not be used [\#389](https://github.com/chutney-testing/chutney/pull/389) ([rbenyoussef](https://github.com/rbenyoussef))
- Add test on GwtScenarioMapper [\#388](https://github.com/chutney-testing/chutney/pull/388) 
- Global variables resolution [\#386](https://github.com/chutney-testing/chutney/pull/386) 
- feat: add campaign recurrent Planning per day, week and month [\#381](https://github.com/chutney-testing/chutney/pull/381) ([RedouaeElalami](https://github.com/RedouaeElalami))
- feat\(server/ui/engine\): permit asserts declarations in tasks one [\#373](https://github.com/chutney-testing/chutney/pull/373) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.3.0](https://github.com/chutney-testing/chutney/tree/1.3.0) (2021-03-31)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.20...1.3.0)

**Fixed bugs:**

- 🐛 | Push chutney-junit on main project [\#383](https://github.com/chutney-testing/chutney/issues/383)
- 🐛 | Glacio test timeout [\#367](https://github.com/chutney-testing/chutney/issues/367)

**Merged pull requests:**

- chore\(\): chutney junit in main project [\#384](https://github.com/chutney-testing/chutney/pull/384) 
- chore\(engine+server+task-impl\): Use awaitility + tools class instead of thread sleep [\#382](https://github.com/chutney-testing/chutney/pull/382) 
- Chore/fix test timeout [\#378](https://github.com/chutney-testing/chutney/pull/378) 
- refactor\(server\): Move parameters logic from infra to domain [\#327](https://github.com/chutney-testing/chutney/pull/327) 

## [1.2.20](https://github.com/chutney-testing/chutney/tree/1.2.20) (2021-03-22)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.19...1.2.20)

**Fixed bugs:**

- 🐛 | Unused autowired field [\#379](https://github.com/chutney-testing/chutney/issues/379)
- 🐛 | Maven plugin `cxf-xjc-plugin` generates sources in the wrong folder [\#376](https://github.com/chutney-testing/chutney/issues/376)

**Merged pull requests:**

- bugfix: forget to remove unused autowired field [\#380](https://github.com/chutney-testing/chutney/pull/380) 
- chore: Fix pkg src generation [\#377](https://github.com/chutney-testing/chutney/pull/377) 


## [1.2.19](https://github.com/chutney-testing/chutney/tree/1.2.19) (2021-03-22)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.18...1.2.19)

**Implemented enhancements:**

- 🚀 | SSH Task : Add timeout input [\#369](https://github.com/chutney-testing/chutney/issues/369)

**Fixed bugs:**

- 🐛 | Force tls1.1 for old ldap [\#371](https://github.com/chutney-testing/chutney/issues/371)

**Merged pull requests:**

- bugfix\(server\): Add awaitibility for test and Fix Step unit Test [\#375](https://github.com/chutney-testing/chutney/pull/375) 
- bugfix\(task-impl\): Redo commit f61bf42733271a67c17c266e00df77cce9be32… [\#374](https://github.com/chutney-testing/chutney/pull/374) 
- feat\(server\): Configuration for ldap with only TLS1.1 [\#372](https://github.com/chutney-testing/chutney/pull/372) 
- chore: Setup jdk11 config with nix [\#370](https://github.com/chutney-testing/chutney/pull/370) 


## [1.2.18](https://github.com/chutney-testing/chutney/tree/1.2.18) (2021-03-09)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.16...1.2.18)


**Merged pull requests:**

- chore\(\): Upgrade to jdk11 [\#316](https://github.com/chutney-testing/chutney/pull/316) 

## [1.2.16](https://github.com/chutney-testing/chutney/tree/1.2.16) (2021-03-05)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.15...1.2.16)

**Implemented enhancements:**

- 🚀 | Task http-soap : add http headers parameter [\#364](https://github.com/chutney-testing/chutney/issues/364)

**Fixed bugs:**

- Campaign link on scenario history view does not work with Ctrl+click [\#345](https://github.com/chutney-testing/chutney/issues/345)

**Closed issues:**

- Add templates for issues and PRs [\#347](https://github.com/chutney-testing/chutney/issues/347)

**Merged pull requests:**

- bugfix\(ui\): remove double dash when selecting an execution [\#366](https://github.com/chutney-testing/chutney/pull/366) 
- feat\(task-impl\): allow add headers to http soap task [\#363](https://github.com/chutney-testing/chutney/pull/363) ([rbenyoussef](https://github.com/rbenyoussef))
- chore\(deps\): Bump maven-surefire-plugin from 3.0.0-M4 to 3.0.0-M5 [\#360](https://github.com/chutney-testing/chutney/pull/360) 
- chore\(deps-dev\): Bump activemq.version from 5.16.0 to 5.16.1 [\#359](https://github.com/chutney-testing/chutney/pull/359) 
- chore\(deps\): Bump mongodb.version from 3.8.0 to 3.12.8 [\#358](https://github.com/chutney-testing/chutney/pull/358) 
- chore\(deps\): Bump build-helper-maven-plugin from 3.0.0 to 3.2.0 [\#357](https://github.com/chutney-testing/chutney/pull/357) 
- feat\(ui\): preview scenario error on campaign report [\#356](https://github.com/chutney-testing/chutney/pull/356) ([rbenyoussef](https://github.com/rbenyoussef))
- Added issue and PR template [\#350](https://github.com/chutney-testing/chutney/pull/350) ([96RadhikaJadhav](https://github.com/96RadhikaJadhav))

## [1.2.15](https://github.com/chutney-testing/chutney/tree/1.2.15) (2021-02-26)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.14...1.2.15)

**Implemented enhancements:**

- Add jira feature [\#339](https://github.com/chutney-testing/chutney/issues/339)

**Fixed bugs:**

- Campaign link on scenario history view does not work with Ctrl+click [\#345](https://github.com/chutney-testing/chutney/issues/345)
- Infinite loop on selenium zk By [\#340](https://github.com/chutney-testing/chutney/issues/340)

**Merged pull requests:**

- doc: Update Readme with Kotlin DSL & Discussions [\#353](https://github.com/chutney-testing/chutney/pull/353) 
- bugfix\(task-impl\): infinite loop on Selenium ZK By \(\#340\) [\#351](https://github.com/chutney-testing/chutney/pull/351) ([rbenyoussef](https://github.com/rbenyoussef))
- Feat/add jira feature [\#349](https://github.com/chutney-testing/chutney/pull/349) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(ui\): Missing hash in URLs on scenario exec page [\#348](https://github.com/chutney-testing/chutney/pull/348) 

## [1.2.14](https://github.com/chutney-testing/chutney/tree/1.2.14) (2021-02-16)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.13...1.2.14)

**Implemented enhancements:**

- Importing an environment [\#164](https://github.com/chutney-testing/chutney/issues/164)
- Glacio - Write scenario as feature file [\#144](https://github.com/chutney-testing/chutney/issues/144)

**Fixed bugs:**

- Scenarios executions in campaign report have random order [\#72](https://github.com/chutney-testing/chutney/issues/72)

**Merged pull requests:**

- refacto\(glacio-adapter\): Refacto to expose dto and not domain [\#343](https://github.com/chutney-testing/chutney/pull/343) 
- server: fix dataset iterations bug [\#336](https://github.com/chutney-testing/chutney/pull/336) ([rbenyoussef](https://github.com/rbenyoussef))
- Chore/refacto env [\#325](https://github.com/chutney-testing/chutney/pull/325) 
- chore\(deps\): Bump picocli from 4.2.0 to 4.6.1 [\#319](https://github.com/chutney-testing/chutney/pull/319) 
- chore\(deps\): Bump groovy from 2.5.0 to 2.5.14 [\#315](https://github.com/chutney-testing/chutney/pull/315) 
- chore\(\): Update timeout for old slow computers [\#314](https://github.com/chutney-testing/chutney/pull/314) 
- chore\(\): remove junit4 [\#312](https://github.com/chutney-testing/chutney/pull/312) 
- Chore/release github action [\#311](https://github.com/chutney-testing/chutney/pull/311) 
- chore\(\): githubaction build [\#307](https://github.com/chutney-testing/chutney/pull/307) 
- feat: Use glacio lang [\#94](https://github.com/chutney-testing/chutney/pull/94) 


## [1.2.13](https://github.com/chutney-testing/chutney/tree/1.2.13) (2021-01-14)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.12...1.2.13)

**Fixed bugs:**

- Consume kafka is in error for duplicate header [\#296](https://github.com/chutney-testing/chutney/issues/296)
- Component edition : Wrong component dropped when tag filter selected [\#294](https://github.com/chutney-testing/chutney/issues/294)
- When 2 or more linkyfier in same bloc the labels are concatenate [\#279](https://github.com/chutney-testing/chutney/issues/279)

**Closed issues:**

- Add output in execution report [\#300](https://github.com/chutney-testing/chutney/issues/300)
- Add placeholder on xml assert task [\#299](https://github.com/chutney-testing/chutney/issues/299)
- fix\(ui\): Scenario execution tile stays running [\#292](https://github.com/chutney-testing/chutney/issues/292)

**Merged pull requests:**

- Bugfix/flaky tests [\#304](https://github.com/chutney-testing/chutney/pull/304) 
- feat\(engine+server+ui\): Add output step result in report [\#301](https://github.com/chutney-testing/chutney/pull/301) 
- feat\(task-impl\): Add placeholder on xml assert task [\#298](https://github.com/chutney-testing/chutney/pull/298) 
- bugfix\(task-impl\): consume kafka with duplicated header \(\#296\) [\#297](https://github.com/chutney-testing/chutney/pull/297) ([rbenyoussef](https://github.com/rbenyoussef))
- fix\(ui\): Component edition : Apply tag filter on drag and drop model [\#295](https://github.com/chutney-testing/chutney/pull/295) 
- fix\(ui\): Override completely history executions on refresh [\#293](https://github.com/chutney-testing/chutney/pull/293) 
- chore\(deps\): Bump maven-failsafe-plugin from 3.0.0-M4 to 3.0.0-M5 [\#291](https://github.com/chutney-testing/chutney/pull/291) 
- chore\(deps\): Bump rxjava from 2.2.6 to 2.2.20 [\#290](https://github.com/chutney-testing/chutney/pull/290) 
- chore\(deps-dev\): Bump rabbitmq-mock from 1.0.14 to 1.1.1 [\#288](https://github.com/chutney-testing/chutney/pull/288) 
- Bugfix/dataset iterations [\#286](https://github.com/chutney-testing/chutney/pull/286) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(ui\): Wrong linkifier label [\#281](https://github.com/chutney-testing/chutney/pull/281) 

## [1.2.12](https://github.com/chutney-testing/chutney/tree/1.2.12) (2020-12-23)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.11...1.2.12)

**Merged pull requests:**

- bugfix\(server\): wrong generated iteration [\#285](https://github.com/chutney-testing/chutney/pull/285) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.2.11](https://github.com/chutney-testing/chutney/tree/1.2.11) (2020-12-22)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.10...1.2.11)

**Merged pull requests:**

- bugfix\(server\): indexed output combined to external multivalues dataset for step iteration [\#284](https://github.com/chutney-testing/chutney/pull/284) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.2.10](https://github.com/chutney-testing/chutney/tree/1.2.10) (2020-12-21)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.9...1.2.10)

**Implemented enhancements:**

- Schedule campagne on a specific day [\#266](https://github.com/chutney-testing/chutney/issues/266)

**Fixed bugs:**

- NPE in ComposedTestCaseIterationsPreProcessor.indexInputs [\#280](https://github.com/chutney-testing/chutney/issues/280)
- Not redirect to page requested when disconnected [\#278](https://github.com/chutney-testing/chutney/issues/278)
- Composed testcase : Cannot instantiate context-put task in some campaign executions [\#275](https://github.com/chutney-testing/chutney/issues/275)

**Merged pull requests:**

- bugfix\(server\): NPE in ComposedTestCaseIterationsPreProcessor [\#283](https://github.com/chutney-testing/chutney/pull/283) 
- fix\(ui\): Keep asked url when redirecting to login [\#282](https://github.com/chutney-testing/chutney/pull/282) 
- feat\(ui+server\): Schedule campaign on a specific time \#266 [\#277](https://github.com/chutney-testing/chutney/pull/277) 
- Raw implementation mapper thread safety [\#276](https://github.com/chutney-testing/chutney/pull/276) 

## [1.2.9](https://github.com/chutney-testing/chutney/tree/1.2.9) (2020-12-10)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.8...1.2.9)

**Closed issues:**

- dataset iterations output overided by last iteration output  [\#242](https://github.com/chutney-testing/chutney/issues/242)

**Merged pull requests:**

- feat\(server\): Step iteration [\#261](https://github.com/chutney-testing/chutney/pull/261) 

## [1.2.8](https://github.com/chutney-testing/chutney/tree/1.2.8) (2020-12-09)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.7...1.2.8)

**Fixed bugs:**

- Session expire too fast \(\<10 min\) [\#249](https://github.com/chutney-testing/chutney/issues/249)

**Closed issues:**

- Scheduled campaigns between 00:00 and 00:10 aren't executed [\#264](https://github.com/chutney-testing/chutney/issues/264)
- Add error message when component save is ko [\#250](https://github.com/chutney-testing/chutney/issues/250)

**Merged pull requests:**

- Fix/component implementation mapping [\#268](https://github.com/chutney-testing/chutney/pull/268) 
- fix scheduled campaigns execution at midnight [\#267](https://github.com/chutney-testing/chutney/pull/267) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(task-impl\): Allow bad content type in message received [\#265](https://github.com/chutney-testing/chutney/pull/265) 
- Remove Lucene dependecies and point to travis.com [\#263](https://github.com/chutney-testing/chutney/pull/263) 
- Add component duplication [\#262](https://github.com/chutney-testing/chutney/pull/262) 
- Session management with and without anonymous user [\#260](https://github.com/chutney-testing/chutney/pull/260) 
- Refactor/split composable step repo [\#259](https://github.com/chutney-testing/chutney/pull/259) 

## [1.2.7](https://github.com/chutney-testing/chutney/tree/1.2.7) (2020-11-17)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.6...1.2.7)

**Merged pull requests:**

- feat\(server\): Make number of parrelel thread fixed to 20 [\#258](https://github.com/chutney-testing/chutney/pull/258) 
- feat\(server\): fix gauge update, add status on scenario\_execution\_time… [\#257](https://github.com/chutney-testing/chutney/pull/257) 
- Add edition information for concurrency edition check [\#256](https://github.com/chutney-testing/chutney/pull/256) 
- chore\(deps-dev\): Bump JUnitParams from 1.1.0 to 1.1.1 [\#251](https://github.com/chutney-testing/chutney/pull/251) 

## [1.2.6](https://github.com/chutney-testing/chutney/tree/1.2.6) (2020-10-28)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.5...1.2.6)

**Implemented enhancements:**

- Explicitly show GWT steps in a scenario report [\#210](https://github.com/chutney-testing/chutney/issues/210)
- Scenario steps' Info/Detail are not user friendly nor provide enough value [\#208](https://github.com/chutney-testing/chutney/issues/208)
- Add export environment [\#166](https://github.com/chutney-testing/chutney/issues/166)

**Closed issues:**

- Task Kafka - Add XML's payload processing [\#246](https://github.com/chutney-testing/chutney/issues/246)
- component parameters values erased when executed [\#240](https://github.com/chutney-testing/chutney/issues/240)
- Support navigation patterns [\#239](https://github.com/chutney-testing/chutney/issues/239)

**Merged pull requests:**

- feat: Change micrometer metrics [\#248](https://github.com/chutney-testing/chutney/pull/248) 
- Kafka consume task - Add xml payload processing [\#247](https://github.com/chutney-testing/chutney/pull/247) 
- Feat/navigation patterns [\#244](https://github.com/chutney-testing/chutney/pull/244) 
- Micrometer tasks [\#241](https://github.com/chutney-testing/chutney/pull/241) 
- chore\(ui\): Update node version to v12.18.4 & provide IntelliJ run conf [\#238](https://github.com/chutney-testing/chutney/pull/238) 

## [1.2.5](https://github.com/chutney-testing/chutney/tree/1.2.5) (2020-10-01)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.4...1.2.5)

**Implemented enhancements:**

- Do not break on generated step iterations [\#213](https://github.com/chutney-testing/chutney/issues/213)
- Choose which variable to display with the debug task [\#209](https://github.com/chutney-testing/chutney/issues/209)
- Add audit on execution request [\#116](https://github.com/chutney-testing/chutney/issues/116)

**Closed issues:**

- Strategy retry [\#92](https://github.com/chutney-testing/chutney/issues/92)

**Merged pull requests:**

- Allow ldap and inmemory authentication [\#232](https://github.com/chutney-testing/chutney/pull/232) 
- feat\(ui/server\): Notify campaign execution status to xray/jira [\#231](https://github.com/chutney-testing/chutney/pull/231) 
- Feat/export import environment [\#230](https://github.com/chutney-testing/chutney/pull/230) ([TuLinhNGUYEN](https://github.com/TuLinhNGUYEN))
- fix : Unsecure api for development [\#229](https://github.com/chutney-testing/chutney/pull/229) 
- fix\(engine\): fix nested retry strategy [\#228](https://github.com/chutney-testing/chutney/pull/228) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(server+engine\): do not break on generated step iterations [\#227](https://github.com/chutney-testing/chutney/pull/227) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(task-impl\): create new json asserter \(lessThan and GreaterThan\) [\#226](https://github.com/chutney-testing/chutney/pull/226) ([TuLinhNGUYEN](https://github.com/TuLinhNGUYEN))
- Ordering for inputs/outputs/parameters and fix debug task with filters parameter [\#225](https://github.com/chutney-testing/chutney/pull/225) 
- chore\(deps\): Bump jaxb2-maven-plugin from 2.3.1 to 2.5.0 [\#224](https://github.com/chutney-testing/chutney/pull/224) 
- chore\(deps\): Bump maven-jar-plugin from 3.0.2 to 3.2.0 [\#223](https://github.com/chutney-testing/chutney/pull/223) 
- chore\(deps-dev\): Bump activemq.version from 5.15.11 to 5.16.0 [\#222](https://github.com/chutney-testing/chutney/pull/222) 
- chore\(deps\): Bump postgresql from 42.2.6 to 42.2.16 [\#221](https://github.com/chutney-testing/chutney/pull/221) 
- feat\(task-impl\): Filter scenario context with debug task [\#216](https://github.com/chutney-testing/chutney/pull/216) 
- feat\(server+ui\): Register user on api actions [\#187](https://github.com/chutney-testing/chutney/pull/187) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.2.4](https://github.com/chutney-testing/chutney/tree/1.2.4) (2020-08-18)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.3...1.2.4)

**Merged pull requests:**

- fix\(task-impl\): Use ZoneDateTime for json-assert on dates [\#218](https://github.com/chutney-testing/chutney/pull/218) 
- Feat/improve report ui [\#215](https://github.com/chutney-testing/chutney/pull/215) 

## [1.2.3](https://github.com/chutney-testing/chutney/tree/1.2.3) (2020-08-04)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.2...1.2.3)

**Fixed bugs:**

- Search using Ctrl-F was lost in ace editor component
- Observe automatically a scenario's execution when it does not have parameters

**Closed issues:**

- Scenario Edition - Cannot use Ctrl-F to find content in editor [\#203](https://github.com/chutney-testing/chutney/issues/203)
- Scenario execution - Observation is not automatic for scenarios without parameters [\#204](https://github.com/chutney-testing/chutney/issues/204)

**Merged pull requests:**

- Version 1.2.2 : ui bugs [\#205](https://github.com/chutney-testing/chutney/pull/205) 

## [1.2.2](https://github.com/chutney-testing/chutney/tree/1.2.2) (2020-07-31)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.1...1.2.2)

**Implemented enhancements:**

- Enhance json assert task [\#184](https://github.com/chutney-testing/chutney/issues/184)
- Adding to context log [\#160](https://github.com/chutney-testing/chutney/issues/160)
- Dataset - Execution [\#101](https://github.com/chutney-testing/chutney/issues/101)
- Dataset - Selection of the dataset [\#100](https://github.com/chutney-testing/chutney/issues/100)
- Dataset - Edition [\#99](https://github.com/chutney-testing/chutney/issues/99)
- Dataset - Dataset CRUD [\#98](https://github.com/chutney-testing/chutney/issues/98)
- Dataset - List dataset [\#97](https://github.com/chutney-testing/chutney/issues/97)

**Fixed bugs:**

- Refresh running scenario remove opened/closed chevron [\#191](https://github.com/chutney-testing/chutney/issues/191)
- Stop execution campaign seems not working [\#162](https://github.com/chutney-testing/chutney/issues/162)
- Inconsistency when execution id does not match the scenario /\#/scenario/xxx/execution/yyy [\#161](https://github.com/chutney-testing/chutney/issues/161)
- NPE in contextput [\#156](https://github.com/chutney-testing/chutney/issues/156)
- Maximize screen in json edition [\#150](https://github.com/chutney-testing/chutney/issues/150)

**Closed issues:**

- Allow target without port specified [\#189](https://github.com/chutney-testing/chutney/issues/189)

**Merged pull requests:**

- Fix/189 target whitout port [\#197](https://github.com/chutney-testing/chutney/pull/197) 
- Add option to acknowledge only messages matching selector, true by default [\#196](https://github.com/chutney-testing/chutney/pull/196) ([PKode](https://github.com/PKode))
- Fix/191 : Scenario executions [\#195](https://github.com/chutney-testing/chutney/pull/195) 
- feat/extend\_composable\_testcase\_dataset\_management [\#192](https://github.com/chutney-testing/chutney/pull/192) 
- feat\(task\_impl\): Add placeholder for assert in  JsonTask [\#188](https://github.com/chutney-testing/chutney/pull/188) 
- feat\(ui\): Stop campaign bug.  [\#185](https://github.com/chutney-testing/chutney/pull/185) 
- bugfix\(ui + server\): Raise error when asking for report not corresponding to the scenarioId [\#178](https://github.com/chutney-testing/chutney/pull/178) 
- Bugfix/aceeditor [\#177](https://github.com/chutney-testing/chutney/pull/177) 
- bugfix\(task-impl\): Prevent NPE on null value + pretty log for most usual type [\#176](https://github.com/chutney-testing/chutney/pull/176) 
- chore\(ui\): Update node version to new LTS v12.18.0 [\#172](https://github.com/chutney-testing/chutney/pull/172) 

## [1.2.1](https://github.com/chutney-testing/chutney/tree/1.2.1) (2020-06-16)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.0...1.2.1)

**Fixed bugs:**

- Wrong last execution order on scenarii list  [\#149](https://github.com/chutney-testing/chutney/issues/149)
- bugfix\(ui\): Correctly order scenarios by last execution when one is not executed [\#174](https://github.com/chutney-testing/chutney/pull/174) 
- bugfix\(engine\): Parsing full objects in spel did not work \(introduced in pr134\) [\#173](https://github.com/chutney-testing/chutney/pull/173) 

## [1.2.0](https://github.com/chutney-testing/chutney/tree/1.2.0) (2020-06-12)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.4...1.2.0)

**Implemented enhancements:**

- Campaign report [\#142](https://github.com/chutney-testing/chutney/issues/142)
- Scenarios list : Show total number of scenarios [\#133](https://github.com/chutney-testing/chutney/issues/133)
- Add sort and filter on table [\#111](https://github.com/chutney-testing/chutney/issues/111)
- Remove limitation of running scenario while it's already running [\#90](https://github.com/chutney-testing/chutney/issues/90)
- Keep search/filter during navigation [\#74](https://github.com/chutney-testing/chutney/issues/74)

**Fixed bugs:**

- Replay scenario for stopped campaign [\#118](https://github.com/chutney-testing/chutney/issues/118)
- Issue with SpEL in component step [\#113](https://github.com/chutney-testing/chutney/issues/113)

**Closed issues:**

- Campaign history report - add stop status [\#141](https://github.com/chutney-testing/chutney/issues/141)
- Campaign stopped - See not executed scenarios [\#140](https://github.com/chutney-testing/chutney/issues/140)
- Stop scenario containing component with retry strategy [\#135](https://github.com/chutney-testing/chutney/issues/135)

**Merged pull requests:**

- feat\(ui\): Add current scenario read-only on execution page [\#148](https://github.com/chutney-testing/chutney/pull/148) 
- feat\(engine\): Allow to stop step in a retry strategy [\#146](https://github.com/chutney-testing/chutney/pull/146) 
- Feat/nice campaign history [\#145](https://github.com/chutney-testing/chutney/pull/145) 
- feat\(ui\):Add chart for campaign report [\#143](https://github.com/chutney-testing/chutney/pull/143) 
- refactor\(ui\): fix typo [\#139](https://github.com/chutney-testing/chutney/pull/139) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(ui+core\): Remove restriction to execute a scenario already running [\#138](https://github.com/chutney-testing/chutney/pull/138) 
- feat\(ui\): Sort campaign report scenarios by properties [\#137](https://github.com/chutney-testing/chutney/pull/137) 
- Chore/comm [\#136](https://github.com/chutney-testing/chutney/pull/136) 
- bugfix\(engine\): issue with spel [\#134](https://github.com/chutney-testing/chutney/pull/134) ([rbenyoussef](https://github.com/rbenyoussef))
- chore\(deps\): Bump sshj from 0.26.0 to 0.27.0 [\#130](https://github.com/chutney-testing/chutney/pull/130) 
- chore\(deps\): Bump jool from 0.9.12 to 0.9.14 [\#129](https://github.com/chutney-testing/chutney/pull/129) 
- chore\(deps\): Bump wiremock-standalone from 2.19.0 to 2.26.3 [\#128](https://github.com/chutney-testing/chutney/pull/128) 
- chore\(deps\): Bump orientdb.version from 3.0.28 to 3.0.30 [\#127](https://github.com/chutney-testing/chutney/pull/127) 
- chore\(deps\): Bump maven-source-plugin from 3.2.0 to 3.2.1 [\#126](https://github.com/chutney-testing/chutney/pull/126) 
- bugfix\(ui\): show replay button for stopped campaign [\#125](https://github.com/chutney-testing/chutney/pull/125) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(ui\): Bookmark scenarios search filters [\#124](https://github.com/chutney-testing/chutney/pull/124) 
- refactor\(core/engine\): Fix typo [\#123](https://github.com/chutney-testing/chutney/pull/123) 
- chore\(\): Add generate changelog in Contributing.md [\#122](https://github.com/chutney-testing/chutney/pull/122) 


## [1.1.4](https://github.com/chutney-testing/chutney/tree/1.1.4) (2020-04-30)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.3...1.1.4)

**Implemented enhancements:**

- Add environment name to context and global variable [\#19](https://github.com/chutney-testing/chutney/issues/19)

**Merged pull requests:**

- bugfix: Task using isPresent [\#121](https://github.com/chutney-testing/chutney/pull/121) 
- Feat/add environment name to context and global variables [\#120](https://github.com/chutney-testing/chutney/pull/120) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.1.3](https://github.com/chutney-testing/chutney/tree/1.1.3) (2020-04-24)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.2...1.1.3)

**Implemented enhancements:**

- Add number of scenario run on campaign report [\#96](https://github.com/chutney-testing/chutney/issues/96)

**Fixed bugs:**

- Component/Scenario edition : Cannot update step parameter value after precedent step deletion [\#107](https://github.com/chutney-testing/chutney/issues/107)
- Campaign run duration on parallels run [\#86](https://github.com/chutney-testing/chutney/issues/86)
- Execution environment for scenario not displayed for direct access [\#85](https://github.com/chutney-testing/chutney/issues/85)
- Need more labels in campaign report [\#73](https://github.com/chutney-testing/chutney/issues/73)
- No alert when closing component screen [\#70](https://github.com/chutney-testing/chutney/issues/70)
- Target are not sorted in chrome [\#22](https://github.com/chutney-testing/chutney/issues/22)
- Scenario description are truncated [\#21](https://github.com/chutney-testing/chutney/issues/21)

**Closed issues:**

- Add an alert when removing global var [\#91](https://github.com/chutney-testing/chutney/issues/91)

**Merged pull requests:**

- Feat/ui improve campaign ux [\#115](https://github.com/chutney-testing/chutney/pull/115) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(ui\): Show number of passed/failed scenarios on campaign report [\#114](https://github.com/chutney-testing/chutney/pull/114) 
- Bugfix/alert on globalvar deletion [\#109](https://github.com/chutney-testing/chutney/pull/109) 
- fix\(ui\): Component edition - Must recreate steps parameter values [\#108](https://github.com/chutney-testing/chutney/pull/108) 
- Feat/ui improve scenario execution report [\#105](https://github.com/chutney-testing/chutney/pull/105) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(ui\): Sort target and environment names [\#104](https://github.com/chutney-testing/chutney/pull/104) 
- bugfix\(ui\): Show scenario description without truncation [\#103](https://github.com/chutney-testing/chutney/pull/103) 
- Feat/ui display improvement [\#102](https://github.com/chutney-testing/chutney/pull/102) ([rbenyoussef](https://github.com/rbenyoussef))
- Fix/86 [\#93](https://github.com/chutney-testing/chutney/pull/93) 
- Tech/clean engine target model [\#89](https://github.com/chutney-testing/chutney/pull/89) 
- Fix \#85 [\#88](https://github.com/chutney-testing/chutney/pull/88) 
- bugfix\(ui\): Add alert when leaving component scenario edition with mo… [\#87](https://github.com/chutney-testing/chutney/pull/87) 
- Tech/reduce cli coupling [\#84](https://github.com/chutney-testing/chutney/pull/84) 
- feat\(ui\): Add stop label for campaign execution report summarize [\#78](https://github.com/chutney-testing/chutney/pull/78) 
- chore\(ui\): Add a test on scenarii.component.spec.ts [\#77](https://github.com/chutney-testing/chutney/pull/77) 
- chore\(ui\): Add some ng test [\#76](https://github.com/chutney-testing/chutney/pull/76) 

## [1.1.2](https://github.com/chutney-testing/chutney/tree/1.1.2) (2020-03-19)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.1...1.1.2)

**Implemented enhancements:**

- Add a new task to consume message from a kafka topic [\#38](https://github.com/chutney-testing/chutney/issues/38)
- Display environment execution for scenario [\#20](https://github.com/chutney-testing/chutney/issues/20)

**Fixed bugs:**

- Trim all tags [\#59](https://github.com/chutney-testing/chutney/issues/59)

**Closed issues:**

- Wrong redirection when cancelling campaign edition [\#58](https://github.com/chutney-testing/chutney/issues/58)

**Merged pull requests:**

- Feature/add kafka basic consume task [\#68](https://github.com/chutney-testing/chutney/pull/68) 
- feat\(core + ui\): Add environment info to scenario execution history [\#66](https://github.com/chutney-testing/chutney/pull/66) 
- Avoid NullPointerException with message having null value as header [\#65](https://github.com/chutney-testing/chutney/pull/65) ([GeVa2072](https://github.com/GeVa2072))
- feat\(ui\): In campaign edition, cancel redirect to edited campaign [\#64](https://github.com/chutney-testing/chutney/pull/64) 
- bugfix\(server\): uppercase and trim tags at saved [\#62](https://github.com/chutney-testing/chutney/pull/62) 

## [1.1.1](https://github.com/chutney-testing/chutney/tree/1.1.1) (2020-03-10)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.0...1.1.1)

**Merged pull requests:**

- bugfix\(ui\): NPE on saveComponent + Add more ui coherence between comp… [\#61](https://github.com/chutney-testing/chutney/pull/61) 
- chore: Share Intellij run config [\#57](https://github.com/chutney-testing/chutney/pull/57) 

## [1.1.0](https://github.com/chutney-testing/chutney/tree/1.1.0) (2020-03-06)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.0.0...1.1.0)

**Implemented enhancements:**

- Font size smaller on campaign page  [\#18](https://github.com/chutney-testing/chutney/issues/18)

**Fixed bugs:**

- Navigation between component sometime broken [\#24](https://github.com/chutney-testing/chutney/issues/24)
- Pop up should not appear on scenario execution page [\#16](https://github.com/chutney-testing/chutney/issues/16)
- Id not reload when creating component [\#15](https://github.com/chutney-testing/chutney/issues/15)

**Merged pull requests:**

- feat\(core\): Parameterized component strategies [\#60](https://github.com/chutney-testing/chutney/pull/60) 
- Fix/parameters escaping [\#56](https://github.com/chutney-testing/chutney/pull/56) 
- bugfix\(ui\): Fix broken redirection on child component [\#55](https://github.com/chutney-testing/chutney/pull/55) 
- bugfix\(ui\): Remove canDeactivate guard on execution history page [\#54](https://github.com/chutney-testing/chutney/pull/54) 
- Tech/clean up [\#53](https://github.com/chutney-testing/chutney/pull/53) 
- feat\(ui\): Reduce font size on campaign page [\#52](https://github.com/chutney-testing/chutney/pull/52) 
- Chore/travis zulip hook [\#50](https://github.com/chutney-testing/chutney/pull/50) 
- chore\(deps\): Bump picocli from 3.9.0 to 4.2.0 [\#49](https://github.com/chutney-testing/chutney/pull/49) 
- chore\(deps-dev\): Bump rabbitmq-mock from 1.0.4 to 1.0.14 [\#48](https://github.com/chutney-testing/chutney/pull/48) 
- chore\(deps\): Bump build-helper-maven-plugin from 1.12 to 3.0.0 [\#47](https://github.com/chutney-testing/chutney/pull/47) 
- chore\(deps\): Bump maven-clean-plugin from 3.0.0 to 3.1.0 [\#46](https://github.com/chutney-testing/chutney/pull/46) 
- chore\(deps-dev\): Bump activemq.version from 5.15.0 to 5.15.11 [\#45](https://github.com/chutney-testing/chutney/pull/45) 
- doc: Update release management [\#43](https://github.com/chutney-testing/chutney/pull/43) 
- chore: Update project info [\#41](https://github.com/chutney-testing/chutney/pull/41) 
- fix\(core+ui\): Set id on newly created component. [\#37](https://github.com/chutney-testing/chutney/pull/37) 

## [1.0.0](https://github.com/chutney-testing/chutney/tree/1.0.0) (2020-02-26)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/d734d7933351ed031c3c598b0a2de7098153b42f...1.0.0)

**Fixed bugs:**

- Global var character escaped twice [\#9](https://github.com/chutney-testing/chutney/issues/9)

**Closed issues:**

- Add a new task to consume message from a kafka topic [\#39](https://github.com/chutney-testing/chutney/issues/39)

**Merged pull requests:**

- chore\(ui\): Generate sources archive [\#40](https://github.com/chutney-testing/chutney/pull/40) 
- Bugfix/escaped char [\#36](https://github.com/chutney-testing/chutney/pull/36) 
- Revert "Ignore IntelliJ root configuration files" [\#35](https://github.com/chutney-testing/chutney/pull/35) 
- chore: Licence compliance with Fossa [\#34](https://github.com/chutney-testing/chutney/pull/34) 
- Add randomLong Generator [\#33](https://github.com/chutney-testing/chutney/pull/33) ([GeVa2072](https://github.com/GeVa2072))
- Fix use of == instead of equals method [\#32](https://github.com/chutney-testing/chutney/pull/32) ([GeVa2072](https://github.com/GeVa2072))
- chore: Add Travis deployment. Fix project version. [\#14](https://github.com/chutney-testing/chutney/pull/14) 
- Fix typo in Selenium documentation snippet [\#13](https://github.com/chutney-testing/chutney/pull/13) ([ledoyen](https://github.com/ledoyen))
- Fix Finally Action target mapping error [\#12](https://github.com/chutney-testing/chutney/pull/12) ([ledoyen](https://github.com/ledoyen))
- Allow use of external npm installation [\#11](https://github.com/chutney-testing/chutney/pull/11) ([ledoyen](https://github.com/ledoyen))
- Ignore IntelliJ root configuration files [\#10](https://github.com/chutney-testing/chutney/pull/10) ([ledoyen](https://github.com/ledoyen))
- chore: Add to Travis build JDK version and install command [\#4](https://github.com/chutney-testing/chutney/pull/4) 
- Bump npm from 5.8.0 to 6.13.4 in /ui [\#2](https://github.com/chutney-testing/chutney/pull/2) 
- Bump amqp-client from 5.3.0 to 5.8.0 [\#1](https://github.com/chutney-testing/chutney/pull/1) 

# Before open source

## 2019-09-10: v2.0.0-34

* feature(ui): Use a context variable as task input for List or Map
* feature(core/ui): Override scenarios parameters from campaign level
* feature(task): Add selector to basic consume amqp task
* feature(task): Get web element attribute
* feature(task): Resize browser
    
* refactor(ui): Project structure

* bugfix: Fix broken component deletion
    
## 2019-09-10: v2.0.0-34

* feature: Migrate from H2 to PostgreSQL
* feature(ui): Manage campaign scenario execution order
* feature(ui): Scenario raw edition form default to HJSON

* feature(core): Support conversion of scenarios using multiple when steps
* feature(core/ui): Copy scenario
* feature(core/ui): Add tags on composite scenario
* feature(core/ui): Prevent deletion of a component block used somewhere else
* feature(core/ui): Improve execution parameters for composite scenarios

* bugfix(core): Truncate too long error message before saving to DB
* bugfix(task): Send an empty body on http post

* test: Add Mutation Testing

## 2019-06-06: v2.0.0

* removed: Automatic step library is removed in favor of component block creation

* feature(ui): Show execution count on scenario list
* feature(ui): Do not show default step strategy on scenario form edition
* feature(task): Scroll web elements

* feature(core/ui): Scenario creation using component blocks
* feature(core/ui): Avoid creating a cyclic component
* feature(core/ui): Execution on component only for repl-like purpose 
* feature(core/ui): Filter scenarios by type (normal / composite)
* feature(core/ui): Filter scenarios without tag
* feature(core/ui): Improve campaign creation & edition
* feature(core/ui): Improve campaign list
* feature(core/ui): Improve campaign execution
* feature(core/ui): Prevent saving a scenario with format errors
* feature(core/ui): Show "Target not found" error on scenario execution


* refactor(core): Improve logging on scheduled campaign execution
* refactor(core/domain): Remove scenario "blob"
* refactor(core/domain): Scenarios by use case
* refactor(core/api): Improve some error messages
* refactor(core/api): Old scenario format retro-compatibility
* refactor(ui): Project structure

* bugfix:(core): Fix on adding a target preventing scenario execution

* doc: Correct an error

## 2019-04-04: v1.14.1-146

* feature(ui): Confirmation box on closing unsaved scenario
* feature(ui): Confirmation box on campaign deletion 
* feature(ui): Keep scenarios list filter preference
* feature(ui): View scenarios by list or by card
* feature(ui): Start/Stop/Pause/Resume a scenario execution
* feature(core/ui): Schedule daily campaign execution
* feature(core): Scenario deletion is now logical only
* feature(core): List all tasks and their parameters
* feature(task): Selenium clear()

* refactor(ui): Remove pop-up on campaign edition
* refactor(ui): Remove pop-up on scenario edition
* refactor(core/ui): Reduce campaign loading time

* security: LDAP authentication

## 2019-03-05: v1.14.1-104

* feature(ui): Follow each step execution in real time
* feature(task): Enable hovering an element then clicking on another
* feature(task): Selenium tests should run on internet explorer
* feature(task): SeleniumSwitchToTask should handle Ok/Cancel alert box
* feature(task): Validate JSON content against JSON schema
* feature(task): Assert missing JSON field 

* bugfix: Fix routing after creation forms

* security(core): SSL

## 2019-02-14: v1.14.1-86

* feature(task): Add AMQP task
* feature(core/plugin): Run a scenario from Intellij
* feature(core): Start/Stop/Pause/Resume a scenario execution
* feature(core): Notify scenario execution events
* feature(core/ui): Enable/Disable documentation examples

* refactor: Remove SQL datasource cache

* bugfix: Campaign XML reports

## 2019-01-14: v1.14.1-68

* feature: Enable switching to popups in order to perform Selenium actions
* feature(ux): Scenario edition
* feature(ux): Step library
* feature(task): Add keystore for https server task
* feature: CLI for running scenario execution

* refactor: Création d'un plugin Intellij pour l'execution et l'écriture (autocomplétion, ...) de scénario
* refactor: Start execution engine without core module

* bugfix: Edition page performance 
* bugfix: Avoid cyclic step creation
* bugfix: Adding empty dataset
* bugfix(core/ui): Out of Memory on campaign loading
* bugfix(task): Close SQL session
* bugfix(task): Fix selenium getText
* bugfix(ui): Scrollbar problem on technical steps edition

* doc: Document all tasks

## 2018-12-13: v1.14.1-44

* feature(task): Add https server start/stop & https listener
* feature: Add showroom page 

* refactor: Decouple core and engine

* bugfix: Fix resource leak

* doc: Add scenario examples

## 2018-11-28: v1.14.1-23

* feature(core/ui): Add scenario parameters

* refactor: Scenario database backup

* bugfix(ui): Scenario form edition (misc)

* doc: Ajout d'un menu d'administration pour la documentation (Exemples à venir)

## 2018-11-12: v1.14.1-10

* removed: Remove step library pop-up on scenario form edition

* feature(task): Add task for validating XML against an XSD
* feature(ui): Redesign scenario form edition UX/UI

* bugfix(ui): Deleting step description does not remove sub-step 

## 2018-11-07: v1.14.1-7

* removed: Remove scenario parameters management

* feature(ui): Provide scenario edition with a web form (without step strategies)
* feature(ui): Show syntax and content errors when editing a scenario
* feature(task): AMQP Clean Queue task accepts a list of files
* feature(core): Manage git scenarios repository

* refactor(ui): Update scenario execution view
* refactor(core/engine): Improve error management

* bugfix(ui): Tags and scenarios card list

## 2018-10-28: v1.13.1-193

* refactor(core/engine/ui): Show complete exception stack
* refactor(task): JMS task provides complete exception stack

## 2018-10-28: v1.13.1-191

* feature(ui): Group admin features under one menu entry
* feature(ui): Provide read only access for unauthenticated users
* feature(func): XPATH function makes use of document namespaces
* feature(task): XML assert task makes use of document namespaces

* refactor(task): Improve content of Selenium tasks information

* bugfix(engine): Each task input/output is evaluated sequentially and evaluation context is updated accordingly after each
* bugfix(core/ui): View saved execution reports after scenario changes

* chore: Add Weblogic 10.3 client dependency
    
## 2018-10-18: v1.13.1-179

* feature(ui): Add new way to create and edit a scenario
* feature(ui): Update global template
* feature(ui): Update scenarios list view
* feature(core/ui): Keep all execution reports and show 20 last reports
* feature(task): Add Selenium tasks

* refactor(core/engine): Decouple Core and Engine
* refactor(engine): Delete optional attributes on Step and StepDefinition

## 2018-10-02: v1.13.1-162

* feature(task): Kafka Basic Publish
* feature(task): Add timeout on http tasks
* feature(task): Json assert compare Number vs String as numbers

* refactor(perf): Cache local agent when compiling scenario
* refactor(test): Move all cucumber scenarios input to ui api

* bugfix: Fix ssh authentication with private key
* bugfix: Fix optional parameters which were mandatory

## 2018-09-20: v1.13.1-153

* feature(ui): Disable edition of scenario saved outside of the local repository
* feature(ui): Scroll to top on target change
* feature(ui): Activate search in editor

* bugfix(core): Removing a scenario deletes all its references in database
* bugfix(core): Save a scenario even if saving a referenced step fails
* bugfix(core): Fix entity mapper test for Windows
* bugfix(task): HTTP headers cannot be injected
* bugfix(task): JSON assert compare number
* bugfix(task): String or JSON in http body
