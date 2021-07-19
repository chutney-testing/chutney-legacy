# Changelog

## [1.3.11](https://github.com/chutney-testing/chutney/tree/1.3.11) (2021-07-19)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.10...HEAD)

**Fixed bugs:**

- 🐛 | Links to JIRA are not working [\#508](https://github.com/chutney-testing/chutney/issues/508)

**Merged pull requests:**

- fix\(server+ui\): JIRA configuration url endpoint produces plain text [\#509](https://github.com/chutney-testing/chutney/pull/509) ([boddissattva](https://github.com/boddissattva))


## [1.3.10](https://github.com/chutney-testing/chutney/tree/1.3.10) (2021-07-13)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.9...HEAD)

**Fixed bugs:**

- 🐛 | LDAP authentification problem - Authorities have role prefix ROLE\_ [\#506](https://github.com/chutney-testing/chutney/issues/506)
- 🐛 | Execute button of scenario does not work [\#505](https://github.com/chutney-testing/chutney/issues/505)

**Merged pull requests:**

- fix/scenario execute UI [\#507](https://github.com/chutney-testing/chutney/pull/507) ([boddissattva](https://github.com/boddissattva))


## [1.3.9](https://github.com/chutney-testing/chutney/tree/1.3.9) (2021-07-12)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.8...HEAD)

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

- fix\(egine\): Validations only set FAILURE step status [\#504](https://github.com/chutney-testing/chutney/pull/504) ([boddissattva](https://github.com/boddissattva))
- fix\(ui\): Map deselect all event on angular-multiselect [\#497](https://github.com/chutney-testing/chutney/pull/497) ([boddissattva](https://github.com/boddissattva))
- fix\(junit\): Use class filter in junit engine [\#496](https://github.com/chutney-testing/chutney/pull/496) ([boddissattva](https://github.com/boddissattva))
- chore\(ui+server\): Load independently testcase header and content [\#493](https://github.com/chutney-testing/chutney/pull/493) ([boddissattva](https://github.com/boddissattva))
- chore\(deps-dev\): bump assertj-core from 3.19.0 to 3.20.2 [\#491](https://github.com/chutney-testing/chutney/pull/491) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps\): bump sl4j.api.version from 1.7.30 to 1.7.31 [\#490](https://github.com/chutney-testing/chutney/pull/490) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps-dev\): bump mockito.version from 3.10.0 to 3.11.2 [\#489](https://github.com/chutney-testing/chutney/pull/489) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps\): bump commons-io from 2.8.0 to 2.10.0 [\#487](https://github.com/chutney-testing/chutney/pull/487) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps\): bump maven-javadoc-plugin from 3.2.0 to 3.3.0 [\#485](https://github.com/chutney-testing/chutney/pull/485) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps-dev\): bump otj-pg-embedded from 0.13.1 to 0.13.4 [\#484](https://github.com/chutney-testing/chutney/pull/484) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps\): bump qpid-broker.version from 7.0.6 to 8.0.5 [\#483](https://github.com/chutney-testing/chutney/pull/483) ([dependabot[bot]](https://github.com/apps/dependabot))
- Feat/authorizations [\#477](https://github.com/chutney-testing/chutney/pull/477) ([boddissattva](https://github.com/boddissattva))

## [1.3.8](https://github.com/chutney-testing/chutney/tree/1.3.8) (2021-06-23)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.7...HEAD)

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
- 🚀 | Missing dataset data in git export [\#461](https://github.com/chutney-testing/chutney/issues/461)
- 🐛 | Environment backup [\#457](https://github.com/chutney-testing/chutney/issues/457)
- 🐛 | Navigation to scenario components is broken [\#434](https://github.com/chutney-testing/chutney/issues/434)

**Merged pull requests:**

- fix\(task\): SQL result with duplicated column name [\#479](https://github.com/chutney-testing/chutney/pull/479) ([bessonm](https://github.com/bessonm))
- bugfix\(engine\): Add pause as report status for running scenario [\#475](https://github.com/chutney-testing/chutney/pull/475) ([nbrouand](https://github.com/nbrouand))
- fix\(server\): Backup all environments [\#474](https://github.com/chutney-testing/chutney/pull/474) ([bessonm](https://github.com/bessonm))
- fix\(server\): Includes datatable & constants on exporting dataset to git [\#473](https://github.com/chutney-testing/chutney/pull/473) ([bessonm](https://github.com/bessonm))
- feat\(ui+server\): Backup jira links [\#472](https://github.com/chutney-testing/chutney/pull/472) ([bessonm](https://github.com/bessonm))
- Bugfix/sql task change [\#471](https://github.com/chutney-testing/chutney/pull/471) ([nbrouand](https://github.com/nbrouand))
- fix\(task-impl\): Keep numeric and date JDBC SQL Types in task result [\#468](https://github.com/chutney-testing/chutney/pull/468) ([boddissattva](https://github.com/boddissattva))
- Chore/taskexecutor [\#467](https://github.com/chutney-testing/chutney/pull/467) ([nbrouand](https://github.com/nbrouand))
- feat\(ui+server\): Add full text scenario search [\#465](https://github.com/chutney-testing/chutney/pull/465) ([nbrouand](https://github.com/nbrouand))


## [1.3.7](https://github.com/chutney-testing/chutney/tree/1.3.7) (2021-06-09)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.6...HEAD)

**Fixed bugs:**

- 🐛 | When setting Xms on Chutney, SQL task always raise NotEnoughMemoryException [\#463](https://github.com/chutney-testing/chutney/issues/463)

**Merged pull requests:**

- bugfix\(engine+tools\): Fix memory check on sql task. Should not use co… [\#464](https://github.com/chutney-testing/chutney/pull/464) ([nbrouand](https://github.com/nbrouand))


## [1.3.6](https://github.com/chutney-testing/chutney/tree/1.3.6) (2021-06-08)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.5...1.3.6)

**Implemented enhancements:**

- 🚀 | Chutney working directory configuration [\#437](https://github.com/chutney-testing/chutney/issues/437)

**Fixed bugs:**

- 🐛 | Empty campaign execution user [\#454](https://github.com/chutney-testing/chutney/issues/454)
- 🐛 | Empty tag on component scenarios [\#441](https://github.com/chutney-testing/chutney/issues/441)
- 🐛 | Component's validations list keeps adding empty validations [\#439](https://github.com/chutney-testing/chutney/issues/439)

**Merged pull requests:**

- feat\(task\) : Prevent OOM on large SQL queries [\#462](https://github.com/chutney-testing/chutney/pull/462) ([bessonm](https://github.com/bessonm))
- bugfix\(ui\): fix empty campaign execution user [\#455](https://github.com/chutney-testing/chutney/pull/455) ([rbenyoussef](https://github.com/rbenyoussef))
- chore\(deps\): bump junit5.version from 5.7.1 to 5.7.2 [\#453](https://github.com/chutney-testing/chutney/pull/453) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps-dev\): bump mockito-core from 2.8.9 to 3.10.0 [\#452](https://github.com/chutney-testing/chutney/pull/452) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps\): bump exec-maven-plugin from 1.6.0 to 3.0.0 [\#451](https://github.com/chutney-testing/chutney/pull/451) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps\): bump jacoco-maven-plugin from 0.8.5 to 0.8.7 [\#450](https://github.com/chutney-testing/chutney/pull/450) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps\): bump cxf-xjc-plugin from 3.3.0 to 3.3.1 [\#448](https://github.com/chutney-testing/chutney/pull/448) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(deps\): bump everit-json-schema from 1.11.0 to 1.12.2 [\#444](https://github.com/chutney-testing/chutney/pull/444) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore: Upgrade selenium version to 3.141.59 [\#443](https://github.com/chutney-testing/chutney/pull/443) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(server\): Prevent creating empty tags [\#442](https://github.com/chutney-testing/chutney/pull/442) ([bessonm](https://github.com/bessonm))
- bugfix\(ui\): Reset component validations list when selecting another one [\#440](https://github.com/chutney-testing/chutney/pull/440) ([bessonm](https://github.com/bessonm))
- chore\(\): Single chutney working directory configuration [\#436](https://github.com/chutney-testing/chutney/pull/436) ([boddissattva](https://github.com/boddissattva))

## [1.3.5](https://github.com/chutney-testing/chutney/tree/1.3.5) (2021-05-25)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.4...1.3.5)

**Merged pull requests:**

- bugfix\(task-impl\): fix NPE on null sql result column [\#438](https://github.com/chutney-testing/chutney/pull/438) ([rbenyoussef](https://github.com/rbenyoussef))


## [1.3.4](https://github.com/chutney-testing/chutney/tree/1.3.4)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.3...1.3.4)

**Implemented enhancements:**

- 🚀 | Search campaign more easily [\#355](https://github.com/chutney-testing/chutney/issues/355)

**Fixed bugs:**

- 🐛 | Cannot edit component since 1.3.3 [\#433](https://github.com/chutney-testing/chutney/issues/433)

**Merged pull requests:**

- bugfix\(ui\): Fix component & scenario edition [\#435](https://github.com/chutney-testing/chutney/pull/435) ([bessonm](https://github.com/bessonm))
- chore: Add maven badge [\#432](https://github.com/chutney-testing/chutney/pull/432) ([nbrouand](https://github.com/nbrouand))
- feat\(ui+server\): add tags for campaigns [\#431](https://github.com/chutney-testing/chutney/pull/431) ([rbenyoussef](https://github.com/rbenyoussef))


## [1.3.3](https://github.com/chutney-testing/chutney/tree/1.3.3)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.3.2...1.3.3)

**Implemented enhancements:**

- Enhance read-only scenario with parameters [\#158](https://github.com/chutney-testing/chutney/issues/158)

**Fixed bugs:**

- 🐛 |  Cannot load home page [\#429](https://github.com/chutney-testing/chutney/issues/429)

**Merged pull requests:**

- bugfix\(server\): Fix home page serialization [\#430](https://github.com/chutney-testing/chutney/pull/430) ([nbrouand](https://github.com/nbrouand))
- 🚀 feat\(tasks\): Pretty print SQL tasks results for execution reports [\#428](https://github.com/chutney-testing/chutney/pull/428) ([bessonm](https://github.com/bessonm))
- feat\(server+ui\): Evaluate parameters for reading scenario [\#427](https://github.com/chutney-testing/chutney/pull/427) ([nbrouand](https://github.com/nbrouand))


## [1.3.2](https://github.com/chutney-testing/chutney/tree/1.3.2)

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
- Upgrade to GitHub-native Dependabot [\#407](https://github.com/chutney-testing/chutney/pull/407) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- feat\(ui\): show line count on matrix dataset [\#406](https://github.com/chutney-testing/chutney/pull/406) ([rbenyoussef](https://github.com/rbenyoussef))
- chore: upgrade spring boot version [\#405](https://github.com/chutney-testing/chutney/pull/405) ([nbrouand](https://github.com/nbrouand))
- chore: Release to maven central [\#404](https://github.com/chutney-testing/chutney/pull/404) ([bessonm](https://github.com/bessonm))
- feat\(engine\): Add finally action in report [\#403](https://github.com/chutney-testing/chutney/pull/403) ([nbrouand](https://github.com/nbrouand))
- Feat/git export [\#394](https://github.com/chutney-testing/chutney/pull/394) ([bessonm](https://github.com/bessonm))


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

- feat\(engine\): Add finally action in report [\#403](https://github.com/chutney-testing/chutney/pull/403) ([nbrouand](https://github.com/nbrouand))
- Chore/engine dependency fix [\#401](https://github.com/chutney-testing/chutney/pull/401) ([nbrouand](https://github.com/nbrouand))
- bugfix\(ui\): Show error message to users [\#395](https://github.com/chutney-testing/chutney/pull/395) ([bessonm](https://github.com/bessonm))
- Feat/git export [\#394](https://github.com/chutney-testing/chutney/pull/394) ([bessonm](https://github.com/bessonm))
- feat\(ui\): remove forcing campaigns & scenarios uppercased titles [\#392](https://github.com/chutney-testing/chutney/pull/392) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(server\): Dataset with tab space at extrimity could not be used [\#389](https://github.com/chutney-testing/chutney/pull/389) ([rbenyoussef](https://github.com/rbenyoussef))
- Add test on GwtScenarioMapper [\#388](https://github.com/chutney-testing/chutney/pull/388) ([nbrouand](https://github.com/nbrouand))
- Global variables resolution [\#386](https://github.com/chutney-testing/chutney/pull/386) ([boddissattva](https://github.com/boddissattva))
- feat: add campaign recurrent Planning per day, week and month [\#381](https://github.com/chutney-testing/chutney/pull/381) ([RedouaeElalami](https://github.com/RedouaeElalami))
- feat\(server/ui/engine\): permit asserts declarations in tasks one [\#373](https://github.com/chutney-testing/chutney/pull/373) ([rbenyoussef](https://github.com/rbenyoussef))


## [1.3.0](https://github.com/chutney-testing/chutney/tree/1.3.0)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.20...1.3.0)

**Fixed bugs:**

- 🐛 | Push chutney-junit on main project [\#383](https://github.com/chutney-testing/chutney/issues/383)
- 🐛 | Glacio test timeout [\#367](https://github.com/chutney-testing/chutney/issues/367)

**Merged pull requests:**

- chore\(\): chutney junit in main project [\#384](https://github.com/chutney-testing/chutney/pull/384) ([nbrouand](https://github.com/nbrouand))
- chore\(engine+server+task-impl\): Use awaitility + tools class instead of thread sleep [\#382](https://github.com/chutney-testing/chutney/pull/382) ([nbrouand](https://github.com/nbrouand))
- Chore/fix test timeout [\#378](https://github.com/chutney-testing/chutney/pull/378) ([boddissattva](https://github.com/boddissattva))
- refactor\(server\): Move parameters logic from infra to domain [\#327](https://github.com/chutney-testing/chutney/pull/327) ([bessonm](https://github.com/bessonm))


## [1.2.20](https://github.com/chutney-testing/chutney/tree/1.2.20)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.19...1.2.20)

**Fixed bugs:**

- 🐛 | Unused autowired field [\#379](https://github.com/chutney-testing/chutney/issues/379)
- 🐛 | Maven plugin `cxf-xjc-plugin` generates sources in the wrong folder [\#376](https://github.com/chutney-testing/chutney/issues/376)

**Merged pull requests:**

- bugfix: forget to remove unused autowired field [\#380](https://github.com/chutney-testing/chutney/pull/380) ([nbrouand](https://github.com/nbrouand))
- chore: Fix pkg src generation [\#377](https://github.com/chutney-testing/chutney/pull/377) ([bessonm](https://github.com/bessonm))


## [1.2.19](https://github.com/chutney-testing/chutney/tree/1.2.19) (2021-03-22)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.18...1.2.19)

**Implemented enhancements:**

- 🚀 | SSH Task : Add timeout input [\#369](https://github.com/chutney-testing/chutney/issues/369)

**Fixed bugs:**

- 🐛 | Force tls1.1 for old ldap [\#371](https://github.com/chutney-testing/chutney/issues/371)

**Merged pull requests:**

- bugfix\(server\): Add awaitibility for test and Fix Step unit Test [\#375](https://github.com/chutney-testing/chutney/pull/375) ([nbrouand](https://github.com/nbrouand))
- bugfix\(task-impl\): Redo commit f61bf42733271a67c17c266e00df77cce9be32… [\#374](https://github.com/chutney-testing/chutney/pull/374) ([nbrouand](https://github.com/nbrouand))
- feat\(server\): Configuration for ldap with only TLS1.1 [\#372](https://github.com/chutney-testing/chutney/pull/372) ([nbrouand](https://github.com/nbrouand))
- chore: Setup jdk11 config with nix [\#370](https://github.com/chutney-testing/chutney/pull/370) ([bessonm](https://github.com/bessonm))


## [1.2.18](https://github.com/chutney-testing/chutney/tree/1.2.18) (2021-03-09)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.16...1.2.18)


**Merged pull requests:**

- chore\(\): Upgrade to jdk11 [\#316](https://github.com/chutney-testing/chutney/pull/316) ([nbrouand](https://github.com/nbrouand))


## [1.2.16](https://github.com/chutney-testing/chutney/tree/1.2.16)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.15...1.2.16)

**Implemented enhancements:**

- 🚀 | Task http-soap : add http headers parameter [\#364](https://github.com/chutney-testing/chutney/issues/364)

**Fixed bugs:**

- Campaign link on scenario history view does not work with Ctrl+click [\#345](https://github.com/chutney-testing/chutney/issues/345)

**Closed issues:**

- Add templates for issues and PRs [\#347](https://github.com/chutney-testing/chutney/issues/347)

**Merged pull requests:**

- bugfix\(ui\): remove double dash when selecting an execution [\#366](https://github.com/chutney-testing/chutney/pull/366) ([nbrouand](https://github.com/nbrouand))
- feat\(task-impl\): allow add headers to http soap task [\#363](https://github.com/chutney-testing/chutney/pull/363) ([rbenyoussef](https://github.com/rbenyoussef))
- chore\(deps\): Bump maven-surefire-plugin from 3.0.0-M4 to 3.0.0-M5 [\#360](https://github.com/chutney-testing/chutney/pull/360) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps-dev\): Bump activemq.version from 5.16.0 to 5.16.1 [\#359](https://github.com/chutney-testing/chutney/pull/359) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump mongodb.version from 3.8.0 to 3.12.8 [\#358](https://github.com/chutney-testing/chutney/pull/358) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump build-helper-maven-plugin from 3.0.0 to 3.2.0 [\#357](https://github.com/chutney-testing/chutney/pull/357) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- feat\(ui\): preview scenario error on campaign report [\#356](https://github.com/chutney-testing/chutney/pull/356) ([rbenyoussef](https://github.com/rbenyoussef))
- Added issue and PR template [\#350](https://github.com/chutney-testing/chutney/pull/350) ([96RadhikaJadhav](https://github.com/96RadhikaJadhav))


## [1.2.15](https://github.com/chutney-testing/chutney/tree/1.2.15)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.14...1.2.15)

**Implemented enhancements:**

- Add jira feature [\#339](https://github.com/chutney-testing/chutney/issues/339)

**Fixed bugs:**

- Campaign link on scenario history view does not work with Ctrl+click [\#345](https://github.com/chutney-testing/chutney/issues/345)
- Infinite loop on selenium zk By [\#340](https://github.com/chutney-testing/chutney/issues/340)

**Merged pull requests:**

- doc: Update Readme with Kotlin DSL & Discussions [\#353](https://github.com/chutney-testing/chutney/pull/353) ([bessonm](https://github.com/bessonm))
- bugfix\(task-impl\): infinite loop on Selenium ZK By \(\#340\) [\#351](https://github.com/chutney-testing/chutney/pull/351) ([rbenyoussef](https://github.com/rbenyoussef))
- Feat/add jira feature [\#349](https://github.com/chutney-testing/chutney/pull/349) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(ui\): Missing hash in URLs on scenario exec page [\#348](https://github.com/chutney-testing/chutney/pull/348) ([bessonm](https://github.com/bessonm))


## [1.2.14](https://github.com/chutney-testing/chutney/tree/1.2.14)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.13...1.2.14)

**Implemented enhancements:**

- Importing an environment [\#164](https://github.com/chutney-testing/chutney/issues/164)
- Glacio - Write scenario as feature file [\#144](https://github.com/chutney-testing/chutney/issues/144)

**Fixed bugs:**

- Scenarios executions in campaign report have random order [\#72](https://github.com/chutney-testing/chutney/issues/72)

**Merged pull requests:**

- refacto\(glacio-adapter\): Refacto to expose dto and not domain [\#343](https://github.com/chutney-testing/chutney/pull/343) ([nbrouand](https://github.com/nbrouand))
- server: fix dataset iterations bug [\#336](https://github.com/chutney-testing/chutney/pull/336) ([rbenyoussef](https://github.com/rbenyoussef))
- Chore/refacto env [\#325](https://github.com/chutney-testing/chutney/pull/325) ([nbrouand](https://github.com/nbrouand))
- chore\(deps\): Bump picocli from 4.2.0 to 4.6.1 [\#319](https://github.com/chutney-testing/chutney/pull/319) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump groovy from 2.5.0 to 2.5.14 [\#315](https://github.com/chutney-testing/chutney/pull/315) ([dependabot[bot]](https://github.com/apps/dependabot))
- chore\(\): Update timeout for old slow computers [\#314](https://github.com/chutney-testing/chutney/pull/314) ([nbrouand](https://github.com/nbrouand))
- chore\(\): remove junit4 [\#312](https://github.com/chutney-testing/chutney/pull/312) ([nbrouand](https://github.com/nbrouand))
- Chore/release github action [\#311](https://github.com/chutney-testing/chutney/pull/311) ([nbrouand](https://github.com/nbrouand))
- chore\(\): githubaction build [\#307](https://github.com/chutney-testing/chutney/pull/307) ([nbrouand](https://github.com/nbrouand))
- feat: Use glacio lang [\#94](https://github.com/chutney-testing/chutney/pull/94) ([bessonm](https://github.com/bessonm))


## [1.2.13](https://github.com/chutney-testing/chutney/tree/1.2.13)

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

- Bugfix/flaky tests [\#304](https://github.com/chutney-testing/chutney/pull/304) ([bessonm](https://github.com/bessonm))
- feat\(engine+server+ui\): Add output step result in report [\#301](https://github.com/chutney-testing/chutney/pull/301) ([nbrouand](https://github.com/nbrouand))
- feat\(task-impl\): Add placeholder on xml assert task [\#298](https://github.com/chutney-testing/chutney/pull/298) ([nbrouand](https://github.com/nbrouand))
- bugfix\(task-impl\): consume kafka with duplicated header \(\#296\) [\#297](https://github.com/chutney-testing/chutney/pull/297) ([rbenyoussef](https://github.com/rbenyoussef))
- fix\(ui\): Component edition : Apply tag filter on drag and drop model [\#295](https://github.com/chutney-testing/chutney/pull/295) ([boddissattva](https://github.com/boddissattva))
- fix\(ui\): Override completely history executions on refresh [\#293](https://github.com/chutney-testing/chutney/pull/293) ([boddissattva](https://github.com/boddissattva))
- chore\(deps\): Bump maven-failsafe-plugin from 3.0.0-M4 to 3.0.0-M5 [\#291](https://github.com/chutney-testing/chutney/pull/291) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump rxjava from 2.2.6 to 2.2.20 [\#290](https://github.com/chutney-testing/chutney/pull/290) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps-dev\): Bump rabbitmq-mock from 1.0.14 to 1.1.1 [\#288](https://github.com/chutney-testing/chutney/pull/288) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bugfix/dataset iterations [\#286](https://github.com/chutney-testing/chutney/pull/286) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(ui\): Wrong linkifier label [\#281](https://github.com/chutney-testing/chutney/pull/281) ([bessonm](https://github.com/bessonm))

## [1.2.12](https://github.com/chutney-testing/chutney/tree/1.2.12)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.11...1.2.12)

**Merged pull requests:**

- bugfix\(server\): wrong generated iteration [\#285](https://github.com/chutney-testing/chutney/pull/285) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.2.11](https://github.com/chutney-testing/chutney/tree/1.2.11)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.10...1.2.11)

**Merged pull requests:**

- bugfix\(server\): indexed output combined to external multivalues dataset for step iteration [\#284](https://github.com/chutney-testing/chutney/pull/284) ([rbenyoussef](https://github.com/rbenyoussef))


## [1.2.10](https://github.com/chutney-testing/chutney/tree/1.2.10)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.9...1.2.10)

**Implemented enhancements:**

- Schedule campagne on a specific day [\#266](https://github.com/chutney-testing/chutney/issues/266)

**Fixed bugs:**

- NPE in ComposedTestCaseIterationsPreProcessor.indexInputs [\#280](https://github.com/chutney-testing/chutney/issues/280)
- Not redirect to page requested when disconnected [\#278](https://github.com/chutney-testing/chutney/issues/278)
- Composed testcase : Cannot instantiate context-put task in some campaign executions [\#275](https://github.com/chutney-testing/chutney/issues/275)

**Merged pull requests:**

- bugfix\(server\): NPE in ComposedTestCaseIterationsPreProcessor [\#283](https://github.com/chutney-testing/chutney/pull/283) ([nbrouand](https://github.com/nbrouand))
- fix\(ui\): Keep asked url when redirecting to login [\#282](https://github.com/chutney-testing/chutney/pull/282) ([boddissattva](https://github.com/boddissattva))
- feat\(ui+server\): Schedule campaign on a specific time \#266 [\#277](https://github.com/chutney-testing/chutney/pull/277) ([nbrouand](https://github.com/nbrouand))
- Raw implementation mapper thread safety [\#276](https://github.com/chutney-testing/chutney/pull/276) ([boddissattva](https://github.com/boddissattva))

## [1.2.9](https://github.com/chutney-testing/chutney/tree/1.2.9)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.8...1.2.9)

**Closed issues:**

- dataset iterations output overided by last iteration output  [\#242](https://github.com/chutney-testing/chutney/issues/242)

**Merged pull requests:**

- feat\(server\): Step iteration [\#261](https://github.com/chutney-testing/chutney/pull/261) ([bessonm](https://github.com/bessonm))

## [1.2.8](https://github.com/chutney-testing/chutney/tree/1.2.8)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.7...1.2.8)

**Fixed bugs:**

- Session expire too fast \(\<10 min\) [\#249](https://github.com/chutney-testing/chutney/issues/249)

**Closed issues:**

- Scheduled campaigns between 00:00 and 00:10 aren't executed [\#264](https://github.com/chutney-testing/chutney/issues/264)
- Add error message when component save is ko [\#250](https://github.com/chutney-testing/chutney/issues/250)

**Merged pull requests:**

- Fix/component implementation mapping [\#268](https://github.com/chutney-testing/chutney/pull/268) ([boddissattva](https://github.com/boddissattva))
- fix scheduled campaigns execution at midnight [\#267](https://github.com/chutney-testing/chutney/pull/267) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(task-impl\): Allow bad content type in message received [\#265](https://github.com/chutney-testing/chutney/pull/265) ([nbrouand](https://github.com/nbrouand))
- Remove Lucene dependecies and point to travis.com [\#263](https://github.com/chutney-testing/chutney/pull/263) ([boddissattva](https://github.com/boddissattva))
- Add component duplication [\#262](https://github.com/chutney-testing/chutney/pull/262) ([boddissattva](https://github.com/boddissattva))
- Session management with and without anonymous user [\#260](https://github.com/chutney-testing/chutney/pull/260) ([boddissattva](https://github.com/boddissattva))
- Refactor/split composable step repo [\#259](https://github.com/chutney-testing/chutney/pull/259) ([bessonm](https://github.com/bessonm))


## [1.2.7](https://github.com/chutney-testing/chutney/tree/1.2.7)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.6...1.2.7)

**Merged pull requests:**

- feat\(server\): Make number of parrelel thread fixed to 20 [\#258](https://github.com/chutney-testing/chutney/pull/258) ([nbrouand](https://github.com/nbrouand))
- feat\(server\): fix gauge update, add status on scenario\_execution\_time… [\#257](https://github.com/chutney-testing/chutney/pull/257) ([nbrouand](https://github.com/nbrouand))
- Add edition information for concurrency edition check [\#256](https://github.com/chutney-testing/chutney/pull/256) ([boddissattva](https://github.com/boddissattva))
- chore\(deps-dev\): Bump JUnitParams from 1.1.0 to 1.1.1 [\#251](https://github.com/chutney-testing/chutney/pull/251) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [1.2.6](https://github.com/chutney-testing/chutney/tree/1.2.6)

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

- feat: Change micrometer metrics [\#248](https://github.com/chutney-testing/chutney/pull/248) ([nbrouand](https://github.com/nbrouand))
- Kafka consume task - Add xml payload processing [\#247](https://github.com/chutney-testing/chutney/pull/247) ([boddissattva](https://github.com/boddissattva))
- Feat/navigation patterns [\#244](https://github.com/chutney-testing/chutney/pull/244) ([bessonm](https://github.com/bessonm))
- Micrometer tasks [\#241](https://github.com/chutney-testing/chutney/pull/241) ([boddissattva](https://github.com/boddissattva))
- chore\(ui\): Update node version to v12.18.4 & provide IntelliJ run conf [\#238](https://github.com/chutney-testing/chutney/pull/238) ([bessonm](https://github.com/bessonm))



## [1.2.5](https://github.com/chutney-testing/chutney/tree/1.2.5) (2020-09-01)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.4...1.2.5)

**Implemented enhancements:**

- Do not break on generated step iterations [\#213](https://github.com/chutney-testing/chutney/issues/213)
- Choose which variable to display with the debug task [\#209](https://github.com/chutney-testing/chutney/issues/209)
- Add audit on execution request [\#116](https://github.com/chutney-testing/chutney/issues/116)

**Closed issues:**

- Strategy retry [\#92](https://github.com/chutney-testing/chutney/issues/92)

**Merged pull requests:**

- Allow ldap and inmemory authentication [\#232](https://github.com/chutney-testing/chutney/pull/232) ([boddissattva](https://github.com/boddissattva))
- feat\(ui/server\): Notify campaign execution status to xray/jira [\#231](https://github.com/chutney-testing/chutney/pull/231) ([bessonm](https://github.com/bessonm))
- Feat/export import environment [\#230](https://github.com/chutney-testing/chutney/pull/230) ([TuLinhNGUYEN](https://github.com/TuLinhNGUYEN))
- fix : Unsecure api for development [\#229](https://github.com/chutney-testing/chutney/pull/229) ([boddissattva](https://github.com/boddissattva))
- fix\(engine\): fix nested retry strategy [\#228](https://github.com/chutney-testing/chutney/pull/228) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(server+engine\): do not break on generated step iterations [\#227](https://github.com/chutney-testing/chutney/pull/227) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(task-impl\): create new json asserter \(lessThan and GreaterThan\) [\#226](https://github.com/chutney-testing/chutney/pull/226) ([TuLinhNGUYEN](https://github.com/TuLinhNGUYEN))
- Ordering for inputs/outputs/parameters and fix debug task with filters parameter [\#225](https://github.com/chutney-testing/chutney/pull/225) ([boddissattva](https://github.com/boddissattva))
- chore\(deps\): Bump jaxb2-maven-plugin from 2.3.1 to 2.5.0 [\#224](https://github.com/chutney-testing/chutney/pull/224) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump maven-jar-plugin from 3.0.2 to 3.2.0 [\#223](https://github.com/chutney-testing/chutney/pull/223) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps-dev\): Bump activemq.version from 5.15.11 to 5.16.0 [\#222](https://github.com/chutney-testing/chutney/pull/222) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump postgresql from 42.2.6 to 42.2.16 [\#221](https://github.com/chutney-testing/chutney/pull/221) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- feat\(task-impl\): Filter scenario context with debug task [\#216](https://github.com/chutney-testing/chutney/pull/216) ([bessonm](https://github.com/bessonm))
- feat\(server+ui\): Register user on api actions [\#187](https://github.com/chutney-testing/chutney/pull/187) ([rbenyoussef](https://github.com/rbenyoussef))

## [1.2.4](https://github.com/chutney-testing/chutney/tree/1.2.4) (2020-08-18)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.3...1.2.4)

**Merged pull requests:**

- fix\(task-impl\): Use ZoneDateTime for json-assert on dates [\#218](https://github.com/chutney-testing/chutney/pull/218) ([boddissattva](https://github.com/boddissattva))
- Feat/improve report ui [\#215](https://github.com/chutney-testing/chutney/pull/215) ([bessonm](https://github.com/bessonm))

## [1.2.3](https://github.com/chutney-testing/chutney/tree/1.2.3) (2020-08-04)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.2...1.2.3)

**Fixed bugs:**

- Search using Ctrl-F was lost in ace editor component
- Observe automatically a scenario's execution when it does not have parameters

**Closed issues:**

- Scenario Edition - Cannot use Ctrl-F to find content in editor [\#203](https://github.com/chutney-testing/chutney/issues/203)
- Scenario execution - Observation is not automatic for scenarios without parameters [\#204](https://github.com/chutney-testing/chutney/issues/204)

**Merged pull requests:**

- Version 1.2.2 : ui bugs [\#205](https://github.com/chutney-testing/chutney/pull/205) ([boddissattva](https://github.com/boddissattva))

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

- Fix/189 target whitout port [\#197](https://github.com/chutney-testing/chutney/pull/197) ([boddissattva](https://github.com/boddissattva))
- Add option to acknowledge only messages matching selector, true by default [\#196](https://github.com/chutney-testing/chutney/pull/196) ([PKode](https://github.com/PKode))
- Fix/191 : Scenario executions [\#195](https://github.com/chutney-testing/chutney/pull/195) ([boddissattva](https://github.com/boddissattva))
- feat/extend\_composable\_testcase\_dataset\_management [\#192](https://github.com/chutney-testing/chutney/pull/192) ([boddissattva](https://github.com/boddissattva))
- feat\(task\_impl\): Add placeholder for assert in  JsonTask [\#188](https://github.com/chutney-testing/chutney/pull/188) ([nbrouand](https://github.com/nbrouand))
- feat\(ui\): Stop campaign bug.  [\#185](https://github.com/chutney-testing/chutney/pull/185) ([nbrouand](https://github.com/nbrouand))
- bugfix\(ui + server\): Raise error when asking for report not corresponding to the scenarioId [\#178](https://github.com/chutney-testing/chutney/pull/178) ([nbrouand](https://github.com/nbrouand))
- Bugfix/aceeditor [\#177](https://github.com/chutney-testing/chutney/pull/177) ([nbrouand](https://github.com/nbrouand))
- bugfix\(task-impl\): Prevent NPE on null value + pretty log for most usual type [\#176](https://github.com/chutney-testing/chutney/pull/176) ([nbrouand](https://github.com/nbrouand))
- chore\(ui\): Update node version to new LTS v12.18.0 [\#172](https://github.com/chutney-testing/chutney/pull/172) ([bessonm](https://github.com/bessonm))

## [1.2.1](https://github.com/chutney-testing/chutney/tree/1.2.1) (2020-06-16)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.2.0...1.2.1)

**Fixed bugs:**

- Wrong last execution order on scenarii list  [\#149](https://github.com/chutney-testing/chutney/issues/149)
- bugfix\(ui\): Correctly order scenarios by last execution when one is not executed [\#174](https://github.com/chutney-testing/chutney/pull/174) ([bessonm](https://github.com/bessonm))
- bugfix\(engine\): Parsing full objects in spel did not work \(introduced in pr134\) [\#173](https://github.com/chutney-testing/chutney/pull/173) ([nbrouand](https://github.com/nbrouand))

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

- feat\(ui\): Add current scenario read-only on execution page [\#148](https://github.com/chutney-testing/chutney/pull/148) ([nbrouand](https://github.com/nbrouand))
- feat\(engine\): Allow to stop step in a retry strategy [\#146](https://github.com/chutney-testing/chutney/pull/146) ([nbrouand](https://github.com/nbrouand))
- Feat/nice campaign history [\#145](https://github.com/chutney-testing/chutney/pull/145) ([nbrouand](https://github.com/nbrouand))
- feat\(ui\):Add chart for campaign report [\#143](https://github.com/chutney-testing/chutney/pull/143) ([nbrouand](https://github.com/nbrouand))
- refactor\(ui\): fix typo [\#139](https://github.com/chutney-testing/chutney/pull/139) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(ui+core\): Remove restriction to execute a scenario already running [\#138](https://github.com/chutney-testing/chutney/pull/138) ([nbrouand](https://github.com/nbrouand))
- feat\(ui\): Sort campaign report scenarios by properties [\#137](https://github.com/chutney-testing/chutney/pull/137) ([bessonm](https://github.com/bessonm))
- Chore/comm [\#136](https://github.com/chutney-testing/chutney/pull/136) ([nbrouand](https://github.com/nbrouand))
- bugfix\(engine\): issue with spel [\#134](https://github.com/chutney-testing/chutney/pull/134) ([rbenyoussef](https://github.com/rbenyoussef))
- chore\(deps\): Bump sshj from 0.26.0 to 0.27.0 [\#130](https://github.com/chutney-testing/chutney/pull/130) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump jool from 0.9.12 to 0.9.14 [\#129](https://github.com/chutney-testing/chutney/pull/129) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump wiremock-standalone from 2.19.0 to 2.26.3 [\#128](https://github.com/chutney-testing/chutney/pull/128) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump orientdb.version from 3.0.28 to 3.0.30 [\#127](https://github.com/chutney-testing/chutney/pull/127) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump maven-source-plugin from 3.2.0 to 3.2.1 [\#126](https://github.com/chutney-testing/chutney/pull/126) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- bugfix\(ui\): show replay button for stopped campaign [\#125](https://github.com/chutney-testing/chutney/pull/125) ([rbenyoussef](https://github.com/rbenyoussef))
- feat\(ui\): Bookmark scenarios search filters [\#124](https://github.com/chutney-testing/chutney/pull/124) ([bessonm](https://github.com/bessonm))
- refactor\(core/engine\): Fix typo [\#123](https://github.com/chutney-testing/chutney/pull/123) ([bessonm](https://github.com/bessonm))
- chore\(\): Add generate changelog in Contributing.md [\#122](https://github.com/chutney-testing/chutney/pull/122) ([nbrouand](https://github.com/nbrouand))


## [1.1.4](https://github.com/chutney-testing/chutney/tree/1.1.4) (2020-04-30)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.3...1.1.4)

**Implemented enhancements:**

- Add environment name to context and global variable [\#19](https://github.com/chutney-testing/chutney/issues/19)

**Merged pull requests:**

- bugfix: Task using isPresent [\#121](https://github.com/chutney-testing/chutney/pull/121) ([bessonm](https://github.com/bessonm))
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
- feat\(ui\): Show number of passed/failed scenarios on campaign report [\#114](https://github.com/chutney-testing/chutney/pull/114) ([bessonm](https://github.com/bessonm))
- Bugfix/alert on globalvar deletion [\#109](https://github.com/chutney-testing/chutney/pull/109) ([bessonm](https://github.com/bessonm))
- fix\(ui\): Component edition - Must recreate steps parameter values [\#108](https://github.com/chutney-testing/chutney/pull/108) ([boddissattva](https://github.com/boddissattva))
- Feat/ui improve scenario execution report [\#105](https://github.com/chutney-testing/chutney/pull/105) ([rbenyoussef](https://github.com/rbenyoussef))
- bugfix\(ui\): Sort target and environment names [\#104](https://github.com/chutney-testing/chutney/pull/104) ([bessonm](https://github.com/bessonm))
- bugfix\(ui\): Show scenario description without truncation [\#103](https://github.com/chutney-testing/chutney/pull/103) ([bessonm](https://github.com/bessonm))
- Feat/ui display improvement [\#102](https://github.com/chutney-testing/chutney/pull/102) ([rbenyoussef](https://github.com/rbenyoussef))
- Fix/86 [\#93](https://github.com/chutney-testing/chutney/pull/93) ([boddissattva](https://github.com/boddissattva))
- Tech/clean engine target model [\#89](https://github.com/chutney-testing/chutney/pull/89) ([bessonm](https://github.com/bessonm))
- Fix \#85 [\#88](https://github.com/chutney-testing/chutney/pull/88) ([boddissattva](https://github.com/boddissattva))
- bugfix\(ui\): Add alert when leaving component scenario edition with mo… [\#87](https://github.com/chutney-testing/chutney/pull/87) ([nbrouand](https://github.com/nbrouand))
- Tech/reduce cli coupling [\#84](https://github.com/chutney-testing/chutney/pull/84) ([bessonm](https://github.com/bessonm))
- feat\(ui\): Add stop label for campaign execution report summarize [\#78](https://github.com/chutney-testing/chutney/pull/78) ([nbrouand](https://github.com/nbrouand))
- chore\(ui\): Add a test on scenarii.component.spec.ts [\#77](https://github.com/chutney-testing/chutney/pull/77) ([nbrouand](https://github.com/nbrouand))
- chore\(ui\): Add some ng test [\#76](https://github.com/chutney-testing/chutney/pull/76) ([nbrouand](https://github.com/nbrouand))

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

- Feature/add kafka basic consume task [\#68](https://github.com/chutney-testing/chutney/pull/68) ([bessonm](https://github.com/bessonm))
- feat\(core + ui\): Add environment info to scenario execution history [\#66](https://github.com/chutney-testing/chutney/pull/66) ([nbrouand](https://github.com/nbrouand))
- Avoid NullPointerException with message having null value as header [\#65](https://github.com/chutney-testing/chutney/pull/65) ([GeVa2072](https://github.com/GeVa2072))
- feat\(ui\): In campaign edition, cancel redirect to edited campaign [\#64](https://github.com/chutney-testing/chutney/pull/64) ([nbrouand](https://github.com/nbrouand))
- bugfix\(server\): uppercase and trim tags at saved [\#62](https://github.com/chutney-testing/chutney/pull/62) ([nbrouand](https://github.com/nbrouand))

## [1.1.1](https://github.com/chutney-testing/chutney/tree/1.1.1) (2020-03-10)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.1.0...1.1.1)

**Merged pull requests:**

- bugfix\(ui\): NPE on saveComponent + Add more ui coherence between comp… [\#61](https://github.com/chutney-testing/chutney/pull/61) ([nbrouand](https://github.com/nbrouand))
- chore: Share Intellij run config [\#57](https://github.com/chutney-testing/chutney/pull/57) ([bessonm](https://github.com/bessonm))

## [1.1.0](https://github.com/chutney-testing/chutney/tree/1.1.0) (2020-03-06)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/1.0.0...1.1.0)

**Implemented enhancements:**

- Font size smaller on campaign page  [\#18](https://github.com/chutney-testing/chutney/issues/18)

**Fixed bugs:**

- Navigation between component sometime broken [\#24](https://github.com/chutney-testing/chutney/issues/24)
- Pop up should not appear on scenario execution page [\#16](https://github.com/chutney-testing/chutney/issues/16)
- Id not reload when creating component [\#15](https://github.com/chutney-testing/chutney/issues/15)

**Merged pull requests:**

- feat\(core\): Parameterized component strategies [\#60](https://github.com/chutney-testing/chutney/pull/60) ([bessonm](https://github.com/bessonm))
- Fix/parameters escaping [\#56](https://github.com/chutney-testing/chutney/pull/56) ([boddissattva](https://github.com/boddissattva))
- bugfix\(ui\): Fix broken redirection on child component [\#55](https://github.com/chutney-testing/chutney/pull/55) ([nbrouand](https://github.com/nbrouand))
- bugfix\(ui\): Remove canDeactivate guard on execution history page [\#54](https://github.com/chutney-testing/chutney/pull/54) ([nbrouand](https://github.com/nbrouand))
- Tech/clean up [\#53](https://github.com/chutney-testing/chutney/pull/53) ([bessonm](https://github.com/bessonm))
- feat\(ui\): Reduce font size on campaign page [\#52](https://github.com/chutney-testing/chutney/pull/52) ([nbrouand](https://github.com/nbrouand))
- Chore/travis zulip hook [\#50](https://github.com/chutney-testing/chutney/pull/50) ([bessonm](https://github.com/bessonm))
- chore\(deps\): Bump picocli from 3.9.0 to 4.2.0 [\#49](https://github.com/chutney-testing/chutney/pull/49) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps-dev\): Bump rabbitmq-mock from 1.0.4 to 1.0.14 [\#48](https://github.com/chutney-testing/chutney/pull/48) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump build-helper-maven-plugin from 1.12 to 3.0.0 [\#47](https://github.com/chutney-testing/chutney/pull/47) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps\): Bump maven-clean-plugin from 3.0.0 to 3.1.0 [\#46](https://github.com/chutney-testing/chutney/pull/46) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- chore\(deps-dev\): Bump activemq.version from 5.15.0 to 5.15.11 [\#45](https://github.com/chutney-testing/chutney/pull/45) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- doc: Update release management [\#43](https://github.com/chutney-testing/chutney/pull/43) ([bessonm](https://github.com/bessonm))
- chore: Update project info [\#41](https://github.com/chutney-testing/chutney/pull/41) ([bessonm](https://github.com/bessonm))
- fix\(core+ui\): Set id on newly created component. [\#37](https://github.com/chutney-testing/chutney/pull/37) ([boddissattva](https://github.com/boddissattva))

## [1.0.0](https://github.com/chutney-testing/chutney/tree/1.0.0) (2020-02-26)

[Full Changelog](https://github.com/chutney-testing/chutney/compare/d734d7933351ed031c3c598b0a2de7098153b42f...1.0.0)

**Fixed bugs:**

- Global var character escaped twice [\#9](https://github.com/chutney-testing/chutney/issues/9)

**Closed issues:**

- Add a new task to consume message from a kafka topic [\#39](https://github.com/chutney-testing/chutney/issues/39)

**Merged pull requests:**

- chore\(ui\): Generate sources archive [\#40](https://github.com/chutney-testing/chutney/pull/40) ([boddissattva](https://github.com/boddissattva))
- Bugfix/escaped char [\#36](https://github.com/chutney-testing/chutney/pull/36) ([nbrouand](https://github.com/nbrouand))
- Revert "Ignore IntelliJ root configuration files" [\#35](https://github.com/chutney-testing/chutney/pull/35) ([nbrouand](https://github.com/nbrouand))
- chore: Licence compliance with Fossa [\#34](https://github.com/chutney-testing/chutney/pull/34) ([bessonm](https://github.com/bessonm))
- Add randomLong Generator [\#33](https://github.com/chutney-testing/chutney/pull/33) ([GeVa2072](https://github.com/GeVa2072))
- Fix use of == instead of equals method [\#32](https://github.com/chutney-testing/chutney/pull/32) ([GeVa2072](https://github.com/GeVa2072))
- chore: Add Travis deployment. Fix project version. [\#14](https://github.com/chutney-testing/chutney/pull/14) ([boddissattva](https://github.com/boddissattva))
- Fix typo in Selenium documentation snippet [\#13](https://github.com/chutney-testing/chutney/pull/13) ([ledoyen](https://github.com/ledoyen))
- Fix Finally Action target mapping error [\#12](https://github.com/chutney-testing/chutney/pull/12) ([ledoyen](https://github.com/ledoyen))
- Allow use of external npm installation [\#11](https://github.com/chutney-testing/chutney/pull/11) ([ledoyen](https://github.com/ledoyen))
- Ignore IntelliJ root configuration files [\#10](https://github.com/chutney-testing/chutney/pull/10) ([ledoyen](https://github.com/ledoyen))
- chore: Add to Travis build JDK version and install command [\#4](https://github.com/chutney-testing/chutney/pull/4) ([nbrouand](https://github.com/nbrouand))
- Bump npm from 5.8.0 to 6.13.4 in /ui [\#2](https://github.com/chutney-testing/chutney/pull/2) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump amqp-client from 5.3.0 to 5.8.0 [\#1](https://github.com/chutney-testing/chutney/pull/1) ([dependabot[bot]](https://github.com/apps/dependabot))

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
